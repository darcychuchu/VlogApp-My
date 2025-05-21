package com.vlog.my.screens.subscripts.components

import android.net.Uri
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vlog.my.data.scripts.SubScripts
import com.vlog.my.utils.ImagePickerHelper
import com.vlog.my.utils.rememberImagePicker
import kotlinx.coroutines.launch

/**
 * Logo编辑对话框
 * @param subScripts API配置
 * @param onDismiss 关闭对话框回调
 * @param onLogoUpdated 更新Logo回调，参数为新的Logo路径
 */
@Composable
fun ScriptsLogoEditDialog(
    subScripts: SubScripts,
    onDismiss: () -> Unit,
    onLogoUpdated: (SubScripts, String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 当前选择的图片Uri
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    // 处理后的图片路径
    var processedImagePath by remember { mutableStateOf<String?>(null) }
    // 是否正在处理图片
    var isProcessing by remember { mutableStateOf(false) }
    
    // 图片选择器
    val imagePicker = rememberImagePicker { uri ->
        selectedImageUri = uri
        isProcessing = true
        
        // 在协程中处理图片
        scope.launch {
            try {
                // 压缩图片
                val compressedBitmap = ImagePickerHelper.compressBitmap(
                    context = context,
                    uri = uri,
                    maxWidth = 300,
                    maxHeight = 300,
                    quality = 80
                )
                
                compressedBitmap?.let { bitmap ->
                    // 转换为圆形
                    val circularBitmap = ImagePickerHelper.getCircularBitmap(bitmap)
                    
                    // 保存到文件
                    val filename = "logo_${subScripts.id}_${System.currentTimeMillis()}.png"
                    val filePath = ImagePickerHelper.saveBitmapToFile(
                        context = context,
                        bitmap = circularBitmap,
                        filename = filename
                    )
                    
                    // 更新处理后的图片路径
                    processedImagePath = filePath
                    
                    // 回收Bitmap
                    bitmap.recycle()
                    circularBitmap.recycle()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isProcessing = false
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑Logo") },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 显示当前Logo或选择的新Logo
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    when {
                        // 显示处理后的图片
                        processedImagePath != null -> {
                            AsyncImage(
                                model = processedImagePath,
                                contentDescription = "处理后的Logo",
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // 显示选择的图片
                        selectedImageUri != null -> {
                            if (isProcessing) {
                                // 显示加载中
                                CircularProgressIndicator()
                            } else {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "选择的Logo",
                                    modifier = Modifier.size(120.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                        // 显示现有Logo
                        !subScripts.logoUrl.isNullOrEmpty() -> {
                            AsyncImage(
                                model = subScripts.logoUrl,
                                contentDescription = "现有Logo",
                                modifier = Modifier.size(120.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                        // 显示默认图标
                        else -> {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "默认Logo",
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 选择图片按钮
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从相册选择图片")
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // 取消按钮
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
                
                // 确认按钮
                Button(
                    onClick = {
                        // 如果有处理后的图片，更新Logo
                        processedImagePath?.let { path ->
                            onLogoUpdated(subScripts, path)
                        }
                        onDismiss()
                    },
                    enabled = processedImagePath != null && !isProcessing
                ) {
                    Text("确认")
                }
            }
        }
    )
}