#!/bin/bash

# ============================================================================
# 雁宝AI相机App - APK构建脚本
# ============================================================================
# 此脚本自动构建Debug和Release APK
# 使用: ./build-apk.sh [debug|release|all]
# ============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目信息
PROJECT_NAME="雁宝AI相机App"
APP_NAME="yanbao-camera-app"
VERSION="1.0.0"
BUILD_NUMBER="1"

# 目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/app/build"
OUTPUT_DIR="$PROJECT_DIR/build-output"
LOGS_DIR="$OUTPUT_DIR/logs"

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
# 检查环境
# ============================================================================

check_environment() {
    print_header "检查构建环境"
    
    # 检查Java
    if ! command -v java &> /dev/null; then
        print_error "Java未安装"
        exit 1
    fi
    JAVA_VERSION=$(java -version 2>&1 | grep 'version' | awk '{print $3}' | tr -d '"')
    print_success "Java版本: $JAVA_VERSION"
    
    # 检查Gradle
    if [ ! -f "$PROJECT_DIR/gradlew" ]; then
        print_error "gradlew不存在"
        exit 1
    fi
    print_success "Gradle Wrapper已找到"
    
    # 检查Android SDK
    if [ -z "$ANDROID_SDK_ROOT" ] && [ -z "$ANDROID_HOME" ]; then
        print_warning "ANDROID_SDK_ROOT/ANDROID_HOME未设置"
        print_info "尝试使用默认位置..."
        export ANDROID_SDK_ROOT=$HOME/Android/Sdk
    fi
    
    print_success "环境检查完成"
    echo ""
}

# ============================================================================
# 清理构建
# ============================================================================

clean_build() {
    print_header "清理旧的构建"
    
    print_info "删除build目录..."
    rm -rf "$BUILD_DIR"
    
    print_info "删除输出目录..."
    rm -rf "$OUTPUT_DIR"
    
    print_success "清理完成"
    echo ""
}

# ============================================================================
# 创建输出目录
# ============================================================================

create_output_dirs() {
    print_header "创建输出目录"
    
    mkdir -p "$OUTPUT_DIR"
    mkdir -p "$LOGS_DIR"
    mkdir -p "$OUTPUT_DIR/apk"
    mkdir -p "$OUTPUT_DIR/bundle"
    
    print_success "输出目录已创建"
    echo ""
}

# ============================================================================
# 构建Debug APK
# ============================================================================

build_debug_apk() {
    print_header "构建Debug APK"
    
    print_info "运行: ./gradlew assembleDebug"
    
    if ./gradlew assembleDebug \
        --no-daemon \
        --stacktrace \
        2>&1 | tee "$LOGS_DIR/debug-build.log"; then
        
        # 复制APK
        if [ -f "$BUILD_DIR/outputs/apk/debug/app-debug.apk" ]; then
            cp "$BUILD_DIR/outputs/apk/debug/app-debug.apk" "$OUTPUT_DIR/apk/"
            print_success "Debug APK构建成功"
            print_info "位置: $OUTPUT_DIR/apk/app-debug.apk"
            echo ""
            return 0
        else
            print_error "Debug APK文件未找到"
            return 1
        fi
    else
        print_error "Debug APK构建失败"
        print_info "查看日志: $LOGS_DIR/debug-build.log"
        return 1
    fi
}

# ============================================================================
# 构建Release APK
# ============================================================================

build_release_apk() {
    print_header "构建Release APK"
    
    print_info "运行: ./gradlew assembleRelease"
    
    if ./gradlew assembleRelease \
        --no-daemon \
        --stacktrace \
        2>&1 | tee "$LOGS_DIR/release-build.log"; then
        
        # 复制APK
        if [ -f "$BUILD_DIR/outputs/apk/release/app-release-unsigned.apk" ]; then
            cp "$BUILD_DIR/outputs/apk/release/app-release-unsigned.apk" "$OUTPUT_DIR/apk/app-release-unsigned.apk"
            print_success "Release APK构建成功"
            print_info "位置: $OUTPUT_DIR/apk/app-release-unsigned.apk"
            print_warning "注意: 这是未签名的APK，需要签名后才能发布"
            echo ""
            return 0
        else
            print_error "Release APK文件未找到"
            return 1
        fi
    else
        print_error "Release APK构建失败"
        print_info "查看日志: $LOGS_DIR/release-build.log"
        return 1
    fi
}

# ============================================================================
# 构建Release Bundle
# ============================================================================

build_release_bundle() {
    print_header "构建Release Bundle"
    
    print_info "运行: ./gradlew bundleRelease"
    
    if ./gradlew bundleRelease \
        --no-daemon \
        --stacktrace \
        2>&1 | tee "$LOGS_DIR/bundle-build.log"; then
        
        # 复制Bundle
        if [ -f "$BUILD_DIR/outputs/bundle/release/app-release.aab" ]; then
            cp "$BUILD_DIR/outputs/bundle/release/app-release.aab" "$OUTPUT_DIR/bundle/"
            print_success "Release Bundle构建成功"
            print_info "位置: $OUTPUT_DIR/bundle/app-release.aab"
            echo ""
            return 0
        else
            print_error "Release Bundle文件未找到"
            return 1
        fi
    else
        print_error "Release Bundle构建失败"
        print_info "查看日志: $LOGS_DIR/bundle-build.log"
        return 1
    fi
}

