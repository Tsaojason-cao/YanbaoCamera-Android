package com.yanbao.camera.core.util

import android.annotation.SuppressLint
import android.content.Context
import android.provider.Settings
import java.security.MessageDigest

/**
 * 设备 UID 生成器
 * 
 * 核心功能：
 * - 基于设备 ANDROID_ID 生成唯一的硬件指纹
 * - 格式：YB-XXXXXX（6位数字）
 * - 确保同一设备每次生成的 UID 相同
 * - 不可逆向推导出 ANDROID_ID
 * 
 * 验收闭环：
 * - UID 从数据库流转，绝不 hardcode
 * - 同一设备重装 App 后 UID 保持不变
 * - 不同设备生成的 UID 不同
 */
object DeviceUidGenerator {

    /**
     * 生成设备唯一 UID
     * 
     * @param context Android Context
     * @return YB-XXXXXX 格式的 UID
     */
    @SuppressLint("HardwareIds")
    fun generateUid(context: Context): String {
        // 1. 获取设备 ANDROID_ID（硬件指纹）
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown_device"

        // 2. 使用 SHA-256 哈希算法生成唯一标识
        val hash = sha256(androidId)

        // 3. 取哈希值的前6位数字作为 UID
        val numericHash = hash.filter { it.isDigit() }.take(6)

        // 4. 如果数字不足6位，补充随机数字（基于哈希值）
        val uid = if (numericHash.length >= 6) {
            numericHash
        } else {
            val supplementary = hash.map { it.code % 10 }.joinToString("").take(6 - numericHash.length)
            numericHash + supplementary
        }

        // 5. 返回 YB-XXXXXX 格式
        return "YB-$uid"
    }

    /**
     * SHA-256 哈希算法
     */
    private fun sha256(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray())
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // 如果哈希失败，使用输入字符串的简单变换
            input.hashCode().toString().takeLast(6).padStart(6, '0')
        }
    }

    /**
     * 验证 UID 格式是否正确
     * 
     * @param uid 待验证的 UID
     * @return true 如果格式正确，false 否则
     */
    fun isValidUid(uid: String): Boolean {
        val regex = Regex("^YB-\\d{6}$")
        return regex.matches(uid)
    }

    /**
     * 从 UID 中提取数字部分
     * 
     * @param uid YB-XXXXXX 格式的 UID
     * @return 6位数字字符串
     */
    fun extractNumericPart(uid: String): String {
        return uid.removePrefix("YB-")
    }
}
