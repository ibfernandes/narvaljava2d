#version 330 core
in vec2 TexCoords;
in vec3 FragPos;

out vec4 color;

#define MAX_POINT_LIGHTS 10

uniform sampler2D image;
uniform sampler2D normalTex;
uniform vec4 spriteColor;
uniform float dayTime;
uniform vec3 ambientColor;

struct PointLight {
	vec3 position;

	vec3 ambient;
	vec3 lightColor; //also diffuseColor

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
		( (imgTex.xyz + ambientColor + lightPoint.lightColor * diffuse *attenuation*0.8) ) * finalAttenuation
		); //TODO: apply normals
}

vec3 calculatePointLightLOPENGL(PointLight light){
	//vec3 lightDir = normalize(light.position - FragPos);
	vec3 lightDir = vec3(0,0,2);
	vec3 normal = normalize(texture(normalTex, TexCoords).xyz);

    // Diffuse shading
    float diff = max(dot(normal, lightDir), 0.0);

    // Attenuation
    float distance = length(light.position - FragPos);
    float attenuation = 1.0f / (light.constant + light.linear * distance + light.quadratic * (distance * distance));    

    // Combine results
    vec3 ambient = light.ambient * vec3(texture(image, TexCoords));
    vec3 diffuse = light.lightColor * diff * vec3(texture(image, TexCoords));

    ambient *= clamp(attenuation,dayTime, 1.0f);
    diffuse *= clamp(attenuation,dayTime, 1.0f);
    return (ambient + diffuse);
}

void main(){

	vec4 imgTex =  texture(image, TexCoords).xyzw;
	vec3 result = vec3(0,0,0);

	for(int i=0; i< 5; i++)
		if(pointLights[i].position.x>0 && pointLights[i].position.y>0)
			result += calculatePointLight(pointLights[i], imgTex);
			//result += calculatePointLightLOPENGL(pointLights[i]);

	//result += imgTex.xyz + ambientColor;

	if(imgTex.w>0){
		color = vec4(result, imgTex.w);
	}else{
		color = vec4(result, imgTex.w);
	}
}
