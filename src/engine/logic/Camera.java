package engine.logic;

import java.nio.FloatBuffer;

import engine.engine.Engine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.RenderComponent;
import engine.renderer.CubeRenderer;
import engine.utilities.BufferUtilities;
import engine.utilities.Commons;
import engine.utilities.ResourceManager;
import engine.utilities.Timer;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Camera {
	private float x = 0, y = 0;
	private Entity focus;
	private Mat4 camera;
	private EntityManager em;
	private Mat4 transform = new Mat4();
	private float delta = 1;
	private Timer timer = new Timer(300);
	private float offset = 50;
	private Vec2 direction = new Vec2(0, 0);
	private FloatBuffer floatBuffer;

	public Camera(EntityManager em) {
		this.em = em;
		camera = new Mat4();
		camera = camera.identity();
		floatBuffer = BufferUtilities.createFloatBuffer(4*4);
	}

	/**
	 * Sets this entity as the camera's center.
	 * 
	 * @param entity
	 */
	public void setFocusOn(Entity entity) {
		focus = entity;
	}

	/**
	 * Moves the camera directly to this point.
	 * 
	 * @param x
	 * @param y
	 */
	public void moveDirectTo(float x, float y) {
		this.x = x;
		this.y = y;
		camera = camera.identity();
		camera.translate(x, y, 0);
	}

	/**
	 * Moves camera axis by x and y values.
	 * 
	 * @param x
	 * @param y
	 */
	public void move(float x, float y) {
		this.x += x;
		this.y += y;
		camera = camera.identity();
		camera.translate(this.x, this.y, 0);
	}

	/**
	 * Detects if the object moved more than at least 1 pixel.
	 * 
	 * @param pos
	 * @param pos2
	 * @return
	 */
	private boolean detectIfObjectOfFocusMoved(Vec2 pos, Vec2 pos2) {
		float xVar = Math.abs(pos.x - pos2.x);
		float yVar = Math.abs(pos.y - pos2.y);
		if (xVar > 0.1f || yVar > 0.1f)
			return true;
		else
			return false;
	}

	public void update(float deltaTime) {
	}

	public void variableUpdate(float alpha) {
		RenderComponent rc = ((RenderComponent) (em.getFirstComponent(focus, RenderComponent.class)));
		BasicComponent bc = ((BasicComponent) (em.getFirstComponent(focus, BasicComponent.class)));
		Vec2 position = rc.getRenderPosition();

		if (detectIfObjectOfFocusMoved(bc.getPosition(), bc.getPreviousPosition())) {
			if (timer.isReversed())
				timer.reverse();
		} else {
			if (!timer.isReversed())
				timer.reverse();
		}
		delta = timer.getElapsedDelta();

		direction = Commons.calculateDirection(position.x, position.y, -x + Engine.getSelf().getWindow().getWidth() / 2,
				-y + Engine.getSelf().getWindow().getHeight() / 2);

		if (focus != null)
			moveDirectTo(direction.x * offset * delta - position.x + Engine.getSelf().getWindow().getWidth() / 2,
					direction.y * offset * delta - position.y + Engine.getSelf().getWindow().getHeight() / 2);

		transform = transform.identity();
		transform.translate(this.x, this.y, 0);
		floatBuffer = BufferUtilities.fillFloatBuffer(floatBuffer, transform);

		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setMat4("camera", floatBuffer);

		ResourceManager.getSelf().getShader("texturev2").use();
		ResourceManager.getSelf().getShader("texturev2").setMat4("camera", floatBuffer);

		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("camera", floatBuffer);

		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("camera", floatBuffer);

		ResourceManager.getSelf().getShader("cube").use();
		ResourceManager.getSelf().getShader("cube").setMat4("camera", floatBuffer);
		
		ResourceManager.getSelf().getShader("ui").use();
		ResourceManager.getSelf().getShader("ui").setMat4("camera", floatBuffer);
	}

	public void render() {
//		renderDebug();
	}

	public void renderDebug() {
		RenderComponent rc = ((RenderComponent) (em.getFirstComponent(focus, RenderComponent.class)));
		Vec2 position = rc.getRenderPosition();

		// Focus anchor
		((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(position, new Vec2(10, 10), 0,
				new Vec4(1, 0, 0, 1));

		// Camera anchor
		((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(getCenter(), new Vec2(8, 8), 0,
				new Vec4(1, 1, 1, 1));

		// Delta offset
		ResourceManager.getSelf().getFont("sourcesanspro").render("D" + String.valueOf(delta) + "\n", getCenterX() + 12,
				getCenterY(), new Vec4(1, 1, 1, 1));
	}

	public Vec2 getCenter() {
		return new Vec2(getX() + Engine.getSelf().getWindow().getWidth() / 2,
				getY() + Engine.getSelf().getWindow().getHeight() / 2);
	}

	public float getCenterX() {
		return getX() + Engine.getSelf().getWindow().getWidth() / 2;
	}

	public float getCenterY() {
		return getY() + Engine.getSelf().getWindow().getHeight() / 2;
	}

	public float getX() {
		return x * -1;
	}

	public float getY() {
		return y * -1;
	}

	public Vec2 getPos() {
		return new Vec2(x * -1, y * -1);
	}
}
