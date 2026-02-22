package com.yanbao.camera.presentation.camera

import android.opengl.GLSurfaceView
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.yanbao.camera.core.render.Camera2GLRenderer
import com.yanbao.camera.core.utils.ImageSaver
import kotlinx.coroutines.launch
import android.widget.Toast
import android.util.Log

/**
 * 专业相机界面
 * 
 * 布局：
 * - 70% 预览区（全屏 Camera2 + OpenGL 渲染）
 * - 30% 操作区（毛玻璃效果，包含专业参数控制）
 * 
 * 工业级特性：
 * - 使用 AndroidView 封装 GLSurfaceView
 * - Camera2 预览帧通过 SurfaceTexture 传递给 OpenGL
 * - 专业参数（ISO/曝光/白平衡）实时控制硬件和渲染器
 * - RGB 曲线实时更新 LUT 纹理
 */
@Composable
fun ProCameraScreen(
    viewModel: ProCameraViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // 专业模式参数
    val iso by viewModel.iso.collectAsState()
    val exposureTime by viewModel.exposureTime.collectAsState()
    val whiteBalance by viewModel.whiteBalance.collectAsState()
    
    // RGB 曲线面板显示状态
    var showRGBCurvePanel by remember { mutableStateOf(false) }
    
    // GLSurfaceView 和 Renderer
    var glSurfaceView by remember { mutableStateOf<GLSurfaceView?>(null) }
    var glRenderer by remember { mutableStateOf<Camera2GLRenderer?>(null) }
    
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // === 70% 预览区 ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f)
                    .background(Color.Black)
            ) {
                // OpenGL 预览
                AndroidView(
                    factory = { ctx ->
                        GLSurfaceView(ctx).apply {
                            setEGLContextClientVersion(3)
                            
                            val renderer = Camera2GLRenderer(ctx) { surfaceTexture ->
                                // SurfaceTexture 准备好后，通知 ViewModel
                                viewModel.setSurfaceTexture(surfaceTexture)
                            }
                            
                            setRenderer(renderer)
                            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                            
                            glSurfaceView = this
                            glRenderer = renderer
                            viewModel.initGLRenderer(renderer)
                            
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // 顶部品牌标识
                Text(
                    text = "yanbao AI",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp)
                )
            }
            
            // === 30% 操作区 ===
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .background(Color.Black.copy(alpha = 0.15f))
                    .blur(25.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 专业参数控制
                    ProModeControls(
                        iso = iso,
                        exposureTime = exposureTime,
                        whiteBalance = whiteBalance,
                        onISOChange = { viewModel.updateISO(it) },
                        onExposureTimeChange = { viewModel.updateExposureTime(it) },
                        onWhiteBalanceChange = { viewModel.updateWhiteBalance(it) }
                    )
                    
                    // 底部工具栏
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // RGB 曲线按钮
                        IconButton(
                            onClick = { showRGBCurvePanel = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "RGB 曲线",
                                tint = Color(0xFFFFB6C1),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        
                        // 拍照按钮
                        Button(
                            onClick = {
                                scope.launch {
                                    try {
                                        // 使用当前专业参数拍照
                                        Log.d("ProCameraScreen", "Capture with ISO=$iso, Exposure=$exposureTime, WB=$whiteBalance")
                                        Toast.makeText(context, "拍照中...", Toast.LENGTH_SHORT).show()
                                        // TODO: 实际拍照需要从 Camera2GLRenderer 中获取当前帧
                                        // 这里是占位实现
                                    } catch (e: Exception) {
                                        Log.e("ProCameraScreen", "Capture failed", e)
                                        Toast.makeText(context, "拍照失败", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFB6C1)
                            ),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Text(text = "📸", fontSize = 32.sp)
                        }
                        
                        // 占位符（保持对称）
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }
            }
        }
        
        // RGB 曲线面板（全屏覆盖）
        if (showRGBCurvePanel) {
            RGBCurvePanel(
                onLUTUpdate = { lutData ->
                    viewModel.updateLUT(lutData)
                },
                onDismiss = { showRGBCurvePanel = false }
            )
        }
    }
    
    // 清理资源
    DisposableEffect(Unit) {
        onDispose {
            glSurfaceView?.onPause()
            glRenderer?.release()
        }
    }
}
