package rendering.shading

import loadTextResource
import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUniform1i

private val lighting = loadTextResource("shaders/lighting.glsl")

private val sceneHeader = """
struct Scene {
  mat4 cameraTransform;
  vec3 cameraDirection;
};

layout(std140) uniform SceneUniform {
    Scene scene;
};

"""

private val vertexShaderWithoutNormals = """
$sceneHeader
uniform mat4 modelTransform;
uniform vec4 uniformColor;

out vec4 fragmentColor;
out vec4 fragmentPosition;
out vec2 textureCoordinates;

void main() {
  fragmentColor = uniformColor;
  vec4 position4 = vec4(position, 1.0);
  vec4 modelPosition = modelTransform * position4;
  fragmentPosition = modelPosition;
  gl_Position = scene.cameraTransform * modelPosition;
  textureCoordinates = uv;
}
"""

private val weightHeader = """
layout (std140) uniform BoneTransforms {
  mat4[128] boneTransforms;
};
"""

private val weightApplication = """
  vec3 position3 = vec3(0.0);

  for (int i = 0; i < 4; ++i) {
    int boneIndex = int(joints[i]);
    float strength = weights[i];
    position3 += (boneTransforms[boneIndex] * position4).xyz * strength;
  }
  position4 = vec4(position3, 1.0);
"""

private val flatVertex = """
$sceneHeader
uniform mat4 modelTransform;
uniform vec4 uniformColor;

//#weightHeader

out vec4 fragment_color;

void main() {
	fragment_color = uniformColor;
  vec4 position4 = vec4(position, 1.0);
//#weightApplication
  vec4 modelPosition = modelTransform * position4;
  gl_Position = scene.cameraTransform * modelPosition;
}
"""

private val flatFragment = """
in vec4 fragment_color;
out vec4 output_color;

void main() {
  output_color = fragment_color;
}
"""

fun routeTexture(program: ShaderProgram, name: String, unit: Int) {
  val location = glGetUniformLocation(program.id, name)
  program.activate()
  glUniform1i(location, unit)
}

fun insertTemplates(source: String, replacements: Map<String, String>): String {
  var result = source
  replacements.forEach { name, snippet -> result = result.replace("//#" + name + "", snippet) }
  return result
}

private val mainVertex = sceneHeader + loadTextResource("shaders/mainVertex.glsl")

private fun addWeightShading(source: String) = insertTemplates(source, mapOf(
    "weightHeader" to weightHeader,
    "weightApplication" to weightApplication
))

private val coloredFragment = """
$sceneHeader
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
out vec4 output_color;
uniform mat4 normalTransform;
uniform float glow;
uniform mat4 modelTransform;

$lighting

void main() {
  vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);
	output_color = fragmentColor * vec4(lightResult, 1.0);
}
"""

private val lightingApplication1 = "vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);"
private val lightingApplication2 = "fragmentColor * vec4(lightResult, 1.0)"

private val texturedFragmentBase = """
$sceneHeader
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

//#lightingHeader

uniform sampler2D text;
uniform mat4 normalTransform;
uniform float glow;
uniform mat4 modelTransform;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
//#lightingApplication1
  output_color = @outColor;
}
"""

private val texturedFragmentWithoutNormalBase = """
$sceneHeader
in vec4 fragmentPosition;
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

uniform sampler2D text;
uniform float glow;
uniform mat4 modelTransform;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
  output_color = @outColor;
}
"""

private val texturedFragment = insertTemplates(texturedFragmentBase, mapOf(
    "lightingHeader" to lighting,
    "lightingApplication1" to lightingApplication1
)).replace("@outColor", "sampled * " + lightingApplication2)

private val texturedFragmentFlat = texturedFragmentBase
    .replace("@outColor", "sampled")

private val texturedFragmentFlatWithoutNormal = texturedFragmentWithoutNormalBase
    .replace("@outColor", "sampled")

class PerspectiveFeature(program: ShaderProgram) {
  val modelTransform = MatrixProperty(program, "modelTransform")
}

class ColoringFeature(program: ShaderProgram) {
  val colorProperty = Vector4Property(program, "uniformColor")
}

enum class UniformBufferId {
  SceneUniform,
  SectionUniform,
  BoneTransforms
}

fun bindUniformBuffer(id: UniformBufferId, program: ShaderProgram, buffer: UniformBuffer): UniformBufferProperty {
  val index = id.ordinal + 1
  return UniformBufferProperty(program, id.name, index, buffer)
}

class ShadingFeature(program: ShaderProgram, sectionBuffer: UniformBuffer) {
  val normalTransformProperty = MatrixProperty(program, "normalTransform")
  val glowProperty = FloatProperty(program, "glow")
  val sectionProperty = bindUniformBuffer(UniformBufferId.SectionUniform, program, sectionBuffer)
}

class SkeletonFeature(program: ShaderProgram, boneBuffer: UniformBuffer) {
  val boneTransformsProperty = bindUniformBuffer(UniformBufferId.BoneTransforms, program, boneBuffer)
}

fun populateBoneBuffer(boneBuffer: UniformBuffer, originalTransforms: List<Matrix>, transforms: List<Matrix>): UniformBuffer {
  val bytes = createBoneTransformBuffer(originalTransforms, transforms)
  boneBuffer.load(bytes)
  checkError("sending bone transforms")
  return boneBuffer
}

