layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

uniform vec3 cameraDirection;
uniform mat4 cameraTransform;
uniform mat4 modelTransform;
uniform mat4 normalTransform;
uniform vec4 uniformColor;

out vec4 fragmentColor;
out vec2 textureCoordinates;

// #{lighting}

void main() {
  vec3 fragmentNormal = normalize((normalTransform * vec4(normal, 1.0)).xyz);
  vec4 modelPosition = modelTransform * vec4(position, 1.0);
  vec3 rgb = processLights(uniformColor, fragmentNormal, cameraDirection, modelPosition.xyz);
  gl_Position = cameraTransform * modelTransform * vec4(position, 1);
  fragmentColor = vec4(rgb, uniformColor.a);
}