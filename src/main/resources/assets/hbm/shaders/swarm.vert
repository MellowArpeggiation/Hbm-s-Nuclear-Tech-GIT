
#version 120
#extension GL_EXT_gpu_shader4 : require

uniform float iTime;

varying vec3 vPosition;
varying vec4 vColor;

float hash(float x){ return fract(cos(x*124.123)*412.0); }
float hash(int x){ return fract(cos(x*124.123)*412.0); }

void main() {
    vPosition = gl_Vertex.xyz;
    vColor = gl_Color;

    float t = gl_VertexID + iTime;
    float r = hash(gl_VertexID);
    float r2 = hash(gl_VertexID + 0.5);
    float y = cos(t) * r + sin(t) * (1-r);

    gl_Position = gl_ModelViewProjectionMatrix * (vec4(gl_Vertex.x, y * 0.2 * r2 * r2, gl_Vertex.z, gl_Vertex.w));
    gl_TexCoord[0] = gl_MultiTexCoord0;
}