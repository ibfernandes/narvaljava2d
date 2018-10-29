package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.opengl.GL31.*;
import static org.lwjgl.opengl.GL33.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import org.lwjgl.system.MemoryUtil;

import engine.entity.component.RenderComponent;
import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class TextureRendererV2 implements Renderer{

	private int layers;
	public int numVertices;
	private Shader shader;

	//per instance
	private static final int INSTANCE_VBO_MAX_OBJECTS = 5000; // A maximum of 5.000 objects on the buffer at same time
	
	//vec4 spriteFrame, vec2 flip, mat4 model
	//Length (also Stride) in floats. When passing as arg it must be in BYTES.
	private static final int INSTANCE_DATA_STRIDE = 4 + 2 + (4 * 4);
	private static final int INSTANCE_DATA_STRIDE_IN_BYTES = (4 + 2 + (4 * 4))*Float.BYTES;
	
	int vertexVBO;
	int instanceVBO;
	int VAO;
	
	FloatBuffer objectBuffer;
	float vertices[];
	
	int objectsCount = 0;
	
	
	public TextureRendererV2(Shader shader) {
		this.shader = shader;
		init();
	}
	
	private void init() {
		layers = 1;
		vertices = getVertices(layers);
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
	
	public float[] getVertices(int layers) {
		int p = 24; // 4 floats per vetex (obj.x, obj.y, tex.x, tex.y) * 6 vertices = 24 floats
		float vertices[] = new float[p*layers];
		float offset = 1/(float)layers;
		
		for(int i=layers-1; i>=0; i--) {
			vertices[i*p + 0] = 0; vertices[i*p + 1] = offset + i*offset; 	vertices[i*p + 2] = 0; vertices[i*p + 3] = offset + i*offset;
			vertices[i*p + 4] = 1; vertices[i*p + 5] = i*offset; 			vertices[i*p + 6] = 1; vertices[i*p + 7] = i*offset;
			vertices[i*p + 8] = 0; vertices[i*p + 9] = i*offset; 			vertices[i*p +10] = 0; vertices[i*p +11] = i*offset;
			
			vertices[i*p +12] = 0; vertices[i*p +13] = offset + i*offset; 	vertices[i*p +14] = 0; vertices[i*p +15] = offset + i*offset;
			vertices[i*p +16] = 1; vertices[i*p +17] = offset + i*offset; 	vertices[i*p +18] = 1; vertices[i*p +19] = offset + i*offset;
			vertices[i*p +20] = 1; vertices[i*p +21] = i*offset; 			vertices[i*p +22] = 1; vertices[i*p +23] = i*offset;
		}
		
		return vertices;
	}
	
	
	public void start() {
		objectBuffer.clear();
		objectsCount = 0;
		
		glBindVertexArray(VAO);
		
		shader.use();
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		ResourceManager.getSelf().getTexture("rogue").bind();
		
		glActiveTexture(GL_TEXTURE1);
		ResourceManager.getSelf().getTexture("rogue").bind();
	}
	
	public void render(RenderComponent rc)  {
		Mat4 model = new Mat4();
		
		model = model.translate(rc.getRenderPosition().x, rc.getRenderPosition().y, 0);
		model = model.translate(0.5f * rc.getSize().x, 0.5f * rc.getSize().y, 0);
		model = model.rotate(rc.getRotation(), 0, 0, 1);
		model = model.translate(-0.5f * rc.getSize().x, -0.5f *rc.getSize().y, 0);
		model = model.scale(rc.getSize().x, rc.getSize().y, 1);

		objectBuffer.put(rc.getAnimations().getCurrentAnimation().getCurrentFrame().toFA_());
		objectBuffer.put(rc.getOrientation().toFA_());
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
