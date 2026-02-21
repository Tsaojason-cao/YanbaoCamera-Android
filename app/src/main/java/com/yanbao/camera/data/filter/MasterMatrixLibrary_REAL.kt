package com.yanbao.camera.data.filter

/**
 * yanbao AI - 大师·典藏系列滤镜库
 * 
 * 71 套专业滤镜，涵盖 8 大风格系列：
 * - 写意大师 (10 套)
 * - 工笔大师 (10 套)
 * - 极简大师 (10 套)
 * - 古典大师 (8 套)
 * - 现代大师 (8 套)
 * - 电影大师 (8 套)
 * - 黑白大师 (7 套)
 * - 胶片大师 (10 套)
 * 
 * 每套预设包含 33 维参数：
 * - 影调 (Tone) 12D: [0-11]
 * - 质感 (Texture) 12D: [12-23]
 * - 骨相 (Bone) 8D: [24-31]
 * - 社交 (Trend) 1D: [32]
 * 
 * Developed by Jason Cao for yanbao AI
 * Copyright © 2026 Jason Cao. All rights reserved.
 */
object MasterMatrixLibrary_REAL {

    /**
     * 滤镜系列枚举
     */
    enum class FilterSeries(val displayName: String, val icon: String) {
        XIEYI("写意大师", "XY"),
        GONGBI("工笔大师", "GB"),
        JIJIAN("极简大师", "JJ"),
        GUDIAN("古典大师", "GD"),
        XIANDAI("现代大师", "XD"),
        DIANYING("电影大师", "DY"),
        HEIBAI("黑白大师", "HB"),
        JIAOPIAN("胶片大师", "JP")
    }

    /**
     * 大师滤镜数据类
     */
    data class MasterFilter(
        val id: String,
        val series: FilterSeries,
        val name: String,
        val description: String,
        val params: FloatArray,
        val characteristics: String
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as MasterFilter

            if (id != other.id) return false

            return true
        }

