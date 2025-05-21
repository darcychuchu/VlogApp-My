package com.vlog.my.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 图片选择和处理工具类
 */
class ImagePickerHelper {
    companion object {
        /**
         * 压缩图片
         * @param context 上下文
         * @param uri 图片Uri
         * @param maxWidth 最大宽度
         * @param maxHeight 最大高度
         * @param quality 压缩质量 (0-100)
         * @return 压缩后的Bitmap
         */
        suspend fun compressBitmap(
            context: Context,
            uri: Uri,
            maxWidth: Int = 300,
            maxHeight: Int = 300,
            quality: Int = 80
        ): Bitmap? = withContext(Dispatchers.IO) {
            try {
                // 从Uri加载原始图片
                val inputStream = context.contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                
                if (originalBitmap == null) return@withContext null
                
                // 计算缩放比例
                val width = originalBitmap.width
                val height = originalBitmap.height
                val scaleWidth = maxWidth.toFloat() / width
                val scaleHeight = maxHeight.toFloat() / height
                val scale = scaleWidth.coerceAtMost(scaleHeight)
                
                // 如果图片已经小于目标尺寸，不需要缩放
                if (scale >= 1) return@withContext originalBitmap
                
                // 创建缩放后的Bitmap
                val scaledBitmap = Bitmap.createScaledBitmap(
                    originalBitmap,
                    (width * scale).toInt(),
                    (height * scale).toInt(),
                    true
                )
                
                // 如果原始图片和缩放后的图片不同，回收原始图片
                if (originalBitmap != scaledBitmap) {
                    originalBitmap.recycle()
                }
                
                // 压缩图片质量
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val compressedData = outputStream.toByteArray()
                outputStream.close()
                
                // 从压缩后的数据创建新的Bitmap
                BitmapFactory.decodeByteArray(compressedData, 0, compressedData.size)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        
        /**
         * 将Bitmap转换为圆形
         * @param bitmap 原始Bitmap
         * @return 圆形Bitmap
         */
        fun getCircularBitmap(bitmap: Bitmap): Bitmap {
            val output = Bitmap.createBitmap(
                bitmap.width,
                bitmap.height,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(output)
            
            val paint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.BLACK
            }
            
            val rect = Rect(0, 0, bitmap.width, bitmap.height)
            val radius = bitmap.width.coerceAtMost(bitmap.height) / 2f
            
            canvas.drawCircle(bitmap.width / 2f, bitmap.height / 2f, radius, paint)
            
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(bitmap, rect, rect, paint)
            
            return output
        }
        
        /**
         * 保存Bitmap到文件
         * @param context 上下文
         * @param bitmap 要保存的Bitmap
         * @param filename 文件名
         * @return 保存的文件路径
         */
        suspend fun saveBitmapToFile(
            context: Context,
            bitmap: Bitmap,
            filename: String
        ): String = withContext(Dispatchers.IO) {
            try {
                val file = File(context.filesDir, filename)
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                file.absolutePath
            } catch (e: Exception) {
                e.printStackTrace()
                ""
            }
        }
    }
}

/**
 * Composable函数，用于创建图片选择器
 * @param onImageSelected 图片选择后的回调，返回选择的Uri
 * @return 图片选择器启动器
 */
@Composable
fun rememberImagePicker(
    onImageSelected: (Uri) -> Unit
): ActivityResultLauncher<String> {
    return rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onImageSelected(it) }
    }
}

/**
 * Composable函数，用于处理图片选择、压缩和转换为圆形的完整流程
 * @param onProcessComplete 处理完成后的回调，返回处理后的图片文件路径
 * @return 图片选择器启动器
 */
@Composable
fun rememberImagePickerWithProcessing(
    onProcessComplete: (String) -> Unit
): ActivityResultLauncher<String> {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
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
                        val filename = "logo_${System.currentTimeMillis()}.png"
                        val filePath = ImagePickerHelper.saveBitmapToFile(
                            context = context,
                            bitmap = circularBitmap,
                            filename = filename
                        )
                        
                        // 回调处理完成的文件路径
                        onProcessComplete(filePath)
                        
                        // 回收Bitmap
                        bitmap.recycle()
                        circularBitmap.recycle()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
