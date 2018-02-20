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