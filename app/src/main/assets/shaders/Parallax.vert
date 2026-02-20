#version 300 es
layout(location = 0) in vec4 aPosition;
layout(location = 1) in vec2 aTexCoord;

uniform float uOffsetX; // 陀螺仪 X 轴补偿
uniform float uOffsetY; // 陀螺仪 Y 轴补偿
out vec2 vTexCoord;

void main() {
    gl_Position = aPosition;
    // 对纹理坐标微移，实现视差补偿
    vTexCoord = (aTexCoord * 0.95) + vec2(uOffsetX * 0.05, uOffsetY * 0.05);
}
