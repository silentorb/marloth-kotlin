package mythic.drawing

import mythic.glowing.MatrixProperty
import mythic.glowing.ShaderProgram
import mythic.glowing.Vector4Property
import mythic.spatial.Matrix
import mythic.spatial.Vector4


private val imageVertex = """
in vec4 vertex;
out vec2 TexCoords;

uniform mat4 projection;
uniform mat4 transform;

void main()
{
    vec4 temp = transform * vec4(vertex.xy, 0.0, 1.0);
    gl_Position = vec4(temp.x / 500.0 - 1.0, (1000.0 - temp.y) / 500.0 - 1.0, 0.0, 1);
 //    gl_Position = projection * transform * vec4(vertex.xy, 0.0, 1.0);
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

//fun createImageShader() = ShaderProgram(imageVertex, imageFragment)

class ColoredImageShader {
  val program: ShaderProgram = ShaderProgram(imageVertex, imageFragment)
  val projectionProperty = MatrixProperty(program, "projection")
  val transformProperty = MatrixProperty(program, "transform")
  val colorProperty = Vector4Property(program, "color")
}

class ColoredImageEffect(private val shader: ColoredImageShader, projection: Matrix) {
  init {
    shader.projectionProperty.setValue(projection)
  }

  fun activate(transform: Matrix, color: Vector4) {
    shader.transformProperty.setValue(transform)
    shader.colorProperty.setValue(color)
    shader.program.activate()
  }
}