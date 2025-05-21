package com.vlog.my.screens.subscripts.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.vlog.my.data.LocalDataHelper
import com.vlog.my.data.scripts.SubScriptsDataHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import com.vlog.my.data.scripts.articles.ArticlesScriptsRepository
import com.vlog.my.data.scripts.articles.ArticlesMappingConfig
import com.vlog.my.data.scripts.articles.ArticlesScriptsDataHelper
import com.vlog.my.parser.ArticlesScriptParser
import org.json.JSONObject

@HiltWorker
class ExecutionWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val articlesScriptsRepository: ArticlesScriptsRepository,
    private val subScriptsDataHelper: SubScriptsDataHelper,
    private val articlesScriptsDataHelper: ArticlesScriptsDataHelper
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "ExecutionWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            // 获取apiConfigId参数
            val apiConfigId = inputData.getString("apiConfigId") ?: ""
            val workName = if (apiConfigId.isEmpty()) "items_refresh" else "api_$apiConfigId"

            Log.d(TAG, "=== Work execution started ===")
            Log.d(TAG, "Start time: ${dateFormat.format(Date(startTime))}")
            Log.d(TAG, "Work ID: ${this@ExecutionWorker.id}")
            Log.d(TAG, "Run attempt: $runAttemptCount")

            // 获取当前设置
            val workerItem = subScriptsDataHelper.getWorkersByName(workName)
            val apiConfig = subScriptsDataHelper.getUserScriptsById(apiConfigId)
            Log.d(
                TAG,
                "Current settings: enabled=${workerItem?.isEnabled}, interval=${workerItem?.isValued}min, refreshCount=${workerItem?.refreshCount}"
            )

            if (workerItem?.isEnabled != 1) {
                Log.d(TAG, "Auto refresh is disabled, skipping work")
                return@withContext Result.success()
            }

            val configObject = JSONObject(apiConfig?.mappingConfig!!)
            val itemsObject = configObject.getJSONObject("itemsMapping")
            val itemsMapping = ArticlesMappingConfig.ItemsMapping(
                rootPath = itemsObject.getString("rootPath"),
                idField = itemsObject.getString("idField"),
                titleField = itemsObject.getString("titleField"),
                picField = if (itemsObject.has("picField")) itemsObject.getString("picField") else null,
                contentField = if (itemsObject.has("contentField")) itemsObject.getString("contentField") else null,
                categoryIdField = if (itemsObject.has("categoryIdField")) itemsObject.getString(
                    "categoryIdField"
                ) else null,
                tagsField = if (itemsObject.has("tagsField")) itemsObject.getString("tagsField") else null,
                urlTypeField = itemsObject.getInt("urlTypeField"),
                apiUrlField = itemsObject.getString("apiUrlField")
            )
            // 构建URL，添加API Key（如果有）
            val finalUrlString = if (apiConfig.apiKey != null) {
                "${itemsMapping.apiUrlField}?api_key=${apiConfig.apiKey}"
            } else {
                itemsMapping.apiUrlField
            }

            try {
                // 调用API获取最新数据
                Log.d(TAG, "Fetching new stories...")
                val response = articlesScriptsRepository.getItemList(finalUrlString)

                if (response.isSuccess) {
                    Log.d(TAG, "Received $response from API")
                    val parser = ArticlesScriptParser()
                    // 处理API返回的JSON对象
                    val responseData = response.getOrNull()
                    if (responseData == null) {
                        Log.e(TAG, "API response data is null")
                        return@withContext if (runAttemptCount < 3) Result.retry() else Result.failure()
                    }
                    
                    // 将响应转换为字符串
                    val responseString = when(responseData) {
                        is String -> responseData
                        else -> responseData.toString()
                    }
                    
                    val items = parser.parseArticlesItems(responseString, itemsMapping, apiConfig.id)

                    // 创建一个使用正确数据库名称的ScriptsDataHelper实例
                    val customArticlesScriptsDataHelper = if (!apiConfigId.isEmpty() && apiConfig.databaseName != null) {
                        Log.d(TAG, "使用自定义数据库: ${apiConfig.databaseName}")
                        ArticlesScriptsDataHelper(applicationContext, apiConfig.databaseName!!, apiConfigId)
                    } else {
                        Log.d(TAG, "使用默认数据库")
                        articlesScriptsDataHelper
                    }
                    
                    var count = 0
                    for (item in items) {
                        customArticlesScriptsDataHelper.insertOrUpdateItem(item)
                        count++
                    }
                    count
                    Log.d(TAG, "=== $count ===")

                    // 更新统计信息
                    val endTime = System.currentTimeMillis()
                    val duration = endTime - startTime

                    subScriptsDataHelper.updateRefreshCount(workName)

                    // 验证更新是否成功
                    val updatedSettings = subScriptsDataHelper.getWorkersByName(workName)
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
                    val nextRunTime = endTime + (workerItem.isValued * 60 * 1000)
                    Log.d(
                        TAG,
                        "Next execution scheduled for: ${dateFormat.format(Date(nextRunTime))}"
                    )
                    Log.d(TAG, "=== Work execution finished ===\n")

                    return@withContext Result.success()
                } else {
                    Log.e(
                        TAG,
                        "API request failed: code=${response}"
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