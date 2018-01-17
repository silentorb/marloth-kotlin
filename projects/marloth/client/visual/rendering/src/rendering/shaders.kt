package rendering

import mythic.glowing.MatrixProperty
import mythic.glowing.ShaderProgram
import mythic.drawing.DrawingEffects
import mythic.drawing.createDrawingEffects

val coloredVertex = """
uniform 	mat4 view;
uniform 	mat4 projection;
uniform 	vec3 camera_direction;

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

vec3 process_light(Light light, vec4 input_color, vec3 normal, vec3 camera_direction, vec3 position) {
	Relationship info = get_relationship(light, position);

	float attenuation = 1.5;
	vec3 half_vector = normalize(info.direction + camera_direction);

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

vec3 process_lights(vec4 input_color, vec3 normal, vec3 camera_direction, vec3 position) {
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
        result += process_light(light, input_color, normal, camera_direction, position);
    }

    {
        Light light;
        light.type = 0;
        light.brightness = 1.0;
        light.position = vec3(10, -10, 1000);
        light.direction = vec3(0);
        light.color = vec3(0.4);
        result += process_light(light, input_color, normal, camera_direction, position);
    }

//    result += ambient;

	return min(result, vec3(1.0));
}

in vec3 position;
in vec3 normal;
in vec4 color;

out vec4 fragment_color;
out vec3 fragment_position;
out vec3 fragment_normal;

uniform mat4 model;
uniform mat4 normal_transform;
uniform vec4 color_filter;

void main() {
  	fragment_normal = normalize((normal_transform * vec4(normal, 1.0)).xyz);
  	vec4 model_position = model * vec4(position, 1.0);
	fragment_position = model_position.xyz;
    gl_Position = projection * view * model_position;

	vec3 rgb = process_lights(color, fragment_normal, camera_direction, model_position.xyz);
    fragment_color = vec4(rgb, color.a) * color_filter;
}
"""

val coloredFragment = """
in vec4 fragment_color;
out vec4 output_color;
void main() {
	output_color = fragment_color;
}
"""

val flatVertex = """
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

val flatFragment = """
in vec4 fragment_color;
out vec4 output_color;

void main() {
  output_color = fragment_color;
}
"""

val texturedVertex = """
uniform mat4 cameraTransform;
uniform mat4 modelTransform;

layout (location = 0) in vec3 position;
layout (location = 1) in vec3 normal;
layout (location = 2) in vec2 uv;

out vec4 fragment_color;
out vec2 textureCoordinates;

void main() {
	fragmentColor = vertex_color;
  gl_Position = cameraTransform * modelTransform * vec4(position, 1);
}
"""

val texturedFragment = """
in vec4 fragmentColor;
in vec2 textureCoordinates;
out vec4 output_color;

void main() {
  vec4 sample = vec4(1.0, 1.0, 1.0, texture(text, textureCoordinates).r);
  output_color = sample;
}
"""

class PerspectiveShader(val program: ShaderProgram) {
  val cameraTransform = MatrixProperty(program, "cameraTransform")
  fun activate() {
    program.activate()
  }
}

data class Shaders(
    val textured: ShaderProgram,
    val colored: ShaderProgram,
    val flat: PerspectiveShader,
    val drawing: DrawingEffects
)

fun createShaders(): Shaders {
  return Shaders(
      ShaderProgram(texturedVertex, texturedFragment),
      ShaderProgram(coloredVertex, coloredFragment),
      PerspectiveShader(ShaderProgram(flatVertex, flatFragment)),
      createDrawingEffects()
  )
}