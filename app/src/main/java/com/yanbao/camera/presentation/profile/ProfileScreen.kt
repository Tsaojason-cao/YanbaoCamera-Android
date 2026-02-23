package com.yanbao.camera.presentation.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.yanbao.camera.R

private val KUROMI_PINK = Color(0xFFEC4899)
private val OBSIDIAN_BLACK = Color(0xFF0A0A0A)
private val CARD_BG = Color(0xFF1E1E1E)

/**
 * 个人中心界面（满血版 v2）
 *
 * 功能：
 * - 背景墙（粉紫渐变 + 自定义背景）
 * - 圆形头像（带粉色光晕）
 * - 昵称 / ID / 会员号 / 与雁宝同行天数
 * - 统计数据卡片（作品数 / 记忆数 / 获赞数）
 * - 11个设置项（含开关、清理缓存、Git备份等）
 * - 所有图标使用真实 drawable，无 emoji
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsState()
    val stats by viewModel.stats.collectAsState()
    val backupStatus by viewModel.backupStatus.collectAsState()
    val cacheSize by viewModel.cacheSize.collectAsState()
    val clearCacheStatus by viewModel.clearCacheStatus.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsState()
    val highQualityExport by viewModel.highQualityExport.collectAsState()

    val avatarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateAvatar(it) } }

    val backgroundLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { viewModel.updateBackground(it) } }

    var showIdDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(OBSIDIAN_BLACK)
    ) {
        // ─── 顶部背景墙 + 头像 ───────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {
                // 背景墙
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF7C3AED), Color(0xFFEC4899))
                            )
                        )
                        .clickable { backgroundLauncher.launch("image/*") }
                ) {
                    profile.backgroundUri?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "背景",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.25f))
                    )
                }

                // 返回按钮
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .padding(top = 32.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onBack(); onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_back),
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 编辑资料按钮（右上角）
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .padding(top = 32.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable { onEditProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "编辑资料",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 头像（底部居中，向下偏移）
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 50.dp)
                ) {
                    // 粉色光晕
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(KUROMI_PINK.copy(alpha = 0.4f), Color.Transparent)
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.Center)
                            .clip(CircleShape)
                            .border(3.dp, KUROMI_PINK, CircleShape)
                            .background(Color(0xFF2A2A2A))
                            .clickable { avatarLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (profile.avatarUri != null) {
                            Image(
                                painter = rememberAsyncImagePainter(profile.avatarUri),
                                contentDescription = "头像",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = "头像",
                                tint = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }
            }
        }

        // ─── 用户信息区 ───────────────────────────────────────────────────
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 60.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = profile.userName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { showIdDialog = true }
                ) {
                    Text(
                        text = profile.userId,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "编辑ID",
                        tint = Color.White.copy(alpha = 0.4f),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // 会员信息卡片
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CARD_BG)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("会员号", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                profile.memberNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = KUROMI_PINK
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("与雁宝同行", fontSize = 11.sp, color = Color.White.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${profile.daysWithYanbao} 天",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                    }
                }
            }
        }

        // ─── 统计数据卡片 ─────────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CARD_BG)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        value = stats.worksCount.toString(),
                        label = "作品"
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    StatItem(
                        value = stats.memoriesCount.toString(),
                        label = "记忆",
                        valueColor = KUROMI_PINK
                    )
                    Divider(
                        modifier = Modifier
                            .height(36.dp)
                            .width(1.dp),
                        color = Color.White.copy(alpha = 0.1f)
                    )
                    StatItem(
                        value = stats.likesCount,
                        label = "获赞"
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ─── 设置列表（11项）────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CARD_BG)
            ) {
                Column {
                    // 1. 更换背景
                    SettingItem(
                        iconRes = R.drawable.ic_gallery,
                        title = "更换背景",
                        subtitle = "自定义个人主页背景",
                        onClick = { backgroundLauncher.launch("image/*") }
                    )
                    SettingDivider()

                    // 2. 更换头像
                    SettingItem(
                        iconRes = R.drawable.ic_camera,
                        title = "更换头像",
                        subtitle = "从相册选择头像",
                        onClick = { avatarLauncher.launch("image/*") }
                    )
                    SettingDivider()

                    // 3. 修改 ID
                    SettingItem(
                        iconRes = R.drawable.ic_edit,
                        title = "修改 ID",
                        subtitle = profile.userId,
                        onClick = { showIdDialog = true }
                    )
                    SettingDivider()

                    // 4. 通知设置（开关）
                    SettingItemWithSwitch(
                        iconRes = R.drawable.ic_info,
                        title = "推送通知",
                        subtitle = "接收雁宝记忆提醒",
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.toggleNotifications() }
                    )
                    SettingDivider()

                    // 5. 自动备份（开关）
                    SettingItemWithSwitch(
                        iconRes = R.drawable.ic_memory,
                        title = "自动 Git 备份",
                        subtitle = if (autoBackupEnabled) "已开启" else "已关闭",
                        checked = autoBackupEnabled,
                        onCheckedChange = { viewModel.toggleAutoBackup() }
                    )
                    SettingDivider()

                    // 6. 高质量导出（开关）
                    SettingItemWithSwitch(
                        iconRes = R.drawable.ic_share,
                        title = "高质量导出",
                        subtitle = "分享时保留原始画质",
                        checked = highQualityExport,
                        onCheckedChange = { viewModel.toggleHighQualityExport() }
                    )
                    SettingDivider()

                    // 7. Git 同步备份
                    SettingItem(
                        iconRes = R.drawable.ic_memory,
                        title = "Git 同步备份",
                        subtitle = backupStatus ?: "立即备份雁宝记忆到 GitHub",
                        subtitleColor = when {
                            backupStatus?.startsWith("✅") == true -> Color(0xFF10B981)
                            backupStatus?.startsWith("❌") == true -> Color(0xFFFF6B6B)
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        onClick = { viewModel.performGitBackup() }
                    )
                    SettingDivider()

                    // 8. 清理缓存
                    SettingItem(
                        iconRes = R.drawable.ic_delete,
                        title = "清理缓存",
                        subtitle = clearCacheStatus ?: "当前缓存：$cacheSize",
                        subtitleColor = when {
                            clearCacheStatus?.startsWith("✅") == true -> Color(0xFF10B981)
                            clearCacheStatus?.startsWith("❌") == true -> Color(0xFFFF6B6B)
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        onClick = { showClearCacheDialog = true }
                    )
                    SettingDivider()

                    // 9. 隐私设置
                    SettingItem(
                        iconRes = R.drawable.ic_settings,
                        title = "隐私设置",
                        subtitle = "管理数据权限",
                        onClick = { /* 跳转隐私设置页 */ }
                    )
                    SettingDivider()

                    // 10. 帮助中心
                    SettingItem(
                        iconRes = R.drawable.ic_info,
                        title = "帮助中心",
                        subtitle = "使用教程与常见问题",
                        onClick = { /* 跳转帮助页 */ }
                    )
                    SettingDivider()

                    // 11. 关于雁宝
                    SettingItem(
                        iconRes = R.drawable.ic_settings,
                        title = "关于雁宝 AI",
                        subtitle = "版本 1.0.0 · Phase 1",
                        onClick = { /* 跳转关于页 */ },
                        showArrow = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // ─── 修改 ID 对话框 ───────────────────────────────────────────────────
    if (showIdDialog) {
        var idInput by remember { mutableStateOf(profile.userId) }
        AlertDialog(
            onDismissRequest = { showIdDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("修改 ID", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = idInput,
                    onValueChange = { idInput = it },
                    label = { Text("用户 ID", color = Color.White.copy(alpha = 0.6f)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = KUROMI_PINK,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateUserId(idInput)
                    showIdDialog = false
                }) {
                    Text("保存", color = KUROMI_PINK)
                }
            },
            dismissButton = {
                TextButton(onClick = { showIdDialog = false }) {
                    Text("取消", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }

    // ─── 清理缓存确认对话框 ───────────────────────────────────────────────
    if (showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = { showClearCacheDialog = false },
            containerColor = Color(0xFF1A1A1A),
            title = { Text("清理缓存", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "确定要清理 $cacheSize 的缓存吗？",
                    color = Color.White.copy(alpha = 0.8f)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCache()
                    showClearCacheDialog = false
                }) {
                    Text("清理", color = Color(0xFFFF6B6B))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCacheDialog = false }) {
                    Text("取消", color = Color.White.copy(alpha = 0.6f))
                }
            }
        )
    }
}

// ─── 子组件 ──────────────────────────────────────────────────────────────────

@Composable
private fun StatItem(
    value: String,
    label: String,
    valueColor: Color = Color.White
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.55f)
        )
    }
}

@Composable
private fun SettingItem(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    subtitleColor: Color = Color.White.copy(alpha = 0.5f),
    showArrow: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KUROMI_PINK.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                tint = KUROMI_PINK,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor,
                    maxLines = 1
                )
            }
        }
        if (showArrow) {
            Icon(
                painter = painterResource(R.drawable.ic_back),
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier
                    .size(16.dp)
                    .graphicsLayerMirror()
            )
        }
    }
}

@Composable
private fun SettingItemWithSwitch(
    iconRes: Int,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KUROMI_PINK.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = title,
                tint = KUROMI_PINK,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 12.sp, color = Color.White.copy(alpha = 0.5f))
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = KUROMI_PINK,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.White.copy(alpha = 0.2f)
            )
        )
    }
}

@Composable
private fun SettingDivider() {
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = Color.White.copy(alpha = 0.07f),
        thickness = 0.5.dp
    )
}

// 水平镜像修饰符（用于箭头图标朝右）
private fun Modifier.graphicsLayerMirror(): Modifier = this.graphicsLayer { rotationY = 180f }
