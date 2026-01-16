#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;
in vec4 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec4 vertexColor;
out vec4 normal;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // C'EST LA LIGNE CRUCIALE : On passe les coordonnées UV au fragment shader
    texCoord = UV0;
    vertexColor = Color;
    normal = Normal;
}
