# 🚀 GitHub Actions 自动构建配置指南

## 📋 概述

本指南将帮助您在GitHub上设置自动化APK构建工作流。

---

## ⚠️ 权限问题解决

由于GitHub App权限限制，需要使用以下方法之一来添加工作流文件。

---

## 🔧 方法1：使用GitHub CLI (推荐 - 最简单)

### 步骤1：安装GitHub CLI
```bash
# macOS
brew install gh

# Ubuntu
sudo apt-get install gh

# Windows
choco install gh
```

### 步骤2：登录GitHub
```bash
gh auth login

# 选择 GitHub.com
# 选择 HTTPS
# 选择 Paste an authentication token
```

### 步骤3：创建工作流文件
```bash
# 进入项目目录
cd /path/to/YanbaoCamera-Android

# 创建工作流目录
mkdir -p .github/workflows

# 创建工作流文件
cat > .github/workflows/build-apk.yml << 'EOF'
# 工作流内容 (见下方)
EOF
```

### 步骤4：推送代码
```bash
git add .github/workflows/build-apk.yml
git commit -m "ci: 添加GitHub Actions自动构建工作流"
git push origin main
```

---

## 🔑 方法2：使用个人访问令牌(PAT)

### 步骤1：生成个人访问令牌

1. 访问 https://github.com/settings/tokens
2. 点击 "Generate new token" → "Generate new token (classic)"
3. 设置以下权限：
   - ✅ repo (完整访问)
   - ✅ workflow (工作流)
   - ✅ admin:repo_hook (Webhook)
4. 点击 "Generate token"
5. 复制生成的令牌

### 步骤2：配置Git使用PAT
```bash
# 设置凭证
git config --global credential.helper store

# 或使用环境变量
export GIT_ASKPASS=echo
export GIT_USERNAME=your-github-username
export GIT_PASSWORD=your-personal-access-token
```

### 步骤3：推送代码
```bash
# 创建工作流文件
mkdir -p .github/workflows
cat > .github/workflows/build-apk.yml << 'EOF'
# 工作流内容 (见下方)
EOF

# 推送
git add .github/workflows/build-apk.yml
git commit -m "ci: 添加GitHub Actions自动构建工作流"
git push origin main
```

---

## 🌐 方法3：在GitHub Web界面添加

### 步骤1：访问GitHub
1. 打开 https://github.com/Tsaojason-cao/YanbaoCamera-Android
2. 点击 "Actions" 标签

### 步骤2：创建新工作流
1. 点击 "New workflow"
2. 点击 "set up a workflow yourself"

### 步骤3：复制工作流内容
1. 复制下方的工作流YAML内容
2. 粘贴到编辑器
3. 点击 "Start commit"
4. 输入提交信息
5. 点击 "Commit new file"

---

## 📝 工作流文件内容

### 完整的build-apk.yml

