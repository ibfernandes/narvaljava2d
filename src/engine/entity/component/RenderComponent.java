package engine.entity.component;

import engine.geometry.Rectangle;
import engine.logic.GameObject;
import engine.renderer.ASM;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class RenderComponent extends Component implements Comparable<RenderComponent>{
	public RenderComponent(long entityID) {
		super(entityID);
		
	}

	private Vec2 renderPosition = new Vec2(0,0);
	private String texture;	
	private Vec2 orientation = new Vec2(0,0); //Default: facing the same as image
	private Vec2 size		 = new Vec2(0,0);
	private Vec2 anchorPoint = new Vec2(0,0); //Default: ANCHOR_TOP_LEFT
	private Vec2 skew 		= new Vec2(0,0);
	private Vec4 color 		= new Vec4(1,1,1,1);
	private ASM animations;
	private float rotation;
	private Rectangle boundingBox = new Rectangle(0,0,0,0);
	private boolean disabled = false;
	
	public Rectangle getBoundingBox() {
		boundingBox.x = renderPosition.x;
		boundingBox.y = renderPosition.y;
		boundingBox.width = size.x;
		boundingBox.height = size.y;
		
		return boundingBox;
	}
	public Vec2 getRenderPosition() {
		return renderPosition;
	}
	public void setRenderPosition(Vec2 renderPosition) {
		this.renderPosition = renderPosition;
	}
	public void setRenderPosition(float x, float y) {
		this.renderPosition.x = x;
		this.renderPosition.y = y;
	}
	public String getTexture() {
		return texture;
	}
	public void setTexture(String texture) {
		this.texture = texture;
	}
	public Vec2 getOrientation() {
		return orientation;
	}
	public void setOrientation(Vec2 orientation) {
		this.orientation = orientation;
	}
	public Vec2 getSize() {
		return size;
	}
	public void setSize(Vec2 size) {
		this.size = size;
	}
	public Vec2 getAnchorPoint() {
		return anchorPoint;
	}
	public void setAnchorPoint(Vec2 anchorPoint) {
		this.anchorPoint = anchorPoint;
	}
	public Vec2 getSkew() {
		return skew;
	}
	public void setSkew(Vec2 skew) {
		this.skew = skew;
	}
	public Vec4 getColor() {
		return color;
	}
	public void setColor(Vec4 color) {
		this.color = color;
	}
	public ASM getAnimations() {
		return animations;
	}
	public void setAnimations(ASM animations) {
		this.animations = animations;
	}
	public float getRotation() {
		return rotation;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	//TODO: what if the object doesnt have a baseBox?
	// -1 this < obj
	// 0 this = obj
	// 1 this > obj
	public int compareTo(RenderComponent rc) {
		if(getRenderPosition().y > rc.getRenderPosition().y) 
			return 1;
		if(getRenderPosition().y == rc.getRenderPosition().y) 
			return 0;
		if(getRenderPosition().y < rc.getRenderPosition().y) 
			return -1;
		
		return 0;
	}
}
