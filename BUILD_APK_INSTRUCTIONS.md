# 🚀 雁宝AI相机App - APK构建完整指南

## 📌 快速开始

### 选项1：使用Android Studio（推荐 - 最简单）

#### 步骤1：安装Android Studio
- 下载: https://developer.android.com/studio
- 安装并启动

#### 步骤2：打开项目
```bash
# 在Android Studio中
File → Open → 选择 /tmp/YanbaoCamera_Complete 目录
```

#### 步骤3：等待Gradle同步
- Android Studio会自动下载依赖
- 等待"Gradle build finished"

#### 步骤4：构建APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

#### 步骤5：找到APK
```
app/build/outputs/apk/debug/app-debug.apk
或
app/build/outputs/apk/release/app-release.apk
```

---

### 选项2：使用GitHub Actions（推荐 - 自动化）

#### 步骤1：推送代码到GitHub
```bash
cd /tmp/YanbaoCamera_Complete
git remote add origin https://github.com/Tsaojason-cao/yanbao-camera-app.git
git branch -M main
git push -u origin main
```

#### 步骤2：启用GitHub Actions
1. 进入GitHub仓库
2. 点击 "Actions" 标签
3. 选择 "Android CI" 工作流
4. 点击 "Enable workflow"

#### 步骤3：自动构建
- 每次push时自动构建APK
- 在Actions标签中查看构建进度
- 从构建结果中下载APK

#### 步骤4：下载APK
```
Actions → Latest workflow run → Artifacts → app-release.apk
```

---

### 选项3：使用Docker（完全隔离）

#### 步骤1：安装Docker
```bash
# Ubuntu/Debian
sudo apt-get install docker.io

# macOS
brew install docker
```

#### 步骤2：使用Docker构建
```bash
# 创建Dockerfile
cat > Dockerfile << 'EOF'
FROM ubuntu:22.04

# 安装依赖
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    gradle \
    git \
    wget \
    unzip

# 设置Android SDK
RUN mkdir -p /opt/android-sdk
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin

# 下载Android SDK
RUN cd /opt/android-sdk && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip && \
    unzip commandlinetools-linux-10406996_latest.zip && \
    rm commandlinetools-linux-10406996_latest.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# 接受许可证
RUN yes | sdkmanager --licenses

# 安装SDK
RUN sdkmanager "platforms;android-34" "build-tools;34.0.0"

# 复制项目
COPY . /app
WORKDIR /app

# 构建APK
RUN gradle assembleRelease

# 输出APK
CMD ["cp", "app/build/outputs/apk/release/app-release.apk", "/output/"]
EOF

# 构建Docker镜像
docker build -t yanbao-camera-builder .

# 运行构建
docker run -v $(pwd)/output:/output yanbao-camera-builder

# APK将在 ./output/app-release.apk
```

---

### 选项4：使用命令行（需要Android SDK）

#### 步骤1：安装Android SDK
```bash
# 下载SDK命令行工具
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip

# 解压
unzip commandlinetools-linux-10406996_latest.zip

# 设置环境变量
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
```

#### 步骤2：安装必要的SDK组件
```bash
# 接受许可证
yes | sdkmanager --licenses

# 安装SDK
sdkmanager "platforms;android-34" "build-tools;34.0.0" "ndk;25.1.8937393"
```

#### 步骤3：构建APK
```bash
cd /tmp/YanbaoCamera_Complete

# Debug APK
gradle assembleDebug

# Release APK
gradle assembleRelease
```

#### 步骤4：签名APK（Release）
```bash
# 生成密钥库（首次）
keytool -genkey -v -keystore my-release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias

# 签名APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  my-key-alias

# 对齐APK
zipalign -v 4 app/build/outputs/apk/release/app-release-unsigned.apk \
  app/build/outputs/apk/release/app-release.apk
```

---

## 📋 构建文件位置

### Debug APK
```
app/build/outputs/apk/debug/app-debug.apk
```

### Release APK
```
app/build/outputs/apk/release/app-release.apk
```

### Bundle（用于Google Play）
```
app/build/outputs/bundle/release/app-release.aab
```

---

## 🔍 APK信息

### 文件大小
- Debug: 约80-100MB
- Release: 约50-70MB（已优化）

### 最小要求
- **最小SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)
- **Java版本**: 17

### 签名
- **算法**: SHA256withRSA
- **有效期**: 25年

---

## 📱 安装APK

### 在真实设备上安装

#### 使用ADB
```bash
# 连接设备
adb devices

# 安装APK
adb install -r app-release.apk

# 启动应用
adb shell am start -n com.yanbao.camera/.MainActivity

# 查看日志
adb logcat | grep YanbaoCamera
```

#### 使用文件管理器
1. 将APK复制到设备
2. 打开文件管理器
3. 点击APK文件
4. 点击"安装"

