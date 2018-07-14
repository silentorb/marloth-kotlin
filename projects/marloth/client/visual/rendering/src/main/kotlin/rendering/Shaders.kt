package rendering

import mythic.breeze.Bones
import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector3
import mythic.spatial.Vector4
import java.util.*

private fun loadBinaryResource(name: String): String {
  val classloader = Thread.currentThread().contextClassLoader
  val inputStream = classloader.getResourceAsStream(name)
  val s = Scanner(inputStream).useDelimiter("\\A")
  val result = if (s.hasNext()) s.next() else ""
  return result
}

private val lighting = loadBinaryResource("shaders/lighting.glsl")

private val weightHeader = """
layout (location = 3) in vec2[2] weights;
layout (std140) uniform BoneTransforms {
  mat4[128] boneTransforms;
};
"""

private val weightApplication = """
  vec3 position3 = position * (1 - weights[0][1] - weights[1][1]);

  for (int i = 0; i < 2; ++i) {
    int boneIndex = int(weights[i][0]);
    float strength = weights[i][1];
    position3 += (boneTransforms[boneIndex] * position4).xyz * strength;
  }
  position4 = vec4(position3, 1.0);
"""

private val flatVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;
uniform vec4 uniformColor;

layout (location = 0) in vec3 position;

//#weightHeader

out vec4 fragment_color;

void main() {
	fragment_color = uniformColor;
  vec4 position4 = vec4(position, 1.0);
//#weightApplication
  vec4 modelPosition = modelTransform * position4;
  gl_Position = cameraTransform * modelPosition;
}
"""

private val flatFragment = """
in vec4 fragment_color;
out vec4 output_color;

void main() {
  output_color = fragment_color;
}
"""

fun insertTemplates(source: String, replacements: Map<String, String>): String {
  var result = source
  replacements.forEach { name, snippet -> result = result.replace("//#" + name + "", snippet) }
  return result
}

private val mainVertex = loadBinaryResource("shaders/mainVertex.glsl")

private fun addWeightShading(source: String) = insertTemplates(source, mapOf(
    "weightHeader" to weightHeader,
    "weightApplication" to weightApplication
))

private val animatedVertex = addWeightShading(mainVertex)
private val animatedFlatVertex = addWeightShading(flatVertex)

private val coloredFragment = """
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
out vec4 output_color;
uniform mat4 normalTransform;
uniform vec3 cameraDirection;
uniform float glow;
uniform mat4 modelTransform;

${lighting}

void main() {
  vec3 lightResult = processLights(vec4(1), fragmentNormal, cameraDirection, fragmentPosition.xyz, glow);
	output_color = fragmentColor * vec4(lightResult, 1.0);
}
"""

private val lightingApplication1 = "vec3 lightResult = processLights(vec4(1), fragmentNormal, cameraDirection, fragmentPosition.xyz, glow);"
private val lightingApplication2 = "fragmentColor * vec4(lightResult, 1.0)"

private val texturedFragmentBase = """
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

//#lightingHeader

