package com.yanbao.camera.core.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * 相机设置持久化管理器
 *
 * 使用 SharedPreferences 保存用户的相机偏好设置，
 * 确保 App 重启后恢复上次的拍摄状态，包括：
 *  - 当前拍摄模式
 *  - 闪光灯模式
 *  - 画幅比例
 *  - 帧率设置
 *  - 2.9D 视差强度
 *  - 镜头方向
 */
class CameraPreferencesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "yanbao_camera_prefs"

        // Keys
        const val KEY_LAST_MODE = "last_camera_mode"
        const val KEY_FLASH_MODE = "flash_mode"
        const val KEY_ASPECT_RATIO = "aspect_ratio"
        const val KEY_LENS_FACING = "lens_facing"
        const val KEY_TIMER = "timer"
        const val KEY_SELECTED_FPS = "selected_fps"
        const val KEY_PARALLAX_STRENGTH = "parallax_strength"
        const val KEY_PARALLAX_PRESET = "parallax_preset"
        const val KEY_TIMELAPSE_INTERVAL = "timelapse_interval"
        const val KEY_TOTAL_DURATION = "total_duration"
        const val KEY_AR_CATEGORY = "ar_category"
        const val KEY_NATIVE_ISO = "native_iso"
        const val KEY_NATIVE_EV = "native_ev"
        const val KEY_NATIVE_WB = "native_wb"

        // Defaults
        const val DEFAULT_MODE = "BASIC"
        const val DEFAULT_FLASH = 0
        const val DEFAULT_ASPECT = 0
        const val DEFAULT_LENS = 0
        const val DEFAULT_TIMER = 0
        const val DEFAULT_FPS = 60
        const val DEFAULT_PARALLAX_STRENGTH = 0.65f
        const val DEFAULT_PARALLAX_PRESET = 0
        const val DEFAULT_TIMELAPSE = 2.0f
        const val DEFAULT_DURATION = 5.0f
        const val DEFAULT_AR_CATEGORY = 0
        const val DEFAULT_ISO = 400
        const val DEFAULT_EV = 0.3f
        const val DEFAULT_WB = 5500
    }

    // ─── 读取 ──────────────────────────────────────────────────────────────

    fun getLastMode(): String = prefs.getString(KEY_LAST_MODE, DEFAULT_MODE) ?: DEFAULT_MODE
    fun getFlashMode(): Int = prefs.getInt(KEY_FLASH_MODE, DEFAULT_FLASH)
    fun getAspectRatio(): Int = prefs.getInt(KEY_ASPECT_RATIO, DEFAULT_ASPECT)
    fun getLensFacing(): Int = prefs.getInt(KEY_LENS_FACING, DEFAULT_LENS)
    fun getTimer(): Int = prefs.getInt(KEY_TIMER, DEFAULT_TIMER)
    fun getSelectedFps(): Int = prefs.getInt(KEY_SELECTED_FPS, DEFAULT_FPS)
    fun getParallaxStrength(): Float = prefs.getFloat(KEY_PARALLAX_STRENGTH, DEFAULT_PARALLAX_STRENGTH)
    fun getParallaxPreset(): Int = prefs.getInt(KEY_PARALLAX_PRESET, DEFAULT_PARALLAX_PRESET)
    fun getTimelapseInterval(): Float = prefs.getFloat(KEY_TIMELAPSE_INTERVAL, DEFAULT_TIMELAPSE)
    fun getTotalDuration(): Float = prefs.getFloat(KEY_TOTAL_DURATION, DEFAULT_DURATION)
    fun getArCategory(): Int = prefs.getInt(KEY_AR_CATEGORY, DEFAULT_AR_CATEGORY)
    fun getNativeIso(): Int = prefs.getInt(KEY_NATIVE_ISO, DEFAULT_ISO)
    fun getNativeEv(): Float = prefs.getFloat(KEY_NATIVE_EV, DEFAULT_EV)
    fun getNativeWb(): Int = prefs.getInt(KEY_NATIVE_WB, DEFAULT_WB)

    // ─── 写入 ──────────────────────────────────────────────────────────────

    fun saveLastMode(mode: String) = prefs.edit { putString(KEY_LAST_MODE, mode) }
    fun saveFlashMode(mode: Int) = prefs.edit { putInt(KEY_FLASH_MODE, mode) }
    fun saveAspectRatio(ratio: Int) = prefs.edit { putInt(KEY_ASPECT_RATIO, ratio) }
    fun saveLensFacing(facing: Int) = prefs.edit { putInt(KEY_LENS_FACING, facing) }
    fun saveTimer(timer: Int) = prefs.edit { putInt(KEY_TIMER, timer) }
    fun saveSelectedFps(fps: Int) = prefs.edit { putInt(KEY_SELECTED_FPS, fps) }
    fun saveParallaxStrength(strength: Float) = prefs.edit { putFloat(KEY_PARALLAX_STRENGTH, strength) }
    fun saveParallaxPreset(preset: Int) = prefs.edit { putInt(KEY_PARALLAX_PRESET, preset) }
    fun saveTimelapseInterval(interval: Float) = prefs.edit { putFloat(KEY_TIMELAPSE_INTERVAL, interval) }
    fun saveTotalDuration(duration: Float) = prefs.edit { putFloat(KEY_TOTAL_DURATION, duration) }
    fun saveArCategory(category: Int) = prefs.edit { putInt(KEY_AR_CATEGORY, category) }
    fun saveNativeIso(iso: Int) = prefs.edit { putInt(KEY_NATIVE_ISO, iso) }
    fun saveNativeEv(ev: Float) = prefs.edit { putFloat(KEY_NATIVE_EV, ev) }
    fun saveNativeWb(wb: Int) = prefs.edit { putInt(KEY_NATIVE_WB, wb) }

    /** 一次性保存所有核心相机状态（用于 onCleared） */
    fun saveAllCameraState(
        mode: String,
        flashMode: Int,
        aspectRatio: Int,
        lensFacing: Int,
        timer: Int,
        fps: Int,
        parallaxStrength: Float,
        parallaxPreset: Int,
        timelapseInterval: Float,
        totalDuration: Float,
        arCategory: Int,
        nativeIso: Int,
        nativeEv: Float,
        nativeWb: Int
    ) {
        prefs.edit {
            putString(KEY_LAST_MODE, mode)
            putInt(KEY_FLASH_MODE, flashMode)
            putInt(KEY_ASPECT_RATIO, aspectRatio)
            putInt(KEY_LENS_FACING, lensFacing)
            putInt(KEY_TIMER, timer)
            putInt(KEY_SELECTED_FPS, fps)
            putFloat(KEY_PARALLAX_STRENGTH, parallaxStrength)
            putInt(KEY_PARALLAX_PRESET, parallaxPreset)
            putFloat(KEY_TIMELAPSE_INTERVAL, timelapseInterval)
            putFloat(KEY_TOTAL_DURATION, totalDuration)
            putInt(KEY_AR_CATEGORY, arCategory)
            putInt(KEY_NATIVE_ISO, nativeIso)
            putFloat(KEY_NATIVE_EV, nativeEv)
            putInt(KEY_NATIVE_WB, nativeWb)
        }
    }
}
