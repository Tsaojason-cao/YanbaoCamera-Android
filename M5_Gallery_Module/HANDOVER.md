# SheYan M5 相册模块满血版 — 完整交接清册

---

**项目负责人:** Manus AI
**交付日期:** 2026-02-26
**Git Commit:** `feat(M5): Add SheYan M5 Gallery Module - Full UI Designs (10 screens) + Architecture Audit + DB Schema`

---

## 一、 满血审查指令 — 官方答复

### Q1: 数据一致性 (删除“雁宝记忆”照片)

**策略：** 采用“软删除 + 标记同步”机制。

1.  **本地软删除：** 用户在相册中删除“雁宝记忆”照片时，该 `PhotoEntity` 在 Room 数据库中并不会被立即物理删除，而是将其 `isDeleted` 标记置为 `true`。
2.  **Git 标记同步：** 下次 Git 同步时，系统会检测到 `isDeleted=true` 的实体，但不会直接删除云端的参数包文件。相反，它会在 Git 仓库中该参数文件对应的元数据（或一个独立的 manifest 文件）中添加一个 `deleted:true` 的标记。这确保了参数历史的完整性，以备未来恢复或审计。
3.  **云端清理策略：** 可设置一个独立的、定期的云端清理任务（例如，每月一次），专门清理那些被标记为 `deleted` 超过30天的参数包，以节省存储空间。

**结论：** 优先保证参数历史不丢失，避免误删。本地立即隐藏，云端延迟清理。

### Q2: 加载性能 (千张“雁宝记忆”照片)

**策略：** 采用“数据库索引 + 异步分页加载 + 内存缓存”组合拳。

1.  **数据库索引 (Indexing)：** 正如 `PhotoEntity` 结构所定义，我们在 `isMemory` 和 `creationTimestamp` 字段上建立了数据库索引。这使得 `WHERE isMemory = true ORDER BY creationTimestamp DESC` 的查询操作能达到毫秒级响应，无论照片总量多大，都能快速定位到所有“雁宝记忆”照片。
2.  **异步分页加载 (Paging 3)：** 相册网格界面将采用 Android Jetpack Paging 3 库。它能实现高效的数据库分页加载，每次只从数据库中加载屏幕可见区域所需的少量数据（例如 20-30 张），当用户滑动时再异步加载下一页。这确保了极低的内存占用和流畅的滑动体验。
3.  **JSON 异步解析与缓存 (Async Parsing & LruCache)：** 我们不会在列表加载时就解析所有照片的 `memoryParamsJson`。只有当用户点击照片进入详情页或长按预览时，才会触发对该特定照片 JSON 的异步解析。首次解析的结果会存入一个 `LruCache`（最近最少使用缓存）中，后续再次查看同一张照片将直接从内存中读取，实现秒开。

**结论：** 即使有数万张“雁宝记忆”照片，通过索引定位、分页加载和按需解析，相册主界面的滑动性能也能媲美原生系统相册。

### Q3: 检索逻辑 (相机反向调用参数)

**策略：** 通过 `ContentProvider` 和自定义 URI 实现跨模块安全调用。

1.  **定义 ContentProvider：** 我们将创建一个名为 `SheYanMemoryProvider` 的 `ContentProvider`，其 `authority` 为 `im.sheyan.provider.memory`。
2.  **自定义 URI 格式：** 当用户在相册中点击“以此为记忆拍摄”按钮时，系统会生成一个指向该照片参数的特定 URI，格式如下：
    ```
    content://im.sheyan.provider.memory/params/[photo_id]
    ```
    其中 `[photo_id]` 是该照片在 `PhotoEntity` 中的主键 ID。
3.  **相机模块调用：** 相机模块通过 `Intent` 接收到这个 URI。然后，它使用 `ContentResolver` 向 `SheYanMemoryProvider` 发起查询请求。
    ```kotlin
    val cursor = contentResolver.query(uri, null, null, null, null)
    ```
4.  **Provider 内部实现：** `SheYanMemoryProvider` 在其 `query()` 方法中，会解析 URI 中的 `photo_id`，然后从 Room 数据库中查询对应的 `PhotoEntity`，最后将 `memoryParamsJson` 字段的内容作为查询结果返回。
5.  **参数应用：** 相机模块拿到 JSON 字符串后，反序列化为参数对象，并将其应用到实时预览和拍摄流程中，完成 1:1 的参数套用。

**结论：** `ContentProvider` 机制是 Android 系统推荐的安全、解耦的跨模块数据共享方式，完美契合此场景。

---

## 二、 PhotoEntity 数据库结构 (Room)

```kotlin
@Entity(tableName = "photos")
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uri: String, // 文件URI
    @ColumnInfo(index = true) val creationTimestamp: Long, // 创建时间戳 (索引)
    @ColumnInfo(index = true) val isMemory: Boolean = false, // 是否为雁宝记忆 (索引)
    val isFeatured: Boolean = false, // 是否为推荐
    val gitHash: String?, // Git Commit Hash
    val memoryParamsJson: String?, // 雁宝记忆参数包 (JSON)
    val parentMemoryId: Long?, // 父记忆点ID (用于记忆分支)
    val aiScore: Float?, // AI评分
    @ColumnInfo(index = true) val latitude: Double?, // 纬度 (索引)
    @ColumnInfo(index = true) val longitude: Double?, // 经度 (索引)
    val locationName: String?, // 地理位置名称
    val isDeleted: Boolean = false // 软删除标记
)
```

---

## 三、 全套 UI 设计图（10 张，纯 UI 无手机框）

| # | 文件名 | 界面/功能 |
|---|---|---|
| 01 | `GAL_M5_01_main_grid.png` | 相册主网格 (三大分类) |
| 02 | `GAL_M5_02_photo_detail.png` | 照片详情页 (LBS/参数/调用) |
| 03 | `GAL_M5_03_memory_params_popup.png` | 雁宝记忆参数预览浮窗 |
| 04 | `GAL_M5_04_featured_feed.png` | 推荐分类竖屏流预览 |
| 05 | `GAL_M5_05_longpress_preview.png` | 长按高斯模糊预览 |
| 06 | `GAL_M5_06_memory_branch.png` | 记忆分支版本链 |
| 07 | `GAL_M5_07_standard_multiselect.png` | 一般分类多选操作 |
| 08 | `GAL_M5_08_lbs_map.png` | LBS 地图足迹 |
| 09 | `GAL_M5_09_search_filter.png` | 搜索与筛选面板 |
| 10 | `GAL_M5_10_db_flow_diagram.png` | 数据库架构与参数回流逻辑图 |

所有设计图均已打包至 `SheYan_M5_Gallery_UI_Assets.zip`。
