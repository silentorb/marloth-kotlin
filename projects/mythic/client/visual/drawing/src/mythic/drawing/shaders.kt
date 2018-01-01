package mythic.drawing

import mythic.glowing.MatrixProperty
import mythic.glowing.ShaderProgram
import mythic.glowing.Vector4Property
import mythic.spatial.Matrix
import mythic.spatial.Vector4


private val imageVertex = """
in vec4 vertex;
out vec2 TexCoords;

uniform mat4 transform;

void main()
{
    vec4 temp = transform * vec4(vertex.xy, 0.0, 1.0);
    gl_Position = vec4(temp.x / 500.0 - 1.0, (1000.0 - temp.y) / 500.0 - 1.0, 0.0, 1);
   TexCoords = vertex.zw;
}
"""

private val imageFragment = """
in vec2 TexCoords;
out vec4 output_color;

uniform sampler2D text;
uniform vec4 color;

void main()
{
    vec4 sampled = vec4(1.0, 1.0, 1.0, texture(text, TexCoords).r);
    output_color = color * sampled;
}
"""

private val singleColorVertex = """
in vec2 vertex;

uniform mat4 projection;
uniform mat4 transform;

void main()
{
    vec4 temp = transform * vec4(vertex.xy, 0.0, 1.0);
    gl_Position = vec4(temp.x / 500.0 - 1.0, (1000.0 - temp.y) / 500.0 - 1.0, 0.0, 1);
}
"""

private val singleColorFragment = """
uniform vec4 color;
out vec4 output_color;

void main()
{
    output_color = color;
}
"""

class ColoredImageShader {
  val program: ShaderProgram = ShaderProgram(imageVertex, imageFragment)
  val transformProperty = MatrixProperty(program, "transform")
  val colorProperty = Vector4Property(program, "color")

  fun activate(transform: Matrix, color: Vector4) {
    transformProperty.setValue(transform)
    colorProperty.setValue(color)
    program.activate()
  }
}

class SingleColorShader {

}

data class DrawingEffects(
    val coloredImage: ColoredImageShader,
    val singleColorShader: SingleColorShader
)

fun createDrawingEffects() = DrawingEffects(
    ColoredImageShader(),
    SingleColorShader()
)
