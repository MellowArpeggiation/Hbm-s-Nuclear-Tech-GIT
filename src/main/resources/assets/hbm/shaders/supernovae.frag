#version 120

uniform float iTime;
uniform sampler2D iChannel1;

vec3 vPosition;

#define pi 3.14159265
#define R(p, a) p=cos(a)*p+sin(a)*vec2(p.y, -p.x)

// iq's noise
float noise(in vec3 x)
{
    vec3 p = floor(x);
    vec3 f = fract(x);
    f = f * f * (3.0 - 2.0 * f);
    vec2 uv = (p.xy + vec2(37.0, 17.0) * p.z) + f.xy;
    vec2 rg = texture2D(iChannel1, (uv + 0.5) / 256.0).yx;
    return 1.0 - 0.82 * mix(rg.x, rg.y, f.z);
}

float fbm(vec3 p)
{
    return noise(p * 0.06125) * 0.5 + noise(p * 0.125) * 0.25 + noise(p * 0.25) * 0.125 + noise(p * 0.4) * 0.2;
}

float Sphere(vec3 p, float r)
{
    return length(p) - r;
}
//==============================================================
// otaviogood's noise from https://www.shadertoy.com/view/ld2SzK
//--------------------------------------------------------------
// This spiral noise works by successively adding and rotating sin waves while increasing frequency.
// It should work the same on all computers since it's not based on a hash function like some other noises.
// It can be much faster than other noise functions if you're ok with some repetition.
const float nudge = 4.;	// size of perpendicular vector
float normalizer = 1.0 / sqrt(1.0 + nudge*nudge);	// pythagorean theorem on that perpendicular to maintain scale

const float nudge = 4.0;
float normalizer = 1.0 / sqrt(1.0 + nudge * nudge);
float SpiralNoiseC(vec3 p)
{
    float n = 1.0 - mod((iTime - 1.0) * 0.1, -1.0);
    float iter = 2.0;
    for (int i = 0; i < 8; i++)
    {
        n += -abs(sin(p.y * iter) + cos(p.x * iter)) / iter;
        p.xy += vec2(p.y, -p.x) * nudge;
        p.xy *= normalizer;
        p.xz += vec2(p.z, -p.x) * nudge;
        p.xz *= normalizer;
        iter *= 1.733733;
    }
    return n;
}

float VolumetricExplosion(vec3 p)
{
    float final = Sphere(p, 4.0);
    final += noise(p * 20.0) * 0.4;
    final += SpiralNoiseC(p.zxy * fbm(p * 10.0)) * 2.5;
    return final;
}

float map(vec3 p)
{
    R(p.xz, 0.008 * pi + (iTime - 1.0) * 0.05);
    float igt = 2.0 * sqrt(1.0 + mod((iTime - 1.0) * 0.1, -1.0));
    float VolExplosion = VolumetricExplosion(p / igt) * igt;
    return abs(VolExplosion) + 0.05 + igt * 0.0175;
}

vec3 computeColor(float density, float radius)
{
    vec3 result = mix(vec3(0.25, 0.5, 1.0), vec3(1.0, 0.5, 0.25), density);
    vec3 colCenter = 2.0 * vec3(0.1, 0.1, 4.0);
    vec3 colEdge = 2.0 * vec3(0.6, 0.1, 0.1);
    result *= mix(colCenter, colEdge, min((radius + 0.05) / 0.9, 1.15));
    return result;
}

bool RaySphereIntersect(vec3 org, vec3 dir, out float near, out float far)
{
    float b = dot(dir, org);
    float c = dot(org, org) - 8.0;
    float delta = b * b - c;
    if (delta < 0.0)
        return false;
    float deltasqrt = sqrt(delta);
    near = -b - deltasqrt;
    far = -b + deltasqrt;
    return far > 0.0;
}

void main()
{
    vec2 uv = gl_TexCoord[0].xy;
    uv = -1.0 + 2.0 * uv;
    uv *= 5.0;

    vec3 lookAt = vec3(0.0, -0.1, 0.0);

    float eyer = 2.0;
    float eyea = 0.0;
    float eyea2 = -0.24 * pi * 2.0;

    vec3 ro = vec3(
        eyer * cos(eyea) * sin(eyea2),
        eyer * cos(eyea2),
        eyer * sin(eyea) * sin(eyea2)
    );

    vec3 front = normalize(lookAt - ro);
    vec3 left = normalize(cross(normalize(vec3(0.0, 1, -0.1)), front));
    vec3 up = normalize(cross(front, left));
    vec3 rd = normalize(front * 1.5 + left * uv.x + up * uv.y);

    float ld = 0.0, td = 0.0, w = 0.0;
    float d = 1.0, t = 0.0;

    const float h = 0.1;
    vec4 sum = vec4(0.0);
    float min_dist = 0.0, max_dist = 0.0;

    if (RaySphereIntersect(ro, rd, min_dist, max_dist))
    {
        t = min_dist * step(t, min_dist);

        for (int i = 0; i < 64; i++)
        {
            vec3 pos = ro + t * rd;

            if (td > 0.9 || d < 0.11 * t || t > 10.0 || sum.a > 0.99 || t > max_dist)
                break;

            float d = map(pos);

            vec3 ldst = vec3(0.0) - pos;
            float lDist = max(length(ldst), 0.001);

            vec3 lightColor = vec3(0.5, 0.5, 0.5);
            sum.rgb += (vec3(0.67, 0.75, 1.00) / (lDist * lDist * 15.0)) / 100.0;
            sum.rgb += (lightColor / exp(lDist * lDist * lDist * 0.08) / 30.0);

            if (d < h)
            {
                ld = h - d;
                w = (1.0 - td) * ld;
                td += w + 1.0 / 200.0;

                vec4 col = vec4(computeColor(td, lDist), td);
                sum += sum.a * vec4(sum.rgb, 0.0) * 0.2 / lDist;
                col.a *= 0.2;
                col.rgb *= col.a;
                sum = sum + col * (1.0 - sum.a);
            }

            td += 1.0 / 70.0;

            t += max(d * 0.1 * max(min(length(ldst), length(ro)), 0.1), 0.01);
        }

        sum *= 1.0 / exp(ld * 0.2) * 0.8;
        sum = clamp(sum, 0.0, 1.0);
        sum.xyz = sum.xyz * sum.xyz * (3.0 - 2.0 * sum.xyz);
    }

    float tm = mod((iTime - 1.0), 10.0);

	float brightness = dot(sum.rgb, vec3(0.299, 0.587, 0.114)); 

	if (brightness < 0.5) {
		gl_FragColor = vec4(sum.rgb, 0.0); 
	} else {
		gl_FragColor = vec4(sum.rgb, 1.0); 
	}
}
