#version 300 es
precision mediump float;

uniform sampler2D uTexture;     // 原始图像纹理
uniform sampler2D uCurveLut;    // 曲线映射纹理 (256x1)

in vec2 vTexCoord;
out vec4 fragColor;

void main() {
    // 采样原始图像
    vec4 base = texture(uTexture, vTexCoord);
    
    // 使用原始 RGB 值作为 LUT 纹理的坐标进行映射
    // x 坐标是原始颜色值 (0.0-1.0)
    // y 坐标用于区分 R/G/B 通道：
    //   R: y=0.125, G: y=0.375, B: y=0.625, 全通道: y=0.875
    
    float r = texture(uCurveLut, vec2(base.r, 0.125)).r;
    float g = texture(uCurveLut, vec2(base.g, 0.375)).g;
    float b = texture(uCurveLut, vec2(base.b, 0.625)).b;
    
    // 输出映射后的颜色，保持 Alpha 通道
    fragColor = vec4(r, g, b, base.a);
}
