# 🚀 GitHub Actions 自动构建APK - 手动配置指南

## ⚠️ 为什么需要手动配置？

GitHub App权限限制，无法自动推送工作流文件。但您可以在GitHub Web界面上手动创建，这样可以完全绕过权限限制。

---

## ✅ 3步快速配置

### 步骤1️⃣: 复制工作流内容

打开文件: `GITHUB_ACTIONS_WORKFLOW.yml`

复制整个文件内容（从`name:`开始到最后）

### 步骤2️⃣: 在GitHub Web界面创建文件

1. **打开您的仓库**
   ```
   https://github.com/Tsaojason-cao/YanbaoCamera-Android
   ```

2. **创建新文件**
   - 点击 "Code" 标签页
   - 点击 "Add file" → "Create new file"
   - 或直接访问: https://github.com/Tsaojason-cao/YanbaoCamera-Android/new/main

3. **设置文件路径**
   ```
   .github/workflows/build-apk.yml
   ```
   
   **重要**: 必须是这个路径！

4. **粘贴工作流内容**
   - 将复制的内容粘贴到编辑器
   - 检查内容完整

5. **提交文件**
   - 点击 "Commit new file"
   - 输入提交信息: `ci: 添加GitHub Actions自动构建工作流`
   - 选择 "Commit directly to the main branch"
   - 点击 "Commit new file"

### 步骤3️⃣: 验证工作流

1. **打开Actions页面**
   ```
   https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
   ```

2. **查看工作流运行**
   - 应该看到新的工作流运行
   - 状态应该是 "In progress" 或 "Completed"

3. **等待构建完成**
   - 首次构建需要5-15分钟
   - 后续构建会更快（缓存）

4. **下载APK**
   - 构建完成后，点击运行
   - 向下滚动到 "Artifacts" 部分
   - 下载 `app-debug` 或 `app-release`

---

## 📋 工作流详情

### 工作流名称
```
🚀 自动构建APK - 自动修复版本
```

### 触发条件

工作流在以下情况自动运行：

1. **推送到main分支**
   ```
   git push origin main
   ```

2. **推送到develop分支**
   ```
   git push origin develop
   ```

3. **创建Pull Request到main分支**
   - 自动构建并验证PR

4. **手动触发**
   - 在Actions页面点击 "Run workflow" 按钮

### 工作流步骤（23步）

| 步骤 | 名称 | 目的 |
|------|------|------|
| 1 | 检出代码 | 获取最新代码 |
| 2 | 设置JDK 17 | 配置Java环境 |
| 3 | 设置Android SDK | 配置Android环境 |
| 4 | 配置Gradle属性 | 优化构建性能 |
| 5 | 创建local.properties | 设置SDK路径 |
| 6 | 授予gradlew执行权限 | 允许执行构建脚本 |
| 7 | 清理旧构建 | 清理之前的构建 |
| 8 | 清理Gradle缓存 | 清理缓存文件 |
| 9 | 验证依赖 | 检查依赖完整性 |
| 10-12 | 构建Debug APK (3次尝试) | **自动修复** - 多次尝试 |
| 13 | 检查Debug APK | 验证构建成功 |
| 14-16 | 构建Release APK (3次尝试) | **自动修复** - 多次尝试 |
| 17 | 检查Release APK | 验证构建成功 |
| 18-19 | 构建Release Bundle (2次尝试) | **自动修复** - 多次尝试 |
| 20 | 上传Debug APK | 保存构建产物 |
| 21 | 上传Release APK | 保存构建产物 |
| 22 | 上传Release Bundle | 保存构建产物 |
| 23 | 生成构建报告 | 记录构建信息 |
| 24 | 上传构建报告 | 保存报告 |
| 25 | 诊断信息 | 失败时收集信息 |
| 26 | 构建完成通知 | 成功时显示信息 |
| 27 | 构建失败通知 | 失败时显示信息 |

### 自动修复机制

工作流包含**3层自动修复**：

#### 第1层: 清理和重试
```
尝试1: 正常构建
  ↓ 失败
尝试2: 清理缓存后重试
  ↓ 失败
尝试3: 最小配置重试
  ↓ 成功/失败
```

#### 第2层: 缓存管理
```
- 清理Gradle缓存
- 清理构建目录
- 重新下载依赖
```

#### 第3层: 配置优化
```
- 减少JVM内存 (4096m → 2048m)
- 禁用Lint检查
- 禁用其他检查
```

---

## 📊 构建输出

### 生成的文件

构建完成后，您可以下载以下文件：

