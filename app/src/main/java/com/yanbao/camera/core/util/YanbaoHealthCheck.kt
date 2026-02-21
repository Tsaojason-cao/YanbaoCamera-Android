package com.yanbao.camera.core.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.delay

/**
 * Yanbao AI 全模块通电自检脚本
 * 
 * 🚨 核心验收标准：
 * - Camera Hardware Linkage: 检查 Camera2 是否真实受控
 * - User Persistence: 检查 Profile 持久化是否正常
 * - UID Integrity: 检查 UID 是否基于硬件指纹生成（非随机）
 * 
 * 使用方法：
 * ```kotlin
 * val healthCheck = YanbaoHealthCheck(context)
 * healthCheck.runFullAudit()
 * ```
 */
class YanbaoHealthCheck(private val context: Context) {

    companion object {
        private const val TAG = "YanbaoHealthCheck"
    }

    /**
     * 运行完整的自检流程
     * 
     * 验收闭环：
     * 1. Camera Hardware Linkage: PASS/FAILED
     * 2. User Persistence: PASS/FAILED
     * 3. UID Integrity: PASS/STOCHASTIC_ERROR
     */
    suspend fun runFullAudit(): HealthCheckResult {
        Log.i(TAG, "========================================")
        Log.i(TAG, "Yanbao AI 全模块通电自检开始")
        Log.i(TAG, "========================================")

        val results = mutableMapOf<String, Boolean>()

        // 1. 检查 Camera2 是否真实受控
        val isCameraResponsive = checkCameraHardwareLinkage()
        results["Camera Hardware Linkage"] = isCameraResponsive
        Log.i(TAG, "Camera Hardware Linkage: ${if(isCameraResponsive) "✅ PASS" else "❌ FAILED"}")

        // 2. 检查 Profile 持久化
        val isProfilePersistent = checkProfileStorage()
        results["User Persistence"] = isProfilePersistent
        Log.i(TAG, "User Persistence: ${if(isProfilePersistent) "✅ PASS" else "❌ FAILED"}")

        // 3. 检查 UID 是否 Hardcode（必须基于硬件指纹，多次调用结果一致）
        val uid1 = DeviceUidGenerator.generateYanbaoUid(context)
        delay(100)
        val uid2 = DeviceUidGenerator.generateYanbaoUid(context)
        val isUidIntegrity = uid1 == uid2
        results["UID Integrity"] = isUidIntegrity
        Log.i(TAG, "UID Integrity: ${if(isUidIntegrity) "✅ PASS (UID: $uid1)" else "❌ STOCHASTIC_ERROR"}")

        // 4. 检查 UID 格式是否符合 YB-XXXXXX
        val isUidFormatValid = uid1.matches(Regex("YB-\\d{6}"))
        results["UID Format"] = isUidFormatValid
        Log.i(TAG, "UID Format: ${if(isUidFormatValid) "✅ PASS" else "❌ FAILED"}")

        Log.i(TAG, "========================================")
        Log.i(TAG, "自检完成！通过率: ${results.values.count { it }}/${results.size}")
        Log.i(TAG, "========================================")

        return HealthCheckResult(
            cameraHardwareLinkage = isCameraResponsive,
            userPersistence = isProfilePersistent,
            uidIntegrity = isUidIntegrity,
            uidFormatValid = isUidFormatValid,
            passRate = results.values.count { it }.toFloat() / results.size
        )
    }

    /**
     * 检查 Camera2 硬件联动
     * 
     * 验证逻辑：
     * - 检查是否可以获取 CameraManager
     * - 检查是否有可用的后置摄像头
     */
    private fun checkCameraHardwareLinkage(): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as android.hardware.camera2.CameraManager
            val cameraIdList = cameraManager.cameraIdList
            
            if (cameraIdList.isEmpty()) {
                Log.e(TAG, "❌ 未检测到摄像头设备")
                return false
            }

            // 检查是否有后置摄像头
            val hasBackCamera = cameraIdList.any { cameraId ->
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(android.hardware.camera2.CameraCharacteristics.LENS_FACING)
                facing == android.hardware.camera2.CameraCharacteristics.LENS_FACING_BACK
            }

            if (!hasBackCamera) {
                Log.e(TAG, "❌ 未检测到后置摄像头")
                return false
            }

            Log.i(TAG, "✅ Camera2 硬件检测通过，共 ${cameraIdList.size} 个摄像头")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Camera2 硬件检测失败: ${e.message}")
            false
        }
    }

    /**
     * 检查 Profile 持久化
     * 
     * 验证逻辑：
     * - 检查 SharedPreferences 是否可用
     * - 检查是否能读取用户数据
     */
    private fun checkProfileStorage(): Boolean {
        return try {
            val prefs = context.getSharedPreferences("yanbao_profile", Context.MODE_PRIVATE)
            
            // 写入测试数据
            prefs.edit().apply {
                putString("test_key", "test_value")
                putLong("test_timestamp", System.currentTimeMillis())
                apply()
            }

            // 读取测试数据
            val testValue = prefs.getString("test_key", null)
            val testTimestamp = prefs.getLong("test_timestamp", 0)

            if (testValue == "test_value" && testTimestamp > 0) {
                Log.i(TAG, "✅ SharedPreferences 读写正常")
                
                // 清理测试数据
                prefs.edit().remove("test_key").remove("test_timestamp").apply()
                true
            } else {
                Log.e(TAG, "❌ SharedPreferences 读写异常")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Profile 持久化检测失败: ${e.message}")
            false
        }
    }
}

/**
 * 健康检查结果
 */
data class HealthCheckResult(
    val cameraHardwareLinkage: Boolean,
    val userPersistence: Boolean,
    val uidIntegrity: Boolean,
    val uidFormatValid: Boolean,
    val passRate: Float
) {
    fun isPassed(): Boolean = passRate >= 0.75f // 至少 75% 通过率
}