uniform sampler2D text;
uniform mat4 normalTransform;
uniform vec3 cameraDirection;
uniform float glow;
uniform mat4 modelTransform;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
//#lightingApplication1
  output_color = @outColor;
}
"""

private val texturedFragment = insertTemplates(texturedFragmentBase, mapOf(
    "lightingHeader" to lighting,
    "lightingApplication1" to lightingApplication1
)).replace("@outColor", "sampled * " + lightingApplication2)

private val texturedFragmentFlat = texturedFragmentBase
    .replace("@outColor", "sampled")

class PerspectiveFeature(program: ShaderProgram) {
  val cameraTransform = MatrixProperty(program, "cameraTransform")
  val modelTransform = MatrixProperty(program, "modelTransform")
}

class ColoringFeature(program: ShaderProgram) {
  val colorProperty = Vector4Property(program, "uniformColor")
}

class ShadingFeature(program: ShaderProgram, sceneBuffer: UniformBuffer) {
  val normalTransformProperty = MatrixProperty(program, "normalTransform")
  val glowProperty = FloatProperty(program, "glow")
  val cameraDirection = Vector3Property(program, "cameraDirection")
  val sceneProperty = UniformBufferProperty(program, "SceneUniform", 1, sceneBuffer)
}

class SkeletonFeature(program: ShaderProgram, boneBuffer: UniformBuffer) {
  val boneTransformsProperty = UniformBufferProperty(program, "BoneTransforms", 2, boneBuffer)
}

fun populateBoneBuffer(boneBuffer: UniformBuffer, bones: Bones): UniformBuffer {
  val bytes = createBoneTransformBuffer(bones)
  boneBuffer.load(bytes)
  checkError("sending bone transforms")
  return boneBuffer
}

data class ShaderFeatureConfig(
    val shading: Boolean = false,
    val skeleton: Boolean = false
)

data class SceneShaderConfig(
    val cameraTransform: Matrix,
    val cameraDirection: Vector3,
    val sceneBuffer: UniformBuffer
)

data class ObjectShaderConfig(
    val transform: Matrix = Matrix(),
    val texture: Texture? = null,
    val color: Vector4 = Vector4(1f),
    val glow: Float = 0f,
    val normalTransform: Matrix? = null,
    val boneBuffer: UniformBuffer? = null
)

fun generateShaderProgram(vertexShader: String, fragmentShader: String, featureConfig: ShaderFeatureConfig): ShaderProgram {
  return ShaderProgram(vertexShader, fragmentShader)
}

class GeneralShader(buffers: UniformBuffers, vertexShader: String, fragmentShader: String, featureConfig: ShaderFeatureConfig) {
  val program = generateShaderProgram(vertexShader, fragmentShader, featureConfig)
  val perspective: PerspectiveFeature = PerspectiveFeature(program)
  val coloring: ColoringFeature = ColoringFeature(program)
  val shading: ShadingFeature? = if (featureConfig.shading) ShadingFeature(program, buffers.scene) else null
  val skeleton: SkeletonFeature? = if (featureConfig.skeleton) SkeletonFeature(program, buffers.bone) else null

  fun updateScene(config: SceneShaderConfig) {
    program.activate()
    perspective.cameraTransform.setValue(config.cameraTransform)
    if (shading != null) {
      shading.cameraDirection.setValue(config.cameraDirection)
//      shading.sceneProperty.setValue(config.sceneBuffer)
    }
  }

  // IntelliJ will flag this use of inline as a warning, but using inline here
  // causes the JVM to optimize away the ObjectShaderConfig allocation and significantly
  // reduces the amount of objects created each frame.
  inline fun activate(config: ObjectShaderConfig) {
    program.activate()

    perspective.modelTransform.setValue(config.transform)
    coloring.colorProperty.setValue(config.color)

    if (shading != null) {
      shading.glowProperty.setValue(config.glow)
      shading.normalTransformProperty.setValue(config.normalTransform!!)
    }

    if (skeleton != null) {
//      skeleton.boneTransformsProperty.setValue(config.boneBuffer!!)
    }

    if (config.texture != null) {
      config.texture.activate()
    }
  }

}

data class Shaders(
    val textured: GeneralShader,
    val texturedFlat: GeneralShader,
    val animated: GeneralShader,
    val colored: GeneralShader,
    val flat: GeneralShader,
    val flatAnimated: GeneralShader
)

data class UniformBuffers(
    val scene: UniformBuffer,
    val bone: UniformBuffer
)

fun createShaders(buffers: UniformBuffers): Shaders {
  return Shaders(
      textured = GeneralShader(buffers, mainVertex, texturedFragment, ShaderFeatureConfig(
          shading = true
      )),
      texturedFlat = GeneralShader(buffers, mainVertex, texturedFragmentFlat, ShaderFeatureConfig()),
      animated = GeneralShader(buffers, animatedVertex, texturedFragment, ShaderFeatureConfig(
          shading = true,
          skeleton = true
      )),
      colored = GeneralShader(buffers, mainVertex, coloredFragment, ShaderFeatureConfig(
          shading = true
      )),
      flat = GeneralShader(buffers, flatVertex, flatFragment, ShaderFeatureConfig()),
      flatAnimated = GeneralShader(buffers, animatedFlatVertex, flatFragment, ShaderFeatureConfig(
          skeleton = true
      ))
  )
}