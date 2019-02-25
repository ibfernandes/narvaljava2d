package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;
import java.util.Arrays;
import engine.entity.component.RenderComponent;
import engine.graphic.Shader;
import engine.utilities.ArraysExt;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class GrassRenderer implements Renderer{
	private Shader shader;
	private int quadVAO;
	public final static int numOfLayers = 8;
	private Mat4 skew = new Mat4();
	private Mat4 model = new Mat4();
	private FloatBuffer floatBuffer;

	
	public GrassRenderer(Shader shader) {
		this.shader = shader;
		floatBuffer = BufferUtilities.createFloatBuffer(4*4);
		init();
	}
	
	private void init() {
		int VBO;
		
		quadVAO = glGenVertexArrays();
		VBO = glGenBuffers();
		
		glBindBuffer(GL_ARRAY_BUFFER, VBO);
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(generateLayers(numOfLayers)), GL_STATIC_DRAW); 
		
		glBindVertexArray(quadVAO);
		
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0 , 4, GL_FLOAT, false, Float.BYTES *5, 0);
		
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1 , 1, GL_FLOAT, false, Float.BYTES *5,  Float.BYTES *4);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
	}
	
	/**
	 * Divides the vertices in "layers" composing the final rectangle. Each layer has 2 triangles composed of 6 points each (12 points per layer).
	 * E.g:
	 *  ___
	 * |_\_| layer 1
	 * |_\_| layer 2...
	 * 
	 * @param layers
	 * @return
	 */
	public static float[] generateLayers(int layers) {
		int p = 12 + 12 + 6; //total de coordenadas por camada, 12 dos dois triangulos + 12 das texturas + 6 dos pesos = stride
		float vertices[] = new float[p*layers];
		float offset = 1/(float)layers;

		float weight[] = { //ascending order
				1f/44f,	//base
				1f/42f,
				1f/40f,
				1f/26f,
				1f/20f,
				1f/16f,
				1f/14f,
				1f/8f	//topo
		};
		
		float baseWeight[] = new float[layers];
		float upperWeight[] = new float[layers];
		
		Arrays.fill(baseWeight, 0);
		Arrays.fill(upperWeight, 0);
		
		for(int i=0; i <layers; i++) {
			if(i==0)
				baseWeight[i] = 0;
			else
				baseWeight[i] = upperWeight[i-1];
			
			if(i==0)
				upperWeight[i] = weight[i];
			else
				upperWeight[i] = weight[i] + baseWeight[i]; 
		}

		ArraysExt.reverse(baseWeight);
		ArraysExt.reverse(upperWeight);
		
		//for each row: Vec2 vertex, Vec2 texCoord, float weight
		for(int i=0; i<layers; i++){
			vertices[i*p +  0] = 0; vertices[i*p +  1] = offset + i*offset; 	vertices[i*p + 2] = 0; vertices[i*p + 3] = offset + i*offset; 	vertices[i*p +  4] = baseWeight[i];
			vertices[i*p +  5] = 1; vertices[i*p +  6] = i*offset; 				vertices[i*p + 7] = 1; vertices[i*p + 8] = i*offset;			vertices[i*p +  9] = upperWeight[i]; 	//upper vertice
			vertices[i*p + 10] = 0; vertices[i*p + 11] = i*offset; 				vertices[i*p +12] = 0; vertices[i*p +13] = i*offset;			vertices[i*p + 14] = upperWeight[i];	//upper vertice
			
			vertices[i*p +15] = 0; vertices[i*p +16] = offset + i*offset; 		vertices[i*p +17] = 0; vertices[i*p +18] = offset + i*offset;	vertices[i*p + 19] = baseWeight[i];
			vertices[i*p +20] = 1; vertices[i*p +21] = offset + i*offset; 		vertices[i*p +22] = 1; vertices[i*p +23] = offset + i*offset;	vertices[i*p + 24] = baseWeight[i];                                           
			vertices[i*p +25] = 1; vertices[i*p +26] = i*offset; 				vertices[i*p +27] = 1; vertices[i*p +28] = i*offset;			vertices[i*p + 29] = upperWeight[i];	//upper vertice
		}
	
		return vertices;
	}
	
	public void render(RenderComponent rc) {
		render(ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()).getId(), -1, rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(),
				rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
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
		glDrawArrays(GL_TRIANGLES, 0, 6*numOfLayers);

		glBindVertexArray(0);
	}
}
