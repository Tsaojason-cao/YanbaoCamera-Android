// app/src/main/java/com/yanbao/camera/presentation/camera/YanbaoCameraScreen.kt
package com.yanbao.camera.presentation.camera

import android.Manifest
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import kotlin.math.roundToInt

/**
 * Yanbao AI Camera Screen - Industrial Design Implementation
 * 
 * Features:
 * - 100% fullscreen camera preview (Layer 0)
 * - 28% bottom control panel with blur effect (Layer 1)
 * - 9-mode dial with horizontal scroll
 * - 29D value bubble (Layer 2)
 * - Pink highlight for selected mode (#EC4899)
 */

private val PinkHighlight = Color(0xFFEC4899)
private val ObsidianBlack = Color(0xFF0F0F0F)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun YanbaoCameraScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {}
) {
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    
    LaunchedEffect(Unit) {
        if (cameraPermissionState.status !is PermissionStatus.Granted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
    
    if (cameraPermissionState.status is PermissionStatus.Granted) {
        YanbaoCameraContent(modifier = modifier)
    } else {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "需要相机权限才能使用此功能",
                color = Color.White,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun YanbaoCameraContent(modifier: Modifier = Modifier) {
    var selectedModeIndex by remember { mutableStateOf(0) }
    var isFlashOn by remember { mutableStateOf(false) }
    var value29D by remember { mutableStateOf(0.5f) }
    
    val bottomPanelHeightFraction = 0.28f
    
    Box(modifier = modifier.fillMaxSize()) {
        // Layer 0: Fullscreen camera preview
        CameraPreviewLayer(
            modifier = Modifier.fillMaxSize()
        )
        
        // Layer 1: Bottom 28% control panel with blur
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(bottomPanelHeightFraction)
                .align(Alignment.BottomCenter)
                .background(ObsidianBlack.copy(alpha = 0.85f))
                .blur(40.dp)
        ) {
            ControlPanel(
                modifier = Modifier.fillMaxSize(),
                selectedModeIndex = selectedModeIndex,
                onModeSelected = { selectedModeIndex = it },
                isFlashOn = isFlashOn,
                onFlashToggle = { isFlashOn = !isFlashOn },
                onSettingsClick = {
                    Log.d("YanbaoCamera", "Settings clicked")
                },
                onCaptureClick = {
                    Log.d("YanbaoCamera", "Capture clicked in mode: $selectedModeIndex")
                    // TODO: Implement capture logic based on selectedModeIndex
                }
            )
        }
        
        // Layer 2: 29D value bubble (above bottom panel)
        // Position it 60dp above the bottom panel
        val bubbleOffsetY = with(LocalDensity.current) { 
            (bottomPanelHeightFraction * LocalConfiguration.current.screenHeightDp).dp.toPx() 
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(Alignment.BottomCenter)
                .offset(y = (-60).dp)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            ValueBubble(value = value29D)
        }
    }
}

@Composable
private fun CameraPreviewLayer(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    
    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview
                )
            } catch (e: Exception) {
                Log.e("YanbaoCamera", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    AndroidView(
        factory = { previewView },
        modifier = modifier
    )
}

@Composable
private fun ControlPanel(
    modifier: Modifier = Modifier,
    selectedModeIndex: Int,
    onModeSelected: (Int) -> Unit,
    isFlashOn: Boolean,
    onFlashToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onCaptureClick: () -> Unit,
) {
    val modes = listOf(
        "基本相机",
        "原相机",
        "雁宝记忆",
        "29D",
        "2.9D",
        "大师滤镜",
        "一键美颜",
        "录像",
        "AR空间"
    )
    
    Column(
        modifier = modifier.padding(vertical = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mode dial - horizontal scrollable row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(modes) { index, mode ->
                val isSelected = index == selectedModeIndex
                val textColor = if (isSelected) PinkHighlight else Color.White
                val borderColor = if (isSelected) PinkHighlight else Color.Transparent
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .clip(CircleShape)
                        .border(width = 2.dp, color = borderColor, shape = CircleShape)
                        .background(Color.Transparent)
                        .clickable { onModeSelected(index) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode,
                        color = textColor,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Bottom controls row: Flash, Capture, Settings
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onFlashToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FlashOn,
                    contentDescription = if (isFlashOn) "关闭闪光灯" else "开启闪光灯",
                    tint = if (isFlashOn) PinkHighlight else Color.White
                )
            }
            
            // Capture button - big pink circle
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(PinkHighlight)
                    .clickable(onClick = onCaptureClick),
                contentAlignment = Alignment.Center
            ) {
                // Inner smaller white circle for style
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )
            }
            
            IconButton(
                onClick = onSettingsClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun ValueBubble(value: Float) {
    // Bubble style: pink border, black semi-transparent background, white text
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.7f))
            .border(width = 2.dp, color = PinkHighlight, shape = CircleShape)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Text(
            text = String.format("29D: %.2f", value),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
