#version 150
#moj_import <fog.glsl>
uniform sampler2D Sampler0;
uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float BlightBlend;
in float vertexDistance;
in vec2 texCoord0;
in vec4 vertexColor;
out vec4 fragColor;
void main() {
    vec4 sampleColor = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (sampleColor.a < 0.1) discard;
    float luminance = dot(sampleColor.rgb, vec3(0.299, 0.587, 0.114));
    vec3 stain = vertexColor.r > vertexColor.g * 1.45 ? vec3(1.6, 0.45, 0.42) : vec3(1.0);
    sampleColor.rgb = mix(sampleColor.rgb, vec3(luminance) * stain, BlightBlend);
    fragColor = linear_fog(sampleColor, vertexDistance, FogStart, FogEnd, FogColor);
}
