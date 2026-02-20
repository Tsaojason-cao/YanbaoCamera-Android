package com.yanbao.camera

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            SplashScreen {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}

@Composable
fun SplashScreen(onFinish: () -> Unit) {
    val scope = rememberCoroutineScope()
    var progress by remember { mutableStateOf(0f) }
    
    LaunchedEffect(Unit) {
        scope.launch {
            for (i in 0..100) {
                progress = i / 100f
                delay(30)
            }
            delay(500)
            onFinish()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFA78BFA),
                        Color(0xFFEC4899),
                        Color(0xFFF9A8D4)
                    )
                )
            )
    ) {
        FloatingOrbs()
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.weight(0.3f))
            
            KuromiCharacter()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Yanbao Camera",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(8.dp),
                color = Color(0xFFEC4899),
                trackColor = Color.White.copy(alpha = 0.3f)
            )
            
            Spacer(modifier = Modifier.weight(0.3f))
        }
    }
}

@Composable
fun FloatingOrbs() {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    
    val orbs = remember {
        listOf(
            Triple(0.1f, 0.2f, 80.dp),
            Triple(0.8f, 0.15f, 120.dp),
            Triple(0.2f, 0.6f, 100.dp),
            Triple(0.7f, 0.7f, 90.dp),
            Triple(0.5f, 0.3f, 60.dp),
            Triple(0.3f, 0.8f, 70.dp)
        )
    }
    
    orbs.forEachIndexed { index, (x, y, size) ->
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000 + index * 200, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "orb_scale_$index"
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(align = Alignment.TopStart)
                .offset(x = (x * 400).dp, y = (y * 800).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size)
                    .scale(scale)
                    .background(
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .blur(20.dp)
            )
        }
    }
}

@Composable
fun KuromiCharacter() {
    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .background(
                    color = Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
                .blur(30.dp)
        )
        
        Text(
            text = "🐰",
            fontSize = 120.sp
        )
    }
}
