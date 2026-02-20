package com.yanbao.camera.presentation.camera

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.yanbao.camera.core.util.CameraManager

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager() }
    
    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    LaunchedEffect(Unit) {
        if (!permissionsState.allPermissionsGranted) {
            permissionsState.launchMultiplePermissionRequest()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (permissionsState.allPermissionsGranted) {
            AndroidView(
                factory = { ctx ->
                    PreviewView(ctx).apply {
                        cameraManager.startCamera(ctx, lifecycleOwner, this)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            KuromiDecorations()
            
            TopControlBar(onNavigateBack = onNavigateBack)
            
            BottomControlBar(
                onTakePhoto = {
                    cameraManager.takePhoto(context) { success, message ->
                    }
                }
            )
        } else {
            PermissionDeniedScreen()
        }
    }
}

@Composable
fun BoxScope.KuromiDecorations() {
    val kuromiEmoji = "🐰"
    val heartEmoji = "💗"
    val starEmoji = "⭐"
    
    Text(
        text = "$kuromiEmoji$heartEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(16.dp)
    )
    
    Text(
        text = "$heartEmoji$kuromiEmoji",
        fontSize = 32.sp,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(16.dp)
    )
    
    Text(
        text = "$starEmoji$kuromiEmoji$heartEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(start = 16.dp, bottom = 120.dp)
    )
    
    Text(
        text = "$heartEmoji$kuromiEmoji$starEmoji",
        fontSize = 28.sp,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 120.dp)
    )
}

@Composable
fun BoxScope.TopControlBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .fillMaxWidth()
            .height(100.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x80F9A8D4),
                        Color(0x80A78BFA)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "返回",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "首页",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.FlashAuto,
                    contentDescription = "闪光灯",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "自动", color = Color.White, fontSize = 10.sp)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Cameraswitch,
                    contentDescription = "翻转",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "翻转", color = Color.White, fontSize = 10.sp)
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = "录像",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(text = "录像", color = Color.White, fontSize = 10.sp)
        }
        
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = Color(0xFFEC4899),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "🐰", fontSize = 24.sp)
        }
        
        IconButton(onClick = {}) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "更多",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun BoxScope.BottomControlBar(onTakePhoto: () -> Unit) {
    Row(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .height(140.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF9A8D4),
                        Color(0xFFA78BFA)
                    )
                )
            )
            .padding(horizontal = 32.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "相册", color = Color.White, fontSize = 14.sp)
        }
        
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .background(
                        color = Color(0x40EC4899),
                        shape = CircleShape
                    )
                    .blur(20.dp)
            )
            
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .border(
                        width = 4.dp,
                        color = Color(0xFFEC4899),
                        shape = CircleShape
                    )
            )
            
            IconButton(
                onClick = onTakePhoto,
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        color = Color(0xFFEC4899),
                        shape = CircleShape
                    )
            ) {
                Text(text = "🐰", fontSize = 40.sp)
            }
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = {}) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "设置",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(text = "设置", color = Color.White, fontSize = 14.sp)
        }
    }
}

@Composable
fun PermissionDeniedScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "请授予相机权限",
            color = Color.White,
            fontSize = 20.sp
        )
    }
}
