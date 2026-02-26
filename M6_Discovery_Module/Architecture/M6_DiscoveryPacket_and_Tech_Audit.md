# SheYan M6 推荐模块 — DiscoveryPacket 结构与技术审计

---

## 一、 DiscoveryPacket 数据包 JSON 结构定义

为了实现“灵感一键提取”功能，每一条推荐内容都将绑定一个结构化的 `DiscoveryPacket`。该数据包采用 JSON 格式，其核心字段定义如下：

```json
{
  "packetId": "uuid-string-1234-abcd-5678",
  "authorId": "user-id-string",
  "authorUsername": "作者昵称",
  "authorAvatarUrl": "https://.../avatar.png",
  "mediaUrl": "https://.../media.mp4",
  "mediaType": "video", // "image" or "video"
  "description": "今天在京都拍的樱花大片，快来 Get 同款参数和机位！🌸",
  "timestamp": 1677400000,
  "stats": {
    "likes": 1280,
    "comments": 245,
    "shares": 560
  },
  "yanbaoMemoryParams": {
    // 这里完整内嵌来自 M4/M5 的“雁宝记忆”参数包 JSON 对象
    "filterId": "master_film_075",
    "filterIntensity": 0.8,
    "adjustments": {
      "exposure": 0.15,
      "contrast": 0.2
    },
    "beautyParams": {
      "skinSmoothing": 0.6
    },
    "render29D": {
      "photonBounce": 0.65
    }
  },
  "devicePose": {
    "pitch": -15.5, // 俯仰角 (度)
    "roll": 2.1,    // 翻滚角 (度)
    "yaw": 120.3   // 偏航角 (度)
  },
  "location": {
    "latitude": 34.9956,   // 纬度
    "longitude": 135.782,  // 经度
    "name": "京都, 日本庭园"
  },
  "poseTemplate": {
    "type": "2d_skeleton_keypoints",
    "url": "https://.../pose_data.json" // 指向骨架关键点数据的链接
  }
}
```

---

## 二、 满血审查指令 — 技术审计回答

### Q1: 参数安全性 (传输与映射)

**策略：** 采用“签名验证 + 版本化映射”机制，确保数据安全与准确性。

1.  **传输安全与防篡改：** 所有 `DiscoveryPacket` 的下载将通过 **HTTPS** 进行。更重要的是，每个 JSON 包在从服务器下发时，都会使用服务器端的一个密钥（Secret Key）对其内容进行 **HMAC-SHA256 签名**，并将签名附加到响应头中。客户端在收到数据包后，会用相同的密钥和算法在本地计算签名，并与响应头中的签名进行比对。**签名不匹配的数据包将被直接丢弃**，从而彻底杜绝了中间人攻击和数据篡改的风险。

2.  **准确映射：** 参数包中的 `filterId` 和各类调整参数的键（如 `exposure`）都是标准化的、版本化的字符串。App 本地会维护一个**参数映射表（Parameter Mapping Table）**。例如，当收到 `filterId: "master_film_075"` 时，渲染引擎会查询该 ID 对应的本地滤镜实现（如某个具体的 LUT 文件或 Shader）。如果遇到一个无法识别的 ID（可能来自未来版本的 App），系统会优雅地忽略该参数，而不是崩溃。这种机制确保了跨版本兼容性和渲染的准确性。

### Q2: AR 性能 (CPU/GPU 占用优化)

**策略：** 采用“分层渲染 + 渐进式加载”策略，平衡效果与性能。

1.  **推荐流预览优化：** 在推荐信息流中，用户看到的 2.9D 视差效果将是**预渲染的短视频**，而非实时 3D 渲染。这使得滑动浏览体验极为流畅，CPU/GPU 占用几乎为零。

2.  **相机启动优化：** 点击“Get 同款参数”后，跳转相机的过程将采用渐进式加载：
    *   **第一帧 (0-50ms):** 立即应用计算量最小的参数，如 **18 个基础工具的色彩调整**和**滤镜（通过 LUT）**。这些操作在现代 GPU 上几乎是瞬时的。
    *   **第二阶段 (50-150ms):** 加载并叠加 **AR 姿势引导**。姿势虚影是一个轻量级的 2D 矢量图形或透明 PNG，由 GPU 叠加，对性能影响极小。
    *   **第三阶段 (150ms+):** 当相机预览稳定后，再加载并渲染计算密集型效果，如 **29D 渲染**或**复杂的实时美颜算法**。在用户拖动手机调整角度时，这些重度效果可以临时降低采样率或切换到近似模型，一旦用户停止移动，再恢复完整精度渲染，从而保证操作的流畅性，避免发热和掉帧。

### Q3: LBS 深度 (机位共享)

**策略：** 记录并还原完整的**空间位姿 (6DoF)**，而不仅仅是 GPS 坐标。

1.  **数据记录：** 在用户拍摄“雁宝记忆”照片的瞬间，我们不仅会记录 GPS 提供的**经纬度（Latitude, Longitude）**，还会通过 **ARCore/ARKit 的运动追踪传感器（Motion Tracking）** 或直接访问陀螺仪（Gyroscope）和加速度计（Accelerometer），捕获并记录下手机在世界坐标系中的完整空间姿态。这包括：
    *   **位置 (Position):** x, y, z (虽然在当前需求中主要用 GPS)
    *   **姿态 (Orientation):** **Pitch (俯仰角)**, **Roll (翻滚角)**, **Yaw (偏航角)**，以四元数（Quaternion）或欧拉角的形式存储在 `devicePose` 对象中。

2.  **数据还原 (AR 辅助)：** 当其他用户选择“Get 同款机位”并开启相机后：
    *   App 会读取 `devicePose` 中的目标姿态数据。
    *   通过 AR 引擎，在相机预览界面上绘制一个**辅助对齐的 3D 虚影或参考框**（例如，一个半透明的手机模型或一个地平面对齐网格）。
    *   系统会实时比较当前手机的姿态与目标姿态的差异，并通过 UI 元素（如箭头、颜色变化）**实时引导用户调整手机的角度**，直到当前姿态与原作者的 `devicePose` 完全匹配。此时，UI 会提示“机位对齐成功！”，帮助用户完美复刻原作的拍摄视角。
