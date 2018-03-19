package graphic;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.Random;

import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.utilities.BufferUtilities;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class TextureRenderer {
	private Shader shader;
	private int quadVAO;
	
	public TextureRenderer(Shader shader) {
		this.shader = shader;
		init();
	}
	
	private void init() {
		int VBO;
		float vertices [] = {
				//Pos	//Texture
				0,	1,	0,	1f,
				1,	0,	1f,	0,
				0,	0,	0,	0,
				
				0,	1,	0,	1f,
				1,	1,	1f,	1f,
				1,	0,	1f,	0
		};
		
		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(getVertices(3)), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES *4, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}

	public float[] getVertices(int layers) {
		int p = 24;
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
	
	
	
	public void render(Texture texture, Vec2 position, Vec2 size, float rotate, Vec4 color) {
		shader.use();
		Mat4 model = new Mat4();
		
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.scale(size.x, size.y, 1);
	

		shader.setMat4("model", model);
		shader.setVec4("spriteColor", color);
		
		shader.setInteger("image", 0);
		
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		glActiveTexture(GL_TEXTURE1);//TODO: this is not correct (binding the same twice)
		texture.bind();
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
	}
	
	//TODO: cascate render
	public void render(int texId, Vec2 position, Vec2 size, float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation, Vec2 skewVec) {
		shader.use();
		Mat4 model = new Mat4();//TODO: instantiate only once when 
		
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1);
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.scale(size.x, size.y, 1);
	

		shader.setMat4("model", model);
		shader.setVec4("spriteColor", color);
		shader.setVec2("flip", orientation);
		shader.setVec4("spriteFrame", spriteFrame);
		
		shader.setInteger("image", 0);
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texId);
		
		glActiveTexture(GL_TEXTURE1);
		glBindTexture(GL_TEXTURE_2D, texId);
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6*3);
		glBindVertexArray(0);
	}
	
	/**
	 * spriteFrame must be already normalized.
	 * @param texture
	 * @param position
	 * @param size
	 * @param rotate
	 * @param color
	 * @param spriteFrame
	 */
	public void render(Texture texture, Texture normal, Vec2 position, Vec2 size, float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation, Vec2 skewVec) {
		shader.use();
		Mat4 model = new Mat4();
		
		//TODO: Encapsulate this in a proper method
		float skewX = (float) Math.tan(Math.toRadians(skewVec.x)); //sys
		float skewY = (float) Math.tan(Math.toRadians(skewVec.y));
		if(skewX>10)
			skewX= 10;
		if(skewX<-10)
			skewX = -10;
		
		Mat4 skew = new Mat4();
		skew.m00 =1 ; skew.m10 = skewX; skew.m20 = 0; skew.m30 = 0;
		skew.m01 = skewY; skew.m11 = 1; skew.m21 = 0; skew.m31 = 0;
		skew.m02 = 0; skew.m12 = 0; skew.m22 = 1; skew.m32 = 0;
		skew.m03 = 0; skew.m13 = 0; skew.m23 = 0; skew.m33 = 1; 
		
		
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1); // Must be in radians
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.mul(skew);
		model = model.scale(size.x, size.y, 1);

		
		

		
		
		shader.setMat4("model", model);
		shader.setVec4("spriteColor", color);
		shader.setVec2("flip", orientation);
		shader.setVec4("spriteFrame", spriteFrame);
		
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		texture.bind();
		
		glActiveTexture(GL_TEXTURE1);
		texture.bind(); // TODO: should be normal.bind()
		
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6*3);
		glBindVertexArray(0);
		
	}
}
