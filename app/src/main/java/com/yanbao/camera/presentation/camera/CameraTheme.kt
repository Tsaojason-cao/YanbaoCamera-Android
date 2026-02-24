package com.yanbao.camera.presentation.camera

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object CameraColors {
    val ObsidianBlack   = Color(0xFF0A0A0A)
    val KuromiPink      = Color(0xFFEC4899)
    val KuromiPurple    = Color(0xFF9D4EDD)
    val GlassBgDark     = Color(0xCC0A0A0A)   // 深色毛玻璃底（80%黑）
    val White           = Color(0xFFFFFFFF)
    val White70         = Color(0xB3FFFFFF)
    val White40         = Color(0x66FFFFFF)
    val SliderTrack     = Color(0xFF3C2850)
    val LabelColor      = Color(0xFFC8B4DC)
    val RedDot          = Color(0xFFFF3C3C)
    val DarkPanel       = Color(0xE60F0F19)
}

object CameraDimens {
    val ViewfinderRatio = 0.75f
    val ControlPanelRatio = 0.25f
    val PanelCornerRadius = 24.dp
    val BlurRadius = 40.dp
    val ShutterButtonRadius = 72.dp
    val ShutterInnerRadius = 56.dp
    val FocalButtonHeight = 56.dp
    val NeonBorderWidth = 2.dp
    val CardCornerRadius = 20.dp
    val ToolbarHeight = 80.dp
    val TabHeight = 60.dp
}

object CameraTextSizes {
    val Title = 28.sp
    val Subtitle = 22.sp
    val Body = 18.sp
    val Caption = 14.sp
    val Small = 12.sp
    val ButtonText = 20.sp
    val SliderLabel = 16.sp
    val SliderValue = 16.sp
    val TabLabel = 14.sp
    val BrandName = 20.sp
    val FocalLabel = 16.sp
}
