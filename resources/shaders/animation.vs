#version 330 core
layout (location = 0) in vec4 vertex; //xy position
									 //zw Tex coord

out vec2 TexCoords;
out vec3 FragPos;

uniform mat4 model;
uniform mat4 projection;
uniform mat4 camera;
uniform vec4 spriteFrame; 	//xy 	top left point of the spriteFrame
														//zw	width and height
uniform vec2 flip;

void main(){
	vec2 tCoords;

	if(flip.x==1)
		tCoords = vec2(1 - vertex.z,  vertex.w);
	if(flip.y==1)
		tCoords = vec2(vertex.z,  1 - vertex.w);
	if(flip.x==1 && flip.y==1)
		tCoords = vec2(1 - vertex.z,  1 - vertex.w);
	if(flip.x==0 && flip.y ==0)
		tCoords = vec2(vertex.z, vertex.w);

	tCoords *= spriteFrame.zw;
	tCoords += spriteFrame.xy;

	TexCoords = tCoords;
	gl_Position = projection * camera * model *  vec4(vertex.xy, 0.0 , 1.0);
	FragPos = vec3(model * vec4(vertex.xy , 0.0, 1.0));
}
