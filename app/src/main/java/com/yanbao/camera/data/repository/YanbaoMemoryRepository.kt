package com.yanbao.camera.data.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.media.ExifInterface
import android.net.Uri
import android.util.Log
import com.yanbao.camera.data.local.dao.YanbaoMemoryDao
import com.yanbao.camera.data.local.entity.YanbaoMemory
import com.yanbao.camera.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 雁宝记忆 Repository（满血版 v2.0）
 *
 * 核心职责：
 * 1. [saveMemoryPackage]   拍照后将完整参数包序列化为 JSON，存入 Room 数据库
 * 2. [extractFromPhoto]    从相册旧照片的 Metadata 中反序列化参数包
 * 3. [applyMemoryToCamera] 将参数包 1:1 恢复到取景器（通过 Flow 通知 ViewModel）
 * 4. [getAllMemories]       获取所有记忆列表（用于相册展示）
 *
 * 这是"雁宝记忆"功能的数据闭环核心。
 */
@Singleton
class YanbaoMemoryRepository @Inject constructor(
    private val context: Context,
    private val memoryDao: YanbaoMemoryDao,
    private val gitBackupManager: GitBackupManager
) {

    private val TAG = "YanbaoMemoryRepo"

    // ─────────────────────────────────────────────────────────────────────────
    // 1. 保存记忆包（拍照后调用）
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 拍照完成后，将当前所有模式参数序列化为 JSON 并存入数据库。
     * 同时将 JSON 写入照片文件的 EXIF UserComment 字段，实现"参数随图片走"。
     *
     * @param imagePath  刚拍摄的照片路径
     * @param pkg        当前取景器的完整参数包
     */
    suspend fun saveMemoryPackage(imagePath: String, pkg: YanbaoMemoryPackage): Result<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val packageWithPath = pkg.copy(
                    imagePath = imagePath,
                    packageId = generatePackageId(),
                    timestamp = System.currentTimeMillis()
                )
                val json = packageWithPath.toJson()

                // 1. 写入 EXIF UserComment（让参数随图片文件走）
                writeJsonToExif(imagePath, json)

                // 2. 存入 Room 数据库
                val entity = YanbaoMemory(
                    imagePath = imagePath,
                    latitude = pkg.lbs.latitude,
                    longitude = pkg.lbs.longitude,
                    locationName = pkg.lbs.locationName,
                    weatherType = pkg.lbs.weatherType,
                    shootingMode = pkg.shootingMode,
                    parameterSnapshotJson = json,
                    memberNumber = getMemberNumber(),
                    timestamp = packageWithPath.timestamp
                )
                val rowId = memoryDao.insert(entity)

                // 3. 触发 Git 备份（非阻塞）
                try {
                    gitBackupManager.performFullBackup()
                    Log.d(TAG, "✅ Git 备份触发成功 [rowId=$rowId]")
                } catch (e: Exception) {
                    Log.w(TAG, "⚠️ Git 备份失败（不影响主流程）: ${e.message}")
                }

                Log.d(TAG, "✅ 记忆包保存成功 [rowId=$rowId, mode=${pkg.shootingMode}]")
                Result.success(rowId)
            } catch (e: Exception) {
                Log.e(TAG, "❌ 记忆包保存失败: ${e.message}", e)
                Result.failure(e)
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. 从旧照片提取参数包（雁宝记忆模式的核心）
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 从相册中选择的旧照片提取完整参数包。
     *
     * 提取策略（优先级从高到低）：
     * 1. 优先从 EXIF UserComment 中读取雁宝专属 JSON（最完整）
     * 2. 其次从 Room 数据库中按 imagePath 查询（本机拍摄）
     * 3. 最后从标准 EXIF 字段提取基础参数（ISO/快门/WB 等）
     *
     * @param imageUri  相册中的照片 URI
     * @return 提取到的参数包，如果完全无法提取则返回默认包
     */
    suspend fun extractFromPhoto(imageUri: Uri): ExtractionResult {
        return withContext(Dispatchers.IO) {
            try {
                val imagePath = getPathFromUri(imageUri)

                // 策略 1: 从 EXIF UserComment 读取雁宝 JSON
                val jsonFromExif = readJsonFromExif(imagePath)
                if (jsonFromExif != null) {
                    val pkg = YanbaoMemoryPackage.fromJson(jsonFromExif)
                    if (pkg != null) {
                        Log.d(TAG, "✅ 策略1成功：从 EXIF 提取完整参数包 [filterId=${pkg.masterFilter.filterId}]")
                        return@withContext ExtractionResult.Success(pkg, ExtractionSource.EXIF_YANBAO_JSON)
                    }
                }

                // 策略 2: 从 Room 数据库查询
                if (imagePath != null) {
                    val entity = memoryDao.findByImagePath(imagePath)
                    if (entity != null) {
                        val pkg = YanbaoMemoryPackage.fromJson(entity.parameterSnapshotJson)
                        if (pkg != null) {
                            Log.d(TAG, "✅ 策略2成功：从数据库提取参数包")
                            return@withContext ExtractionResult.Success(pkg, ExtractionSource.DATABASE)
                        }
                    }
                }

                // 策略 3: 从标准 EXIF 提取基础参数
                val basicPkg = extractFromStandardExif(imagePath)
                if (basicPkg != null) {
                    Log.d(TAG, "⚠️ 策略3：仅提取到基础 EXIF 参数（ISO/快门/WB）")
                    return@withContext ExtractionResult.Partial(basicPkg, ExtractionSource.STANDARD_EXIF,
                        "仅恢复了基础相机参数（ISO/快门/白平衡），滤镜和美颜参数无法恢复")
                }

                Log.w(TAG, "⚠️ 无法从照片提取任何参数，返回默认包")
                ExtractionResult.Empty("该照片没有可恢复的拍摄参数")

            } catch (e: Exception) {
                Log.e(TAG, "❌ 参数提取失败: ${e.message}", e)
                ExtractionResult.Error(e.message ?: "未知错误")
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. 数据库查询
    // ─────────────────────────────────────────────────────────────────────────

    fun getAllMemories(): Flow<List<YanbaoMemory>> = memoryDao.getAllMemories()

    fun getMemoriesByMode(mode: String): Flow<List<YanbaoMemory>> =
        memoryDao.getAllMemories().map { list -> list.filter { it.shootingMode == mode } }

    suspend fun getMemoryByImagePath(path: String): YanbaoMemory? =
        withContext(Dispatchers.IO) { memoryDao.findByImagePath(path) }

    suspend fun deleteMemory(id: Long) = withContext(Dispatchers.IO) { memoryDao.deleteById(id) }

    suspend fun getMemoryCount(): Int = withContext(Dispatchers.IO) { memoryDao.getCount() }

    // ─────────────────────────────────────────────────────────────────────────
    // 私有辅助方法
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * 将雁宝参数 JSON 写入照片的 EXIF UserComment 字段
     * 格式: "YANBAO_MEMORY_V2::{json}"
     */
    private fun writeJsonToExif(imagePath: String?, json: String) {
        if (imagePath == null) return
        try {
            val file = File(imagePath)
            if (!file.exists() || !file.canWrite()) return
            val exif = ExifInterface(imagePath)
            exif.setAttribute(
                ExifInterface.TAG_USER_COMMENT,
                "YANBAO_MEMORY_V2::$json"
            )
            exif.saveAttributes()
            Log.d(TAG, "✅ EXIF 写入成功 [size=${json.length} chars]")
        } catch (e: Exception) {
            Log.w(TAG, "⚠️ EXIF 写入失败: ${e.message}")
        }
    }

    /**
     * 从照片 EXIF UserComment 读取雁宝参数 JSON
     */
    private fun readJsonFromExif(imagePath: String?): String? {
        if (imagePath == null) return null
        return try {
            val exif = ExifInterface(imagePath)
            val comment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT) ?: return null
            if (comment.startsWith("YANBAO_MEMORY_V2::")) {
                comment.removePrefix("YANBAO_MEMORY_V2::")
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 从标准 EXIF 字段提取基础相机参数（ISO/快门/WB/EV）
     */
    private fun extractFromStandardExif(imagePath: String?): YanbaoMemoryPackage? {
        if (imagePath == null) return null
        return try {
            val exif = ExifInterface(imagePath)

            val iso = exif.getAttributeInt(ExifInterface.TAG_ISO_SPEED_RATINGS, -1)
            val shutterStr = exif.getAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE)
            val evStr = exif.getAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE)
            val ev = evStr?.toFloatOrNull() ?: 0f

            // 解析快门速度（APEX 值转换为纳秒）
            val shutterNanos = parseShutterApexToNanos(shutterStr)

            // 从 GPS 提取位置
            val lat = exif.getAttributeDouble(ExifInterface.TAG_GPS_LATITUDE, 0.0)
            val lon = exif.getAttributeDouble(ExifInterface.TAG_GPS_LONGITUDE, 0.0)

            YanbaoMemoryPackage(
                shootingMode = "MANUAL",
                manual = ManualCameraParams(
                    iso = iso,
                    shutterSpeedNanos = shutterNanos,
                    exposureCompensation = ev
                ),
                lbs = LbsInfo(latitude = lat, longitude = lon)
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 将 EXIF APEX 快门速度值转换为纳秒
     * APEX 公式: shutterNanos = 1 / (2^apexValue) 秒
     */
    private fun parseShutterApexToNanos(apexStr: String?): Long {
        if (apexStr == null) return -1L
        return try {
            // EXIF 快门速度可能是 "1/250" 或 APEX 值
            if (apexStr.contains("/")) {
                val parts = apexStr.split("/")
                val num = parts[0].toDouble()
                val den = parts[1].toDouble()
                ((num / den) * 1_000_000_000L).toLong()
            } else {
                val apex = apexStr.toDouble()
                val seconds = 1.0 / Math.pow(2.0, apex)
                (seconds * 1_000_000_000L).toLong()
            }
        } catch (e: Exception) {
            -1L
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        return try {
            when (uri.scheme) {
                "file" -> uri.path
                "content" -> {
                    context.contentResolver.query(uri, arrayOf("_data"), null, null, null)?.use { cursor ->
                        if (cursor.moveToFirst()) cursor.getString(0) else null
                    }
                }
                else -> null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun generatePackageId(): String =
        "YB_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}_${(1000..9999).random()}"

    private fun getMemberNumber(): String =
        context.getSharedPreferences("yanbao_prefs", Context.MODE_PRIVATE)
            .getString("member_number", "YB-88888") ?: "YB-88888"
}

// ─────────────────────────────────────────────────────────────────────────────
// 提取结果密封类
// ─────────────────────────────────────────────────────────────────────────────

sealed class ExtractionResult {
    data class Success(val pkg: YanbaoMemoryPackage, val source: ExtractionSource) : ExtractionResult()
    data class Partial(val pkg: YanbaoMemoryPackage, val source: ExtractionSource, val message: String) : ExtractionResult()
    data class Empty(val message: String) : ExtractionResult()
    data class Error(val message: String) : ExtractionResult()
}

enum class ExtractionSource {
    EXIF_YANBAO_JSON,   // 从雁宝专属 EXIF JSON 提取（最完整）
    DATABASE,           // 从本机 Room 数据库提取
    STANDARD_EXIF,      // 从标准 EXIF 字段提取（仅基础参数）
}
