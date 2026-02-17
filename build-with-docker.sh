#!/bin/bash

# ============================================================================
# 雁宝AI相机App - Docker构建脚本
# ============================================================================
# 此脚本使用Docker构建APK
# 使用: ./build-with-docker.sh [build|run|clean]
# ============================================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 项目信息
PROJECT_NAME="雁宝AI相机App"
IMAGE_NAME="yanbao-camera-builder"
IMAGE_TAG="latest"
CONTAINER_NAME="yanbao-camera-build"

# 目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$PROJECT_DIR/docker-output"

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
# 检查Docker
# ============================================================================

check_docker() {
    print_header "检查Docker环境"
    
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装"
        print_info "请访问: https://www.docker.com/products/docker-desktop"
        exit 1
    fi
    
    DOCKER_VERSION=$(docker --version)
    print_success "Docker已安装: $DOCKER_VERSION"
    
    # 检查Docker daemon
    if ! docker ps &> /dev/null; then
        print_error "Docker daemon未运行"
        print_info "请启动Docker Desktop或Docker daemon"
        exit 1
    fi
    
    print_success "Docker daemon正在运行"
    echo ""
}

# ============================================================================
# 构建Docker镜像
# ============================================================================

build_docker_image() {
    print_header "构建Docker镜像"
    
    print_info "镜像名称: $IMAGE_NAME:$IMAGE_TAG"
    print_info "这可能需要10-20分钟..."
    echo ""
    
    if docker build -t "$IMAGE_NAME:$IMAGE_TAG" -f "$PROJECT_DIR/Dockerfile" "$PROJECT_DIR"; then
        print_success "Docker镜像构建成功"
        echo ""
        return 0
    else
        print_error "Docker镜像构建失败"
        return 1
    fi
}

# ============================================================================
# 运行Docker容器
# ============================================================================

run_docker_container() {
    print_header "运行Docker容器构建APK"
    
    # 创建输出目录
    mkdir -p "$OUTPUT_DIR"
    
    print_info "输出目录: $OUTPUT_DIR"
    print_info "这可能需要10-20分钟..."
    echo ""
    
    # 运行容器
    if docker run --rm \
        -v "$OUTPUT_DIR:/app/output" \
        --name "$CONTAINER_NAME" \
        "$IMAGE_NAME:$IMAGE_TAG"; then
        
        print_success "Docker容器执行成功"
        echo ""
        
        # 显示输出文件
        if [ -d "$OUTPUT_DIR" ] && [ "$(ls -A $OUTPUT_DIR)" ]; then
            print_success "生成的APK文件:"
            ls -lh "$OUTPUT_DIR"
            echo ""
        fi
        
        return 0
    else
        print_error "Docker容器执行失败"
        return 1
    fi
}

# ============================================================================
# 清理Docker
# ============================================================================

clean_docker() {
    print_header "清理Docker资源"
    
    # 停止运行中的容器
    if docker ps -a | grep -q "$CONTAINER_NAME"; then
        print_info "停止容器: $CONTAINER_NAME"
        docker stop "$CONTAINER_NAME" 2>/dev/null || true
        docker rm "$CONTAINER_NAME" 2>/dev/null || true
    fi
    
    # 删除镜像
    if docker images | grep -q "$IMAGE_NAME"; then
        print_info "删除镜像: $IMAGE_NAME:$IMAGE_TAG"
        docker rmi "$IMAGE_NAME:$IMAGE_TAG" 2>/dev/null || true
    fi
    
    # 删除输出目录
    if [ -d "$OUTPUT_DIR" ]; then
        print_info "删除输出目录: $OUTPUT_DIR"
        rm -rf "$OUTPUT_DIR"
    fi
    
    print_success "清理完成"
    echo ""
}

# ============================================================================
# 显示使用帮助
# ============================================================================

show_help() {
    cat << EOF
使用方法: $0 [选项]

选项:
  build       构建Docker镜像并运行容器构建APK
  run         仅运行Docker容器 (需要先构建镜像)
  clean       清理Docker资源和输出文件
  help        显示此帮助信息

示例:
  $0 build              # 完整构建流程
  $0 run                # 仅运行容器
  $0 clean              # 清理所有资源

输出目录: $OUTPUT_DIR

系统要求:
  - Docker (https://www.docker.com/)
  - 20GB+ 磁盘空间
  - 4GB+ 内存

EOF
}

# ============================================================================
# 主函数
# ============================================================================

main() {
    print_header "$PROJECT_NAME - Docker构建脚本"
    
    # 解析命令行参数
    COMMAND="${1:-build}"
    
    case "$COMMAND" in
        build)
            check_docker
            build_docker_image
            run_docker_container
            ;;
        run)
            check_docker
            run_docker_container
            ;;
        clean)
            clean_docker
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
    
    print_header "完成"
    
    if [ -d "$OUTPUT_DIR" ] && [ "$(ls -A $OUTPUT_DIR)" ]; then
        print_success "APK已生成到: $OUTPUT_DIR"
        echo ""
        print_info "下一步:"
        echo "  1. 在设备上安装Debug APK进行测试"
        echo "  2. 签名Release APK用于发布"
        echo "  3. 上传Bundle到Google Play"
        echo ""
    fi
}

# ============================================================================
# 执行主函数
# ============================================================================

cd "$PROJECT_DIR"
main "$@"
