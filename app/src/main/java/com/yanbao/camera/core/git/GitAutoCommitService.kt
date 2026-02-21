package com.yanbao.camera.core.git

import android.content.Context
import android.util.Log
import com.yanbao.camera.core.config.ThemeConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

/**
 * Git 自动提交服务
 * 
 * 核心功能：
 * - 拍照即触发 `git add . && git commit -m "29D_Seed_[Hash]"`
 * - 将元数据写入「雁宝记忆」
 * - 在「我的」页面显示真实的 .git 目录体积与 Commit 总数
 * 
 * 技术栈：
 * - 使用 ProcessBuilder 执行 git 命令
 * - 使用 SHA-256 生成 29D Seed Hash
 */
class GitAutoCommitService(private val context: Context) {
    
    private val themeConfig = ThemeConfig.load(context)
    private val gitRepoPath = File(context.filesDir, "yanbao_memories")
    
    companion object {
        private const val TAG = "GitAutoCommitService"
    }
    
    init {
        // 初始化 Git 仓库
        initializeGitRepo()
    }
    
    /**
     * 初始化 Git 仓库
     */
    private fun initializeGitRepo() {
        if (!gitRepoPath.exists()) {
            gitRepoPath.mkdirs()
        }
        
        val gitDir = File(gitRepoPath, ".git")
        if (!gitDir.exists()) {
            try {
                executeGitCommand("init")
                executeGitCommand("config", "user.name", "yanbao AI")
                executeGitCommand("config", "user.email", "memories@yanbao.ai")
                Log.d(TAG, "✅ Git 仓库初始化成功: ${gitRepoPath.absolutePath}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Git 仓库初始化失败", e)
            }
        }
    }
    
    /**
     * 拍照后自动提交
     * 
     * @param photoPath 照片路径
     * @param metadata 29D 参数元数据
     * @return 提交的 Hash
     */
    suspend fun autoCommitOnCapture(
        photoPath: String,
        metadata: Map<String, Any>
    ): String? = withContext(Dispatchers.IO) {
        if (!themeConfig.git_sync_protocol.auto_commit_on_capture) {
            Log.d(TAG, "⚠️ Git 自动提交已禁用")
            return@withContext null
        }
        
        try {
            // 1. 生成 29D Seed Hash
            val seed29D = generate29DSeedHash(metadata)
            
            // 2. 将照片复制到 Git 仓库
            val photoFile = File(photoPath)
            val targetFile = File(gitRepoPath, "photos/${photoFile.name}")
            targetFile.parentFile?.mkdirs()
            photoFile.copyTo(targetFile, overwrite = true)
            
            // 3. 将元数据写入 JSON 文件
            val metadataFile = File(gitRepoPath, "metadata/${photoFile.nameWithoutExtension}.json")
            metadataFile.parentFile?.mkdirs()
            metadataFile.writeText(metadata.toString())
            
            // 4. 执行 git add .
            executeGitCommand("add", ".")
            
            // 5. 执行 git commit
            val commitMessage = "29D_Seed_${seed29D}"
            executeGitCommand("commit", "-m", commitMessage)
            
            Log.d(TAG, "✅ Git 自动提交成功: $commitMessage")
            
            seed29D
        } catch (e: Exception) {
            Log.e(TAG, "❌ Git 自动提交失败", e)
            null
        }
    }
    
    /**
     * 生成 29D Seed Hash
     * 
     * 使用 SHA-256 对 29D 参数进行哈希
     */
    private fun generate29DSeedHash(metadata: Map<String, Any>): String {
        val input = metadata.entries
            .sortedBy { it.key }
            .joinToString("|") { "${it.key}=${it.value}" }
        
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }.take(8) // 取前8位
    }
    
    /**
     * 获取 Git 仓库统计信息
     * 
     * @return Pair<照片数量, 存储占用(MB)>
     */
    fun getGitRepoStats(): Pair<Int, Double> {
        try {
            // 1. 统计照片数量
            val photosDir = File(gitRepoPath, "photos")
            val photoCount = photosDir.listFiles()?.size ?: 0
            
            // 2. 统计存储占用
            val totalSize = gitRepoPath.walkTopDown()
                .filter { it.isFile }
                .map { it.length() }
                .sum()
            val sizeMB = totalSize / (1024.0 * 1024.0)
            
            Log.d(TAG, "📊 Git 仓库统计: $photoCount 张照片, ${String.format("%.2f", sizeMB)} MB")
            
            return Pair(photoCount, sizeMB)
        } catch (e: Exception) {
            Log.e(TAG, "❌ 获取 Git 仓库统计失败", e)
            return Pair(0, 0.0)
        }
    }
    
    /**
     * 获取 Commit 总数
     */
    fun getCommitCount(): Int {
        return try {
            val output = executeGitCommand("rev-list", "--count", "HEAD")
            output.trim().toIntOrNull() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "❌ 获取 Commit 总数失败", e)
            0
        }
    }
    
    /**
     * 执行 Git 命令
     */
    private fun executeGitCommand(vararg command: String): String {
        val fullCommand = listOf("git", "-C", gitRepoPath.absolutePath) + command
        
        val process = ProcessBuilder(fullCommand)
            .redirectErrorStream(true)
            .start()
        
        val output = process.inputStream.bufferedReader().readText()
        val exitCode = process.waitFor()
        
        if (exitCode != 0) {
            throw RuntimeException("Git command failed: ${fullCommand.joinToString(" ")}\nOutput: $output")
        }
        
        return output
    }
}
