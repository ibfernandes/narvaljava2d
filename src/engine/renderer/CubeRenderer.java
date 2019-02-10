package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import engine.entity.component.RenderComponent;
import engine.geometry.Rectangle;
import engine.graphic.Shader;
import engine.utilities.BufferUtilities;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class CubeRenderer implements Renderer{
	private Shader shader;
	private int quadVAO;
	private FloatBuffer floatBuffer;
	private Mat4 model = new Mat4();
	
	public CubeRenderer(Shader shader) {
		this.shader = shader;
		floatBuffer = BufferUtilities.createFloatBuffer(4*4);
		init();
	}
	
	private void init() {
		int VBO;
		
		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(TextureRenderer.generateLayers(1)), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES *4, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	public void render(Rectangle r, float rotate, Vec4 color) {
		render(r.getPos(), r.getSize(), rotate, color);
	}
	
	public void render(RenderComponent r) {
		render(r.getRenderPosition(), r.getSize(), r.getRotation(), r.getColor());
	}
	
	public void render(Vec2 position, Vec2 size, float rotate, Vec4 color) {
		shader.use();
		
		model = model.identity();
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0);
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0);
		model = model.scale(size.x, size.y, 1);
		
		floatBuffer = BufferUtilities.fillFloatBuffer(floatBuffer, model);

		shader.setMat4("model", floatBuffer);
		shader.setVec4("cubeColor", color);
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
}
