# APK构建指南

## 🎯 目标

生成完整可用的APK文件，用于安装和测试。

---

## 📋 前置条件

### 1. 系统要求
- Android SDK 34+
- Gradle 8.0+
- JDK 11+
- 至少4GB RAM

### 2. 环境配置

```bash
# 检查Android SDK
echo $ANDROID_SDK_ROOT

# 检查Gradle
gradle --version

# 检查JDK
java -version
```

---

## 🚀 构建步骤

### 方式1：使用Gradle构建（推荐）

#### 1. 清理项目
```bash
cd /tmp/YanbaoCamera_Complete
./gradlew clean
```

#### 2. 构建Debug APK
```bash
./gradlew assembleDebug
```

**输出位置**：
```
app/build/outputs/apk/debug/app-debug.apk
```

#### 3. 构建Release APK（需要签名）
```bash
./gradlew assembleRelease
```

**输出位置**：
```
app/build/outputs/apk/release/app-release.apk
```

---

### 方式2：使用Android Studio

1. 打开项目
2. 菜单：Build → Build Bundle(s) / APK(s) → Build APK(s)
3. 等待构建完成
4. 在Build输出窗口中找到APK路径

---

## 📦 APK信息

### Debug APK
- **文件名**：app-debug.apk
- **大小**：约50-80MB
- **签名**：Debug签名（用于测试）
- **用途**：开发和测试

### Release APK
- **文件名**：app-release.apk
- **大小**：约40-60MB
- **签名**：需要自签名或使用发布密钥
- **用途**：发布到Google Play Store

---

## 🔑 签名配置（Release）

### 1. 生成签名密钥

```bash
keytool -genkey -v -keystore yanbao-camera.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias yanbao-camera-key
```

### 2. 配置build.gradle.kts

```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("path/to/yanbao-camera.jks")
            storePassword = "your_password"
            keyAlias = "yanbao-camera-key"
            keyPassword = "your_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 3. 构建Release APK

```bash
./gradlew assembleRelease
```

---

## 📱 安装APK

### 方式1：使用ADB

```bash
# 连接设备
adb devices

# 安装APK
adb install app/build/outputs/apk/debug/app-debug.apk

# 卸载应用
adb uninstall com.yanbao.camera
```

### 方式2：直接拖放

1. 将APK文件拖放到Android设备
2. 使用文件管理器打开APK
3. 点击安装

### 方式3：使用Android Studio

1. 菜单：Run → Run 'app'
2. 选择目标设备
3. 自动构建和安装

---

## 🧪 测试清单

### 功能测试

#### Splash屏幕
- [ ] 显示库洛米角色
- [ ] 进度条动画
- [ ] 3秒后自动跳转

#### Home屏幕
- [ ] 推荐卡片显示
- [ ] 点赞功能
- [ ] 搜索功能

#### Camera屏幕
- [ ] 相机预览
- [ ] 模式选择
- [ ] 参数调节
- [ ] 拍照功能

#### Edit屏幕
- [ ] 滤镜应用
- [ ] 参数调节
- [ ] AI增强

#### Gallery屏幕
- [ ] 相册显示
- [ ] 照片预览
- [ ] 分享功能

#### Recommend屏幕
- [ ] 搜索功能
- [ ] 推荐显示
- [ ] 地图集成

#### Profile屏幕
- [ ] 用户信息
- [ ] 统计数据
- [ ] 菜单功能

### 性能测试

- [ ] 启动时间 < 2秒
- [ ] 内存占用 < 150MB
- [ ] 帧率 > 50fps
- [ ] 没有明显卡顿

### 兼容性测试

- [ ] Android 7.0+
- [ ] 竖屏/横屏
- [ ] 不同屏幕尺寸
- [ ] 不同设备型号

---

## 🐛 常见问题

### 问题1：Gradle构建失败

**症状**：`./gradlew assembleDebug` 失败

**解决方案**：
```bash
# 清理缓存
./gradlew clean

# 更新依赖
./gradlew build --refresh-dependencies

# 重新构建
./gradlew assembleDebug
```

### 问题2：找不到Android SDK

**症状**：`ANDROID_SDK_ROOT not set`

**解决方案**：
```bash
# 设置环境变量
export ANDROID_SDK_ROOT=~/Android/Sdk
export PATH=$PATH:$ANDROID_SDK_ROOT/tools:$ANDROID_SDK_ROOT/platform-tools
```

### 问题3：内存不足

**症状**：`OutOfMemoryError`

**解决方案**：
```bash
# 增加Gradle内存
export GRADLE_OPTS="-Xmx4g"
./gradlew assembleDebug
```

### 问题4：APK太大

**症状**：APK文件 > 100MB

**解决方案**：
```kotlin
// 在build.gradle.kts中启用分割APK
android {
    bundle {
        density.enableSplit = true
        language.enableSplit = true
        abi.enableSplit = true
    }
}
```

---

## 📊 构建输出

### 成功构建

```
BUILD SUCCESSFUL in 2m 30s
```

### APK位置

```
app/build/outputs/apk/debug/app-debug.apk
```

### 文件信息

```bash
# 查看APK信息
aapt dump badging app/build/outputs/apk/debug/app-debug.apk

# 查看APK大小
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

---

## 🎯 发布到Google Play Store

### 1. 准备Release APK

```bash
./gradlew assembleRelease
```

### 2. 上传到Google Play Console

1. 访问 https://play.google.com/console
2. 创建新应用
3. 上传Release APK
4. 填写应用信息
5. 提交审核

### 3. 应用信息

- **应用名称**：雁宝AI相机
- **包名**：com.yanbao.camera
- **版本号**：1.0.0
- **最小SDK**：24
- **目标SDK**：34

---

## ✅ 构建检查清单

- [ ] 项目编译无错误
- [ ] 所有依赖已下载
- [ ] APK文件已生成
- [ ] APK大小合理（< 100MB）
- [ ] APK可以安装
- [ ] 应用可以启动
- [ ] 所有功能正常
- [ ] 没有崩溃

---

## 📞 支持

如果遇到问题，请检查：

1. **Gradle日志**：`app/build/outputs/` 目录
2. **Android Studio日志**：Logcat窗口
3. **设备日志**：`adb logcat`
4. **GitHub Issues**：项目仓库

---

**准备构建APK了吗？** 🚀
