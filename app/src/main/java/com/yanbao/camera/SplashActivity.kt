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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

/**
 * 启动页 (SplashActivity)
 *
 * 设计规范：
 * - 背景：纯黑 #0A0A0A（曜石黑）
 * - 中心：雁宝 IP 形象（手持相机）
 * - 品牌字：「摄颜 SheYan」白色大字 + 粉色霓虹外发光
 * - 副标题：「AI 相机 · 雁宝记忆」品牌粉
 * - 进度条：胡萝卜橙 #F97316，细线样式，底部
 * - 版权：「© 2026 SheYan」底部居中
 * - 逻辑：执行环境检查（数据库加载、权限检查）
 */
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
        setContent {
            YanbaoSplashScreen(
                onTimeout = { checkAndRequestPermissions() }
            )
        }
    }

    private fun checkAndRequestPermissions() {
        val perms = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_MEDIA_IMAGES
        )
        if (perms.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            navigateToMain()
        } else {
            permissionLauncher.launch(perms)
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 启动页主 Composable
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun YanbaoSplashScreen(onTimeout: () -> Unit = {}) {
    // 进度状态（0f → 1f）
    var progress by remember { mutableStateOf(0f) }

    val animProgress by animateFloatAsState(
        targetValue   = progress,
        animationSpec = tween(180, easing = LinearEasing),
        label         = "splash_progress"
    )

    // 启动序列：约3秒加载完成
    LaunchedEffect(Unit) {
        repeat(100) {
            delay(22L)
            progress = (it + 1) / 100f
        }
        delay(300L)
        onTimeout()
    }

    // ─── 主布局：纯黑背景 ──────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))   // 曜石黑
    ) {
        // 背景散景光点（粉色光晕）
        SplashBokehCanvas()

        // 主内容列
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── 雁宝 IP 形象（手持相机，62% 宽度）───────────────────────
            // 重要：不得改动雁宝IP形象，保持原始62%宽度
            Image(
                painter            = painterResource(id = R.drawable.yanbao_jk_uniform),
                contentDescription = "雁宝 AI 相机",
                modifier           = Modifier
                    .fillMaxWidth(0.62f)
                    .wrapContentHeight()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 品牌字：「摄颜 SheYan」白色大字 + 粉色霓虹外发光 ─────────────────
            // 设计图确认：「摄颜 SheYan」中英文同行
            Text(
                text  = "摄颜 SheYan",
                style = TextStyle(
                    color      = Color.White,
                    fontSize   = 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    shadow     = Shadow(
                        color      = Color(0xCCEC4899),   // 品牌粉霓虹发光
                        offset     = Offset(0f, 0f),
                        blurRadius = 40f
                    )
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // ── 副标题：「AI 相机 · 雁宝记忆」─────────────────────────────────
            Text(
                text       = "AI 相机 · 雁宝记忆",
                fontSize   = 15.sp,
                color      = Color(0xFFEC4899).copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium,
                textAlign  = TextAlign.Center,
                fontFamily = FontFamily.SansSerif
            )
        }

        // ── 底部：胡萝卜橙宽条进度条（带熊掌图标）+ 版权 ──────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CarrotProgressBar(progress = animProgress)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text      = "\u00a9 2026 SheYan",
                fontSize  = 11.sp,
                color     = Color.White.copy(alpha = 0.35f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 胡萝卜橙进度条（设计图规范：细线4dp，胡萝卜橙 #F97316，无熊掌头）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CarrotProgressBar(
    progress:  Float,
    checkStep: String = ""
) {
    val carrotOrange = Color(0xFFF97316)
    val clampedProg  = progress.coerceIn(0f, 1f)

    Column(
        modifier            = Modifier.fillMaxWidth(0.80f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 检查步骤文案
        if (checkStep.isNotEmpty()) {
            Text(
                text      = checkStep,
                fontSize  = 11.sp,
                color     = Color.White.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier  = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
        ) {
            // 轨道背景（深灰，30% 透明）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x4DFFFFFF))
            )

            // 胡萝卜橙填充进度
            if (clampedProg > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProg)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    carrotOrange,
                                    Color(0xFFFF6B00)
                                )
                            )
                        )
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 樱花粒子 + 散景光点 Canvas（设计图风格：粉色樱花飘落 + 星点氛围）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashBokehCanvas() {
    val infiniteTransition = rememberInfiniteTransition(label = "sakura")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue  = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sakura_fall"
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        // 散景光晕
        data class Bokeh(val fx: Float, val fy: Float, val r: Float, val color: Color)
        val bokehList = listOf(
            Bokeh(0.10f, 0.08f, 120f, Color(0x22EC4899)),
            Bokeh(0.88f, 0.06f, 90f,  Color(0x1AEC4899)),
            Bokeh(0.05f, 0.50f, 110f, Color(0x15F97316)),
            Bokeh(0.92f, 0.42f, 80f,  Color(0x1AEC4899)),
            Bokeh(0.18f, 0.82f, 85f,  Color(0x15F97316)),
            Bokeh(0.78f, 0.88f, 95f,  Color(0x1AEC4899)),
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

        // 樱花粒子（固定位置 + 随时间偏移，模拟飘落）
        data class Petal(val fx: Float, val fy: Float, val r: Float, val phase: Float)
        val petals = listOf(
            Petal(0.15f, 0.12f, 6f,  0.0f),
            Petal(0.72f, 0.08f, 4f,  0.2f),
            Petal(0.88f, 0.22f, 5f,  0.4f),
            Petal(0.05f, 0.35f, 4f,  0.6f),
            Petal(0.92f, 0.55f, 6f,  0.1f),
            Petal(0.30f, 0.70f, 5f,  0.3f),
            Petal(0.60f, 0.15f, 4f,  0.7f),
            Petal(0.45f, 0.05f, 3f,  0.5f),
            Petal(0.80f, 0.75f, 5f,  0.8f),
            Petal(0.20f, 0.90f, 4f,  0.9f),
        )
        for (p in petals) {
            val phase = (offsetY + p.phase) % 1f
            val cx = size.width  * (p.fx + phase * 0.05f)
            val cy = size.height * ((p.fy + phase * 0.6f) % 1f)
            drawCircle(
                color  = Color(0xCCF9A8D4),
                radius = p.r,
                center = Offset(cx, cy)
            )
        }
    }
}
