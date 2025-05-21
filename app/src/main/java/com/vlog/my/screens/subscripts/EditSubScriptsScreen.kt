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
    var apiKey by remember { mutableStateOf(userScripts?.apiKey) }
    var databaseName by remember { mutableStateOf(userScripts?.databaseName) }

    val configObject = JSONObject(userScripts?.mappingConfig!!)
    var itemsObject by remember { mutableStateOf(configObject.getJSONObject("itemsMapping")) }
    var categoriesObject by remember { mutableStateOf(configObject.getJSONObject("categoriesMapping")) }

    var itemsConfig by remember { mutableStateOf(itemsObject.toString()) }
    var categoriesConfig by remember { mutableStateOf(categoriesObject.toString()) }

    var itemsState by remember {mutableIntStateOf(configObject.getInt("itemsState"))}
    var categoriesState by remember {mutableIntStateOf(configObject.getInt("categoriesState"))}

    var enableItemsMapping by remember { mutableStateOf(itemsState == 1) }
    var enableCategoriesMapping by remember { mutableStateOf(categoriesState == 1) }
    var mappingConfig by remember { mutableStateOf(
        """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
    ) }


    var isTyped by remember {
        mutableIntStateOf(userScripts.isTyped)
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
                value = name.toString(),
                onValueChange = { name = it },
                label = { Text("API名称") },
                modifier = Modifier.fillMaxWidth()
            )

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
                value = apiKey.toString(),
                onValueChange = { apiKey = it },
                label = { Text("API Key (可选)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("启用Items表映射", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = enableItemsMapping,
                            onCheckedChange = {
                                enableItemsMapping = it
                                if (enableItemsMapping){
                                    itemsState = 1
                                    itemsConfig = itemsObject.toString()
                                } else {
                                    itemsState = 0
                                    itemsConfig = EMPTY_CONFIG
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (enableItemsMapping) {
                        val fieldValues = remember { mutableMapOf<String, String>() }

                        // 初始化字段值Map
                        LaunchedEffect(Unit) {
                            val keys = itemsObject.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                fieldValues[key] = itemsObject.getString(key)
                            }
                        }
                        // 将Iterator转换为List
                        val keysList = mutableListOf<String>()
                        val keysIterator = itemsObject.keys()
                        while (keysIterator.hasNext()) {
                            keysList.add(keysIterator.next())
                        }


                        Text(
                            text = "Items表映射已开启",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 使用List进行forEach循环
                        keysList.forEach { key ->
                            // 使用可变状态来存储每个字段的值，确保UI更新
                            var fieldValue by remember { mutableStateOf(fieldValues[key] ?: itemsObject.getString(key)) }

                            OutlinedTextField(
                                value = fieldValue,
                                onValueChange = { newValue ->
                                    // 更新本地状态变量
                                    fieldValue = newValue
                                    // 更新字段值Map
                                    fieldValues[key] = newValue
                                    try {
                                        // 更新jsonObject
                                        itemsObject.put(key, newValue)
                                        // 更新jsonConfig
                                        itemsConfig = itemsObject.toString()
                                    } catch (e: JSONException) {
                                        // 处理JSON异常
                                        Log.e("AddScriptsScreen", "JSON更新失败: ${e.message}")
                                    }
                                },
                                label = { Text("- $key -") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
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

            Spacer(modifier = Modifier.height(16.dp))


            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    // 启用/禁用开关
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("启用Categories表映射", style = MaterialTheme.typography.titleMedium)
                        Switch(
                            checked = enableCategoriesMapping,
                            onCheckedChange = {
                                enableCategoriesMapping = it
                                if (enableCategoriesMapping){
                                    categoriesState = 1
                                    categoriesConfig = CATEGORY_CONFIG
                                } else {
                                    categoriesState = 0
                                    categoriesConfig = EMPTY_CONFIG
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (enableCategoriesMapping) {

                        val fieldValues = remember { mutableMapOf<String, String>() }

                        // 初始化字段值Map
                        LaunchedEffect(Unit) {
                            val keys = categoriesObject.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                fieldValues[key] = categoriesObject.getString(key)
                            }
                        }
                        // 将Iterator转换为List
                        val keysList = mutableListOf<String>()
                        val keysIterator = categoriesObject.keys()
                        while (keysIterator.hasNext()) {
                            keysList.add(keysIterator.next())
                        }


                        Text(
                            text = "Categories表映射已开启",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        // 使用List进行forEach循环
                        keysList.forEach { key ->
                            // 使用可变状态来存储每个字段的值，确保UI更新
                            var fieldValue by remember { mutableStateOf(fieldValues[key] ?: categoriesObject.getString(key)) }

                            OutlinedTextField(
                                value = fieldValue,
                                onValueChange = { newValue ->
                                    // 更新本地状态变量
                                    fieldValue = newValue
                                    // 更新字段值Map
                                    fieldValues[key] = newValue
                                    try {
                                        // 更新jsonObject
                                        categoriesObject.put(key, newValue)
                                        // 更新jsonConfig
                                        categoriesConfig = categoriesObject.toString()
                                    } catch (e: JSONException) {
                                        // 处理JSON异常
                                        Log.e("AddScriptsScreen", "JSON更新失败: ${e.message}")
                                    }
                                },
                                label = { Text("- $key -") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))
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




            // 保存按钮
            Button(
                onClick = {
                    mappingConfig = """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("更新配置")
            }
            // 映射配置
            OutlinedTextField(
                value = mappingConfig.toString(),
                onValueChange = { mappingConfig = it },
                label = { Text("映射配置 (JSON格式)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text("请输入JSON格式的映射配置") },
                readOnly = false
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 保存按钮
            Button(
                onClick = {
                    // 创建当前编辑的userScripts对象
                    val testSubScripts = SubScripts(
                        id = scriptId,
                        name = name.toString(),
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig.toString(),
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
                    // 创建当前编辑的UserScripts对象
                    val testSubScripts = SubScripts(
                        id = scriptId,
                        name = name.toString(),
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig.toString(),
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
                    // 更新UserScripts对象
                    val updateSubScripts = SubScripts(
                        id = scriptId,
                        name = name.toString(),
                        apiKey = apiKey.takeIf { it?.isNotEmpty() == true },
                        mappingConfig = mappingConfig.toString(),
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
