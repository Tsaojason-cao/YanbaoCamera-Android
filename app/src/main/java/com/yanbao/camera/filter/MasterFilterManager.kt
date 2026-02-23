package com.yanbao.camera.filter

import android.content.Context
import android.location.Geocoder
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.util.Locale

/**
 * Phase 2: 大师滤镜管理器
 *
 * 从 assets/filters.json 加载 91 国滤镜数据，提供：
 * - 按国家代码查询
 * - 按 GPS 坐标（逆地理编码）查询
 * - 全量列表获取
 * - 随机推荐
 *
 * 使用懒加载，首次调用时从 assets 读取并缓存。
 */
class MasterFilterManager(private val context: Context) {

    companion object {
        private const val TAG = "MasterFilterManager"
        private const val FILTERS_ASSET = "filters.json"
    }

    private val gson = Gson()

    /** 懒加载滤镜列表 */
    private val filters: List<CountryFilter> by lazy {
        loadFiltersFromAssets()
    }

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 获取所有滤镜
     */
    fun getAllFilters(): List<CountryFilter> = filters

    /**
     * 按国家代码查询滤镜（大小写不敏感）
     */
    fun getFilterForCountry(countryCode: String): CountryFilter? =
        filters.find { it.countryCode.equals(countryCode, ignoreCase = true) }

