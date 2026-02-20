package com.yanbao.camera.core.util

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Git 备份管理器
 * 
 * 功能：
 * 1. 初始化 Git 仓库
 * 2. 备份 SharedPreferences 配置文件
 * 3. 备份雁宝记忆数据库
 * 4. 提交到本地 Git 仓库
 * 
 * 审计要点：
 * - 备份成功后必须有 .git 目录
 * - 备份文件必须出现在 Git 提交记录中
 */
class GitBackupManager(private val context: Context) {
    
    companion object {
        private const val TAG = "GitBackupManager"
        private const val BACKUP_DIR_NAME = "yanbao_backup"
    }
    
    // 备份目录：Android/data/com.yanbao.camera/files/yanbao_backup
    private val backupDir = File(context.getExternalFilesDir(null), BACKUP_DIR_NAME)
    
    /**
     * 初始化 Git 仓库
     */
    suspend fun initGitRepo(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 创建备份目录
            if (!backupDir.exists()) {
                backupDir.mkdirs()
                Log.d(TAG, "✅ 备份目录已创建: ${backupDir.absolutePath}")
            }
            
            // 检查是否已初始化 Git
            val gitDir = File(backupDir, ".git")
            if (gitDir.exists()) {
                Log.d(TAG, "✅ Git 仓库已存在")
                return@withContext Result.success("Git 仓库已存在")
            }
            
            // 初始化 Git 仓库
            val process = Runtime.getRuntime().exec(
                arrayOf("git", "init"),
                null,
                backupDir
            )
            
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            
            if (exitCode == 0) {
                Log.d(TAG, "✅ Git 仓库初始化成功: $output")
                
                // 配置 Git 用户信息
                configureGitUser()
                
                // 创建 .gitignore
                createGitignore()
                
                Result.success("Git 仓库初始化成功")
            } else {
                Log.e(TAG, "❌ Git 仓库初始化失败: $error")
                Result.failure(Exception("Git 初始化失败: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 仓库初始化异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 配置 Git 用户信息
     */
    private fun configureGitUser() {
        try {
            Runtime.getRuntime().exec(
                arrayOf("git", "config", "user.name", "YanbaoCamera"),
                null,
                backupDir
            ).waitFor()
            
            Runtime.getRuntime().exec(
                arrayOf("git", "config", "user.email", "backup@yanbao.ai"),
                null,
                backupDir
            ).waitFor()
            
            Log.d(TAG, "✅ Git 用户信息配置成功")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 用户信息配置失败", e)
        }
    }
    
    /**
     * 创建 .gitignore 文件
     */
    private fun createGitignore() {
        try {
            val gitignoreFile = File(backupDir, ".gitignore")
            gitignoreFile.writeText("""
                # 忽略临时文件
                *.tmp
                *.log
                
                # 忽略大文件
                *.jpg
                *.png
                *.mp4
            """.trimIndent())
            
            Log.d(TAG, "✅ .gitignore 文件已创建")
        } catch (e: Exception) {
            Log.e(TAG, "❌ .gitignore 文件创建失败", e)
        }
    }
    
    /**
     * 备份 SharedPreferences 配置文件
     */
    suspend fun backupSharedPreferences(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // SharedPreferences 文件路径
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            val targetDir = File(backupDir, "shared_prefs")
            
            if (!targetDir.exists()) {
                targetDir.mkdirs()
            }
            
            // 复制所有 SharedPreferences 文件
            prefsDir.listFiles()?.forEach { file ->
                if (file.extension == "xml") {
                    val targetFile = File(targetDir, file.name)
                    file.copyTo(targetFile, overwrite = true)
                    Log.d(TAG, "✅ 已备份: ${file.name}")
                }
            }
            
            Result.success("SharedPreferences 备份成功")
        } catch (e: Exception) {
            Log.e(TAG, "❌ SharedPreferences 备份失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 备份雁宝记忆数据库
     */
    suspend fun backupDatabase(): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 数据库文件路径
            val dbFile = context.getDatabasePath("yanbao_memory_database")
            val targetFile = File(backupDir, "yanbao_memory_database.db")
            
            if (dbFile.exists()) {
                dbFile.copyTo(targetFile, overwrite = true)
                Log.d(TAG, "✅ 数据库备份成功: ${dbFile.length()} bytes")
                Result.success("数据库备份成功")
            } else {
                Log.w(TAG, "⚠️ 数据库文件不存在")
                Result.success("数据库文件不存在")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ 数据库备份失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 提交到 Git 仓库
     */
    suspend fun commitToGit(message: String = "Auto backup"): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 添加所有文件
            val addProcess = Runtime.getRuntime().exec(
                arrayOf("git", "add", "."),
                null,
                backupDir
            )
            addProcess.waitFor()
            
            // 提交
            val commitProcess = Runtime.getRuntime().exec(
                arrayOf("git", "commit", "-m", message),
                null,
                backupDir
            )
            
            val exitCode = commitProcess.waitFor()
            val output = commitProcess.inputStream.bufferedReader().readText()
            val error = commitProcess.errorStream.bufferedReader().readText()
            
            if (exitCode == 0 || error.contains("nothing to commit")) {
                Log.d(TAG, "✅ Git 提交成功: $output")
                Result.success("Git 提交成功")
            } else {
                Log.e(TAG, "❌ Git 提交失败: $error")
                Result.failure(Exception("Git 提交失败: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 提交异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 执行完整备份流程
     */
    suspend fun performFullBackup(): Result<String> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "🚀 开始完整备份...")
            
            // 1. 初始化 Git 仓库
            initGitRepo().getOrThrow()
            
            // 2. 备份 SharedPreferences
            backupSharedPreferences().getOrThrow()
            
            // 3. 备份数据库
            backupDatabase().getOrThrow()
            
            // 4. 提交到 Git
            val timestamp = System.currentTimeMillis()
            commitToGit("Auto backup at $timestamp").getOrThrow()
            
            Log.d(TAG, "✅ 完整备份成功")
            Result.success("完整备份成功\n备份路径: ${backupDir.absolutePath}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ 完整备份失败", e)
            Result.failure(e)
        }
    }
    
    /**
     * 获取 Git 提交记录
     */
    suspend fun getGitLog(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(
                arrayOf("git", "log", "--oneline", "-10"),
                null,
                backupDir
            )
            
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            
            if (exitCode == 0) {
                Log.d(TAG, "✅ Git 日志获取成功:\n$output")
                Result.success(output)
            } else {
                Result.failure(Exception("Git 日志获取失败"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 日志获取异常", e)
            Result.failure(e)
        }
    }
    
    /**
     * 检查 Git 仓库状态
     */
    suspend fun checkGitStatus(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val gitDir = File(backupDir, ".git")
            if (!gitDir.exists()) {
                return@withContext Result.failure(Exception("Git 仓库未初始化"))
            }
            
            val process = Runtime.getRuntime().exec(
                arrayOf("git", "status", "--short"),
                null,
                backupDir
            )
            
            val exitCode = process.waitFor()
            val output = process.inputStream.bufferedReader().readText()
            
            if (exitCode == 0) {
                Log.d(TAG, "✅ Git 状态:\n$output")
                Result.success(output.ifEmpty { "工作区干净" })
            } else {
                Result.failure(Exception("Git 状态检查失败"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 状态检查异常", e)
            Result.failure(e)
        }
    }
}
