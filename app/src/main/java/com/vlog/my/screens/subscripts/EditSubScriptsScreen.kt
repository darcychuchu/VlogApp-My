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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import com.vlog.my.data.scripts.ContentType
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.SubScripts
// Assuming EBOOK_TYPE_ID is defined in AddSubScriptsScreen or a shared constants file.
// If not, define it here: const val EBOOK_TYPE_ID = 4
// For this exercise, I'll re-declare it, but ideally it would be shared.
// import com.vlog.my.screens.subscripts.EBOOK_TYPE_ID // If it were in a shared file
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
import java.util.UUID


// Re-declaring for standalone modification context. Ideally, this is shared.
const val EBOOK_TYPE_ID = 4

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

    var isTyped by remember { mutableIntStateOf(userScripts?.isTyped ?: ContentType.NEWS.typeId) }
    val isEbookType by remember { derivedStateOf { isTyped == EBOOK_TYPE_ID } }

    // 状态变量
    var name by remember { mutableStateOf(userScripts?.name ?: "") }
    var apiKey by remember { mutableStateOf(userScripts?.apiKey ?: "") }
    // For Ebook, if dbName is null/empty, we don't auto-generate here unless specifically required for edit.
    // The requirement was for AddScreen. Here we preserve what's there or allow user to set.
    var databaseName by remember { mutableStateOf(userScripts?.databaseName ?: "") }


    // Initial mapping config state based on whether it's an Ebook type
    val initialMappingConfig = userScripts?.mappingConfig ?: "{}"
    var itemsConfig by remember { mutableStateOf(EMPTY_CONFIG) }
    var categoriesConfig by remember { mutableStateOf(EMPTY_CONFIG) }
    var itemsState by remember { mutableIntStateOf(0) }
    var categoriesState by remember { mutableIntStateOf(0) }

    if (!isEbookType && initialMappingConfig.isNotBlank() && initialMappingConfig != "{}") {
        try {
            val configJson = JSONObject(initialMappingConfig)
            itemsState = configJson.optInt("itemsState", 0)
            categoriesState = configJson.optInt("categoriesState", 0)
            itemsConfig = configJson.optJSONObject("itemsMapping")?.toString() ?: EMPTY_CONFIG
            categoriesConfig = configJson.optJSONObject("categoriesMapping")?.toString() ?: EMPTY_CONFIG
        } catch (e: JSONException) {
            Log.e("EditSubScriptsScreen", "Error parsing mappingConfig: $initialMappingConfig", e)
            // Defaults are already set
        }
    }

    var enableItemsMapping by remember { mutableStateOf(itemsState == 1 && !isEbookType) }
    var enableCategoriesMapping by remember { mutableStateOf(categoriesState == 1 && !isEbookType) }

    var mappingConfig by remember {
        mutableStateOf(
            if (isEbookType) "{}" else initialMappingConfig
        )
    }
    
    // Effect to update mappingConfig when dependent states change and it's not an Ebook
    LaunchedEffect(isEbookType, itemsState, itemsConfig, categoriesState, categoriesConfig, enableItemsMapping, enableCategoriesMapping) {
        if (!isEbookType) {
            mappingConfig = """{"itemsState":${if(enableItemsMapping) 1 else 0},"itemsMapping":${if(enableItemsMapping) itemsConfig else EMPTY_CONFIG},"categoriesState":${if(enableCategoriesMapping) 1 else 0},"categoriesMapping":${if(enableCategoriesMapping) categoriesConfig else EMPTY_CONFIG}}"""
        } else {
            mappingConfig = "{}"
            enableItemsMapping = false
            enableCategoriesMapping = false
        }
    }
    
    // When type changes to EBOOK, reset relevant fields
    LaunchedEffect(isEbookType) {
        if (isEbookType) {
            apiKey = "" // Clear API key for Ebook type
            enableItemsMapping = false
            enableCategoriesMapping = false
            itemsConfig = EMPTY_CONFIG
            categoriesConfig = EMPTY_CONFIG
            mappingConfig = "{}"
            // Auto-generate DB name if Ebook type and name is empty (only if it was not set initially for an ebook)
            if (databaseName.isBlank() && userScripts?.isTyped == EBOOK_TYPE_ID && userScripts.databaseName.isNullOrBlank()) {
                 databaseName = "ebook_subscript_${UUID.randomUUID()}.db"
            }
        }
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
                        IconButton(onClick = { navController?.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text("Edit SubScript", modifier = Modifier.padding(8.dp))
                        }
                        // Optional: Add a placeholder or action button on the right if needed for balance
                        Spacer(modifier = Modifier.padding(start = 48.dp)) // Adjust spacer as needed
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("API名称") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ContentType.toList().forEach { contentType ->
                    FilterChip(
                        selected = isTyped == contentType.typeId,
                        onClick = {
                            val oldTypeIsEbook = isEbookType
                            isTyped = contentType.typeId
                            val newTypeIsEbook = isEbookType

                            if (newTypeIsEbook && !oldTypeIsEbook) { // Switched to Ebook
                                apiKey = ""
                                enableItemsMapping = false
                                enableCategoriesMapping = false
                                itemsConfig = EMPTY_CONFIG
                                categoriesConfig = EMPTY_CONFIG
                                mappingConfig = "{}"
                                if (databaseName.isBlank()) { // Only auto-gen if field is empty
                                   databaseName = "ebook_subscript_${UUID.randomUUID()}.db"
                                }
                            } else if (!newTypeIsEbook && oldTypeIsEbook) { // Switched from Ebook
                                // Potentially reload original non-ebook settings if desired,
                                // or leave for user to re-configure.
                                // For now, fields remain as they were (likely empty from Ebook mode)
                                // Re-parse original config if available and not "{}", or set to default new state
                                try {
                                    val originalNonEbookConfig = userScripts?.mappingConfig ?: "{}"
                                    if (originalNonEbookConfig.isNotBlank() && originalNonEbookConfig != "{}") {
                                        val configJson = JSONObject(originalNonEbookConfig)
                                        itemsState = configJson.optInt("itemsState", 0)
                                        categoriesState = configJson.optInt("categoriesState", 0)
                                        itemsConfig = configJson.optJSONObject("itemsMapping")?.toString() ?: EMPTY_CONFIG
                                        categoriesConfig = configJson.optJSONObject("categoriesMapping")?.toString() ?: EMPTY_CONFIG
                                        enableItemsMapping = itemsState == 1
                                        enableCategoriesMapping = categoriesState == 1
                                        mappingConfig = originalNonEbookConfig
                                    } else {
                                        // Set to default non-ebook state
                                        itemsState = 0
                                        categoriesState = 0
                                        itemsConfig = EMPTY_CONFIG
                                        categoriesConfig = EMPTY_CONFIG
                                        enableItemsMapping = false
                                        enableCategoriesMapping = false
                                        mappingConfig = """{"itemsState":0,"itemsMapping":{},"categoriesState":0,"categoriesMapping":{}}"""
                                    }
                                } catch (e: JSONException) {
                                     Log.e("EditSubScriptsScreen", "Error re-parsing non-ebook mappingConfig: ${userScripts?.mappingConfig}", e)
                                }
                            }
                        },
                        label = { Text(contentType.typeName) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = databaseName,
                onValueChange = { databaseName = it },
                label = { Text(if (isEbookType) "数据库名称" else "数据库名称 (可选)") },
                placeholder = { Text(if (isEbookType && databaseName.isBlank()) "e.g., ebook_mydiary.db" else "留空则使用默认数据库") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = isEbookType && userScripts?.isTyped == EBOOK_TYPE_ID && !userScripts.databaseName.isNullOrBlank() // Prevent editing auto-generated for existing Ebook if it was originally blank
            )
            Spacer(Modifier.height(16.dp))

            if (!isEbookType) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (可选)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))

                // Items Mapping Card
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("启用Items表映射", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = enableItemsMapping,
                                onCheckedChange = {
                                    enableItemsMapping = it
                                    itemsState = if (it) 1 else 0
                                    if (!it) itemsConfig = EMPTY_CONFIG
                                    // else user might want to restore previous itemsConfig or use default
                                }
                            )
                        }
                        if (enableItemsMapping) {
                            val currentItemsObj = remember(itemsConfig) { try { JSONObject(itemsConfig) } catch (e: JSONException) { JSONObject() } }
                            val keysList = remember(itemsConfig) { currentItemsObj.keys().asSequence().toList() }
                            keysList.forEach { key ->
                                var fieldValue by remember(itemsConfig, key) { mutableStateOf(currentItemsObj.optString(key, "")) }
                                OutlinedTextField(
                                    value = fieldValue,
                                    onValueChange = {
                                        fieldValue = it
                                        try {
                                            currentItemsObj.put(key, it)
                                            itemsConfig = currentItemsObj.toString()
                                        } catch (e: JSONException) { Log.e("EditScreen", "Error updating itemsConfig JSON", e) }
                                    },
                                    label = { Text("- $key -") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        } else {
                             Text(
                                text = "Items表映射已禁用",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                // Categories Mapping Card
                Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("启用Categories表映射", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = enableCategoriesMapping,
                                onCheckedChange = {
                                    enableCategoriesMapping = it
                                    categoriesState = if (it) 1 else 0
                                    if (!it) categoriesConfig = EMPTY_CONFIG
                                }
                            )
                        }
                        if (enableCategoriesMapping) {
                             val currentCategoriesObj = remember(categoriesConfig) { try { JSONObject(categoriesConfig) } catch (e: JSONException) { JSONObject() } }
                             val keysList = remember(categoriesConfig) { currentCategoriesObj.keys().asSequence().toList() }
                             keysList.forEach { key ->
                                var fieldValue by remember(categoriesConfig, key) { mutableStateOf(currentCategoriesObj.optString(key, "")) }
                                OutlinedTextField(
                                    value = fieldValue,
                                    onValueChange = {
                                        fieldValue = it
                                        try {
                                            currentCategoriesObj.put(key, it)
                                            categoriesConfig = currentCategoriesObj.toString()
                                        } catch (e: JSONException) { Log.e("EditScreen", "Error updating categoriesConfig JSON", e) }
                                    },
                                    label = { Text("- $key -") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                )
                            }
                        } else {
                             Text(
                                text = "Categories表映射已禁用",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))

                 Button(
                    onClick = { // This button's purpose is to explicitly reconstruct mappingConfig
                        if (!isEbookType) {
                           mappingConfig = """{"itemsState":${if(enableItemsMapping) 1 else 0},"itemsMapping":${if(enableItemsMapping) itemsConfig else EMPTY_CONFIG},"categoriesState":${if(enableCategoriesMapping) 1 else 0},"categoriesMapping":${if(enableCategoriesMapping) categoriesConfig else EMPTY_CONFIG}}"""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("更新配置预览") }
                Spacer(Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = mappingConfig,
                onValueChange = { if (!isEbookType) mappingConfig = it },
                label = { Text("映射配置 (JSON格式)") },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                placeholder = { Text(if (isEbookType) "N/A for Ebook type" else "请输入JSON格式的映射配置") },
                readOnly = isEbookType
            )
            Spacer(Modifier.height(8.dp))
            
            // N8N and Test buttons are kept but will use the (empty) mappingConfig for Ebooks.
            // Their internal logic might need adjustment if Ebook type should disable them entirely.
            Button(
                onClick = {
                    val currentSubScript = SubScripts(
                        id = scriptId, name = name, apiKey = apiKey.takeIf { it.isNotEmpty() },
                        mappingConfig = mappingConfig, // Will be "{}" for Ebooks
                        databaseName = databaseName.takeIf { it.isNotEmpty() }, isTyped = isTyped
                    )
                    n8nPost(currentSubScript, snackBarHostState)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("N8N交互") }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                     val currentSubScript = SubScripts(
                        id = scriptId, name = name, apiKey = apiKey.takeIf { it.isNotEmpty() },
                        mappingConfig = mappingConfig, // Will be "{}" for Ebooks
                        databaseName = databaseName.takeIf { it.isNotEmpty() }, isTyped = isTyped
                    )
                    val helper = ArticlesScriptsDataHelper(context, currentSubScript.databaseName ?: "sub-scripts.db", scriptId)
                    testApiAndMapping(currentSubScript, helper, snackBarHostState)
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("测试数据") }
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val finalMappingConfig = if (isEbookType) "{}" else mappingConfig
                    val finalApiKey = if (isEbookType) null else apiKey.takeIf { it.isNotEmpty() }
                    // Ensure databaseName is not empty string if it's optional for non-ebooks but required for ebooks
                    val finalDatabaseName = if (databaseName.isBlank() && isEbookType) {
                        "ebook_subscript_${UUID.randomUUID()}.db" // Generate if somehow still blank for ebook
                    } else {
                        databaseName.takeIf { it.isNotEmpty() }
                    }


                    val updatedSubScripts = SubScripts(
                        id = scriptId,
                        name = name,
                        apiKey = finalApiKey,
                        mappingConfig = finalMappingConfig,
                        databaseName = finalDatabaseName,
                        isTyped = isTyped,
                        // Preserve other fields not edited on this screen
                        createdBy = userScripts?.createdBy,
                        isEnabled = userScripts?.isEnabled ?: 0,
                        isLocked = userScripts?.isLocked ?: 0,
                        isValued = userScripts?.isValued ?: 0,
                        listUrl = userScripts?.listUrl,
                        logoUrl = userScripts?.logoUrl,
                        url = userScripts?.url,
                        version = userScripts?.version ?: 0
                    )
                    scriptsDataHelper.updateUserScripts(updatedSubScripts)
                    navController?.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("保存") }
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
            val configObject = JSONObject(subScripts.mappingConfig)
            val itemsState = configObject.optInt("itemsState", 0)
            
            // 检查Items映射配置是否启用
            if (itemsState != 1 || !configObject.has("itemsMapping")) {
                snackBarHostState.showSnackbar("错误：Items表映射未启用或配置不正确")
                return@launch
            }
            
            // 获取Items映射配置
            val itemsObject = configObject.getJSONObject("itemsMapping")

            val itemsMapping = ArticlesMappingConfig.ItemsMapping(
                rootPath = itemsObject.getString("rootPath"),
                idField = itemsObject.getString("idField"),
                titleField = itemsObject.getString("titleField"),
                picField = if (itemsObject.has("picField")) itemsObject.getString("picField") else null,
                contentField = if (itemsObject.has("contentField")) itemsObject.getString("contentField") else null,
                categoryIdField = if (itemsObject.has("categoryIdField")) itemsObject.getString("categoryIdField") else null,
                tagsField = if (itemsObject.has("tagsField")) itemsObject.getString("tagsField") else null,
                urlTypeField = itemsObject.getInt("urlTypeField"),
                apiUrlField = itemsObject.getString("apiUrlField")
            )

            if (itemsMapping.apiUrlField.isBlank() || itemsMapping.apiUrlField == "http://") {
                snackBarHostState.showSnackbar("错误：API URL未设置")
                return@launch
            }
            
            // 创建解析器
            val parser = ArticlesScriptParser()

            // 在IO线程中执行网络请求
            val apiResponse = withContext(Dispatchers.IO) {
                try {
                    // 构建URL，添加API Key（如果有）
                    val finalUrlString = if (subScripts.apiKey != null) {
                        "${itemsMapping.apiUrlField}?api_key=${subScripts.apiKey}"
                    } else {
                        itemsMapping.apiUrlField
                    }
                    
                    // 发起网络请求
                    val connection = URL(finalUrlString).openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()
                    
                    // 读取响应
                    connection.getInputStream().bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    throw Exception("网络请求失败: ${e.message}")
                }
            }

            // 解析数据并保存到数据库
            val itemsCount = withContext(Dispatchers.IO) {
                try {
                    // 解析并保存items
                    val items = parser.parseArticlesItems(apiResponse, itemsMapping, subScripts.id)
                    var count = 0
                    for (item in items) {
                        articlesScriptsDataHelper.insertOrUpdateItem(item)
                        count++
                    }
                    count
                } catch (e: Exception) {
                    throw Exception("数据解析或保存失败: ${e.message}")
                }
            }

            // 显示成功消息
            snackBarHostState.showSnackbar("测试成功：成功获取并保存 $itemsCount 条数据")
        } catch (e: Exception) {
            // 处理错误
            e.printStackTrace()
            snackBarHostState.showSnackbar("测试失败：${e.message}")
        }
    }
}


/**
 * 测试API和映射配置是否能正常获取数据并写入数据库
 * @param SubScripts API配置
 * @param snackBarHostState 用于显示消息的SnackbarHostState
 */
private fun n8nPost(subScripts: SubScripts, snackBarHostState: SnackbarHostState) {
    // 创建协程作用域
    val coroutineScope = CoroutineScope(Dispatchers.Main)

    coroutineScope.launch {
        try {

            // 在IO线程中执行网络请求
            val apiResponse = withContext(Dispatchers.IO) {
                try {
                    // 发起网络请求
                    val connection = URL("https://n8n.66log.com/webhook-test/ai?data=${subScripts.mappingConfig}").openConnection()
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    connection.connect()

                    // 读取响应
                    connection.getInputStream().bufferedReader().use { it.readText() }
                } catch (e: Exception) {
                    throw Exception("网络请求失败: ${e.message}")
                }
            }

            // 解析数据并保存到数据库
            withContext(Dispatchers.IO) {
                try {
                    snackBarHostState.showSnackbar("测试成功：$apiResponse")
                } catch (e: Exception) {
                    throw Exception("数据解析或保存失败: ${e.message}")
                }
            }

        } catch (e: Exception) {
            // 处理错误
            e.printStackTrace()
            snackBarHostState.showSnackbar("测试失败：${e.message}")
        }
    }
}
