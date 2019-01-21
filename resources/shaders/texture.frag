#version 330 core
in vec2 TexCoords;
in vec3 FragPos;

out vec4 color;

#define MAX_POINT_LIGHTS 1

uniform sampler2D image;
uniform sampler2D normalTex;
uniform vec4 spriteColor;
uniform float dayTime;
uniform vec3 ambientColor;

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

	vec3 lightDir = vec3(0,0,2);
	//vec3 lightDir = normalize(lightPoint.position - FragPos);
	float diff = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = diff * lightPoint.lightColor;

	/*light.constant = 1;
	light.linear = 0.002;
	light.constant = 0.000002;
	light.position = vec3(600,300,500);*/

	//FragPos.x/2
	float distance = length(lightPoint.position - vec3(FragPos.xyz));
	float attenuation = 1 / (lightPoint.constant + lightPoint.linear * distance + lightPoint.quadratic * (distance* distance));

	float finalAttenuation = max(attenuation,dayTime);
	//vec3 finalDiffuse = lightPoint.lightColor * finalAttenuation;
	//vec3 finalAmbient = lightPoint.ambient * finalAttenuation;

	//return vec3((finalDiffuse + finalAmbient) * (imgTex.xyz * spriteColor.xyz));
	return vec3(
		(imgTex.xyz + ambientColor + (lightPoint.lightColor * diffuse *attenuation*0.8) ) * finalAttenuation
		); //TODO: apply normals
}

void main(){

	vec4 imgTex =  texture(image, TexCoords).xyzw;
	vec3 result = vec3(0,0,0);

	for(int i=0; i< MAX_POINT_LIGHTS; i++)
		result += calculatePointLight(pointLights[i], imgTex);

	if(imgTex.w>0){
		color = vec4(result, imgTex.w);
	}else{
		color = vec4(result, imgTex.w);
	}
}
