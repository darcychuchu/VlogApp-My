package com.vlog.my.screens.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.vlog.my.data.LocalDataHelper
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
import kotlin.math.min

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val scriptsDataHelper: SubScriptsDataHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "SettingsViewModel"
        private const val WORK_NAME = "stories_refresh"
    }

    private val workManager = WorkManager.getInstance(context)
    
    private val _workSettings = MutableStateFlow<Workers?>(null)
    val workSettings: StateFlow<Workers?> = _workSettings.asStateFlow()

//    private val _performanceTestState = MutableStateFlow<PerformanceTestState>(PerformanceTestState.Idle)
//    val performanceTestState: StateFlow<PerformanceTestState> = _performanceTestState.asStateFlow()

    init {
        loadWorkSettings()
        observeWorkStatus()
    }

    private fun loadWorkSettings() {
        viewModelScope.launch {
            val settings = scriptsDataHelper.getWorkersByName(WORK_NAME)
            _workSettings.value = settings ?: Workers(
                name = WORK_NAME,
                api = "/api/stories",
                version = 0,
                isEnabled = 0,
                isValued = 15,
                refreshCount = 0,
                lastRefreshTime = 0
            )
            Log.d(TAG, "Loaded settings: ${settings?.isEnabled}, ${settings?.isValued}min")
        }
    }

    fun updateWorkSettings(isEnabled: Boolean, intervalMinutes: Int) {
        viewModelScope.launch {
            Log.d(TAG, "Updating settings: enabled=$isEnabled, interval=${intervalMinutes}min")
            
            // 首先取消现有的工作
            cancelWork()
            
            val currentSettings = _workSettings.value ?: Workers(name = WORK_NAME)
            val newSettings = currentSettings.copy(
                isEnabled = if (isEnabled) 1 else 0,
                isValued = intervalMinutes,
                lastRefreshTime = System.currentTimeMillis()
            )
            scriptsDataHelper.updateWorkers(newSettings)
            _workSettings.value = newSettings

            if (isEnabled) {
                scheduleWork(intervalMinutes)
            }
        }
    }

    private fun scheduleWork(intervalMinutes: Int) {
        Log.d(TAG, "Scheduling work with interval: ${intervalMinutes}min")

        // 移除网络约束，使任务可以在没有网络时也能执行
        val constraints = Constraints.Builder()
            .build()

        // 创建一次性工作请求，带延迟
        val oneTimeRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
            .setConstraints(constraints)
            .addTag(WORK_NAME)
            .build()

        // 使用唯一工作请求，确保只有一个实例在运行
        workManager.enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            oneTimeRequest
        )

        Log.d(TAG, "Work scheduled: ${oneTimeRequest.id}")
    }

    private fun cancelWork() {
        Log.d(TAG, "Cancelling all work")
        workManager.cancelUniqueWork(WORK_NAME)
    }

    private fun observeWorkStatus() {
        // 观察工作状态
        workManager.getWorkInfosByTagLiveData(WORK_NAME)
            .observeForever { workInfos ->
                workInfos?.forEach { workInfo ->
                    Log.d(TAG, "Work status: ${workInfo.state}")
                    if (workInfo.state.isFinished) {
                        // 当工作完成时，检查是否需要调度下一次工作
                        val settings = _workSettings.value
                        if (settings?.isEnabled == 1) {
                            // 重新调度下一次工作
                            val intervalMinutes = settings.isValued
                            Log.d(TAG, "Scheduling next work in $intervalMinutes minutes")
                            
                            val nextRequest = OneTimeWorkRequestBuilder<UpdateWorker>()
                                .setInitialDelay(intervalMinutes.toLong(), TimeUnit.MINUTES)
                                .addTag(WORK_NAME)
                                .build()

                            workManager.enqueueUniqueWork(
                                WORK_NAME,
                                ExistingWorkPolicy.REPLACE,
                                nextRequest
                            )
                        }
                        loadWorkSettings() // 刷新设置
                    }
                }
            }
    }

//    fun performanceBatchInsert(count: Int) {
//        viewModelScope.launch {
//            try {
//                _performanceTestState.value = PerformanceTestState.Running(0)
//
//                withContext(Dispatchers.IO) {
//                    val startTime = System.currentTimeMillis()
//                    val batchSize = 100
//                    var processed = 0
//
//                    while (processed < count) {
//                        val currentBatchSize = min(batchSize, count - processed)
//                        val settingsList = (processed until processed + currentBatchSize).map { index ->
//                            Workers(
//                                name = "test_setting_$index",
//                                api = "/api/test/$index",
//                                version = index,
//                                isEnabled = 1,
//                                isValued = (index % 60) + 1,
//                                refreshCount = index,
//                                lastRefreshTime = System.currentTimeMillis(),
//                                updatedAt = System.currentTimeMillis()
//                            )
//                        }
//
//                        localDataHelper.batchInsertWorkers(settingsList)
//                        processed += currentBatchSize
//
////                        val progress = (processed.toFloat() / count * 100).toInt()
////                        _performanceTestState.value = PerformanceTestState.Running(progress)
//                    }
//
////                    val endTime = System.currentTimeMillis()
////                    val duration = endTime - startTime
////
////                    _performanceTestState.value = PerformanceTestState.Completed(
////                        count = count,
////                        durationMs = duration
////                    )
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Performance test failed", e)
//                _performanceTestState.value = PerformanceTestState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }

    override fun onCleared() {
        super.onCleared()
        workManager.getWorkInfosByTagLiveData(WORK_NAME).removeObserver { }
    }
}

//sealed class PerformanceTestState {
//    object Idle : PerformanceTestState()
//    data class Running(val progress: Int) : PerformanceTestState()
//    data class Completed(val count: Int, val durationMs: Long) : PerformanceTestState()
//    data class Error(val message: String) : PerformanceTestState()
//}