# ============================================================================
# 生成构建报告
# ============================================================================

generate_report() {
    print_header "生成构建报告"
    
    REPORT_FILE="$OUTPUT_DIR/BUILD_REPORT.txt"
    
    cat > "$REPORT_FILE" << EOF
================================================================================
                    $PROJECT_NAME - 构建报告
================================================================================

构建时间: $(date)
项目版本: $VERSION
构建号: $BUILD_NUMBER

================================================================================
构建结果
================================================================================

EOF

    # 检查Debug APK
    if [ -f "$OUTPUT_DIR/apk/app-debug.apk" ]; then
        DEBUG_SIZE=$(du -h "$OUTPUT_DIR/apk/app-debug.apk" | cut -f1)
        echo "✅ Debug APK: $DEBUG_SIZE" >> "$REPORT_FILE"
        echo "   位置: $OUTPUT_DIR/apk/app-debug.apk" >> "$REPORT_FILE"
    else
        echo "❌ Debug APK: 未生成" >> "$REPORT_FILE"
    fi

    # 检查Release APK
    if [ -f "$OUTPUT_DIR/apk/app-release-unsigned.apk" ]; then
        RELEASE_SIZE=$(du -h "$OUTPUT_DIR/apk/app-release-unsigned.apk" | cut -f1)
        echo "✅ Release APK (未签名): $RELEASE_SIZE" >> "$REPORT_FILE"
        echo "   位置: $OUTPUT_DIR/apk/app-release-unsigned.apk" >> "$REPORT_FILE"
    else
        echo "❌ Release APK: 未生成" >> "$REPORT_FILE"
    fi

    # 检查Release Bundle
    if [ -f "$OUTPUT_DIR/bundle/app-release.aab" ]; then
        BUNDLE_SIZE=$(du -h "$OUTPUT_DIR/bundle/app-release.aab" | cut -f1)
        echo "✅ Release Bundle: $BUNDLE_SIZE" >> "$REPORT_FILE"
        echo "   位置: $OUTPUT_DIR/bundle/app-release.aab" >> "$REPORT_FILE"
    else
        echo "❌ Release Bundle: 未生成" >> "$REPORT_FILE"
    fi

    cat >> "$REPORT_FILE" << EOF

================================================================================
下一步
================================================================================

1. Debug APK:
   - 可直接安装到设备进行测试
   - 命令: adb install -r app-debug.apk

2. Release APK (未签名):
   - 需要签名后才能发布
   - 签名命令: jarsigner -verbose -sigalg SHA256withRSA ...
   - 对齐命令: zipalign -v 4 ...

3. Release Bundle:
   - 用于发布到Google Play
   - 需要签名后上传

================================================================================
日志文件
================================================================================

Debug构建日志: $LOGS_DIR/debug-build.log
Release构建日志: $LOGS_DIR/release-build.log
Bundle构建日志: $LOGS_DIR/bundle-build.log

================================================================================
EOF

    print_success "构建报告已生成"
    print_info "位置: $REPORT_FILE"
    echo ""
    
    cat "$REPORT_FILE"
}

# ============================================================================
# 显示使用帮助
# ============================================================================

show_help() {
    cat << EOF
使用方法: $0 [选项]

选项:
  debug       构建Debug APK
  release     构建Release APK和Bundle
  all         构建所有 (Debug + Release + Bundle)
  clean       清理构建文件
  help        显示此帮助信息

示例:
  $0 debug              # 构建Debug APK
  $0 release            # 构建Release APK
  $0 all                # 构建所有
  $0 clean && $0 all    # 清理并构建所有

输出目录: $OUTPUT_DIR

EOF
}

# ============================================================================
# 主函数
# ============================================================================

main() {
    print_header "$PROJECT_NAME - APK构建脚本"
    
    # 解析命令行参数
    BUILD_TYPE="${1:-all}"
    
    case "$BUILD_TYPE" in
        debug)
            check_environment
            create_output_dirs
            build_debug_apk
            generate_report
            ;;
        release)
            check_environment
            create_output_dirs
            build_release_apk
            build_release_bundle
            generate_report
            ;;
        all)
            check_environment
            create_output_dirs
            build_debug_apk
            build_release_apk
            build_release_bundle
            generate_report
            ;;
        clean)
            clean_build
            ;;
        help)
            show_help
            ;;
        *)
            print_error "未知的选项: $BUILD_TYPE"
            show_help
            exit 1
            ;;
    esac
    
    print_header "构建完成"
    print_success "所有APK已生成到: $OUTPUT_DIR"
    echo ""
}

# ============================================================================
# 执行主函数
# ============================================================================

cd "$PROJECT_DIR"
main "$@"
