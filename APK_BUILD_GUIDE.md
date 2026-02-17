# 🚀 雁宝AI相机App - APK构建完整指南

**项目状态**: ✅ 代码完成 | ✅ 功能完整 | ✅ 设计一致 | ✅ 准备构建

---

## 📋 快速选择

### 我想要最简单的方式 → **Android Studio**
### 我想要完全隔离 → **Docker**
### 我想要本地构建 → **构建脚本**

---

## 🎯 构建方法对比

| 方法 | 时间 | 难度 | 需求 | 推荐 |
|------|------|------|------|------|
| **Android Studio** | 3-8分钟 | ⭐ | Android Studio | ✅ 最简单 |
| **Docker** | 10-20分钟 | ⭐⭐ | Docker | ✅ 最隔离 |
| **构建脚本** | 5-15分钟 | ⭐⭐ | Android SDK | ⚠️ 需要SDK |

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
```

**时间**: 3-8分钟
**难度**: ⭐ (最简单)

---

## 🐳 方法2：Docker（推荐 - 完全隔离）

### 步骤1：安装Docker

#### macOS
```bash
brew install docker
# 或从官网下载Docker Desktop
https://www.docker.com/products/docker-desktop
```

#### Ubuntu
```bash
sudo apt-get install docker.io
sudo usermod -aG docker $USER
newgrp docker
```

#### Windows
```bash
# 从官网下载Docker Desktop
https://www.docker.com/products/docker-desktop
```

### 步骤2：克隆项目
```bash
git clone https://github.com/Tsaojason-cao/YanbaoCamera-Android.git
cd YanbaoCamera-Android
```

### 步骤3：运行Docker构建
```bash
# 完整构建流程 (构建镜像 + 运行容器)
./build-with-docker.sh build

# 或分步执行
./build-with-docker.sh build    # 构建镜像
./build-with-docker.sh run      # 运行容器
./build-with-docker.sh clean    # 清理资源
```

### 步骤4：获取APK
```bash
# APK将在以下目录中
docker-output/
├── app-debug.apk
├── app-release-unsigned.apk
└── app-release.aab
```

**时间**: 10-20分钟 (首次)
**难度**: ⭐⭐ (简单)
**优点**: 完全隔离，可重复，跨平台

---

## 💻 方法3：本地构建脚本

### 步骤1：安装Android SDK

#### 自动安装 (推荐)
```bash
# 下载SDK命令行工具
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip

# 解压
unzip commandlinetools-linux-10406996_latest.zip

# 设置环境变量
export ANDROID_SDK_ROOT=$HOME/android-sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin
```

#### 手动安装
1. 从官网下载 Android SDK: https://developer.android.com/studio
2. 设置 ANDROID_SDK_ROOT 环境变量
3. 运行 sdkmanager 安装组件

### 步骤2：安装SDK组件
```bash
# 接受许可证
yes | sdkmanager --licenses

# 安装SDK
sdkmanager "platforms;android-34" "build-tools;34.0.0"
```

### 步骤3：运行构建脚本
```bash
# 构建Debug APK
./build-apk.sh debug

# 构建Release APK
./build-apk.sh release

# 构建所有
./build-apk.sh all

# 清理
./build-apk.sh clean
```

### 步骤4：获取APK
```bash
# APK将在以下目录中
build-output/
├── apk/
│   ├── app-debug.apk
│   ├── app-release-unsigned.apk
│   └── app-release.aab
└── logs/
    ├── debug-build.log
    ├── release-build.log
    └── bundle-build.log
```

**时间**: 5-15分钟
**难度**: ⭐⭐ (中等)
**需求**: 本地Android SDK

---

## 📱 安装和测试APK

### 使用ADB安装
```bash
# 连接设备
adb devices

# 安装APK
adb install -r app-debug.apk

# 启动应用
adb shell am start -n com.yanbao.camera/.MainActivity

# 查看日志
adb logcat | grep YanbaoCamera
```

### 使用文件管理器安装
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

# 查看APK信息
aapt dump badging app-debug.apk
```

