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
import androidx.compose.ui.draw.shadow
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
            // ── 雁宝 IP 形象（手持相机，60% 宽度） ────────────────────────
            Image(
                painter            = painterResource(id = R.drawable.yanbao_jk_uniform),
                contentDescription = "雁宝 AI 相机",
                modifier           = Modifier
                    .fillMaxWidth(0.62f)
                    .wrapContentHeight()
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── 品牌字：「摄颜 SheYan」白色大字 + 粉色霓虹外发光 ───────────
            Text(
                text  = "摄颜 SheYan",
                style = TextStyle(
                    color      = Color.White,
                    fontSize   = 44.sp,
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

        // ── 底部：胡萝卜橙进度条（细线）+ 版权 ──────────────────────────────
        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .height(3.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x33FFFFFF))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animProgress)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFFF97316), Color(0xFFFF8C00))
                            )
                        )
                )
            }
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
// 胡萝卜橙进度条（规范：胡萝卜橙 #F97316，进度头为粉色熊掌印）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CarrotProgressBar(
    progress:  Float,
    checkStep: String = ""
) {
    val carrotOrange = Color(0xFFF97316)
    val brandPink    = Color(0xFFEC4899)
    val clampedProg  = progress.coerceIn(0f, 1f)

    Column(
        modifier            = Modifier.fillMaxWidth(0.74f),
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
                .wrapContentHeight()
        ) {
            // 轨道背景（深灰，40% 透明）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0x66333333))
            )

            // 胡萝卜橙填充进度
            if (clampedProg > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProg)
                        .height(20.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    carrotOrange,
                                    Color(0xFFFF6B00)   // 深橙
                                )
                            )
                        )
                )
                // 顶部高光
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProg)
                        .height(8.dp)
                        .clip(RoundedCornerShape(topStart = 50.dp, topEnd = 50.dp))
                        .background(Color(0x44FFFFFF))
                )
            }

            // 进度头：粉色熊掌印圆形（在进度条末端）
            if (clampedProg > 0.02f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(clampedProg)
                        .height(20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .shadow(6.dp, CircleShape, spotColor = brandPink)
                            .clip(CircleShape)
                            .background(brandPink),
                        contentAlignment = Alignment.Center
                    ) {
                        // 熊掌印图标（白色）
                        androidx.compose.material3.Icon(
                            painter            = painterResource(R.drawable.ic_shutter_paw),
                            contentDescription = null,
                            tint               = Color.White,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 散景光点 Canvas（粉色 + 橙色光晕，曜石黑背景上的氛围感）
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SplashBokehCanvas() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        data class Bokeh(val fx: Float, val fy: Float, val r: Float, val color: Color)

        val bokehList = listOf(
            Bokeh(0.10f, 0.08f, 90f,  Color(0x22EC4899)),   // 品牌粉
            Bokeh(0.88f, 0.06f, 70f,  Color(0x1AEC4899)),
            Bokeh(0.05f, 0.50f, 100f, Color(0x15F97316)),   // 胡萝卜橙
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
    }
}
