#version 330 core
layout (location = 0) in vec4 vertex;

out vec2 TexCoords;

uniform mat4 model;
uniform mat4 projection;
uniform vec4 spriteFrame; 	//xy 	top left point of the spriteFrame
							//zw	width and height
uniform vec2 flip;

void main(){

	float xSkew = 0.6;
	float ySkew = 0.0;

   // Create a transform that will skew our texture coords
   mat2 trans = mat2(
	  1.0       , tan(xSkew),
	  tan(ySkew), 1.0
   );

	vec2 tCoords = spriteFrame.xy + (vertex.zw * spriteFrame.zw * flip.xy) ;
	vec2 aux = (tCoords.xy * trans).xy;


	TexCoords = tCoords;
	gl_Position = projection  * model *  vec4(vertex.xy, 0.0 , 1.0);
}
