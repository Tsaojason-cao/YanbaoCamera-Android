package com.yanbao.camera.ar

import android.content.Context
import android.util.Log
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Frame
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.CameraNotAvailableException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Phase 2: AR 空间会话管理器
 *
 * 封装 ARCore Session 生命周期，提供：
 * - ARCore 可用性检测
 * - Session 创建/恢复/暂停
 * - 平面检测状态
 * - 帧更新回调
 *
 * 使用方式：
 * ```kotlin
 * val arManager = ArSessionManager(context)
 * if (arManager.isArCoreAvailable()) {
 *     arManager.createSession()
 *     arManager.resume()
 * }
 * // 在渲染循环中
 * val frame = arManager.update()
 * ```
 */
class ArSessionManager(private val context: Context) {

    companion object {
        private const val TAG = "ArSessionManager"
    }

    // ─── 状态 ─────────────────────────────────────────────────────────────

    enum class ArState {
        UNAVAILABLE,    // 设备不支持 ARCore
        NOT_INSTALLED,  // ARCore 未安装
        IDLE,           // 已初始化，未启动
        RUNNING,        // 正在运行
        PAUSED,         // 已暂停
        ERROR           // 错误状态
    }

    private val _arState = MutableStateFlow(ArState.IDLE)
    val arState: StateFlow<ArState> = _arState.asStateFlow()

    private val _detectedPlanes = MutableStateFlow<List<PlaneInfo>>(emptyList())
    val detectedPlanes: StateFlow<List<PlaneInfo>> = _detectedPlanes.asStateFlow()

    private val _trackingState = MutableStateFlow(TrackingState.STOPPED)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    private var session: Session? = null

    // ─── 公开 API ─────────────────────────────────────────────────────────

    /**
     * 检查 ARCore 是否可用
     */
    fun isArCoreAvailable(): Boolean {
        return try {
            val availability = ArCoreApk.getInstance().checkAvailability(context)
            val available = availability.isSupported
            if (!available) {
                _arState.value = ArState.UNAVAILABLE
                Log.w(TAG, "ARCore not supported: $availability")
            }
            available
        } catch (e: Exception) {
            Log.e(TAG, "ARCore availability check failed: ${e.message}")
            _arState.value = ArState.UNAVAILABLE
            false
        }
    }

    /**
     * 创建 ARCore Session
     * @return 是否成功
     */
    fun createSession(): Boolean {
        if (session != null) return true

        return try {
            session = Session(context).apply {
                configure(
                    Config(this).apply {
                        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                        lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                        depthMode = if (isDepthModeSupported(Config.DepthMode.AUTOMATIC)) {
                            Config.DepthMode.AUTOMATIC
                        } else {
                            Config.DepthMode.DISABLED
                        }
                        updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
                    }
                )
            }
            _arState.value = ArState.IDLE
            Log.d(TAG, "ARCore session created")
            true
        } catch (e: UnavailableArcoreNotInstalledException) {
            Log.e(TAG, "ARCore not installed")
            _arState.value = ArState.NOT_INSTALLED
            false
        } catch (e: UnavailableApkTooOldException) {
            Log.e(TAG, "ARCore APK too old")
            _arState.value = ArState.ERROR
            false
        } catch (e: UnavailableSdkTooOldException) {
            Log.e(TAG, "ARCore SDK too old")
            _arState.value = ArState.ERROR
            false
        } catch (e: UnavailableDeviceNotCompatibleException) {
            Log.e(TAG, "Device not compatible with ARCore")
            _arState.value = ArState.UNAVAILABLE
            false
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create ARCore session: ${e.message}")
            _arState.value = ArState.ERROR
            false
        }
    }

    /**
     * 恢复 Session（在 Activity.onResume 中调用）
     */
    fun resume() {
        try {
            session?.resume()
            _arState.value = ArState.RUNNING
            Log.d(TAG, "ARCore session resumed")
        } catch (e: CameraNotAvailableException) {
            Log.e(TAG, "Camera not available for ARCore: ${e.message}")
            _arState.value = ArState.ERROR
        }
    }

    /**
     * 暂停 Session（在 Activity.onPause 中调用）
     */
    fun pause() {
        session?.pause()
        _arState.value = ArState.PAUSED
        Log.d(TAG, "ARCore session paused")
    }

    /**
     * 更新帧（在渲染循环中每帧调用）
     * @return 当前 ARCore Frame，失败返回 null
     */
    fun update(): Frame? {
        val sess = session ?: return null
        return try {
            val frame = sess.update()
            _trackingState.value = frame.camera.trackingState

            // 更新检测到的平面
            val planes = sess.getAllTrackables(Plane::class.java)
                .filter { it.trackingState == TrackingState.TRACKING }
                .map { plane ->
                    PlaneInfo(
                        type = when (plane.type) {
                            Plane.Type.HORIZONTAL_UPWARD_FACING -> PlaneType.FLOOR
                            Plane.Type.HORIZONTAL_DOWNWARD_FACING -> PlaneType.CEILING
                            Plane.Type.VERTICAL -> PlaneType.WALL
                            else -> PlaneType.UNKNOWN
                        },
                        centerX = plane.centerPose.tx(),
                        centerY = plane.centerPose.ty(),
                        centerZ = plane.centerPose.tz(),
                        extentX = plane.extentX,
                        extentZ = plane.extentZ
                    )
                }
            _detectedPlanes.value = planes

            frame
        } catch (e: Exception) {
            Log.w(TAG, "ARCore update failed: ${e.message}")
            null
        }
    }

    /**
     * 释放 Session 资源（在 Activity.onDestroy 中调用）
     */
    fun release() {
        session?.close()
        session = null
        _arState.value = ArState.IDLE
        Log.d(TAG, "ARCore session released")
    }

    /**
     * 获取底层 Session（供 ArRenderer 使用）
     */
    fun getSession(): Session? = session
}

// ─── 数据类 ───────────────────────────────────────────────────────────────

enum class PlaneType { FLOOR, CEILING, WALL, UNKNOWN }

data class PlaneInfo(
    val type: PlaneType,
    val centerX: Float,
    val centerY: Float,
    val centerZ: Float,
    val extentX: Float,
    val extentZ: Float
)
