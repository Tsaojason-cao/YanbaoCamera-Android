package com.yanbao.camera.presentation.camera

import android.graphics.Bitmap
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.core.camera.Camera2PreviewManager
import kotlinx.coroutines.launch

/**
 * Camera2 预览视图 - Compose封装
 *
 * Phase 1 真实功能：
 * - 使用AndroidView封装SurfaceView
 * - 实际绑定到Camera2的CaptureRequest
 * - 监听 ViewModel.captureRequested 触发真实拍照
 * - 拍照结果通过 onPictureTaken 回调返回 Bitmap
 * - 严禁使用静态占位图片或Image组件模拟预览
 */
@Composable
fun Camera2PreviewView(
    onCaptureClick: () -> Unit,
    onPictureTaken: (Bitmap) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // 监听 ViewModel 的拍照触发器
    val captureRequested by viewModel.captureRequested.collectAsState()

    // 创建Camera2PreviewManager（单例，生命周期绑定到Composable）
    val previewManager = remember {
        Camera2PreviewManager(context)
    }

    // 生命周期管理
    DisposableEffect(Unit) {
        onDispose {
            Log.d("Camera2PreviewView", "Disposing camera resources")
            previewManager.release()
        }
    }

    // 使用AndroidView封装SurfaceView
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        Log.d("Camera2PreviewView", "Surface created")
                        scope.launch {
                            try {
                                val success = previewManager.openCamera(holder.surface)
                                if (success) {
                                    Log.d("Camera2PreviewView", "Camera opened and preview started")
                                } else {
                                    Log.e("Camera2PreviewView", "Failed to open camera")
                                }
                            } catch (e: Exception) {
                                Log.e("Camera2PreviewView", "Error opening camera", e)
                            }
                        }
                    }

                    override fun surfaceChanged(
                        holder: SurfaceHolder,
                        format: Int,
                        width: Int,
                        height: Int
                    ) {
                        Log.d("Camera2PreviewView", "Surface changed: ${width}x${height}")
                    }

                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        Log.d("Camera2PreviewView", "Surface destroyed")
                        previewManager.closeCamera()
                    }
                })
            }
        },
        modifier = modifier.fillMaxSize()
    )

    // 监听 ViewModel 的拍照请求：captureRequested = true 时执行真实拍照
    LaunchedEffect(captureRequested) {
        if (captureRequested) {
            // 立即重置，避免重复触发
            viewModel.resetCaptureRequest()
            try {
                Log.d("Camera2PreviewView", "Taking picture via Camera2PreviewManager")
                val bitmap = previewManager.takePicture()
                if (bitmap != null) {
                    Log.d("Camera2PreviewView", "Picture taken: ${bitmap.width}x${bitmap.height}")
                    onPictureTaken(bitmap)
                } else {
                    Log.e("Camera2PreviewView", "takePicture returned null bitmap")
                }
            } catch (e: Exception) {
                Log.e("Camera2PreviewView", "Error taking picture", e)
            }
        }
    }
}

/**
 * 拍照功能扩展
 */
@Composable
fun rememberCamera2PreviewManager(): Camera2PreviewManager {
    val context = LocalContext.current
    return remember {
        Camera2PreviewManager(context)
    }
}
