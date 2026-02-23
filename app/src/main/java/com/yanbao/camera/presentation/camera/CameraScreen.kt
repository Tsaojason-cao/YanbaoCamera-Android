package com.yanbao.camera.presentation.camera

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanbao.camera.R
import com.yanbao.camera.core.utils.ImageSaver
import com.yanbao.camera.ui.theme.KUROMI_PINK
import com.yanbao.camera.ui.theme.OBSIDIAN_BLACK
import kotlinx.coroutines.launch

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel(),
    onNavigateToGallery: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val currentMode by viewModel.currentMode.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecordingMemory.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val config = LocalConfiguration.current
    val screenHeight = config.screenHeightDp.dp
    val layer0Height = screenHeight * 0.75f
    val layer1Height = screenHeight * 0.25f

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        // Layer 0: 取景器（真实相机预览 + 拍照）
        Camera2PreviewView(
            onCaptureClick = { viewModel.takePhoto() },
            onPictureTaken = { bitmap: Bitmap ->
                scope.launch {
                    ImageSaver.saveBitmapToGallery(context, bitmap)
                }
            },
            viewModel = viewModel,
            modifier = Modifier
                .fillMaxWidth()
                .height(layer0Height)
        )
        // 取景器上的悬浮元素
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer0Height)
                .padding(16.dp)
        ) {
            // 左上角：模式名 + 录制红点
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currentMode.displayName,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (isRecording) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).background(Color.Red, CircleShape))
                }
            }
            // 右上角：参数气泡（当前显示ISO/快门）
            if (currentMode == CameraMode.PARAM29D || currentMode == CameraMode.NATIVE) {
                val params by viewModel.params29D.collectAsStateWithLifecycle()
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("ISO ${params.iso}", color = KUROMI_PINK, fontSize = 12.sp)
                    Text("1/${params.shutterSpeed}", color = KUROMI_PINK, fontSize = 12.sp)
                    Text("${params.colorTemp}K", color = KUROMI_PINK, fontSize = 12.sp)
                }
            }
        }
        // Layer 1: 底部控制面板
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(layer1Height)
                .align(Alignment.BottomCenter),
            color = OBSIDIAN_BLACK.copy(alpha = 0.85f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // 顶部快捷工具栏
                QuickToolbar(viewModel)
                Spacer(modifier = Modifier.height(8.dp))
                // 中央主控区
                MainControlRow(
                    onGalleryClick = onNavigateToGallery,
                    onShutterClick = {
                        if (currentMode == CameraMode.VIDEO) {
                            if (viewModel.isRecordingState) viewModel.stopVideo()
                            else viewModel.startVideo()
                        } else {
                            viewModel.takePhoto()
                        }
                    },
                    onMemoryClick = { viewModel.toggleMemoryRecording() },
                    isVideoMode = currentMode == CameraMode.VIDEO
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 模式切换栏
                ModeSelectorRow(
                    modes = CameraMode.values().toList(),
                    selectedMode = currentMode,
                    onModeSelected = { viewModel.setMode(it) }
                )
                // 动态参数面板
                when {
                    currentMode.requires29DPanel -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        Param29DPanel(viewModel)
                    }
                    currentMode.requiresMasterWheel -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        MasterWheel(viewModel)
                    }
                    currentMode.requiresNativeControls -> {
                        Spacer(modifier = Modifier.height(8.dp))
                        NativeControls(viewModel)
                    }
                }
            }
        }
    }
}

@Composable
private fun MainControlRow(
    onGalleryClick: () -> Unit,
    onShutterClick: () -> Unit,
    onMemoryClick: () -> Unit,
    isVideoMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // 左侧相册缩略图
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable { onGalleryClick() }
        )
        // 中央快门按钮
        ShutterButton(
            onClick = onShutterClick,
            isVideoMode = isVideoMode,
            modifier = Modifier.size(72.dp)
        )
        // 右侧记忆按钮
        IconButton(onClick = onMemoryClick) {
            Icon(
                painter = painterResource(id = R.drawable.ic_memory),
                contentDescription = "记忆",
                tint = Color.White
            )
        }
    }
}
