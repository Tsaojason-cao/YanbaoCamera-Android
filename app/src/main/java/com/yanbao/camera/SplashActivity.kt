package com.yanbao.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay
import kotlin.math.*

class SplashActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { navigateToMain() }

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
        setContent { YanbaoSplashScreen(onTimeout = { checkAndRequestPermissions() }) }
    }

    private fun checkAndRequestPermissions() {
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        if (perms.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED })
            navigateToMain()
        else
            permissionLauncher.launch(perms)
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ─────────────────────────────────────────────────────────────
// 主 Composable：严格按照指定架构实现
// ─────────────────────────────────────────────────────────────
@Composable
fun YanbaoSplashScreen(onTimeout: () -> Unit = {}) {
    var progress by remember { mutableStateOf(0f) }
    val animProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(200, easing = LinearEasing),
        label = "prog"
    )

    LaunchedEffect(Unit) {
        repeat(100) {
            delay(22L)
            progress = (it + 1) / 100f
        }
        delay(300L)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF3A1478), Color(0xFFF08CB4))
                )
            )
    ) {
        // 1. 背景螺旋线 (Canvas 绘制)
        SplashSwirlCanvas()

        // 2. 散景光点
        SplashBokehCanvas()

        // 主内容列
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 3. 库洛米手持相机立绘（透明背景，60% 宽度）
            Image(
                painter = painterResource(id = R.drawable.kuromi_camera_pro),
                contentDescription = "Kuromi with Camera",
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 4. 品牌字：yanbao AI，ExtraBold，42sp，白色，粉色外发光
            Text(
                text = "yanbao AI",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    shadow = Shadow(
                        color = Color(0x99EC4899),
                        offset = Offset(0f, 0f),
                        blurRadius = 24f
                    )
                )
            )

            Spacer(modifier = Modifier.height(64.dp))

            // 5. 霓虹进度条
            NeonProgressBar(progress = animProgress)
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 螺旋线 Canvas（3 条白色半透明螺旋）
// ─────────────────────────────────────────────────────────────
@Composable
private fun SplashSwirlCanvas() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val cx = size.width / 2f
        val cy = size.height * 0.40f
        for (arm in 0 until 3) {
            val baseAngle = arm * (2.0 * PI / 3.0)
            val pts = (0 until 200).map { ti ->
                val t = ti / 200.0 * 2.5 * PI + baseAngle
                val r = 80.0 + 40.0 * sin(t * 2.0)
                Offset(
                    (cx + r * cos(t) * 1.7f).toFloat(),
                    (cy + r * sin(t) * 0.52f).toFloat()
                )
            }
            for (i in 0 until pts.size - 1) {
                val frac = i / pts.size.toFloat()
                val alpha = (0.50f * (1f - abs(frac - 0.5f) * 2f)).coerceIn(0.05f, 0.55f)
                drawLine(
                    color = Color.White.copy(alpha = alpha),
                    start = pts[i],
                    end = pts[i + 1],
                    strokeWidth = 2.5f
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 散景光点 Canvas（6 个淡紫/淡粉半透明圆点）
// ─────────────────────────────────────────────────────────────
@Composable
private fun SplashBokehCanvas() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        data class Bokeh(val fx: Float, val fy: Float, val r: Float, val color: Color)
        val bokehList = listOf(
            Bokeh(0.08f, 0.10f, 70f,  Color(0x44B090E8)),
            Bokeh(0.85f, 0.07f, 55f,  Color(0x33FFFFFF)),
            Bokeh(0.06f, 0.48f, 80f,  Color(0x33FFFFFF)),
            Bokeh(0.90f, 0.40f, 60f,  Color(0x33FFB0D0)),
            Bokeh(0.20f, 0.80f, 65f,  Color(0x33FFFFFF)),
            Bokeh(0.75f, 0.85f, 75f,  Color(0x33FFB0D0)),
        )
        for (b in bokehList) {
            val cx = size.width * b.fx
            val cy = size.height * b.fy
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(b.color, Color(0x00000000)),
                    center = Offset(cx, cy),
                    radius = b.r
                ),
                radius = b.r,
                center = Offset(cx, cy)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// 霓虹进度条（胶囊形，粉紫渐变，外发光）
// ─────────────────────────────────────────────────────────────
@Composable
fun NeonProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth(0.74f)
            .wrapContentHeight()
    ) {
        // 外发光层（8dp 粉色光晕）
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(50),
                    ambientColor = Color(0xFFEC4899),
                    spotColor = Color(0xFFEC4899)
                )
                .clip(RoundedCornerShape(50))
                .background(Color(0x50EC4899))
        )
        // 轨道背景
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x60321E3C))
        )
        // 填充进度（粉→紫渐变）
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(28.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color(0xFFEC4899), Color(0xFFD946EF))
                    )
                )
        )
        // 顶部高光
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(10.dp)
                .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                .background(Color(0x55FFFFFF))
        )
    }
}
