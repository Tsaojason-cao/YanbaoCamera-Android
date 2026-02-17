# 🚀 雁宝AI相机App - APK构建快速开始指南

**项目状态**: ✅ 代码完成 | ✅ 功能完整 | ✅ 设计一致 | ✅ 准备构建

---

## ⚡ 快速选择

### 我想要最简单的方式 → **Android Studio**
### 我想要自动化 → **GitHub Actions**
### 我想要完全隔离 → **Docker**

---

## 📱 方法1：Android Studio（推荐 - 最简单）

### 步骤1：安装Android Studio
```bash
# 下载
https://developer.android.com/studio

# 或使用包管理器
# macOS
brew install android-studio

# Ubuntu
sudo snap install android-studio --classic

# Windows
# 从官网下载安装程序
```

### 步骤2：打开项目
```bash
# 在Android Studio中
File → Open → 选择项目目录
/tmp/YanbaoCamera_Complete
```

### 步骤3：等待Gradle同步
- Android Studio会自动下载SDK和依赖
- 等待"Gradle build finished"消息

### 步骤4：构建APK
```
Build → Build Bundle(s) / APK(s) → Build APK(s)
```

### 步骤5：找到APK
```
app/build/outputs/apk/debug/app-debug.apk
或
app/build/outputs/apk/release/app-release.apk
```

**时间**: 3-8分钟
**难度**: ⭐ (最简单)

---

## 🤖 方法2：GitHub Actions（推荐 - 自动化）

### 步骤1：推送代码到GitHub
```bash
cd /tmp/YanbaoCamera_Complete

# 添加远程仓库
git remote add origin https://github.com/YOUR_USERNAME/yanbao-camera-app.git

# 推送代码
git branch -M main
git push -u origin main
```

### 步骤2：启用GitHub Actions
1. 进入GitHub仓库
2. 点击 "Actions" 标签
3. 点击 "I understand my workflows, go ahead and enable them"
4. 选择 "Build APK" 工作流

### 步骤3：自动构建
- 每次push时自动构建
- 在Actions标签中查看进度
- 构建完成后在Artifacts中下载APK

### 步骤4：下载APK
```
Actions → Latest workflow run → Artifacts
→ app-debug 或 app-release
```

**时间**: 5-15分钟
**难度**: ⭐⭐ (简单)
**优点**: 无需本地SDK，完全自动化

---

## 🐳 方法3：Docker（完全隔离）

### 步骤1：安装Docker
```bash
# macOS
brew install docker

# Ubuntu
sudo apt-get install docker.io

# Windows
# 从官网下载Docker Desktop
```

### 步骤2：构建Docker镜像
```bash
cd /tmp/YanbaoCamera_Complete

# 构建镜像
docker build -t yanbao-camera-builder .

# 或使用预定义的Dockerfile
docker build -f Dockerfile.android -t yanbao-camera-builder .
```

### 步骤3：运行构建
```bash
# 创建输出目录
mkdir -p output

# 运行构建
docker run -v $(pwd)/output:/app/output yanbao-camera-builder

# APK将在 output/ 目录中
```

**时间**: 10-20分钟
**难度**: ⭐⭐⭐ (中等)
**优点**: 完全隔离，可重复，跨平台

---

## 💻 方法4：命令行（需要本地SDK）

### 步骤1：安装Android SDK
```bash
# 下载SDK命令行工具
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip

# 解压
unzip commandlinetools-linux-10406996_latest.zip

# 设置环境变量
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
```

### 步骤2：安装SDK组件
```bash
# 接受许可证
yes | sdkmanager --licenses

# 安装SDK
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### 步骤3：构建APK
```bash
cd /tmp/YanbaoCamera_Complete

# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease
```

### 步骤4：签名（Release）
```bash
# 生成密钥库
keytool -genkey -v -keystore my-release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias

# 签名APK
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.keystore \
  app/build/outputs/apk/release/app-release-unsigned.apk \
  my-key-alias

# 对齐APK
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

**时间**: 3-8分钟
**难度**: ⭐⭐⭐⭐ (复杂)
**优点**: 快速，完全控制

---

## 📊 方法对比

| 方法 | 时间 | 难度 | 自动化 | 推荐 |
|------|------|------|--------|------|
| Android Studio | 3-8分钟 | ⭐ | ❌ | ✅ 最简单 |
| GitHub Actions | 5-15分钟 | ⭐⭐ | ✅ | ✅ 最自动 |
| Docker | 10-20分钟 | ⭐⭐⭐ | ✅ | ✅ 最隔离 |
| 命令行 | 3-8分钟 | ⭐⭐⭐⭐ | ⚠️ | ❌ 最复杂 |

---

## 📱 安装APK

### 使用ADB
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

### 使用文件管理器
1. 将APK复制到设备
2. 打开文件管理器
3. 点击APK文件
4. 点击"安装"

---

## ✅ 验证APK

### 检查APK完整性
```bash
# 验证签名
jarsigner -verify -verbose -certs app-release.apk

# 检查对齐
zipalign -c 4 app-release.apk
```

### 测试功能
- [ ] 应用启动正常
- [ ] 所有屏幕可导航
- [ ] 相机功能工作
- [ ] 编辑功能工作
- [ ] 没有崩溃
- [ ] 性能良好

---

## 🐛 故障排除

### 问题1：SDK未找到
**解决方案**:
```bash
# 设置ANDROID_SDK_ROOT
export ANDROID_SDK_ROOT=$HOME/Android/Sdk

# 或在local.properties中
sdk.dir=/path/to/android-sdk
```

### 问题2：编译失败
**解决方案**:
```bash
# 清理构建
./gradlew clean

# 重新同步
./gradlew sync

# 重新构建
./gradlew assembleDebug
```

### 问题3：APK无法安装
**解决方案**:
```bash
# 卸载旧版本
adb uninstall com.yanbao.camera

# 重新安装
adb install -r app-release.apk
```

---

## 📦 APK信息

### 文件位置
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release.apk`
- Bundle: `app/build/outputs/bundle/release/app-release.aab`

### 文件大小
- Debug: 80-100MB
- Release: 50-70MB
- Bundle: 40-60MB

### 最小要求
- **最小SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)
- **Java**: 17

---

## 🎯 推荐流程

### 本地开发
1. 使用Android Studio
2. 构建Debug APK
3. 在模拟器/设备上测试

### 发布前
1. 使用GitHub Actions
2. 自动构建Release APK
3. 从Artifacts下载

### 生产环境
1. 使用Docker
2. 可重复构建
3. 完全隔离

---

## 📞 需要帮助？

### 文档
- [Android官方文档](https://developer.android.com/)
- [Gradle文档](https://gradle.org/docs/)
- [GitHub Actions文档](https://docs.github.com/en/actions)

### 常见问题
- 查看 BUILD_APK_INSTRUCTIONS.md
- 查看 FINAL_APK_BUILD_GUIDE.md
- 查看项目中的其他.md文件

---

## 🚀 立即开始

**选择您最喜欢的方法，立即构建APK！**

1. **最简单**: Android Studio
2. **最自动**: GitHub Actions
3. **最隔离**: Docker

**预计时间**: 5-20分钟
**难度**: ⭐ 到 ⭐⭐⭐

**开始构建！** 🎉

---

**项目**: 雁宝AI相机App
**版本**: 1.0.0
**状态**: ✅ 准备构建
**最后更新**: 2026年2月17日
