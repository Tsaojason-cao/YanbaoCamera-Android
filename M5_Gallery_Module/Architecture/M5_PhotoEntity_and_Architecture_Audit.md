# SheYan M5 相册模块 — PhotoEntity 数据库结构与架构审计

**版本:** 1.0
**日期:** 2026-02-26
**作者:** Manus AI

---

## 一、 PhotoEntity 数据库结构定义 (Room for Android)

为了支持“雁宝记忆”、“记忆分支”和 LBS 地理标记等复杂功能，`PhotoEntity` 在 Room 数据库中的结构定义如下。该结构充分考虑了性能、索引和未来扩展性。

```kotlin
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "photos",
    indices = [
        Index(value = ["creationTimestamp"], name = "idx_photos_timestamp"),
        Index(value = ["isMemory"], name = "idx_photos_is_memory"),
        Index(value = ["latitude", "longitude"], name = "idx_photos_location")
    ]
)
data class PhotoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // --- 核心文件与分类 ---
    val uri: String,                      // 文件的 Content URI 或绝对路径
    val creationTimestamp: Long,          // 创建时间戳，用于排序
    val isMemory: Boolean = false,        // 是否为“雁宝记忆”照片，核心索引字段
    val isFeatured: Boolean = false,      // 是否为“推荐”照片
    val gitHash: String? = null,          // 对应 Git 仓库中参数包的 commit hash，用于同步状态

    // --- 雁宝记忆参数包 ---
    val memoryParamsJson: String? = null, // 完整的参数 JSON 字符串 (相机+编辑+IP)
    val parentMemoryId: Long? = null,     // “记忆分支”逻辑，指向父“记忆点”的 ID

    // --- AI 评分与标签 ---
    val aiScore: Float? = null,           // AI 综合评分 (0-100)
    val aiTags: List<String>? = null,     // AI 识别的标签，如 ["大师质感", "2.9D佳作", "人像"]

    // --- LBS 地理标记 ---
    val latitude: Double? = null,         // 纬度
    val longitude: Double? = null,        // 经度
    val locationName: String? = null      // 反向地理编码的具体地点名称
)
```

### 字段设计解析：

| 字段 | 类型 | 作用 | 性能考量 |
| :--- | :--- | :--- | :--- |
| `id` | Long | 主键 | 自动增长，确保唯一性 |
| `uri` | String | 文件路径 | 快速定位文件资源 |
| `creationTimestamp` | Long | 创建时间 | **创建索引**，用于默认时间排序，保证相册加载流畅 |
| `isMemory` | Boolean | **核心分类** | **创建索引**，高效筛选“雁宝记忆”分类，实现秒级切换 |
| `isFeatured` | Boolean | 推荐分类 | 可按需创建索引，用于“推荐”流 |
| `gitHash` | String | 同步状态 | 快速比对本地与云端参数版本，显示同步图标 |
| `memoryParamsJson` | String | **参数灵魂** | 存储完整的 JSON 参数包，不直接用于查询，避免性能损耗 |
| `parentMemoryId` | Long | **记忆分支** | 通过外键关联实现“记忆”的版本链，可追溯编辑历史 |
| `aiScore` / `aiTags` | Float / List | AI 标签 | 用于“推荐”分类的筛选和展示 |
| `latitude` / `longitude` | Double | LBS | **创建复合索引**，为未来 LBS 功能（如“足迹地图”）提供高效查询基础 |

---

## 二、 满血审查指令 — 官方回答

### Q1：数据一致性（删除“雁宝记忆”照片的同步策略）

**策略：双重确认下的逻辑删除，参数包默认保留。**

当用户在相册中删除一张“雁宝记忆”照片时，系统并不会立即执行物理删除，而是采用更安全、更灵活的逻辑：

1.  **本地逻辑删除**：在 Room 数据库中，该 `PhotoEntity` 会被标记为 `isDeleted = true`（需在 Entity 中增加此字段），但记录本身和其 `memoryParamsJson` 字段会保留。此时，照片将从所有相册视图中消失。
2.  **Git 参数包保留**：云端 Git 仓库中的参数包 JSON 文件**不会被删除**。这是基于“记忆即资产”的核心理念，用户的参数配置是宝贵的创作数据，不应因误删照片而永久丢失。
3.  **提供“最近删除”功能**：相册模块将提供一个“最近删除”入口，用户可以在 30 天内恢复被逻辑删除的照片。恢复时，只需将 `isDeleted` 标记改回 `false` 即可。
4.  **彻底删除与同步**：只有当用户在“最近删除”中再次执行“彻底删除”操作时，系统才会：
    *   从本地文件系统删除照片物理文件。
    *   从 Room 数据库中删除对应的 `PhotoEntity` 记录。
    *   **触发一个 Git commit，将云端的参数包 JSON 文件删除**，并附上明确的 commit message `chore(params): Remove memory package for deleted photo [photo_id]`。

