package com.yanbao.camera.core.engine

/**
 * 大師機位参数（用於推薦模塊）
 */
data class MasterLocationParams(
    val id: String,             // 地點 ID
    val name: String,           // 参数名稱（如 Master_101）
    val displayName: String,    // 显示名稱（如 "101 專屬預設"）
    val kelvin: Int,            // 色溫（K）
    val exposure: Float,        // 曝光補償（EV）
    val contrast: Float,        // 對比度
    val iso: Int,               // ISO 感光度
    val shutterSpeed: String    // 快門速度（如 "1/60"）
)

/**
 * 大師機位参数存儲
 */
object MasterLocationParamsStore {
    
    val MASTER_LOCATIONS = listOf(
        MasterLocationParams(
            id = "1",
            name = "Master_101",
            displayName = "101 專屬預設",
            kelvin = 3200,
            exposure = 0.5f,
            contrast = 20f,
            iso = 800,
            shutterSpeed = "1/60"
        ),
        MasterLocationParams(
            id = "2",
            name = "Master_Jiufen",
            displayName = "九份紅燈籠",
            kelvin = 2800,
            exposure = 0.2f,
            contrast = 30f,
            iso = 1200,
            shutterSpeed = "1/60"
        ),
        MasterLocationParams(
            id = "3",
            name = "Master_SunMoonLake",
            displayName = "日月潭晨霧",
            kelvin = 5500,
            exposure = 0.0f,
            contrast = 10f,
            iso = 200,
            shutterSpeed = "1/250"
        ),
        MasterLocationParams(
            id = "4",
            name = "Master_Taroko",
            displayName = "太魯閣峽谷",
            kelvin = 5800,
            exposure = 0.3f,
            contrast = 15f,
            iso = 400,
            shutterSpeed = "1/125"
        )
    )
    
    /**
     * 根據地點 ID 獲取大師模式参数
     */
    fun getMasterParamsByLocationId(locationId: String): MasterLocationParams? {
        return MASTER_LOCATIONS.find { it.id == locationId }
    }
}