data class ShaderFeatureConfig(
    val shading: Boolean = false,
    val skeleton: Boolean = false,
    val texture: Boolean = false,
    val includeNormal: Boolean = true
)

data class ObjectShaderConfig(
    val transform: Matrix = Matrix(),
    val texture: Texture? = null,
    val color: Vector4 = Vector4(1f),
    val glow: Float = 0f,
    val normalTransform: Matrix? = null,
    val boneBuffer: UniformBuffer? = null
)

fun generateVertexInputHeader(config: ShaderFeatureConfig): String {
  val baseInputs = if (config.includeNormal)
    listOf(
        "vec3 position",
        "vec3 normal",
        "vec2 uv"
    )
  else
    listOf(
        "vec3 position",
        "vec2 uv"
    )

  val weightInputs = listOf(
      "vec4 joints",
      "vec4 weights"
  )

  val inputs = if (config.skeleton)
    baseInputs.plus(weightInputs)
  else
    baseInputs

  return inputs.mapIndexed { i, input ->
    "layout(location = ${i}) in ${input};"
  }.joinToString("\n")
}

fun generateVertexCodeBody(config: ShaderFeatureConfig): String {
  val baseCode = if (!config.includeNormal)
    vertexShaderWithoutNormals
  else if (config.shading || config.texture)
    mainVertex
  else
    flatVertex

  return if (config.skeleton)
    addWeightShading(baseCode)
  else
    baseCode
}

fun generateVertexCode(config: ShaderFeatureConfig): String {
  val inputHeader = generateVertexInputHeader(config)
  val mainBody = generateVertexCodeBody(config)

  return inputHeader + mainBody
}

fun generateShaderProgram(fragmentShader: String, featureConfig: ShaderFeatureConfig): ShaderProgram {
  val vertexShader = generateVertexCode(featureConfig)
  return ShaderProgram(vertexShader, fragmentShader)
}

class GeneralPerspectiveShader(buffers: UniformBuffers, fragmentShader: String, featureConfig: ShaderFeatureConfig) {
  val program = generateShaderProgram(fragmentShader, featureConfig)
  val perspective: PerspectiveFeature = PerspectiveFeature(program)
  val coloring: ColoringFeature = ColoringFeature(program)
  val sceneProperty = bindUniformBuffer(UniformBufferId.SceneUniform, program, buffers.scene)
  val shading: ShadingFeature? = if (featureConfig.shading) ShadingFeature(program, buffers.section) else null
  val skeleton: SkeletonFeature? = if (featureConfig.skeleton) SkeletonFeature(program, buffers.bone) else null

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

//    if (skeleton != null) {
//      skeleton.boneTransformsProperty.setValue(config.boneBuffer!!)
//    }

    if (config.texture != null) {
      config.texture.activate()
    }
  }
}

data class Shaders(
    val billboard: GeneralPerspectiveShader,
    val textured: GeneralPerspectiveShader,
    val texturedFlat: GeneralPerspectiveShader,
    val animated: GeneralPerspectiveShader,
    val colored: GeneralPerspectiveShader,
    val flat: GeneralPerspectiveShader,
    val coloredAnimated: GeneralPerspectiveShader,
    val flatAnimated: GeneralPerspectiveShader,
    val depthOfField: DepthScreenShader,
    val screenColor: ScreenColorShader,
    val screenDesaturation: DepthScreenShader,
    val screenTexture: DepthScreenShader
)

data class UniformBuffers(
    val scene: UniformBuffer,
    val section: UniformBuffer,
    val bone: UniformBuffer
)

fun createShaders(buffers: UniformBuffers): Shaders {
  return Shaders(
      billboard = GeneralPerspectiveShader(buffers, texturedFragmentFlatWithoutNormal, ShaderFeatureConfig(
          texture = true,
          includeNormal = false
      )),
      textured = GeneralPerspectiveShader(buffers, texturedFragment, ShaderFeatureConfig(
          shading = true,
          texture = true
      )),
      texturedFlat = GeneralPerspectiveShader(buffers, texturedFragmentFlat, ShaderFeatureConfig(
          texture = true
      )),
      animated = GeneralPerspectiveShader(buffers, texturedFragment, ShaderFeatureConfig(
          shading = true,
          skeleton = true,
          texture = true
      )),
      colored = GeneralPerspectiveShader(buffers, coloredFragment, ShaderFeatureConfig(
          shading = true
      )),
      coloredAnimated = GeneralPerspectiveShader(buffers, coloredFragment, ShaderFeatureConfig(
          shading = true,
          skeleton = true
      )),
      flat = GeneralPerspectiveShader(buffers, flatFragment, ShaderFeatureConfig()),
      flatAnimated = GeneralPerspectiveShader(buffers, flatFragment, ShaderFeatureConfig(
          skeleton = true
      )),
      depthOfField = DepthScreenShader(ShaderProgram(screenVertex, depthOfFieldFragment)),
      screenColor = ScreenColorShader(ShaderProgram(screenVertex, screenColorFragment)),
      screenDesaturation = DepthScreenShader(ShaderProgram(screenVertex, screenDesaturation)),
      screenTexture = DepthScreenShader(ShaderProgram(screenVertex, screenTextureFragment))
  )
}
