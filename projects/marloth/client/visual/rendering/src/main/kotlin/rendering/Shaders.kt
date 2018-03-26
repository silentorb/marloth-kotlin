package rendering

import mythic.drawing.DrawingEffects
import mythic.drawing.createDrawingEffects
import mythic.glowing.*
import mythic.spatial.Matrix
import mythic.spatial.Vector4
import java.util.*

private fun loadResource(name: String): String {
  val classloader = Thread.currentThread().contextClassLoader
  val inputStream = classloader.getResourceAsStream(name)
  val s = Scanner(inputStream).useDelimiter("\\A")
  val result = if (s.hasNext()) s.next() else ""
  return result
}

private val lighting = loadResource("shaders/lighting.glsl")

private val flatVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;
uniform vec4 uniformColor;

layout (location = 0) in vec3 position;

out vec4 fragment_color;

void main() {
	fragment_color = uniformColor;
  gl_Position = cameraTransform * modelTransform * vec4(position, 1);
}
"""

private val flatFragment = """
in vec4 fragment_color;
out vec4 output_color;

void main() {
  output_color = fragment_color;
}
"""

private val mainVertex = loadResource("shaders/mainVertex.glsl").replace("// #{lighting}", lighting)

private val texturedVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

out vec4 fragmentColor;
out vec4 fragmentPosition;
out vec3 fragmentNormal;
out vec2 textureCoordinates;

${lighting}

void main() {
  fragmentColor = vec4(1.0);
  fragmentPosition = normalTransform * vec4(fragmentPosition, 1.0);
  fragmentNormal = normalize((normalTransform * vec4(normal, 1.0)).xyz);
  gl_Position = cameraTransform * modelTransform * vec4(position, 1);
  textureCoordinates = uv;
}
"""

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

private val texturedFragment = """
in vec4 fragmentPosition;
in vec3 fragmentNormal;
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

${lighting}

uniform sampler2D text;
uniform mat4 normalTransform;
uniform vec3 cameraDirection;
uniform float glow;
uniform mat4 modelTransform;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
  vec3 lightResult = processLights(vec4(1), fragmentNormal, cameraDirection, fragmentPosition.xyz, glow);
  output_color = sampled * fragmentColor * vec4(lightResult, 1.0);
}
"""

class PerspectiveShader(val program: ShaderProgram) {
  val cameraTransform = MatrixProperty(program, "cameraTransform")
  val cameraDirection = Vector3Property(program, "cameraDirection")
  fun activate() {
    program.activate()
  }
}

class ColoredPerspectiveShader(val shader: PerspectiveShader) {
  val normalTransformProperty = MatrixProperty(shader.program, "normalTransform")
  val colorProperty = Vector4Property(shader.program, "uniformColor")
  val glowProperty = FloatProperty(shader.program, "glow")

  fun activate(color: Vector4, glow: Float, normalTransform: Matrix) {
    colorProperty.setValue(color)
    glowProperty.setValue(glow)
    normalTransformProperty.setValue(normalTransform)
    shader.activate()
  }
}

class FlatColoredPerspectiveShader(val shader: PerspectiveShader) {
  val colorProperty = Vector4Property(shader.program, "uniformColor")
  fun activate(color: Vector4) {
    colorProperty.setValue(color)
    shader.activate()
  }
}

class TextureShader(val colorShader: ColoredPerspectiveShader) {

  fun activate(texture: Texture, color: Vector4, glow: Float, normalTransform: Matrix) {
    texture.activate()
    colorShader.activate(color, glow, normalTransform)
  }
}

data class Shaders(
    val textured: TextureShader,
    val colored: ColoredPerspectiveShader,
    val flat: FlatColoredPerspectiveShader,
    val drawing: DrawingEffects
)

fun createShaders(): Shaders {
  return Shaders(
      textured = TextureShader(ColoredPerspectiveShader(PerspectiveShader(ShaderProgram(mainVertex, texturedFragment)))),
      colored = ColoredPerspectiveShader(PerspectiveShader(ShaderProgram(mainVertex, coloredFragment))),
      flat = FlatColoredPerspectiveShader(PerspectiveShader(ShaderProgram(flatVertex, flatFragment))),
      drawing = createDrawingEffects()
  )
}