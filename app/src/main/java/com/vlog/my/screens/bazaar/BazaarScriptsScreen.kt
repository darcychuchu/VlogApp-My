package com.vlog.my.screens.bazaar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vlog.my.data.bazaar.BazaarScripts
import com.vlog.my.data.scripts.ContentType // For EBOOK_TYPE_ID
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.di.Constants.IMAGE_BASE_URL
import kotlinx.coroutines.launch
import android.app.Application // For BazaarDownloadViewModelFactory

// Define EBOOK_TYPE_ID if not universally available
private const val EBOOK_TYPE_ID = 4 // Matches ContentType.EBOOK.typeId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BazaarScriptsScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    viewModel: BazaarScriptsViewModel = hiltViewModel(),
    // Manually creating factory for BazaarDownloadViewModel as it's not Hilt-managed here
    downloadViewModelFactory: BazaarDownloadViewModelFactory = BazaarDownloadViewModelFactory(
        LocalContext.current.applicationContext as Application,
        SubScriptsDataHelper(LocalContext.current.applicationContext)
    ),
    downloadViewModel: BazaarDownloadViewModel = viewModel(factory = downloadViewModelFactory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTabIndex by viewModel.selectedTabIndex.collectAsState()
    val serverScripts by viewModel.serverScripts.collectAsState()
    val localScripts by viewModel.localScripts.collectAsState()
    val selectedScript by viewModel.selectedScript.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 显示消息
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearMessage()
            }
        }
    }

    // 显示错误
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar("错误: $it")
                viewModel.clearError()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(text = "Bazaar Scripts")
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标签页
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("社区") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("我的") }
                )
            }

            // 内容区域
            Box(modifier = Modifier.fillMaxSize()) {
                when (selectedTabIndex) {
                    0 -> ServerScriptsList(
                        scripts = serverScripts,
                        onScriptClick = { viewModel.selectScript(it) },
                        onDownloadClick = { bazaarScript ->
                            if (bazaarScript.configTyped == EBOOK_TYPE_ID && !bazaarScript.databaseUrl.isNullOrBlank()) {
                                scope.launch {
                                    val success = downloadViewModel.downloadAndIntegrateEbook(bazaarScript)
                                    if (success) {
                                        snackbarHostState.showSnackbar("${bazaarScript.title} downloaded and integrated successfully.")
                                        viewModel.loadLocalScripts() // Refresh local list
                                    } else {
                                        snackbarHostState.showSnackbar("Failed to download/integrate ${bazaarScript.title}.")
                                    }
                                }
                            } else {
                                // Fallback to original download logic for non-ebooks or if URL is missing
                                viewModel.downloadScript(bazaarScript)
                            }
                        },
                        onRefreshClick = { viewModel.loadServerScripts() },
                        isLoading = uiState.isLoading
                    )
                    1 -> LocalScriptsList(
                        scripts = localScripts,
                        onScriptClick = { viewModel.selectScript(it) },
                        onDeleteClick = { viewModel.deleteScript(it) },
                        onRefreshClick = { viewModel.loadLocalScripts() },
                        isLoading = uiState.isLoading
                    )
                }
                
                // 加载指示器
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
    
    // 脚本详情对话框
    if (uiState.showScriptDetails && selectedScript != null) {
        ScriptDetailsDialog(
            script = selectedScript!!,
            onDismiss = { viewModel.closeScriptDetails() }
        )
    }
    
    // 更新对话框
    if (uiState.showUpdateDialog && uiState.scriptToUpdate != null) {
        UpdateScriptDialog(
            script = uiState.scriptToUpdate!!,
            onConfirm = { viewModel.updateScript(it) },
            onDismiss = { viewModel.dismissUpdateDialog() }
        )
    }
}

@Composable
fun ServerScriptsList(
    scripts: List<BazaarScripts>,
    onScriptClick: (BazaarScripts) -> Unit,
    onDownloadClick: (BazaarScripts) -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 刷新按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onRefreshClick, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }
        
        if (scripts.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无数据，点击刷新按钮获取数据")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
            ) {
                items(scripts) { script ->
                    ScriptItem(
                        script = script,
                        onClick = { onScriptClick(script) },
                        actionIcon = Icons.Default.Download,
                        onActionClick = { onDownloadClick(script) },
                        actionDescription = "下载"
                    )
                }
            }
        }
    }
}

@Composable
fun LocalScriptsList(
    scripts: List<BazaarScripts>,
    onScriptClick: (BazaarScripts) -> Unit,
    onDeleteClick: (BazaarScripts) -> Unit,
    onRefreshClick: () -> Unit,
    isLoading: Boolean
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // 刷新按钮
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onRefreshClick, enabled = !isLoading) {
                Icon(Icons.Default.Refresh, contentDescription = "刷新")
            }
        }
        
        if (scripts.isEmpty() && !isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("暂无数据，请从社区下载脚本")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(8.dp)
            ) {
                items(scripts) { script ->
                    ScriptItem(
                        script = script,
                        onClick = { onScriptClick(script) },
                        actionIcon = Icons.Default.Delete,
                        onActionClick = { onDeleteClick(script) },
                        actionDescription = "删除"
                    )
                }
            }
        }
    }
}

@Composable
fun ScriptItem(
    script: BazaarScripts,
    onClick: () -> Unit,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onActionClick: () -> Unit,
    actionDescription: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (script.attachmentId != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data("${IMAGE_BASE_URL}${script.attachmentId}")
                            .crossfade(true)
                            .build(),
                        contentDescription = "Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text = script.title?.take(1)?.uppercase() ?: "",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 信息
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = script.title ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "版本: ${script.version ?: 0}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 操作按钮
            IconButton(onClick = onActionClick) {
                Icon(
                    imageVector = actionIcon,
                    contentDescription = actionDescription
                )
            }
        }
    }
}

@Composable
fun ScriptDetailsDialog(
    script: BazaarScripts,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "脚本详情",
                        style = MaterialTheme.typography.titleLarge
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "关闭")
                    }
                }
                
                // Logo和名称
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Logo
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (script.attachmentId != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("${IMAGE_BASE_URL}${script.attachmentId}")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Logo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = script.title.toString(),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = script.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "版本: ${script.version}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                
                // 详细信息
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    InfoRow("类型", when(script.isTyped) {
                        0 -> "文章/新闻"
                        1 -> "音乐/音频"
                        2 -> "电影/视频"
                        3 -> "电子书"
                        else -> "未知"
                    })
                    
                    InfoRow("状态", if (script.isEnabled == 0) "本地" else "在线")
                    
                    InfoRow("锁定状态", if (script.isLocked == 0) "免费" else "贡献值")

                    script.createdBy?.let { InfoRow("创建者", it) }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("关闭")
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp)
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun UpdateScriptDialog(
    script: BazaarScripts,
    onConfirm: (BazaarScripts) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("发现更新") },
        text = { 
            Text("脚本 '${script.title}' 有新版本可用，是否更新？\n当前版本: ${script.version}")
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(script) }) {
                Text("更新")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

