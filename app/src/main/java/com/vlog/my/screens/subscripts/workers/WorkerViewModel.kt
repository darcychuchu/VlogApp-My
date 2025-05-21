package com.vlog.my.screens.subscripts.workers

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import androidx.work.Data
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.model.Workers
import com.vlog.my.data.scripts.SubScriptsDataHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.collections.map
import kotlin.math.min
import kotlin.ranges.until

@HiltViewModel
class WorkerViewModel @Inject constructor(
    private val scriptsDataHelper: SubScriptsDataHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "WorkerViewModel"
        private const val WORK_NAME = "items_refresh"
    }
    
    // 当前正在操作的apiConfigId
    private var currentApiConfigId: String = ""

    private val workManager = WorkManager.getInstance(context)

    private val _subScripts = MutableStateFlow<SubScripts?>(null)
    val subScripts: StateFlow<SubScripts?> = _subScripts.asStateFlow()
    
    private val _workersSettings = MutableStateFlow<Workers?>(null)
    val workersSettings: StateFlow<Workers?> = _workersSettings.asStateFlow()

    private val _performanceTestState =
        MutableStateFlow<PerformanceTestState>(PerformanceTestState.Idle)
    val performanceTestState: StateFlow<PerformanceTestState> = _performanceTestState.asStateFlow()

    init {
        loadWorkSettings()
        observeWorkStatus()
    }

    private fun loadWorkSettings() {
        viewModelScope.launch {
            val workers = scriptsDataHelper.getWorkersByName(WORK_NAME)
            _workersSettings.value = workers ?: Workers(
                name = WORK_NAME,
                api = "workers",
                version = 0,
                isEnabled = 0,
                isValued = 15,
                refreshCount = 0,
                lastRefreshTime = 0,
                pageValued = 0
            )
            Log.d(TAG, "Loaded workers: ${workers?.isEnabled}, ${workers?.isValued}min")
        }
    }
    
    // 根据ApiConfig ID加载Workers配置
    fun loadWorkSettingsByApiConfigId(scriptId: String) {
        currentApiConfigId = scriptId
        viewModelScope.launch {
            val workName = if (scriptId.isEmpty()) WORK_NAME else "api_${scriptId}"
            _subScripts.value = scriptsDataHelper.getUserScriptsById(scriptId)
            val workers = scriptsDataHelper.getWorkersByName(workName)
            
            // 如果不存在，则创建一个新的Workers配置
            _workersSettings.value = workers ?: Workers(
                name = workName,
                api = scriptId,
                version = 0,
                isEnabled = 0,
                isValued = 15,
                refreshCount = 0,
                lastRefreshTime = 0,
                pageValued = 0
            )
            Log.d(TAG, "Loaded workers for API $scriptId: ${workers?.isEnabled}, ${workers?.isValued}min")
        }
    }

    fun updateWorkSettings(isEnabled: Boolean, intervalMinutes: Int, scriptId: String) {
        viewModelScope.launch {
            Log.d(TAG, "Updating workers: enabled=$isEnabled, interval=${intervalMinutes}min, apiConfigId=$scriptId")
            
            // 确定工作名称
            val workName = if (scriptId.isEmpty()) WORK_NAME else "api_${scriptId}"
            
            // 首先取消现有的工作
            cancelWork(workName)

            val currentWorkers = _workersSettings.value ?: Workers(name = workName)
            val updateWorker = currentWorkers.copy(
                name = workName,
                api = scriptId.ifEmpty { "workers" },
                isEnabled = if (isEnabled) 1 else 0,
                isValued = intervalMinutes,
                lastRefreshTime = System.currentTimeMillis()
            )
            scriptsDataHelper.updateWorkers(updateWorker)
            _workersSettings.value = updateWorker

            if (isEnabled) {
                scheduleWork(intervalMinutes, workName, scriptId)
            }
        }
    }

    private fun scheduleWork(intervalMinutes: Int, workName: String = WORK_NAME, scriptId: String) {
        Log.d(TAG, "Scheduling work with interval: ${intervalMinutes}min, workName: $workName, apiConfigId: $scriptId")

        // 移除网络约束，使任务可以在没有网络时也能执行
        val constraints = Constraints.Builder()
            .build()
            
        // 创建输入数据，包含apiConfigId
        val inputData = Data.Builder()
            .putString("apiConfigId", scriptId)
            .build()

        // 创建一次性工作请求，带延迟
        val oneTimeRequest = OneTimeWorkRequestBuilder<ExecutionWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .addTag(workName)
            .build()

        // 使用唯一工作请求，确保只有一个实例在运行
        workManager.enqueueUniqueWork(
            workName,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )

        Log.d(TAG, "Work scheduled: ${oneTimeRequest.id}")
    }

    private fun cancelWork(workName: String = WORK_NAME) {
        Log.d(TAG, "Cancelling work: $workName")
        workManager.cancelUniqueWork(workName)
    }

    private fun observeWorkStatus() {
        // 观察默认工作状态
        observeWorkStatusByName(WORK_NAME)
        
        // 观察所有API相关的工作状态
        viewModelScope.launch {
            val allWorkers = scriptsDataHelper.getAllWorkers()
            allWorkers.forEach { worker ->
                if (worker.name.startsWith("api_")) {
                    observeWorkStatusByName(worker.name)
                }
            }
        }
    }
    
    private fun observeWorkStatusByName(workName: String) {
        // 观察工作状态
        workManager.getWorkInfosByTagLiveData(workName)
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    Log.d(TAG, "Work status for $workName: ${workInfo.state}")
                    if (workInfo.state.isFinished) {
                        // 当工作完成时，检查是否需要调度下一次工作
                        val workers = if (workName == WORK_NAME) {
                            _workersSettings.value
                        } else {
                            scriptsDataHelper.getWorkersByName(workName)
                        }
                        
                        if (workers?.isEnabled == 1) {
                            // 重新调度下一次工作
                            val intervalMinutes = workers.isValued
                            val apiConfigId = workers.api ?: ""
                            Log.d(TAG, "Scheduling next work in $intervalMinutes minutes for $workName")
                            
                            // 创建输入数据，包含apiConfigId
                            val inputData = Data.Builder()
                                .putString("apiConfigId", apiConfigId)
                                .build()
                            
                            val nextRequest = OneTimeWorkRequestBuilder<ExecutionWorker>()
                                .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
                                .setInputData(inputData)
                                .addTag(workName)
                                .build()

                            workManager.enqueueUniqueWork(
                                workName,
                                ExistingWorkPolicy.REPLACE,
                                nextRequest
                            )
                        }
                        
                        // 如果是当前正在查看的Workers，则刷新设置
                        if (workName == WORK_NAME || (currentApiConfigId.isNotEmpty() && workName == "api_$currentApiConfigId")) {
                            if (workName == WORK_NAME) {
                                loadWorkSettings()
                            } else {
                                loadWorkSettingsByApiConfigId(currentApiConfigId)
                            }
                        }
                    }
                }
            }
    }

    fun performanceBatchInsert(count: Int) {
        viewModelScope.launch {
            try {
                _performanceTestState.value = PerformanceTestState.Running(0)

                withContext(Dispatchers.IO) {
                    val startTime = System.currentTimeMillis()
                    val batchSize = 100
                    var processed = 0

                    while (processed < count) {
                        val currentBatchSize = min(batchSize, count - processed)
                        val workersList =
                            (processed until processed + currentBatchSize).map { index ->
                                Workers(
                                    name = "test_worker_$index",
                                    api = "/api/test/$index",
                                    version = index,
                                    isEnabled = 1,
                                    isValued = (index % 60) + 1,
                                    refreshCount = index,
                                    lastRefreshTime = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                            }

                        scriptsDataHelper.batchInsertWorkers(workersList)
                        processed += currentBatchSize

                        val progress = (processed.toFloat() / count * 100).toInt()
                        _performanceTestState.value = PerformanceTestState.Running(progress)
                    }

                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    _performanceTestState.value = PerformanceTestState.Completed(
                        count = count,
                        durationMs = duration
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Performance test failed", e)
                _performanceTestState.value = PerformanceTestState.Error(e.message ?: "Unknown error")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        workManager.getWorkInfosByTagLiveData(WORK_NAME).removeObserver { }
    }
}

sealed class PerformanceTestState {
    object Idle : PerformanceTestState()
    data class Running(val progress: Int) : PerformanceTestState()
    data class Completed(val count: Int, val durationMs: Long) : PerformanceTestState()
    data class Error(val message: String) : PerformanceTestState()
}