### 测试功能
- [ ] 应用启动正常
- [ ] 所有屏幕可导航
- [ ] 相机功能工作
- [ ] 编辑功能工作
- [ ] 没有崩溃
- [ ] 性能良好

---

## 🔐 签名Release APK

### 生成密钥库
```bash
keytool -genkey -v -keystore my-release-key.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias my-key-alias
```

### 签名APK
```bash
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
  -keystore my-release-key.keystore \
  app-release-unsigned.apk \
  my-key-alias
```

### 对齐APK
```bash
zipalign -v 4 app-release-unsigned.apk app-release.apk
```

---

## 📦 发布到应用商店

### Google Play
1. 创建Google Play开发者账户
2. 创建应用
3. 上传签名的APK或Bundle
4. 填写应用信息
5. 提交审核

### 华为AppGallery
1. 创建华为开发者账户
2. 创建应用
3. 上传APK
4. 填写应用信息
5. 提交审核

### 小米应用商店
1. 创建小米开发者账户
2. 创建应用
3. 上传APK
4. 填写应用信息
5. 提交审核

---

## 🐛 故障排除

### 问题1：SDK未找到
**错误**: `SDK location not found`

**解决方案**:
```bash
# 设置ANDROID_SDK_ROOT
export ANDROID_SDK_ROOT=$HOME/Android/Sdk

# 或在local.properties中
sdk.dir=/path/to/android-sdk
```

### 问题2：编译失败
**错误**: `Build failed with an exception`

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
**错误**: `INSTALL_FAILED_INVALID_APK`

**解决方案**:
```bash
# 卸载旧版本
adb uninstall com.yanbao.camera

# 重新安装
adb install -r app-debug.apk
```

### 问题4：Docker构建超时
**错误**: `Docker build timeout`

**解决方案**:
```bash
# 增加Docker内存
# 在Docker Desktop中: Preferences → Resources → Memory: 8GB+

# 或使用本地构建脚本
./build-apk.sh debug
```

---

## 📊 APK信息

### 文件大小
- Debug APK: 80-100MB
- Release APK: 50-70MB
- Release Bundle: 40-60MB

### 最小要求
- **最小SDK**: API 24 (Android 7.0)
- **目标SDK**: API 34 (Android 14)
- **Java**: 17

### 支持的架构
- arm64-v8a (推荐)
- armeabi-v7a
- x86_64

---

## 🎯 推荐流程

### 本地开发
```bash
1. 使用Android Studio
2. 构建Debug APK
3. 在模拟器/设备上测试
4. 修复bug
```

### 发布前
```bash
1. 使用Docker构建
2. 自动构建Release APK
3. 签名APK
4. 验证功能
```

### 生产环境
```bash
1. 使用Docker构建
2. 自动化流程
3. 上传到应用商店
4. 监控用户反馈
```

---

## 📞 需要帮助？

### 文档
- [Android官方文档](https://developer.android.com/)
- [Gradle文档](https://gradle.org/docs/)
- [Docker文档](https://docs.docker.com/)

### 常见问题
- 查看 APK_BUILD_QUICK_START.md
- 查看 BUILD_APK_INSTRUCTIONS.md
- 查看项目中的其他.md文件

### 联系方式
- GitHub: https://github.com/Tsaojason-cao/YanbaoCamera-Android
- 邮箱: dev@yanbao.camera

---

## 🚀 立即开始

### 最简单的方式 (推荐)
```bash
# 1. 安装Android Studio
# 2. 打开项目
# 3. Build → Build APK(s)
# 4. 完成！
```

### 完全隔离的方式
```bash
# 1. 安装Docker
# 2. 运行脚本
./build-with-docker.sh build
# 3. 完成！
```

### 本地构建的方式
```bash
# 1. 安装Android SDK
# 2. 运行脚本
./build-apk.sh all
# 3. 完成！
```

---

**预计时间**: 5-20分钟（取决于选择的方法）
**难度**: ⭐ 到 ⭐⭐

**选择您最喜欢的方法，立即构建APK！** 🎉

---

**项目**: 雁宝AI相机App
**版本**: 1.0.0
**状态**: ✅ 准备构建
**最后更新**: 2026年2月17日
