package graphic;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.logic.GameObject;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class ShadowRenderer {
	private Shader shader;
	private int quadVAO;
	private GameObject shadow = new GameObject();
	private Vec4 color = new Vec4(0,0,0,1);
	
	public ShadowRenderer(Shader shader) {
		this.shader = shader;
		shadow = new GameObject();
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
		glBufferData(GL_ARRAY_BUFFER, BufferUtilities.createFloatBuffer(vertices), GL_STATIC_DRAW); 
		
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
	public void render(GameObject obj) {
		shader.use();
		shadow.setTexture(obj.getTexture());
		shadow.setRotation(obj.getRotation());
		shadow.setOrientation(obj.getOrientation());
		shadow.setAnimations(obj.getAnimations());
		shadow.setSize(obj.getSize());

		float angle = obj.getAngle(new Vec2(600,350));
		float distance = obj.getDistance(new Vec2(600,350));

		shadow.setSize(new Vec2(obj.getSize().x , obj.getSize().y * (distance/(1 + Math.pow(distance, 1.2)))));
		
		if(angle<=0) { // arco de cima
			shadow.setRotation((float) Math.toRadians(-180));
			shadow.setOrientation(new Vec2(obj.getOrientation().x,1));
			shadow.setSkew(new Vec2(
					-angle-90,
					0
					));
			shadow.setPosition(new Vec2(obj.getPosition().x, obj.getPosition().y + obj.getSize().y - shadow.getSize().y));		
			
		}else { //arco de baixo
			shadow.setRotation((float) Math.toRadians(0));
			shadow.setOrientation(new Vec2(obj.getOrientation().x,1));
			shadow.setSkew(new Vec2(
					-angle-90,
					0
					));
			shadow.setPosition(new Vec2(obj.getPosition().x, obj.getPosition().y + obj.getSize().y));		
		}
			
		Mat4 model = new Mat4();
		
		//TODO: Encapsulate this in a proper method
		float skewX = (float) Math.tan(Math.toRadians(shadow.getSkew().x));
		float skewY = (float) Math.tan(Math.toRadians(shadow.getSkew().y));
		if(skewX>10)
			skewX= 10;
		if(skewX<-10)
			skewX = -10;
		
		Mat4 skew = new Mat4();
		skew.m00 =1 ; skew.m10 = skewX; skew.m20 = 0; skew.m30 = 0;
		skew.m01 = skewY; skew.m11 = 1; skew.m21 = 0; skew.m31 = 0;
		skew.m02 = 0; skew.m12 = 0; skew.m22 = 1; skew.m32 = 0;
		skew.m03 = 0; skew.m13 = 0; skew.m23 = 0; skew.m33 = 1; 
		
		
		model = model.translate(shadow.getPosition().x, shadow.getPosition().y, 0);
		model = model.translate(0.5f * shadow.getSize().x, 0.5f *shadow.getSize().y, 0); //Move the origin of rotation to object's center
		model = model.rotate(shadow.getRotation(), 0, 0, 1); // Must be in radians
		model = model.translate(-0.5f * shadow.getSize().x, -0.5f *shadow.getSize().y, 0); //Move the origin of rotation back to it's top left
		model = model.mul(skew);
		model = model.scale(shadow.getSize().x, shadow.getSize().y, 1);

		shader.setMat4("model", model);
		shader.setVec4("spriteColor", color);
		shader.setVec2("flip", shadow.getOrientation());
		if(shadow.getAnimations()!=null)
			shader.setVec4("spriteFrame", shadow.getAnimations().getCurrentAnimation().getCurrentFrame());
		else
			shader.setVec4("spriteFrame", new Vec4(0,0,1,1));
		
		shader.setInteger("image", 0);
		shader.setInteger("normalTex", 1);
		
		glActiveTexture(GL_TEXTURE0);
		if(shadow.getAnimations()!=null)
			ResourceManager.getSelf().getTexture(shadow.getAnimations().getCurrentAnimation().getTexture()).bind();
		else
			ResourceManager.getSelf().getTexture(shadow.getTexture()).bind();
		
		glActiveTexture(GL_TEXTURE1);
		if(shadow.getAnimations()!=null)
			ResourceManager.getSelf().getTexture(shadow.getAnimations().getCurrentAnimation().getTexture()).bind();
		else
			ResourceManager.getSelf().getTexture(shadow.getTexture()).bind();
		
		
		glBindVertexArray(quadVAO);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);
		
	}
}
