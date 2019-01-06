package engine.entity.component;

import engine.geometry.Rectangle;
import glm.vec._2.Vec2;

public class BasicComponent extends Component{
	private Vec2 position;
	private Vec2 previousPosition;
	private Vec2 size = new Vec2(0,0);
	private Rectangle boundingBox = new Rectangle(0,0,0,0);

	public BasicComponent(long entityID) {
		super(entityID);
	}
	
	public Vec2 getPosition() {
		return position;
	}
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	
	public Vec2 getSize() {
		return size;
	}
	public void setSize(Vec2 size) {
		this.size = size;
	}

	public Rectangle getBoundingBox() {
		boundingBox.x = position.x;
		boundingBox.y = position.y;
		boundingBox.width = size.x;
		boundingBox.height = size.y;
		
		return boundingBox;
	}

	public Vec2 getPreviousPosition() {
		return previousPosition;
	}

	public void setPreviousPosition(Vec2 previousPosition) {
		this.previousPosition = previousPosition;
	}
}
