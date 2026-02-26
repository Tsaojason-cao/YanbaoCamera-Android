# SheYan M7「我的/雁宝园地」— 技术审计与数据结构定义

---

**项目负责人:** Manus AI
**交付日期:** 2026-02-26

---

## 一、 满血审查指令 — 官方答复

### Q1: 防作弊逻辑 (刷胡萝卜)

采用 **服务器端权威时间 + 本地 Room 数据库双重验证** 机制。

1.  **网络时间校验 (NTP):** App 启动时及每次“喂食”操作前，强制从服务器获取权威网络时间。本地所有与“每日”相关的逻辑（如每日首签、任务上限）均以此时间为准，彻底忽略用户手机的系统时间。
2.  **本地记录:** Room 数据库中的 `UserStats` 表会记录 `lastFeedTimestamp` (上次喂食的服务器时间戳) 和 `dailyFeedCount` (当日已喂食次数)。
3.  **判定逻辑:** 用户点击喂食时，App 检查 `lastFeedTimestamp` 是否与当前服务器时间在同一天（按 UTC+8 计算）。若不是同一天，则 `dailyFeedCount` 清零并允许喂食（每日首签）。若是同一天，则检查 `dailyFeedCount` 是否已达到上限 (3次)。超过上限则禁止操作。

通过此方案，即使用户断网并修改本地时间，也无法通过“每日首签”刷胡萝卜；联网状态下，服务器时间戳使其无法作弊。

### Q2: LBS 隐私与 Git 同步

采用 **路径约定 + Git LFS (Large File Storage)** 方案。

1.  **图片存储:** 用户头像不直接存入 Git 仓库。开启 LBS 共享后，新头像将通过 App 的后端 API 上传至专用的 S3/OSS 存储桶，并返回一个**永久性的 CDN URL**。
2.  **Git 同步内容:** Git 仓库中同步的不是图片文件本身，而是在 `UserProfile.json` 文件中更新用户的 `avatarUrl` 字段为新的 CDN URL。
3.  **数据流:** 用户更换头像 -> App 上传图片至 S3 -> 获取 `avatarUrl` -> 更新本地 `UserProfile.json` -> 触发 Git 异步任务，将 `UserProfile.json` 的变更 commit & push。
4.  **地图展示:** 其他用户在“机位地图”上查看时，App 从 Git 拉取最新的 `UserProfile.json`，通过 `avatarUrl` 字段加载该用户的头像，确保始终显示最新版本，且无需将大量图片资源存入 Git，保持仓库轻量。

### Q3: 会员状态流转与 UI 引导

1.  **会员天数计算:** `Membership Days` 的计算逻辑为 `(当前服务器时间戳 - 用户注册时间戳) / (24 * 60 * 60)`，结果向下取整。该计算在每次进入“我的”页面时动态执行，确保实时准确。
2.  **UI 引导逻辑:** 当用户在相机模块试图使用“大师模式”或“2.9D”等特权功能，但 `privilege_count` (胡萝卜能量) 为 0 时，触发以下 UI 流程：
    *   **弹出提示框 (Toast/Snackbar):** 立即在相机界面显示“胡萝卜能量不足！”的提示。
    *   **浮层引导卡片 (Overlay Card):** 紧接着，弹出一个半透明浮层，内容为：“您的胡萝卜能量已耗尽。前往「推荐」模块分享一篇作品，即可获得更多胡萝卜哦！”，并提供两个按钮：【取消】和【前往推荐】。
    *   **跳转:** 用户点击【前往推荐】后，App 自动跳转至 M6 推荐模块的主信息流页面，鼓励用户进行分享互动，形成运营闭环。

---

## 二、 UserStatUpdate 数据类定义 (Kotlin/Room)

当用户成功喂食胡萝卜后，App 将创建一个 `UserStatUpdate` 实例，用于更新数据库和触发后续逻辑。

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
