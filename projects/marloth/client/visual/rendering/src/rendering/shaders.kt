package rendering

import mythic.glowing.MatrixProperty
import mythic.glowing.ShaderProgram
import mythic.drawing.DrawingEffects
import mythic.drawing.createDrawingEffects
import mythic.glowing.Texture
import mythic.glowing.Vector3Property

private val lighting = """

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

private val coloredVertex = """
uniform 	mat4 view;
uniform 	mat4 projection;
uniform 	vec3 cameraDirection;

in vec3 position;
in vec3 normal;
in vec4 color;

out vec4 fragment_color;
out vec3 fragmentPosition;
out vec3 fragment_normal;

uniform mat4 model;
uniform mat4 normal_transform;
uniform vec4 color_filter;

void main() {
  	fragment_normal = normalize((normal_transform * vec4(normal, 1.0)).xyz);
  	vec4 modelPosition = model * vec4(position, 1.0);
	fragmentPosition = modelPosition.xyz;
    gl_Position = projection * view * modelPosition;

	vec3 rgb = processLights(color, fragment_normal, cameraDirection, modelPosition.xyz);
    fragment_color = vec4(rgb, color.a) * color_filter;
}
"""

private val coloredFragment = """
in vec4 fragment_color;
out vec4 output_color;
void main() {
	output_color = fragment_color;
}
"""

private val flatVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec4 vertex_color;

out vec4 fragment_color;

void main() {
	fragment_color = vertex_color;
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

private val texturedVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;
uniform mat4 normalTransform;
uniform vec3 cameraDirection;

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

out vec4 fragmentColor;
out vec2 textureCoordinates;

${lighting}

void main() {
  vec3 fragmentNormal = normalize((normalTransform * vec4(normal, 1.0)).xyz);
  vec4 modelPosition = normalTransform * vec4(position, 1.0);
  vec3 rgb = processLights(vec4(1), fragmentNormal, cameraDirection, modelPosition.xyz);

  fragmentColor = vec4(rgb, 1);
  gl_Position = cameraTransform * modelTransform * vec4(position, 1);
  textureCoordinates = uv;
}
"""

private val texturedFragment = """
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

uniform sampler2D text;

void main() {
  vec4 sampled = texture(text, textureCoordinates);
  output_color = sampled * fragmentColor;
}
"""

class PerspectiveShader(val program: ShaderProgram) {
  val cameraTransform = MatrixProperty(program, "cameraTransform")
  fun activate() {
    program.activate()
  }
}

class TextureShader(val program: ShaderProgram) {
  val cameraTransform = MatrixProperty(program, "cameraTransform")
  val cameraDirection = Vector3Property(program, "cameraDirection")

  fun activate(texture: Texture) {
    texture.activate()
    program.activate()
  }
}

data class Shaders(
    val textured: TextureShader,
//    val colored: ShaderProgram,
    val flat: PerspectiveShader,
    val drawing: DrawingEffects
)

fun createShaders(): Shaders {
  return Shaders(
      textured = TextureShader(ShaderProgram(texturedVertex, texturedFragment)),
//      colored = ShaderProgram(coloredVertex, coloredFragment),
      flat = PerspectiveShader(ShaderProgram(flatVertex, flatFragment)),
      drawing = createDrawingEffects()
  )
}