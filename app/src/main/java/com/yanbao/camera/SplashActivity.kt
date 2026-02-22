package com.yanbao.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlin.math.*

/**
 * SplashActivity — 严格对标 01_splash_ui_01.png
 * 背景：紫色星空渐变（深紫→亮紫→粉紫→粉色）
 * 库洛米立绘：58% 宽度居中，白色螺旋光晕
 * 品牌名：Yanbao Camera，粗体，白色，42.sp
 * 进度条：圆角胶囊，粉红渐变 #EC4899→#D946EF
 */
class SplashActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        results.forEach { (perm, granted) ->
            Log.d("SplashActivity", "权限 $perm: $granted")
        }
        navigateToMain()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = AndroidColor.TRANSPARENT
        window.navigationBarColor = AndroidColor.TRANSPARENT
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        setContent {
            SplashScreenUI(onTimeout = { checkAndRequestPermissions() })
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        val allGranted = permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) navigateToMain() else permissionLauncher.launch(permissions)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
fun SplashScreenUI(onTimeout: () -> Unit) {
    var contentVisible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "content_alpha"
    )
    val progressAnim by animateFloatAsState(
        targetValue = if (contentVisible) 0.85f else 0f,
        animationSpec = tween(durationMillis = 1800, easing = LinearEasing),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        contentVisible = true
        delay(2200L)
        onTimeout()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // 紫色星空渐变背景
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.00f to Color(0xFF3A1478),
                        0.30f to Color(0xFF6E2DB4),
                        0.60f to Color(0xFFAA46A0),
                        0.85f to Color(0xFFD264A0),
                        1.00f to Color(0xFFF08CB4)
                    ),
                    startY = 0f, endY = size.height
                )
            )
        }

        // 散景光斑
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bokeh = listOf(
                Triple(0.15f, 0.14f, 55f),
                Triple(0.82f, 0.09f, 40f),
                Triple(0.08f, 0.47f, 65f),
                Triple(0.90f, 0.41f, 45f),
                Triple(0.46f, 0.85f, 70f),
                Triple(0.20f, 0.77f, 35f),
                Triple(0.87f, 0.77f, 50f),
            )
            for ((fx, fy, r) in bokeh) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x55FFFFFF), Color(0x00FFFFFF)),
                        center = Offset(size.width * fx, size.height * fy),
                        radius = r
                    ),
                    radius = r, center = Offset(size.width * fx, size.height * fy)
                )
            }
        }

        // 内容层（淡入）
        Column(
            modifier = Modifier.fillMaxSize().alpha(contentAlpha),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 螺旋光晕 + 库洛米
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // 螺旋光晕线
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    val cy = size.height / 2
                    for (spiralIdx in 0 until 3) {
                        val offsetAngle = spiralIdx * (2 * Math.PI / 3)
                        val pts = (0 until 200).map { ti ->
                            val t = ti / 200.0 * 2 * Math.PI + offsetAngle
                            val r = 80 + 30 * sin(t * 2)
                            Offset((cx + r * cos(t) * 1.8).toFloat(), (cy + r * sin(t) * 0.6).toFloat())
                        }
                        for (i in 0 until pts.size - 1) {
                            val a = (80 * (1 - abs(i / pts.size.toFloat() - 0.5f) * 2)).toInt()
                            drawLine(Color.White.copy(alpha = a / 255f), pts[i], pts[i + 1], 2f)
                        }
                    }
                }

                // 金色星星
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val stars = listOf(
                        Triple(0.22f, 0.24f, 18f), Triple(0.79f, 0.15f, 14f),
                        Triple(0.14f, 0.62f, 16f), Triple(0.87f, 0.57f, 20f),
                        Triple(0.67f, 0.80f, 15f), Triple(0.38f, 0.88f, 12f),
                    )
                    for ((fx, fy, ss) in stars) {
                        val scx = size.width * fx; val scy = size.height * fy
                        val pts = (0 until 10).map { i ->
                            val angle = Math.PI * i / 5 - Math.PI / 2
                            val r = if (i % 2 == 0) ss else ss * 0.4f
                            Offset((scx + r * cos(angle)).toFloat(), (scy + r * sin(angle)).toFloat())
                        }
                        drawPath(Path().apply {
                            moveTo(pts[0].x, pts[0].y)
                            pts.drop(1).forEach { lineTo(it.x, it.y) }
                            close()
                        }, Color(0xFFFFD232))
                    }
                }

                // 库洛米立绘（58% 宽度）
                BoxWithConstraints(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    androidx.compose.foundation.Image(
                        painter = painterResource(id = R.drawable.ic_kuromi_standard),
                        contentDescription = "Kuromi",
                        modifier = Modifier.fillMaxWidth(0.58f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 品牌名：Yanbao Camera，粗体，白色，42.sp
            Text(
                text = "Yanbao Camera",
                fontFamily = FontFamily.SansSerif,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 圆角胶囊进度条（粉红渐变）
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.72f)
                    .height(22.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(50)).background(Color(0x80321E3C)))
                Box(
                    modifier = Modifier.fillMaxHeight().fillMaxWidth(progressAnim)
                        .clip(RoundedCornerShape(50))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFFD946EF))))
                )
                Box(
                    modifier = Modifier.fillMaxWidth(progressAnim).fillMaxHeight(0.45f)
                        .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                        .background(Color(0x33FFFFFF))
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
