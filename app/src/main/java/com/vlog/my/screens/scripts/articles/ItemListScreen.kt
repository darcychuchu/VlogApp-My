package com.vlog.my.screens.scripts.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.text.style.TextOverflow
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
fun ItemListScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    scriptId: String
) {
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)
    val userScripts = scriptsDataHelper.getUserScriptsById(scriptId)
    val articlesScriptsDataHelper = ArticlesScriptsDataHelper(context, userScripts?.databaseName ?: "sub-scripts.db", scriptId)


    // 状态变量
    var items by remember { mutableStateOf<List<ArticlesItems>>(emptyList()) }
    var categories by remember { mutableStateOf<List<ArticlesCategories>>(emptyList()) }
    val selectedParentCategory  by remember { mutableStateOf<ArticlesCategories>(ArticlesCategories()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // 加载数据
    LaunchedEffect(Unit) {
        items = articlesScriptsDataHelper.getAllItems()
        categories = articlesScriptsDataHelper.getAllCategories()
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
                            text = "内容列表",
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
        } else if (items.isEmpty()) {
            // 无数据
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无数据，请先在设置中配置API并获取数据")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (categories.isNotEmpty()) {
                    ScrollableTabRow(
                        selectedTabIndex = categories.indexOfFirst { it.id == selectedParentCategory.id }
                            .takeIf { it >= 0 } ?: 0,
                        edgePadding = 16.dp
                    ) {
                        categories.forEach { category ->
                            Tab(
                                selected = category.id == selectedParentCategory.id,
                                onClick = {  },
                                text = { Text(category.title) }
                            )
                        }
                    }
                }

                // 显示列表
                LazyColumn() {
                    items(items) { item ->
                        ItemCard(
                            item = item,
                            onClick = {
                                // 导航到详情页
                                item.id?.let { id ->
                                    navController?.navigate("item_detail/$scriptId/$id")
                                }
                            }
                        )
                    }
                }
            }



        }
    }
}

@Composable
fun ItemCard(
    item: ArticlesItems,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图片
            item.pic?.let { picUrl ->
                AsyncImage(
                    model = picUrl,
                    contentDescription = item.title,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            // 标题和ID
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "ID: ${item.id}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

