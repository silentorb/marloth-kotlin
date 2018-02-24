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

private val lighting2 = """

struct Light {
	int type;
	float brightness;
	vec3 position;
	vec3 direction;
	vec3 color;
};

const float constant_attenuation = 0.5;
const float linear_attenuation = 0.2;
const float quadratic_attenuation = 0.05;
const float shininess = 0.9;
const float strength = 0.3;
//const vec3 ambient = vec3(0.5);
vec3 ambient = vec3(0.1);

struct Relationship {
    vec3 direction;
    float distance;
};

Relationship get_relationship(Light light, vec3 position) {
    Relationship info;
	info.direction = normalize(light.position - position);
//	info.direction = normalize(vec3(-1, 0, 0));
	info.distance = length(info.direction);
	return info;
}

vec3 process_light(Light light, vec4 input_color, vec3 normal, vec3 cameraDirection, vec3 position) {
	Relationship info = get_relationship(light, position);

	float attenuation = 1.5;
	vec3 half_vector = normalize(info.direction + cameraDirection);

	float diffuse = max(0.0, dot(normal, info.direction * 1.0));
	float specular = max(0.0, dot(normal, half_vector));
//	diffuse = diffuse > 0.01 ? 2.0 : 0.5;

	if (diffuse == 0.0)
		specular = 0.0;
	 else
		specular = pow(specular, shininess) * strength;

	//specular = 0;
	vec3 scattered_light = ambient + light.color * diffuse * attenuation;
	vec3 reflected_light = light.color * specular * attenuation;
	reflected_light = vec3(0);
//	return scattered_light;
	vec3 rgb = min(input_color.rgb * scattered_light + reflected_light, vec3(1.0));
	return rgb;
}

vec3 processLights(vec4 input_color, vec3 normal, vec3 cameraDirection, vec3 position) {
	vec3 result = vec3(0);
	for(int i = 0; i < 1; ++i) {
//		result += process_light(Lighting.lights[i], input_color, normal);
	}

    {
        Light light;
        light.type = 0;
        light.brightness = 1.0;
        light.position = vec3(-1000, 0, 0);
        light.direction = vec3(0);
        light.color = vec3(0.9);
        result += process_light(light, input_color, normal, cameraDirection, position);
    }

    {
        Light light;
        light.type = 0;
        light.brightness = 1.0;
        light.position = vec3(10, -10, 1000);
        light.direction = vec3(0);
        light.color = vec3(0.4);
        result += process_light(light, input_color, normal, cameraDirection, position);
    }

//    result += ambient;

	return min(result, vec3(1.0));
}

"""

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
uniform mat4 modelTransform;
${lighting}

void main() {
  vec3 lightResult = processLights(vec4(1), fragmentNormal, cameraDirection, fragmentPosition.xyz);
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
uniform mat4 modelTransform;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
  vec3 lightResult = processLights(vec4(1), fragmentNormal, cameraDirection, fragmentPosition.xyz);
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
  fun activate(color: Vector4, normalTransform: Matrix) {
    colorProperty.setValue(color)
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

  fun activate(texture: Texture, color: Vector4, normalTransform: Matrix) {
    texture.activate()
    colorShader.activate(color, normalTransform)
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