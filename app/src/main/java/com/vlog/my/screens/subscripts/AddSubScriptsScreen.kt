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
import kotlin.String


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
    var sourceUrl by remember { mutableStateOf("") }
    var selectedFeedType by remember { mutableStateOf("JSON") } // Default to JSON

    // State variables for new mapping fields
    var articleRootPath by remember { mutableStateOf("") }
    var idField by remember { mutableStateOf("") }
    var titleField by remember { mutableStateOf("") }
    var contentField by remember { mutableStateOf("") }
    var imageUrlField by remember { mutableStateOf("") } // Optional
    var categoryIdField by remember { mutableStateOf("") } // Optional
    var tagsField by remember { mutableStateOf("") } // Optional
    var sourceUrlField_mapping by remember { mutableStateOf("") } // Optional

    var mappingConfig by remember { mutableStateOf("{}") }

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
        // categoriesMapping can be added here if needed in the future
        // mainConfigObject.put("categoriesMapping", JSONObject()) // Example for empty categories mapping

        mappingConfig = mainConfigObject.toString()
    }

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

            // Add Source URL field
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



            // 保存按钮
            Button(
                onClick = {
                    // mappingConfig is already updated by LaunchedEffect
                    val subScripts = SubScripts(
                        name = name,
                        apiKey = apiKey.takeIf { it.isNotEmpty() },
                        mappingConfig = mappingConfig,
                        databaseName = databaseName.takeIf { it.isNotEmpty() },
                        createdBy = viewModel.getCurrentUser()?.name,
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
