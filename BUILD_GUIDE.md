# 雁宝AI相机 - 构建指南

## 📋 项目概览

**项目名称**：雁宝AI相机 (Yanbao Camera)  
**平台**：Android 7.0+ (API 24+)  
**开发语言**：Kotlin + Jetpack Compose  
**构建系统**：Gradle 8.5  
**目标SDK**：Android 14 (API 34)

---

## 🛠️ 环境要求

### 必需工具
- **Android Studio** Hedgehog (2023.1.1) 或更新版本
- **Java Development Kit (JDK)** 17 或更新版本
- **Android SDK** 包含：
  - Android SDK Platform 34 (Android 14)
  - Android SDK Build-Tools 34.0.0+
  - Android Emulator (可选，用于测试)

### 推荐配置
- **操作系统**：Windows 10/11, macOS 12+, 或 Linux (Ubuntu 20.04+)
- **内存**：8GB RAM 最少，16GB 推荐
- **磁盘空间**：10GB 可用空间

---

## 📥 获取源代码

### 方式1：使用Git克隆

```bash
git clone https://github.com/Tsaojason-cao/YanbaoCamera-Android.git
cd YanbaoCamera-Android
```

### 方式2：直接下载ZIP

访问 https://github.com/Tsaojason-cao/YanbaoCamera-Android/archive/refs/heads/main.zip

---

## 🔨 本地构建

### 步骤1：打开项目

```bash
# 使用Android Studio打开项目
# 或者使用命令行
cd YanbaoCamera-Android
```

### 步骤2：同步Gradle依赖

```bash
# 使用Gradle Wrapper（推荐）
./gradlew build

# 或者使用本地Gradle
gradle build
```

**首次构建可能需要10-15分钟**，因为需要下载所有依赖。

### 步骤3：构建Debug APK

```bash
./gradlew assembleDebug
```

**输出路径**：`app/build/outputs/apk/debug/app-debug.apk`

### 步骤4：构建Release APK（生产版本）

```bash
# 需要签名配置
./gradlew assembleRelease
```

**输出路径**：`app/build/outputs/apk/release/app-release.apk`

---

## 📱 安装到设备

### 方式1：使用ADB（Android Debug Bridge）

```bash
# 连接设备或启动模拟器
adb devices

# 安装APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 启动应用
adb shell am start -n com.yanbao.camera/.MainActivity
```

### 方式2：使用Android Studio

1. 连接设备或启动模拟器
2. 点击 "Run" 按钮或按 `Shift + F10`
3. 选择目标设备
4. 应用将自动构建并安装

### 方式3：直接安装APK

1. 将 `app-debug.apk` 复制到设备
2. 使用文件管理器打开APK
3. 按照提示安装

---

## 🔧 常见问题解决

### 问题1：Gradle同步失败

**症状**：`Failed to sync Gradle`

**解决方案**：
```bash
# 清理缓存
./gradlew clean

# 重新同步
./gradlew build --refresh-dependencies
```

### 问题2：SDK版本不兼容

**症状**：`Failed to find SDK with path: ...`

**解决方案**：
1. 打开 Android Studio
2. 进入 Tools → SDK Manager
3. 安装缺失的SDK平台和构建工具
4. 确保安装了 Android SDK 34

### 问题3：内存不足

**症状**：`OutOfMemoryError: Java heap space`

**解决方案**：
```bash
# 增加Gradle内存
export GRADLE_OPTS="-Xmx2048m"
./gradlew assembleDebug
```

### 问题4：权限错误

**症状**：`Permission denied: ./gradlew`

**解决方案**：
```bash
chmod +x gradlew
./gradlew assembleDebug
```

---

## 🚀 CI/CD构建（GitHub Actions）

### 自动构建流程

项目已配置GitHub Actions，每次推送到 `main` 分支时自动构建：

1. **检出代码** - 克隆最新源代码
2. **设置Java环境** - 安装JDK 17
3. **构建APK** - 运行 `./gradlew assembleDebug`
4. **上传Artifacts** - 保存生成的APK文件
5. **创建Release** - 发布到GitHub Releases

