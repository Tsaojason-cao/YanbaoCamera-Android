package com.yanbao.camera.presentation.recommend

/**
 * 相機參數數據類
 * 
 * 用於從推薦模塊傳遞大師模式參數到相機模塊
 */
data class CameraParams(
    // 地點信息
    val locationId: String,
    val locationName: String,
    
    // 大師模式參數 ID
    val masterParamsId: String,
    
    // 相機參數
    val kelvin: Int,           // 色溫
    val exposure: Float,       // 曝光
    val contrast: Float,       // 對比度
    val iso: Int,              // ISO
    val shutterSpeed: String,  // 快門速度（例如 "1/1000"）
    
    // 是否鎖定參數
    val isLocked: Boolean = false
)
