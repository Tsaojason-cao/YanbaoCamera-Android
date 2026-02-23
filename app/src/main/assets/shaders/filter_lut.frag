#extension GL_OES_EGL_image_external : require
precision mediump float;

varying vec2 vTexCoord;
uniform samplerExternalOES uTexture;  // 相机预览纹理
uniform sampler2D uLutTexture;        // LUT 查找表（512x512 strip 格式）
uniform float uLutIntensity;          // LUT 混合强度 0-1

// LUT 尺寸（64x64x64 标准 LUT，展开为 512x512）
const float LUT_SIZE = 64.0;
const float LUT_STRIP_SIZE = 512.0;

/**
 * 从 512x512 LUT 纹理中查找颜色
 * LUT 格式：64 个 64x64 的色块横向排列（共 512 宽）
 */
vec3 applyLut(vec3 color) {
    // 将颜色值映射到 LUT 索引
    float blueIdx = color.b * (LUT_SIZE - 1.0);
    float blueFloor = floor(blueIdx);
    float blueFrac = blueIdx - blueFloor;

    // 计算两个相邻蓝色切片的 UV 坐标
    float tileX1 = blueFloor / LUT_SIZE;
    float tileX2 = (blueFloor + 1.0) / LUT_SIZE;

    float u1 = (tileX1 + color.r / LUT_SIZE) * (LUT_SIZE / LUT_STRIP_SIZE);
    float u2 = (tileX2 + color.r / LUT_SIZE) * (LUT_SIZE / LUT_STRIP_SIZE);
    float v = color.g;

    vec3 lut1 = texture2D(uLutTexture, vec2(u1, v)).rgb;
    vec3 lut2 = texture2D(uLutTexture, vec2(u2, v)).rgb;

    return mix(lut1, lut2, blueFrac);
}

void main() {
    vec4 texColor = texture2D(uTexture, vTexCoord);
    vec3 original = texColor.rgb;

    // 应用 LUT
    vec3 lutColor = applyLut(original);

    // 与原始颜色混合（强度控制）
    vec3 result = mix(original, lutColor, uLutIntensity);

    gl_FragColor = vec4(result, texColor.a);
}
