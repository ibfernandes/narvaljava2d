package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import engine.geometry.Rectangle;
import engine.graphic.Shader;
import engine.utilities.BufferUtilities;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class CubeRenderer implements Renderer{
	private Shader shader;
	private int quadVAO;
	
	public CubeRenderer(Shader shader) {
		this.shader = shader;
		init();
	}
	
	private void init() {
		int VBO;
		float vertices [] = {
				//Pos	//Texture
				0,	1,	0,	1,
				1,	0,	1,	0,
				0,	0,	0,	0,
				
				0,	1,	0,	1,
				1,	1,	1,	1,
				1,	0,	1,	0
		};
		
		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(vertices), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES *4, 0);
		//glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	
	public void render(Vec2 position, Vec2 size, float rotate, Vec3 color) {
		
		shader.use();
		Mat4 model = new Mat4();
		
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.scale(size.x, size.y, 1);

		shader.setMat4("model", model);
		shader.setVec3("cubeColor", color);
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
	
	public void render(Rectangle r, float rotate, Vec3 color) {
		
		shader.use();
		Mat4 model = new Mat4();
		
		model = model.translate(r.x, r.y, 0);
		model = model.translate(0.5f * r.width, 0.5f *r.height, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * r.width, -0.5f *r.height, 0); //Move the origin of rotation back to it's top left
		model = model.scale(r.width, r.height, 1);

		shader.setMat4("model", model);
		shader.setVec3("cubeColor", color);
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
}
