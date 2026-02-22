package com.yanbao.camera

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yanbao.camera.presentation.YanbaoApp
import com.yanbao.camera.presentation.theme.YanbaoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /**
     * 所有需要运行时请求的权限（完整版）
     *
     * - CAMERA：相机拍摄
     * - RECORD_AUDIO：录像
     * - ACCESS_FINE_LOCATION：LBS 推荐 + 记忆 GPS 坐标
     * - ACCESS_COARSE_LOCATION：降级定位
     * - READ_MEDIA_IMAGES / READ_MEDIA_VIDEO：Android 13+ 相册扫描
     * - READ_EXTERNAL_STORAGE：Android ≤12 相册扫描
     * - WRITE_EXTERNAL_STORAGE：Android ≤9 写入
     */
    private val requiredPermissions: Array<String>
        get() {
            val base = mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+
                base += Manifest.permission.READ_MEDIA_IMAGES
                base += Manifest.permission.READ_MEDIA_VIDEO
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10-12
                base += Manifest.permission.READ_EXTERNAL_STORAGE
            } else {
                // Android 9 及以下
                base += Manifest.permission.READ_EXTERNAL_STORAGE
                base += Manifest.permission.WRITE_EXTERNAL_STORAGE
            }
            return base.toTypedArray()
        }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // 无论权限是否全部授予，都启动 App
        // 各模块内部会根据权限状态显示引导或降级处理
        val granted = permissions.entries.filter { it.value }.map { it.key }
        val denied = permissions.entries.filter { !it.value }.map { it.key }
        if (denied.isNotEmpty()) {
            android.util.Log.w("MainActivity", "Permissions denied: $denied")
        }
        if (granted.isNotEmpty()) {
            android.util.Log.d("MainActivity", "Permissions granted: $granted")
        }
        launchApp()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION

        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            launchApp()
        } else {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun launchApp() {
        setContent {
            YanbaoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    YanbaoApp()
                }
            }
        }
    }
}
