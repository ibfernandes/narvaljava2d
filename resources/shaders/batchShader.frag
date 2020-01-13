#version 330 core
#define MAX_POINT_LIGHTS 10
#define GRASS vec4(0.392, 0.447, 0.415 , 1)
#define TURKISH vec4(0.101, 0.737, 0.611 , 1)
#define TURKISH_DARKER vec4(0.086, 0.627, 0.627, 1)
#define SAND vec4(0.956, 0.917, 0.733 , 1)
#define WET_SAND vec4(0.956, 0.839, 0.654,1)
in vec2 TexCoords;
in vec3 FragPos;

out vec4 color;

struct PointLight {
	vec3 position;

	vec3 ambient;
	vec3 lightColor;

	float constant;
	float linear;
	float quadratic;
};

uniform sampler2D image;
uniform sampler2D normalTex;
uniform float dayTime;
uniform vec3 ambientColor;
uniform bool terrainMode;
uniform float waveDx;
uniform PointLight pointLights[MAX_POINT_LIGHTS];

float normalize(float xmin, float xmax, float x, float a, float b) {
	return (b-a) * (x - xmin)/(xmax - xmin) + a;
}

vec3 calculatePointLight(PointLight lightPoint, vec4 imgTex){
	vec3 normal = vec3(0,0,0);
	//normal = normalize(texture(normalTex, TexCoords).xyz);

	vec3 lightDir = vec3(0,0,2);
	//vec3 lightDir = normalize(lightPoint.position - FragPos);
	float diff = max(dot(normal, lightDir), 0.0);
	vec3 diffuse = diff * lightPoint.lightColor;
	diffuse = vec3 (1,1,1);

	float distance = length(lightPoint.position - vec3(FragPos.xyz));
	float attenuation = 1 / (lightPoint.constant + lightPoint.linear * distance + lightPoint.quadratic * (distance* distance));

	float finalAttenuation = max(attenuation,dayTime);

	return vec3(
		( (imgTex.xyz + ambientColor + lightPoint.lightColor * diffuse *attenuation*0.8) ) * finalAttenuation
		); //TODO: Apply diffuse
}

vec4 generateTerrain(vec4 perlinTex, float waveDx){
	vec4 imgTex;
	if (perlinTex.x > -.1) { // land
		imgTex = GRASS; 
	}
	if (perlinTex.x <= -0.1f) { // sand
		imgTex =  SAND;
	}

	if (perlinTex.x < -0.230f + waveDx * 0.016f) { // wet sand
		imgTex =  WET_SAND; 
	}

	if (perlinTex.x < -0.230f + waveDx * 0.016f) // espuma
		imgTex = vec4(1,1,1,1);

	if (perlinTex.x < -0.244f + waveDx * 0.016f) { // espuma back
		imgTex = TURKISH_DARKER;
	}

	if (perlinTex.x <= -0.266f + waveDx * 0.016f){ // water
		imgTex = TURKISH * normalize(-0.8f, -0.283f, perlinTex.x, 0.7f, 1.0f);
	}

	return imgTex;
}

void main(){

	vec4 imgTex;
	if(terrainMode)
		imgTex = generateTerrain(texture(image, TexCoords).xyzw, waveDx);
	else
		imgTex =  texture(image, TexCoords).xyzw;

	vec3 result = vec3(0,0,0);

	for(int i=0; i< MAX_POINT_LIGHTS; i++)
		if(pointLights[i].position.x>0 && pointLights[i].position.y>0)
			result += calculatePointLight(pointLights[i], imgTex);

	if(imgTex.w>0){
		color = vec4(result, imgTex.w);
	}else{
		color = vec4(result, imgTex.w);
	}
}
