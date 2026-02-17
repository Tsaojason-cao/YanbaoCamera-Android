# Phase 2: 功能完善 - 实现计划

## 目标
实现相机、编辑、推荐、相册、地图等核心功能，使应用具备专业相机App的能力。

## 时间预期
3-5天

## 实现清单

### 1. CameraX相机集成 (Day 1-2)
- [ ] CameraScreen完整实现
  - 实时相机预览
  - 拍照功能（保存到MediaStore）
  - 前后摄像头切换
  - 闪光灯模式（自动/开/关）
  - 缩放控制（双指捏合）
  
- [ ] 5种拍照模式
  - 自动模式（Auto）
  - 人像模式（Portrait）
  - 风景模式（Landscape）
  - 夜景模式（Night）
  - 视频模式（Video）

- [ ] 专业模式参数调节
  - ISO滑块（100-6400）
  - 快门速度（1/4000s ~ 30s）
  - 白平衡（自动/日光/阴天/钨丝灯/荧光灯）
  - 曝光补偿（-3EV ~ +3EV）
  - 对焦模式（自动/手动峰值对焦）

### 2. 高级图片编辑工具 (Day 2-3)
- [ ] EditScreen完整实现
  - 三层嵌套编辑UI
  - 基础编辑（裁剪、旋转、翻转）
  - 滤镜应用（5款基础滤镜）
  
- [ ] 高级编辑工具
  - 曲线工具（RGB曲线 + 单独通道）
  - HSL调节（色相、饱和度、明度）
  - 局部调整（选择区域调节）
  - 修复画笔（去除污点）

### 3. 实时滤镜系统 (Day 3)
- [ ] 20+实时滤镜集成
  - 黑白滤镜
  - 复古滤镜
  - 电影滤镜
  - LOMO滤镜
  - 等等...
  
- [ ] 滤镜强度调节
- [ ] 滤镜预览

### 4. 推荐模块 (Day 3-4)
- [ ] RecommendScreen完整实现
  - 搜索栏（毛玻璃效果）
  - 推荐卡片列表（Paging 3）
  - 库洛米装饰
  
- [ ] 搜索功能
- [ ] 位置信息显示
- [ ] 卡片点赞/评论

### 5. 相册管理 (Day 4)
- [ ] GalleryScreen完整实现
  - 相册网格（LazyVerticalGrid）
  - 相册分组（按日期）
  - 库洛米装饰
  
- [ ] 相册详情页
  - 大图预览
  - 照片信息（EXIF）
  - 分享功能
  - 删除功能

### 6. 地图集成 (Day 4-5)
- [ ] RecommendScreen地图功能
  - Google Maps集成
  - 位置标记
  - 缩放/平移
  - 位置搜索

### 7. 个人资料页 (Day 5)
- [ ] ProfileScreen完整实现
  - 用户信息展示
  - 统计数据（照片数、粉丝数等）
  - 设置选项
  - 库洛米装饰

## 技术栈

### 依赖库
- CameraX (相机)
- GPUImage (滤镜)
- Coil (图片加载)
- Paging 3 (分页)
- Google Maps (地图)
- MediaStore (相册)

### 架构
- MVVM + StateFlow
- Navigation Compose
- Jetpack Compose UI

## 文件结构

```
app/src/main/java/com/yanbao/camera/
├── ui/
│   ├── screens/
│   │   ├── CameraScreen.kt (完整实现)
│   │   ├── EditScreen.kt (完整实现)
│   │   ├── RecommendScreen.kt (完整实现)
│   │   ├── GalleryScreen.kt (完整实现)
│   │   └── ProfileScreen.kt (完整实现)
│   ├── components/
│   │   ├── CameraControls.kt (新增)
│   │   ├── FilterSelector.kt (新增)
│   │   ├── EditToolbar.kt (新增)
│   │   ├── GalleryGrid.kt (新增)
│   │   └── MapView.kt (新增)
│   └── theme/
│       └── (已完成)
├── viewmodel/
│   ├── CameraViewModel.kt (完整实现)
│   ├── EditViewModel.kt (完整实现)
│   ├── GalleryViewModel.kt (完整实现)
│   └── RecommendViewModel.kt (新增)
├── repository/
│   ├── CameraRepository.kt (新增)
│   ├── GalleryRepository.kt (新增)
│   └── FilterRepository.kt (新增)
└── model/
    ├── (已完成)
    └── Filter.kt (新增)
```

## 关键实现细节

### CameraX集成
```kotlin
// 使用CameraX的Preview、ImageCapture、VideoCapture
// 实现实时预览和拍照功能
```

### 滤镜系统
```kotlin
// 使用GPUImage库
// 支持20+滤镜
// 实时预览和强度调节
```

### 地图集成
```kotlin
// 使用Google Maps SDK
// API Key已配置
// 支持位置标记和搜索
```

## 测试清单
- [ ] 相机预览正常
- [ ] 拍照功能可用
- [ ] 滤镜实时预览
- [ ] 编辑工具正常
- [ ] 相册加载正常
- [ ] 地图显示正常
- [ ] 性能流畅（无卡顿）

## 交付物
- 完整的功能代码
- 更新的README
- GitHub提交记录
- APK文件（通过GitHub Actions）

## 注意事项
- 所有权限已在AndroidManifest.xml中配置
- 所有资源使用strings.xml定义
- 遵循设计规范（粉紫渐变、毛玻璃、库洛米装饰）
- 性能优化（懒加载、内存管理）
