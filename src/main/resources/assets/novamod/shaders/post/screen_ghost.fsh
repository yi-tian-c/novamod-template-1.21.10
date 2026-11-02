#version 150

uniform sampler2D Scene;
uniform sampler2D SceneDepth;
layout(std140) uniform MemoryData {
    mat4 InverseWorld;
    vec4 ScreenDirection;
    vec4 Progress;
    vec4 CameraFocus;
    vec4 FocusTail;
};

#define ScreenSize ScreenDirection.xy
#define Direction ScreenDirection.zw
#define GhostProgress Progress.x
#define Obscuration Progress.y
#define EffectTime Progress.z
#define TerrainMapping int(Progress.w)
#define CameraOffset CameraFocus.xyz
#define Focus vec2(CameraFocus.w, FocusTail.x)

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 point) {
    return fract(sin(dot(point, vec2(127.1, 311.7))) * 43758.5453);
}

float noise(vec2 point) {
    vec2 cell = floor(point);
    vec2 fraction = fract(point);
    fraction = fraction * fraction * (3.0 - 2.0 * fraction);
    return mix(mix(hash(cell), hash(cell + vec2(1.0, 0.0)), fraction.x),
               mix(hash(cell + vec2(0.0, 1.0)), hash(cell + vec2(1.0, 1.0)), fraction.x), fraction.y);
}

float worldNoise(vec3 point) {
    // 三个世界平面的连续噪声共同控制表面花纹，避免只沿屏幕坐标扩散。
    return noise(point.xz) * 0.5
         + noise(point.xy + vec2(13.7, 7.1)) * 0.25
         + noise(point.yz + vec2(5.3, 19.9)) * 0.25;
}

float terrainMask(vec2 uv) {
    float depth = texture(SceneDepth, uv).r;
    if (depth >= 0.999999) {
        return smoothstep(0.12, 0.94, Obscuration);
    }
    // 深度和逆矩阵将屏幕像素还原为世界表面位置，黑化花纹固定在地面及物体上。
    vec4 relative = InverseWorld * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec3 position = relative.xyz / relative.w + CameraOffset;
    float grain = worldNoise(position * 0.75) * 2.2
                + worldNoise(position * 2.1) * 0.65;
    float radius = 28.0 * pow(1.0 - Obscuration, 2.5) - Obscuration * 1.8;
    float mask = smoothstep(radius - 0.85, radius + 0.85, length(position) + grain - 1.4);
    return mask * smoothstep(0.0, 0.08, Obscuration);
}

void main() {
    // 完全闭合后不再采样场景，保证任何亮度、夜视和光影下都输出纯黑。
    if (Obscuration >= 1.0) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }
    // 逐渐向黑方块焦点拉近，并施加轻微扭转，形成被卷入而非骤然切黑的运动。
    float pull = smoothstep(0.05, 0.95, Obscuration);
    vec2 delta = texCoord - Focus;
    float angle = pull * pull * 0.065;
    mat2 twist = mat2(cos(angle), -sin(angle), sin(angle), cos(angle));
    vec2 sampleUv = clamp(Focus + twist * delta * (1.0 - pull * 0.13), 0.0, 1.0);
    vec4 accumulated = texture(Scene, sampleUv);
    float totalWeight = 1.0;

    for (int index = 1; index <= 4; index++) {
        float layerProgress = clamp(GhostProgress - float(index - 1), 0.0, 1.0);
        float weight = layerProgress * (0.38 / float(index));
        vec2 pixelOffset = (Direction * ScreenSize.y * 0.003
                         + delta * ScreenSize * pull * 0.022) * float(index) * layerProgress;
        vec2 uvOffset = pixelOffset / ScreenSize;
        accumulated += texture(Scene, clamp(sampleUv - uvOffset, 0.0, 1.0)) * weight;
        totalWeight += weight;
    }

    vec3 scene = (accumulated / totalWeight).rgb;
    float luminance = dot(scene, vec3(0.2126, 0.7152, 0.0722));
    scene = mix(scene, vec3(luminance), pull * 0.85);
    if (TerrainMapping != 0) {
        fragColor = vec4(scene * (1.0 - terrainMask(sampleUv)), 1.0);
        return;
    }
    // 以屏幕高度为统一尺度，视野从四周向中心闭合，噪声使边缘呈翻卷烟雾。
    vec2 centered = (texCoord - 0.5) * vec2(ScreenSize.x / ScreenSize.y, 1.0);
    float aspect = ScreenSize.x / ScreenSize.y;
    float cornerRadius = length(vec2(aspect * 0.5, 0.5));
    float opening = 1.0 - Obscuration;
    float radius = (cornerRadius + 0.2) * opening;
    float billow = noise(centered * 10.0 + vec2(EffectTime * 0.33, -EffectTime * 0.27)) * 0.09;
    billow += noise(centered * 23.0 - EffectTime * 0.42) * 0.035;
    billow *= min(1.0, opening * 5.0);
    float feather = max(0.0015, 0.07 * opening);
    float mask = smoothstep(radius - feather, radius + feather, length(centered) + billow);
    mask *= smoothstep(0.0, 0.06, Obscuration);
    fragColor = vec4(scene * (1.0 - mask), 1.0);
}
