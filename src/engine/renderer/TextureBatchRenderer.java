package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.FloatBuffer;
import engine.entity.component.RenderComponent;
import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.utilities.BufferUtilities;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class TextureBatchRenderer implements Renderer{

	private int layers;
	private Shader shader;

	//per instance
	private static final int INSTANCE_VBO_MAX_OBJECTS = 5000; // A maximum of 5.000 objects on the buffer at same time

	private static final int INSTANCE_DATA_STRIDE = 4 + 2 + (4 * 4); //vec4 spriteFrame, vec2 flip, mat4 model
	private static final int INSTANCE_DATA_STRIDE_IN_BYTES = INSTANCE_DATA_STRIDE*Float.BYTES;
	
	private int vertexVBO;
	private int instanceVBO;
	private int VAO;
	
	private FloatBuffer objectBuffer;
	private float vertices[];
	
	private int objectsCount = 0;
	
	
	public TextureBatchRenderer(Shader shader) {
		this.shader = shader;
		init();
	}
	
	private void init() {
		layers = 1;
		vertices = TextureRenderer.generateLayers(layers);
		objectBuffer = BufferUtilities.createFloatBuffer(INSTANCE_VBO_MAX_OBJECTS*INSTANCE_DATA_STRIDE);
		
		VAO = glGenVertexArrays();
		glBindVertexArray(VAO);
		
		vertexVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(vertices), GL_STATIC_DRAW);
		
		instanceVBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glBufferData(GL_ARRAY_BUFFER, INSTANCE_VBO_MAX_OBJECTS * INSTANCE_DATA_STRIDE_IN_BYTES, GL_STREAM_DRAW );
		
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
	
	
	public void start(Texture texture) {
		objectBuffer.clear();
		objectsCount = 0;
		
		glBindVertexArray(VAO);
		
		shader.use();
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		
		glActiveTexture(GL_TEXTURE1);
		texture.bind();
	}
	
	public void render(RenderComponent rc)  {	
		render(rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation());
	}
	
	public void render(Vec2 position, Vec2 size, float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation) {
		Mat4 model = new Mat4();
		
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f * size.y, 0);
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0);
		model = model.scale(size.x, size.y, 1);

		objectBuffer.put(spriteFrame.toFA_());
		objectBuffer.put(orientation.toFA_());
		objectBuffer.put(model.toFa_());
		objectsCount++;
	}
	
	public void end() {
		objectBuffer.flip();
		
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glBufferData(GL_ARRAY_BUFFER, INSTANCE_VBO_MAX_OBJECTS * INSTANCE_DATA_STRIDE_IN_BYTES, GL_STREAM_DRAW );
		glBufferSubData(GL_ARRAY_BUFFER, 0, objectBuffer);

		//Vertex Vec4 
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, vertexVBO);
		glVertexAttribPointer(
		 0, // attribute layout position in shader
		 4, // size
		 GL_FLOAT, // type
		 false, // normalized?
		 Vec4.SIZE, // stride
		 0 // array buffer offset
		);
		
		//Vec4 spriteFrame
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(1 , 4, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 0);
		
		//Vec2 flip
		glEnableVertexAttribArray(2);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(2 , 2, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 4 * Float.BYTES);
		
		//Mat4 model
		glEnableVertexAttribArray(3);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(3 , 4, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 6 * Float.BYTES);
		
		glEnableVertexAttribArray(4);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(4 , 4, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 10 * Float.BYTES);
		
		glEnableVertexAttribArray(5);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(5 , 4, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 14 * Float.BYTES);
		
		glEnableVertexAttribArray(6);
		glBindBuffer(GL_ARRAY_BUFFER, instanceVBO);
		glVertexAttribPointer(6 , 4, GL_FLOAT, false, INSTANCE_DATA_STRIDE_IN_BYTES, 18 * Float.BYTES);
		
		glVertexAttribDivisor(0, 0); 
		glVertexAttribDivisor(1, 1);
		glVertexAttribDivisor(2, 1);
		glVertexAttribDivisor(3, 1);
		glVertexAttribDivisor(4, 1);
		glVertexAttribDivisor(5, 1);
		glVertexAttribDivisor(6, 1);
		
		glDrawArraysInstanced(GL_TRIANGLES, 0, 6*layers, objectsCount);
		
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glDisableVertexAttribArray(3);
		glDisableVertexAttribArray(4);
		glDisableVertexAttribArray(5);
		glDisableVertexAttribArray(6);
		glBindVertexArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}
}
