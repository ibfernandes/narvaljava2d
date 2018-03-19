package graphic;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.util.Random;

import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.logic.GameObject;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class GrassRenderer {
	private Shader shader;
	private int quadVAO;
	private int numOfLayers = 8;
	
	public GrassRenderer(Shader shader) {
		this.shader = shader;
		init();
	}
	
	private void init() {
		int VBO;
		
		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(getVertices(numOfLayers)), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES *5, 0);
		
		
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1 , 1, GL_FLOAT, false, Float.BYTES *5,  Float.BYTES *4);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	/**
	 * Divides the vertices in "layers" composing the final rectangle. Each layer has 2 triangles composed of 6 points each (12 total).
	 * E.g:
	 *  ___
	 * |_\_| layer 1
	 * |_\_| layer 2...
	 * @param layers
	 * @return
	 */
	public float[] getVertices(int layers) {
		int p = 12 + 12 + 6; //total de coordenadas por camada, 12 dos dois triangulos + 12 das texturas + 6 dos pesos = stride
		float vertices[] = new float[p*layers];
		float offset = 1/(float)layers;
		
		float weight[] = {
				1f/30f,			//top
				1f/32f,
				1f/34f,
				1f/36f,
				1f/38f,
				1f/40f,
				1f/42f,
				1f/44f			//base
			};
		
		//for each row,
		// vertex		texCoord	weight
		//	x	y		x	y		w
		for(int i=0; i<numOfLayers; i++){
			vertices[i*p +  0] = 0; vertices[i*p +  1] = offset + i*offset; 	vertices[i*p + 2] = 0; vertices[i*p + 3] = offset + i*offset; 	vertices[i*p +  4] = (i==0) ? 0 : weight[i-1]/2;
			vertices[i*p +  5] = 1; vertices[i*p +  6] = i*offset; 				vertices[i*p + 7] = 1; vertices[i*p + 8] = i*offset;			vertices[i*p +  9] = weight[i];
			vertices[i*p + 10] = 0; vertices[i*p + 11] = i*offset; 				vertices[i*p +12] = 0; vertices[i*p +13] = i*offset;			vertices[i*p + 14] = weight[i];			
			
			vertices[i*p +15] = 0; vertices[i*p +16] = offset + i*offset; 		vertices[i*p +17] = 0; vertices[i*p +18] = offset + i*offset;	vertices[i*p + 19] = (i==0) ? 0 : weight[i-1]/2;
			vertices[i*p +20] = 1; vertices[i*p +21] = offset + i*offset; 		vertices[i*p +22] = 1; vertices[i*p +23] = offset + i*offset;	vertices[i*p + 24] = (i==0) ? 0 : weight[i-1]/2;
			vertices[i*p +25] = 1; vertices[i*p +26] = i*offset; 				vertices[i*p +27] = 1; vertices[i*p +28] = i*offset;			vertices[i*p + 29] = weight[i];
			
		}
	
		return vertices;
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
	public void render(GameObject o) {
		shader.use();
		Mat4 model = new Mat4();
		
		//TODO: Encapsulate this in a proper method
		float skewX = (float) Math.tan(Math.toRadians(o.getSkew().x)); //sys
		float skewY = (float) Math.tan(Math.toRadians(o.getSkew().y));
		if(skewX>10)
			skewX= 10;
		if(skewX<-10)
			skewX = -10;
		
		Mat4 skew = new Mat4();
		skew.m00 =1 ; skew.m10 = skewX; skew.m20 = 0; skew.m30 = 0;
		skew.m01 = skewY; skew.m11 = 1; skew.m21 = 0; skew.m31 = 0;
		skew.m02 = 0; skew.m12 = 0; skew.m22 = 1; skew.m32 = 0;
		skew.m03 = 0; skew.m13 = 0; skew.m23 = 0; skew.m33 = 1; 
		
		
		model = model.translate(o.getPosition().x, o.getPosition().y, 0);
		model = model.translate(0.5f * o.getSize().x, 0.5f *o.getSize().y, 0); //Move the origin of rotation to object's center
		model = model.rotate(o.getRotation(), 0, 0, 1); // Must be in radians
		model = model.translate(-0.5f * o.getSize().x, -0.5f *o.getSize().y, 0); //Move the origin of rotation back to it's top left
		model = model.mul(skew);
		model = model.scale(o.getSize().x, o.getSize().y, 1);

		
		

		shader.setMat4("model", model);
		shader.setVec4("spriteColor", o.getColor());
		shader.setVec2("flip", o.getOrientation());
		shader.setVec4("spriteFrame", o.getAnimations().getCurrentAnimation().getCurrentFrame());
		
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		ResourceManager.getSelf().getTexture(o.getAnimations().getCurrentAnimation().getTexture()).bind();
		
		glActiveTexture(GL_TEXTURE1);
		ResourceManager.getSelf().getTexture(o.getAnimations().getCurrentAnimation().getTexture()).bind(); // TODO: should be normal.bind()
		
		
		//glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6*numOfLayers);

		glBindVertexArray(0);
		//glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
		
	}
}
