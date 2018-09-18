package rendering

import loadTextResource
import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUniform1i
import randomly.Dice

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

private val weightHeader = """
layout (std140) uniform BoneTransforms {
  mat4[128] boneTransforms;
};
"""

private val weightApplication = """
  vec3 position3 = vec3(0.0);

  for (int i = 0; i < 3; ++i) {
    int boneIndex = int(joints[i]);
    float strength = weights[i];
    position3 += (boneTransforms[boneIndex] * position4).xyz * strength;
  }
  position4 = vec4(position3, 1.0);
"""

private val flatVertex = """
${sceneHeader}
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

private val screenVertex = """
in vec4 vertex;
out vec2 texCoords;

void main()
{
  vec4 temp = vec4(vertex.xy, 0.0, 1.0);
  gl_Position = vec4(temp.x * 2.0 - 1.0, temp.y * 2.0 - 1.0, 0.0, 1.0);
  texCoords = vertex.zw;
}
"""

fun blurPrecalculations(range: Int): String {
  var result = ""
  val dice = Dice()
  var divisor = 0f
  for (smallY in -range..range) {
    for (smallX in -range..range) {
      if (smallX == 0 && smallY == 0)
        continue

      val x = smallX// * 2 + dice.getInt(0, 1) * if (smallX > 0) 1 else -1
      val y = smallY// * 2 + dice.getInt(0, 1) * if (smallY > 0) 1 else -1

      val distance = Vector2(x.toFloat(), y.toFloat()).length()
      val strength = 1f / (1f + distance / 2)
      divisor += strength
      result += """
      {
      vec3 localColorSample = textureOffset(colorTexture, texCoords, ivec2(${x}, ${y})).xyz;
//      float brightness = 0.2126 * localColorSample.x + 0.7152 * localColorSample.y + 0.0722 * localColorSample.z;
//      float strength = min(depthStrength * ${strength} + brightness / 2.0, 1.0);
      accumulator += localColorSample * ${strength};
//      divisor += ${strength};
      }
      """.trimIndent()
    }
  }
  return result + "float divisor = ${divisor};"
}

private const val blurRange = 3

//private const val minBlurDepth = 0.999f
private const val minBlurDepth = 0.9985f
//private const val maxBlurDepth = 1f
private const val blurDepthStretch = 1f / (1f - minBlurDepth)

private val screenFragment = """
in vec2 texCoords;
out vec4 output_color;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

void main()
{
  vec3 primaryColorSample = texture(colorTexture, texCoords).xyz;
  float primaryDepthSample = texture(depthTexture, texCoords).x;

  // Filter out any depth values below minBlurDepth
  float filteredDepth = max(primaryDepthSample - ${minBlurDepth}, 0.0);
  float depthStrength = filteredDepth * ${blurDepthStretch};

  vec3 accumulator = primaryColorSample;

${blurPrecalculations(blurRange)}

  vec3 average = accumulator / divisor;
 vec3 result = average * depthStrength + primaryColorSample * (1 - depthStrength);

  output_color = vec4(result, 1.0);
}
"""

fun routeTexture(program: ShaderProgram, name: String, unit: Int) {
  val location = glGetUniformLocation(program.id, name)
  program.activate()
  glUniform1i(location, unit)
}

class ScreenShader(val program: ShaderProgram) {

  init {
    routeTexture(program, "colorTexture", 0)
    routeTexture(program, "depthTexture", 1)
  }

  fun activate() {
    program.activate()
  }
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
${sceneHeader}
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
out vec4 output_color;
uniform mat4 normalTransform;
uniform float glow;
uniform mat4 modelTransform;

${lighting}

void main() {
  vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);
	output_color = fragmentColor * vec4(lightResult, 1.0);
}
"""

private val lightingApplication1 = "vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);"
private val lightingApplication2 = "fragmentColor * vec4(lightResult, 1.0)"

private val texturedFragmentBase = """
${sceneHeader}
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

private val texturedFragment = insertTemplates(texturedFragmentBase, mapOf(
    "lightingHeader" to lighting,
    "lightingApplication1" to lightingApplication1
)).replace("@outColor", "sampled * " + lightingApplication2)

private val texturedFragmentFlat = texturedFragmentBase
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
    val texture: Boolean = false
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
  val baseInputs = listOf(
      "vec3 position",
      "vec3 normal",
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
  val baseCode = if (config.shading || config.texture)
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
    val textured: GeneralPerspectiveShader,
    val texturedFlat: GeneralPerspectiveShader,
    val animated: GeneralPerspectiveShader,
    val colored: GeneralPerspectiveShader,
    val flat: GeneralPerspectiveShader,
    val flatAnimated: GeneralPerspectiveShader,
    val screen: ScreenShader
)

data class UniformBuffers(
    val scene: UniformBuffer,
    val section: UniformBuffer,
    val bone: UniformBuffer
)

fun createShaders(buffers: UniformBuffers): Shaders {
  return Shaders(
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
      flat = GeneralPerspectiveShader(buffers, flatFragment, ShaderFeatureConfig()),
      flatAnimated = GeneralPerspectiveShader(buffers, flatFragment, ShaderFeatureConfig(
          skeleton = true
      )),
      screen = ScreenShader(ShaderProgram(screenVertex, screenFragment))
  )
}