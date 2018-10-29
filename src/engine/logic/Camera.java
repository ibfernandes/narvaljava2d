package engine.logic;

import engine.engine.Engine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.RenderComponent;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class Camera {
	private float x=0,y=0,z=0;
	private Entity focus;
	private Mat4 camera;
	private EntityManager em;
	
	public Camera(EntityManager em) {
		this.em = em;
		camera = new Mat4();
		camera = camera.identity();
	}
	
	public void setFocusOn(Entity entity) {
		focus = entity;
	}
	
	public void moveDirectTo(float x, float y) {
		this.x = x;
		this.y = y;
		camera = camera.identity();
		//camera.scale(0.5f, .5f,0); //TODO: should scale shadow texture as well
		camera.translate(x, y,0);
	}
	
	public void move(float x, float y) {
		this.x += x;
		this.y += y;
		camera = camera.identity();
		//camera.scale(0.5f, .5f,0); //TODO: should scale shadow texture as well
		camera.translate(this.x, this.y,0);
		
	}
	
	public void update(float deltaTime) {
	}
	
	Mat4 transform  = new Mat4();
	
	public void variableUpdate(float alpha) {
		RenderComponent rc = ((RenderComponent)(em.getFirstComponent(focus, RenderComponent.class)));
		Vec2 position = rc.getRenderPosition();
		Vec2 size = rc.getSize();
		
		if(focus != null)
			moveDirectTo(-position.x + 1280/2 - size.x/2, -position.y +720/2 - size.y/2); //TODO: should use window.width and height, also should use renderX and renderY?

		transform = transform.identity();
		transform.translate(this.x,this.y,0);
		
		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setMat4("camera", transform);
		
		ResourceManager.getSelf().getShader("texturev2").use();
		ResourceManager.getSelf().getShader("texturev2").setMat4("camera", transform);
		
		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("camera", transform);
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("camera", transform);
		
		ResourceManager.getSelf().getShader("cube").use();
		ResourceManager.getSelf().getShader("cube").setMat4("camera", transform);
	}

	public float getX() {
		return x*-1;
	}

	public float getY() {
		return y*-1;
	}
	
	public Vec2 getPos() {
		return new Vec2(x*-1,y*-1);
	}
}
