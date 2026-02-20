#version 300 es
precision mediump float;

uniform sampler2D uTexture;
in vec2 vTexCoord;
out vec4 fragColor;

// 29D 核心参数采样
uniform float uBrightness;   // 亮度
uniform float uContrast;     // 对比度
uniform float uSaturation;   // 饱和度
uniform float uSharpness;    // 锐化 (预留)
uniform vec3 uWhiteBalance;  // 白平衡增益

void main() {
    vec4 color = texture(uTexture, vTexCoord);
    
    // 1. 白平衡调节
    color.rgb *= uWhiteBalance;
    
    // 2. 亮度与对比度
    color.rgb = (color.rgb - 0.5) * uContrast + 0.5 + uBrightness;
    
    // 3. 饱和度调节 (亮度保持)
    float luminance = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(vec3(luminance), color.rgb, uSaturation);
    
    // 4. 锐化逻辑 (留空，但必须预留)
    
    fragColor = vec4(clamp(color.rgb, 0.0, 1.0), color.a);
}
