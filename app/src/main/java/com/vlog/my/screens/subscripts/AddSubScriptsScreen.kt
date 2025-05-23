package com.vlog.my.screens.subscripts

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.data.scripts.articles.ArticlesConfig.CATEGORY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesConfig.EMPTY_CONFIG
import com.vlog.my.data.scripts.articles.ArticlesConfig.ITEM_CONFIG
import com.vlog.my.screens.users.UserViewModel
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID // For generating unique DB names
import kotlin.String

// Define ContentType.EBOOK.typeId, assuming it's 4 as per requirements
const val EBOOK_TYPE_ID = 4

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubScriptsScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: UserViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)

    // 状态变量
    var name by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf("") }
    var databaseName by remember { mutableStateOf("") }

    var itemsConfig by remember { mutableStateOf("{}") }
    var categoriesConfig by remember { mutableStateOf("{}") }
    val itemsMappingConfig = ITEM_CONFIG
    var categoriesMappingConfig = CATEGORY_CONFIG

    var itemsState by remember { mutableIntStateOf(0) }
    var categoriesState by remember { mutableIntStateOf(0) }
    var enableItemsMapping by remember { mutableStateOf(false) }
    var enableCategoriesMapping by remember { mutableStateOf(false) }

    var isTyped by remember { mutableIntStateOf(ContentType.NEWS.typeId) }

    // Derived state to check if the current type is EBOOK
    val isEbookType by remember { derivedStateOf { isTyped == EBOOK_TYPE_ID } }

    var mappingConfig by remember {
        mutableStateOf(
            if (isEbookType) "{}" else """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
        )
    }

    //error info
    val snackBarHostState = remember { SnackbarHostState() }

    // Effect to update mappingConfig when dependent states change and it's not an Ebook
    LaunchedEffect(isEbookType, itemsState, itemsConfig, categoriesState, categoriesConfig) {
        if (!isEbookType) {
            mappingConfig = """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
        } else {
            mappingConfig = "{}"
        }
    }
    
    // Effect to auto-generate databaseName when EBOOK type is selected and databaseName is empty
    LaunchedEffect(isEbookType) {
        if (isEbookType && databaseName.isBlank()) {
            databaseName = "ebook_subscript_${UUID.randomUUID()}.db"
        }
    }


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
                            onClick = { /* Implement if needed */ }
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
                            if (isTyped == EBOOK_TYPE_ID) {
                                // Auto-generate DB name if Ebook type and name is empty
                                if (databaseName.isBlank()) {
                                    databaseName = "ebook_subscript_${UUID.randomUUID()}.db"
                                }
                                // Reset/disable mapping fields for Ebook
                                enableItemsMapping = false
                                enableCategoriesMapping = false
                                itemsConfig = EMPTY_CONFIG
                                categoriesConfig = EMPTY_CONFIG
                                mappingConfig = "{}"
                            } else {
                                // Restore or enable mapping fields for other types if needed
                                // This example assumes user will re-enable manually if they switch back and forth
                            }
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
                label = { Text(if (isEbookType) "数据库名称" else "数据库名称 (可选)") },
                placeholder = { Text(if (isEbookType) "e.g., ebook_mydiary.db" else "留空则使用默认数据库") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Conditional UI for non-Ebook types
            if (!isEbookType) {
                // API Key
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key (可选)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Items Mapping Card
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
                                    if (enableItemsMapping) {
                                        itemsState = 1
                                        itemsConfig = itemsMappingConfig
                                    } else {
                                        itemsState = 0
                                        itemsConfig = EMPTY_CONFIG
                                    }
                                }
                            )
                        }
                        if (enableItemsMapping) {
                            // Dynamic fields for itemsConfig (existing logic)
                            val itemsObject = remember(itemsConfig) { JSONObject(itemsConfig) }
                            val fieldValues = remember(itemsConfig) { mutableMapOf<String, String>() }
                            LaunchedEffect(itemsConfig) {
                                val keys = itemsObject.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    fieldValues[key] = itemsObject.getString(key)
                                }
                            }
                            val keysList = remember(itemsConfig) {
                                mutableListOf<String>().apply {
                                    val keysIterator = itemsObject.keys()
                                    while (keysIterator.hasNext()) { add(keysIterator.next()) }
                                }
                            }
                            keysList.forEach { key ->
                                var fieldValue by remember(itemsConfig, key) { mutableStateOf(fieldValues[key] ?: itemsObject.getString(key)) }
                                OutlinedTextField(
                                    value = fieldValue,
                                    onValueChange = { newValue ->
                                        fieldValue = newValue
                                        fieldValues[key] = newValue
                                        try {
                                            itemsObject.put(key, newValue)
                                            itemsConfig = itemsObject.toString()
                                        } catch (e: JSONException) {
                                            Log.e("AddSubScriptsScreen", "JSON更新失败: ${e.message}")
                                        }
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
                Spacer(modifier = Modifier.height(16.dp))

                // Categories Mapping Card
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
                            Text("启用Categories表映射", style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = enableCategoriesMapping,
                                onCheckedChange = {
                                    enableCategoriesMapping = it
                                    if (enableCategoriesMapping) {
                                        categoriesState = 1
                                        categoriesConfig = categoriesMappingConfig
                                    } else {
                                        categoriesState = 0
                                        categoriesConfig = EMPTY_CONFIG
                                    }
                                }
                            )
                        }
                        if (enableCategoriesMapping) {
                            // Dynamic fields for categoriesConfig (existing logic)
                            val categoriesObject = remember(categoriesConfig) { JSONObject(categoriesConfig) }
                            val fieldValues = remember(categoriesConfig) { mutableMapOf<String, String>() }
                            LaunchedEffect(categoriesConfig) {
                                val keys = categoriesObject.keys()
                                while (keys.hasNext()) {
                                    val key = keys.next()
                                    fieldValues[key] = categoriesObject.getString(key)
                                }
                            }
                             val keysList = remember(categoriesConfig) {
                                mutableListOf<String>().apply {
                                    val keysIterator = categoriesObject.keys()
                                    while (keysIterator.hasNext()) { add(keysIterator.next()) }
                                }
                            }
                            keysList.forEach { key ->
                                var fieldValue by remember(categoriesConfig, key) { mutableStateOf(fieldValues[key] ?: categoriesObject.getString(key)) }
                                OutlinedTextField(
                                    value = fieldValue,
                                    onValueChange = { newValue ->
                                        fieldValue = newValue
                                        fieldValues[key] = newValue
                                        try {
                                            categoriesObject.put(key, newValue)
                                            categoriesConfig = categoriesObject.toString()
                                        } catch (e: JSONException) {
                                            Log.e("AddSubScriptsScreen", "JSON更新失败: ${e.message}")
                                        }
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
                Spacer(modifier = Modifier.height(16.dp))

                // "Update Config" button - This seems to primarily update the mappingConfig state
                // For Ebook type, this will be effectively disabled or hidden by the parent conditional.
                Button(
                    onClick = {
                        if (!isEbookType) {
                             mappingConfig = """{"itemsState":$itemsState,"itemsMapping":$itemsConfig,"categoriesState":$categoriesState,"categoriesMapping":$categoriesConfig}"""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("更新配置")
                }
                 Spacer(modifier = Modifier.height(8.dp))
            }


            // 映射配置 OutlinedTextField
            OutlinedTextField(
                value = mappingConfig,
                onValueChange = { if (!isEbookType) mappingConfig = it },
                label = { Text("映射配置 (JSON格式)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                placeholder = { Text(if (isEbookType) "N/A for Ebook type" else "请输入JSON格式的映射配置") },
                readOnly = isEbookType // Make read-only for Ebook type
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 保存按钮
            Button(
                onClick = {
                    val finalMappingConfig = if (isEbookType) "{}" else mappingConfig
                    val finalApiKey = if (isEbookType) null else apiKey.takeIf { it.isNotEmpty() }

                    val subScripts = SubScripts(
                        name = name,
                        apiKey = finalApiKey,
                        mappingConfig = finalMappingConfig,
                        databaseName = databaseName.takeIf { it.isNotEmpty() },
                        createdBy = viewModel.getCurrentUser()?.name,
                        isTyped = isTyped
                    )

                    scriptsDataHelper.insertUserScripts(subScripts)
                    navController?.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
}
