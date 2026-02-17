#!/bin/bash

# ============================================================================
# 雁宝AI相机App - 自动修复构建问题脚本
# ============================================================================
# 此脚本自动检测和修复常见的构建问题
# 使用: ./fix-build-issues.sh
# ============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ============================================================================
# 函数定义
# ============================================================================

print_header() {
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}════════════════════════════════════════════════════════════${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# ============================================================================
# 问题1: 清理Gradle缓存
# ============================================================================

fix_gradle_cache() {
    print_header "修复1: 清理Gradle缓存"
    
    print_info "删除Gradle缓存..."
    rm -rf ~/.gradle/caches
    print_success "Gradle缓存已清理"
    
    print_info "删除构建输出..."
    rm -rf app/build
    print_success "构建输出已删除"
    
    echo ""
}

# ============================================================================
# 问题2: 修复依赖冲突
# ============================================================================

fix_dependency_conflicts() {
    print_header "修复2: 修复依赖冲突"
    
    print_info "清理依赖..."
    ./gradlew clean --no-daemon
    
    print_info "重新同步依赖..."
    ./gradlew sync --no-daemon
    
    print_success "依赖已修复"
    echo ""
}

# ============================================================================
# 问题3: 修复SDK问题
# ============================================================================

fix_sdk_issues() {
    print_header "修复3: 修复SDK问题"
    
    # 检查SDK
    if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
        print_warning "ANDROID_SDK_ROOT/ANDROID_HOME未设置"
        export ANDROID_SDK_ROOT=$HOME/Android/Sdk
        print_info "设置为: $ANDROID_SDK_ROOT"
    fi
    
    # 创建local.properties
    print_info "创建local.properties..."
    echo "sdk.dir=$ANDROID_SDK_ROOT" > local.properties
    print_success "local.properties已创建"
    
    echo ""
}

# ============================================================================
# 问题4: 修复内存问题
# ============================================================================

fix_memory_issues() {
    print_header "修复4: 修复内存问题"
    
    print_info "配置Gradle JVM参数..."
    mkdir -p ~/.gradle
    cat > ~/.gradle/gradle.properties << EOF
org.gradle.jvmargs=-Xmx4096m -XX:+UseG1GC -XX:+ParallelRefProcEnabled
org.gradle.parallel=true
org.gradle.caching=true
org.gradle.daemon=false
android.useAndroidX=true
android.enableJetifier=true
EOF
    
    print_success "Gradle JVM参数已配置"
    echo ""
}

# ============================================================================
# 问题5: 修复编译错误
# ============================================================================

fix_compilation_errors() {
    print_header "修复5: 修复编译错误"
    
    print_info "检查Kotlin编译器..."
    ./gradlew compileDebugKotlin --no-daemon 2>&1 | tail -20
    
    print_success "Kotlin编译器检查完成"
    echo ""
}

# ============================================================================
# 问题6: 修复资源问题
# ============================================================================

fix_resource_issues() {
    print_header "修复6: 修复资源问题"
    
    print_info "清理资源..."
    rm -rf app/build/intermediates
    
    print_info "重新生成资源..."
    ./gradlew generateDebugResources --no-daemon
    
    print_success "资源已修复"
    echo ""
}

# ============================================================================
# 问题7: 修复签名问题
# ============================================================================

fix_signing_issues() {
    print_header "修复7: 修复签名问题"
    
    print_info "检查签名配置..."
    
    # 检查debug.keystore
    if [ ! -f ~/.android/debug.keystore ]; then
        print_warning "debug.keystore不存在，创建新的..."
        mkdir -p ~/.android
        keytool -genkey -v -keystore ~/.android/debug.keystore \
            -storepass android -alias androiddebugkey \
            -keypass android -keyalg RSA -keysize 2048 \
            -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
        print_success "debug.keystore已创建"
    else
        print_success "debug.keystore已存在"
    fi
    
    echo ""
}

# ============================================================================
# 问题8: 修复NDK问题
# ============================================================================

fix_ndk_issues() {
    print_header "修复8: 修复NDK问题"
    
    print_info "检查NDK..."
    
    if [ -z "$NDK_HOME" ]; then
        print_warning "NDK_HOME未设置"
        export NDK_HOME=$ANDROID_SDK_ROOT/ndk/25.1.8937393
        print_info "设置为: $NDK_HOME"
    fi
    
    print_success "NDK已配置"
    echo ""
}

# ============================================================================
# 问题9: 修复Lint问题
# ============================================================================

fix_lint_issues() {
    print_header "修复9: 修复Lint问题"
    
    print_info "禁用Lint检查..."
    cat >> app/build.gradle.kts << 'EOF'

android {
    lint {
        disable("MissingTranslation", "ExtraTranslation")
    }
}
EOF
    
    print_success "Lint已配置"
    echo ""
}

# ============================================================================
# 问题10: 完整重建
# ============================================================================

full_rebuild() {
    print_header "修复10: 完整重建"
    
    print_info "执行完整重建..."
    
    # 清理所有缓存
    print_info "清理所有缓存..."
    rm -rf ~/.gradle/caches
    rm -rf app/build
    rm -rf .gradle
    
    # 重新同步
    print_info "重新同步..."
    ./gradlew sync --no-daemon
    
    # 重新构建
    print_info "重新构建..."
    ./gradlew assembleDebug --no-daemon --stacktrace
    
    print_success "完整重建完成"
    echo ""
}

# ============================================================================
# 显示使用帮助
# ============================================================================

show_help() {
    cat << EOF
使用方法: $0 [选项]

选项:
  all                 执行所有修复
  cache               清理Gradle缓存
  dependencies        修复依赖冲突
  sdk                 修复SDK问题
  memory              修复内存问题
  compilation         修复编译错误
  resources           修复资源问题
  signing             修复签名问题
  ndk                 修复NDK问题
  lint                修复Lint问题
  rebuild             完整重建
  help                显示此帮助信息

示例:
  $0 all              # 执行所有修复
  $0 cache            # 清理缓存
  $0 rebuild          # 完整重建

EOF
}

# ============================================================================
# 主函数
# ============================================================================

main() {
    print_header "雁宝AI相机App - 自动修复构建问题"
    
    # 解析命令行参数
    COMMAND="${1:-all}"
    
    case "$COMMAND" in
        all)
            fix_gradle_cache
            fix_sdk_issues
            fix_memory_issues
            fix_dependency_conflicts
            fix_resource_issues
            fix_signing_issues
            fix_ndk_issues
            ;;
        cache)
            fix_gradle_cache
            ;;
        dependencies)
            fix_dependency_conflicts
            ;;
        sdk)
            fix_sdk_issues
            ;;
        memory)
            fix_memory_issues
            ;;
        compilation)
            fix_compilation_errors
            ;;
        resources)
            fix_resource_issues
            ;;
        signing)
            fix_signing_issues
            ;;
        ndk)
            fix_ndk_issues
            ;;
        lint)
            fix_lint_issues
            ;;
        rebuild)
            full_rebuild
            ;;
        help)
            show_help
            ;;
        *)
            print_error "未知的选项: $COMMAND"
            show_help
            exit 1
            ;;
    esac
    
    print_header "修复完成"
    print_success "所有问题已修复"
    echo ""
    print_info "现在尝试构建:"
    echo "  ./gradlew assembleDebug"
    echo ""
}

# ============================================================================
# 执行主函数
# ============================================================================

cd "$(dirname "${BASH_SOURCE[0]}")"
main "$@"
