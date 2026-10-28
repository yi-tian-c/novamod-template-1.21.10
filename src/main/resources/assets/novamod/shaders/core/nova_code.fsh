#version 150

/*
 * NovaBlock - Code Sky projection
 * Port of Craftmine composite.fsh Code Rain for block faces.
 * Uses screen-space ray so the block acts as a window into the code sky.
 */

uniform sampler2D Sampler0; // code.png font atlas
uniform mat4 InvProjMat;
uniform mat4 InvModelViewMat;
uniform vec2 ScreenSize;
uniform float Time; // worldTime + frame fraction style

out vec4 fragColor;

float random(float v) {
    return fract(sin(v) * 43758.5453123);
}

vec3 getSkyRay() {
    // Window -> NDC
    vec2 ndc = (gl_FragCoord.xy / ScreenSize) * 2.0 - 1.0;

    // Far plane point in view space
    vec4 viewPos = InvProjMat * vec4(ndc, 1.0, 1.0);
    viewPos.xyz /= viewPos.w;

    // View space direction -> world/player aligned
    vec3 ray = mat3(InvModelViewMat) * viewPos.xyz;
    return normalize(ray);
}

bool getCodeSkyUV(vec3 ray, out vec2 hitUV) {
    vec2 horizontal = abs(ray.xz);
    float horizontalMax = max(horizontal.x, horizontal.y);

    if (horizontalMax < 0.0001) {
        return false;
    }

    float t = 100.0 / horizontalMax;
    vec3 hit = ray * t;

    if (hit.y < -400.0 || hit.y > 400.0) {
        return false;
    }

    float localX;
    if (abs(ray.z) >= abs(ray.x)) {
        if (ray.z < 0.0) {
            localX = hit.x;          // 0°
        } else {
            localX = -hit.x;         // 180°
        }
    } else {
        if (ray.x < 0.0) {
            localX = -hit.z;         // +90°
        } else {
            localX = hit.z;          // -90°
        }
    }

    float u = (localX + 100.0) / 200.0;
    float v = (hit.y + 400.0) / 800.0;
    hitUV = vec2(u, v);
    return true;
}

vec4 getCodeRain(vec2 uv) {
    const float columns = 80.0;
    const float rows = 240.0;

    float column = floor(uv.x * columns);
    float row = floor(uv.y * rows);

    float runners = 1.0 + 3.0 * random(column);

    // Time matching original: (worldTime + frameTimeCounter * 20) / 24000
    float gameTime = Time;

    float runner_phase = random(3.6 * column + 0.4231) * 2.0 + gameTime * 300.0;

    float cells_per_runner = floor(rows / runners);
    float runner = floor(row / cells_per_runner);
    float runner_start = cells_per_runner * runner - runner_phase;
    float cell_to_runner_start = row - runner_start;

    float brightness = 1.0 - fract(cell_to_runner_start / cells_per_runner);

    float middleness = 1.0 - 2.0 * abs(uv.y - 0.5);

    float green = pow(brightness, 32.0) * middleness;
    float not_green = pow(brightness, 128.0) * middleness;

    vec4 glow = vec4(not_green, green, not_green, 1.0);

    const float glyph_width = 128.0 / 8.0;   // 16
    const float glyph_height = 128.0 / 8.0;  // 16

    float glyph_row = floor(random(3.0 * column + 5.0 * row) * 8.0);
    float glyph_column = floor(random(7.0 * column + 9.0 * row) * 17.0);

    float shift_x = fract(uv.x * columns);
    float shift_y = 1.0 - fract(uv.y * rows);

    vec4 glyph_color = texture(
        Sampler0,
        vec2(
            (glyph_column + shift_x) / glyph_width,
            (glyph_row + shift_y) / glyph_height
        )
    );

    return glow * glyph_color;
}

void main() {
    vec3 ray = getSkyRay();
    vec2 codeUV;
    bool inside = getCodeSkyUV(ray, codeUV);

    if (!inside) {
        fragColor = vec4(0.0, 0.0, 0.0, 1.0);
        return;
    }

    vec4 code = getCodeRain(codeUV);
    fragColor = vec4(code.rgb, 1.0);
}
