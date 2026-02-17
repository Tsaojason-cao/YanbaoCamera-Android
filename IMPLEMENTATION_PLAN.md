# 雁宝AI相机 - 推荐方案B（专业版）实现计划

## 📋 项目概览

**目标**：在2周内实现功能完整、体验流畅的专业相机应用
**方案**：Phase 1 (MVP) + Phase 2 (专业工具)
**平台**：Android 24+ (Kotlin + Jetpack Compose)
**资源**：全部开源库实现

---

## 🗓️ 时间规划

### 第1周 - Phase 1: 基础完整与性能优化 (MVP)

#### Day 1-2: 项目架构与基础设施
- [ ] 完善项目结构（MVVM架构）
- [ ] 配置依赖库（CameraX、Coil、GPUImage、Navigation等）
- [ ] 创建基础ViewModel和Repository
- [ ] 配置Mock数据源

#### Day 3-4: 核心模块UI实现
- [ ] **SplashScreen** - 启动屏
- [ ] **HomeScreen** - 推荐流（使用Paging 3）
- [ ] **CameraScreen** - 基础相机预览+拍照
- [ ] **EditScreen** - 基础编辑（裁剪、5款滤镜、调整）
- [ ] **GalleryScreen** - 相册浏览（LazyVerticalGrid）
- [ ] **RecommendScreen** - 推荐位置卡片
- [ ] **ProfileScreen** - 个人资料

#### Day 5: 相机功能实现
- [ ] CameraX集成（前后摄像头切换）
- [ ] 拍照保存到MediaStore
- [ ] 闪光灯模式（自动/开/关）
- [ ] 权限处理（CAMERA、READ_EXTERNAL_STORAGE等）

#### Day 6-7: 编辑与性能优化
- [ ] GPUImage滤镜集成（5款基础滤镜）
- [ ] 图片裁剪工具（1:1, 4:3, 16:9）
- [ ] 亮度、对比度、饱和度调整
- [ ] 图片懒加载（LazyColumn/LazyVerticalGrid + Coil）
- [ ] 内存优化（Bitmap复用、资源释放）
- [ ] 启动速度优化（SplashScreen）

**产出**：可安装的APK v0.1（MVP版本）

---

### 第2周 - Phase 2: 专业工具与流畅体验

#### Day 8-9: 相机专业模式
- [ ] ISO调节（100-6400）滑块
- [ ] 快门速度调节（1/4000s ~ 30s）滑块
- [ ] 白平衡选择（自动/日光/阴天/钨丝灯/荧光灯）
- [ ] 曝光补偿（-3EV ~ +3EV）滑块
- [ ] 对焦模式（自动/手动）
- [ ] 实时直方图显示（RGB直方图Canvas绘制）
- [ ] 峰值对焦提示（手动对焦时显示轮廓）

#### Day 10-11: 高级编辑工具
- [ ] 曲线工具（RGB曲线 + 单独通道）
- [ ] HSL调节（色相、饱和度、明度独立调节）
- [ ] 局部调整（基于控制点的区域调节）
- [ ] 修复画笔（笔刷大小调节）
- [ ] 扩展滤镜库（20+款滤镜）
- [ ] 滤镜强度调节

#### Day 12-13: 流畅交互细节
- [ ] 触感反馈（拍照、切换模式时震动）
- [ ] 微动画（点赞心跳、导航栏弹性缩放）
- [ ] 手势操作（相册左右滑动、编辑页面双指缩放）
- [ ] 页面过渡动画优化

#### Day 14: 测试、优化与发布
- [ ] 功能测试（所有页面、功能流程）
- [ ] 性能测试（内存、CPU、电量）
- [ ] Bug修复
- [ ] APK构建与签名
- [ ] 上传到GitHub Releases

**产出**：可安装的APK v1.0（专业版本）

---

## 📦 依赖库配置

```kotlin
// build.gradle.kts (app level)

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Jetpack Compose
    implementation("androidx.compose.ui:ui:1.6.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.6")
    
    // CameraX
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    
    // Image Loading & Processing
    implementation("io.coil-kt:coil-compose:2.5.0")
    implementation("jp.co.cyberagent.android:gpuimage:2.1.0")
    
    // Paging
    implementation("androidx.paging:paging-compose:3.2.1")
    
    // Permissions
    implementation("com.google.accompanist:accompanist-permissions:0.33.2-alpha")
    
    // JSON
    implementation("com.google.code.gson:gson:2.10.1")
}
```

---

## 🏗️ 项目结构

