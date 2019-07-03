package rendering.shading

const val sceneHeader = """
struct Scene {
  mat4 cameraTransform;
  vec3 cameraDirection;
};

layout(std140) uniform SceneUniform {
    Scene scene;
};

"""
