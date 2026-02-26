# SheYan M7「我的/雁宝园地」满血版 — 完整交接清册

---

**项目负责人:** Manus AI
**交付日期:** 2026-02-26
**Git Commit:** `feat(M7): Add SheYan M7 MyProfile Module - Full UI Designs (10 screens) + UserStatUpdate Spec + Tech Audit`

---

## 一、 满血审查指令 — 官方答复

### Q1: 防作弊逻辑 (刷胡萝卜)

采用 **服务器端权威时间 (NTP) + 本地 Room 数据库双重验证** 机制。本地所有“每日”相关逻辑均以服务器时间为准，忽略用户手机时间。Room 数据库记录 `lastFeedTimestamp` 和 `dailyFeedCount`，防止通过修改系统时间重复刷取每日首次喂食奖励。

### Q2: LBS 隐私与 Git 同步

采用 **路径约定 + Git LFS** 方案。用户头像上传至专用 S3/OSS 并返回永久 CDN URL，Git 仓库仅同步 `UserProfile.json` 文件中的 `avatarUrl` 字段，而非图片本身，确保仓库轻量且头像实时更新。

### Q3: 会员状态流转与 UI 引导

**会员天数**通过 `(当前服务器时间戳 - 注册时间戳)` 动态计算。当特权次数用尽时，相机模块会弹出半透明浮层，引导用户“前往「推荐」模块分享作品”以获取更多胡萝卜，形成运营闭环。

---

## 二、 UserStatUpdate 数据类定义 (Kotlin/Room)

```kotlin
// Data class for updating user stats after a successful feeding action
data class UserStatUpdate(
    val userId: String,            // 用户唯一标识, e.g., "YB-2026-0001"
    val privilegeCountChange: Int, // 特权次数变化量 (e.g., +1)
    val totalFeedCount: Int,       // 累计总喂食次数
    val lastFeedTimestamp: Long,   // 本次喂食的服务器时间戳 (ms)
    val isFirstDailyFeed: Boolean, // 是否为每日首签
    val dailyFeedCount: Int        // 更新后，今天的喂食次数
)
```

---

## 三、 全套 UI 设计图（10 张，纯 UI 无手机框）

| # | 文件名 | 界面/功能 |
|---|---|---|
| 01 | `MY_M7_01_profile_main.png` | 个人主页（高斯模糊背景墙 + 雁宝记忆作品流）|
| 02 | `MY_M7_02_yanbao_garden.png` | 雁宝园地（3D 雁宝形象 + 胡萝卜喂食系统）|
| 03 | `MY_M7_03_feeding_success.png` | 喂食成功动画（粒子效果 + 特权解锁反馈）|
| 04 | `MY_M7_04_edit_profile.png` | 编辑个人资料（头像/ID/签名 + LBS 开关）|
| 05 | `MY_M7_05_carrot_empty_guide.png` | 胡萝卜能量不足引导（相机内浮层 + 分享任务）|
| 06 | `MY_M7_06_other_profile.png` | 他人主页（公开参数包 + Get同款按钮）|
| 07 | `MY_M7_07_settings.png` | 设置与隐私中心（Git 同步状态 + LBS 精度）|
| 08 | `MY_M7_08_membership_growth.png` | 会员成长体系（等级 + 勋章墙 + 动态时间线）|
| 09 | `MY_M7_09_notifications.png` | 消息通知中心（互动/系统/雁宝园地分类）|
| 10 | `MY_M7_10_full_system_flow.png` | 全模块业务闭环逻辑横版图（M3-M7）|

---

## 四、 结语

至此，SheYan App 的所有核心模块（M3-M7）已全部定义与设计完毕。整套方案以“雁宝记忆”参数流转为技术核心，以“72:28 布局”为视觉标准，以“胡萝卜喂食”为运营闭环，构成了完整的用户体验生态。