### 查看构建结果

访问：https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions

### 下载APK

1. 打开 GitHub Actions 页面
2. 点击最新的成功构建
3. 下载 "app-debug" artifact
4. 解压并安装APK

---

## 📊 项目结构

```
YanbaoCamera-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yanbao/camera/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── App.kt
│   │   │   │   ├── model/          # 数据模型
│   │   │   │   ├── viewmodel/      # ViewModel层
│   │   │   │   ├── repository/     # 数据仓库
│   │   │   │   ├── ui/
│   │   │   │   │   ├── screens/    # 7个屏幕
│   │   │   │   │   ├── components/ # UI组件
│   │   │   │   │   ├── theme/      # 主题配置
│   │   │   │   │   └── navigation/ # 导航配置
│   │   │   │   └── utils/          # 工具类
│   │   │   ├── res/                # 资源文件
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   # 单元测试
│   ├── build.gradle.kts            # 应用级构建配置
│   └── proguard-rules.pro          # 混淆规则
├── gradle/
│   └── wrapper/                    # Gradle Wrapper
├── build.gradle.kts                # 项目级构建配置
├── settings.gradle.kts             # 项目设置
├── gradle.properties               # Gradle属性
├── gradlew                         # Linux/Mac Gradle Wrapper
├── gradlew.bat                     # Windows Gradle Wrapper
├── .gitignore
├── README.md
└── BUILD_GUIDE.md                  # 本文件
```

---

## 📝 构建配置说明

### build.gradle.kts (应用级)

关键配置：
- **compileSdk**: 34 (Android 14)
- **targetSdk**: 34
- **minSdk**: 24 (Android 7.0)
- **Kotlin编译器版本**: 1.5.8
- **Jetpack Compose版本**: 1.6.0

### 主要依赖

| 库 | 版本 | 用途 |
|---|------|------|
| Jetpack Compose | 1.6.0 | UI框架 |
| CameraX | 1.3.0 | 相机功能 |
| Coil | 2.5.0 | 图片加载 |
| GPUImage | 2.1.0 | 图片滤镜 |
| Paging 3 | 3.2.1 | 分页加载 |
| Navigation | 2.7.6 | 页面导航 |

---

## 🧪 测试

### 运行单元测试

```bash
./gradlew test
```

### 运行集成测试

```bash
./gradlew connectedAndroidTest
```

### 使用Lint检查代码质量

```bash
./gradlew lint
```

---

## 📦 APK签名

### 生成签名密钥

```bash
keytool -genkey -v -keystore release.keystore \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias release
```

### 配置签名信息

编辑 `app/build.gradle.kts`：

```kotlin
signingConfigs {
    release {
        storeFile = file("release.keystore")
        storePassword = "your_password"
        keyAlias = "release"
        keyPassword = "your_password"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.release
    }
}
```

### 构建签名APK

```bash
./gradlew assembleRelease
```

---

## 📤 发布到应用商店

### Google Play Store

1. 生成签名的Release APK
2. 创建Google Play开发者账户
3. 上传APK到Google Play Console
4. 填写应用信息和权限
5. 提交审核

### 其他应用商店

- **华为应用市场**：https://appgallery.huawei.com
- **小米应用商店**：https://app.mi.com
- **OPPO应用商店**：https://open.oppomobile.com
- **VIVO应用商店**：https://dev.vivo.com.cn

---

## 🔗 有用的资源

- [Android官方文档](https://developer.android.com/)
- [Jetpack Compose文档](https://developer.android.com/jetpack/compose)
- [CameraX文档](https://developer.android.com/training/camerax)
- [Gradle官方文档](https://docs.gradle.org/)

---

## 📞 支持

如有问题，请：

1. 检查本指南的常见问题部分
2. 查看项目的GitHub Issues
3. 提交新的Issue或Pull Request

---

**最后更新**：2026-02-17  
**版本**：1.0.0
