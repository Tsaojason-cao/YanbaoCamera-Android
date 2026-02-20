package com.yanbao.camera.presentation.camera

import android.net.Uri
import android.widget.Toast
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.yanbao.camera.core.util.CameraManager

@Composable
fun CameraScreen(
    viewModel: CameraViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraManager = remember { CameraManager() }
    
    var previewView by remember { mutableStateOf<PreviewView?>(null) }
    var lastSavedUri by remember { mutableStateOf<Uri?>(null) }
    
    LaunchedEffect(previewView) {
        previewView?.let { preview ->
            cameraManager.startCamera(
                context = context,
                lifecycleOwner = lifecycleOwner,
                previewView = preview
            )
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraManager.shutdown()
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).also {
                    previewView = it
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .border(
                            width = 4.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFA78BFA)
                                )
                            ),
                            shape = CircleShape
                        )
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFEC4899),
                                    Color(0xFFF9A8D4)
                                )
                            ),
                            shape = CircleShape
                        )
                        .clickable {
                            cameraManager.takePhoto(
                                context = context,
                                onPhotoSaved = { uri ->
                                    lastSavedUri = uri
                                    Toast
                                        .makeText(
                                            context,
                                            "Photo saved: $uri",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                },
                                onError = { exception ->
                                    Toast
                                        .makeText(
                                            context,
                                            "Error: ${exception.message}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                }
            }
        }
        
        lastSavedUri?.let { uri ->
            Text(
                text = "Last photo: ${uri.lastPathSegment}",
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 48.dp)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .padding(8.dp)
            )
        }
    }
}
