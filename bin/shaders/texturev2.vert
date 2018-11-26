#version 330 core
layout (location = 0) in vec4 vertex;
layout (location = 1) in vec4 spriteFrame;
layout (location = 2) in vec2 flip;
layout (location = 3) in mat4 model;

out vec2 TexCoords;
out vec3 FragPos;

uniform mat4 projection;
uniform mat4 camera;

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
