#version 120

uniform float iTime;
uniform sampler2D iChannel0;
vec3 vPosition;
vec2 vTexCoord;

const float PI = 3.14159265359;

const float ditherMatrix[16] = float[16](
    0.0,  8.0,  2.0, 10.0,
   12.0,  4.0, 14.0,  6.0,
    3.0, 11.0,  1.0,  9.0,
   15.0,  7.0, 13.0,  5.0
);

// Function to retrieve dither value
float getDitherValue(vec2 fragCoord) {
    int x = int(mod(fragCoord.x, 4.0));
    int y = int(mod(fragCoord.y, 4.0));
    int index = y * 4 + x;
    return ditherMatrix[index] / 16.0 - 0.5; // Normalize between -0.5 and 0.5
}

float snoise(vec3 uv, float res) {
    const vec3 s = vec3(1e0, 1e2, 1e3);
    uv *= res;
    vec3 uv0 = floor(mod(uv, res)) * s;
    vec3 uv1 = floor(mod(uv + vec3(1.), res)) * s;
    vec3 f = fract(uv);
    f = f * f * (3.0 - 2.0 * f);
    vec4 v = vec4(uv0.x + uv0.y + uv0.z, uv1.x + uv0.y + uv0.z,
                  uv0.x + uv1.y + uv0.z, uv1.x + uv1.y + uv0.z);
    vec4 r = fract(sin(v * 1e-1) * 1e3);
    float r0 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
    r = fract(sin((v + uv1.z - uv0.z) * 1e-1) * 1e3);
    float r1 = mix(mix(r.x, r.y, f.x), mix(r.z, r.w, f.x), f.y);
    return mix(r0, r1, f.z) * 2.0 - 1.0;
}

void main() {
    vec2 uv = gl_TexCoord[0].xy;
    uv = -1.0 + 2.0 * uv;
    uv *= -2.0;

    float color = 3.0 - (2. * length(2. * uv));

    vec3 coord = vec3(atan(uv.x, uv.y) / 3.2832 + 0.5, length(uv) * 0.3, 0.5);

    for (int i = 1; i <= 7; i++) {
        float power = pow(2.0, float(i));
        color += (1.5 / power) * snoise(coord + vec3(0.0, -iTime * 0.05, iTime * 0.01), power * 16.0);
    }

    vec4 fragColor = vec4(color, pow(max(color, 0.0), 2.0) * 0.4, pow(max(color, 0.0), 3.0) * 0.15, 1.0);

    float brightness = dot(fragColor.rgb, vec3(0.299, 0.587, 0.114));

    float ditherValue = getDitherValue(gl_FragCoord.xy) * 1.0; // Small effect scale
    fragColor.rgb += ditherValue;

    if (brightness < 0.1) {
        gl_FragColor = vec4(fragColor.rgb, 0.1);
    } else {
        gl_FragColor = vec4(fragColor.rgb, 1.0);
    }
}
