package engine.entity.component;

import glm.vec._2.Vec2;

public class MoveComponent extends Component{
	public MoveComponent(long entityID) {
		super(entityID);
		
	}
	public float velocity = 0;
	public Vec2 direction = new Vec2(0,0);
	
	public float getVelocity() {
		return velocity;
	}
	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}
}
