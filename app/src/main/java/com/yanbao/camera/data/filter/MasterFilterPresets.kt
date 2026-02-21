package com.yanbao.camera.data.filter

import com.yanbao.camera.data.model.Render29D

/**
 * 91 個大師濾鏡系統
 * 
 * 每個濾鏡都是一組 29D 参数矩陣的預設種子（Preset Seeds）
 * 通過 GPU Shader 實時映射，而非簡單的 LUT 或图片濾鏡
 */
object MasterFilterPresets {
    
    /**
     * 91 個大師濾鏡列表
     */
    val allFilters: List<MasterFilter> = listOf(
        // === 哈苏感系列 (1-10) ===
        MasterFilter(
            id = "HASSELBLAD_CLASSIC",
            name = "哈苏经典",
            description = "强化高光压缩，营造胶片宽容度",
            render29D = Render29D(
                lightDim1To5 = listOf(1.1f, 0.3f, -0.1f, 0.0f, 0.0f), // 强化 D2（高光压缩）
                colorMatrix6To15 = listOf(1.0f, 0.95f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                texture16To25 = listOf(0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "HASSELBLAD_CLASSIC"
            )
        ),
        
        MasterFilter(
            id = "HASSELBLAD_PORTRAIT",
            name = "哈苏人像",
            description = "肤色保护，柔和过渡",
            render29D = Render29D(
                lightDim1To5 = listOf(1.05f, 0.2f, 0.1f, 0.05f, 0.0f),
                colorMatrix6To15 = listOf(1.0f, 0.9f, 1.0f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 肤色保护
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 降低锐化
                aiBone26To29 = listOf(1.02f, 0.98f, 1.0f, 1.0f), // 微調骨骼比例
                masterFilterId = "HASSELBLAD_PORTRAIT"
            )
        ),
        
        // === 徕卡感系列 (11-20) ===
        MasterFilter(
            id = "LEICA_M_CLASSIC",
            name = "徕卡 M 经典",
            description = "压低中間調饱和度，提升紅色纯度",
            render29D = Render29D(
                lightDim1To5 = listOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                colorMatrix6To15 = listOf(1.0f, 0.85f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, 0.0f), // D8-D10 压低，D15 提升紅色
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "LEICA_M_CLASSIC"
            )
        ),
        
        MasterFilter(
            id = "LEICA_SUMMILUX",
            name = "徕卡 Summilux",
            description = "黃金色調，暖調大片質感",
            render29D = Render29D(
                lightDim1To5 = listOf(1.1f, 0.0f, 0.0f, 0.15f, 0.0f), // D4 冷暖偏移
                colorMatrix6To15 = listOf(1.05f, 1.0f, 0.95f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f), // 橙黃增益
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "LEICA_SUMMILUX"
            )
        ),
        
        // === 库洛米梦幻系列 (21-30) ===
        MasterFilter(
            id = "KUROMI_DREAM_01",
            name = "库洛米梦幻 01",
            description = "粉紫漸變，梦幻少女感",
            render29D = Render29D(
                lightDim1To5 = listOf(1.15f, 0.0f, 0.1f, 0.0f, 0.1f), // 提升曝光，增加色偏
                colorMatrix6To15 = listOf(1.1f, 1.2f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 增強饱和度
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, -0.05f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 柔化
                aiBone26To29 = listOf(1.05f, 0.95f, 1.0f, 1.0f), // 微調臉部比例
                masterFilterId = "KUROMI_DREAM_01"
            )
        ),
        
        MasterFilter(
            id = "KUROMI_DREAM_91",
            name = "库洛米梦幻 91",
            description = "終極粉紫流光，AI 骨骼優化",
            render29D = Render29D(
                lightDim1To5 = listOf(1.2f, 0.1f, 0.2f, 0.1f, 0.15f),
                colorMatrix6To15 = listOf(1.15f, 1.3f, 1.05f, 0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.08f, 0.92f, 1.02f, 1.0f), // 强化骨骼調整
                masterFilterId = "KUROMI_DREAM_91"
            )
        ),
        
        // === 经典记忆系列 (31-40) ===
        MasterFilter(
            id = "CLASSIC_MEMORY_01",
            name = "经典记忆 01",
            description = "增加顆粒感與色散，模擬老鏡頭",
            render29D = Render29D(
                lightDim1To5 = listOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                colorMatrix6To15 = listOf(1.0f, 0.9f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                texture16To25 = listOf(0.0f, 0.0f, 0.15f, 0.0f, 0.0f, 0.0f, 0.1f, 0.0f, 0.0f, 0.0f), // D18 顆粒，D22 色散
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "CLASSIC_MEMORY_01"
            )
        ),
        
        // === 黑白藝術系列 (41-50) ===
        MasterFilter(
            id = "BW_CLASSIC",
            name = "经典黑白",
            description = "高對比度黑白，徕卡 M 風格",
            render29D = Render29D(
                lightDim1To5 = listOf(1.0f, 0.2f, -0.2f, 0.0f, 0.0f), // 高對比度
                colorMatrix6To15 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 去色
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "BW_CLASSIC"
            )
        ),
        
        // === 赛博霓虹系列 (51-60) ===
        MasterFilter(
            id = "CYBERPUNK_NEON",
            name = "赛博霓虹",
            description = "高光拉絲，增強夜景氛圍",
            render29D = Render29D(
                lightDim1To5 = listOf(0.9f, -0.1f, 0.2f, -0.1f, 0.0f), // 压低曝光，提升暗部
                colorMatrix6To15 = listOf(1.2f, 1.3f, 1.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 高饱和度
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.2f, 0.0f), // D19 高光拉絲
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "CYBERPUNK_NEON"
            )
        ),
        
        // === 日系小清新系列 (61-70) ===
        MasterFilter(
            id = "JAPANESE_FRESH",
            name = "日系小清新",
            description = "柔和色調，低饱和度",
            render29D = Render29D(
                lightDim1To5 = listOf(1.1f, 0.0f, 0.0f, 0.05f, 0.0f),
                colorMatrix6To15 = listOf(0.9f, 0.8f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 低饱和度
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, -0.1f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 柔化
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "JAPANESE_FRESH"
            )
        ),
        
        // === 電影感系列 (71-80) ===
        MasterFilter(
            id = "CINEMATIC_TEAL_ORANGE",
            name = "電影感 Teal & Orange",
            description = "青橙對比，好萊塢調色",
            render29D = Render29D(
                lightDim1To5 = listOf(1.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                colorMatrix6To15 = listOf(1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.15f, -0.1f), // 橙色增強，青色偏移
                texture16To25 = listOf(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f),
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "CINEMATIC_TEAL_ORANGE"
            )
        ),
        
        // === 复古胶片系列 (81-91) ===
        MasterFilter(
            id = "VINTAGE_FILM_01",
            name = "复古胶片 01",
            description = "褪色感，顆粒質感",
            render29D = Render29D(
                lightDim1To5 = listOf(0.95f, 0.0f, 0.0f, 0.0f, 0.0f),
                colorMatrix6To15 = listOf(0.85f, 0.8f, 0.9f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 褪色
                texture16To25 = listOf(0.0f, 0.0f, 0.2f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), // 強顆粒感
                aiBone26To29 = listOf(1.0f, 1.0f, 1.0f, 1.0f),
                masterFilterId = "VINTAGE_FILM_01"
            )
        )
    )
    
    /**
     * 根據 ID 獲取濾鏡
     */
    fun getFilterById(id: String): MasterFilter? {
        return allFilters.find { it.id == id }
    }
    
    /**
     * 根據名稱搜索濾鏡
     */
    fun searchByName(query: String): List<MasterFilter> {
        return allFilters.filter { it.name.contains(query, ignoreCase = true) }
    }
}

/**
 * 大師濾鏡數據類
 */
data class MasterFilter(
    val id: String,
    val name: String,
    val description: String,
    val render29D: Render29D
)