这种策略在保证数据安全和用户体验之间取得了最佳平衡，避免了误操作导致的核心参数资产丢失。

### Q2：加载性能（“雁宝记忆”分类的性能优化）

**核心策略：索引优化 + 预加载 + 异步解析。**

保证“雁宝记忆”分类在照片数量过千时依然流畅，将采用以下组合拳：

1.  **数据库索引优化**：如 `PhotoEntity` 定义所示，已在 `isMemory` 字段上建立了**数据库索引 (`idx_photos_is_memory`)**。当用户切换到“雁宝记忆”分类时，数据库查询 `WHERE isMemory = true` 的速度是毫秒级的，无论总照片数是多少，都能瞬间筛选出所有符合条件的记录。
2.  **分页加载 (Paging)**：相册网格将使用 Android Jetpack Paging 3 库。这意味着系统一次只从数据库加载一屏（例如 20-30 张）照片的 `PhotoEntity` 对象，而不是一次性加载全部。当用户向下滑动时，Paging 库会自动在后台加载下一页数据，实现无限滚动的流畅体验。
3.  **异步参数解析与缓存**：
    *   **不直接加载 JSON**：在网格视图加载时，仅查询 `id`, `uri` 等轻量级字段用于显示缩略图。`memoryParamsJson` 这个可能很大的字符串**不会被加载**。
    *   **点击时异步解析**：只有当用户点击照片角落的“雁宝标识”时，系统才会根据 `id` 发起一次新的数据库查询，仅获取该照片的 `memoryParamsJson` 字段，并在后台线程 (IO Dispatcher) 中使用 `Gson` 或 `Moshi` 库进行异步解析。
    *   **解析结果缓存**：解析后的参数对象会立即被放入一个 `LruCache` 中，以照片 `id` 为键。如果用户短时间内反复点击同一张照片的参数预览，将直接从内存缓存中读取，避免重复的 JSON 解析开销。

通过“**索引秒筛 -> 分页加载 -> 异步解析 -> 内存缓存**”这一套完整的性能优化方案，可以确保即使用户拥有数万张“雁宝记忆”照片，相册的滑动和参数预览交互依然如丝般顺滑。

### Q3：检索逻辑（相机模块反向调用参数的接口）

**方案：基于 Content Provider 和自定义 URI 的跨模块数据共享。**

相机模块通过一个 URI 就能拿到完整参数，其底层实现逻辑如下：

1.  **定义 `MemoryContentProvider`**：相册模块将实现一个 `ContentProvider`，专门用于向其他模块安全地暴露“雁宝记忆”的参数。这个 Provider 的 `authority` 将被定义为 `im.sheyan.provider.memory`。

2.  **构建参数 URI**：当用户在相册详情页点击“以此为记忆拍摄”按钮时，系统会构建一个指向特定照片参数的 URI。该 URI 格式如下：

    ```
    content://im.sheyan.provider.memory/params/[photo_id]
    ```

    例如，要获取 ID 为 `123` 的照片参数，URI 就是 `content://im.sheyan.provider.memory/params/123`。

3.  **相机模块发起查询**：
    *   相机模块通过 `Intent` 启动，并接收到这个 URI 字符串。
    *   相机模块使用 `ContentResolver` 对该 URI 发起查询：
        ```kotlin
        val cursor = contentResolver.query(memoryUri, null, null, null, null)
        ```

4.  **Content Provider 响应查询**：
    *   `MemoryContentProvider` 接收到查询请求，从 URI 中解析出 `photo_id` (即 `123`)。
    *   Provider 查询 Room 数据库，找到 `PhotoEntity` 中 ID 为 `123` 的记录。
    *   从记录中提取 `memoryParamsJson` 字符串。
    *   将这个 JSON 字符串作为查询结果返回给相机模块。`Cursor` 中可以只有一列，名为 `"params_json"`。

5.  **相机模块解析并应用**：
    *   相机模块从返回的 `Cursor` 中获取 `memoryParamsJson` 字符串。
    *   使用与编辑模块相同的逻辑，反序列化 JSON，得到完整的参数栈。
    *   将所有参数（大师滤镜、29D、美颜等）应用到相机预览的渲染管线中，实现 1:1 的实况拍摄效果。

这个基于 `ContentProvider` 的方案是 Android 系统标准的跨模块数据共享方式，具有**高安全性**（通过权限控制）、**高解耦性**（相机模块无需知道数据库实现细节）和**高效率**的优点。
