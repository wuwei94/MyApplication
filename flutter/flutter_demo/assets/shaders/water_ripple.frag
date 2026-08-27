#version 460 core
#include <flutter/runtime_effect.glsl>

uniform vec2 uSize;
uniform float uTime;

out vec4 fragColor;

void main() {
  vec2 uv = FlutterFragCoord().xy / uSize;

  vec2 center = uv - vec2(0.5);
  float dist = length(center);

  float ripple = sin(dist * 40.0 - uTime * 5.0);
  float glow = smoothstep(0.0, 0.6, 0.5 + 0.5 * ripple);

  vec3 color = mix(vec3(0.08, 0.25, 0.75), vec3(0.95, 0.25, 0.55), glow);
  color += vec3(0.15) * exp(-dist * 8.0);

  fragColor = vec4(color, 1.0);
}
