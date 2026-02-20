#version 300 es
precision mediump float;

in vec2 vTexCoord;
out vec4 fragColor;

uniform sampler2D uTexture;

// ============ 29维参数 Uniforms ============
// 基础调整（6维）
uniform float uBrightness;    // 亮度 [-1.0, 1.0]
uniform float uContrast;      // 对比度 [-1.0, 1.0]
uniform float uSaturation;    // 饱和度 [-1.0, 1.0]
uniform float uTemperature;   // 色温 [-1.0, 1.0]
uniform float uTint;          // 色调 [-1.0, 1.0]
uniform float uVibrance;      // 自然饱和度 [-1.0, 1.0]

// 色调分离（4维）
uniform float uHighlights;    // 高光 [-1.0, 1.0]
uniform float uShadows;       // 阴影 [-1.0, 1.0]
uniform float uWhites;        // 白色 [-1.0, 1.0]
uniform float uBlacks;        // 黑色 [-1.0, 1.0]

// 细节（3维）
uniform float uClarity;       // 清晰度 [-1.0, 1.0]
uniform float uSharpen;       // 锐化 [0.0, 1.0]
uniform float uGrain;         // 颗粒 [0.0, 1.0]

// HSL - 色相（8维：红/橙/黄/绿/青/蓝/紫/洋红）
uniform float uHueRed;
uniform float uHueOrange;
uniform float uHueYellow;
uniform float uHueGreen;
uniform float uHueAqua;
uniform float uHueBlue;
uniform float uHuePurple;
uniform float uHueMagenta;

// 效果（4维）
uniform float uVignette;      // 暗角 [0.0, 1.0]
uniform float uFade;          // 褪色 [0.0, 1.0]
uniform float uBlur;          // 模糊 [0.0, 1.0]
uniform float uAiEnhance;     // AI增强 [0.0, 1.0]

// 颜色科学（4维）
uniform float uLumRed;        // 红色亮度
uniform float uLumOrange;     // 橙色亮度
uniform float uSatRed;        // 红色饱和度
uniform float uSatGreen;      // 绿色饱和度

// ============ 工具函数 ============

// RGB 转 HSL
vec3 rgb2hsl(vec3 color) {
    float maxC = max(color.r, max(color.g, color.b));
    float minC = min(color.r, min(color.g, color.b));
    float delta = maxC - minC;
    float l = (maxC + minC) / 2.0;
    float h = 0.0;
    float s = 0.0;

    if (delta > 0.001) {
        s = delta / (1.0 - abs(2.0 * l - 1.0));
        if (maxC == color.r) {
            h = mod((color.g - color.b) / delta, 6.0) / 6.0;
        } else if (maxC == color.g) {
            h = ((color.b - color.r) / delta + 2.0) / 6.0;
        } else {
            h = ((color.r - color.g) / delta + 4.0) / 6.0;
        }
    }
    return vec3(h, s, l);
}

// HSL 转 RGB
float hue2rgb(float p, float q, float t) {
    if (t < 0.0) t += 1.0;
    if (t > 1.0) t -= 1.0;
    if (t < 1.0/6.0) return p + (q - p) * 6.0 * t;
    if (t < 1.0/2.0) return q;
    if (t < 2.0/3.0) return p + (q - p) * (2.0/3.0 - t) * 6.0;
    return p;
}

vec3 hsl2rgb(vec3 hsl) {
    float h = hsl.x, s = hsl.y, l = hsl.z;
    if (s < 0.001) return vec3(l);
    float q = l < 0.5 ? l * (1.0 + s) : l + s - l * s;
    float p = 2.0 * l - q;
    return vec3(
        hue2rgb(p, q, h + 1.0/3.0),
        hue2rgb(p, q, h),
        hue2rgb(p, q, h - 1.0/3.0)
    );
}

// 亮度调整（使用 Gamma 曲线）
vec3 applyBrightness(vec3 color, float brightness) {
    return clamp(color + brightness, 0.0, 1.0);
}

// 对比度调整
vec3 applyContrast(vec3 color, float contrast) {
    float scale = 1.0 + contrast;
    return clamp((color - 0.5) * scale + 0.5, 0.0, 1.0);
}

// 饱和度调整
vec3 applySaturation(vec3 color, float saturation) {
    float gray = dot(color, vec3(0.299, 0.587, 0.114));
    return clamp(mix(vec3(gray), color, 1.0 + saturation), 0.0, 1.0);
}

// 色温调整（暖/冷）
vec3 applyTemperature(vec3 color, float temperature) {
    color.r = clamp(color.r + temperature * 0.15, 0.0, 1.0);
    color.b = clamp(color.b - temperature * 0.15, 0.0, 1.0);
    return color;
}

// 色调调整（绿/洋红）
vec3 applyTint(vec3 color, float tint) {
    color.g = clamp(color.g + tint * 0.1, 0.0, 1.0);
    return color;
}

// 高光/阴影调整
vec3 applyHighlightsShadows(vec3 color, float highlights, float shadows) {
    float lum = dot(color, vec3(0.299, 0.587, 0.114));
    // 高光：只影响亮部
    float highlightMask = smoothstep(0.5, 1.0, lum);
    // 阴影：只影响暗部
    float shadowMask = 1.0 - smoothstep(0.0, 0.5, lum);
    return clamp(color + highlights * 0.3 * highlightMask + shadows * 0.3 * shadowMask, 0.0, 1.0);
}

// 暗角效果
float applyVignette(vec2 uv, float strength) {
    vec2 center = uv - 0.5;
    float dist = length(center) * 1.414;
    return 1.0 - strength * smoothstep(0.3, 1.0, dist);
}

