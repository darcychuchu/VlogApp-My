package com.vlog.my.screens.subscripts.share

///**
// * 小程序信息数据类
// */
//data class SubScripts(
//    val id: String,
//    val title: String,
//    val description: String = "",
//    val tags: String = "",
//    val createdBy: String = "",
//    val createdAt: Long = 0
//)
//
///**
// * 分享小程序界面
// * 允许用户将当前数据库和配置分享到服务器
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ShareSubScriptsScreen(
//    modifier: Modifier = Modifier,
//    navController: NavController? = null,
//    databaseName: String? = null,
//    userViewModel: UserViewModel = hiltViewModel()
//) {
//    val context = LocalContext.current
//
//    // 获取当前用户信息
//    val currentUser by userViewModel.currentUser.collectAsState()
//    val isLoggedIn = userViewModel.isLoggedIn()
//
//
//    val coroutineScope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // 状态变量
//    var title by remember { mutableStateOf("") }
//    var description by remember { mutableStateOf("") }
//    var tags by remember { mutableStateOf("") }
//    var isUploading by remember { mutableStateOf(false) }
//    var username by remember { mutableStateOf("") }
//    var token by remember { mutableStateOf("") }
//
//    // 从SharedPreferences加载用户信息
//    LaunchedEffect(Unit) {
//        //val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
//        username = currentUser?.name ?: ""
//        token = currentUser?.accessToken ?: ""
//    }
//
////    // 确定要使用的数据库名称
////    val dbNameToUse = databaseName ?: run {
////        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
////        sharedPreferences.getString("custom_database_name", "") ?: ""
////    }
////
////    // 数据库文件路径
////    val dbFile = if (dbNameToUse.isBlank()) {
////        File(context.getDatabasePath("stories.db").path)
////    } else {
////        File(context.getDatabasePath("$dbNameToUse.db").path)
////    }
//
//    // 创建配置文件内容
//    val configContent = remember {
//        val dbHelper = LocalDataHelper(context)
//
//        // 获取所有API配置并转换为JSON字符串
//        val apiConfigs = dbHelper.getAllUserScripts()
//        val configJson = StringBuilder()
//        configJson.append("[")
//        apiConfigs.forEachIndexed { index, config ->
//            configJson.append("{")
//            configJson.append("\"id\":${config.id},")
//            configJson.append("\"isTyped\":${config.isTyped},")
//            configJson.append("\"name\":\"${config.name}\",")
//            configJson.append("\"url\":\"${config.url}\",")
//            config.listUrl?.let { configJson.append("\"listUrl\":\"$it\",") }
//            config.logoUrl?.let { configJson.append("\"logoUrl\":\"$it\",") }
//            config.apiKey?.let { configJson.append("\"apiKey\":\"$it\",") }
//            config.databaseName?.let { configJson.append("\"databaseName\":\"$it\",") }
//            configJson.append("\"mappingConfig\":\"${config.mappingConfig.replace("\"", "\\\"")}\"")
//            configJson.append("}")
//            if (index < apiConfigs.size - 1) configJson.append(",")
//        }
//        configJson.append("]")
//        configJson.toString()
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text("分享小程序") },
//                navigationIcon = {
//                    IconButton(onClick = { navController?.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp)
//                .verticalScroll(rememberScrollState()),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // 标题
//            Text(
//                text = "分享您的小程序",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold
//            )
//
//            // 数据库信息
//            Card {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = "数据库信息",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
////                    Text("数据库名称: ${if (dbNameToUse.isBlank()) "stories.db (默认)" else "$dbNameToUse.db"}")
////                    Text("文件路径: ${dbFile.path}")
////                    Text("文件大小: ${dbFile.length() / 1024} KB")
//                }
//            }
//
//            // 用户信息
//            if (isLoggedIn) {
////                Card {
////                    Column(modifier = Modifier.padding(16.dp)) {
////                        Text(
////                            text = "用户信息未设置",
////                            style = MaterialTheme.typography.titleMedium,
////                            fontWeight = FontWeight.Bold,
////                            color = MaterialTheme.colorScheme.error
////                        )
////                        Spacer(modifier = Modifier.height(8.dp))
////                        Text("请先设置用户名和令牌才能分享小程序")
////                        Spacer(modifier = Modifier.height(8.dp))
////                        OutlinedTextField(
////                            value = username,
////                            onValueChange = { username = it },
////                            label = { Text("用户名") },
////                            modifier = Modifier.fillMaxWidth()
////                        )
////                        Spacer(modifier = Modifier.height(8.dp))
////                        OutlinedTextField(
////                            value = token,
////                            onValueChange = { token = it },
////                            label = { Text("令牌") },
////                            modifier = Modifier.fillMaxWidth()
////                        )
////                        Spacer(modifier = Modifier.height(8.dp))
////                        Button(
////                            onClick = {
////                                // 保存用户信息
////                                val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
////                                sharedPreferences.edit().apply {
////                                    putString("username", username)
////                                    putString("token", token)
////                                    apply()
////                                }
////                                coroutineScope.launch {
////                                    snackbarHostState.showSnackbar("用户信息已保存")
////                                }
////                            },
////                            modifier = Modifier.align(Alignment.End)
////                        ) {
////                            Text("保存用户信息")
////                        }
////                    }
////                }
////            } else {
//                Card {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = "用户信息",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text("用户名: $username")
//                        Text("令牌: ${token.take(8)}...")
//                    }
//                }
//            }
//
//            // 分享信息表单
//            Card {
//                Column(modifier = Modifier.padding(16.dp)) {
//                    Text(
//                        text = "分享信息",
//                        style = MaterialTheme.typography.titleMedium,
//                        fontWeight = FontWeight.Bold
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedTextField(
//                        value = title,
//                        onValueChange = { title = it },
//                        label = { Text("标题 *") },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedTextField(
//                        value = description,
//                        onValueChange = { description = it },
//                        label = { Text("描述") },
//                        modifier = Modifier.fillMaxWidth(),
//                        minLines = 3
//                    )
//                    Spacer(modifier = Modifier.height(8.dp))
//                    OutlinedTextField(
//                        value = tags,
//                        onValueChange = { tags = it },
//                        label = { Text("标签 (用逗号分隔)") },
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            }
//
//            // 分享按钮
//            Button(
//                onClick = {
//                    if (title.isBlank()) {
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("请输入标题")
//                        }
//                        return@Button
//                    }
//
//                    if (username.isBlank() || token.isBlank()) {
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("请先设置用户名和令牌")
//                        }
//                        return@Button
//                    }
//
//                    // 开始上传
//                    isUploading = true
//                    coroutineScope.launch {
//                        try {
//                            // 创建OkHttpClient实例
//                            val client = OkHttpClient()
//
//                            // 获取数据库中的API配置和数据
//                            val dbHelper = LocalDataHelper(context)
////                                if (dbNameToUse.isBlank()) {
////                                LocalDataHelper(context)
////                            } else {
////                                LocalDataHelper(context, "$dbNameToUse.db")
////                            }
//
//                            // 获取API配置列表
//                            val userScripts = dbHelper.getUserScriptsById("---")
//
//                            // 检查是否有API配置
//                            if (userScripts != null) {
//                                withContext(Dispatchers.Main) {
//                                    snackbarHostState.showSnackbar("分享失败: 数据库中没有API配置信息")
//                                    isUploading = false
//                                }
//                                return@launch
//                            }
//
//                            // 创建MultipartBody
//                            val requestBody = MultipartBody.Builder()
//                                .setType(MultipartBody.FORM)
//                                .addFormDataPart("username", username)
//                                .addFormDataPart("token", token)
////                                .addFormDataPart(
////                                    "appFile",
////                                    apiConfig?.databaseName,
////                                    dbFile.asRequestBody("application/octet-stream".toMediaTypeOrNull())
////                                )
//                                .addFormDataPart("title", title)
//                                .addFormDataPart("description", description)
//                                .addFormDataPart("tags", tags)
//                                .addFormDataPart("shareContent", configContent)
//                                .build()
//
//                            // 创建请求，添加API配置信息作为HTTP头部
//                            val request = Request.Builder()
//                                .url("${Constants.API_BASE_URL}/app-shared")
//                                .header("Share-Content", configContent) // 添加API配置信息作为HTTP头部
//                                .post(requestBody)
//                                .build()
//
//                            // 执行请求
//                            withContext(Dispatchers.IO) {
//                                client.newCall(request).execute().use { response ->
//                                    if (response.isSuccessful) {
//                                        val responseBody = response.body?.string() ?: ""
//                                        try {
//                                            // 尝试解析响应
//                                            val jsonObject = JSONObject(responseBody)
//                                            val success = jsonObject.optBoolean("success", false)
//                                            val message = jsonObject.optString("message", "")
//                                            val appId = jsonObject.optJSONObject("data")?.optString("id", "")
//
//                                            withContext(Dispatchers.Main) {
//                                                if (success) {
//                                                    snackbarHostState.showSnackbar("分享成功！小程序ID: $appId")
//                                                    // 分享成功后清空表单
//                                                    title = ""
//                                                    description = ""
//                                                    tags = ""
//                                                } else {
//                                                    snackbarHostState.showSnackbar("分享失败: $message")
//                                                }
//                                            }
//                                        } catch (e: Exception) {
//                                            withContext(Dispatchers.Main) {
//                                                snackbarHostState.showSnackbar("分享成功！但解析响应失败")
//                                                // 分享成功后清空表单
//                                                title = ""
//                                                description = ""
//                                                tags = ""
//                                            }
//                                        }
//                                    } else {
//                                        withContext(Dispatchers.Main) {
//                                            snackbarHostState.showSnackbar("分享失败: ${response.message}")
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            withContext(Dispatchers.Main) {
//                                snackbarHostState.showSnackbar("分享失败: ${e.message}")
//                            }
//                        } finally {
//                            isUploading = false
//                        }
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isUploading
//            ) {
//                if (isUploading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                } else {
//                    Icon(
//                        imageVector = Icons.Default.Share,
//                        contentDescription = "分享",
//                        modifier = Modifier.padding(end = 8.dp)
//                    )
//                    Text("分享小程序")
//                }
//            }
//        }
//    }
//}
//
///**
// * 下载小程序界面
// * 允许用户从服务器下载小程序
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun DownloadAppScreen(
//    modifier: Modifier = Modifier,
//    navController: NavController? = null
//) {
//    val context = LocalContext.current
//    val coroutineScope = rememberCoroutineScope()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // 状态变量
//    var appId by remember { mutableStateOf("") }
//    var isDownloading by remember { mutableStateOf(false) }
//    var isLoading by remember { mutableStateOf(false) }
//    var username by remember { mutableStateOf("") }
//    var token by remember { mutableStateOf("") }
//    var appList by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
//    var selectedAppId by remember { mutableStateOf<String?>(null) }
//
//    // 从SharedPreferences加载用户信息
//    LaunchedEffect(Unit) {
//        val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
//        username = sharedPreferences.getString("username", "") ?: ""
//        token = sharedPreferences.getString("token", "") ?: ""
//
//        // 如果用户信息已设置，自动加载小程序列表
////        if (username.isNotBlank() && token.isNotBlank()) {
//////            loadAppList(username, token, onSuccess = { list ->
//////                appList = list
//////            }, onError = { error ->
//////                coroutineScope.launch {
//////                    snackbarHostState.showSnackbar("获取小程序列表失败: $error")
//////                }
//////            }, onLoading = { loading ->
//////                isLoading = loading
//////            })
////        }
//    }
//
//    // 加载小程序列表函数
//    fun loadAppList(username: String, token: String, onSuccess: (List<AppInfo>) -> Unit, onError: (String) -> Unit, onLoading: (Boolean) -> Unit) {
//        onLoading(true)
//        coroutineScope.launch {
//            try {
//                // 创建OkHttpClient实例
//                val client = OkHttpClient()
//
//                // 创建请求
//                val request = Request.Builder()
//                    .url("${Constants.API_BASE_URL}/app-list?username=$username&token=$token")
//                    .get()
//                    .build()
//
//                // 执行请求
//                withContext(Dispatchers.IO) {
//                    client.newCall(request).execute().use { response ->
//                        if (response.isSuccessful) {
//                            val responseBody = response.body?.string()
//                            if (responseBody != null) {
//                                try {
//                                    // 解析JSON响应
//                                    val jsonObject = JSONObject(responseBody)
//                                    val dataArray = jsonObject.optJSONArray("data") ?: JSONArray()
//                                    val apps = mutableListOf<AppInfo>()
//
//                                    for (i in 0 until dataArray.length()) {
//                                        val appObject = dataArray.getJSONObject(i)
//                                        val app = AppInfo(
//                                            id = appObject.getString("id"),
//                                            title = appObject.getString("title"),
//                                            description = appObject.optString("description", ""),
//                                            tags = appObject.optString("tags", ""),
//                                            createdBy = appObject.optString("createdBy", ""),
//                                            createdAt = appObject.optLong("createdAt", 0)
//                                        )
//                                        apps.add(app)
//                                    }
//
//                                    withContext(Dispatchers.Main) {
//                                        onSuccess(apps)
//                                    }
//                                } catch (e: Exception) {
//                                    withContext(Dispatchers.Main) {
//                                        onError("解析响应失败: ${e.message}")
//                                    }
//                                }
//                            } else {
//                                withContext(Dispatchers.Main) {
//                                    onError("响应为空")
//                                }
//                            }
//                        } else {
//                            withContext(Dispatchers.Main) {
//                                onError("请求失败: ${response.message}")
//                            }
//                        }
//                    }
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    onError("网络错误: ${e.message}")
//                }
//            } finally {
//                withContext(Dispatchers.Main) {
//                    onLoading(false)
//                }
//            }
//        }
//    }
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            CenterAlignedTopAppBar(
//                title = { Text("下载小程序") },
//                navigationIcon = {
//                    IconButton(onClick = { navController?.popBackStack() }) {
//                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
//                    }
//                },
//                actions = {
//                    if (username.isNotBlank() && token.isNotBlank()) {
//                        IconButton(onClick = {
//                            // 刷新小程序列表
//                            loadAppList(username, token, onSuccess = { list ->
//                                appList = list
//                            }, onError = { error ->
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar("获取小程序列表失败: $error")
//                                }
//                            }, onLoading = { loading ->
//                                isLoading = loading
//                            })
//                        }) {
//                            Icon(Icons.Default.Refresh, contentDescription = "刷新列表")
//                        }
//                    }
//                }
//            )
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//                .padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            // 标题
//            Text(
//                text = "下载小程序",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold
//            )
//
//            // 用户信息
//            if (username.isBlank() || token.isBlank()) {
//                Card {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = "用户信息未设置",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold,
//                            color = MaterialTheme.colorScheme.error
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text("请先设置用户名和令牌才能下载小程序")
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = username,
//                            onValueChange = { username = it },
//                            label = { Text("用户名") },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = token,
//                            onValueChange = { token = it },
//                            label = { Text("令牌") },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Button(
//                            onClick = {
//                                // 保存用户信息
//                                val sharedPreferences = context.getSharedPreferences("user_preferences", Context.MODE_PRIVATE)
//                                sharedPreferences.edit().apply {
//                                    putString("username", username)
//                                    putString("token", token)
//                                    apply()
//                                }
//                                coroutineScope.launch {
//                                    snackbarHostState.showSnackbar("用户信息已保存")
//                                }
//
//                                // 保存后自动加载小程序列表
//                                if (username.isNotBlank() && token.isNotBlank()) {
//                                    loadAppList(username, token, onSuccess = { list ->
//                                        appList = list
//                                    }, onError = { error ->
//                                        coroutineScope.launch {
//                                            snackbarHostState.showSnackbar("获取小程序列表失败: $error")
//                                        }
//                                    }, onLoading = { loading ->
//                                        isLoading = loading
//                                    })
//                                }
//                            },
//                            modifier = Modifier.align(Alignment.End)
//                        ) {
//                            Text("保存用户信息")
//                        }
//                    }
//                }
//            } else {
//                Card {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = "用户信息",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        Text("用户名: $username")
//                        Text("令牌: ${token.take(8)}...")
//                    }
//                }
//            }
//
//            // 小程序列表或手动输入ID
//            if (username.isNotBlank() && token.isNotBlank()) {
//                // 小程序列表
//                Card {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween,
//                            verticalAlignment = Alignment.CenterVertically
//                        ) {
//                            Text(
//                                text = "可用小程序列表",
//                                style = MaterialTheme.typography.titleMedium,
//                                fontWeight = FontWeight.Bold
//                            )
//
//                            if (isLoading) {
//                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
//                            }
//                        }
//
//                        Spacer(modifier = Modifier.height(8.dp))
//
//                        if (appList.isEmpty() && !isLoading) {
//                            Text("暂无可用小程序，请点击右上角刷新按钮重试")
//                        } else {
//                            LazyColumn(
//                                modifier = Modifier
//                                    .fillMaxWidth()
//                                    .heightIn(max = 300.dp)
//                            ) {
//                                items(appList) { app ->
//                                    AppListItem(
//                                        app = app,
//                                        isSelected = selectedAppId == app.id,
//                                        onClick = {
//                                            selectedAppId = app.id
//                                            appId = app.id
//                                        }
//                                    )
//                                }
//                            }
//                        }
//                    }
//                }
//
//                // 手动输入ID
//                Card {
//                    Column(modifier = Modifier.padding(16.dp)) {
//                        Text(
//                            text = "或手动输入小程序ID",
//                            style = MaterialTheme.typography.titleMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        Spacer(modifier = Modifier.height(8.dp))
//                        OutlinedTextField(
//                            value = appId,
//                            onValueChange = {
//                                appId = it
//                                // 如果手动输入ID，清除选中状态
//                                if (selectedAppId != null && it != selectedAppId) {
//                                    selectedAppId = null
//                                }
//                            },
//                            label = { Text("小程序ID") },
//                            modifier = Modifier.fillMaxWidth()
//                        )
//                    }
//                }
//            }
//
//            // 下载按钮
//            Button(
//                onClick = {
//                    if (appId.isBlank()) {
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("请选择或输入小程序ID")
//                        }
//                        return@Button
//                    }
//
//                    if (username.isBlank() || token.isBlank()) {
//                        coroutineScope.launch {
//                            snackbarHostState.showSnackbar("请先设置用户名和令牌")
//                        }
//                        return@Button
//                    }
//
//                    // 开始下载
//                    isDownloading = true
//                    coroutineScope.launch {
//                        try {
//                            // 创建OkHttpClient实例
//                            val client = OkHttpClient()
//
//                            // 创建请求
//                            val request = Request.Builder()
//                                .url("${Constants.API_BASE_URL}/app-download?appId=$appId&username=$username&token=$token")
//                                .get()
//                                .build()
//
//                            // 执行请求
//                            withContext(Dispatchers.IO) {
//                                client.newCall(request).execute().use { response ->
//                                    if (response.isSuccessful) {
//                                        // 保存数据库文件
//                                        val responseBody = response.body
//                                        if (responseBody != null) {
//                                            // 创建一个新的数据库文件名
//                                            val newDbName = "downloaded_$appId.db"
//                                            val newDbFile = File(context.getDatabasePath(newDbName).path)
//
//                                            // 确保父目录存在
//                                            newDbFile.parentFile?.mkdirs()
//
//                                            // 写入文件
//                                            FileOutputStream(newDbFile).use { fileOutputStream ->
//                                                responseBody.byteStream().use { inputStream ->
//                                                    val buffer = ByteArray(4096)
//                                                    var bytesRead: Int
//                                                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
//                                                        fileOutputStream.write(buffer, 0, bytesRead)
//                                                    }
//                                                }
//                                            }
//
//                                            // 获取API配置信息
//                                            val shareContentHeader = response.header("Share-Content")
//
//                                            // 保存为当前数据库
//                                            val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
//                                            sharedPreferences.edit().apply {
//                                                putString("custom_database_name", newDbName.removeSuffix(".db"))
//                                                apply()
//                                            }
//
//                                            // 如果有API配置信息，则保存到数据库
//                                            if (!shareContentHeader.isNullOrBlank()) {
//                                                try {
//                                                    // 创建新的LocalDataHelper实例，使用下载的数据库
//                                                    val dbHelper = LocalDataHelper(context, newDbName)
//
//                                                    // 解析API配置信息
//                                                    val apiConfigsJson = JSONArray(shareContentHeader)
//                                                    for (i in 0 until apiConfigsJson.length()) {
//                                                        val configJson = apiConfigsJson.getJSONObject(i)
//                                                        val apiConfig = ApiConfig(
//                                                            name = configJson.getString("name"),
//                                                            url = configJson.getString("url"),
//                                                            listUrl = if (configJson.has("listUrl")) configJson.getString("listUrl") else null,
//                                                            logoUrl = if (configJson.has("logoUrl")) configJson.getString("logoUrl") else null,
//                                                            apiKey = if (configJson.has("apiKey")) configJson.getString("apiKey") else null,
//                                                            mappingConfig = configJson.getString("mappingConfig"),
//                                                            databaseName = if (configJson.has("databaseName")) configJson.getString("databaseName") else null
//                                                        )
//                                                        // 插入API配置
//                                                        dbHelper.insertApiConfig(apiConfig)
//                                                    }
//                                                } catch (e: Exception) {
//                                                    e.printStackTrace()
//                                                    // 错误处理，但不影响主流程
//                                                }
//                                            }
//
//                                            withContext(Dispatchers.Main) {
//                                                snackbarHostState.showSnackbar("下载成功！数据库已设置为: $newDbName")
//                                                // 下载成功后清空表单
//                                                appId = ""
//                                                selectedAppId = null
//
//                                                // 导航回设置页面
//                                                navController?.popBackStack()
//                                            }
//                                        } else {
//                                            withContext(Dispatchers.Main) {
//                                                snackbarHostState.showSnackbar("下载失败: 响应为空")
//                                            }
//                                        }
//                                    } else {
//                                        withContext(Dispatchers.Main) {
//                                            snackbarHostState.showSnackbar("下载失败: ${response.message}")
//                                        }
//                                    }
//                                }
//                            }
//                        } catch (e: Exception) {
//                            withContext(Dispatchers.Main) {
//                                snackbarHostState.showSnackbar("下载失败: ${e.message}")
//                            }
//                        } finally {
//                            isDownloading = false
//                        }
//                    }
//                },
//                modifier = Modifier.fillMaxWidth(),
//                enabled = !isDownloading && !isLoading
//            ) {
//                if (isDownloading) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(24.dp),
//                        color = MaterialTheme.colorScheme.onPrimary
//                    )
//                } else {
//                    Icon(
//                        imageVector = Icons.Default.Download,
//                        contentDescription = "下载",
//                        modifier = Modifier.padding(end = 8.dp)
//                    )
//                    Text("下载小程序")
//                }
//            }
//        }
//    }
//}
//
//@Composable
//fun AppListItem(
//    app: AppInfo,
//    isSelected: Boolean,
//    onClick: () -> Unit
//) {
//    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
//    val formattedDate = remember(app.createdAt) {
//        if (app.createdAt > 0) dateFormat.format(Date(app.createdAt)) else "未知时间"
//    }
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 4.dp)
//            .clip(RoundedCornerShape(8.dp))
//            .border(
//                width = if (isSelected) 2.dp else 0.dp,
//                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
//                shape = RoundedCornerShape(8.dp)
//            )
//            .clickable { onClick() }
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(12.dp)
//        ) {
//            Text(
//                text = app.title,
//                style = MaterialTheme.typography.titleMedium,
//                fontWeight = FontWeight.Bold
//            )
//
//            if (app.description.isNotBlank()) {
//                Spacer(modifier = Modifier.height(4.dp))
//                Text(
//                    text = app.description,
//                    style = MaterialTheme.typography.bodyMedium,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                if (app.tags.isNotBlank()) {
//                    Text(
//                        text = "标签: ${app.tags}",
//                        style = MaterialTheme.typography.bodySmall
//                    )
//                }
//
//                Text(
//                    text = "ID: ${app.id}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//
//            Spacer(modifier = Modifier.height(4.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//                Text(
//                    text = "创建者: ${app.createdBy}",
//                    style = MaterialTheme.typography.bodySmall
//                )
//
//                Text(
//                    text = formattedDate,
//                    style = MaterialTheme.typography.bodySmall
//                )
//            }
//        }
//    }
//}
