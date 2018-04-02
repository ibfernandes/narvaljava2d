package engine.logic;


import java.util.Comparator;

import engine.controllers.Controller;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import graphic.ASM;
import graphic.CubeRenderer;

public class GameObject implements Comparable<GameObject>{
	
	private Vec2 position = new Vec2(0,0);
	private Vec2 previousPosition = new Vec2(0,0);
	private Vec2 orientation = new Vec2(0,0); //Default: facing the same as image
	private Vec2 size		 = new Vec2(0,0);
	private Rectangle boundingBox = new Rectangle(0,0,0,0);
	private Rectangle baseBox	 = new Rectangle(0,0,0,0); //Used to collide objects in motion
	private Vec2 anchorPoint = new Vec2(0,0); //Default: ANCHOR_TOP_LEFT
	private Vec2 skew 		= new Vec2(0,0);
	private Vec4 color 		= new Vec4(1,1,1,1);
	private float rotation, velocity;
	private boolean isSolid, isAlive;
	private ASM animations;
	private Controller controller;
	private String texture;	// Already loaded from the ResourceManager
	public static final int ANCHOR_TOP_LEFT 	=	0,
							ANCHOR_TOP_RIGHT	= 	1,
							ANCHOR_BOTTOM_LEFT  = 	2,
							ANCHOR_BOTTOM_RIGHT =	3,
							ANCHOR_MIDDLE		= 	4;
	public static final int 	TOP    = 0, 
					  			RIGHT  = 1, 
					  			BOTTOM = 2,
					  			LEFT   = 3;

	public GameObject() {
		
	}
	 
	public GameObject(Vec2 position, Vec2 size, float velocity, Vec4 color, float rotation, boolean isSolid, String texture, Controller controller) {
		this.position 	= position;
		this.previousPosition 	= new Vec2(position.x , position.y);
		this.size		= size;
		this.velocity 	= velocity;
		this.color 		= color;
		this.rotation	= rotation;
		this.isSolid 	= isSolid;
		this.texture 	= texture;
		this.controller = controller;
	}
	
	public GameObject(Vec2 position, Vec2 size, float velocity, Vec4 color, float rotation, boolean isSolid, Controller controller) {
		this.position 	= position;
		this.previousPosition 	= new Vec2(position.x , position.y);
		this.size		= size;
		this.velocity 	= velocity;
		this.color 		= color;
		this.rotation	= rotation;
		this.isSolid 	= isSolid;
		this.controller = controller;
	}
	
	public void render() {
		 if (texture!=null) {
			 ResourceManager.getSelf().getTextureRenderer().render(
						ResourceManager.getSelf().getTexture(texture),
						ResourceManager.getSelf().getTexture(texture+"_normal"),
						position, size, rotation, color, new Vec4(0,0,1,1), orientation, skew);
		}else if(animations!=null) {
			ResourceManager.getSelf().getTextureRenderer().render(
					ResourceManager.getSelf().getTexture(animations.getCurrentAnimation().getTexture()),
					ResourceManager.getSelf().getTexture(animations.getCurrentAnimation().getTexture()+"_normal"),
					position, size, rotation, color, animations.getCurrentAnimation().getCurrentFrame(), orientation, skew);
		}else if(texture==null) {
			//cubeRenderer.render(position, size, rotation, color);
		}
	}
	
	public void renderDebug() {
		//ResourceManager.getSelf().getCubeRenderer().render(new Vec2(position.x, position.y + size.y - baseBox.y), baseBox, 0, new Vec3(1,0,0));
		ResourceManager.getSelf().getCubeRenderer().render(baseBox, 0, new Vec3(1,0,0));
	}
	
	public void update(float deltaTime) {
		if(controller!=null)
			controller.update(deltaTime, this);
		if(animations!=null)
			animations.getCurrentAnimation().update();
	}

	public void moveDirectlyTo(float x, float y) {
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		position.x = x;
		position.y = y;
		baseBox.x =  x;
		baseBox.y =  (position.y + size.y - baseBox.height);
	}
	
	/**
	 * Must be called only once per update iteration
	 * @param x
	 * @param y
	 */
	public void move(float x, float y) {
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		position.x += x;
		position.y += y;
		baseBox.x +=  x;
		baseBox.y += y;
	}

	public boolean checkBaseBoxCollisionAABB(GameObject other) { 
		return baseBox.intersects(other.getBaseBox());
	}
	
	public void resolveCollision(GameObject other) {
		/*if((previousPosition.x==position.x && previousPosition.y==position.y) 
				&&
				(other.getPreviousPosition().x!=other.getPosition().x || other.getPreviousPosition().y!=other.getPosition().y)
				){ //TODO: may get into a loop
			other.resolveCollision(this);
			return;
		}
		
		Vec2 depth = new  Vec2();
		depth.x = baseBox.x - other.getBaseBox().x; 
		depth.y = baseBox.y - other.getBaseBox().y;
		
		/*int side = getIntersectionSide(other.getBaseBox());
		int movingDirectionX = getMovingDirectionX();
		int movingDirectionY = getMovingDirectionY();*/
		
		/*float thisTopLeft = ;
		float thisTopRight = ;
		float otherTopRight = ;
		float otherTopRight = ;
												//esq		dir
		float newX = position.x + ((depth.x<=0)? depth.x : depth.x*-1);
												//acima		abaixo
		float newY = position.y + ((depth.y<=0)? depth.y : depth.y*-1);

		moveDirectlyTo(newX, newY);*/
		float dx = other.getBaseBox().x - baseBox.x;
	    float px = (other.getBaseBox().getRadiusX() + baseBox.getRadiusX()) - Math.abs(dx);
	    
	    float dy = other.getBaseBox().y - baseBox.y;
	    float py = (other.getBaseBox().getRadiusY() + baseBox.getRadiusY()) - Math.abs(dy);
	    
		Vec2 delta = new Vec2();
		delta.x = px * MathExt.sign(dx);
		delta.y = py * MathExt.sign(dy);
	
	}
	
