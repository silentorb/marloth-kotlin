package rendering.shading

import assets.loadTextResource

val lightingHeader = loadTextResource("shaders/lighting.glsl")

const val lightingApplication1 = "vec3 lightResult = processLights(vec4(1), fragmentNormal, scene.cameraDirection, fragmentPosition.xyz, glow);"
const val lightingApplication2 = "uniformColor * vec4(lightResult, 1.0)"

fun addIf(condition: Boolean, value: String) = if (condition) value else null

fun fragmentHeader(config: ShaderFeatureConfig): String {
  return listOfNotNull(
      """uniform vec4 uniformColor;
uniform mat4 normalTransform;
uniform float glow;
uniform mat4 modelTransform;
out vec4 output_color;""",
      if (config.texture)
        """uniform sampler2D text;
in vec2 textureCoordinates;"""
      else null,
      addIf(config.shading, lightingHeader)
  ).joinToString("\n")
}

private fun textureOperations(config: ShaderFeatureConfig) =
    if (config.texture)
      "vec4 sampled = texture(text, textureCoordinates);"
    else
      null

fun generateFragmentShader(config: ShaderFeatureConfig): String {
  val outColor = listOfNotNull(
      if (config.instanced) "fragmentColor" else "uniformColor",
      if (config.texture) "sampled" else null,
      if (config.shading) lightingApplication2 else null
  ).joinToString(" * ")

  val mainBody = listOfNotNull(
      textureOperations(config),
      addIf(config.shading, lightingApplication1),
      "output_color = $outColor;"
  ).joinToString("\n")

  val inputs = listOf(
      Pair(4, "fragmentPosition"),
      Pair(3, "fragmentNormal"),
      Pair(2, "fragmentUv"),
      Pair(4, "fragmentColor")
      )
      .filter { mainBody.contains(it.second) }
      .map { (size, name) ->
        "in vec$size $name;"
      }.joinToString("\n")

  val mainFunction = """void main() {
$mainBody
}
"""

  return listOfNotNull(
      sceneHeader,
      inputs,
      fragmentHeader(config),
      mainFunction
  ).joinToString("\n")
}