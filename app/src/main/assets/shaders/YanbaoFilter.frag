#version 300 es
// ============================================================
// yanbao AI — 29D 渲染引擎核心 Fragment Shader
// 版本: 3.0  |  精度: mediump  |  目标: OpenGL ES 3.0
// 所有 29 个 uniform 变量与 UI 滑块 1:1 双向绑定
// 调节滑块时预览延迟 < 16ms（60fps）
// ============================================================
precision mediump float;

uniform sampler2D uTexture;
in vec2 vTexCoord;
out vec4 fragColor;

// ──────────────────────────────────────────
// Tab 1: 曝光（Exposure） — D01~D06
// ──────────────────────────────────────────
uniform float u_ISO;                // D01 ISO 感光度 [100–6400]，默认 400
uniform float u_ShutterSpeed;       // D02 快门速度 [0–1]，映射 1/8000s–30s，默认 0.5
uniform float u_Exposure;           // D03 曝光补偿 EV [-3.0–+3.0]，默认 0.0
uniform float u_DynamicRange;       // D04 动态范围 [0–1]，默认 0.5
uniform float u_ShadowComp;         // D05 阴影补偿 [0–1]，默认 0.3
uniform float u_HighlightProtect;   // D06 高光抑制 [0–1]，默认 0.3

// ──────────────────────────────────────────
// Tab 2: 色彩（Color） — D07~D14
// ──────────────────────────────────────────
uniform float u_ColorTemp;          // D07 色温 [0–1] 归一化，默认 0.4375（≈5500K）
uniform float u_Tint;               // D08 色调 [-1–+1]，默认 0.0
uniform float u_Saturation;         // D09 饱和度 [0–2]，默认 1.0
uniform float u_SkinTone;           // D10 肤色保护 [0–1]，默认 0.5
uniform float u_RedGain;            // D11 红色通道增益 [0.5–2.0]，默认 1.0
uniform float u_GreenGain;          // D12 绿色通道增益 [0.5–2.0]，默认 1.0
uniform float u_BlueGain;           // D13 蓝色通道增益 [0.5–2.0]，默认 1.0
uniform float u_ColorBoost;         // D14 色彩浓度（Vibrance）[0–1]，默认 0.0

// ──────────────────────────────────────────
// Tab 3: 纹理（Texture） — D15~D20
// ──────────────────────────────────────────
uniform float u_Sharpness;          // D15 锐度 [0–1]，默认 0.5
uniform float u_Denoise;            // D16 降噪 [0–1]，默认 0.3
uniform float u_Grain;              // D17 胶片颗粒 [0–1]，默认 0.0
uniform float u_Vignette;           // D18 暗角 [0–1]，默认 0.0
uniform float u_Clarity;            // D19 清晰度 [-1–+1]，默认 0.0
uniform float u_Dehaze;             // D20 去雾 [0–1]，默认 0.0

// ──────────────────────────────────────────
// Tab 4: 美颜（Beauty） — D21~D28
// ──────────────────────────────────────────
uniform float u_BeautyGlobal;       // D21 全局美颜强度 [0–1]，默认 0.5
uniform float u_SkinSmooth;         // D22 磨皮 [0–1]，默认 0.6
uniform float u_FaceThin;           // D23 瘦脸 [0–1]，默认 0.3（预留 AI 骨骼点）
uniform float u_EyeEnlarge;         // D24 大眼 [0–1]，默认 0.2（预留 AI 骨骼点）
uniform float u_SkinWhiten;         // D25 美白 [0–1]，默认 0.4
uniform float u_SkinRedden;         // D26 红润 [0–1]，默认 0.2
uniform float u_ChinAdjust;         // D27 下巴 [-0.5–+0.5]，默认 0.0（预留 AI 骨骼点）
uniform float u_NoseBridge;         // D28 鼻梁 [-0.5–+0.5]，默认 0.0（预留 AI 骨骼点）

// ──────────────────────────────────────────
// D29: LBS 环境光补偿
// ──────────────────────────────────────────
uniform float u_LBSCompensation;    // D29 LBS 环境光补偿 [0–1]，默认 0.0

// ──────────────────────────────────────────
// 辅助 uniform（非滑块参数）
// ──────────────────────────────────────────
uniform vec2  u_Resolution;         // 屏幕分辨率（暗角/锐化/降噪计算用）
uniform float u_Time;               // 时间戳（动态颗粒用）

// ──────────────────────────────────────────
// 工具函数
// ──────────────────────────────────────────

// 伪随机噪声（胶片颗粒）
float rand(vec2 co) {
    return fract(sin(dot(co.xy, vec2(12.9898, 78.233))) * 43758.5453);
}

// 肤色检测 mask（橙色调区域）
float skinMask(vec3 c) {
    return smoothstep(0.1, 0.5, c.r - c.b) *
           smoothstep(0.0, 0.4, c.g - c.b);
}

