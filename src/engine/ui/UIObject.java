package engine.ui;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Comparator;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import engine.controllers.Controller;
import engine.engine.PhysicsEngine;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.renderer.ASM;
import engine.renderer.CubeRenderer;
import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class UIObject implements Comparable<UIObject>, Serializable{
	
	private static final long serialVersionUID = 1L;
	private String group;
	private Vec2 position = new Vec2(0,0);
	private Vec2 previousPosition = new Vec2(0,0);
	private Vec2 orientation = new Vec2(0,0); //Default: facing the same as image
	private Vec2 size		 = new Vec2(0,0);
	private Vec2 interationRange = new Vec2(0,0); //boundingBox + interationRange indicates the full interationBox
	private Rectangle boundingBox = new Rectangle(0,0,0,0);
	private Rectangle sightBox = new Rectangle(0,0,0,0);
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
	public static final int	TOP    = 0, 
				  			RIGHT  = 1, 
				  			BOTTOM = 2,
				  			LEFT   = 3,
		  					TOP_DIAGONAL_LEFT   = 4,
							TOP_DIAGONAL_RIGHT   = 5,
							BOTTOM_DIAGONAL_LEFT  = 6,
				  			BOTTOM_DIAGONAL_RIGHT  = 7;
	
	//Box2D
	private org.jbox2d.common.Vec2 b2BaseBoxPosition;
	private org.jbox2d.common.Vec2 b2Size;
	private transient Body body;
	private BodyType type;
	
	private String text;
	private String font;
	
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
       // org.jbox2d.serialization.JbSerializer srl;
       // srl.serialize(body)
       // oos.writeObject(address.getHouseNumber());
    }
		 
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        createBody(PhysicsEngine.getSelf().getWorld(), type);
    }

	/**
	 * First define baseBox variable so the Box2D body can be properly initialized.
	 * @param world
	 * @param type
	 */
	public void createBody(World world, BodyType type) {
		this.type = type;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = type;
		b2BaseBoxPosition = convertPixelsToMeters(baseBox.x, baseBox.y);
		bodyDef.position.set(b2BaseBoxPosition);
		
		body = world.createBody(bodyDef);
		
		PolygonShape dShape = new PolygonShape();
		b2Size = convertPixelsToMeters(((float)this.baseBox.width/2f),((float)this.baseBox.height/2f));
		
		dShape.setAsBox(b2Size.x,b2Size.y);

		FixtureDef fixDef = new FixtureDef();
		fixDef.shape = dShape;
		//fixDef.density = 10;
		//fixDef.friction = 0.0f;

		body.createFixture(fixDef);
	}
	
	public org.jbox2d.common.Vec2 convertPixelsToMeters(float x, float y){
		return new org.jbox2d.common.Vec2(x/PhysicsEngine.BOX2D_SCALE_FACTOR, y/PhysicsEngine.BOX2D_SCALE_FACTOR);
	}
	
	public Vec2 convertMetersToPixels(float x, float y){
		return new Vec2(x*PhysicsEngine.BOX2D_SCALE_FACTOR, y*PhysicsEngine.BOX2D_SCALE_FACTOR);
	}
	
	public UIObject() {
		
	}
	 
	public UIObject(Vec2 position, Vec2 size, float velocity, Vec4 color, float rotation, boolean isSolid, String texture, Controller controller) {
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
	
	public UIObject(Vec2 position, Vec2 size, float velocity, Vec4 color, float rotation, boolean isSolid, Controller controller) {
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
		 
		 if(text!=null)
			 ResourceManager.getSelf().getFont(font).render(text, position.x, position.y, new Vec4(1f,0f,0,1f));
	}
	
	public void renderDebug() {
		ResourceManager.getSelf().getCubeRenderer().render(baseBox, 0, new Vec3(1,0,0));
		ResourceManager.getSelf().getCubeRenderer().render(boundingBox, 0, new Vec3(1,1,0));
		ResourceManager.getSelf().getCubeRenderer().render(getInterationBox(), 0, new Vec3(0,0,1));
		if(controller!=null)
			controller.renderDebug();
	}
	
	public void updateCoordinateSystems() {
		if(body!=null) {
			Vec2 pos = convertMetersToPixels(body.getPosition().x, body.getPosition().y);
			pos.y = (pos.y - size.y + baseBox.height/2);
			pos.x = pos.x - baseBox.width/2;
			setPosition(pos);
		}
	}
	
	public void update(float deltaTime, Game game) {//TODO: remove Game as paramater
		updateCoordinateSystems();
		
	//	if(controller!=null)
		//	controller.update(deltaTime, this, game);
		if(animations!=null)
			animations.getCurrentAnimation().update();
	}

	public void moveDirectlyTo(float x, float y) {
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		position.x = x;
		position.y = y;
		boundingBox.x = x;
		boundingBox.y = y;
		baseBox.x =  x;
		baseBox.y =  (position.y + size.y - baseBox.height);
		sightBox.x = x;
		sightBox.y = y;
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
		boundingBox.x += x;
		boundingBox.y += y;
		baseBox.x +=  x;
		baseBox.y += y;
		sightBox.x +=  x;
		sightBox.y += y;
	}

	public boolean checkBaseBoxCollisionAABB(UIObject other) { 
		return baseBox.intersects(other.getBaseBox());
	}
	
	public void resolveCollision(UIObject other) {
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
	public void setPosition(Vec2 position) { //TODO: remove and stay only with moveDirectlyTo
		this.position = position;
		this.previousPosition =  new Vec2(position.x , position.y);
		baseBox.x =  position.x;
		baseBox.y = (position.y + size.y - baseBox.height);
		boundingBox.x = position.x;
		boundingBox.y = position.y;
		sightBox.x =  position.x;
		sightBox.y = position.y; //TODO: not correctly calculated
		
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
		boundingBox.width = size.x;
		boundingBox.height = size.y;
		this.size = size;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
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
	public int compareTo(UIObject obj) {
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

	public Rectangle getSightBox() {
		return sightBox;
	}

	public void setSightBox(Vec2 sightBox) {
		this.sightBox.width = sightBox.x;
		this.sightBox.height =  sightBox.y;

	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public Vec2 getInterationRange() {
		return interationRange;
	}
	/**
	 * BoundingBox + interationRange specifies the range in which this object is able to interact with another
	 * @param interationRange
	 */
	public void setInterationRange(Vec2 interationRange) {
		this.interationRange = interationRange;
	}
	
	public Rectangle getInterationBox() {
		return new Rectangle(boundingBox.x-interationRange.x/2, boundingBox.y - interationRange.y/2, boundingBox.width+interationRange.x,boundingBox.height+interationRange.y);
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getFont() {
		return font;
	}

	public void setFont(String font) {
		this.font = font;
	}
}
