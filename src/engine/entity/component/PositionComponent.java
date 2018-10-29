package engine.entity.component;

import glm.vec._2.Vec2;

public class PositionComponent extends Component{
	public PositionComponent(long entityID) {
		super(entityID);
		
	}
	private Vec2 position;
	private Vec2 previousPosition = new Vec2(0,0);
	
	public Vec2 getPosition() {
		return position;
	}
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	public Vec2 getPreviousPosition() {
		return previousPosition;
	}
	public void setPreviousPosition(Vec2 previousPosition) {
		this.previousPosition = previousPosition;
	}
}
