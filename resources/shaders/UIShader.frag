#version 330 core
in vec2 TexCoords;
in vec3 FragPos;
in vec4 componentColor;

out vec4 color;

uniform sampler2D image;

void main(){

	vec4 imgTex;
	imgTex =  texture(image, TexCoords).xyzw;
	imgTex = componentColor;
	color = imgTex;
}
