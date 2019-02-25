#version 330 core
layout (location = 0) in vec4 vertex;
layout (location = 1) in float weight;
layout (location = 2) in vec4 spriteFrame;
layout (location = 3) in vec2 flip;
layout (location = 4) in mat4 model;
layout (location = 8) in float affectedByTheWind;

out vec2 TexCoords;
out vec3 FragPos;

uniform mat4 projection;
uniform mat4 camera;
uniform float windForce;

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
	if(affectedByTheWind==1)
		gl_Position = projection * camera * model *  vec4(vec2(vertex.x + weight * windForce, vertex.y), 0.0 , 1.0);
	else
		gl_Position = projection * camera * model *  vec4(vertex.xy, 0.0 , 1.0);
	FragPos = vec3(model * vec4(vertex.xy , 0.0, 1.0));
}