```
app/src/main/
├── java/com/yanbao/camera/
│   ├── MainActivity.kt
│   ├── ui/
│   │   ├── screens/
│   │   │   ├── SplashScreen.kt
│   │   │   ├── HomeScreen.kt
│   │   │   ├── CameraScreen.kt
│   │   │   ├── EditScreen.kt
│   │   │   ├── GalleryScreen.kt
│   │   │   ├── RecommendScreen.kt
│   │   │   └── ProfileScreen.kt
│   │   ├── components/
│   │   │   ├── BottomNavigation.kt
│   │   │   ├── CameraControls.kt
│   │   │   ├── EditTools.kt
│   │   │   └── ...
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   └── navigation/
│   │       └── NavGraph.kt
│   ├── viewmodel/
│   │   ├── CameraViewModel.kt
│   │   ├── EditViewModel.kt
│   │   ├── GalleryViewModel.kt
│   │   └── HomeViewModel.kt
│   ├── repository/
│   │   ├── CameraRepository.kt
│   │   ├── GalleryRepository.kt
│   │   └── MockDataRepository.kt
│   ├── model/
│   │   ├── Photo.kt
│   │   ├── Post.kt
│   │   ├── User.kt
│   │   └── CameraSettings.kt
│   ├── utils/
│   │   ├── PermissionUtils.kt
│   │   ├── FileUtils.kt
│   │   └── ImageProcessingUtils.kt
│   └── service/
│       └── ImageProcessingService.kt
└── res/
    ├── values/
    │   ├── strings.xml
    │   ├── colors.xml
    │   └── themes.xml
    └── ...
```

---

## 🎯 功能清单

### Phase 1 (MVP)
- [x] 项目架构搭建
- [ ] 7个完整模块UI
- [ ] 基础相机功能（拍照、前后摄像头、闪光灯）
- [ ] 基础编辑（裁剪、5款滤镜、调整）
- [ ] 相册浏览
- [ ] 图片懒加载
- [ ] 内存优化
- [ ] 启动屏

### Phase 2 (专业工具)
- [ ] 相机专业模式（ISO、快门、白平衡、曝光、对焦）
- [ ] 实时直方图
- [ ] 峰值对焦
- [ ] 曲线工具
- [ ] HSL调节
- [ ] 局部调整
- [ ] 修复画笔
- [ ] 20+滤镜库
- [ ] 触感反馈
- [ ] 微动画
- [ ] 手势操作

---

## 📱 开发工具与环境

- **IDE**: Android Studio Hedgehog+
- **Kotlin**: 1.9.21+
- **Gradle**: 8.5+
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

---

## 🚀 执行步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/Tsaojason-cao/YanbaoCamera-Android.git
   cd YanbaoCamera-Android
   ```

2. **更新依赖**
   ```bash
   ./gradlew dependencies
   ```

3. **本地构建**
   ```bash
   ./gradlew assembleDebug
   ```

4. **推送到GitHub**
   ```bash
   git add .
   git commit -m "Phase 1: MVP implementation"
   git push origin main
   ```

5. **GitHub Actions自动构建**
   - 自动触发APK构建
   - 生成artifacts（APK文件）
   - 可下载安装

---

## 📊 预期产出

### Week 1 End (Day 7)
- ✅ APK v0.1 (MVP)
- ✅ 7个完整模块
- ✅ 基础相机+编辑+相册
- ✅ 性能优化
- ✅ 可安装测试

### Week 2 End (Day 14)
- ✅ APK v1.0 (专业版)
- ✅ 相机专业模式
- ✅ 高级编辑工具
- ✅ 20+滤镜库
- ✅ 流畅交互细节
- ✅ 生产级质量

---

## 📝 注意事项

1. **权限处理**：所有权限请求都需要运行时权限处理
2. **内存管理**：大图片处理需要使用缩放和复用
3. **性能**：相机预览和图片处理需要在后台线程
4. **兼容性**：测试Android 7.0-14的兼容性
5. **测试设备**：建议在真实设备上测试相机功能

---

## 🔗 参考资源

- [CameraX官方文档](https://developer.android.com/training/camerax)
- [Jetpack Compose官方文档](https://developer.android.com/jetpack/compose)
- [GPUImage-Android](https://github.com/CyberAgent/android-gpuimage)
- [Coil图片加载库](https://coil-kt.github.io/coil/)
- [Paging 3分页库](https://developer.android.com/topic/libraries/architecture/paging/v3-overview)

---

**开始日期**：2026-02-17
**预期完成**：2026-03-02
**版本**：1.0.0

