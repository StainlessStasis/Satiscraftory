/**
Credit - Scannable by Sangar: https://modrinth.com/mod/scannable | https://www.curseforge.com/minecraft/mc-mods/scannable
Code adapted from: https://github.com/MightyPirates/Scannable/blob/1.21.1/common/src/main/resources/assets/scannable/shaders/core/scan_effect.fsh
*/

#version 330

layout(std140) uniform ScanEffectUniforms {
    mat4 invViewProjMat;
    vec4 pos;
    vec4 center;
    float radius;
};

uniform sampler2D depthTex;

in vec2 texCoord0;

out vec4 fragColor;

const float width = 10;
const float sharpness = 10;
const vec4 outerColor = vec4(0.8, 1.0, 0.9, 1.0);
const vec4 midColor = vec4(0.4, 0.5, 0.7, 1.0);
const vec4 innerColor = vec4(0.1, 0.4, 0.9, 1.0);
const vec4 scanlineColor = vec4(0.6, 1.0, 0.2, 1.0);

float scanlines() {
    return sin(gl_FragCoord.y)*0.5+0.5;
}

vec3 worldpos(float depth) {
    vec4 clipSpacePosition = vec4(texCoord0 * 2.0 - 1.0, depth, 1.0);
    vec4 viewSpacePosition = invViewProjMat * clipSpacePosition;
    viewSpacePosition /= viewSpacePosition.w;

    return pos.xyz + viewSpacePosition.xyz;
}

void main() {
    vec4 color = vec4(0, 0, 0, 0);

    float depth = texture(depthTex, texCoord0).r;
    vec3 fragWorldPos = worldpos(depth);
    float dist = distance(fragWorldPos, center.xyz);

    if (dist < radius && dist > radius - width && depth < 1) {
        float diff = 1.0 - (radius - dist)/width;
        vec4 edge = mix(midColor, outerColor, pow(diff, sharpness));
        color = mix(innerColor, edge, diff) + scanlines()*scanlineColor;
        color *= diff;
    }

    fragColor = color;
}
