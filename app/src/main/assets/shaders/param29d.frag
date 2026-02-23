#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTexCoord;
uniform samplerExternalOES uTexture;

// 28 个参数（29D 去掉一个保留）
// [0]  iso          ISO 感光度归一化 0-1
// [1]  shutter      快门速度归一化
// [2]  ev           曝光补偿 -1..1（对应 -3..+3 EV）
// [3]  dynamicRange 动态范围 0-1
// [4]  shadowComp   阴影补偿 0-1
// [5]  highlight    高光保护 0-1
// [6]  colorTemp    色温 0-1（冷→暖）
// [7]  tint         色调 -1..1
// [8]  saturation   饱和度 0-1（对应 0-2）
// [9]  skinTone     肤色保护 0-1
// [10] redGain      红通道增益
// [11] greenGain    绿通道增益
// [12] blueGain     蓝通道增益
// [13] colorBoost   色彩增强 0-1
// [14] sharpness    锐化 0-1
// [15] denoise      降噪 0-1
// [16] grain        胶片颗粒 0-1
// [17] vignette     暗角 0-1
// [18] clarity      清晰度 -1..1
// [19] dehaze       去雾 0-1
// [20] beautyGlobal 全局美颜 0-1
// [21] skinSmooth   磨皮 0-1
// [22] faceThin     瘦脸 0-1
// [23] eyeEnlarge   大眼 0-1
// [24] skinWhiten   美白 0-1
// [25] skinRedden   红润 0-1
// [26] chinAdjust   下巴调整 -1..1
// [27] noseBridge   鼻梁调整 -1..1
uniform float uParams[28];

// ─── 工具函数 ─────────────────────────────────────────────────────────────

// 亮度调整（乘法）
vec3 adjustBrightness(vec3 color, float factor) {
    return clamp(color * factor, 0.0, 1.0);
}

// 对比度调整
vec3 adjustContrast(vec3 color, float contrast) {
    return clamp((color - 0.5) * (1.0 + contrast) + 0.5, 0.0, 1.0);
}

// 饱和度调整
vec3 adjustSaturation(vec3 color, float sat) {
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    return clamp(mix(vec3(gray), color, sat * 2.0), 0.0, 1.0);
}

// 色温调整（简单 RGB 偏移）
vec3 adjustColorTemp(vec3 color, float temp) {
    // temp: 0=冷(蓝), 0.5=中性, 1=暖(橙)
    float shift = (temp - 0.5) * 0.4;
    color.r = clamp(color.r + shift, 0.0, 1.0);
    color.b = clamp(color.b - shift, 0.0, 1.0);
    return color;
}

// 色调调整（绿-品红轴）
vec3 adjustTint(vec3 color, float tint) {
    color.g = clamp(color.g + tint * 0.15, 0.0, 1.0);
    return color;
}

// 暗角效果
vec3 applyVignette(vec3 color, vec2 uv, float strength) {
    float dist = distance(uv, vec2(0.5, 0.5));
    float vign = 1.0 - smoothstep(0.4, 0.9, dist * strength * 2.0);
    return color * vign;
}

// 阴影提亮
vec3 liftShadows(vec3 color, float lift) {
    return clamp(color + lift * (1.0 - color) * 0.3, 0.0, 1.0);
}

// 高光压制
vec3 protectHighlights(vec3 color, float protect) {
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    float factor = 1.0 - protect * smoothstep(0.7, 1.0, lum) * 0.3;
    return clamp(color * factor, 0.0, 1.0);
}

// 去雾（增加对比度和饱和度）
vec3 applyDehaze(vec3 color, float strength) {
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    vec3 defogged = mix(color, (color - 0.1) * 1.2, strength * 0.5);
    return clamp(defogged, 0.0, 1.0);
}

// 胶片颗粒（伪随机噪点）
vec3 applyGrain(vec3 color, vec2 uv, float strength) {
    float noise = fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453);
    noise = (noise - 0.5) * strength * 0.15;
    return clamp(color + vec3(noise), 0.0, 1.0);
}

