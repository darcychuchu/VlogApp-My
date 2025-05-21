package com.vlog.my.screens.scripts.articles

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.vlog.my.data.LocalDataHelper
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.articles.ArticlesCategories
import com.vlog.my.data.scripts.articles.ArticlesItems
import com.vlog.my.data.scripts.articles.ArticlesScriptsDataHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    itemId: String,
    scriptId: String
) {
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)
    val userScripts = scriptsDataHelper.getUserScriptsById(scriptId)
    val articlesScriptsDataHelper = ArticlesScriptsDataHelper(context, userScripts?.databaseName ?: "sub-scripts.db", scriptId)

    
    // 状态变量
    var item by remember { mutableStateOf<ArticlesItems?>(null) }
    var category by remember { mutableStateOf<ArticlesCategories?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 加载数据
    LaunchedEffect(itemId) {
        item = articlesScriptsDataHelper.getItemById(itemId)
        item?.categoryId?.let { categoryId ->
            category = articlesScriptsDataHelper.getCategoryById(categoryId)
        }
        isLoading = false
    }
    
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 返回按钮
                        IconButton(
                            onClick = { navController?.popBackStack() }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回"
                            )
                        }
                        
                        // 标题
                        Text(
                            text = "内容详情",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge
                        )
                        
                        // 右侧占位
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            // 加载中
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (item == null) {
            // 无数据
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("未找到数据")
            }
        } else {
            // 显示详情
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 标题
                Text(
                    text = item?.title ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 图片
                item?.pic?.let { picUrl ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = picUrl,
                            contentDescription = item?.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // 分类
                category?.let {
                    InfoItem(title = "分类", content = it.title)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // ID
                InfoItem(title = "ID", content = item?.id?.toString() ?: "")
                Spacer(modifier = Modifier.height(8.dp))
                
                // 标签
                item?.tags?.let { tags ->
                    if (tags.isNotEmpty()) {
                        InfoItem(title = "标签", content = tags)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                // 内容
                Text(
                    text = "内容",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = item?.content ?: "无内容",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$title: ",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}