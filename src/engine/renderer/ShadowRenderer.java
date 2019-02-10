package engine.renderer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.FloatBuffer;

import engine.entity.component.RenderComponent;
import engine.graphic.Shader;
import engine.utilities.BufferUtilities;
import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class ShadowRenderer implements Renderer{
	private Shader shader;
	private int quadVAO;
	private Vec4 color = new Vec4(0,0,0,1);
	private Vec4 spriteFrame = new Vec4(0,0,1,1);
	private FloatBuffer floatBuffer;
	private Mat4 skewMat = new Mat4();
	private Mat4 model = new Mat4();
	private Vec2 size = new Vec2();
	private Vec2 orientation = new Vec2();
	private Vec2 skew = new Vec2();
	private Vec2 position = new Vec2();
	
	public ShadowRenderer(Shader shader) {
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
	/**
	 * spriteFrame must be already normalized.
	 * @param texture
	 * @param position
	 * @param size
	 * @param rotate
	 * @param color
	 * @param spriteFrame
	 */
	public void render(RenderComponent rc, Vec2 lightPosition) {
		shader.use();
		if(rc.getTexture()==null && rc.getAnimations()==null)
			return;

		float angle = MathExt.calculateAngle(rc.getRenderPosition(), lightPosition);
		float distance = MathExt.calculateDistance(rc.getRenderPosition(), lightPosition);

		size.x = rc.getSize().x;
		size.y = (float) (rc.getSize().y * (distance/(1 + Math.pow(distance, 1.2))));
		
		float rotation;
		
		if(angle<=0) { // arco de cima
			rotation = ((float) Math.toRadians(-180));
			orientation.x = rc.getOrientation().x;
			orientation.y = 1;
			skew.x = -angle-90;
			skew.y =  0;
			position.x =rc.getRenderPosition().x;
			position.y =  rc.getRenderPosition().y + rc.getSize().y - size.y;		
		}else { //arco de baixo
			rotation = ((float) Math.toRadians(0));
			orientation.x = rc.getOrientation().x;
			orientation.y = 1;
			skew.x = -angle-90;
			skew.y =  0;
			position.x = rc.getRenderPosition().x;
			position.y = rc.getRenderPosition().y + rc.getSize().y;		
		}
			
		float skewX = (float) Math.tan(Math.toRadians(skew.x));
		float skewY = (float) Math.tan(Math.toRadians(skew.y));
		if(skewX>10)
			skewX= 10;
		if(skewX<-10)
			skewX = -10;
		
		
		skewMat.m00 =1 ; skewMat.m10 = skewX; skewMat.m20 = 0; skewMat.m30 = 0;
		skewMat.m01 = skewY; skewMat.m11 = 1; skewMat.m21 = 0; skewMat.m31 = 0;
		skewMat.m02 = 0; skewMat.m12 = 0; skewMat.m22 = 1; skewMat.m32 = 0;
		skewMat.m03 = 0; skewMat.m13 = 0; skewMat.m23 = 0; skewMat.m33 = 1; 
		
		
		model = model.identity();
		model = model.translate(position.x, position.y, 0);
		model = model.translate(0.5f * size.x, 0.5f *size.y, 0); //Move the origin of rotation to object's center
		model = model.rotate(rotation, 0, 0, 1); // Must be in radians
		model = model.translate(-0.5f * size.x, -0.5f *size.y, 0); //Move the origin of rotation back to it's top left
		model = model.mul(skewMat);
		model = model.scale(size.x, size.y, 1);
		
		floatBuffer = BufferUtilities.fillFloatBuffer(floatBuffer, model);
		
		shader.setMat4("model", floatBuffer);
		shader.setVec4("spriteColor", color);
		shader.setVec2("flip", orientation);
		
		if(rc.getAnimations()!=null)
			shader.setVec4("spriteFrame", rc.getAnimations().getCurrentAnimation().getCurrentFrame());
		else
			shader.setVec4("spriteFrame", spriteFrame);
		
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		if(rc.getAnimations()!=null)
			ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()).bind();
		else
			ResourceManager.getSelf().getTexture(rc.getTexture()).bind();
		
		glActiveTexture(GL_TEXTURE1);
		if(rc.getAnimations()!=null)
			ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()).bind();
		else
			ResourceManager.getSelf().getTexture(rc.getTexture()).bind();
		
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
		
	}
}