// 美白（提亮高亮区域）
vec3 applyWhiten(vec3 color, float strength) {
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    return clamp(color + strength * 0.2 * smoothstep(0.3, 0.9, lum), 0.0, 1.0);
}

// 红润（增加红色通道）
vec3 applyRedden(vec3 color, float strength) {
    color.r = clamp(color.r + strength * 0.1, 0.0, 1.0);
    return color;
}

// ─── 主函数 ───────────────────────────────────────────────────────────────

void main() {
    vec4 texColor = texture2D(uTexture, vTexCoord);
    vec3 color = texColor.rgb;

    float iso          = uParams[0];
    float shutter      = uParams[1];
    float ev           = uParams[2];
    float dynamicRange = uParams[3];
    float shadowComp   = uParams[4];
    float highlight    = uParams[5];
    float colorTemp    = uParams[6];
    float tint         = uParams[7];
    float saturation   = uParams[8];
    // skinTone [9] 保留（需人脸检测配合）
    float redGain      = uParams[10];
    float greenGain    = uParams[11];
    float blueGain     = uParams[12];
    float colorBoost   = uParams[13];
    float sharpness    = uParams[14];
    // denoise [15] 需多采样，此处跳过
    float grain        = uParams[16];
    float vignette     = uParams[17];
    float clarity      = uParams[18];
    float dehaze       = uParams[19];
    // beautyGlobal [20] 全局美颜权重
    // skinSmooth [21] 磨皮（需多采样）
    // faceThin [22] 瘦脸（需人脸关键点）
    // eyeEnlarge [23] 大眼（需人脸关键点）
    float skinWhiten   = uParams[24];
    float skinRedden   = uParams[25];
    // chinAdjust [26] 下巴（需人脸关键点）
    // noseBridge [27] 鼻梁（需人脸关键点）

    // ── 1. 曝光：ISO + EV ─────────────────────────────────────────────
    float brightnessFactor = (iso * 0.8 + 0.2) * (1.0 + ev * 0.5);
    color = adjustBrightness(color, brightnessFactor);

    // ── 2. 阴影提亮 ───────────────────────────────────────────────────
    color = liftShadows(color, shadowComp);

    // ── 3. 高光保护 ───────────────────────────────────────────────────
    color = protectHighlights(color, highlight);

    // ── 4. 对比度（clarity 复用） ─────────────────────────────────────
    color = adjustContrast(color, clarity);

    // ── 5. 饱和度 ─────────────────────────────────────────────────────
    color = adjustSaturation(color, saturation);

    // ── 6. 色温 ───────────────────────────────────────────────────────
    color = adjustColorTemp(color, colorTemp);

    // ── 7. 色调 ───────────────────────────────────────────────────────
    color = adjustTint(color, tint);

    // ── 8. RGB 通道增益 ───────────────────────────────────────────────
    color.r = clamp(color.r * redGain, 0.0, 1.0);
    color.g = clamp(color.g * greenGain, 0.0, 1.0);
    color.b = clamp(color.b * blueGain, 0.0, 1.0);

    // ── 9. 色彩增强（colorBoost：进一步提升饱和度） ───────────────────
    if (colorBoost > 0.0) {
        color = adjustSaturation(color, saturation + colorBoost * 0.5);
    }

    // ── 10. 去雾 ──────────────────────────────────────────────────────
    if (dehaze > 0.0) {
        color = applyDehaze(color, dehaze);
    }

    // ── 11. 暗角 ──────────────────────────────────────────────────────
    if (vignette > 0.0) {
        color = applyVignette(color, vTexCoord, vignette);
    }

    // ── 12. 胶片颗粒 ──────────────────────────────────────────────────
    if (grain > 0.0) {
        color = applyGrain(color, vTexCoord, grain);
    }

    // ── 13. 美颜：美白 + 红润 ─────────────────────────────────────────
    if (skinWhiten > 0.0) {
        color = applyWhiten(color, skinWhiten);
    }
    if (skinRedden > 0.0) {
        color = applyRedden(color, skinRedden);
    }

    gl_FragColor = vec4(clamp(color, 0.0, 1.0), texColor.a);
}
