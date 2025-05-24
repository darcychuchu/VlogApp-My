package com.vlog.my.screens.subscripts.components

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.vlog.my.data.bazaar.BazaarScriptsRepository
import com.vlog.my.data.scripts.SubScripts
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

@Composable
fun PublishScriptDialog(
    subScripts: SubScripts,
    bazaarScriptsRepository: BazaarScriptsRepository,
    userToken: String,
    onDismiss: () -> Unit,
    onPublishSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    val jsonAdapter: JsonAdapter<SubScripts> = moshi.adapter(SubScripts::class.java)
    
    // 状态变量
    var title by remember { mutableStateOf(subScripts.name ?: "") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isPublishing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var customLogoUri by remember { mutableStateOf<Uri?>(null) }
    
    // 选择图片的启动器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        customLogoUri = uri
    }
    
    Dialog(onDismissRequest = { if (!isPublishing) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "发布小程序到社区",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Logo选择区域
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        customLogoUri != null -> {
                            AsyncImage(
                                model = customLogoUri,
                                contentDescription = "自定义Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        !subScripts.logoUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = subScripts.logoUrl,
                                contentDescription = "当前Logo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "选择Logo",
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Text(
                    text = "点击更换Logo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 标题输入
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("标题") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 描述输入
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    maxLines = 5
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 标签输入
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("标签 (用逗号分隔)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 错误信息显示
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isPublishing
                    ) {
                        Text("取消")
                    }
                    
                    Button(
                        onClick = {
                            // 验证输入
                            when {
                                title.isBlank() -> {
                                    errorMessage = "请输入标题"
                                    return@Button
                                }
                                description.isBlank() -> {
                                    errorMessage = "请输入描述"
                                    return@Button
                                }
                                customLogoUri == null && subScripts.logoUrl.isNullOrEmpty() -> {
                                    errorMessage = "请选择Logo图片"
                                    return@Button
                                }
                                else -> {
                                    errorMessage = null
                                }
                            }
                            
                            // 开始发布
                            isPublishing = true
                            
                            coroutineScope.launch {
                                try {
                                    // 准备Logo文件
                                    val logoFile = prepareLogoFile(context, customLogoUri, subScripts.logoUrl)
                                    if (logoFile == null) {
                                        errorMessage = "Logo文件准备失败"
                                        isPublishing = false
                                        return@launch
                                    }
                                    
                                    // 创建MultipartBody.Part
                                    val requestFile = logoFile.asRequestBody("image/*".toMediaTypeOrNull())
                                    val logoFilePart = MultipartBody.Part.createFormData(
                                        "logoFile",
                                        logoFile.name,
                                        requestFile
                                    )
                                    
                                    // 调用发布API
                                    val response = bazaarScriptsRepository.createScript(
                                        name = subScripts.name,
                                        token = userToken,
                                        logoFile = logoFilePart,
                                        title = title,
                                        description = description,
                                        tags = tags,
                                        mappingConfig = jsonAdapter.toJson(subScripts),
//                                        mappingConfig = JSONObject().apply {
//                                            put("name", subScripts.name)
//                                            put("logoUrl", subScripts.logoUrl)
//                                            put("isTyped", subScripts.isTyped)
//                                            put("mappingConfig", subScripts.mappingConfig)
//                                        }.toString(),
                                        configTyped = subScripts.isTyped,
                                        databaseFile = null
                                    )
                                    
                                    // 处理响应
                                    if (response.code == 200) {
                                        onPublishSuccess()
                                    } else {
                                        errorMessage = response.message ?: "发布失败"
                                        Log.d("isPublishing",errorMessage.toString())
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "发布过程中出错: ${e.message}"
                                    Log.d("isPublishing",errorMessage.toString())
                                } finally {
                                    isPublishing = false
                                }
                            }
                        },
                        enabled = !isPublishing
                    ) {
                        if (isPublishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("发布")
                        }
                    }
                }
            }
        }
    }
}

// 准备Logo文件
private suspend fun prepareLogoFile(context: Context, customLogoUri: Uri?, existingLogoPath: String?): File? {
    return try {
        when {
            // 使用新选择的图片
            customLogoUri != null -> {
                val inputStream = context.contentResolver.openInputStream(customLogoUri)
                val tempFile = File.createTempFile("logo_", ".jpg", context.cacheDir)
                inputStream?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            }
            // 使用现有的Logo
            !existingLogoPath.isNullOrEmpty() -> {
                val logoFile = File(existingLogoPath)
                if (logoFile.exists()) {
                    logoFile
                } else {
                    // 如果是网络URL或其他格式，可能需要下载或转换
                    // 这里简化处理，实际应用中可能需要更复杂的逻辑
                    null
                }
            }
            else -> null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}