package com.yanbao.camera.core.model

/**
 * 29D 参数状态
 * 
 * 包含雁宝 AI 相机的 29 个专业参数
 */
data class Camera29DState(
    val params: List<Float> = List(29) { 0.5f } // 默认值为 0.5（中间值）
) {
    companion object {
        /**
         * 29D 参数标签
         */
        val PARAM_LABELS = listOf(
            "ISO",              // 0
            "快门速度",          // 1
            "白平衡",            // 2
            "对焦距离",          // 3
            "曝光补偿",          // 4
            "饱和度",            // 5
            "对比度",            // 6
            "锐度",              // 7
            "亮度",              // 8
            "高光",              // 9
            "阴影",              // 10
            "色温",              // 11
            "色调",              // 12
            "清晰度",            // 13
            "自然饱和度",        // 14
            "暗角",              // 15
            "颗粒",              // 16
            "褪色",              // 17
            "分离色调-高光",     // 18
            "分离色调-阴影",     // 19
            "红色主色调",        // 20
            "橙色主色调",        // 21
            "黄色主色调",        // 22
            "绿色主色调",        // 23
            "青色主色调",        // 24
            "蓝色主色调",        // 25
            "紫色主色调",        // 26
            "洋红主色调",        // 27
            "噪点抑制"           // 28
        )
        
        /**
         * 获取参数标签
         */
        fun getParamLabel(index: Int): String {
            return if (index in PARAM_LABELS.indices) {
                PARAM_LABELS[index]
            } else {
                "参数 ${index + 1}"
            }
        }
        
        /**
         * 将参数值映射到实际硬件值
         * 
         * @param index 参数索引
         * @param normalizedValue 归一化值（0.0 - 1.0）
         * @return 实际硬件值
         */
        fun mapToHardwareValue(index: Int, normalizedValue: Float): Any {
            return when (index) {
                0 -> (100 + normalizedValue * 3100).toInt() // ISO: 100-3200
                1 -> (1000000 + normalizedValue * 99000000).toLong() // 快门速度: 1ms-100ms (纳秒)
                2 -> (2000 + normalizedValue * 8000).toInt() // 白平衡: 2000K-10000K
                3 -> normalizedValue * 10f // 对焦距离: 0-10
                4 -> (normalizedValue - 0.5f) * 4f // 曝光补偿: -2 to +2
                5 -> normalizedValue * 2f // 饱和度: 0-2
                6 -> normalizedValue * 2f // 对比度: 0-2
                7 -> normalizedValue * 2f // 锐度: 0-2
                8 -> (normalizedValue - 0.5f) * 2f // 亮度: -1 to +1
                9 -> normalizedValue * 2f // 高光: 0-2
                10 -> normalizedValue * 2f // 阴影: 0-2
                11 -> (2000 + normalizedValue * 8000).toInt() // 色温: 2000K-10000K
                12 -> (normalizedValue - 0.5f) * 200f // 色调: -100 to +100
                13 -> normalizedValue * 100f // 清晰度: 0-100
                14 -> normalizedValue * 100f // 自然饱和度: 0-100
                15 -> normalizedValue * 100f // 暗角: 0-100
                16 -> normalizedValue * 100f // 颗粒: 0-100
                17 -> normalizedValue * 100f // 褪色: 0-100
                18 -> normalizedValue * 360f // 分离色调-高光: 0-360°
                19 -> normalizedValue * 360f // 分离色调-阴影: 0-360°
                20 -> (normalizedValue - 0.5f) * 200f // 红色主色调: -100 to +100
                21 -> (normalizedValue - 0.5f) * 200f // 橙色主色调: -100 to +100
                22 -> (normalizedValue - 0.5f) * 200f // 黄色主色调: -100 to +100
                23 -> (normalizedValue - 0.5f) * 200f // 绿色主色调: -100 to +100
                24 -> (normalizedValue - 0.5f) * 200f // 青色主色调: -100 to +100
                25 -> (normalizedValue - 0.5f) * 200f // 蓝色主色调: -100 to +100
                26 -> (normalizedValue - 0.5f) * 200f // 紫色主色调: -100 to +100
                27 -> (normalizedValue - 0.5f) * 200f // 洋红主色调: -100 to +100
                28 -> normalizedValue * 100f // 噪点抑制: 0-100
                else -> normalizedValue
            }
        }
        
        /**
         * 将参数值格式化为显示文本
         */
        fun formatValue(index: Int, normalizedValue: Float): String {
            val hardwareValue = mapToHardwareValue(index, normalizedValue)
            return when (index) {
                0 -> "ISO ${hardwareValue}"
                1 -> "1/${(1000000000L / (hardwareValue as Long))}s"
                2, 11 -> "${hardwareValue}K"
                3 -> String.format("%.1f", hardwareValue)
                4, 8 -> String.format("%+.1f", hardwareValue)
                5, 6, 7, 9, 10 -> String.format("%.2f", hardwareValue)
                12, 20, 21, 22, 23, 24, 25, 26, 27 -> String.format("%+.0f", hardwareValue)
                13, 14, 15, 16, 17, 28 -> String.format("%.0f", hardwareValue)
                18, 19 -> String.format("%.0f°", hardwareValue)
                else -> String.format("%.2f", normalizedValue)
            }
        }
    }
    
    /**
     * 转换为 JSON 字符串（用于存储到数据库）
     */
    fun toJson(): String {
        val paramsMap = params.mapIndexed { index, value ->
            PARAM_LABELS[index] to mapToHardwareValue(index, value)
        }.toMap()
        return paramsMap.toString() // 简化版，实际应使用 Gson 或 kotlinx.serialization
    }
}
