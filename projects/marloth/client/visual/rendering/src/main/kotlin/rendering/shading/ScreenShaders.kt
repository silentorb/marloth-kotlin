package rendering.shading

import mythic.glowing.ShaderProgram
import mythic.glowing.Vector4Property
import mythic.spatial.Vector2
import mythic.spatial.Vector4
import randomly.Dice


val screenVertex = """
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

val depthOfFieldFragment = """
in vec2 texCoords;
out vec4 output_color;

uniform sampler2D colorTexture;
uniform sampler2D depthTexture;

void main()
{
  vec3 primaryColorSample = texture(colorTexture, texCoords).xyz;
  float primaryDepthSample = texture(depthTexture, texCoords).x;

  // Filter out any depth values below minBlurDepth
  float filteredDepth = max(primaryDepthSample - $minBlurDepth, 0.0);
  float depthStrength = filteredDepth * $blurDepthStretch;

  vec3 accumulator = primaryColorSample;

${blurPrecalculations(blurRange)}

  vec3 average = accumulator / divisor;
 vec3 result = average * depthStrength + primaryColorSample * (1 - depthStrength);

  output_color = vec4(result, 1.0);
}
"""

val screenColorFragment = """
in vec2 texCoords;
out vec4 output_color;
uniform sampler2D colorTexture;
uniform vec4 inputColor;

void main()
{
  vec3 primaryColorSample = texture(colorTexture, texCoords).xyz;
  vec3 rgb = min(vec3(1.0), primaryColorSample + inputColor.xyz * inputColor.w);
  output_color = vec4(rgb, 1.0);
}
"""

val screenDesaturation = """
in vec2 texCoords;
out vec4 output_color;
uniform sampler2D colorTexture;

void main()
{
  vec3 s = texture(colorTexture, texCoords).xyz;
  float level = (s.x + s.y + s.z) / 3.0;
  vec3 rgb = vec3(level);
  output_color = vec4(rgb, 1.0);
}
"""

class ScreenShader(val program: ShaderProgram) {

  init {
    routeTexture(program, "colorTexture", 0)
    routeTexture(program, "depthTexture", 1)
  }

  fun activate() {
    program.activate()
  }
}

class ScreenColorShader(val program: ShaderProgram) {
  private val colorProperty = Vector4Property(program, "inputColor")

  init {
    routeTexture(program, "colorTexture", 0)
    routeTexture(program, "depthTexture", 1)
  }

  fun activate(color: Vector4) {
    colorProperty.setValue(color)
    program.activate()
  }
}