	/**
	 * return X direction (RIGHT or LEFT) or -1 in case it's not moving in this axis
	 * @return
	 */
	public int getMovingDirectionX(){
		float xAxis = previousPosition.x - position.x;
		
		if(xAxis<0)
			return RIGHT;
		if(xAxis>0)
			return LEFT;
		
		return -1;
	}
	/**
	 * return Y direction (TOP or BOTTOM) or -1 in case it's not moving in this axis
	 * @return
	 */
	public int getMovingDirectionY(){
		float yAxis = previousPosition.y - position.y;
		if(yAxis<0)
			return BOTTOM;
		if(yAxis>0)
			return TOP;
		
		return -1;
	}
	
	/**
	 * returns an integer between 0 and 3 indicating which side of the calling object(this) is colliding.
	 * check static int TOP, RIGHT, LEFT and BOTTOM
	 */
	public int getIntersectionSide(Rectangle r){
		double wy = (baseBox.width + r.width) * (baseBox.getCenterY() - r.getCenterY());
		double hx = (baseBox.height + r.height) * (baseBox.getCenterX() - r.getCenterX());

		if (wy > hx)
		    if (wy > -hx)
		       return TOP;
		    else
		        return RIGHT;
		else
		    if (wy > -hx)
		        return LEFT;
		    else
		        return BOTTOM;
	}

	public float getArea() {
		return size.x * size.y;
	}

	public Controller getController() {
		return controller;
	}

	public void setController(Controller controller) {
		this.controller = controller;
	}

	public float getVelocity() {
		return velocity;
	}

	public void setVelocity(float velocity) {
		this.velocity = velocity;
	}

	public Vec2 getPosition() {
		return position;
	}
	
	public Vec2 getPreviousPosition() {
		return previousPosition;
	}

	/**
	 * You must first supply the size and baseBox (if any).
	 * @param position
	 */
	public void setPosition(Vec2 position) {
		this.position = position;
		this.previousPosition =  new Vec2(position.x , position.y);
		baseBox.x =  position.x;
		baseBox.y = (position.y + size.y - baseBox.height);
	}
	
	public float getX() {
		return position.x;
	}
	
	public float getY() {
		return position.y;
	}
	public Vec2 getSize() {
		return size;
	}
	/**
	 * x = width;
	 * y = height
	 * @param size
	 */
	public void setSize(Vec2 size) {
		this.size = size;
	}

	public Vec2 getOrientation() {
		return orientation;
	}
	
	public void setOrientation(Vec2 orientation) {
		this.orientation = orientation;
	}
	/**
	 * Angle in degrees.
	 * @return
	 */
	public Vec2 getSkew() {
		return skew;
	}

	public void setSkew(Vec2 skew) {
		this.skew = skew;
	}

	public float getRotation() {
		return rotation;
	}

	public Vec4 getColor() {
		return color;
	}
	public void setColor(Vec4 color) {
		this.color = color;
	}
	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	public ASM getAnimations() {
		return animations;
	}

	public void setAnimations(ASM animations) {
		this.animations = animations;
	}
	public String getTexture() {
		return texture;
	}
	public void setTexture(String texture) {
		this.texture = texture;
	}
	
	public Vec2 getAnchorPoint() {
		return anchorPoint;
	}
	public void setAnchorPoint(Vec2 anchorPoint) {
		this.anchorPoint = anchorPoint;
	}
	/**
	 * Set anchor point based on some default values (i.e ANCHOR_TOP_LEFT, etc)
	 * @param anchor_point
	 */
	public void setAnchorPoint(int anchor_point) {
		switch(anchor_point) {
			case ANCHOR_TOP_LEFT:
				anchorPoint = new Vec2(0,0);
			case ANCHOR_TOP_RIGHT:
				anchorPoint = new Vec2(size.x,0);
			case ANCHOR_BOTTOM_LEFT:
				anchorPoint = new Vec2(0, size.y);
			case ANCHOR_BOTTOM_RIGHT:
				anchorPoint = new Vec2(size.x,size.y);
			case ANCHOR_MIDDLE:
				anchorPoint = new Vec2(size.x/2,size.y/2);
		}
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}
	/**
	 * Set BoundingBox's width and height
	 * @param boundingBox
	 */
	public void setBoundingBox(Vec2 boundingBox) {
		this.boundingBox.width = boundingBox.x;
		this.boundingBox.height =  boundingBox.y;
	}

	@Override
	// -1 this < obj
	// 0 this = obj
	// 1 this > obj
	public int compareTo(GameObject obj) {
		if(baseBox.y > obj.baseBox.y) 
			return 1;
		if(baseBox.y == obj.baseBox.y) 
			return 0;
		if(baseBox.y < obj.baseBox.y) 
			return -1;
		
		return 0;
	}

	public Rectangle getBaseBox() {
		return baseBox;
	}
	/**
	 * Sets baseBox width and height
	 * @param baseBox
	 */
	public void setBaseBox(Vec2 baseBox) {
		this.baseBox.width = baseBox.x;
		this.baseBox.height = baseBox.y;
	}
	
	public float getAngle(Vec2 pb) {
		return (float) Math.toDegrees(Math.atan2(position.y - pb.y, position.x - pb.x));
	}
	public float getDistance(Vec2 pb) {
		return (float) Math.sqrt(
				Math.pow(position.x - pb.x, 2)
				+
				Math.pow(position.y - pb.y, 2)
				);
	}
}
