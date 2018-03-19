package engine.logic;

import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._3.Vec3;

public class Camera {
	private float x=0,y=0,z=0;
	private GameObject focus;
	private Mat4 camera;
	
	public Camera() {
		camera = new Mat4();
		camera = camera.identity();
	}
	
	public void setFocusOn(GameObject obj) {
		focus = obj;
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
		if(focus !=null)
			move(focus.getPreviousPosition().x - focus.getX(), focus.getPreviousPosition().y - focus.getY());
		
		ResourceManager.getSelf().getShader("animation").use();
		ResourceManager.getSelf().getShader("animation").setMat4("camera", camera);
		
		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("camera", camera);
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("camera", camera);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
