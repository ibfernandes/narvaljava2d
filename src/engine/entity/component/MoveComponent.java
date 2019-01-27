package engine.entity.component;

import glm.vec._2.Vec2;

public class MoveComponent extends Component {
	private float velocity = 0;
	private Vec2 direction = new Vec2(0, 0);

	public MoveComponent(long entityID) {
		super(entityID);

	}

	/**
	 * Gets velocity in pixels/second.
	 * 
	 * @return
	 */
	public float getVelocity() {
		return velocity;
	}

	/**
	 * Sets velocity in pixels/second.
	 * 
	 * @param velocity
	 */
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	public Vec2 getDirection() {
		return direction;
	}

	public void setDirection(Vec2 direction) {
		this.direction = direction;
	}
}