        override fun hashCode(): Int {
            return id.hashCode()
        }
    }

    // ==================== 1. 写意大师系列 (10套) ====================

    private val XIEYI_PRESETS = listOf(
        MasterFilter(
            id = "master_xieyi_01",
            series = FilterSeries.XIEYI,
            name = "写意大师 01",
            description = "水墨写意，空灵淡雅",
            params = floatArrayOf(
                0.1f, 1.2f, 0.3f, -0.2f, 0.15f, -0.15f, 0.05f, 0.8f, 0.1f, 0.05f, -0.1f, 0.2f,  // Tone [0-11]
                0.4f, 0.2f, 0.15f, 0.6f, 0.25f, 0.1f, 0.05f, 0.15f, 0.2f, 0.1f, 0.05f, 0.1f,  // Texture [12-23]
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,  // Bone [24-31]
                0.3f  // Trend [32]
            ),
            characteristics = "高光柔和，阴影通透，色彩淡雅"
        ),
        MasterFilter(
            id = "master_xieyi_02",
            series = FilterSeries.XIEYI,
            name = "写意大师 02",
            description = "泼墨山水，气韵生动",
            params = floatArrayOf(
                0.15f, 1.3f, 0.35f, -0.25f, 0.2f, -0.2f, 0.1f, 0.85f, 0.15f, 0.1f, -0.15f, 0.25f,
                0.45f, 0.25f, 0.2f, 0.65f, 0.3f, 0.15f, 0.1f, 0.2f, 0.25f, 0.15f, 0.1f, 0.15f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.35f
            ),
            characteristics = "对比适中，层次丰富，意境深远"
        ),
        MasterFilter(
            id = "master_xieyi_03",
            series = FilterSeries.XIEYI,
            name = "写意大师 03",
            description = "烟雨朦胧，诗意盎然",
            params = floatArrayOf(
                0.05f, 1.15f, 0.25f, -0.15f, 0.1f, -0.1f, 0.0f, 0.75f, 0.05f, 0.0f, -0.05f, 0.15f,
                0.35f, 0.15f, 0.1f, 0.55f, 0.2f, 0.05f, 0.0f, 0.1f, 0.15f, 0.05f, 0.0f, 0.05f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.25f
            ),
            characteristics = "低对比度，柔和朦胧，如梦如幻"
        ),
        MasterFilter(
            id = "master_xieyi_04",
            series = FilterSeries.XIEYI,
            name = "写意大师 04",
            description = "留白艺术，简约至上",
            params = floatArrayOf(
                0.2f, 1.1f, 0.4f, -0.3f, 0.25f, -0.25f, 0.15f, 0.7f, 0.2f, 0.15f, -0.2f, 0.3f,
                0.3f, 0.1f, 0.05f, 0.5f, 0.15f, 0.0f, -0.05f, 0.05f, 0.1f, 0.0f, -0.05f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.2f
            ),
            characteristics = "大量留白，简洁明快，意境悠远"
        ),
        MasterFilter(
            id = "master_xieyi_05",
            series = FilterSeries.XIEYI,
            name = "写意大师 05",
            description = "淡彩渲染，清新雅致",
            params = floatArrayOf(
                0.12f, 1.25f, 0.32f, -0.22f, 0.18f, -0.18f, 0.08f, 0.82f, 0.12f, 0.08f, -0.12f, 0.22f,
                0.42f, 0.22f, 0.18f, 0.62f, 0.28f, 0.12f, 0.08f, 0.18f, 0.22f, 0.12f, 0.08f, 0.12f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.32f
            ),
            characteristics = "色彩淡雅，渲染自然，清新脱俗"
        ),
        MasterFilter(
            id = "master_xieyi_06",
            series = FilterSeries.XIEYI,
            name = "写意大师 06",
            description = "墨韵流转，笔意纵横",
            params = floatArrayOf(
                0.18f, 1.35f, 0.38f, -0.28f, 0.22f, -0.22f, 0.12f, 0.88f, 0.18f, 0.12f, -0.18f, 0.28f,
                0.48f, 0.28f, 0.22f, 0.68f, 0.32f, 0.18f, 0.12f, 0.22f, 0.28f, 0.18f, 0.12f, 0.18f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.38f
            ),
            characteristics = "笔触流畅，墨韵生动，气势磅礴"
        ),
        MasterFilter(
            id = "master_xieyi_07",
            series = FilterSeries.XIEYI,
            name = "写意大师 07",
            description = "虚实相生，意境悠远",
            params = floatArrayOf(
                0.08f, 1.18f, 0.28f, -0.18f, 0.12f, -0.12f, 0.02f, 0.78f, 0.08f, 0.02f, -0.08f, 0.18f,
                0.38f, 0.18f, 0.12f, 0.58f, 0.22f, 0.08f, 0.02f, 0.12f, 0.18f, 0.08f, 0.02f, 0.08f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.28f
            ),
            characteristics = "虚实结合，层次分明，意境深远"
        ),
        MasterFilter(
            id = "master_xieyi_08",
            series = FilterSeries.XIEYI,
            name = "写意大师 08",
            description = "浓墨重彩，气韵磅礴",
            params = floatArrayOf(
                0.22f, 1.4f, 0.42f, -0.32f, 0.28f, -0.28f, 0.18f, 0.92f, 0.22f, 0.18f, -0.22f, 0.32f,
                0.52f, 0.32f, 0.28f, 0.72f, 0.38f, 0.22f, 0.18f, 0.28f, 0.32f, 0.22f, 0.18f, 0.22f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.42f
            ),
            characteristics = "浓墨重彩，对比强烈，气势恢宏"
        ),
        MasterFilter(
            id = "master_xieyi_09",
            series = FilterSeries.XIEYI,
            name = "写意大师 09",
            description = "淡墨轻描，清雅脱俗",
            params = floatArrayOf(
                0.06f, 1.12f, 0.22f, -0.12f, 0.08f, -0.08f, -0.02f, 0.72f, 0.06f, -0.02f, -0.06f, 0.12f,
                0.32f, 0.12f, 0.08f, 0.52f, 0.18f, 0.02f, -0.02f, 0.08f, 0.12f, 0.02f, -0.02f, 0.02f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.22f
            ),
            characteristics = "淡墨轻描，清雅脱俗，意境悠远"
        ),
        MasterFilter(
            id = "master_xieyi_10",
            series = FilterSeries.XIEYI,
            name = "写意大师 10",
            description = "禅意空灵，返璞归真",
            params = floatArrayOf(
                0.0f, 1.0f, 0.2f, -0.1f, 0.05f, -0.05f, -0.05f, 0.7f, 0.0f, -0.05f, 0.0f, 0.1f,
                0.3f, 0.1f, 0.05f, 0.5f, 0.15f, 0.0f, -0.05f, 0.05f, 0.1f, 0.0f, -0.05f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.2f
            ),
            characteristics = "禅意空灵，返璞归真，大道至简"
        )
    )

    // ==================== 2. 工笔大师系列 (10套) ====================

    private val GONGBI_PRESETS = listOf(
        MasterFilter(
            id = "master_gongbi_01",
            series = FilterSeries.GONGBI,
            name = "工笔大师 01",
            description = "细腻精致，层次分明",
            params = floatArrayOf(
                0.15f, 1.35f, 0.45f, -0.15f, 0.3f, -0.2f, 0.2f, 0.9f, 0.25f, 0.2f, -0.1f, 0.3f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.45f, 0.3f, 0.25f, 0.35f, 0.4f, 0.3f, 0.25f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.45f
            ),
            characteristics = "细节丰富，色彩饱满，层次清晰"
        ),
        MasterFilter(
            id = "master_gongbi_02",
            series = FilterSeries.GONGBI,
            name = "工笔大师 02",
            description = "重彩工笔，富丽堂皇",
            params = floatArrayOf(
                0.2f, 1.4f, 0.5f, -0.2f, 0.35f, -0.25f, 0.25f, 0.95f, 0.3f, 0.25f, -0.15f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "色彩浓郁，对比强烈，富丽堂皇"
        ),
        MasterFilter(
            id = "master_gongbi_03",
            series = FilterSeries.GONGBI,
            name = "工笔大师 03",
            description = "淡彩工笔，清新雅致",
            params = floatArrayOf(
                0.1f, 1.25f, 0.35f, -0.1f, 0.2f, -0.15f, 0.15f, 0.85f, 0.2f, 0.15f, -0.05f, 0.25f,
                0.5f, 0.3f, 0.2f, 0.6f, 0.35f, 0.2f, 0.15f, 0.25f, 0.3f, 0.2f, 0.15f, 0.2f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.35f
            ),
            characteristics = "淡彩雅致，细节精美，清新脱俗"
        ),
        MasterFilter(
            id = "master_gongbi_04",
            series = FilterSeries.GONGBI,
            name = "工笔大师 04",
            description = "没骨工笔，色彩融合",
            params = floatArrayOf(
                0.12f, 1.3f, 0.4f, -0.12f, 0.25f, -0.18f, 0.18f, 0.88f, 0.22f, 0.18f, -0.08f, 0.28f,
                0.55f, 0.35f, 0.25f, 0.65f, 0.4f, 0.25f, 0.2f, 0.3f, 0.35f, 0.25f, 0.2f, 0.25f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.4f
            ),
            characteristics = "色彩融合，过渡自然，层次丰富"
        ),
        MasterFilter(
            id = "master_gongbi_05",
            series = FilterSeries.GONGBI,
            name = "工笔大师 05",
            description = "金碧工笔，华贵典雅",
            params = floatArrayOf(
                0.25f, 1.45f, 0.55f, -0.25f, 0.4f, -0.3f, 0.3f, 1.0f, 0.35f, 0.3f, -0.2f, 0.4f,
                0.7f, 0.5f, 0.4f, 0.8f, 0.55f, 0.4f, 0.35f, 0.45f, 0.5f, 0.4f, 0.35f, 0.4f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.55f
            ),
            characteristics = "金碧辉煌，华贵典雅，富丽堂皇"
        ),
        MasterFilter(
            id = "master_gongbi_06",
            series = FilterSeries.GONGBI,
            name = "工笔大师 06",
            description = "白描工笔，线条流畅",
            params = floatArrayOf(
                0.08f, 1.2f, 0.3f, -0.08f, 0.15f, -0.12f, 0.12f, 0.82f, 0.18f, 0.12f, -0.02f, 0.22f,
                0.45f, 0.25f, 0.15f, 0.55f, 0.3f, 0.15f, 0.1f, 0.2f, 0.25f, 0.15f, 0.1f, 0.15f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.3f
            ),
            characteristics = "线条流畅，结构清晰，简洁明快"
        ),
        MasterFilter(
            id = "master_gongbi_07",
            series = FilterSeries.GONGBI,
            name = "工笔大师 07",
            description = "青绿工笔，山水意境",
            params = floatArrayOf(
                0.18f, 1.38f, 0.48f, -0.18f, 0.32f, -0.22f, 0.22f, 0.92f, 0.28f, 0.22f, -0.12f, 0.32f,
                0.62f, 0.42f, 0.32f, 0.72f, 0.48f, 0.32f, 0.28f, 0.38f, 0.42f, 0.32f, 0.28f, 0.32f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.48f
            ),
            characteristics = "青绿山水，意境深远，古朴典雅"
        ),
        MasterFilter(
            id = "master_gongbi_08",
            series = FilterSeries.GONGBI,
            name = "工笔大师 08",
            description = "重彩花鸟，生动传神",
            params = floatArrayOf(
                0.22f, 1.42f, 0.52f, -0.22f, 0.38f, -0.28f, 0.28f, 0.98f, 0.32f, 0.28f, -0.18f, 0.38f,
                0.68f, 0.48f, 0.38f, 0.78f, 0.52f, 0.38f, 0.32f, 0.42f, 0.48f, 0.38f, 0.32f, 0.38f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.52f
            ),
            characteristics = "色彩艳丽，生动传神，栩栩如生"
        ),
        MasterFilter(
            id = "master_gongbi_09",
            series = FilterSeries.GONGBI,
            name = "工笔大师 09",
            description = "仕女工笔，柔美细腻",
            params = floatArrayOf(
                0.14f, 1.32f, 0.42f, -0.14f, 0.28f, -0.2f, 0.2f, 0.9f, 0.24f, 0.2f, -0.1f, 0.3f,
                0.58f, 0.38f, 0.28f, 0.68f, 0.42f, 0.28f, 0.22f, 0.32f, 0.38f, 0.28f, 0.22f, 0.28f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.42f
            ),
            characteristics = "柔美细腻，优雅端庄，韵味十足"
        ),
        MasterFilter(
            id = "master_gongbi_10",
            series = FilterSeries.GONGBI,
            name = "工笔大师 10",
            description = "界画工笔，精准严谨",
            params = floatArrayOf(
                0.16f, 1.36f, 0.46f, -0.16f, 0.3f, -0.24f, 0.24f, 0.94f, 0.26f, 0.24f, -0.14f, 0.34f,
                0.64f, 0.44f, 0.34f, 0.74f, 0.46f, 0.34f, 0.26f, 0.36f, 0.44f, 0.34f, 0.26f, 0.34f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.46f
            ),
            characteristics = "线条精准，结构严谨，气势恢宏"
        )
    )

    // ==================== 3. 极简大师系列 (10套) ====================

    private val JIJIAN_PRESETS = listOf(
        MasterFilter(
            id = "master_jijian_01",
            series = FilterSeries.JIJIAN,
            name = "极简大师 01",
            description = "纯净简约，留白艺术",
            params = floatArrayOf(
                0.05f, 1.05f, 0.15f, -0.05f, 0.1f, -0.1f, 0.05f, 0.75f, 0.05f, 0.0f, 0.0f, 0.1f,
                0.2f, 0.05f, 0.0f, 0.4f, 0.1f, 0.0f, -0.05f, 0.05f, 0.05f, 0.0f, -0.05f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.15f
            ),
            characteristics = "极简纯净，留白艺术，意境深远"
        ),
        MasterFilter(
            id = "master_jijian_02",
            series = FilterSeries.JIJIAN,
            name = "极简大师 02",
            description = "黑白对比，简洁有力",
            params = floatArrayOf(
                0.1f, 1.3f, 0.2f, -0.1f, 0.15f, -0.15f, 0.1f, 0.0f, 0.1f, 0.05f, 0.05f, 0.15f,
                0.3f, 0.1f, 0.05f, 0.5f, 0.15f, 0.05f, 0.0f, 0.1f, 0.1f, 0.05f, 0.0f, 0.05f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.2f
            ),
            characteristics = "黑白对比，简洁有力，视觉冲击"
        ),
        MasterFilter(
            id = "master_jijian_03",
            series = FilterSeries.JIJIAN,
            name = "极简大师 03",
            description = "低饱和度，静谧淡雅",
            params = floatArrayOf(
                0.0f, 1.0f, 0.1f, 0.0f, 0.05f, -0.05f, 0.0f, 0.7f, 0.0f, -0.05f, -0.05f, 0.05f,
                0.15f, 0.0f, -0.05f, 0.35f, 0.05f, -0.05f, -0.1f, 0.0f, 0.0f, -0.05f, -0.1f, -0.05f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.1f
            ),
            characteristics = "低饱和度，静谧淡雅，禅意空灵"
        ),
        MasterFilter(
            id = "master_jijian_04",
            series = FilterSeries.JIJIAN,
            name = "极简大师 04",
            description = "几何构图，现代简约",
            params = floatArrayOf(
                0.08f, 1.15f, 0.18f, -0.08f, 0.12f, -0.12f, 0.08f, 0.78f, 0.08f, 0.02f, 0.02f, 0.12f,
                0.25f, 0.08f, 0.02f, 0.45f, 0.12f, 0.02f, -0.02f, 0.08f, 0.08f, 0.02f, -0.02f, 0.02f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.18f
            ),
            characteristics = "几何构图，现代简约，线条利落"
        ),
        MasterFilter(
            id = "master_jijian_05",
            series = FilterSeries.JIJIAN,
            name = "极简大师 05",
            description = "单色调，纯粹表达",
            params = floatArrayOf(
                0.02f, 1.08f, 0.12f, -0.02f, 0.08f, -0.08f, 0.02f, 0.72f, 0.02f, -0.02f, -0.02f, 0.08f,
                0.18f, 0.02f, -0.02f, 0.38f, 0.08f, -0.02f, -0.08f, 0.02f, 0.02f, -0.02f, -0.08f, -0.02f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.12f
            ),
            characteristics = "单色调，纯粹表达，意境深远"
        ),
        MasterFilter(
            id = "master_jijian_06",
            series = FilterSeries.JIJIAN,
            name = "极简大师 06",
            description = "负空间，视觉平衡",
            params = floatArrayOf(
                0.06f, 1.12f, 0.16f, -0.06f, 0.1f, -0.1f, 0.06f, 0.76f, 0.06f, 0.0f, 0.0f, 0.1f,
                0.22f, 0.06f, 0.0f, 0.42f, 0.1f, 0.0f, -0.06f, 0.06f, 0.06f, 0.0f, -0.06f, 0.0f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.16f
            ),
            characteristics = "负空间，视觉平衡，和谐统一"
        ),
        MasterFilter(
            id = "master_jijian_07",
            series = FilterSeries.JIJIAN,
            name = "极简大师 07",
            description = "线条艺术，简洁流畅",
            params = floatArrayOf(
                0.12f, 1.25f, 0.22f, -0.12f, 0.18f, -0.18f, 0.12f, 0.82f, 0.12f, 0.08f, 0.08f, 0.18f,
                0.35f, 0.12f, 0.08f, 0.55f, 0.18f, 0.08f, 0.02f, 0.12f, 0.12f, 0.08f, 0.02f, 0.08f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.22f
            ),
            characteristics = "线条艺术，简洁流畅，优雅大气"
        ),
        MasterFilter(
            id = "master_jijian_08",
            series = FilterSeries.JIJIAN,
            name = "极简大师 08",
            description = "纯色背景，主体突出",
            params = floatArrayOf(
                0.15f, 1.35f, 0.25f, -0.15f, 0.2f, -0.2f, 0.15f, 0.85f, 0.15f, 0.1f, 0.1f, 0.2f,
                0.4f, 0.15f, 0.1f, 0.6f, 0.2f, 0.1f, 0.05f, 0.15f, 0.15f, 0.1f, 0.05f, 0.1f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.25f
            ),
            characteristics = "纯色背景，主体突出，视觉焦点"
        ),
        MasterFilter(
            id = "master_jijian_09",
            series = FilterSeries.JIJIAN,
            name = "极简大师 09",
            description = "对称构图，秩序美感",
            params = floatArrayOf(
                0.09f, 1.18f, 0.19f, -0.09f, 0.14f, -0.14f, 0.09f, 0.79f, 0.09f, 0.04f, 0.04f, 0.14f,
                0.28f, 0.09f, 0.04f, 0.48f, 0.14f, 0.04f, -0.01f, 0.09f, 0.09f, 0.04f, -0.01f, 0.04f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.19f
            ),
            characteristics = "对称构图，秩序美感，和谐平衡"
        ),
        MasterFilter(
            id = "master_jijian_10",
            series = FilterSeries.JIJIAN,
            name = "极简大师 10",
            description = "极致简约，返璞归真",
            params = floatArrayOf(
                0.0f, 1.0f, 0.1f, 0.0f, 0.05f, -0.05f, 0.0f, 0.7f, 0.0f, -0.05f, -0.05f, 0.05f,
                0.15f, 0.0f, -0.05f, 0.35f, 0.05f, -0.05f, -0.1f, 0.0f, 0.0f, -0.05f, -0.1f, -0.05f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.1f
            ),
            characteristics = "极致简约，返璞归真，大道至简"
        )
    )

    // ==================== 4. 古典大师系列 (8套) ====================

    private val GUDIAN_PRESETS = listOf(
        MasterFilter(
            id = "master_gudian_01",
            series = FilterSeries.GUDIAN,
            name = "古典大师 01",
            description = "文艺复兴，光影大师",
            params = floatArrayOf(
                0.2f, 1.45f, 0.6f, -0.3f, 0.4f, -0.35f, 0.3f, 0.95f, 0.35f, 0.3f, -0.2f, 0.4f,
                0.7f, 0.5f, 0.4f, 0.8f, 0.55f, 0.4f, 0.35f, 0.45f, 0.5f, 0.4f, 0.35f, 0.4f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.6f
            ),
            characteristics = "明暗对比强烈，光影层次丰富"
        ),
        MasterFilter(
            id = "master_gudian_02",
            series = FilterSeries.GUDIAN,
            name = "古典大师 02",
            description = "巴洛克，戏剧张力",
            params = floatArrayOf(
                0.25f, 1.5f, 0.65f, -0.35f, 0.45f, -0.4f, 0.35f, 1.0f, 0.4f, 0.35f, -0.25f, 0.45f,
                0.75f, 0.55f, 0.45f, 0.85f, 0.6f, 0.45f, 0.4f, 0.5f, 0.55f, 0.45f, 0.4f, 0.45f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.65f
            ),
            characteristics = "戏剧张力，动感强烈，富丽堂皇"
        ),
        MasterFilter(
            id = "master_gudian_03",
            series = FilterSeries.GUDIAN,
            name = "古典大师 03",
            description = "洛可可，优雅精致",
            params = floatArrayOf(
                0.15f, 1.35f, 0.5f, -0.2f, 0.3f, -0.25f, 0.2f, 0.9f, 0.25f, 0.2f, -0.15f, 0.3f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.45f, 0.3f, 0.25f, 0.35f, 0.4f, 0.3f, 0.25f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "优雅精致，色彩柔和，装饰华丽"
        ),
        MasterFilter(
            id = "master_gudian_04",
            series = FilterSeries.GUDIAN,
            name = "古典大师 04",
            description = "新古典，庄重典雅",
            params = floatArrayOf(
                0.18f, 1.4f, 0.55f, -0.25f, 0.35f, -0.3f, 0.25f, 0.92f, 0.3f, 0.25f, -0.18f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.55f
            ),
            characteristics = "庄重典雅，结构严谨，古朴大气"
        ),
        MasterFilter(
            id = "master_gudian_05",
            series = FilterSeries.GUDIAN,
            name = "古典大师 05",
            description = "印象派，光影变幻",
            params = floatArrayOf(
                0.12f, 1.3f, 0.45f, -0.15f, 0.25f, -0.2f, 0.15f, 0.88f, 0.22f, 0.15f, -0.12f, 0.25f,
                0.55f, 0.35f, 0.25f, 0.65f, 0.4f, 0.25f, 0.2f, 0.3f, 0.35f, 0.25f, 0.2f, 0.25f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.45f
            ),
            characteristics = "光影变幻，色彩明快，笔触生动"
        ),
        MasterFilter(
            id = "master_gudian_06",
            series = FilterSeries.GUDIAN,
            name = "古典大师 06",
            description = "浪漫主义，情感丰富",
            params = floatArrayOf(
                0.22f, 1.42f, 0.58f, -0.28f, 0.38f, -0.32f, 0.28f, 0.96f, 0.32f, 0.28f, -0.22f, 0.38f,
                0.68f, 0.48f, 0.38f, 0.78f, 0.52f, 0.38f, 0.32f, 0.42f, 0.48f, 0.38f, 0.32f, 0.38f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.58f
            ),
            characteristics = "情感丰富，色彩浓郁，气氛浓烈"
        ),
        MasterFilter(
            id = "master_gudian_07",
            series = FilterSeries.GUDIAN,
            name = "古典大师 07",
            description = "写实主义，细节精准",
            params = floatArrayOf(
                0.16f, 1.38f, 0.52f, -0.22f, 0.32f, -0.28f, 0.22f, 0.9f, 0.28f, 0.22f, -0.16f, 0.32f,
                0.62f, 0.42f, 0.32f, 0.72f, 0.48f, 0.32f, 0.28f, 0.38f, 0.42f, 0.32f, 0.28f, 0.32f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.52f
            ),
            characteristics = "细节精准，写实逼真，质感强烈"
        ),
        MasterFilter(
            id = "master_gudian_08",
            series = FilterSeries.GUDIAN,
            name = "古典大师 08",
            description = "学院派，技法精湛",
            params = floatArrayOf(
                0.2f, 1.4f, 0.56f, -0.26f, 0.36f, -0.3f, 0.26f, 0.94f, 0.3f, 0.26f, -0.2f, 0.36f,
                0.66f, 0.46f, 0.36f, 0.76f, 0.5f, 0.36f, 0.3f, 0.4f, 0.46f, 0.36f, 0.3f, 0.36f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.56f
            ),
            characteristics = "技法精湛，构图严谨，古典美学"
        )
    )

    // ==================== 5. 现代大师系列 (8套) ====================

    private val XIANDAI_PRESETS = listOf(
        MasterFilter(
            id = "master_xiandai_01",
            series = FilterSeries.XIANDAI,
            name = "现代大师 01",
            description = "抽象表现，色彩爆发",
            params = floatArrayOf(
                0.3f, 1.6f, 0.7f, -0.4f, 0.5f, -0.45f, 0.4f, 1.1f, 0.45f, 0.4f, -0.3f, 0.5f,
                0.8f, 0.6f, 0.5f, 0.9f, 0.65f, 0.5f, 0.45f, 0.55f, 0.6f, 0.5f, 0.45f, 0.5f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.7f
            ),
            characteristics = "色彩爆发，对比强烈，视觉冲击"
        ),
        MasterFilter(
            id = "master_xiandai_02",
            series = FilterSeries.XIANDAI,
            name = "现代大师 02",
            description = "波普艺术，鲜艳夸张",
            params = floatArrayOf(
                0.35f, 1.7f, 0.75f, -0.45f, 0.55f, -0.5f, 0.45f, 1.15f, 0.5f, 0.45f, -0.35f, 0.55f,
                0.85f, 0.65f, 0.55f, 0.95f, 0.7f, 0.55f, 0.5f, 0.6f, 0.65f, 0.55f, 0.5f, 0.55f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.75f
            ),
            characteristics = "鲜艳夸张，大胆前卫，流行文化"
        ),
        MasterFilter(
            id = "master_xiandai_03",
            series = FilterSeries.XIANDAI,
            name = "现代大师 03",
            description = "极简主义，纯粹形式",
            params = floatArrayOf(
                0.0f, 1.0f, 0.1f, 0.0f, 0.05f, -0.05f, 0.0f, 0.7f, 0.0f, -0.05f, -0.05f, 0.05f,
                0.15f, 0.0f, -0.05f, 0.35f, 0.05f, -0.05f, -0.1f, 0.0f, 0.0f, -0.05f, -0.1f, -0.05f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.1f
            ),
            characteristics = "纯粹形式，极简主义，哲学思考"
        ),
        MasterFilter(
            id = "master_xiandai_04",
            series = FilterSeries.XIANDAI,
            name = "现代大师 04",
            description = "概念艺术，观念先行",
            params = floatArrayOf(
                0.1f, 1.2f, 0.3f, -0.1f, 0.15f, -0.15f, 0.1f, 0.8f, 0.1f, 0.05f, 0.0f, 0.15f,
                0.35f, 0.15f, 0.1f, 0.5f, 0.2f, 0.1f, 0.05f, 0.15f, 0.15f, 0.1f, 0.05f, 0.1f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.3f
            ),
            characteristics = "观念先行，思想深刻，艺术哲学"
        ),
        MasterFilter(
            id = "master_xiandai_05",
            series = FilterSeries.XIANDAI,
            name = "现代大师 05",
            description = "超现实，梦幻奇异",
            params = floatArrayOf(
                0.25f, 1.5f, 0.6f, -0.3f, 0.4f, -0.35f, 0.3f, 1.0f, 0.35f, 0.3f, -0.25f, 0.4f,
                0.7f, 0.5f, 0.4f, 0.8f, 0.55f, 0.4f, 0.35f, 0.45f, 0.5f, 0.4f, 0.35f, 0.4f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.6f
            ),
            characteristics = "梦幻奇异，超现实主义，想象力"
        ),
        MasterFilter(
            id = "master_xiandai_06",
            series = FilterSeries.XIANDAI,
            name = "现代大师 06",
            description = "表现主义，情感强烈",
            params = floatArrayOf(
                0.28f, 1.55f, 0.65f, -0.35f, 0.45f, -0.4f, 0.35f, 1.05f, 0.4f, 0.35f, -0.28f, 0.45f,
                0.75f, 0.55f, 0.45f, 0.85f, 0.6f, 0.45f, 0.4f, 0.5f, 0.55f, 0.45f, 0.4f, 0.45f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.65f
            ),
            characteristics = "情感强烈，色彩浓烈，主观表达"
        ),
        MasterFilter(
            id = "master_xiandai_07",
            series = FilterSeries.XIANDAI,
            name = "现代大师 07",
            description = "立体主义，多维视角",
            params = floatArrayOf(
                0.2f, 1.4f, 0.55f, -0.25f, 0.35f, -0.3f, 0.25f, 0.95f, 0.3f, 0.25f, -0.2f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.55f
            ),
            characteristics = "多维视角，几何解构，创新思维"
        ),
        MasterFilter(
            id = "master_xiandai_08",
            series = FilterSeries.XIANDAI,
            name = "现代大师 08",
            description = "未来主义，动感科技",
            params = floatArrayOf(
                0.32f, 1.65f, 0.72f, -0.42f, 0.52f, -0.48f, 0.42f, 1.12f, 0.48f, 0.42f, -0.32f, 0.52f,
                0.82f, 0.62f, 0.52f, 0.92f, 0.68f, 0.52f, 0.48f, 0.58f, 0.62f, 0.52f, 0.48f, 0.52f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.72f
            ),
            characteristics = "动感科技，未来感，速度与力量"
        )
    )

    // ==================== 6. 电影大师系列 (8套) ====================

    private val DIANYING_PRESETS = listOf(
        MasterFilter(
            id = "master_dianying_01",
            series = FilterSeries.DIANYING,
            name = "电影大师 01",
            description = "黑色电影，高对比",
            params = floatArrayOf(
                0.15f, 1.6f, 0.5f, -0.4f, 0.45f, -0.5f, 0.35f, 0.0f, 0.3f, 0.25f, 0.2f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "高对比度，黑白分明，悬疑氛围"
        ),
        MasterFilter(
            id = "master_dianying_02",
            series = FilterSeries.DIANYING,
            name = "电影大师 02",
            description = "赛博朋克，霓虹冷调",
            params = floatArrayOf(
                0.2f, 1.5f, 0.6f, -0.35f, 0.4f, -0.4f, 0.3f, 0.3f, 0.35f, 0.3f, 0.25f, 0.4f,
                0.7f, 0.5f, 0.4f, 0.8f, 0.55f, 0.4f, 0.35f, 0.45f, 0.5f, 0.4f, 0.35f, 0.4f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.6f
            ),
            characteristics = "霓虹冷调，未来感，科技氛围"
        ),
        MasterFilter(
            id = "master_dianying_03",
            series = FilterSeries.DIANYING,
            name = "电影大师 03",
            description = "西部片，暖色沙漠",
            params = floatArrayOf(
                0.25f, 1.45f, 0.55f, -0.3f, 0.35f, -0.35f, 0.25f, 0.6f, 0.3f, 0.25f, 0.2f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.55f
            ),
            characteristics = "暖色沙漠，粗犷豪放，西部风情"
        ),
        MasterFilter(
            id = "master_dianying_04",
            series = FilterSeries.DIANYING,
            name = "电影大师 04",
            description = "恐怖片，阴暗诡异",
            params = floatArrayOf(
                -0.1f, 1.7f, 0.4f, -0.5f, 0.5f, -0.6f, 0.4f, -0.2f, 0.25f, 0.2f, 0.15f, 0.3f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.45f, 0.3f, 0.25f, 0.35f, 0.4f, 0.3f, 0.25f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.4f
            ),
            characteristics = "阴暗诡异，恐怖氛围，悬疑感"
        ),
        MasterFilter(
            id = "master_dianying_05",
            series = FilterSeries.DIANYING,
            name = "电影大师 05",
            description = "文艺片，柔和淡雅",
            params = floatArrayOf(
                0.1f, 1.2f, 0.35f, -0.15f, 0.2f, -0.2f, 0.15f, 0.85f, 0.15f, 0.1f, 0.05f, 0.2f,
                0.45f, 0.25f, 0.15f, 0.55f, 0.3f, 0.15f, 0.1f, 0.2f, 0.25f, 0.15f, 0.1f, 0.15f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.35f
            ),
            characteristics = "柔和淡雅，文艺气息，情感细腻"
        ),
        MasterFilter(
            id = "master_dianying_06",
            series = FilterSeries.DIANYING,
            name = "电影大师 06",
            description = "动作片，高饱和度",
            params = floatArrayOf(
                0.3f, 1.65f, 0.7f, -0.4f, 0.5f, -0.45f, 0.4f, 1.0f, 0.45f, 0.4f, 0.35f, 0.5f,
                0.8f, 0.6f, 0.5f, 0.9f, 0.65f, 0.5f, 0.45f, 0.55f, 0.6f, 0.5f, 0.45f, 0.5f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.7f
            ),
            characteristics = "高饱和度，动感十足，视觉冲击"
        ),
        MasterFilter(
            id = "master_dianying_07",
            series = FilterSeries.DIANYING,
            name = "电影大师 07",
            description = "爱情片，温暖柔光",
            params = floatArrayOf(
                0.18f, 1.25f, 0.42f, -0.18f, 0.25f, -0.22f, 0.18f, 0.88f, 0.22f, 0.18f, 0.12f, 0.25f,
                0.52f, 0.32f, 0.22f, 0.62f, 0.38f, 0.22f, 0.18f, 0.28f, 0.32f, 0.22f, 0.18f, 0.22f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.42f
            ),
            characteristics = "温暖柔光，浪漫氛围，情感丰富"
        ),
        MasterFilter(
            id = "master_dianying_08",
            series = FilterSeries.DIANYING,
            name = "电影大师 08",
            description = "科幻片，冷色调",
            params = floatArrayOf(
                0.12f, 1.55f, 0.48f, -0.32f, 0.38f, -0.38f, 0.28f, 0.2f, 0.32f, 0.28f, 0.22f, 0.38f,
                0.68f, 0.48f, 0.38f, 0.78f, 0.52f, 0.38f, 0.32f, 0.42f, 0.48f, 0.38f, 0.32f, 0.38f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.48f
            ),
            characteristics = "冷色调，科幻感，未来氛围"
        )
    )

    // ==================== 7. 黑白大师系列 (7套) ====================

    private val HEIBAI_PRESETS = listOf(
        MasterFilter(
            id = "master_heibai_01",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 01",
            description = "经典黑白，对比强烈",
            params = floatArrayOf(
                0.1f, 1.5f, 0.5f, -0.4f, 0.4f, -0.45f, 0.35f, 0.0f, 0.3f, 0.25f, 0.2f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "经典黑白，对比强烈，层次丰富"
        ),
        MasterFilter(
            id = "master_heibai_02",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 02",
            description = "高调黑白，明亮通透",
            params = floatArrayOf(
                0.3f, 1.3f, 0.6f, -0.2f, 0.3f, -0.25f, 0.2f, 0.0f, 0.25f, 0.2f, 0.15f, 0.3f,
                0.55f, 0.35f, 0.25f, 0.65f, 0.4f, 0.25f, 0.2f, 0.3f, 0.35f, 0.25f, 0.2f, 0.25f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.6f
            ),
            characteristics = "高调明亮，通透清新，优雅大气"
        ),
        MasterFilter(
            id = "master_heibai_03",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 03",
            description = "低调黑白，深沉神秘",
            params = floatArrayOf(
                -0.2f, 1.7f, 0.4f, -0.6f, 0.5f, -0.65f, 0.5f, 0.0f, 0.35f, 0.3f, 0.25f, 0.4f,
                0.7f, 0.5f, 0.4f, 0.8f, 0.55f, 0.4f, 0.35f, 0.45f, 0.5f, 0.4f, 0.35f, 0.4f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.4f
            ),
            characteristics = "低调深沉，神秘氛围，戏剧张力"
        ),
        MasterFilter(
            id = "master_heibai_04",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 04",
            description = "中间调黑白，平衡和谐",
            params = floatArrayOf(
                0.05f, 1.4f, 0.45f, -0.3f, 0.35f, -0.35f, 0.3f, 0.0f, 0.28f, 0.22f, 0.18f, 0.32f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.45f, 0.3f, 0.25f, 0.35f, 0.4f, 0.3f, 0.25f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.45f
            ),
            characteristics = "中间调，平衡和谐，细节丰富"
        ),
        MasterFilter(
            id = "master_heibai_05",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 05",
            description = "颗粒黑白，复古质感",
            params = floatArrayOf(
                0.08f, 1.45f, 0.48f, -0.32f, 0.38f, -0.38f, 0.32f, 0.0f, 0.3f, 0.25f, 0.2f, 0.35f,
                0.62f, 0.42f, 0.32f, 0.72f, 0.48f, 0.32f, 0.28f, 0.38f, 0.42f, 0.32f, 0.28f, 0.32f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.48f
            ),
            characteristics = "颗粒质感，复古氛围，胶片感"
        ),
        MasterFilter(
            id = "master_heibai_06",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 06",
            description = "柔和黑白，细腻优雅",
            params = floatArrayOf(
                0.15f, 1.25f, 0.4f, -0.2f, 0.25f, -0.25f, 0.2f, 0.0f, 0.22f, 0.18f, 0.12f, 0.25f,
                0.5f, 0.3f, 0.2f, 0.6f, 0.35f, 0.2f, 0.15f, 0.25f, 0.3f, 0.2f, 0.15f, 0.2f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.4f
            ),
            characteristics = "柔和细腻，优雅气质，人文情怀"
        ),
        MasterFilter(
            id = "master_heibai_07",
            series = FilterSeries.HEIBAI,
            name = "黑白大师 07",
            description = "硬调黑白，极致对比",
            params = floatArrayOf(
                0.0f, 1.8f, 0.5f, -0.7f, 0.6f, -0.75f, 0.6f, 0.0f, 0.4f, 0.35f, 0.3f, 0.45f,
                0.75f, 0.55f, 0.45f, 0.85f, 0.6f, 0.45f, 0.4f, 0.5f, 0.55f, 0.45f, 0.4f, 0.45f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "极致对比，硬调风格，视觉冲击"
        )
    )

    // ==================== 8. 胶片大师系列 (10套) ====================

    private val JIAOPIAN_PRESETS = listOf(
        MasterFilter(
            id = "master_jiaopian_01",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 01",
            description = "富士 Velvia，色彩浓郁",
            params = floatArrayOf(
                0.15f, 1.45f, 0.55f, -0.25f, 0.35f, -0.3f, 0.25f, 0.95f, 0.3f, 0.25f, 0.2f, 0.35f,
                0.65f, 0.45f, 0.35f, 0.75f, 0.5f, 0.35f, 0.3f, 0.4f, 0.45f, 0.35f, 0.3f, 0.35f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.55f
            ),
            characteristics = "色彩浓郁，饱和度高，风景利器"
        ),
        MasterFilter(
            id = "master_jiaopian_02",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 02",
            description = "柯达 Portra，肤色优美",
            params = floatArrayOf(
                0.12f, 1.3f, 0.45f, -0.18f, 0.28f, -0.22f, 0.18f, 0.88f, 0.22f, 0.18f, 0.12f, 0.28f,
                0.55f, 0.35f, 0.25f, 0.65f, 0.4f, 0.25f, 0.2f, 0.3f, 0.35f, 0.25f, 0.2f, 0.25f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.45f
            ),
            characteristics = "肤色优美，色彩自然，人像首选"
        ),
        MasterFilter(
            id = "master_jiaopian_03",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 03",
            description = "柯达 Ektar，细节丰富",
            params = floatArrayOf(
                0.18f, 1.4f, 0.5f, -0.22f, 0.32f, -0.28f, 0.22f, 0.92f, 0.28f, 0.22f, 0.18f, 0.32f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.45f, 0.3f, 0.25f, 0.35f, 0.4f, 0.3f, 0.25f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "细节丰富，色彩鲜艳，扫描感"
        ),
        MasterFilter(
            id = "master_jiaopian_04",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 04",
            description = "富士 Pro 400H，柔和淡雅",
            params = floatArrayOf(
                0.1f, 1.25f, 0.4f, -0.15f, 0.25f, -0.2f, 0.15f, 0.85f, 0.2f, 0.15f, 0.1f, 0.25f,
                0.5f, 0.3f, 0.2f, 0.6f, 0.35f, 0.2f, 0.15f, 0.25f, 0.3f, 0.2f, 0.15f, 0.2f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.4f
            ),
            characteristics = "柔和淡雅，色彩清新，婚礼摄影"
        ),
        MasterFilter(
            id = "master_jiaopian_05",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 05",
            description = "柯达 Gold 200，温暖复古",
            params = floatArrayOf(
                0.2f, 1.35f, 0.48f, -0.2f, 0.3f, -0.25f, 0.2f, 0.9f, 0.25f, 0.2f, 0.15f, 0.3f,
                0.58f, 0.38f, 0.28f, 0.68f, 0.42f, 0.28f, 0.22f, 0.32f, 0.38f, 0.28f, 0.22f, 0.28f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.48f
            ),
            characteristics = "温暖复古，黄调明显，怀旧感"
        ),
        MasterFilter(
            id = "master_jiaopian_06",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 06",
            description = "Lomography，色彩偏移",
            params = floatArrayOf(
                0.22f, 1.5f, 0.52f, -0.28f, 0.38f, -0.32f, 0.28f, 0.98f, 0.32f, 0.28f, 0.22f, 0.38f,
                0.62f, 0.42f, 0.32f, 0.72f, 0.48f, 0.32f, 0.28f, 0.38f, 0.42f, 0.32f, 0.28f, 0.32f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.52f
            ),
            characteristics = "色彩偏移，暗角明显，玩具相机"
        ),
        MasterFilter(
            id = "master_jiaopian_07",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 07",
            description = "Ilford HP5，黑白颗粒",
            params = floatArrayOf(
                0.08f, 1.42f, 0.46f, -0.3f, 0.36f, -0.36f, 0.3f, 0.0f, 0.28f, 0.24f, 0.18f, 0.34f,
                0.64f, 0.44f, 0.34f, 0.74f, 0.48f, 0.34f, 0.28f, 0.38f, 0.44f, 0.34f, 0.28f, 0.34f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.46f
            ),
            characteristics = "黑白颗粒，对比适中，经典胶片"
        ),
        MasterFilter(
            id = "master_jiaopian_08",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 08",
            description = "Cinestill 800T，电影感",
            params = floatArrayOf(
                0.16f, 1.38f, 0.5f, -0.24f, 0.34f, -0.3f, 0.24f, 0.3f, 0.3f, 0.24f, 0.2f, 0.34f,
                0.6f, 0.4f, 0.3f, 0.7f, 0.46f, 0.3f, 0.26f, 0.36f, 0.4f, 0.3f, 0.26f, 0.3f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.5f
            ),
            characteristics = "电影感，冷调蓝绿，夜景光晕"
        ),
        MasterFilter(
            id = "master_jiaopian_09",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 09",
            description = "Agfa Vista，鲜艳明快",
            params = floatArrayOf(
                0.25f, 1.48f, 0.58f, -0.26f, 0.4f, -0.34f, 0.26f, 0.96f, 0.34f, 0.26f, 0.24f, 0.4f,
                0.68f, 0.48f, 0.38f, 0.78f, 0.52f, 0.38f, 0.32f, 0.42f, 0.48f, 0.38f, 0.32f, 0.38f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.58f
            ),
            characteristics = "鲜艳明快，色彩饱满，欧洲风情"
        ),
        MasterFilter(
            id = "master_jiaopian_10",
            series = FilterSeries.JIAOPIAN,
            name = "胶片大师 10",
            description = "Polaroid SX-70，即时成像",
            params = floatArrayOf(
                0.14f, 1.32f, 0.44f, -0.16f, 0.26f, -0.24f, 0.16f, 0.86f, 0.24f, 0.16f, 0.14f, 0.26f,
                0.54f, 0.34f, 0.24f, 0.64f, 0.4f, 0.24f, 0.18f, 0.28f, 0.34f, 0.24f, 0.18f, 0.24f,
                0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f,
                0.44f
            ),
            characteristics = "即时成像，柔和梦幻，怀旧情怀"
        )
    )

    /**
     * 获取所有大师滤镜列表
     */
    fun getAllMasterFilters(): List<MasterFilter> {
        return XIEYI_PRESETS + GONGBI_PRESETS + JIJIAN_PRESETS + GUDIAN_PRESETS + 
               XIANDAI_PRESETS + DIANYING_PRESETS + HEIBAI_PRESETS + JIAOPIAN_PRESETS
    }

    /**
     * 根据系列获取滤镜列表
     */
    fun getFiltersBySeries(series: FilterSeries): List<MasterFilter> {
        return when (series) {
            FilterSeries.XIEYI -> XIEYI_PRESETS
            FilterSeries.GONGBI -> GONGBI_PRESETS
            FilterSeries.JIJIAN -> JIJIAN_PRESETS
            FilterSeries.GUDIAN -> GUDIAN_PRESETS
            FilterSeries.XIANDAI -> XIANDAI_PRESETS
            FilterSeries.DIANYING -> DIANYING_PRESETS
            FilterSeries.HEIBAI -> HEIBAI_PRESETS
            FilterSeries.JIAOPIAN -> JIAOPIAN_PRESETS
        }
    }

    /**
     * 根据 ID 获取滤镜
     */
    fun getFilterById(id: String): MasterFilter? {
        return getAllMasterFilters().find { it.id == id }
    }

    /**
     * 获取滤镜总数
     */
    fun getTotalFilterCount(): Int {
        return getAllMasterFilters().size
    }
}
