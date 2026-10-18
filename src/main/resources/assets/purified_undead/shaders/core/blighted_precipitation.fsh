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
    // Anchored in repeating texture space: follows the same snowflake as UVs scroll.
    // Smooth mask avoids hard per-frame switches and never colors an entire weather quad.
    vec2 uv = fract(texCoord0);
    float patch = (1.0 - smoothstep(0.045, 0.13, distance(uv, vec2(0.28, 0.34))))
                + (1.0 - smoothstep(0.035, 0.10, distance(uv, vec2(0.73, 0.78))));
    vec3 stain = mix(vec3(1.0), vec3(1.6, 0.45, 0.42), clamp(patch, 0.0, 1.0));
    sampleColor.rgb = mix(sampleColor.rgb, vec3(luminance) * stain, BlightBlend);
    fragColor = linear_fog(sampleColor, vertexDistance, FogStart, FogEnd, FogColor);
}
