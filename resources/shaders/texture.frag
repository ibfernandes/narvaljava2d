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

uniform float waveDx;
uniform bool terrainMode;

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

#define GRASS vec4(0.392, 0.447, 0.415 , 1)
#define TURKISH vec4(0.101, 0.737, 0.611 , 1)
#define TURKISH_DARKER vec4(0.086, 0.627, 0.627, 1)
#define SAND vec4(0.956, 0.917, 0.733 , 1)
#define WET_SAND vec4(0.956, 0.839, 0.654,1)

float normalize(float xmin, float xmax, float x, float a, float b) {
	return (b-a) * (x - xmin)/(xmax - xmin) + a;
}

void main(){

	vec4 imgTex;
	if(terrainMode){
		vec4 perlinTex =  texture(image, TexCoords).xyzw;

		if (perlinTex.x > -.1) { // land
			imgTex = GRASS; 
		}
		if (perlinTex.x <= -0.1f) { // sand
			imgTex =  SAND;
		//	if (whiteNoise[x][y] > 0)
			//	mapRGB[x][y] = (255 << 24) | (234 << 16) | (224 << 8) | (167); // ARGB
		}

		if (perlinTex.x < -0.230f + waveDx * 0.016f) { // wet sand
			imgTex =  WET_SAND; 
		//	if (whiteNoise[x][y] > 0)
		//		mapRGB[x][y] = (255 << 24) | (234 << 16) | (224 << 8) | (167); // ARGB
		}

		if (perlinTex.x < -0.230f + waveDx * 0.016f) // espuma
			imgTex = vec4(1,1,1,1);

		if (perlinTex.x < -0.244f + waveDx * 0.016f) { // espuma back
			imgTex = TURKISH_DARKER;
			//if (perlinNoise[x][y] > -.2445 + waterDx * waveVariation) {
			//	if (whiteNoise[x][y] < 0.2f)
			//		mapRGB[x][y] = Color.WHITE;
			//}
		}

		if (perlinTex.x <= -0.266f + waveDx * 0.016f){ // water
			imgTex = TURKISH * normalize(-0.8f, -0.283f, perlinTex.x, 0.7f, 1.0f);
		}
	}else{
		imgTex =  texture(image, TexCoords).xyzw;
	}

	vec3 result = vec3(0,0,0);

	for(int i=0; i< 5; i++)
		if(pointLights[i].position.x>0 && pointLights[i].position.y>0)
			result += calculatePointLight(pointLights[i], imgTex);
			//result += calculatePointLightLOPENGL(pointLights[i]);

	//result += imgTex.xyz + ambientColor;

	if(imgTex.w>0){
		color = vec4(result, spriteColor.w);
	}else{
		color = vec4(result, imgTex.w);
	}
}
