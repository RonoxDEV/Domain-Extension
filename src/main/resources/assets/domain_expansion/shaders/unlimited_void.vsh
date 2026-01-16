attribute vec3 Position;
attribute vec2 UV;

varying vec2 vUV;

uniform mat4 ModelViewProjectionMat;

void main() {
    vUV = UV;
    gl_Position = ModelViewProjectionMat * vec4(Position, 1.0);
}
