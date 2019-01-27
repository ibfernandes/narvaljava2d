package engine.entity.component;

import engine.geometry.Rectangle;
import glm.vec._2.Vec2;

public class BasicComponent extends Component{
	private Vec2 position  = new Vec2(0,0);;
	private Vec2 previousPosition  = new Vec2(0,0);;
	private Vec2 size = new Vec2(0,0);
	private Rectangle boundingBox = new Rectangle(0,0,0,0);

	public BasicComponent(long entityID) {
		super(entityID);
	}
	
	public Vec2 getPosition() {
		return position;
	}
	public void setPosition(Vec2 position) {
		this.position.x = position.x;
		this.position.y = position.y;
	}
	
	public Vec2 getCenterPoint() {
		Rectangle r  = getBoundingBox();
		return new Vec2(r.x + r.width/2, r.y + r.height/2);
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
		this.previousPosition.x = previousPosition.x;
		this.previousPosition.y = previousPosition.y;
	}
}
