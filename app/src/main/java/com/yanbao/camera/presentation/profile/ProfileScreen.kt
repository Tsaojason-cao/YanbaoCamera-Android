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
import com.yanbao.camera.ui.theme.YanbaoBrandTitle

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
 * - 15个设置项（含开关、清理缓存、Git备份等）
 * - 所有图标使用真实 drawable，无 emoji
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onPrivacy: () -> Unit = {},
    onHelp: () -> Unit = {},
    onAbout: () -> Unit = {},
    onYanbaoGarden: () -> Unit = {}
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
                        painter = painterResource(R.drawable.ic_back_kuromi),
                        contentDescription = "返回",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // 品牌标识（顶部居中）
                YanbaoBrandTitle(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 48.dp)
                )

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
                        painter = painterResource(R.drawable.ic_edit_kuromi),
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
                                painter = painterResource(R.drawable.ic_camera_kuromi),
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
                        painter = painterResource(R.drawable.ic_edit_kuromi),
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

        // ─── 雁宝园地入口卡片 ─────────────────────────────────────────
        item {
            YanbaoGardenEntryCard(
                onEnterGarden = { onYanbaoGarden() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ─── 设置列表（15项）────────────────────────────────────────────
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CARD_BG)
            ) {
                Column {
                    // 1. 个人隐私
                    SettingItem(
                        iconRes = R.drawable.ic_privacy_kuromi,
                        title = "个人隐私",
                        subtitle = "管理数据权限",
                        onClick = { onPrivacy() }
                    )
                    SettingDivider()
                    // 2. 账号状态
                    SettingItem(
                        iconRes = R.drawable.ic_account_kuromi,
                        title = "账号状态",
                        subtitle = "公开",
                        onClick = { android.util.Log.d("ProfileScreen", "账号状态页") }
                    )
                    SettingDivider()
                    // 3. LBS定位
                    SettingItem(
                        iconRes = R.drawable.ic_lbs_kuromi,
                        title = "LBS定位",
                        subtitle = "位置服务设置",
                        onClick = { android.util.Log.d("ProfileScreen", "LBS定位页") }
                    )
                    SettingDivider()
                    // 4. 资产管理
                    SettingItem(
                        iconRes = R.drawable.ic_asset_kuromi,
                        title = "资产管理",
                        subtitle = "查看我的资产",
                        onClick = { android.util.Log.d("ProfileScreen", "资产管理页") }
                    )
                    SettingDivider()
                    // 5. 我的雁宝记忆
                    SettingItem(
                        iconRes = R.drawable.ic_memory_kuromi,
                        title = "我的雁宝记忆",
                        subtitle = "${stats.memoriesCount}个",
                        onClick = { android.util.Log.d("ProfileScreen", "雁宝记忆页") }
                    )
                    SettingDivider()
                    // 6. 交易记录
                    SettingItem(
                        iconRes = R.drawable.ic_transaction_kuromi,
                        title = "交易记录",
                        subtitle = "查看消费明细",
                        onClick = { android.util.Log.d("ProfileScreen", "交易记录页") }
                    )
                    SettingDivider()
                    // 7. 内容设置
                    SettingItem(
                        iconRes = R.drawable.ic_content_kuromi,
                        title = "内容设置",
                        subtitle = "管理内容展示偏好",
                        onClick = { android.util.Log.d("ProfileScreen", "内容设置页") }
                    )
                    SettingDivider()
                    // 8. 相机预设
                    SettingItem(
                        iconRes = R.drawable.ic_camera_preset_kuromi,
                        title = "相机预设",
                        subtitle = "自定义拍摄参数预设",
                        onClick = { android.util.Log.d("ProfileScreen", "相机预设页") }
                    )
                    SettingDivider()
                    // 9. 浮水印
                    SettingItem(
                        iconRes = R.drawable.ic_watermark_kuromi,
                        title = "浮水印",
                        subtitle = "设置照片水印样式",
                        onClick = { android.util.Log.d("ProfileScreen", "浮水印设置页") }
                    )
                    SettingDivider()
                    // 10. HD高画质上传
                    SettingItemWithSwitch(
                        iconRes = R.drawable.ic_hd_kuromi,
                        title = "HD高画质上传",
                        subtitle = "分享时保留原始画质",
                        checked = highQualityExport,
                        onCheckedChange = { viewModel.toggleHighQualityExport() }
                    )
                    SettingDivider()
                    // 11. 系统工具
                    SettingItem(
                        iconRes = R.drawable.ic_system_kuromi,
                        title = "系统工具",
                        subtitle = "设备信息与诊断",
                        onClick = { android.util.Log.d("ProfileScreen", "系统工具页") }
                    )
                    SettingDivider()
                    // 12. 语言选择
                    SettingItem(
                        iconRes = R.drawable.ic_language_kuromi,
                        title = "语言选择",
                        subtitle = "简体中文",
                        onClick = { android.util.Log.d("ProfileScreen", "语言选择页") }
                    )
                    SettingDivider()
                    // 13. 清理缓存
                    SettingItem(
                        iconRes = R.drawable.ic_clear_cache_kuromi,
                        title = "清理缓存",
                        subtitle = clearCacheStatus ?: "当前缓存：$cacheSize",
                        subtitleColor = when {
                            clearCacheStatus?.startsWith("[OK]") == true -> Color(0xFF10B981)
                            clearCacheStatus?.startsWith("[ERR]") == true -> Color(0xFFFF6B6B)
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        onClick = { showClearCacheDialog = true }
                    )
                    SettingDivider()
                    // 14. Git备份
                    SettingItem(
                        iconRes = R.drawable.ic_git_kuromi,
                        title = "Git备份",
                        subtitle = backupStatus ?: "立即备份雁宝记忆到 GitHub",
                        subtitleColor = when {
                            backupStatus?.startsWith("[OK]") == true -> Color(0xFF10B981)
                            backupStatus?.startsWith("[ERR]") == true -> Color(0xFFFF6B6B)
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        onClick = { viewModel.performGitBackup() }
                    )
                    SettingDivider()
                    // 15. 检查更新
                    SettingItem(
                        iconRes = R.drawable.ic_update_kuromi,
                        title = "检查更新",
                        subtitle = "v1.0.0",
                        onClick = { onHelp() },
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
                painter = painterResource(R.drawable.ic_back_kuromi),
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

// ─── 雁宝园地入口卡片 ─────────────────────────────────────────────────────────

/**
 * 雁宝园地入口卡片
 *
 * 在个人中心页面展示，引导用户进入园地喂食互动。
 * 设计：胡萝卜橙渐变背景 + 雁宝 IP 图片 + 动态文案
 */
@Composable
fun YanbaoGardenEntryCard(
    onEnterGarden: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 呼吸光晕动画
    val infiniteTransition = rememberInfiniteTransition(label = "garden_entry")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1A0A00), Color(0xFF2A1200))
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF97316).copy(alpha = glowAlpha),
                        Color(0xFFEC4899).copy(alpha = glowAlpha * 0.7f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onEnterGarden() }
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 雁宝图标
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF97316).copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.yanbao_caring),
                    contentDescription = "雁宝",
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // 文案区域
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "雁宝园地",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFF97316).copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "互动",
                            fontSize = 10.sp,
                            color = Color(0xFFF97316),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "喂食胡萝卜，解锁专属特权",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                // 今日次数提示
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316).copy(alpha = if (index == 0) 1f else 0.3f))
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "今日可喂 3 次",
                        fontSize = 11.sp,
                        color = Color(0xFFF97316).copy(alpha = 0.8f)
                    )
                }
            }

            // 进入箭头
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF97316).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_back_kuromi),
                    contentDescription = "进入园地",
                    tint = Color(0xFFF97316),
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer { rotationY = 180f }
                )
            }
        }
    }
}
