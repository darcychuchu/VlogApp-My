package com.vlog.my.data.scripts.music

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * 音乐文件工具类，用于处理音乐文件的读取、转换和存储
 */
object MusicFileUtils {
    private const val TAG = "MusicFileUtils"
    
    /**
     * 从Uri读取音乐文件并转换为字节数组
     * 
     * @param context 上下文
     * @param uri 音乐文件的Uri
     * @return 音乐文件的字节数组，如果读取失败则返回null
     */
    fun readMusicFileToByteArray(context: Context, uri: Uri): ByteArray? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            }
        } catch (e: IOException) {
            Log.e(TAG, "读取音乐文件失败: ${e.message}")
            null
        }
    }
    
    /**
     * 获取文件名称
     * 
     * @param context 上下文
     * @param uri 文件的Uri
     * @return 文件名称，如果获取失败则返回null
     */
    fun getFileName(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                cursor.moveToFirst()
                cursor.getString(nameIndex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "获取文件名称失败: ${e.message}")
            null
        }
    }
    
    /**
     * 获取文件大小
     * 
     * @param context 上下文
     * @param uri 文件的Uri
     * @return 文件大小（字节），如果获取失败则返回-1
     */
    fun getFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndexOrThrow(OpenableColumns.SIZE)
                cursor.moveToFirst()
                cursor.getLong(sizeIndex)
            } ?: -1
        } catch (e: Exception) {
            Log.e(TAG, "获取文件大小失败: ${e.message}")
            -1
        }
    }
    
    /**
     * 将字节数组保存为临时文件
     * 
     * @param context 上下文
     * @param trackId 音乐轨道ID
     * @param musicData 音乐数据字节数组
     * @return 临时文件的Uri，如果保存失败则返回null
     */
    fun saveBlobToTempFile(context: Context, trackId: String, musicData: ByteArray): Uri? {
        val tempFile = File(context.cacheDir, "temp_audio_${trackId}.dat")
        return try {
            FileOutputStream(tempFile).use { fos ->
                fos.write(musicData)
            }
            Uri.fromFile(tempFile)
        } catch (e: IOException) {
            Log.e(TAG, "保存音乐数据到临时文件失败: ${e.message}")
            null
        }
    }
}