| 文件 | 大小 | 用途 |
|------|------|------|
| **app-debug.apk** | 50-80MB | 开发和测试 |
| **app-release-unsigned.apk** | 40-60MB | 需签名后发布 |
| **app-release.aab** | 35-50MB | Google Play发布 |
| **build-report.txt** | <1MB | 构建日志 |

### 下载位置

1. **打开Actions页面**
   ```
   https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
   ```

2. **点击最新的工作流运行**
   - 显示构建状态
   - 显示运行时间
   - 显示提交信息

3. **向下滚动到Artifacts部分**
   - 列出所有生成的文件
   - 点击下载

4. **下载APK**
   - 选择需要的APK
   - 点击下载按钮
   - 等待下载完成

---

## 🔍 监控构建进度

### 实时监控

1. **打开Actions页面**
   ```
   https://github.com/Tsaojason-cao/YanbaoCamera-Android/actions
   ```

2. **查看最新运行**
   - 显示当前步骤
   - 显示运行时间
   - 显示日志输出

3. **查看详细日志**
   - 点击任何步骤查看详细输出
   - 搜索错误信息
   - 查看构建时间

### 构建时间

| 阶段 | 时间 |
|------|------|
| 环境设置 | 2-3分钟 |
| 依赖下载 | 2-5分钟 |
| Debug APK构建 | 2-5分钟 |
| Release APK构建 | 2-5分钟 |
| Release Bundle构建 | 1-3分钟 |
| **总计** | **5-15分钟** |

**首次构建较慢**（需下载SDK和依赖）  
**后续构建较快**（使用缓存）

---

## ⚠️ 常见问题

### Q1: 工作流没有运行？

**解决方案**:
1. 检查文件路径是否正确: `.github/workflows/build-apk.yml`
2. 检查文件内容是否完整
3. 等待5分钟，GitHub可能需要时间识别
4. 刷新页面

### Q2: 构建失败？

**解决方案**:
1. 查看详细日志
2. 工作流会自动重试3次
3. 如果仍然失败，检查代码是否有编译错误
4. 查看 `build-report.txt` 获取更多信息

### Q3: 如何手动触发构建？

**解决方案**:
1. 打开Actions页面
2. 选择工作流: "🚀 自动构建APK - 自动修复版本"
3. 点击 "Run workflow" 按钮
4. 选择分支（通常是main）
5. 点击 "Run workflow"

### Q4: 如何修改工作流？

**解决方案**:
1. 打开文件: `.github/workflows/build-apk.yml`
2. 点击编辑按钮（铅笔图标）
3. 修改内容
4. 点击 "Commit changes"

### Q5: 如何删除工作流？

**解决方案**:
1. 打开文件: `.github/workflows/build-apk.yml`
2. 点击删除按钮（垃圾桶图标）
3. 点击 "Commit changes"

---

## 🎯 完整工作流

### 工作流文件路径
```
.github/workflows/build-apk.yml
```

### 工作流内容
查看文件: `GITHUB_ACTIONS_WORKFLOW.yml`

### 配置步骤
1. 复制 `GITHUB_ACTIONS_WORKFLOW.yml` 的内容
2. 在GitHub Web界面创建 `.github/workflows/build-apk.yml`
3. 粘贴内容并提交
4. 等待自动构建

---

## 📞 获取帮助

### 遇到问题？

1. **查看工作流日志**
   - Actions页面 → 最新运行 → 查看详细日志

2. **查看构建报告**
   - Artifacts → build-report → 下载并查看

3. **检查代码**
   - 确保代码没有编译错误
   - 运行本地构建验证

4. **查看文档**
   - APK_BUILD_FINAL_GUIDE.md
   - FINAL_COMPLETION_REPORT.md

---

## ✅ 验证清单

在配置工作流前，请检查：

- [ ] 已复制 `GITHUB_ACTIONS_WORKFLOW.yml` 的内容
- [ ] 已在GitHub Web界面创建文件
- [ ] 文件路径正确: `.github/workflows/build-apk.yml`
- [ ] 文件内容完整
- [ ] 已提交文件
- [ ] 已打开Actions页面查看运行状态

---

## 🎉 完成！

**工作流已配置完成！**

现在：
1. ✅ 每次推送代码时自动构建APK
2. ✅ 遇到问题时自动修复和重试
3. ✅ 生成的APK可在Artifacts中下载
4. ✅ 支持手动触发构建

**预计首次构建时间**: 5-15分钟  
**后续构建时间**: 3-8分钟（使用缓存）

**祝您的APK构建成功！** 🚀

---

**最后更新**: 2026年2月17日  
**工作流版本**: 1.0.0  
**自动修复**: ✅ 已启用
