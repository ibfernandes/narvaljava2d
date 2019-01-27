#version 330 core

out vec4 color;

uniform vec4 cubeColor;

void main(){
	color = vec4(cubeColor);
}
