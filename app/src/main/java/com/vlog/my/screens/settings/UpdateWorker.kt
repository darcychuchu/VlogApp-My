package com.vlog.my.screens.settings

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vlog.my.data.LocalDataHelper
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.stories.StoriesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltWorker
class UpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val storiesRepository: StoriesRepository,
    private val scriptsDataHelper: SubScriptsDataHelper,
    private val localDataHelper: LocalDataHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "UpdateWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            Log.d(TAG, "=== Work execution started ===")
            Log.d(TAG, "Start time: ${dateFormat.format(Date(startTime))}")
            Log.d(TAG, "Work ID: ${this@UpdateWorker.id}")
            Log.d(TAG, "Run attempt: $runAttemptCount ")

            // 获取当前设置
            val settings = scriptsDataHelper.getWorkersByName("stories_refresh")
            Log.d(
                TAG,
                "Current settings: enabled=${settings?.isEnabled}, interval=${settings?.isValued}min, refreshCount=${settings?.refreshCount}"
            )

            if (settings?.isEnabled != 1) {
                Log.d(TAG, "Auto refresh is disabled, skipping work")
                return@withContext Result.success()
            }

            try {
                // 调用API获取最新数据
                Log.d(TAG, "Fetching new stories...")
                val response = storiesRepository.getStoriesList(typed = 0)

                if (response.code == 200 && response.data?.items != null) {
                    Log.d(TAG, "Received ${response.data.items.size} stories from API")

                    // 更新数据库
                    response.data.items.forEach { story ->
                        localDataHelper.insertStories(story)
                    }

                    // 更新统计信息
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    scriptsDataHelper.updateRefreshCount("stories_refresh")

                    // 验证更新是否成功
                    val updatedSettings = scriptsDataHelper.getWorkersByName("stories_refresh")
                    Log.d(
                        TAG,
                        "Updated settings - refresh count: ${updatedSettings?.refreshCount}, last refresh: ${
                            dateFormat.format(Date(updatedSettings?.lastRefreshTime ?: 0))
                        }"
                    )

                    Log.d(
                        TAG,
                        "Work completed successfully in ${duration}ms at: ${
                            dateFormat.format(
                                Date(endTime)
                            )
                        }"
                    )

                    // 记录下次执行时间
                    val nextRunTime = endTime + (settings.isValued * 60 * 1000)
                    Log.d(
                        TAG,
                        "Next execution scheduled for: ${dateFormat.format(Date(nextRunTime))}"
                    )
                    Log.d(TAG, "=== Work execution finished ===\n")

                    return@withContext Result.success()
                } else {
                    Log.e(
                        TAG,
                        "API request failed: code=${response.code}, message=${response.message}"
                    )
                    return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network error: ${e.message}", e)
                return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Worker error: ${e.message}", e)
            return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}