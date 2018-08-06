#version 330 core
in vec2 TexCoords;
in vec3 FragPos;

out vec4 color;

#define MAX_POINT_LIGHTS 1

uniform sampler2D image;
uniform sampler2D normalTex;
uniform vec4 spriteColor;

struct PointLight {
	vec3 position;

	vec3 ambient;
	vec3 lightColor;

	float constant;
	float linear;
	float quadratic;
};

uniform PointLight pointLights[MAX_POINT_LIGHTS];

vec3 calculatePointLight(PointLight lightPoint, vec4 imgTex){

	vec3 normal = normalize(texture(normalTex, TexCoords).xyz);

	vec3 lightDir = normalize(lightPoint.position - FragPos);
	float diff = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = diff * lightPoint.lightColor;

	/*light.constant = 1;
	light.linear = 0.002;
	light.constant = 0.000002;
	light.position = vec3(600,300,500);*/

	float distance = length(lightPoint.position - FragPos);
	float attenuation = 1 / (lightPoint.constant + lightPoint.linear * distance + lightPoint.quadratic * (distance* distance));

	vec3 finalDiffuse = lightPoint.lightColor * attenuation;
	vec3 finalAmbient = lightPoint.ambient * attenuation;

	return vec3((finalDiffuse * finalAmbient) + (imgTex.xyz * spriteColor.xyz))*1;
}

void main(){

	vec4 imgTex =  texture(image, TexCoords).xyzw;
	vec3 result = vec3(0,0,0);

	for(int i=0; i< MAX_POINT_LIGHTS; i++)
		result += calculatePointLight(pointLights[i], imgTex);

	if(imgTex.w>0){
		color = vec4(result, spriteColor.w);
	}else{
		color = vec4(result, 0);
	}
}