### 在模拟器上安装

```bash
# 启动模拟器
emulator -avd Pixel_4_API_34

# 等待启动完成后
adb install -r app-release.apk
```

---

## 🧪 测试清单

安装后，请测试以下功能：

### 基本功能
- [ ] 应用启动正常
- [ ] Splash屏幕显示
- [ ] 所有屏幕可以导航
- [ ] 没有崩溃

### 相机功能
- [ ] 相机预览正常
- [ ] 拍照功能工作
- [ ] 闪光灯控制工作
- [ ] 摄像头切换工作
- [ ] 所有模式都可选

### 编辑功能
- [ ] 编辑屏幕打开
- [ ] 参数调节工作
- [ ] 滤镜应用工作
- [ ] 保存功能工作

### 其他功能
- [ ] 相册显示图片
- [ ] 推荐位置显示
- [ ] 个人资料显示
- [ ] 导航栏工作

### 性能
- [ ] 启动速度快（<3秒）
- [ ] 帧率稳定（>30fps）
- [ ] 没有内存泄漏
- [ ] 电池消耗正常

---

## 🐛 故障排除

### 问题1：APK无法安装
**错误**: "INSTALL_FAILED_INVALID_APK"
**解决方案**:
```bash
# 检查APK完整性
zipalign -c 4 app-release.apk

# 重新签名
jarsigner -verify -verbose -certs app-release.apk
```

### 问题2：应用崩溃
**错误**: "Unfortunately, Yanbao Camera has stopped"
**解决方案**:
```bash
# 查看崩溃日志
adb logcat | grep FATAL

# 检查权限
adb shell pm list permissions | grep CAMERA

# 重新安装
adb uninstall com.yanbao.camera
adb install -r app-release.apk
```

### 问题3：相机黑屏
**原因**: 权限未授予或硬件不支持
**解决方案**:
```bash
# 授予权限
adb shell pm grant com.yanbao.camera android.permission.CAMERA
adb shell pm grant com.yanbao.camera android.permission.WRITE_EXTERNAL_STORAGE

# 检查相机可用性
adb shell getprop ro.hardware.camera
```

### 问题4：Gradle构建失败
**错误**: "Unable to find SDK"
**解决方案**:
```bash
# 设置ANDROID_SDK_ROOT
export ANDROID_SDK_ROOT=$HOME/Android/Sdk

# 或在gradle.properties中
sdk.dir=/path/to/android-sdk
```

---

## 📊 构建统计

### 编译时间
- Debug: 2-5分钟
- Release: 3-8分钟

### 输出大小
- Debug APK: 80-100MB
- Release APK: 50-70MB
- Bundle: 40-60MB

### 依赖项数量
- 直接依赖: 30+
- 传递依赖: 100+

---

## 🎯 推荐的构建流程

### 本地开发
```bash
# 1. Debug构建（快速迭代）
gradle assembleDebug

# 2. 在模拟器上测试
adb install -r app/build/outputs/apk/debug/app-debug.apk

# 3. 查看日志
adb logcat
```

### 发布前
```bash
# 1. Release构建
gradle assembleRelease

# 2. 签名
jarsigner -keystore my-release-key.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk

# 3. 对齐
zipalign -v 4 app-release-unsigned.apk app-release.apk

# 4. 验证
zipalign -c 4 app-release.apk
```

### 自动化（GitHub Actions）
```bash
# 1. 推送代码
git push origin main

# 2. Actions自动构建
# 3. 从Artifacts下载APK
```

---

## 📞 获取帮助

### 常用资源
- [Android官方文档](https://developer.android.com/)
- [Gradle文档](https://gradle.org/docs/)
- [Jetpack Compose文档](https://developer.android.com/jetpack/compose)

### 社区支持
- Stack Overflow: android, gradle, kotlin
- GitHub Issues: yanbao-camera-app
- Android开发者论坛

---

## ✅ 构建检查清单

- [ ] 已安装Android Studio或SDK
- [ ] 已设置ANDROID_SDK_ROOT环境变量
- [ ] 已安装Java 17或更高版本
- [ ] 已克隆项目代码
- [ ] 已运行gradle sync
- [ ] 已选择构建方法（Android Studio/CLI/GitHub Actions）
- [ ] 已成功构建APK
- [ ] 已签名APK（Release）
- [ ] 已在设备上安装和测试
- [ ] 所有测试通过

---

## 🎉 完成！

APK构建完成后，您可以：

1. **安装到设备** - 使用ADB或文件管理器
2. **发布到应用商店** - Google Play、华为、小米等
3. **分享给用户** - 通过链接或QR码
4. **收集反馈** - 改进应用

**祝贺！雁宝AI相机App已准备好发布！** 🎊

---

**最后更新**: 2026年2月17日
**版本**: 1.0.0