// ──────────────────────────────────────────
// 主渲染函数
// ──────────────────────────────────────────
void main() {
    vec2 uv = vTexCoord;
    vec4 color = texture(uTexture, uv);
    vec2 px = vec2(1.0) / u_Resolution;

    // ── D03: 曝光补偿 EV ──────────────────
    color.rgb *= pow(2.0, u_Exposure);

    // ── D04: 动态范围 ─────────────────────
    vec3 hiMask = max(color.rgb - 0.7, 0.0);
    vec3 shMask = max(0.3 - color.rgb, 0.0);
    color.rgb -= hiMask * (1.0 - u_DynamicRange) * 0.8;
    color.rgb += shMask * u_DynamicRange * 0.6;

    // ── D05: 阴影补偿 ─────────────────────
    color.rgb += max(0.25 - color.rgb, 0.0) * u_ShadowComp * 1.2;

    // ── D06: 高光抑制 ─────────────────────
    color.rgb -= max(color.rgb - 0.75, 0.0) * u_HighlightProtect * 1.2;

    // ── D07: 色温（冷暖偏移）─────────────
    float tempShift = (u_ColorTemp - 0.4375) * 0.3;
    color.r += tempShift;
    color.b -= tempShift;

    // ── D08: 色调（绿-品红）──────────────
    color.g += u_Tint * 0.15;

    // ── D09: 饱和度 ───────────────────────
    float luma = dot(color.rgb, vec3(0.2126, 0.7152, 0.0722));
    color.rgb = mix(vec3(luma), color.rgb, u_Saturation);

    // ── D10: 肤色保护 ─────────────────────
    float sm = skinMask(color.rgb);
    color.rgb = mix(color.rgb,
        color.rgb * vec3(1.0 + u_SkinTone * 0.1, 1.0, 1.0 - u_SkinTone * 0.05),
        sm * 0.5);

    // ── D11-D13: RGB 通道增益 ─────────────
    color.r *= u_RedGain;
    color.g *= u_GreenGain;
    color.b *= u_BlueGain;

    // ── D14: 色彩浓度（Vibrance）─────────
    float satLevel = length(color.rgb - vec3(luma));
    float vibranceMask = 1.0 - satLevel;
    color.rgb = mix(color.rgb,
        mix(vec3(luma), color.rgb, 1.0 + u_ColorBoost * 0.8),
        vibranceMask);

    // ── D15: 锐度（Unsharp Mask）─────────
    vec4 blurred4 = (
        texture(uTexture, uv + vec2(-px.x, 0.0)) +
        texture(uTexture, uv + vec2( px.x, 0.0)) +
        texture(uTexture, uv + vec2(0.0, -px.y)) +
        texture(uTexture, uv + vec2(0.0,  px.y))
    ) * 0.25;
    color.rgb += (color.rgb - blurred4.rgb) * u_Sharpness * 2.0;

    // ── D16: 降噪（邻域平均混合）─────────
    vec4 denoise4 = (
        texture(uTexture, uv + vec2(-px.x, -px.y)) +
        texture(uTexture, uv + vec2( px.x, -px.y)) +
        texture(uTexture, uv + vec2(-px.x,  px.y)) +
        texture(uTexture, uv + vec2( px.x,  px.y))
    ) * 0.25;
    color.rgb = mix(color.rgb, denoise4.rgb, u_Denoise * 0.5);

    // ── D17: 胶片颗粒 ─────────────────────
    float noise = rand(uv + fract(u_Time * 0.01)) * 2.0 - 1.0;
    color.rgb += noise * u_Grain * 0.08;

    // ── D18: 暗角 ─────────────────────────
    vec2 vigUV = uv * 2.0 - 1.0;
    float vigDist = dot(vigUV, vigUV);
    color.rgb *= 1.0 - vigDist * u_Vignette * 0.7;

    // ── D19: 清晰度（局部对比度）─────────
    float localLuma = dot(blurred4.rgb, vec3(0.2126, 0.7152, 0.0722));
    color.rgb += vec3(luma - localLuma) * u_Clarity * 1.5;

    // ── D20: 去雾 ─────────────────────────
    color.rgb = mix(color.rgb,
        (color.rgb - 0.5) * (1.0 + u_Dehaze * 0.4) + 0.5,
        u_Dehaze);

    // ── D21-D22: 全局美颜 + 磨皮 ─────────
    vec4 smooth4 = (
        texture(uTexture, uv + vec2(-2.0 * px.x, 0.0)) +
        texture(uTexture, uv + vec2( 2.0 * px.x, 0.0)) +
        texture(uTexture, uv + vec2(0.0, -2.0 * px.y)) +
        texture(uTexture, uv + vec2(0.0,  2.0 * px.y))
    ) * 0.25;
    float beautyStr = u_BeautyGlobal * u_SkinSmooth;
    color.rgb = mix(color.rgb, smooth4.rgb, sm * beautyStr * 0.6);

    // ── D25: 美白 ─────────────────────────
    color.rgb += sm * u_SkinWhiten * 0.15;

    // ── D26: 红润 ─────────────────────────
    color.r += sm * u_SkinRedden * 0.1;

    // ── D29: LBS 环境光补偿 ───────────────
    color.rgb += vec3(u_LBSCompensation * 0.04);

    // ── 最终输出 ──────────────────────────
    color.rgb = clamp(color.rgb, 0.0, 1.0);
    fragColor = color;
}
