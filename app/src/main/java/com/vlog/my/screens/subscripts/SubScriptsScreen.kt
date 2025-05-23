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
import com.vlog.my.data.scripts.ContentType // Added import
import com.vlog.my.data.scripts.SubScriptsDataHelper
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.navigation.Screen
import com.vlog.my.screens.subscripts.components.ScriptsLogoEditDialog
import com.vlog.my.screens.subscripts.components.SubScriptsItem
import com.vlog.my.screens.subscripts.components.PublishScriptDialog
import com.vlog.my.screens.users.UserViewModel
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.screens.bazaar.BazaarScriptsViewModel
import com.vlog.my.screens.videos.components.PasswordDialogMode
import com.vlog.my.screens.videos.components.PasswordPromptDialog
import com.vlog.my.utils.PasswordUtils

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

    // 获取当前用户信息
    val currentUser by userViewModel.currentUser.collectAsState()
    val isLoggedIn = userViewModel.isLoggedIn()
    
    // 状态变量
    var userScriptList by remember { mutableStateOf<List<SubScripts>>(emptyList()) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showLogoEditDialog by remember { mutableStateOf(false) }
    var showPublishDialog by remember { mutableStateOf(false) }
    var selectedSubScriptsForDeletion by remember { mutableStateOf<SubScripts?>(null) } // Renamed for clarity
    var selectedLogoSubScripts by remember { mutableStateOf<SubScripts?>(null) }
    var selectedPublishSubScripts by remember { mutableStateOf<SubScripts?>(null) }

    // States for Password Dialog
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentScriptForPassword by remember { mutableStateOf<SubScripts?>(null) }
    var passwordDialogMode by remember { mutableStateOf(PasswordDialogMode.ENTER_PASSWORD) }
    var passwordErrorMessage by remember { mutableStateOf<String?>(null) }
    
//    // 获取数据库路径
//    val articlesScriptsDataHelper = remember { ArticlesScriptsDataHelper(context) }
//    val databasePath = remember { articlesScriptsDataHelper.getDatabasePath() }
    
    // 加载API配置列表
    LaunchedEffect(Unit) {
        userScriptList = scriptsDataHelper.getAllUserScripts()
    }

    // Delete confirmation dialog
    if (showDeleteDialog && selectedSubScriptsForDeletion != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirm Delete") },
            text = { Text("Are you sure you want to delete API config '${selectedSubScriptsForDeletion?.name} - ${selectedSubScriptsForDeletion?.id}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedSubScriptsForDeletion?.id?.let { scriptsDataHelper.deleteUserScripts(it) }
                        userScriptList = scriptsDataHelper.getAllUserScripts() // Refresh list
                        showDeleteDialog = false
                        selectedSubScriptsForDeletion = null
                    }
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false; selectedSubScriptsForDeletion = null }) { Text("Cancel") }
            }
        )
    }

    // Password Prompt Dialog
    if (showPasswordDialog && currentScriptForPassword != null) {
        PasswordPromptDialog(
            mode = passwordDialogMode,
            onDismiss = {
                showPasswordDialog = false
                currentScriptForPassword = null
                passwordErrorMessage = null
            },
            onPasswordSet = { password ->
                // This is for SET_PASSWORD mode
                currentScriptForPassword?.let { script ->
                    val storedPasswordHash = PasswordUtils.generateStoredPassword(password)
                    val success = scriptsDataHelper.updateScriptPasswordHash(script.id!!, storedPasswordHash)
                    if (success > 0) {
                        // Update in-memory list as well, or refetch
                        userScriptList = scriptsDataHelper.getAllUserScripts()
                        showPasswordDialog = false
                        navController?.navigate(Screen.VideoList.createRoute(script.id!!, script.databaseName!!))
                    } else {
                        passwordErrorMessage = "Failed to set password. Please try again."
                    }
                }
            },
            onPasswordEntered = { password ->
                // This is for ENTER_PASSWORD mode
                currentScriptForPassword?.let { script ->
                    val storedHash = script.scriptPasswordHash // Fetched earlier
                    if (PasswordUtils.verifyPassword(password, storedHash)) {
                        showPasswordDialog = false
                        navController?.navigate(Screen.VideoList.createRoute(script.id!!, script.databaseName!!))
                    } else {
                        passwordErrorMessage = "Incorrect password. Please try again."
                    }
                }
            },
            errorMessage = passwordErrorMessage
        )
    }
    
    // Logo edit dialog
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
                                selectedSubScriptsForDeletion = userScripts // Use the renamed state var
                                showDeleteDialog = true
                            },
                            onFetchDataClick = { clickedUserScript ->
                                if (clickedUserScript.contentType == ContentType.VIDEOS.typeId) {
                                    // Fetch the full script details to get the latest password hash
                                    val fullScriptDetails = clickedUserScript.id?.let { scriptsDataHelper.getUserScriptsById(it) }
                                    if (fullScriptDetails?.databaseName == null) {
                                         println("Error: Database name is null for script ${fullScriptDetails?.id}")
                                        return@onFetchDataClick
                                    }
                                    currentScriptForPassword = fullScriptDetails // Store the fetched script
                                    passwordErrorMessage = null // Clear previous errors

                                    if (fullScriptDetails.scriptPasswordHash.isNullOrEmpty()) {
                                        passwordDialogMode = PasswordDialogMode.SET_PASSWORD
                                    } else {
                                        passwordDialogMode = PasswordDialogMode.ENTER_PASSWORD
                                    }
                                    showPasswordDialog = true
                                } else {
                                    navController?.navigate("item_list/${clickedUserScript.id}")
                                }
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
                            }
                        )
                    }
                }
            }
        }
    }
}