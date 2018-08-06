#version 330 core
in vec2 TexCoords;
in vec3 FragPos;

out vec4 color;

uniform sampler2D image;
uniform sampler2D normalTex;
uniform vec4 spriteColor; // change to vec4


struct PointLight {
	vec3 position;

	vec3 ambient;
	vec3 diffuse;
	vec3 specular;

	float constant;
	float linear;
	float quadratic;
};

struct SpotLight {
	vec3  position;
  vec3  direction;
  float cutOff;		// must be a cos value
	float outerCutOff;
};

void main(){

	//diffuse
	vec3 lightPos 	= vec3(500,500,20000);
	vec3 lightColor = vec3(1,0.9,0.9);
	vec3 ambient 		= vec3(1,1,1);
	vec4 imgTex 		=  texture(image, TexCoords).xyzw;

	vec3 normal = normalize(texture(normalTex, TexCoords).xyz);

	vec3 lightDir = normalize(lightPos - FragPos);
	float diff = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = diff * lightColor;

	//directional light (sunLight)
	//vec3 lightDir = normalize(lightPos);

	//Point lightPoints
	PointLight light;
	light.constant = 1;
	light.linear = 0.004;
	light.constant = 0.000002;
	light.position = vec3(600,300,500);

	float distance = length(light.position - FragPos);
	float attenuation = 1 / (light.constant + light.linear * distance + light.quadratic * (distance* distance));

	//SpotLight
	SpotLight spotlight;
	spotlight.position = vec3(200,200, 1);
	spotlight.direction = vec3(200,200,0);
	spotlight.cutOff = 0.2;
	spotlight.outerCutOff = 0.4;

	float theta = dot(lightDir, normalize(-spotlight.direction));
	float epsilon   = spotlight.cutOff - spotlight.outerCutOff;
	float intensity = clamp((theta - spotlight.outerCutOff) / epsilon, 0.0, 1.0);

	vec3 result;

	if(theta > spotlight.cutOff){
		result = (diffuse * 0 + ambient * 0) * imgTex.xyz * spriteColor.xyz;
	}else{  // else, use ambient light so scene isn't completely dark outside the spotlight.
	  result = (diffuse * attenuation + ambient * attenuation) * imgTex.xyz * spriteColor.xyz;
	}

	if(imgTex.w>0){
		color = vec4(result, spriteColor.w);
	}else{
		color = vec4(result, 0);
	}
}
