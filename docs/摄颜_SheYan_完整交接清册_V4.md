# 摄颜（SheYan）AI 摄影 App — 完整交接清册 V4.0

**版本：** V4.0  
**日期：** 2026-02-27  
**仓库：** [Tsaojason-cao/YanbaoCamera-Android](https://github.com/Tsaojason-cao/YanbaoCamera-Android)  
**最新提交：** `905da69`

---

## 一、项目概览

摄颜（SheYan）是一款以「雁宝」IP 为核心的 AI 摄影 App，主打 AI 参数复刻、AR 姿势对齐、胡萝卜花园养成等差异化功能。技术栈为 **Android + Jetpack Compose + Hilt + Room + Camera2**，UI 风格为纯黑暗色调（#0A0A0A）+ 胡萝卜橙（#F97316）+ 品牌粉（#EC4899）。

---

## 二、设计规范（全模块统一标准）

### 2.1 颜色体系

| 名称 | 色值 | 用途 |
|------|------|------|
| 曜石黑 OBSIDIAN_BLACK | `#0A0A0A` | 全局背景 |
| 胡萝卜橙 CARROT_ORANGE | `#F97316` | 主按钮、中间大圆按钮、强调色 |
| 品牌粉 BRAND_PINK | `#EC4899` | 霓虹边框、激活状态、雁宝 IP 配色 |
| 深灰卡片 | `#1A1A1A` | 卡片背景 |
| 白色文字 | `#FFFFFF` | 主文字 |
| 灰色副文字 | `#9CA3AF` | 次要文字 |

### 2.2 底部导航标准（全模块统一，不可更改）

```
首页 | 相机 | 🥕胡萝卜橙大圆按钮（56dp，相机入口） | 相册 | 推荐 | 我的
```

- **无状态栏**（无手机时间/信号/电量显示）
- **无手机边框**（纯 App 界面）
- 中间大圆按钮：胡萝卜橙渐变（`#FF8C00 → #F97316`），阴影发光效果
- 激活 Tab：粉色（`#EC4899`）图标 + 粉色下划线

### 2.3 雁宝 IP 标准形象

- 黑色小兔子身体
- **超大粉色蝴蝶结**（头顶，比例夸张）
- 黑色 JK 水手服（深蓝色百褶裙 + 粉色领结）
- 圆形大眼睛，腮红粉色
- **严禁自由发挥**：不得使用其他兔子形象替代

---

## 三、模块清单与设计图对照

### M1 — 启动与引导（12 张）

| 文件名 | 内容 |
|--------|------|
| LAUNCH_M1_01_splash.png | 启动页：雁宝全身 + 「摄颜」标题 + 胡萝卜橙光晕 |
| LAUNCH_M1_02_onboard_1.png | 引导页1：AI 参数复刻功能介绍 |
| LAUNCH_M1_03_onboard_2.png | 引导页2：AR 姿势对齐功能介绍 |
| LAUNCH_M1_04_onboard_3.png | 引导页3：雁宝花园养成功能介绍 |
| LAUNCH_M1_05_login.png | 登录页：手机号 + 微信/Apple 登录 |
| LAUNCH_M1_06_register.png | 注册页：基本信息填写 |
| LAUNCH_M1_07_permission.png | 权限申请页：相机/相册/位置 |
| LAUNCH_M1_08_profile_setup.png | 个人资料设置 |
| LAUNCH_M1_09_style_select.png | 摄影风格偏好选择 |
| LAUNCH_M1_10_follow_suggest.png | 推荐关注页 |
| LAUNCH_M1_11_complete.png | 注册完成 + 雁宝欢迎动画 |
| LAUNCH_M1_12_flow_diagram.png | M1 完整流程图 |

### M2 — 首页（5 张）

| 文件名 | 内容 |
|--------|------|
| HOME_M2_01_main.png | 首页主界面：横向滚动推荐卡片 + 雁宝打招呼 |
| HOME_M2_02_ai_suggest.png | AI 今日推荐：根据天气/时间/位置推荐拍摄场景 |
| HOME_M2_03_trending.png | 热门话题 + 挑战赛入口 |
| HOME_M2_04_nearby.png | 附近摄影师 + 热门机位 |
| HOME_M2_05_flow_diagram.png | M2 完整流程图 |

### M3 — 相机（9 张）

| 文件名 | 内容 |
|--------|------|
| CAM_M3_01_basic.png | 相机主界面：取景框 + 快门 + 模式切换 |
| CAM_M3_02_ai_mode.png | AI 模式：实时参数建议叠加层 |
| CAM_M3_03_ar_align.png | AR 姿势对齐：半透明参考图叠加 |
| CAM_M3_04_pose_guide.png | 姿势引导：骨骼点检测 + 箭头提示 |
| CAM_M3_05_params_panel.png | 参数面板：ISO/快门/白平衡等 |
| CAM_M3_06_filter_preview.png | 滤镜预览：底部横向滚动 |
| CAM_M3_07_timer.png | 定时拍摄：3s/5s/10s 倒计时 |
| CAM_M3_08_burst.png | 连拍模式 |
| CAM_M3_09_flow_diagram.png | M3 完整流程图 |

### M4 — 编辑器（15 张）

| 文件名 | 内容 |
|--------|------|
| EDIT_M4_01_main.png | 编辑器主界面：工具列表 3行×8个 |
| EDIT_M4_02_adjust.png | 调整工具：曝光/对比度/饱和度滑块 |
| EDIT_M4_03_filter.png | 滤镜选择：雁宝风格滤镜 |
| EDIT_M4_04_beauty.png | 美颜工具：磨皮/瘦脸/大眼 |
| EDIT_M4_05_crop.png | 裁剪：多比例选择 |
| EDIT_M4_06_text.png | 文字工具：字体/颜色/样式 |
| EDIT_M4_07_watermark.png | 水印：蝴蝶结水印/雁宝水印 |
| EDIT_M4_08_ai_enhance.png | AI 超分辨率增强 |
| EDIT_M4_09_sticker.png | 雁宝贴纸：10款 IP 变体（日常/酷炫/害羞/兴奋/摄影/爱心/思考/运动/节日/睡衣） |
| EDIT_M4_10_local_adjust.png | 局部调色：画笔选区 |
| EDIT_M4_11_remove_object.png | 消除笔：AI 物体消除 |
| EDIT_M4_12_perspective.png | 透视矫正 |
| EDIT_M4_13_compare.png | 前后对比滑动 |
| EDIT_M4_14_export.png | 导出设置：格式/质量/尺寸 |
| EDIT_M4_15_flow_diagram.png | M4 完整流程图 |

**M4 工具列表（严格对标 M4_01 设计图）：**

```
第1行：曝光 | 对比度 | 饱和度 | 亮度 | 高光 | 阴影 | 色温 | 色调
第2行：色调 | 锐化   | 颗粒   | 暗角 | 裁剪 | 透视 | 消除 | 螺旋
第3行：消除笔 | 局部调色 | 贴纸 | 水印 | AI超分
```

### M5 — 相册（11 张）

| 文件名 | 内容 |
|--------|------|
| GAL_M5_01_main_grid.png | 相册主界面：3列网格，Tab「雁宝记忆/推荐/一般」 |
| GAL_M5_02_memory_detail.png | 雁宝记忆详情：参数信息卡片 |
| GAL_M5_03_photo_detail.png | 照片详情：全屏查看 + 操作栏 |
| GAL_M5_04_featured_feed.png | 精选 Feed：瀑布流布局 |
| GAL_M5_05_search.png | 搜索：按场景/参数/时间筛选 |
| GAL_M5_06_album_manage.png | 相册管理：新建/删除/排序 |
| GAL_M5_07_multi_select.png | 多选模式：批量操作 |
| GAL_M5_08_share.png | 分享：多平台分享选项 |
| GAL_M5_09_export.png | 导出：格式/质量选择 |
| GAL_M5_10_memory_create.png | 创建雁宝记忆：添加参数/标签 |
| GAL_M5_11_flow_diagram.png | M5 完整流程图 |

**M5 Tab 标准：** 雁宝记忆（默认激活）/ 推荐 / 一般

### M6 — 推荐（10 张，V2 版）

| 文件名 | 内容 |
|--------|------|
| REC_M6_01_main_feed.png | 推荐主 Feed：TikTok 式全屏照片卡片 |
| REC_M6_02_get_params_popup.png | 「一键 Get 同款参数」弹窗 |
| REC_M6_03_ar_camera_align.png | AR 相机对齐界面 |
| REC_M6_04_pose_aligned_success.png | 姿势对齐成功提示 |
| REC_M6_05_comments.png | 评论区 |
| REC_M6_06_lbs_map_bubble.png | LBS 地图气泡 |
| REC_M6_07_following_feed.png | 关注 Feed |
| REC_M6_08_flow_diagram.png | M6 完整流程图 |
| REC_M6_09_nearby_discover.png | 附近发现 |
| REC_M6_10_publish_share.png | 发布/分享 |

**M6 布局标准（严格对标 REC_M6_01 设计图）：**
- 顶部：「推荐」标题 + 搜索🔍 + 筛选▽
- Tab 栏：推荐 / 关注 / 附近（粉色激活下划线）
- 主内容：VerticalPager 全屏照片卡片（粉色霓虹边框）
- 右侧操作栏：熊掌赞（🐾）/ 评论 / 分享 / 收藏
- 底部：「🐾 一键 Get 同款参数」胡萝卜橙大按钮

### M7 — 我的（10 张，V2 版）

| 文件名 | 内容 |
|--------|------|
| MY_M7_01_profile_main.png | 个人主页：头像+统计+雁宝花园+照片网格 |
| MY_M7_02_yanbao_garden.png | 雁宝花园详情：胡萝卜种植/收获 |
| MY_M7_03_feeding_success.png | 喂雁宝成功：动画 + 积分奖励 |
| MY_M7_04_edit_profile.png | 编辑个人资料 |
| MY_M7_05_carrot_empty_guide.png | 胡萝卜空状态引导 |
| MY_M7_06_other_profile.png | 他人主页 |
| MY_M7_07_settings.png | 设置页 |
| MY_M7_08_membership_growth.png | 会员成长体系 |
| MY_M7_09_notifications.png | 通知中心 |
| MY_M7_10_full_system_flow.png | M7 完整流程图 |

**M7 布局标准（严格对标 MY_M7_01 设计图）：**
- 顶部标题栏：⚙️设置 / 「我的」 / 分享图标
- 头像：圆形，粉色霓虹发光边框（双层）
- 用户名：「春日摄影小桃」+ 「雁宝会员 Lv.3」粉色标签
- 统计数据：作品 128 / 关注 256 / 粉丝 1.2k
- 雁宝花园卡片：绿色草地背景 + 雁宝站立 + 胡萝卜×12 + 「喂雁宝」橙色大按钮
- 照片网格：3列，粉色霓虹边框选中态

---

## 四、雁宝 IP 图标集（10 款）

| 文件名 | 款式 |
|--------|------|
| yanbao_icon_01_normal.png | 日常款（标准站姿） |
| yanbao_icon_02_cool.png | 酷炫款（墨镜） |
| yanbao_icon_03_shy.png | 害羞款（遮脸） |
| yanbao_icon_04_excited.png | 兴奋款（举手跳跃） |
| yanbao_icon_05_photographer.png | 摄影款（拿相机） |
| yanbao_icon_06_heart.png | 爱心款（捧心） |
| yanbao_icon_07_think.png | 思考款（托腮） |
| yanbao_icon_08_sport.png | 运动款（运动服） |
| yanbao_icon_09_festival.png | 节日款（彩带/礼物） |
| yanbao_icon_10_sleepy.png | 睡衣款（睡帽/抱枕） |

---

## 五、Android 代码结构

```
YanbaoCamera-Android/
├── app/src/main/java/com/yanbao/camera/
│   ├── presentation/
│   │   ├── YanbaoApp.kt              ← 主导航 + 底部导航（5 tab）
│   │   ├── splash/SplashActivity.kt  ← 启动页（雁宝 IP 动画）
│   │   ├── home/HomeScreen.kt        ← M2 首页
│   │   ├── camera/CameraScreen.kt    ← M3 相机（Camera2 全屏）
│   │   ├── editor/EditorScreen.kt    ← M4 编辑器（工具列表 3行×8个）
│   │   ├── gallery/
│   │   │   ├── GalleryScreen.kt      ← M5 相册（Tab: 雁宝记忆/推荐/一般）
│   │   │   └── GalleryViewModel.kt   ← MediaStore + Room 双路扫描
│   │   ├── recommend/
│   │   │   └── RecommendScreen.kt    ← M6 推荐（TikTok 式全屏 Feed）
│   │   └── profile/ProfileScreen.kt  ← M7 我的（雁宝花园 + 照片网格）
│   ├── data/
│   │   ├── local/dao/                ← Room DAO（雁宝记忆、照片元数据）
│   │   └── database/AppDatabase.kt   ← Room 数据库
│   └── core/
│       ├── util/YanbaoExifParser.kt  ← Exif 参数读取
│       └── util/GitBackupManager.kt  ← Git 自动备份
├── app/src/main/res/drawable/        ← 所有图标资源（ic_yanbao_*.xml）
└── docs/ui-designs/                  ← 全套设计图（M1-M7 + IP 图标集）
```

---

## 六、本次修正记录（V4 vs V3）

| 修正项 | 问题描述 | 修正内容 | 提交 |
|--------|----------|----------|------|
| 底部导航 | 旧版有 6 个 tab（含编辑），中间无大圆按钮 | 改为 5 tab，去掉编辑，中间改为胡萝卜橙大圆按钮 | `42e1c02` |
| M6 推荐页 | 旧版是 LBS 地图机位推荐，有状态栏 | 完全重写为 TikTok 式全屏照片 Feed，无状态栏 | `42e1c02` |
| M7 个人页 | 统计数据是「作品/记忆/获赞」，花园卡片样式不对 | 改为「作品/关注/粉丝」，花园卡片改为绿色草地场景 | `42e1c02` |
| M4 编辑器工具列表 | 工具分类不对标设计图 | 改为 3行×8个，严格对标 M4_01 设计图 | `905da69` |
| M5 相册 Tab | Tab 是「全部/雁宝记忆/推荐LBS」 | 改为「雁宝记忆/推荐/一般」，标题改为「摄颜相册」 | `905da69` |
| M4_09 贴纸 IP | 雁宝 IP 形象不正确（无大蝴蝶结） | 重新生成，严格使用黑兔+大粉色蝴蝶结+黑色JK水手服 | `60de464` |
| 雁宝 IP 图标集 | 缺少多款变体图标 | 新增 10 款独立变体图标 | `60de464` |

---

## 七、Git 提交记录

```
905da69  fix(ui): 1:1 对标 M4/M5/M6/M7 设计图修正
42e1c02  fix: 1:1 对标设计图修正导航和核心屏幕
60de464  feat(ui): M6/M7 v2 redesign + Yanbao IP icons + M4_09 sticker fix
06303ba  docs: Add M1-M7 complete parameter flow diagram (final version)
1a176c8  feat(M7): Add SheYan M7 MyProfile Module - Full UI Designs
```

---

## 八、下一步建议

1. **编译验证**：在 Android Studio 中打开 `YanbaoCamera-Android` 项目，执行 `Build > Make Project` 验证无编译错误
2. **UI 走查**：在模拟器/真机上逐屏对照设计图，记录差异并反馈
3. **Stitch 前端**：如需 Web 原型，在 [stitch.withgoogle.com](https://stitch.withgoogle.com) 上传设计图 PNG，配合 Prompt 描述生成 HTML/CSS 代码，然后通过「汇出 → .zip」下载（需在本地浏览器操作，沙盒环境无法触发文件下载）
4. **GitHub Actions**：可配置 CI/CD 工作流，推送后自动编译 Debug APK

---

*本文档由 Manus AI 生成，版本 V4.0，2026-02-27*
