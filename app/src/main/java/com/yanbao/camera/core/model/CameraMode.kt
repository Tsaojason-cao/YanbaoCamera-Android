package com.yanbao.camera.core.model

import androidx.compose.ui.graphics.Color

/**
 * 相机模式枚举
 * 
 * 9大模式：
 * 1. 拍照模式 (Photo)
 * 2. 录像模式 (Video)
 * 3. 人像模式 (Portrait)
 * 4. 夜景模式 (Night)
 * 5. 专业模式 (Professional)
 * 6. 全景模式 (Panorama)
 * 7. 延时摄影 (Timelapse)
 * 8. 大师滤镜 (Master Filters)
 * 9. iPhone风格 (iPhone Style)
 */
enum class CameraMode(
    val displayName: String,
    val icon: String,
    val color: Color,
    val description: String
) {
    PHOTO(
        displayName = "拍照",
        icon = "📷",
        color = Color(0xFFEC4899),
        description = "标准拍照模式，支持自动对焦和曝光"
    ),
    
    VIDEO(
        displayName = "录像",
        icon = "🎥",
        color = Color(0xFFEF4444),
        description = "视频录制模式，支持4K/1080p录制"
    ),
    
    PORTRAIT(
        displayName = "人像",
        icon = "🎨",
        color = Color(0xFFF59E0B),
        description = "人像模式，自动虚化背景，突出主体"
    ),
    
    NIGHT(
        displayName = "夜景",
        icon = "🌙",
        color = Color(0xFF8B5CF6),
        description = "夜景模式，多帧合成降噪，提升暗光表现"
    ),
    
    PROFESSIONAL(
        displayName = "专业",
        icon = "🔧",
        color = Color(0xFF10B981),
        description = "专业模式，手动控制ISO、快门、白平衡等参数"
    ),
    
    PANORAMA(
        displayName = "全景",
        icon = "🌄",
        color = Color(0xFF3B82F6),
        description = "全景模式，拍摄超广角全景照片"
    ),
    
    TIMELAPSE(
        displayName = "延时",
        icon = "⏱️",
        color = Color(0xFF06B6D4),
        description = "延时摄影，记录时间流逝的美妙瞬间"
    ),
    
    MASTER_FILTERS(
        displayName = "滤镜",
        icon = "✨",
        color = Color(0xFFEC4899),
        description = "大师滤镜，一键应用专业级调色方案"
    ),
    
    IPHONE_STYLE(
        displayName = "iPhone",
        icon = "📱",
        color = Color(0xFF6366F1),
        description = "iPhone风格，模拟iPhone相机的拍摄效果"
    );
    
    companion object {
        /**
         * 获取所有模式列表
         */
        fun getAllModes(): List<CameraMode> = values().toList()
        
        /**
         * 根据名称获取模式
         */
        fun fromName(name: String): CameraMode? {
            return values().find { it.name == name }
        }
    }
}