// 颗粒噪声
float noise(vec2 uv, float seed) {
    return fract(sin(dot(uv + seed, vec2(12.9898, 78.233))) * 43758.5453);
}

// 褪色效果（提升黑色）
vec3 applyFade(vec3 color, float fade) {
    return mix(color, vec3(0.15), fade * 0.4);
}

// 自然饱和度（只增强低饱和区域）
vec3 applyVibrance(vec3 color, float vibrance) {
    float maxC = max(color.r, max(color.g, color.b));
    float minC = min(color.r, min(color.g, color.b));
    float sat = maxC - minC;
    float avg = (color.r + color.g + color.b) / 3.0;
    float mask = 1.0 - sat;  // 低饱和区域权重高
    return clamp(mix(vec3(avg), color, 1.0 + vibrance * mask), 0.0, 1.0);
}

// ============ 主函数 ============
void main() {
    vec2 uv = vTexCoord;
    vec4 texColor = texture(uTexture, uv);
    vec3 color = texColor.rgb;

    // 1. 亮度
    color = applyBrightness(color, uBrightness * 0.5);

    // 2. 对比度
    color = applyContrast(color, uContrast);

    // 3. 饱和度
    color = applySaturation(color, uSaturation);

    // 4. 色温
    color = applyTemperature(color, uTemperature);

    // 5. 色调
    color = applyTint(color, uTint);

    // 6. 高光/阴影
    color = applyHighlightsShadows(color, uHighlights, uShadows);

    // 7. 白色/黑色点
    color = clamp(color + uWhites * 0.1, 0.0, 1.0);
    color = clamp(color + uBlacks * 0.1, 0.0, 1.0);

    // 8. 自然饱和度
    color = applyVibrance(color, uVibrance);

    // 9. HSL 色相调整（8色）
    vec3 hsl = rgb2hsl(color);
    float hue = hsl.x;
    float hueShift = 0.0;
    // 红色区域 (0.0 - 0.05, 0.95 - 1.0)
    hueShift += uHueRed * smoothstep(0.0, 0.05, hue) * (1.0 - smoothstep(0.05, 0.1, hue));
    // 橙色区域 (0.05 - 0.1)
    hueShift += uHueOrange * smoothstep(0.05, 0.1, hue) * (1.0 - smoothstep(0.1, 0.15, hue));
    // 黄色区域 (0.1 - 0.2)
    hueShift += uHueYellow * smoothstep(0.1, 0.15, hue) * (1.0 - smoothstep(0.15, 0.2, hue));
    // 绿色区域 (0.2 - 0.4)
    hueShift += uHueGreen * smoothstep(0.2, 0.3, hue) * (1.0 - smoothstep(0.3, 0.4, hue));
    // 青色区域 (0.4 - 0.55)
    hueShift += uHueAqua * smoothstep(0.4, 0.47, hue) * (1.0 - smoothstep(0.47, 0.55, hue));
    // 蓝色区域 (0.55 - 0.7)
    hueShift += uHueBlue * smoothstep(0.55, 0.62, hue) * (1.0 - smoothstep(0.62, 0.7, hue));
    // 紫色区域 (0.7 - 0.85)
    hueShift += uHuePurple * smoothstep(0.7, 0.77, hue) * (1.0 - smoothstep(0.77, 0.85, hue));
    // 洋红区域 (0.85 - 1.0)
    hueShift += uHueMagenta * smoothstep(0.85, 0.92, hue) * (1.0 - smoothstep(0.92, 1.0, hue));

    hsl.x = fract(hsl.x + hueShift * 0.1);
    color = hsl2rgb(hsl);

    // 10. 颜色亮度调整（红/橙）
    vec3 hslNew = rgb2hsl(color);
    float lumAdjust = uLumRed * smoothstep(0.9, 1.0, hslNew.x) + uLumOrange * smoothstep(0.05, 0.15, hslNew.x);
    hslNew.z = clamp(hslNew.z + lumAdjust * 0.1, 0.0, 1.0);
    color = hsl2rgb(hslNew);

    // 11. 颜色饱和度调整（红/绿）
    vec3 hslSat = rgb2hsl(color);
    float satAdjust = uSatRed * smoothstep(0.9, 1.0, hslSat.x) + uSatGreen * smoothstep(0.2, 0.4, hslSat.x);
    hslSat.y = clamp(hslSat.y + satAdjust * 0.2, 0.0, 1.0);
    color = hsl2rgb(hslSat);

    // 12. 清晰度（局部对比度增强）
    if (abs(uClarity) > 0.01) {
        float lum = dot(color, vec3(0.299, 0.587, 0.114));
        color = clamp(color + uClarity * 0.15 * (color - vec3(lum)), 0.0, 1.0);
    }

    // 13. 褪色
    color = applyFade(color, uFade);

    // 14. 暗角
    float vignetteFactor = applyVignette(uv, uVignette);
    color *= vignetteFactor;

    // 15. 颗粒噪声
    if (uGrain > 0.01) {
        float grainValue = noise(uv, 0.5) * 2.0 - 1.0;
        color = clamp(color + grainValue * uGrain * 0.08, 0.0, 1.0);
    }

    // 16. AI增强（提升整体对比度和色彩）
    if (uAiEnhance > 0.01) {
        color = applyContrast(color, uAiEnhance * 0.3);
        color = applyVibrance(color, uAiEnhance * 0.5);
    }

    fragColor = vec4(color, texColor.a);
}
