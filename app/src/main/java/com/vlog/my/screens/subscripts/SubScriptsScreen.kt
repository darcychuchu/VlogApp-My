package com.vlog.my.screens.subscripts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.navigation.Screen
import com.vlog.my.screens.subscripts.components.ScriptsLogoEditDialog
import com.vlog.my.screens.subscripts.components.SubScriptsItem
import com.vlog.my.screens.subscripts.components.PublishScriptDialog
import com.vlog.my.screens.users.UserViewModel
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.screens.bazaar.BazaarScriptsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubScriptsScreen(
    modifier: Modifier = Modifier,
    navController: NavController? = null,
    userViewModel: UserViewModel = hiltViewModel()
) {
    // 通过BazaarScriptsViewModel获取BazaarScriptsRepository
    val bazaarViewModel: BazaarScriptsViewModel = hiltViewModel()
    val bazaarScriptsRepository = bazaarViewModel.repository
    val context = LocalContext.current
    val scriptsDataHelper = SubScriptsDataHelper(context)
    var showMenu by remember { mutableStateOf(false) }

    // 获取当前用户信息
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoggedIn = userViewModel.isLoggedIn()
    
    // 状态变量
    var userScriptList by remember { mutableStateOf<List<SubScripts>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoEditDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var selectedSubScripts by remember { mutableStateOf<SubScripts?>(null) }
    var selectedLogoSubScripts by remember { mutableStateOf<SubScripts?>(null) }
    var selectedPublishSubScripts by remember { mutableStateOf<SubScripts?>(null) }
    
//    // 获取数据库路径
//    val articlesScriptsDataHelper = remember { ArticlesScriptsDataHelper(context) }
//    val databasePath = remember { articlesScriptsDataHelper.getDatabasePath() }
    
    // 加载API配置列表
    LaunchedEffect(Unit) {
        userScriptList = scriptsDataHelper.getAllUserScripts()
    }

    // 删除确认对话框
    if (showDeleteDialog && selectedSubScripts != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除API配置 '${selectedSubScripts?.name} - ${selectedSubScripts?.id}' 吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSubScripts?.id?.let { scriptsDataHelper.deleteUserScripts(it) }
                        userScriptList = scriptsDataHelper.getAllUserScripts()
                        showDeleteDialog = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false }
                ) {
                    Text("取消")
                }
            }
        )
    }

    // Logo编辑对话框
    if (showLogoEditDialog && selectedLogoSubScripts != null) {
        ScriptsLogoEditDialog(
            subScripts = selectedLogoSubScripts!!,
            onDismiss = { showLogoEditDialog = false },
            onLogoUpdated = { userScripts, logoPath ->
                scriptsDataHelper.updateUserScriptsForLogo(userScripts.id ?: "",logoPath)
                userScriptList = scriptsDataHelper.getAllUserScripts()
            }
        )
    }
    
    // 发布对话框
    if (showPublishDialog && selectedPublishSubScripts != null && isLoggedIn) {
        val userToken = userViewModel.currentUser.value?.accessToken ?: ""
        PublishScriptDialog(
            subScripts = selectedPublishSubScripts!!,
            bazaarScriptsRepository = bazaarScriptsRepository,
            userToken = userToken,
            onDismiss = { showPublishDialog = false },
            onPublishSuccess = {
                showPublishDialog = false
                // 可以在这里添加发布成功的提示或其他操作
            }
        )
    }

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 中间的标签页
                        Box(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Sub Scripts",
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            )}
        },
        floatingActionButton = {
            Column {
                // 添加API配置按钮
                FloatingActionButton(
                    onClick = {
                        // 导航到添加API配置页面
                        navController?.navigate("script_add")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加API配置")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            
            // 分享和下载按钮
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        // 导航到分享小程序页面
                        //navController?.navigate(Screen.ShareApp.createRoute(customDatabaseName))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "分享",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("分享小程序")
                }
                
                Button(
                    onClick = {
                        // 导航到下载小程序页面
                        navController?.navigate(Screen.DownloadApp.createRoute())
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "下载",
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("下载小程序")
                }
            }
            
            // API配置列表标题
            Text(
                text = "API配置",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            
            // API配置列表
            if (userScriptList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("暂无API配置，点击右下角按钮添加")
                }
            } else {
                LazyColumn {
                    items(userScriptList) { userScripts ->
                        SubScriptsItem(
                            subScripts = userScripts,
                            onEditClick = {
                                navController?.navigate("script_edit/${userScripts.id}")
                            },
                            onDeleteClick = {
                                selectedSubScripts = userScripts
                                showDeleteDialog = true
                            },
                            onFetchDataClick = {
                                navController?.navigate("item_list/${userScripts.id}")
                            },
                            onWorkClick = { userScripts ->
                                navController?.navigate(Screen.Workers.createRoute(userScripts.id ?: ""))
                            },
                            onLogoEditClick = { config ->
                                selectedLogoSubScripts = config
                                showLogoEditDialog = true
                            },
                            onPublishClick = { config ->
                                if (isLoggedIn) {
                                    selectedPublishSubScripts = config
                                    showPublishDialog = true
                                } else {
                                    // 如果未登录，可以导航到登录页面
                                    navController?.navigate("login")
                                }
                            },
                            onManageTracksClick = { clickedUserScript ->
                                if (clickedUserScript.id != null && clickedUserScript.databaseName != null) {
                                    navController?.navigate(Screen.MusicTracks.createRoute(clickedUserScript.id!!, clickedUserScript.databaseName!!))
                                } else {
                                    // Optional: Log an error or show a toast if id or databaseName is null
                                    android.util.Log.e("SubScriptsScreen", "Cannot navigate to MusicTracksScreen: id or databaseName is null. ID: ${clickedUserScript.id}, DBName: ${clickedUserScript.databaseName}")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}