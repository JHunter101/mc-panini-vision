#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 OutSize;
uniform vec4 Params;
uniform vec4 View;

in vec2 texCoord;

out vec4 fragColor;

vec3 sampleSharp(sampler2D tex, vec2 uv, vec2 texelSize) {
    vec3 c = texture(tex, uv).rgb;
    vec3 n = texture(tex, uv + vec2(0.0, texelSize.y)).rgb;
    vec3 s = texture(tex, uv - vec2(0.0, texelSize.y)).rgb;
    vec3 e = texture(tex, uv + vec2(texelSize.x, 0.0)).rgb;
    vec3 w = texture(tex, uv - vec2(texelSize.x, 0.0)).rgb;

    vec3 crossAvg = 0.25 * (n + s + e + w);
    float sharpenAmount = 0.25;
    vec3 sharpened = c + sharpenAmount * (c - crossAvg);
    return max(sharpened, vec3(0.0));
}

void main() {
    float paniniD = clamp(Params.x, 0.0, 1.0);
    vec2 sourceExtent = max(Params.yz, vec2(0.0001));

    float cosLonEdge = inversesqrt(1.0 + sourceExtent.x * sourceExtent.x);
    float edgeScale = (paniniD + 1.0) / (paniniD + cosLonEdge);
    float fitScale = max(edgeScale * cosLonEdge, 0.0001);

    vec2 ndc = texCoord * 2.0 - 1.0;
    float cameraZoom = clamp(View.x, 0.5, 1.0);
    vec2 paniniPlane = ndc * sourceExtent * fitScale * cameraZoom;

    float dPlusOne = paniniD + 1.0;
    float normalizedPaniniX = paniniPlane.x / dPlusOne;
    float normalizedPaniniX2 = normalizedPaniniX * normalizedPaniniX;
    float discriminant = 1.0 + normalizedPaniniX2 * (1.0 - paniniD * paniniD);
    float cosLon = (-normalizedPaniniX2 * paniniD + sqrt(max(0.0, discriminant))) / (normalizedPaniniX2 + 1.0);
    float paniniScale = dPlusOne / (paniniD + cosLon);
    float horizontalDivisor = max(paniniScale * cosLon, 0.0001);
    float verticalCompression = clamp(Params.w, 0.0, 1.0);
    float verticalDivisor = max(paniniScale * mix(cosLon, 1.0, verticalCompression), 0.0001);

    vec2 sourceNdc = vec2(
        paniniPlane.x / (horizontalDivisor * sourceExtent.x),
        paniniPlane.y / (verticalDivisor * sourceExtent.y)
    );

    vec2 clampedNdc = clamp(sourceNdc, vec2(-1.0), vec2(1.0));
    vec2 sourceUv = clampedNdc * 0.5 + 0.5;

    vec2 texelSize = 1.0 / max(vec2(textureSize(DiffuseSampler, 0)), vec2(1.0));
    vec3 rgb = sampleSharp(DiffuseSampler, sourceUv, texelSize);
    fragColor = vec4(rgb, 1.0);
}