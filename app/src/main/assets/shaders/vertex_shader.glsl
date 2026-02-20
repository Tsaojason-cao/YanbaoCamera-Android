#version 300 es

in vec4 aPosition;
in vec2 aTexCoord;

out vec2 vTexCoord;

uniform mat4 uMVPMatrix;
uniform mat4 uTexMatrix;

void main() {
    gl_Position = uMVPMatrix * aPosition;
    vTexCoord = (uTexMatrix * vec4(aTexCoord, 0.0, 1.0)).xy;
}
