package rendering.shading

import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import org.lwjgl.opengl.GL20.glGetUniformLocation
import org.lwjgl.opengl.GL20.glUniform1i
import rendering.meshes.AttributeName
import rendering.meshes.VertexSchemas

//private val flatFragment = """
//in vec4 fragmentColor;
//out vec4 output_color;
//
//void main() {
//  output_color = uniformColor;
//}
//"""

fun routeTexture(program: ShaderProgram, name: String, unit: Int) {
  val location = glGetUniformLocation(program.id, name)
  program.activate()
  glUniform1i(location, unit)
}

//fun insertTemplates(source: String, replacements: Map<String, String>): String {
//  var result = source
//  replacements.forEach { name, snippet -> result = result.replace("//#" + name + "", snippet) }
//  return result
//}

//private val coloredFragment = """
//$sceneHeader
//in vec4 fragmentPosition;
//in vec3 fragmentNormal;
//in vec4 fragmentColor;
//out vec4 output_color;
//uniform mat4 normalTransform;
//uniform float glow;
//uniform mat4 modelTransform;
//
//$lightingHeader
//
//void main() {
//  vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);
//	output_color = fragmentColor * vec4(lightResult, 1.0);
//}
//"""

//private val texturedFragment = generateFragmentShader(FragmentShaderConfig(
//    lighting = true
//))
//
//private val texturedFragmentFlat = generateFragmentShader(FragmentShaderConfig(
//))

class PerspectiveFeature(program: ShaderProgram) {
  val modelTransform = MatrixProperty(program, "modelTransform")
}

class ColoringFeature(program: ShaderProgram) {
  val colorProperty = Vector4Property(program, "uniformColor")
}

enum class UniformBufferId {
  InstanceUniform,
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
    val instanced: Boolean = false,
    val animatedTexture: Boolean = false // Requires `texture` == true
)

data class ObjectShaderConfig(
    val transform: Matrix = Matrix(),
    val texture: Texture? = null,
    val color: Vector4 = Vector4(1f),
    val glow: Float = 0f,
    val normalTransform: Matrix? = null,
    val boneBuffer: UniformBuffer? = null
)

fun generateShaderProgram(vertexSchema: VertexSchema<AttributeName>, featureConfig: ShaderFeatureConfig): ShaderProgram {
  val vertexShader = generateVertexCode(featureConfig)(vertexSchema)
  val fragmentShader = generateFragmentShader(featureConfig)
  return ShaderProgram(vertexShader, fragmentShader)
}

class GeneralPerspectiveShader(buffers: UniformBuffers, vertexSchema: VertexSchema<AttributeName>, featureConfig: ShaderFeatureConfig) {
  val program = generateShaderProgram(vertexSchema, featureConfig)
  val perspective: PerspectiveFeature = PerspectiveFeature(program)
  val coloring: ColoringFeature = ColoringFeature(program)
  val instanceProperty = if (featureConfig.instanced) bindUniformBuffer(UniformBufferId.InstanceUniform, program, buffers.instance) else null
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
    val instance: UniformBuffer,
    val scene: UniformBuffer,
    val section: UniformBuffer,
    val bone: UniformBuffer
)

fun createShaders(vertexSchemas: VertexSchemas, buffers: UniformBuffers): Shaders {
  return Shaders(
      billboard = GeneralPerspectiveShader(buffers, vertexSchemas.billboard, ShaderFeatureConfig(
          texture = true,
          instanced = true
      )),
      textured = GeneralPerspectiveShader(buffers, vertexSchemas.textured, ShaderFeatureConfig(
          shading = true,
          texture = true
      )),
      texturedFlat = GeneralPerspectiveShader(buffers, vertexSchemas.textured, ShaderFeatureConfig(
          texture = true
      )),
      animated = GeneralPerspectiveShader(buffers, vertexSchemas.animated, ShaderFeatureConfig(
          shading = true,
          skeleton = true,
          texture = true
      )),
      colored = GeneralPerspectiveShader(buffers, vertexSchemas.shaded, ShaderFeatureConfig(
          shading = true
      )),
      coloredAnimated = GeneralPerspectiveShader(buffers, vertexSchemas.animated, ShaderFeatureConfig(
          shading = true,
          skeleton = true
      )),
      flat = GeneralPerspectiveShader(buffers, vertexSchemas.flat, ShaderFeatureConfig()),
      flatAnimated = GeneralPerspectiveShader(buffers, vertexSchemas.animated, ShaderFeatureConfig(
          skeleton = true
      )),
      depthOfField = DepthScreenShader(ShaderProgram(screenVertex, depthOfFieldFragment)),
      screenColor = ScreenColorShader(ShaderProgram(screenVertex, screenColorFragment)),
      screenDesaturation = DepthScreenShader(ShaderProgram(screenVertex, screenDesaturation)),
      screenTexture = DepthScreenShader(ShaderProgram(screenVertex, screenTextureFragment))
  )
}
