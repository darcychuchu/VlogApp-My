package com.vlog.my.screens.subscripts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import com.vlog.my.data.scripts.ContentType
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.articles.ArticlesConfig.CATEGORY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesConfig.EMPTY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesMappingConfig
import com.vlog.my.data.scripts.articles.ArticlesScriptsDataHelper
import com.vlog.my.parser.ArticlesScriptParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditSubScriptsScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    scriptId: String
) {
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)
    val userScripts = scriptsDataHelper.getUserScriptsById(scriptId)

    // 状态变量
    var name by remember { mutableStateOf(userScripts?.name) }
    var apiKey by remember { mutableStateOf(userScripts?.apiKey ?: "") }
    var databaseName by remember { mutableStateOf(userScripts?.databaseName ?: "") }

    // Initial values from userScripts.mappingConfig
    val initialConfigJsonString = userScripts?.mappingConfig ?: "{}"
    val initialConfigObject = try { JSONObject(initialConfigJsonString) } catch (e: JSONException) { JSONObject() }
    val initialItemsSourceUrl = initialConfigObject.optString("sourceUrl", "")
    val initialFeedType = initialConfigObject.optString("feedType", "JSON") // Default to JSON if not found
    val initialItemsMappingObject = initialConfigObject.optJSONObject("itemsMapping") ?: JSONObject()

    var sourceUrl by remember { mutableStateOf(initialItemsSourceUrl) }
    var selectedFeedType by remember { mutableStateOf(initialFeedType) }

    // State variables for new mapping fields, initialized from existing config
    var articleRootPath by remember { mutableStateOf(initialItemsMappingObject.optString("rootPath", "")) }
    var idField by remember { mutableStateOf(initialItemsMappingObject.optString("idField", "")) }
    var titleField by remember { mutableStateOf(initialItemsMappingObject.optString("titleField", "")) }
    var contentField by remember { mutableStateOf(initialItemsMappingObject.optString("contentField", "")) }
    var imageUrlField by remember { mutableStateOf(initialItemsMappingObject.optString("picField", "")) } // maps to picField
    var categoryIdField by remember { mutableStateOf(initialItemsMappingObject.optString("categoryIdField", "")) }
    var tagsField by remember { mutableStateOf(initialItemsMappingObject.optString("tagsField", "")) }
    var sourceUrlField_mapping by remember { mutableStateOf(initialItemsMappingObject.optString("sourceUrlField", "")) } // maps to sourceUrlField

    var mappingConfig by remember { mutableStateOf(initialConfigJsonString) }

    // Update mappingConfig whenever sourceUrl, feedType or any mapping field changes
    LaunchedEffect(
        sourceUrl, selectedFeedType, articleRootPath, idField, titleField, contentField,
        imageUrlField, categoryIdField, tagsField, sourceUrlField_mapping
    ) {
        val itemsMappingObject = JSONObject()
        itemsMappingObject.put("rootPath", articleRootPath)
        itemsMappingObject.put("idField", idField)
        itemsMappingObject.put("titleField", titleField)
        itemsMappingObject.put("contentField", contentField)
        if (imageUrlField.isNotBlank()) itemsMappingObject.put("picField", imageUrlField)
        if (categoryIdField.isNotBlank()) itemsMappingObject.put("categoryIdField", categoryIdField)
        if (tagsField.isNotBlank()) itemsMappingObject.put("tagsField", tagsField)
        if (sourceUrlField_mapping.isNotBlank()) itemsMappingObject.put("sourceUrlField", sourceUrlField_mapping)
        itemsMappingObject.put("apiUrlField", sourceUrl) // Populate apiUrlField with sourceUrl

        val mainConfigObject = JSONObject()
        mainConfigObject.put("sourceUrl", sourceUrl)
        mainConfigObject.put("feedType", selectedFeedType) // Add feedType to mappingConfig
        mainConfigObject.put("itemsMapping", itemsMappingObject)
        // Retain existing categoriesMapping if present, otherwise add empty
        mainConfigObject.put("categoriesMapping", initialConfigObject.optJSONObject("categoriesMapping") ?: JSONObject())


        mappingConfig = mainConfigObject.toString()
    }

    var isTyped by remember {
        mutableIntStateOf(userScripts?.isTyped ?: ContentType.NEWS.typeId)
    }

    //error info
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 左侧返回按钮
                        IconButton(
                            onClick = { navController?.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }

                        // 中间的标题
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Edit",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // API名称
            OutlinedTextField(
                value = name ?: "",
                onValueChange = { name = it },
                label = { Text("API名称") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = sourceUrl,
                onValueChange = { sourceUrl = it },
                label = { Text("源 URL") },
                modifier = Modifier.fillMaxWidth(),
                supportingText = { Text("请输入以 http:// 或 https:// 开头的有效URL") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Feed Type Selection
            Text("选择数据源类型:", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val feedTypes = listOf("JSON", "RSS/XML") // Define feed types
                feedTypes.forEach { type ->
                    FilterChip(
                        selected = selectedFeedType == type,
                        onClick = { selectedFeedType = type },
                        label = { Text(type) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Field Mapping Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("字段映射 (Items Mapping)", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = articleRootPath,
                        onValueChange = { articleRootPath = it },
                        label = { Text("文章根路径") },
                        placeholder = { Text(text = "e.g., data.items or feed.entry") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = idField,
                        onValueChange = { idField = it },
                        label = { Text("ID 字段") },
                        placeholder = { Text(text = "e.g., id or guid") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = titleField,
                        onValueChange = { titleField = it },
                        label = { Text("标题字段") },
                        placeholder = { Text(text = "e.g., title or name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = contentField,
                        onValueChange = { contentField = it },
                        label = { Text("内容字段") },
                        placeholder = { Text(text = "e.g., content or description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = imageUrlField,
                        onValueChange = { imageUrlField = it },
                        label = { Text("图片 URL 字段 (可选)") },
                        placeholder = { Text(text = "e.g., image_url or media:thumbnail_url") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = categoryIdField,
                        onValueChange = { categoryIdField = it },
                        label = { Text("分类 ID 字段 (可选)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tagsField,
                        onValueChange = { tagsField = it },
                        label = { Text("标签字段 (可选)") },
                        placeholder = { Text(text = "e.g., tags or categories (if a list)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sourceUrlField_mapping,
                        onValueChange = { sourceUrlField_mapping = it },
                        label = { Text("原文链接字段 (可选)") },
                        placeholder = { Text(text = "Article's own URL, if different") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContentType.toList().forEach { contentType ->
                    FilterChip(
                        selected = isTyped == contentType.typeId,
                        onClick = {
                            isTyped = contentType.typeId
//                            // 更新字段映射
//                            if (contentType != ContentType.NEWS) {
//                                when (contentType) {
//                                    ContentType.MOVIE -> {}
//                                    ContentType.EBOOK -> {}
//                                    ContentType.MUSIC -> {}
//                                    else -> {}
//                                }
//                            }
                        },
                        label = { Text(contentType.typeName) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))


            // 数据库名称输入框
            OutlinedTextField(
                value = databaseName.toString(),
                onValueChange = { databaseName = it },
                label = { Text("数据库名称 (可选)") },
                placeholder = { Text("留空则使用默认数据库") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // API Key
            OutlinedTextField(
                value = apiKey ?: "",
                onValueChange = { apiKey = it },
                label = { Text("API Key (可选)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))


            // 保存按钮
            Button(
                onClick = {
                    // mappingConfig is updated by LaunchedEffect
                    val testSubScripts = SubScripts(
                        id = scriptId,
                        name = name ?: "",
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig,
                        databaseName = databaseName.takeIf { it?.isNotEmpty() == true }
                    )
                    n8nPost(testSubScripts, snackBarHostState)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("N8N交互")
            }

            // 保存按钮
            Button(
                onClick = {
                    // mappingConfig is updated by LaunchedEffect
                    val testSubScripts = SubScripts(
                        id = scriptId,
                        name = name ?: "",
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig,
                        databaseName = databaseName.takeIf { it?.isNotEmpty() == true },
                        isTyped = isTyped
                    )

                    // 创建ScriptsDataHelper实例，使用指定的数据库名称或默认数据库
                    val articlesScriptsDataHelper = ArticlesScriptsDataHelper(context, testSubScripts.databaseName ?: "sub-scripts.db", scriptId)

                    // 调用测试数据函数
                    testApiAndMapping(testSubScripts, articlesScriptsDataHelper, snackBarHostState)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("测试数据")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 保存按钮
            Button(
                onClick = {
                    // mappingConfig is updated by LaunchedEffect
                    val updateSubScripts = SubScripts(
                        id = scriptId,
                        name = name ?: "",
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig,
                        databaseName = databaseName.takeIf { it?.isNotEmpty() == true },
                        isTyped = isTyped
                    )

                    // 保存到数据库
                    scriptsDataHelper.updateUserScripts(updateSubScripts)

                    // 返回上一页
                    navController?.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}

/**
 * 测试API和映射配置是否能正常获取数据并写入数据库
 * @param SubScripts API配置
 * @param articlesScriptsDataHelper 数据库助手
 * @param snackBarHostState 用于显示消息的SnackbarHostState
 */
private fun testApiAndMapping(subScripts: SubScripts, articlesScriptsDataHelper: ArticlesScriptsDataHelper, snackBarHostState: SnackbarHostState) {
    // 创建协程作用域
    val coroutineScope = CoroutineScope(Dispatchers.Main)

    coroutineScope.launch {
        try {
            // 解析映射配置JSON
            val configObject = try { JSONObject(subScripts.mappingConfig ?: "{}") } catch (e: JSONException) { JSONObject() }
            val sourceUrlValue = configObject.optString("sourceUrl", "")
            val itemsMapping = configObject.optJSONObject("itemsMapping")

            if (sourceUrlValue.isBlank()) {
                snackBarHostState.showSnackbar("错误：源 URL未设置。")
                return@launch
            }
            if (itemsMapping == null) {
                snackBarHostState.showSnackbar("错误：Items Mapping配置未找到。")
                return@launch
            }

            val itemsMappingJson = configObject.optJSONObject("itemsMapping")

            if (sourceUrlValue.isBlank()) {
                snackBarHostState.showSnackbar("Test failed: Source URL is not set.")
                return@launch
            }
            if (itemsMappingJson == null) {
                snackBarHostState.showSnackbar("Test failed: Items Mapping configuration is missing.")
                return@launch
            }

            // Create ItemsMapping instance
            val itemsMappingInstance = ArticlesMappingConfig.ItemsMapping(
                rootPath = itemsMappingJson.optString("rootPath"),
                idField = itemsMappingJson.optString("idField"),
                titleField = itemsMappingJson.optString("titleField"),
                picField = itemsMappingJson.optString("picField", null),
                contentField = itemsMappingJson.optString("contentField", null),
                categoryIdField = itemsMappingJson.optString("categoryIdField", null),
                tagsField = itemsMappingJson.optString("tagsField", null),
                sourceUrlField = itemsMappingJson.optString("sourceUrlField", null),
                // urlTypeField is not currently set by the UI, default to 0 or handle as needed
                urlTypeField = itemsMappingJson.optInt("urlTypeField", 0), 
                apiUrlField = itemsMappingJson.optString("apiUrlField", sourceUrlValue) // Ensure apiUrlField is present
            )
            
            if (itemsMappingInstance.rootPath.isBlank() || itemsMappingInstance.idField.isBlank() || itemsMappingInstance.titleField.isBlank()){
                 snackBarHostState.showSnackbar("Test failed: Essential mapping fields (Root Path, ID Field, Title Field) are not set.")
                return@launch
            }


            // Network Request
            val apiResponse: String
            try {
                val finalUrlString = if (subScripts.apiKey?.isNotBlank() == true) {
                    "${sourceUrlValue}?api_key=${subScripts.apiKey}"
                } else {
                    sourceUrlValue
                }
                val url = URL(finalUrlString)
                val connection = url.openConnection()
                connection.connectTimeout = 5000 // 5 seconds
                connection.readTimeout = 10000  // 10 seconds
                apiResponse = connection.getInputStream().bufferedReader().use { it.readText() }
            } catch (e: java.net.MalformedURLException) {
                snackBarHostState.showSnackbar("Test failed: Invalid URL format: $sourceUrlValue")
                return@launch
            } catch (e: java.io.IOException) {
                snackBarHostState.showSnackbar("Test failed: Network request error (e.g., no internet, server down).")
                Log.e("testApiAndMapping", "IOException: ${e.message}", e)
                return@launch
            } catch (e: Exception) {
                snackBarHostState.showSnackbar("Test failed: Network request failed: ${e.message}")
                Log.e("testApiAndMapping", "Generic network exception: ${e.message}", e)
                return@launch
            }

            // Parsing
            val items: List<ArticlesItems>
            try {
                val parser = ArticlesScriptParser()
                items = parser.parseArticlesItems(apiResponse, itemsMappingInstance, subScripts.id)
            } catch (e: JSONException) {
                snackBarHostState.showSnackbar("Test failed: Could not parse JSON response. Check data format and 'Article Root Path'.")
                Log.e("testApiAndMapping", "JSONException: ${e.message}", e)
                return@launch
            } catch (e: Exception) {
                snackBarHostState.showSnackbar("Test failed: Data parsing error: ${e.message}")
                Log.e("testApiAndMapping", "Generic parsing exception: ${e.message}", e)
                return@launch
            }

            // Validation of Parsed Data
            if (items.isEmpty()) {
                snackBarHostState.showSnackbar("Test successful, but no articles found. Verify 'Article Root Path' and other mapping fields.")
                return@launch
            }

            val firstItem = items.first()
            if (firstItem.id.isBlank() || firstItem.title.isBlank()) {
                snackBarHostState.showSnackbar("Warning: Articles parsed, but the first item is missing ID or Title. Check 'ID Field' and 'Title Field' mappings.")
                // Optionally, still proceed to save if some data is present but partial
            }
            
            // Data Storage
            var count = 0
            try {
                withContext(Dispatchers.IO) {
                    for (item in items) {
                        articlesScriptsDataHelper.insertOrUpdateItem(item)
                        count++
                    }
                }
            } catch (e: Exception) {
                 snackBarHostState.showSnackbar("Error saving articles to database: ${e.message}")
                Log.e("testApiAndMapping", "Database save error: ${e.message}", e)
                return@launch
            }

            snackBarHostState.showSnackbar("Test successful! Fetched and saved $count articles.")

        } catch (e: Exception) {
            // Catch-all for unexpected errors during the setup (e.g., JSON parsing of mappingConfig itself)
            e.printStackTrace()
            snackBarHostState.showSnackbar("Test failed: An unexpected error occurred: ${e.message}")
            Log.e("testApiAndMapping", "Outer catch-all: ${e.message}", e)
        }
    }
}


/**
 * n8nPost function now primarily sends the simplified mappingConfig.
 * The actual usefulness of this for n8n will depend on how n8n is set up to handle this new structure.
 * @param SubScripts API配置 (contains the simplified mappingConfig)
 * @param snackBarHostState 用于显示消息的SnackbarHostState
 */
private fun n8nPost(subScripts: SubScripts, snackBarHostState: SnackbarHostState) {
    // 创建协程作用域
    val coroutineScope = CoroutineScope(Dispatchers.Main)

    coroutineScope.launch {
        try {
            val configJson = subScripts.mappingConfig ?: "{}"
            // 在IO线程中执行网络请求
            val apiResponse = withContext(Dispatchers.IO) {
                try {
                    // 发起网络请求 - Ensure mappingConfig is URL encoded if it contains special characters
                    val encodedMappingConfig = java.net.URLEncoder.encode(configJson, "UTF-8")
                    val connection = URL("https://n8n.66log.com/webhook-test/ai?data=$encodedMappingConfig").openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    // 读取响应
                    connection.getInputStream().bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    throw Exception("网络请求失败: ${e.message}")
                }
            }

            // 显示成功消息
            snackBarHostState.showSnackbar("N8N交互响应: $apiResponse")

        } catch (e: Exception) {
            // 处理错误
            e.printStackTrace()
            snackBarHostState.showSnackbar("N8N交互失败：${e.message}")
        }
    }
}
