# 使用官方Ubuntu基础镜像
FROM ubuntu:22.04

# 设置环境变量
ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV ANDROID_SDK_ROOT=/opt/android-sdk
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools:$ANDROID_SDK_ROOT/tools/bin

# 安装系统依赖
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    gradle \
    git \
    wget \
    unzip \
    curl \
    build-essential \
    libssl-dev \
    && rm -rf /var/lib/apt/lists/*

# 创建Android SDK目录
RUN mkdir -p /opt/android-sdk

# 下载Android SDK命令行工具
RUN cd /opt/android-sdk && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip && \
    unzip -q commandlinetools-linux-10406996_latest.zip && \
    rm commandlinetools-linux-10406996_latest.zip && \
    mkdir -p cmdline-tools/latest && \
    mv cmdline-tools/* cmdline-tools/latest/ 2>/dev/null || true

# 接受Android许可证
RUN yes | sdkmanager --licenses 2>/dev/null || true

# 安装Android SDK组件
RUN sdkmanager --install \
    "platforms;android-34" \
    "build-tools;34.0.0" \
    "ndk;25.1.8937393" \
    "system-images;android-34;google_apis;x86_64" \
    "emulator" \
    2>&1 | grep -v "^$" || true

# 复制项目代码
COPY . /app
WORKDIR /app

# 创建local.properties
RUN echo "sdk.dir=/opt/android-sdk" > local.properties

# 使gradlew可执行
RUN chmod +x gradlew

# 构建Debug APK
RUN ./gradlew assembleDebug --no-daemon --stacktrace 2>&1 | tail -50

# 构建Release APK
RUN ./gradlew assembleRelease --no-daemon --stacktrace 2>&1 | tail -50

# 构建Release Bundle
RUN ./gradlew bundleRelease --no-daemon --stacktrace 2>&1 | tail -50

# 创建输出目录
RUN mkdir -p /app/output

# 复制所有输出文件
RUN cp app/build/outputs/apk/debug/app-debug.apk /app/output/ 2>/dev/null || true && \
    cp app/build/outputs/apk/release/app-release-unsigned.apk /app/output/ 2>/dev/null || true && \
    cp app/build/outputs/bundle/release/app-release.aab /app/output/ 2>/dev/null || true

# 设置输出目录为卷
VOLUME ["/app/output"]

# 设置默认命令
CMD ["bash", "-c", "echo '=== APK构建完成 ===' && ls -lh /app/output/ && echo '' && echo '生成的文件:' && find /app/output -type f -exec ls -lh {} \\;"]