    /**
     * 按 GPS 坐标查询滤镜（通过逆地理编码获取国家代码）
     * 需在 IO 线程调用
     */
    suspend fun getFilterByLocation(lat: Double, lng: Double): CountryFilter? =
        withContext(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                val countryCode = addresses?.firstOrNull()?.countryCode
                Log.d(TAG, "Reverse geocode ($lat, $lng) → countryCode=$countryCode")
                countryCode?.let { getFilterForCountry(it) }
            } catch (e: Exception) {
                Log.w(TAG, "Reverse geocode failed: ${e.message}")
                null
            }
        }

    /**
     * 随机推荐一个滤镜
     */
    fun getRandomFilter(): CountryFilter? = filters.randomOrNull()

    /**
     * 按场景标签查询（如 "portrait", "landscape"）
     */
    fun getFiltersByScene(scene: String): List<CountryFilter> =
        filters.filter { it.scene?.contains(scene, ignoreCase = true) == true }

    // ─── 私有方法 ─────────────────────────────────────────────────────────

    private fun loadFiltersFromAssets(): List<CountryFilter> {
        return try {
            val inputStream = context.assets.open(FILTERS_ASSET)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<CountryFilter>>() {}.type
            val result: List<CountryFilter> = gson.fromJson(reader, type)
            Log.d(TAG, "Loaded ${result.size} filters from $FILTERS_ASSET")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load filters from assets: ${e.message}")
            // 返回内置的默认滤镜列表（防止 assets 文件缺失时崩溃）
            buildDefaultFilters()
        }
    }

    /**
     * 内置默认滤镜（assets/filters.json 不存在时的 fallback）
     */
    private fun buildDefaultFilters(): List<CountryFilter> = listOf(
        CountryFilter("CN", "中国·古韵",    shaderParams = mapOf("colorTemp" to 0.6f, "saturation" to 1.2f, "grain" to 0.1f)),
        CountryFilter("JP", "日本·樱花",    shaderParams = mapOf("colorTemp" to 0.45f, "saturation" to 1.1f, "skinWhiten" to 0.3f)),
        CountryFilter("KR", "韩国·偶像",    shaderParams = mapOf("saturation" to 1.3f, "skinSmooth" to 0.5f, "skinWhiten" to 0.4f)),
        CountryFilter("FR", "法国·浪漫",    shaderParams = mapOf("colorTemp" to 0.55f, "vignette" to 0.3f, "grain" to 0.15f)),
        CountryFilter("IT", "意大利·阳光",  shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.4f, "colorBoost" to 0.2f)),
        CountryFilter("US", "美国·好莱坞",  shaderParams = mapOf("clarity" to 0.3f, "sharpness" to 0.7f, "colorBoost" to 0.15f)),
        CountryFilter("GB", "英国·复古",    shaderParams = mapOf("saturation" to 0.8f, "grain" to 0.2f, "vignette" to 0.4f)),
        CountryFilter("DE", "德国·工业",    shaderParams = mapOf("saturation" to 0.9f, "clarity" to 0.4f, "sharpness" to 0.8f)),
        CountryFilter("ES", "西班牙·热情",  shaderParams = mapOf("colorTemp" to 0.7f, "saturation" to 1.5f, "colorBoost" to 0.3f)),
        CountryFilter("IN", "印度·彩色",    shaderParams = mapOf("saturation" to 1.6f, "colorBoost" to 0.4f, "colorTemp" to 0.65f)),
        CountryFilter("BR", "巴西·热带",    shaderParams = mapOf("saturation" to 1.5f, "colorTemp" to 0.7f, "dehaze" to 0.2f)),
        CountryFilter("AU", "澳大利亚·蓝天",shaderParams = mapOf("colorTemp" to 0.4f, "saturation" to 1.3f, "dehaze" to 0.3f)),
        CountryFilter("CA", "加拿大·枫叶",  shaderParams = mapOf("colorTemp" to 0.55f, "saturation" to 1.2f, "grain" to 0.05f)),
        CountryFilter("MX", "墨西哥·活力",  shaderParams = mapOf("saturation" to 1.6f, "colorTemp" to 0.68f, "colorBoost" to 0.35f)),
        CountryFilter("RU", "俄罗斯·冰雪",  shaderParams = mapOf("colorTemp" to 0.3f, "saturation" to 0.85f, "clarity" to 0.2f)),
        CountryFilter("TR", "土耳其·蓝白",  shaderParams = mapOf("colorTemp" to 0.35f, "saturation" to 1.2f, "sharpness" to 0.6f)),
        CountryFilter("TH", "泰国·金佛",    shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.3f, "colorBoost" to 0.25f)),
        CountryFilter("EG", "埃及·沙漠",    shaderParams = mapOf("colorTemp" to 0.75f, "saturation" to 1.1f, "grain" to 0.1f)),
        CountryFilter("ZA", "南非·草原",    shaderParams = mapOf("colorTemp" to 0.62f, "saturation" to 1.25f, "dehaze" to 0.15f)),
        CountryFilter("AR", "阿根廷·探戈",  shaderParams = mapOf("saturation" to 1.3f, "vignette" to 0.25f, "grain" to 0.1f)),
        CountryFilter("GR", "希腊·爱琴海",  shaderParams = mapOf("colorTemp" to 0.38f, "saturation" to 1.2f, "clarity" to 0.15f)),
        CountryFilter("PT", "葡萄牙·海洋",  shaderParams = mapOf("colorTemp" to 0.42f, "saturation" to 1.15f, "vignette" to 0.2f)),
        CountryFilter("NL", "荷兰·郁金香",  shaderParams = mapOf("saturation" to 1.4f, "colorBoost" to 0.3f, "sharpness" to 0.6f)),
        CountryFilter("SE", "瑞典·极光",    shaderParams = mapOf("colorTemp" to 0.32f, "saturation" to 1.1f, "clarity" to 0.2f)),
        CountryFilter("NO", "挪威·峡湾",    shaderParams = mapOf("colorTemp" to 0.3f, "saturation" to 1.05f, "dehaze" to 0.1f)),
        CountryFilter("CH", "瑞士·雪山",    shaderParams = mapOf("colorTemp" to 0.35f, "sharpness" to 0.7f, "clarity" to 0.25f)),
        CountryFilter("AT", "奥地利·古典",  shaderParams = mapOf("saturation" to 0.95f, "grain" to 0.12f, "vignette" to 0.3f)),
        CountryFilter("PL", "波兰·琥珀",    shaderParams = mapOf("colorTemp" to 0.58f, "saturation" to 1.1f, "grain" to 0.08f)),
        CountryFilter("CZ", "捷克·波西米亚",shaderParams = mapOf("saturation" to 1.05f, "grain" to 0.15f, "vignette" to 0.2f)),
        CountryFilter("HU", "匈牙利·多瑙",  shaderParams = mapOf("colorTemp" to 0.52f, "saturation" to 1.1f, "clarity" to 0.1f)),
        CountryFilter("RO", "罗马尼亚·喀尔巴阡", shaderParams = mapOf("saturation" to 1.1f, "grain" to 0.12f, "dehaze" to 0.1f)),
        CountryFilter("HR", "克罗地亚·亚得里亚", shaderParams = mapOf("colorTemp" to 0.4f, "saturation" to 1.25f, "clarity" to 0.15f)),
        CountryFilter("SK", "斯洛伐克·山谷",shaderParams = mapOf("saturation" to 1.1f, "sharpness" to 0.6f, "grain" to 0.1f)),
        CountryFilter("SI", "斯洛文尼亚·绿野", shaderParams = mapOf("saturation" to 1.2f, "colorTemp" to 0.45f, "clarity" to 0.2f)),
        CountryFilter("BG", "保加利亚·玫瑰",shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.55f, "grain" to 0.08f)),
        CountryFilter("RS", "塞尔维亚·巴尔干", shaderParams = mapOf("saturation" to 1.1f, "grain" to 0.1f, "vignette" to 0.2f)),
        CountryFilter("UA", "乌克兰·麦田",  shaderParams = mapOf("colorTemp" to 0.6f, "saturation" to 1.2f, "colorBoost" to 0.15f)),
        CountryFilter("BY", "白俄罗斯·森林",shaderParams = mapOf("saturation" to 1.05f, "colorTemp" to 0.45f, "grain" to 0.1f)),
        CountryFilter("FI", "芬兰·桑拿",    shaderParams = mapOf("colorTemp" to 0.35f, "saturation" to 0.9f, "grain" to 0.08f)),
        CountryFilter("DK", "丹麦·童话",    shaderParams = mapOf("saturation" to 1.1f, "colorTemp" to 0.42f, "clarity" to 0.1f)),
        CountryFilter("IE", "爱尔兰·翡翠",  shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.42f, "dehaze" to 0.1f)),
        CountryFilter("IS", "冰岛·极光",    shaderParams = mapOf("colorTemp" to 0.28f, "saturation" to 1.0f, "clarity" to 0.3f)),
        CountryFilter("NZ", "新西兰·绿洲",  shaderParams = mapOf("saturation" to 1.25f, "colorTemp" to 0.42f, "sharpness" to 0.65f)),
        CountryFilter("SG", "新加坡·花园",  shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.48f, "sharpness" to 0.6f)),
        CountryFilter("MY", "马来西亚·热带",shaderParams = mapOf("saturation" to 1.4f, "colorTemp" to 0.55f, "colorBoost" to 0.2f)),
        CountryFilter("ID", "印度尼西亚·巴厘", shaderParams = mapOf("saturation" to 1.45f, "colorTemp" to 0.6f, "colorBoost" to 0.25f)),
        CountryFilter("PH", "菲律宾·碧海",  shaderParams = mapOf("colorTemp" to 0.38f, "saturation" to 1.35f, "clarity" to 0.2f)),
        CountryFilter("VN", "越南·青翠",    shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.5f, "grain" to 0.05f)),
        CountryFilter("TW", "台湾·夜市",    shaderParams = mapOf("saturation" to 1.2f, "colorTemp" to 0.55f, "colorBoost" to 0.15f)),
        CountryFilter("HK", "香港·霓虹",    shaderParams = mapOf("saturation" to 1.4f, "colorTemp" to 0.52f, "colorBoost" to 0.3f)),
        CountryFilter("MO", "澳门·金碧",    shaderParams = mapOf("colorTemp" to 0.62f, "saturation" to 1.25f, "colorBoost" to 0.2f)),
        CountryFilter("MN", "蒙古·草原",    shaderParams = mapOf("colorTemp" to 0.5f, "saturation" to 1.1f, "dehaze" to 0.2f)),
        CountryFilter("KZ", "哈萨克·大漠",  shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.05f, "grain" to 0.1f)),
        CountryFilter("UZ", "乌兹别克·丝路",shaderParams = mapOf("colorTemp" to 0.68f, "saturation" to 1.2f, "grain" to 0.08f)),
        CountryFilter("AF", "阿富汗·高原",  shaderParams = mapOf("colorTemp" to 0.6f, "saturation" to 1.0f, "grain" to 0.15f)),
        CountryFilter("PK", "巴基斯坦·山脉",shaderParams = mapOf("colorTemp" to 0.55f, "saturation" to 1.1f, "sharpness" to 0.65f)),
        CountryFilter("BD", "孟加拉·绿野",  shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.5f, "colorBoost" to 0.15f)),
        CountryFilter("LK", "斯里兰卡·茶园",shaderParams = mapOf("saturation" to 1.35f, "colorTemp" to 0.5f, "clarity" to 0.15f)),
        CountryFilter("NP", "尼泊尔·雪峰",  shaderParams = mapOf("colorTemp" to 0.35f, "sharpness" to 0.75f, "clarity" to 0.3f)),
        CountryFilter("BT", "不丹·幸福",    shaderParams = mapOf("saturation" to 1.2f, "colorTemp" to 0.48f, "grain" to 0.08f)),
        CountryFilter("MM", "缅甸·金塔",    shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.25f, "grain" to 0.1f)),
        CountryFilter("KH", "柬埔寨·吴哥",  shaderParams = mapOf("colorTemp" to 0.62f, "saturation" to 1.2f, "grain" to 0.12f)),
        CountryFilter("LA", "老挝·湄公河",  shaderParams = mapOf("saturation" to 1.2f, "colorTemp" to 0.55f, "grain" to 0.08f)),
        CountryFilter("IR", "伊朗·波斯",    shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.15f, "grain" to 0.1f)),
        CountryFilter("IQ", "伊拉克·两河",  shaderParams = mapOf("colorTemp" to 0.7f, "saturation" to 1.0f, "grain" to 0.12f)),
        CountryFilter("SA", "沙特·沙漠",    shaderParams = mapOf("colorTemp" to 0.78f, "saturation" to 1.05f, "grain" to 0.1f)),
        CountryFilter("AE", "迪拜·摩登",    shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.2f, "sharpness" to 0.7f)),
        CountryFilter("QA", "卡塔尔·金沙",  shaderParams = mapOf("colorTemp" to 0.72f, "saturation" to 1.1f, "colorBoost" to 0.15f)),
        CountryFilter("KW", "科威特·石油",  shaderParams = mapOf("colorTemp" to 0.7f, "saturation" to 1.0f, "grain" to 0.08f)),
        CountryFilter("IL", "以色列·圣地",  shaderParams = mapOf("colorTemp" to 0.6f, "saturation" to 1.1f, "sharpness" to 0.6f)),
        CountryFilter("JO", "约旦·玫瑰城",  shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.15f, "grain" to 0.1f)),
        CountryFilter("LB", "黎巴嫩·雪松",  shaderParams = mapOf("saturation" to 1.1f, "colorTemp" to 0.5f, "grain" to 0.08f)),
        CountryFilter("MA", "摩洛哥·蓝城",  shaderParams = mapOf("colorTemp" to 0.42f, "saturation" to 1.25f, "grain" to 0.1f)),
        CountryFilter("TN", "突尼斯·地中海",shaderParams = mapOf("colorTemp" to 0.45f, "saturation" to 1.2f, "clarity" to 0.15f)),
        CountryFilter("DZ", "阿尔及利亚·撒哈拉", shaderParams = mapOf("colorTemp" to 0.75f, "saturation" to 1.0f, "grain" to 0.12f)),
        CountryFilter("LY", "利比亚·沙漠",  shaderParams = mapOf("colorTemp" to 0.78f, "saturation" to 0.95f, "grain" to 0.12f)),
        CountryFilter("NG", "尼日利亚·活力",shaderParams = mapOf("saturation" to 1.5f, "colorTemp" to 0.6f, "colorBoost" to 0.3f)),
        CountryFilter("KE", "肯尼亚·草原",  shaderParams = mapOf("colorTemp" to 0.62f, "saturation" to 1.3f, "dehaze" to 0.15f)),
        CountryFilter("ET", "埃塞俄比亚·高原", shaderParams = mapOf("colorTemp" to 0.6f, "saturation" to 1.15f, "grain" to 0.1f)),
        CountryFilter("TZ", "坦桑尼亚·乞力马扎罗", shaderParams = mapOf("colorTemp" to 0.55f, "saturation" to 1.2f, "sharpness" to 0.65f)),
        CountryFilter("GH", "加纳·黄金",    shaderParams = mapOf("colorTemp" to 0.65f, "saturation" to 1.3f, "colorBoost" to 0.2f)),
        CountryFilter("SN", "塞内加尔·非洲",shaderParams = mapOf("saturation" to 1.4f, "colorTemp" to 0.65f, "grain" to 0.08f)),
        CountryFilter("CM", "喀麦隆·雨林",  shaderParams = mapOf("saturation" to 1.35f, "colorTemp" to 0.52f, "dehaze" to 0.1f)),
        CountryFilter("CL", "智利·安第斯",  shaderParams = mapOf("colorTemp" to 0.4f, "saturation" to 1.2f, "sharpness" to 0.65f)),
        CountryFilter("PE", "秘鲁·马丘比丘",shaderParams = mapOf("colorTemp" to 0.52f, "saturation" to 1.25f, "clarity" to 0.2f)),
        CountryFilter("CO", "哥伦比亚·咖啡",shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.55f, "grain" to 0.08f)),
        CountryFilter("VE", "委内瑞拉·天使瀑布", shaderParams = mapOf("saturation" to 1.35f, "colorTemp" to 0.5f, "dehaze" to 0.15f)),
        CountryFilter("EC", "厄瓜多尔·赤道",shaderParams = mapOf("saturation" to 1.4f, "colorTemp" to 0.52f, "colorBoost" to 0.2f)),
        CountryFilter("BO", "玻利维亚·天空之镜", shaderParams = mapOf("colorTemp" to 0.38f, "saturation" to 1.1f, "clarity" to 0.3f)),
        CountryFilter("PY", "巴拉圭·热带",  shaderParams = mapOf("saturation" to 1.3f, "colorTemp" to 0.6f, "grain" to 0.08f)),
        CountryFilter("UY", "乌拉圭·潘帕斯",shaderParams = mapOf("saturation" to 1.2f, "colorTemp" to 0.5f, "grain" to 0.1f)),
        CountryFilter("CU", "古巴·复古",    shaderParams = mapOf("saturation" to 1.2f, "grain" to 0.2f, "vignette" to 0.3f)),
        CountryFilter("JM", "牙买加·雷鬼",  shaderParams = mapOf("saturation" to 1.5f, "colorTemp" to 0.6f, "colorBoost" to 0.3f)),
        CountryFilter("DO", "多米尼加·加勒比", shaderParams = mapOf("colorTemp" to 0.42f, "saturation" to 1.4f, "clarity" to 0.15f))
    )
}
