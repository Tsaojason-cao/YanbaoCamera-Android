# SheYan M6 推荐模块满血版 — 完整交接清册

---

**项目负责人:** Manus AI
**交付日期:** 2026-02-26
**Git Commit:** `feat(M6): Add SheYan M6 Discovery Module - Full UI Designs (10 screens) + DiscoveryPacket Spec + Tech Audit`

---

## 一、 满血审查指令 — 官方答复

### Q1: 参数安全性 (传输与映射)

所有 `DiscoveryPacket` 的下载通过 **HTTPS** 进行。每个 JSON 包在服务器下发时，使用服务器端密钥进行 **HMAC-SHA256 签名**，签名附加于响应头。客户端收到数据包后，本地计算签名并与响应头比对，**签名不匹配的数据包直接丢弃**，杜绝中间人攻击。参数包中的 `filterId` 等键均为版本化字符串，App 本地维护**参数映射表**，遇到无法识别的 ID 时优雅降级，确保跨版本兼容性。

### Q2: AR 性能 (CPU/GPU 优化)

推荐流中的 2.9D 效果采用**预渲染短视频**，而非实时 3D 渲染，确保滑动流畅。跳转相机后采用**分层渐进式加载**：第一帧（0-50ms）应用色彩调整与 LUT 滤镜；第二阶段（50-150ms）叠加轻量级 AR 姿势虚影；第三阶段（150ms+）加载 29D 渲染等重度效果。用户拖动手机时，重度效果临时降采样，停止移动后恢复完整精度，防止发热掉帧。

### Q3: LBS 深度 (机位共享)

我们记录完整的**6DoF 空间位姿**，而非仅 GPS 坐标。拍摄瞬间通过 ARCore/ARKit 或陀螺仪+加速度计，捕获 **Pitch（俯仰角）、Roll（翻滚角）、Yaw（偏航角）**，以欧拉角形式存入 `devicePose` 对象。其他用户开启相机后，AR 引擎实时比较当前姿态与目标 `devicePose` 的差异，通过箭头和颜色变化引导用户调整手机角度，直至完全匹配，实现完美机位复刻。

---

## 二、 DiscoveryPacket JSON 结构定义

```json
{
  "packetId": "uuid-string-1234-abcd-5678",
  "authorId": "user-id-string",
  "authorUsername": "作者昵称",
  "mediaUrl": "https://.../media.mp4",
  "mediaType": "video",
  "yanbaoMemoryParams": {
    "filterId": "master_film_075",
    "filterIntensity": 0.8,
    "adjustments": { "exposure": 0.15, "contrast": 0.2 },
    "beautyParams": { "skinSmoothing": 0.6 },
    "render29D": { "photonBounce": 0.65 }
  },
  "devicePose": {
    "pitch": -15.5,
    "roll": 2.1,
    "yaw": 120.3
  },
  "location": {
    "latitude": 34.9956,
    "longitude": 135.782,
    "name": "京都, 日本庭园"
  },
  "poseTemplate": {
    "type": "2d_skeleton_keypoints",
    "url": "https://.../pose_data.json"
  }
}
```

---

## 三、 全套 UI 设计图（10 张，纯 UI 无手机框）

| # | 文件名 | 界面/功能 |
|---|---|---|
| 01 | `REC_M6_01_main_feed.png` | 推荐主信息流（全屏竖屏流 + 熊掌点赞 + 一键Get）|
| 02 | `REC_M6_02_get_params_popup.png` | 雁宝参数包弹窗（HMAC验证 + 机位信息 + 套用按钮）|
| 03 | `REC_M6_03_ar_camera_align.png` | AR 机位对齐相机（骨架引导 + 角度指示 + 胡萝卜滑块）|
| 04 | `REC_M6_04_pose_aligned_success.png` | 机位对齐成功 + 社区回流提示 |
| 05 | `REC_M6_05_comments.png` | 评论区（雁宝记忆用户标识 + 作者回复 + 附图评论）|
| 06 | `REC_M6_06_lbs_map_bubble.png` | LBS 机位地图气泡（暗色地图 + 坐标 + 导航按钮）|
| 07 | `REC_M6_07_following_feed.png` | 关注用户信息流（Story圈 + 卡片流 + Get同款）|
| 08 | `REC_M6_08_flow_diagram.png` | 三位一体灵感包与社区闭环逻辑横版图 |
| 09 | `REC_M6_09_nearby_discover.png` | 同城发现（热门地点 + 瀑布流 + Get同款）|
| 10 | `REC_M6_10_publish_share.png` | 发布作品（对比原作 + 记忆链 + 参数包开关）|

---

## 四、 模块业务流程

**分发逻辑：** 优先推送带有完整参数包和 LBS 信息的"雁宝记忆"精选照片，AI 算法基于用户历史偏好和地理位置进行个性化排序。

**消费逻辑：** 用户点击"一键Get同款" → 下载并验证 DiscoveryPacket → 调用相机模块 → 实时加载参数 → 开启 AR 姿势虚影辅助 → 机位对齐。

**回流逻辑：** 拍摄完成后，系统提示"你已复刻 [作者名] 的作品，是否分享你的版本？"，用户发布时自动继承参数链，形成社区闭环。