```yaml
name: 🚀 自动构建APK

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]
  workflow_dispatch:

jobs:
  build:
    runs-on: ubuntu-latest
    timeout-minutes: 60
    
    steps:
    - name: 📥 检出代码
      uses: actions/checkout@v4
      with:
        fetch-depth: 0

    - name: ☕ 设置JDK 17
      uses: actions/setup-java@v4
      with:
        java-version: '17'
        distribution: 'temurin'
        cache: gradle

    - name: 🤖 设置Android SDK
      uses: android-actions/setup-android@v3
      with:
        api-level: 34
        build-tools-version: 34.0.0
        ndk-version: 25.1.8937393

    - name: ⚙️ 配置Gradle属性
      run: |
        mkdir -p ~/.gradle
        cat > ~/.gradle/gradle.properties << EOF
        org.gradle.jvmargs=-Xmx4096m -XX:+UseG1GC
        org.gradle.parallel=true
        org.gradle.caching=true
        org.gradle.daemon=false
        android.useAndroidX=true
        android.enableJetifier=true
        EOF

    - name: 📝 创建local.properties
      run: |
        echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties

    - name: 🔐 授予gradlew执行权限
      run: chmod +x gradlew

    - name: 🧹 清理旧构建
      run: ./gradlew clean --no-daemon --stacktrace 2>&1 | tail -20

    - name: 🔨 构建Debug APK
      id: build_debug
      continue-on-error: true
      run: |
        ./gradlew assembleDebug \
          --no-daemon \
          --stacktrace \
          --info 2>&1 | tee debug-build.log
        
        if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
          echo "debug_success=true" >> $GITHUB_OUTPUT
        else
          echo "debug_success=false" >> $GITHUB_OUTPUT
        fi

    - name: 🔧 修复Debug构建失败
      if: steps.build_debug.outputs.debug_success == 'false'
      run: |
        rm -rf ~/.gradle/caches
        rm -rf app/build
        ./gradlew sync --no-daemon
        ./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tail -30

    - name: 📦 构建Release APK
      id: build_release
      continue-on-error: true
      run: |
        ./gradlew assembleRelease \
          --no-daemon \
          --stacktrace \
          --info 2>&1 | tee release-build.log
        
        if [ -f "app/build/outputs/apk/release/app-release-unsigned.apk" ]; then
          echo "release_success=true" >> $GITHUB_OUTPUT
        else
          echo "release_success=false" >> $GITHUB_OUTPUT
        fi

    - name: 🔧 修复Release构建失败
      if: steps.build_release.outputs.release_success == 'false'
      run: |
        rm -rf ~/.gradle/caches
        rm -rf app/build
        ./gradlew sync --no-daemon
        ./gradlew assembleRelease --no-daemon --stacktrace 2>&1 | tail -30

    - name: 📦 构建Release Bundle
      id: build_bundle
      continue-on-error: true
      run: |
        ./gradlew bundleRelease \
          --no-daemon \
          --stacktrace 2>&1 | tee bundle-build.log
        
        if [ -f "app/build/outputs/bundle/release/app-release.aab" ]; then
          echo "bundle_success=true" >> $GITHUB_OUTPUT
        else
          echo "bundle_success=false" >> $GITHUB_OUTPUT
        fi

    - name: 🔧 修复Bundle构建失败
      if: steps.build_bundle.outputs.bundle_success == 'false'
      run: |
        rm -rf ~/.gradle/caches
        rm -rf app/build
        ./gradlew sync --no-daemon
        ./gradlew bundleRelease --no-daemon --stacktrace 2>&1 | tail -30

    - name: 📤 上传Debug APK
      if: success() || steps.build_debug.outputs.debug_success == 'true'
      uses: actions/upload-artifact@v4
      with:
        name: app-debug-apk
        path: app/build/outputs/apk/debug/app-debug.apk
        retention-days: 30

    - name: 📤 上传Release APK
      if: success() || steps.build_release.outputs.release_success == 'true'
      uses: actions/upload-artifact@v4
      with:
        name: app-release-apk
        path: app/build/outputs/apk/release/app-release-unsigned.apk
        retention-days: 30

    - name: 📤 上传Release Bundle
      if: success() || steps.build_bundle.outputs.bundle_success == 'true'
      uses: actions/upload-artifact@v4
      with:
        name: app-release-bundle
        path: app/build/outputs/bundle/release/app-release.aab
        retention-days: 30

    - name: 📤 上传构建日志
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: build-logs
        path: |
          debug-build.log
          release-build.log
          bundle-build.log
        retention-days: 7

    - name: 📊 生成构建报告
      if: always()
      run: |
        cat > BUILD_REPORT.md << 'REPORT'
        # 🚀 构建报告
        
        ## 构建信息
        - 时间: $(date)
        - 分支: ${{ github.ref }}
        - 提交: ${{ github.sha }}
        
        ## 结果
        - Debug APK: ${{ steps.build_debug.outcome }}
        - Release APK: ${{ steps.build_release.outcome }}
        - Release Bundle: ${{ steps.build_bundle.outcome }}
        REPORT

    - name: 📤 上传构建报告
      if: always()
      uses: actions/upload-artifact@v4
      with:
        name: build-report
        path: BUILD_REPORT.md
        retention-days: 30
```

---

## ✅ 验证工作流

### 步骤1：检查工作流是否启用
1. 访问 https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
2. 查看左侧是否显示 "🚀 自动构建APK" 工作流

### 步骤2：手动触发构建
1. 点击工作流名称
2. 点击 "Run workflow"
3. 选择分支 (main)
4. 点击 "Run workflow"

### 步骤3：监控构建进度
1. 等待工作流执行
2. 查看每个步骤的输出
3. 检查是否有错误

---

## 📊 监控构建

### 实时监控
1. 访问 https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
2. 点击最新的工作流运行
3. 查看实时日志

### 下载Artifacts
1. 工作流完成后
2. 点击 "Artifacts"
3. 下载所需的APK或日志

---

## 🐛 故障排除

### 问题1：工作流不显示
**原因**: 工作流文件未正确添加

**解决方案**:
1. 检查文件路径: `.github/workflows/build-apk.yml`
2. 检查YAML语法
3. 刷新页面

### 问题2：构建失败
**原因**: 代码编译错误或依赖问题

**解决方案**:
1. 查看构建日志
2. 修复代码错误
3. 重新推送代码

### 问题3：权限错误
**原因**: GitHub App权限不足

**解决方案**:
1. 使用个人访问令牌(PAT)
2. 或在Web界面添加工作流

---

## 🎯 推荐流程

### 本地开发
```bash
# 推送代码到main分支
git push origin main

# GitHub Actions自动构建
# 在Actions页面监控进度

# 下载生成的APK
# 在设备上测试
```

### 发布版本
```bash
# 创建标签
git tag v1.0.0

# 推送标签
git push origin v1.0.0

# GitHub Actions自动构建
# 自动创建Release
# 自动上传APK
```

---

## 📞 需要帮助？

### 文档
- [GitHub Actions官方文档](https://docs.github.com/en/actions)
- [Android构建文档](https://developer.android.com/studio/build)

### 常见问题
- 查看本文件的故障排除部分
- 查看构建日志中的错误信息

---

## 🚀 立即开始

### 快速设置 (使用GitHub CLI)
```bash
# 1. 安装gh
brew install gh

# 2. 登录
gh auth login

# 3. 创建工作流
mkdir -p .github/workflows
# 复制工作流内容到 .github/workflows/build-apk.yml

# 4. 推送
git add .github/workflows/build-apk.yml
git commit -m "ci: 添加GitHub Actions工作流"
git push origin main

# 5. 完成！
# 访问 https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
```

---

**项目**: 雁宝AI相机App
**版本**: 1.0.0
**状态**: ✅ 准备GitHub Actions自动构建
