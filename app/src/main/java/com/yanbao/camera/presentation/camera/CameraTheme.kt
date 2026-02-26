package com.yanbao.camera.presentation.camera
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 摄颜 (SheYan) 相机模块颜色系统
 * 品牌三色：曜石黑 #0A0A0A / 品牌粉 #EC4899 / 胡萝卜橙 #F97316
 * KuromiPink/KuromiPurple 已迁移为雁宝品牌色别名，保留旧名以兼容现有代码
 */
object CameraColors {
    // 雁宝核心品牌三色
    val ObsidianBlack   = Color(0xFF0A0A0A)   // 曜石黑
    val BrandPink       = Color(0xFFEC4899)   // 品牌粉
    val CarrotOrange    = Color(0xFFF97316)   // 胡萝卜橙

    // 历史别名（保留兼容性，值已更新为雁宝品牌色）
    val KuromiPink      = BrandPink           // 迁移为品牌粉 #EC4899
    val KuromiPurple    = Color(0xFFBE185D)   // 迁移为深粉（去除紫色调）

    // 毛玻璃/透明度变体
    val GlassBgDark     = Color(0xCC0A0A0A)   // 深色毛玻璃底（80%黑）
    val PinkGlass       = Color(0x33EC4899)   // 品牌粉毛玻璃（20%粉）
    val OrangeGlass     = Color(0x33F97316)   // 胡萝卜橙毛玻璃（20%橙）

    // 文字/辅助色
    val White           = Color(0xFFFFFFFF)
    val White70         = Color(0xB3FFFFFF)
    val White40         = Color(0x66FFFFFF)
    val SliderTrack     = Color(0xFF1A0A0A)   // 滑块轨道（深黑偏粉）
    val LabelColor      = Color(0xFFFFB3D1)   // 标签文字（浅粉）
    val RedDot          = Color(0xFFFF3C3C)   // 录制红点
    val DarkPanel       = Color(0xE60A0A14)   // 控制面板底色

    // 霓虹光晕色
    val NeonPink        = Color(0x80EC4899)   // 熊掌快门霓虹光晕（50%粉）
    val NeonOrange      = Color(0x80F97316)   // 胡萝卜橙霓虹光晕（50%橙）

    // 深色变体
    val DeepPink        = Color(0xFFBE185D)   // 深粉（按压态/选中态）
    val DeepOrange      = Color(0xFFEA580C)   // 深橙（按压态）
}

object CameraDimens {
    val ViewfinderRatio     = 0.72f           // 72% 取景器（黄金比例）
    val ControlPanelRatio   = 0.28f           // 28% 控制面板
    val PanelCornerRadius   = 24.dp
    val BlurRadius          = 40.dp           // 毛玻璃模糊半径
    val ShutterButtonRadius = 72.dp           // 快门按钮直径
    val ShutterInnerRadius  = 56.dp           // 快门内圈直径
    val NeonGlowRadius      = 80.dp           // 霓虹光晕直径
    val FocalButtonHeight   = 56.dp
    val NeonBorderWidth     = 2.dp
    val CardCornerRadius    = 20.dp
    val ToolbarHeight       = 80.dp
    val TabHeight           = 60.dp
}

object CameraTextSizes {
    val Title       = 28.sp
    val Subtitle    = 22.sp
    val Body        = 18.sp
    val Caption     = 14.sp
    val Small       = 12.sp
    val ButtonText  = 20.sp
    val SliderLabel = 16.sp
    val SliderValue = 16.sp
    val TabLabel    = 14.sp
    val BrandName   = 20.sp
    val FocalLabel  = 16.sp
}
