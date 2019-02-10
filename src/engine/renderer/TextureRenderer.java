package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class TextureRenderer implements Renderer{
	public int numVertices;
	private Shader shader;
	private int quadVAO;
	private Mat4 skew = new Mat4();
	private Mat4 model = new Mat4();
	private Vec2 defaultZero2f = new Vec2(0,0);
	private Vec4 defaultSpriteFrame = new Vec4(0,0,1,1);
	private FloatBuffer floatBuffer;
	
	public TextureRenderer(Shader shader) {
		this.shader = shader;
		floatBuffer = BufferUtilities.createFloatBuffer(4*4);
		init();
	}
	
	private void init() {
		int VBO;

		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(generateLayers(3)), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES * 4, 0);
	
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	/**
	 * Generates a float array filled with a rectangle decomposed in layers of two triangles.
	 * 
	 * @param layers
	 * @return
	 */
	public static float[] generateLayers(int layers) {
		int p = 24;
		float vertices[] = new float[p*layers];
		float offset = 1/(float)layers;
		
		//Vec2 Pos	//Vec2 Texture
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
	
	/**
	 * Renders a pre-loaded texture using ResourceManger.
	 * 
	 * @param texName
	 * @param position
	 * @param size
	 * @param rotate
	 * @param color
	 */
	public void render(String texName, Vec2 position, Vec2 size, float rotate, Vec4 color) {
		render(ResourceManager.getSelf().getTexture(texName).getId(), -1, position, size, rotate, color, defaultSpriteFrame, defaultZero2f, defaultZero2f);
	}
	
	public void render(Texture texture, Vec2 position, Vec2 size, float rotate, Vec4 color) {
		render(texture.getId(), -1, position, size, rotate, color, defaultSpriteFrame, defaultZero2f, defaultZero2f);
	}

	public void render(Texture texture, Texture normal, Vec2 position, Vec2 size,
			float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation, Vec2 skewVec) {
		if(normal==null)
			render(texture.getId(), -1, position, size, rotate, color, spriteFrame, orientation, skewVec);
		else
			render(texture.getId(), normal.getId(), position, size, rotate, color, spriteFrame, orientation, skewVec);
	}
	
	public void render(int texId, Vec2 position, Vec2 size,
			float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation, Vec2 skewVec) {		
		render(texId, -1, position, size, rotate, color, spriteFrame, orientation, skewVec);
	}
	
	public void render(int texId, int normalId, Vec2 position, Vec2 size,
			float rotate, Vec4 color, Vec4 spriteFrame, Vec2 orientation, Vec2 skewVec) {		
		shader.use();
		
		float skewX = (float) Math.tan(Math.toRadians(skewVec.x));
		float skewY = (float) Math.tan(Math.toRadians(skewVec.y));
		if(skewX>10)
			skewX= 10;
		if(skewX<-10)
			skewX = -10;
		
		skew.m00 = 1; skew.m10 = skewX; skew.m20 = 0; skew.m30 = 0;
		skew.m01 = skewY; skew.m11 = 1; skew.m21 = 0; skew.m31 = 0;
		skew.m02 = 0; skew.m12 = 0; skew.m22 = 1; skew.m32 = 0;
		skew.m03 = 0; skew.m13 = 0; skew.m23 = 0; skew.m33 = 1; 
		
		model = model.identity();
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotate, 0, 0, 1); // Must be in radians
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.mul(skew);
		model = model.scale(size.x, size.y, 1);
		
		floatBuffer = BufferUtilities.fillFloatBuffer(floatBuffer, model);
		
		shader.setMat4("model", floatBuffer);
		shader.setVec4("spriteColor", color);
		shader.setVec2("flip", orientation);
		shader.setVec4("spriteFrame", spriteFrame);
		
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texId);
		
		if(normalId>=0) {
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, normalId);
		}else {
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, texId);
		}
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6*3);
		glBindVertexArray(0);
	}
}
