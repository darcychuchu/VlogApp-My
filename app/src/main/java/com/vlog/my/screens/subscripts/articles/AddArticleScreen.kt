package com.vlog.my.screens.subscripts.articles

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.vlog.my.data.scripts.ContentType
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.articles.ArticlesConfig.CATEGORY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesConfig.EMPTY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesConfig.ITEM_CONFIG
import com.vlog.my.screens.users.UserViewModel
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.set

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddArticleScreen(
    navController: NavController? = null,
    userViewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)

    // 状态变量
    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var databaseName by remember { mutableStateOf("") }

    //val configList = listOf("Items表映射", "Categories表映射")

    var itemsConfig by remember { mutableStateOf("{}") }
    var categoriesConfig by remember { mutableStateOf("{}") }
    val itemsMappingConfig = ITEM_CONFIG
    var categoriesMappingConfig = CATEGORY_CONFIG


    var itemsState by remember {mutableIntStateOf(0)}
    var categoriesState by remember {mutableIntStateOf(0)}
    var enableItemsMapping by remember { mutableStateOf(false) }
    var enableCategoriesMapping by remember { mutableStateOf(false) }
    var mappingConfig by remember { mutableStateOf(
        """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
    ) }


    var isTyped by remember {
        mutableIntStateOf(ContentType.NEWS.typeId)
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
                                text = "设置",
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        // 右侧占位，保持对称
                        IconButton(
                            onClick = {   }
                        ) {
                            Icon(
                                imageVector = Icons.Default.DataUsage,
                                contentDescription = "Data"
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
                value = name,
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
                value = databaseName,
                onValueChange = { databaseName = it },
                label = { Text("数据库名称 (可选)") },
                placeholder = { Text("留空则使用默认数据库") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

//            // 列表URL输入框
//            OutlinedTextField(
//                value = url,
//                onValueChange = { url = it },
//                label = { Text("数据 API URL") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // 列表URL输入框
//            OutlinedTextField(
//                value = listUrl,
//                onValueChange = { listUrl = it },
//                label = { Text("列表数据 API URL (可选)") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))
//
//            // 分类URL输入框
//            OutlinedTextField(
//                value = cateUrl,
//                onValueChange = { cateUrl = it },
//                label = { Text("分类数据 API URL (可选)") },
//                modifier = Modifier.fillMaxWidth(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
//            )
//
//            Spacer(modifier = Modifier.height(16.dp))

            // API Key
            OutlinedTextField(
                value = apiKey,
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
                                enableItemsMapping = it
                                if (enableItemsMapping){
                                    itemsState = 1
                                    itemsConfig = itemsMappingConfig
                                } else {
                                    itemsState = 0
                                    itemsConfig = EMPTY_CONFIG
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (enableItemsMapping) {
                        Text(
                            text = "Items表映射已开启",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        val itemsObject = remember { JSONObject(itemsConfig) }
                        val fieldValues = remember { mutableMapOf<String, String>() }

                        // 初始化字段值Map
                        LaunchedEffect(Unit) {
                            val keys = itemsObject.keys()
                            while (keys.hasNext()) {
                                val key = keys.next()
                                fieldValues[key] = itemsObject.getString(key)
                            }
                        }

                        // 显示所有字段的输入框
                        // 将Iterator转换为List
                        val keysList = mutableListOf<String>()
                        val keysIterator = itemsObject.keys()
                        while (keysIterator.hasNext()) {
                            keysList.add(keysIterator.next())
                        }

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
                                    categoriesConfig = categoriesMappingConfig
                                } else {
                                    categoriesState = 0
                                    categoriesConfig = EMPTY_CONFIG
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (enableCategoriesMapping) {


                        val categoriesObject = remember { JSONObject(categoriesConfig) }
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
                value = mappingConfig,
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
                    // 创建ApiConfig对象
                    val subScripts = SubScripts(
                        name = name,
                        apiKey = apiKey.takeIf { it.isNotEmpty() },
                        mappingConfig = mappingConfig.toString(),
                        databaseName = databaseName.takeIf { it.isNotEmpty() },
                        createdBy = userViewModel.getCurrentUser()?.name,
                        isTyped = isTyped
                    )

                    // 保存到数据库
                    scriptsDataHelper.insertUserScripts(subScripts)

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
