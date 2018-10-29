package engine.entity.component;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;

import engine.engine.PhysicsEngine;
import engine.geometry.Rectangle;
import glm.vec._2.Vec2;

public class BodyComponent extends Component{
	public BodyComponent(long entityID) {
		super(entityID);
		
	}

	public transient Body body;
	public BodyType type;
	private Rectangle baseBox;
	private Vec2 b2BaseBoxPosition;
	private Vec2 b2Size;
	private Rectangle calculatedBaseBox = new Rectangle(0,0,0,0);
	private Vec2 position = new Vec2();
	
	public Rectangle getBaseBox() {
		return baseBox;
	}
	/**
	 * Rectangle coordinates must be set in normalized local object space. It uses RenderComponent.getBoundingBox as reference (anchorPoint).
	 * @param baseBox
	 */
	public void setBaseBox(Rectangle baseBox) {
		this.baseBox = baseBox;
	}
	
	public Rectangle calculateBaseBox(Vec2 position, Vec2 size) {

		calculatedBaseBox.x = position.x + size.x * baseBox.x;
		calculatedBaseBox.y = position.y + size.y * baseBox.y;

		calculatedBaseBox.width = size.x * baseBox.width;
		calculatedBaseBox.height = size.y * baseBox.height;
		
		return calculatedBaseBox;
	}
	
	public Vec2 calculatePosition(Vec2 pos, Vec2 size) {

		this.position.x = pos.x - size.x * baseBox.x;
		this.position.y = pos.y - size.y * baseBox.y;
		
		return this.position;
	}
	
	public void createBody(World world, BodyType type) {
		this.type = type;
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = type;
		b2BaseBoxPosition = PhysicsEngine.convertPixelsToMeters(calculatedBaseBox.x, calculatedBaseBox.y);
		bodyDef.position.x = b2BaseBoxPosition.x;
		bodyDef.position.y = b2BaseBoxPosition.y;
		
		body = world.createBody(bodyDef);
		
		PolygonShape dShape = new PolygonShape();
		b2Size = PhysicsEngine.convertPixelsToMeters(((float)this.calculatedBaseBox.width/2f),((float)this.calculatedBaseBox.height/2f));
		
		dShape.setAsBox(b2Size.x,b2Size.y);

		FixtureDef fixDef = new FixtureDef();
		fixDef.shape = dShape;
		//fixDef.density = 10;
		//fixDef.friction = 0.0f;

		body.createFixture(fixDef);
	}
	
	public Rectangle getCalculatedBaseBox() {
		return calculatedBaseBox;
	}
}
