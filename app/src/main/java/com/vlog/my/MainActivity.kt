package com.vlog.my

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.vlog.my.navigation.VlogNavigation
import com.vlog.my.ui.theme.VlogAppMyTheme
import com.vlog.my.utils.StoragePermissionHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // 权限请求启动器
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // 初始化权限请求启动器
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            // 权限授予后的操作
            StoragePermissionHelper.handlePermissionResult(permissions)
        }

        if (!StoragePermissionHelper.hasStoragePermission(this)) {
            StoragePermissionHelper.requestStoragePermission(this, permissionLauncher)
        }
        
        setContent {
            VlogAppMyTheme {
                VlogNavigation()
            }
        }
    }
}
