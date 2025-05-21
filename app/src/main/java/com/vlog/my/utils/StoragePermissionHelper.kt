package com.vlog.my.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

/**
 * 存储权限辅助类，用于处理Android不同版本的存储权限请求
 */
class StoragePermissionHelper {
    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
        
        /**
         * 检查是否有存储权限
         */
        fun hasStoragePermission(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11及以上使用MANAGE_EXTERNAL_STORAGE权限
                Environment.isExternalStorageManager()
            } else {
                // Android 10及以下使用READ_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
            }
        }
        
        /**
         * 请求存储权限
         */
        fun requestStoragePermission(activity: Activity, permissionLauncher: ActivityResultLauncher<Array<String>>? = null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11及以上请求MANAGE_EXTERNAL_STORAGE权限
                try {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", activity.packageName, null)
                    intent.data = uri
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activity.startActivity(intent)
                }
            } else {
                // Android 10及以下请求READ_EXTERNAL_STORAGE和WRITE_EXTERNAL_STORAGE权限
                permissionLauncher?.launch(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            }
        }
        
        /**
         * 处理权限请求结果 - 使用Activity Result API
         */
        fun handlePermissionResult(permissions: Map<String, Boolean>, onPermissionGranted: (() -> Unit)? = null) {
            val allGranted = permissions.entries.all { it.value }
            if (allGranted) {
                onPermissionGranted?.invoke()
            }
        }
        
        /**
         * 处理旧版权限请求结果 - 已废弃，保留向后兼容
         * @Deprecated 使用新的Activity Result API
         */
        @Deprecated("使用新的Activity Result API替代", ReplaceWith("handlePermissionResult(Map<String, Boolean>)"))
        fun handlePermissionResult(requestCode: Int, grantResults: IntArray, onPermissionGranted: () -> Unit) {
            if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    onPermissionGranted()
                }
            }
        }
    }
}