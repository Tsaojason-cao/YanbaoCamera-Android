# 🎥 雁宝AI相机 (Yanbao Camera)

一款功能强大的Android原生相机应用，集专业摄影工具、高级编辑功能和社交分享于一体。

## ✨ 核心特性

### 📱 7个核心模块

1. **首页推荐流** - 发现优质摄影作品
2. **相机模块** - 专业级相机功能
3. **编辑工具** - 强大的图片编辑
4. **相册管理** - 智能相册浏览
5. **推荐位置** - 发现摄影圣地
6. **个人资料** - 用户中心
7. **启动屏** - 优雅的应用启动

### 🎨 设计特点

- ✅ 现代化UI设计（Jetpack Compose）
- ✅ 亮色/暗色主题支持
- ✅ 流畅的动画和过渡
- ✅ 响应式布局
- ✅ Material Design 3规范

### 📸 相机功能（计划中）

- 实时预览
- 前后摄像头切换
- 闪光灯模式
- 专业模式（ISO、快门、白平衡）
- 视频录制

### 🖼️ 编辑工具（计划中）

- 亮度、对比度、饱和度调整
- 色调调整
- 20+实时滤镜
- 裁剪和旋转
- 高级编辑（曲线、HSL、局部调整）

### 🌍 社交功能（计划中）

- 作品分享
- 点赞和评论
- 用户关注
- 位置标签
- 参数卡片分享

---

## 🚀 快速开始

### 环境要求

- Android Studio Hedgehog (2023.1.1) 或更新
- JDK 17+
- Android SDK 24+ (Android 7.0+)

### 构建步骤

```bash
# 1. 克隆项目
git clone https://github.com/Tsaojason-cao/YanbaoCamera-Android.git
cd YanbaoCamera-Android

# 2. 同步Gradle依赖
./gradlew build

# 3. 构建Debug APK
./gradlew assembleDebug

# 4. 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

**详细构建指南**：见 [BUILD_GUIDE.md](BUILD_GUIDE.md)

---

## 📊 项目架构

### 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| **UI** | Jetpack Compose | 现代化声明式UI框架 |
| **导航** | Navigation Compose | 应用页面导航 |
| **状态管理** | ViewModel + StateFlow | 响应式状态管理 |
| **数据** | Repository Pattern | 数据访问层 |
| **相机** | CameraX | 统一的相机API |
| **图片** | Coil | 高效的图片加载 |
| **图片处理** | GPUImage | GPU加速的图片滤镜 |
| **分页** | Paging 3 | 高效的列表分页 |
| **异步** | Coroutines | 异步编程 |

### 项目结构

```
app/src/main/java/com/yanbao/camera/
├── MainActivity.kt              # 应用入口
├── App.kt                       # 根组件和导航
├── model/                       # 数据模型
│   ├── Photo.kt
│   ├── Post.kt
│   ├── User.kt
│   └── CameraSettings.kt
├── viewmodel/                   # ViewModel层
│   ├── CameraViewModel.kt
│   ├── EditViewModel.kt
│   ├── GalleryViewModel.kt
│   └── HomeViewModel.kt
├── repository/                  # 数据仓库
│   └── MockDataRepository.kt
└── ui/
    ├── screens/                 # 7个屏幕
    │   ├── SplashScreen.kt
    │   ├── HomeScreen.kt
    │   ├── CameraScreen.kt
    │   ├── EditScreen.kt
    │   ├── GalleryScreen.kt
    │   ├── RecommendScreen.kt
    │   └── ProfileScreen.kt
    ├── components/              # UI组件
    │   ├── BottomNavigation.kt
    │   └── PostCard.kt
    ├── theme/                   # 主题配置
    │   ├── Theme.kt
    │   ├── Color.kt
    │   └── Typography.kt
    └── navigation/              # 导航配置
        └── NavGraph.kt
```

---

## 🎯 开发进度

### Phase 1: MVP (✅ 已完成)

- [x] 项目架构搭建
- [x] 数据模型定义
- [x] Mock数据仓库
- [x] ViewModel层实现
- [x] 7个屏幕UI
- [x] 底部导航
- [x] 主题系统

**状态**：可运行的基础应用框架

### Phase 2: 专业工具 (🔄 进行中)

- [ ] 相机专业模式（ISO、快门、白平衡）
- [ ] 高级编辑工具（曲线、HSL、局部调整）
- [ ] 20+实时滤镜
- [ ] 微动画和过渡
- [ ] 手势优化

**预期完成**：2周内

### Phase 3: 社交与智能 (📅 计划中)

- [ ] AR特效与贴纸
- [ ] AI增强（超分辨率、低光增强）
- [ ] 云同步与备份
- [ ] 用户系统和社交功能
- [ ] 参数卡片分享

**预期完成**：4周内

---

## 🔧 配置和自定义

### 修改应用名称

编辑 `app/src/main/res/values/strings.xml`：

```xml
<string name="app_name">您的应用名称</string>
```

### 修改主题颜色

编辑 `app/src/main/java/com/yanbao/camera/ui/theme/Colors.kt`：

```kotlin
val PrimaryLight = Color(0xFFYourColor)
val SecondaryLight = Color(0xFFYourColor)
// ...
```

### 修改应用包名

1. 在 `app/build.gradle.kts` 中修改 `applicationId`
2. 重命名包目录结构
3. 更新 `AndroidManifest.xml`

---

## 📦 依赖库

主要依赖版本：

```kotlin
// Jetpack
androidx.compose:compose-bom:2024.02.00
androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0
androidx.navigation:navigation-compose:2.7.6

// Camera & Media
androidx.camera:camera-core:1.3.0
androidx.camera:camera-camera2:1.3.0
androidx.camera:camera-lifecycle:1.3.0
io.coil-kt:coil-compose:2.5.0

// Image Processing
jp.co.cyberagent:gpuimage:2.1.0

// Paging
androidx.paging:paging-compose:3.2.1

// Coroutines
org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

// Serialization
com.google.code.gson:gson:2.10.1
```

---

## 🧪 测试

```bash
# 运行单元测试
./gradlew test

# 运行集成测试
./gradlew connectedAndroidTest

# 代码质量检查
./gradlew lint
```

---

## 📱 系统要求

- **最低API级别**：24 (Android 7.0)
- **目标API级别**：34 (Android 14)
- **推荐设备**：Android 10+

---

## 🔐 权限

应用需要以下权限：

```xml
<!-- 相机 -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- 存储 -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

<!-- 位置 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<!-- 其他 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.VIBRATE" />
```

---

## 📄 许可证

MIT License - 详见 [LICENSE](LICENSE) 文件

---

## 🤝 贡献

欢迎提交Issue和Pull Request！

1. Fork项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开Pull Request

---

## 📞 联系方式

- **GitHub Issues**：[提交问题](https://github.com/Tsaojason-cao/YanbaoCamera-Android/issues)
- **Email**：contact@yanbao.app

---

## 🙏 致谢

感谢以下开源项目的支持：

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [CameraX](https://developer.android.com/training/camerax)
- [Coil](https://coil-kt.github.io/coil/)
- [GPUImage](https://github.com/CyberAgent/android-gpuimage)

---

**最后更新**：2026-02-17  
**版本**：1.0.0-MVP  
**作者**：Yanbao Team
