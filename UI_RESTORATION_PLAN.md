# 🎀 雁宝AI相机 - UI 还原规划

## 📋 设计图总览（24张）

| 模块 | 数量 | 优先级 | 状态 |
|-----|------|--------|------|
| 🚀 启动模块 | 1张 | P0 | ⏳ 待开始 |
| 🏠 首页模块 | 1张 | P1 | ⏳ 待开始 |
| 📷 相机模块 | 10张 | P0 | ⏳ 待开始 |
| ✏️ 编辑模块 | 1张 | P2 | ⏳ 待开始 |
| 🖼️ 相册模块 | 3张 | P1 | ⏳ 待开始 |
| ✨ 推荐模块 | 6张 | P2 | ⏳ 待开始 |
| 👤 个人资料模块 | 2张 | P2 | ⏳ 待开始 |

---

## 🎯 Phase 1 目标（当前阶段）

**核心目标**: 完成 P0 优先级模块（启动 + 相机）

### 1. 启动模块（1张）

**设计图**: `01_splash/splash_screen.png`

**还原要点**:
- ✅ 粉紫渐变背景 (#A78BFA → #EC4899 → #F9A8D4)
- ✅ 库洛米角色居中（黑色兔子 + 粉色蝴蝶结）
- ✅ "Yanbao Camera" 白色标题
- ✅ 粉色渐变进度条
- ✅ 动画效果：Logo缩放 + 文字淡入 + 进度条加载
- ✅ 3秒后自动跳转到首页

**技术实现**:
- SplashActivity.kt
- Compose 动画（AnimatedVisibility, animateFloatAsState）
- LaunchedEffect 延时跳转

---

### 2. 相机模块（10张）

#### 2.1 相机主界面

**设计图**: `03_camera/01_camera_main.png`

**页面结构**:
```
┌─────────────────────────────┐
│ 🔦 ⚙️ 🔄                    │ 顶部控制栏
├─────────────────────────────┤
│                             │
│   相机预览区                │
│   （实时取景）              │
│                             │
├─────────────────────────────┤
│ [◀ 相册] [📷 拍照] [▶ 模式] │ 底部操作栏
└─────────────────────────────┘
```

**还原要点**:
- ✅ 顶部控制栏：闪光灯、设置、翻转摄像头
- ✅ 中央预览区：CameraX 实时预览
- ✅ 底部操作栏：相册缩略图、拍照按钮（粉紫渐变 + 呼吸光晕）、模式切换
- ✅ 库洛米装饰（四个角落，15% 透明度）

**技术实现**:
- CameraScreen.kt
- CameraX PreviewView
- Compose Canvas 绘制拍照按钮光晕
- rememberInfiniteTransition 实现呼吸动画

---

#### 2.2 拍照模式菜单

**设计图**: `03_camera/02_camera_modes.png`

**模式列表**（左右滑动切换）:
- 标准模式
- 人像模式
- 夜景模式
- 视频模式
- 专业模式

**还原要点**:
- ✅ LazyRow 水平滚动
- ✅ 选中模式高亮（文字变大 + 粉色阴影）
- ✅ 毛玻璃效果背景

**技术实现**:
- CameraModesDialog.kt
- LazyRow + animateScrollToItem
- Modifier.graphicsLayer 实现缩放动画

---

#### 2.3 美颜模式

**设计图**: `03_camera/03_camera_beauty.png`

**功能**:
- 磨皮、瘦脸、大眼、高鼻梁等美颜参数
- 实时预览效果
- 参数调节滑块

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 6个美颜参数滑块（0-100）
- ✅ 实时预览（Phase 3 实现 OpenGL Shader）
- ✅ 参数变化时 Logcat 输出日志

**技术实现**:
- BeautyModeOverlay.kt
- ModalBottomSheet
- Slider + onValueChange
- StateFlow 管理参数状态

---

#### 2.4 2.9D景深模式

**设计图**: `03_camera/04_camera_29d.png`

**功能**:
- 29个参数调节（亮度、对比度、饱和度等）
- 实时预览效果
- 4个页签分类（基础、阴影、颜色、特效）

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 4个页签分类
- ✅ 29个参数滑块
- ✅ 实时预览（Phase 3 实现 OpenGL Shader）
- ✅ 参数变化时 Logcat 输出日志
- ✅ 陀螺仪数据采集（Phase 3 实现）

**技术实现**:
- TwoDotNineDOverlay.kt
- TabRow + HorizontalPager
- Camera29DState + StateFlow
- SensorEventListener（Phase 3）

---

#### 2.5 AR特效模式

**设计图**: `03_camera/05_camera_ar.png`

**功能**:
- AR贴纸、滤镜、变脸等特效
- 特效分类选择
- 实时预览

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 特效分类（贴纸、滤镜、变脸）
- ✅ 特效缩略图网格
- ✅ 实时预览（Phase 3 实现 ARCore）

**技术实现**:
- ARModeOverlay.kt
- LazyVerticalGrid
- ARCore（Phase 3）

---

#### 2.6 iPhone模拟模式

**设计图**: `03_camera/06_camera_iphone.png`

**功能**:
- 模拟iPhone拍照效果
- 色彩风格选择
- 参数调节

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 色彩风格选择（自然、鲜艳、冷色调、暖色调）
- ✅ 参数调节滑块

**技术实现**:
- IPhoneModeOverlay.kt
- ColorMatrix 色彩调整

---

#### 2.7 雁宝记忆模式

**设计图**: `03_camera/07_camera_memory.png`

**功能**:
- 套用历史拍摄参数
- 快速应用之前的设置
- 参数历史记录

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 历史记录列表（显示时间、地点、参数预览）
- ✅ 点击历史记录 → 应用参数到当前相机
- ✅ 从 YanbaoMemory 数据库读取历史记录

**技术实现**:
- MemoryModeOverlay.kt
- YanbaoMemoryDao.getAllMemories()
- Camera29DState.fromJson()

---

#### 2.8 拍照预览

**设计图**: `03_camera/08_camera_preview.png`

**页面结构**:
```
┌─────────────────────────────┐
│ ◀ 返回                      │
├─────────────────────────────┤
│   照片大图预览              │
├─────────────────────────────┤
│ [❌ 删除] [✏️ 编辑] [✅ 保存] │
└─────────────────────────────┘
```

**还原要点**:
- ✅ 全屏照片预览
- ✅ 顶部返回按钮
- ✅ 底部操作栏（删除、编辑、保存）

**技术实现**:
- PhotoPreviewScreen.kt
- Coil AsyncImage
- 删除 → 删除文件 + 数据库记录
- 编辑 → 跳转到编辑模块
- 保存 → 保存到 MediaStore

---

#### 2.9 视频录制

**设计图**: `03_camera/09_camera_video.png`

**功能**:
- 视频录制按钮
- 录制时长显示
- 视频质量选择

**还原要点**:
- ✅ 录制按钮（红色圆形 + 呼吸动画）
- ✅ 录制时长显示（00:00）
- ✅ 视频质量选择（720p, 1080p, 4K）

**技术实现**:
- VideoRecordScreen.kt
- CameraX VideoCapture
- Timer 显示录制时长

---

#### 2.10 滤镜选择

**设计图**: `03_camera/10_camera_filters.png`

**功能**:
- 实时滤镜预览
- 滤镜分类（风景、人像、复古等）
- 滤镜强度调节

**还原要点**:
- ✅ 底部抽屉式面板（向上滑出）
- ✅ 滤镜分类选择
- ✅ 滤镜缩略图网格
- ✅ 滤镜强度滑块（0-100）

**技术实现**:
- FilterSelectionOverlay.kt
- LazyVerticalGrid
- ColorMatrix 滤镜效果

---

## 🚀 Phase 2 目标（下一阶段）

**核心目标**: 完成 P1 优先级模块（首页 + 相册）

### 1. 首页模块（1张）

**设计图**: `02_home/home_main.png`

**还原要点**:
- 搜索栏（毛玻璃效果）
- 推荐内容卡片流
- 底部导航栏（5个标签）

---

### 2. 相册模块（3张）

**设计图**:
- `05_gallery/01_gallery_main.png`
- `05_gallery/02_gallery_albums.png`
- `05_gallery/03_gallery_preview.png`

**还原要点**:
- 2x4网格布局
- 相册分类
- 照片详情

---

## 🚀 Phase 3 目标（最后阶段）

**核心目标**: 完成 P2 优先级模块（编辑 + 推荐 + 个人资料）

### 1. 编辑模块（1张）

**设计图**: `04_edit/edit_main.png`

**还原要点**:
- 3层嵌套结构（功能 → 分类 → 选项）
- 9个功能按钮
- 91个专业滤镜

---

### 2. 推荐模块（6张）

**设计图**:
- `06_recommend/01_recommend_main.png`
- `06_recommend/02_recommend_map.png`
- `06_recommend/03_recommend_notification.png`
- `06_recommend/04_recommend_filter.png`
- `06_recommend/05_recommend_camera.png`
- `06_recommend/06_recommend_list.png`

**还原要点**:
- AI推荐位置
- 地图视图
- 筛选选项

---

### 3. 个人资料模块（2张）

**设计图**:
- `07_profile/01_profile_main.png`
- `07_profile/02_profile_settings.png`

**还原要点**:
- 用户信息
- 我的作品
- 设置

---

## 📊 进度追踪

| 模块 | 设计图 | 状态 | 完成时间 |
|-----|-------|------|---------|
| 启动模块 | splash_screen.png | ⏳ 待开始 | - |
| 相机主界面 | 01_camera_main.png | ⏳ 待开始 | - |
| 拍照模式菜单 | 02_camera_modes.png | ⏳ 待开始 | - |
| 美颜模式 | 03_camera_beauty.png | ⏳ 待开始 | - |
| 2.9D景深模式 | 04_camera_29d.png | ⏳ 待开始 | - |
| AR特效模式 | 05_camera_ar.png | ⏳ 待开始 | - |
| iPhone模拟模式 | 06_camera_iphone.png | ⏳ 待开始 | - |
| 雁宝记忆模式 | 07_camera_memory.png | ⏳ 待开始 | - |
| 拍照预览 | 08_camera_preview.png | ⏳ 待开始 | - |
| 视频录制 | 09_camera_video.png | ⏳ 待开始 | - |
| 滤镜选择 | 10_camera_filters.png | ⏳ 待开始 | - |

---

**更新时间**: 2026-02-20 22:30 CST
