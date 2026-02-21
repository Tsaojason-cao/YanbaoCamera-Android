package com.yanbao.camera.core.config

import android.content.Context
import android.util.Log
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException

/**
 * yanbao AI 全局主题配置
 * 从 assets/config/theme_config.json 读取
 */
@Serializable
data class ThemeConfig(
    val app_name: String,
    val version: String,
    val ui_logic: UILogic,
    val theme_palette: ThemePalette,
    val camera_29d_engine: Camera29DEngine,
    val git_sync_protocol: GitSyncProtocol,
    val lbs_module: LBSModule
) {
    @Serializable
    data class UILogic(
        val viewport_ratio: Float,
        val control_panel_ratio: Float,
        val global_corner_radius: Int,
        val haptic_feedback: Boolean
    )

    @Serializable
    data class ThemePalette(
        val gradient_start: String,
        val gradient_end: String,
        val overlay_opacity: Float,
        val glass_blur_sigma: Float
    )

    @Serializable
    data class Camera29DEngine(
        val realtime_bubble_enabled: Boolean,
        val bubble_offset_y: Int,
        val rgb_curve_interactive: RGBCurveInteractive
    ) {
        @Serializable
        data class RGBCurveInteractive(
            val node_size: Int,
            val node_glow_color: String,
            val realtime_shader_sync: Boolean
        )
    }

    @Serializable
    data class GitSyncProtocol(
        val auto_commit_on_capture: Boolean,
        val metadata_format: String,
        val branch_default: String,
        val remote_backup_enabled: Boolean
    )

    @Serializable
    data class LBSModule(
        val precision: String,
        val master_filter_mapping: String,
        val auto_refresh_radius_km: Int
    )

    companion object {
        private const val TAG = "ThemeConfig"
        private var instance: ThemeConfig? = null

        fun load(context: Context): ThemeConfig {
            if (instance != null) return instance!!

            return try {
                val jsonString = context.assets.open("config/theme_config.json")
                    .bufferedReader()
                    .use { it.readText() }

                val config = Json { ignoreUnknownKeys = true }.decodeFromString<ThemeConfig>(jsonString)
                instance = config
                Log.d(TAG, "✅ Theme config loaded successfully: ${config.version}")
                config
            } catch (e: IOException) {
                Log.e(TAG, "❌ Failed to load theme config", e)
                // 返回默认配置
                getDefaultConfig()
            }
        }

        private fun getDefaultConfig() = ThemeConfig(
            app_name = "yanbao AI",
            version = "1.0.0_Industrial",
            ui_logic = UILogic(
                viewport_ratio = 0.72f,
                control_panel_ratio = 0.28f,
                global_corner_radius = 24,
                haptic_feedback = true
            ),
            theme_palette = ThemePalette(
                gradient_start = "#A78BFA",
                gradient_end = "#EC4899",
                overlay_opacity = 0.85f,
                glass_blur_sigma = 40.0f
            ),
            camera_29d_engine = Camera29DEngine(
                realtime_bubble_enabled = true,
                bubble_offset_y = -12,
                rgb_curve_interactive = Camera29DEngine.RGBCurveInteractive(
                    node_size = 32,
                    node_glow_color = "#EC489977",
                    realtime_shader_sync = true
                )
            ),
            git_sync_protocol = GitSyncProtocol(
                auto_commit_on_capture = true,
                metadata_format = "JSON_V2",
                branch_default = "master",
                remote_backup_enabled = true
            ),
            lbs_module = LBSModule(
                precision = "high",
                master_filter_mapping = "91_countries_matrix",
                auto_refresh_radius_km = 5
            )
        )
    }
}
