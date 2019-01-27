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

public class BodyComponent extends Component {
	public BodyComponent(long entityID) {
		super(entityID);

	}

	public transient Body body;
	public BodyType type;
	private Rectangle baseBox;
	private Vec2 b2BaseBoxPosition;
	private Vec2 b2Size;
	private Rectangle calculatedBaseBox = new Rectangle(0, 0, 0, 0);
	private Vec2 position = new Vec2();

	public Rectangle getBaseBox() {
		return baseBox;
	}

	/**
	 * Rectangle coordinates must be set in normalized local object space. It uses
	 * RenderComponent.getBoundingBox as reference (anchorPoint).
	 * 
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

	/**
	 * Returns a center point relative to the base box.
	 * 
	 * @return
	 */
	public Vec2 getCenterPoint(Vec2 position, Vec2 size) {
		Rectangle r = calculateBaseBox(position, size);
		return new Vec2(r.x + r.width / 2, r.y + r.height / 2);
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
		b2BaseBoxPosition = PhysicsEngine.convertPixelsToMeters(calculatedBaseBox.x + this.calculatedBaseBox.width / 2,
				calculatedBaseBox.y + this.calculatedBaseBox.width / 2);
		bodyDef.position.x = b2BaseBoxPosition.x;
		bodyDef.position.y = b2BaseBoxPosition.y;

		body = world.createBody(bodyDef);

		PolygonShape dShape = new PolygonShape();
		b2Size = PhysicsEngine.convertPixelsToMeters(this.calculatedBaseBox.width, this.calculatedBaseBox.height);

		dShape.setAsBox(b2Size.x / 2, b2Size.y / 2);

		FixtureDef fixDef = new FixtureDef();
		fixDef.shape = dShape;
		// fixDef.density = 10;
		// fixDef.friction = 0.0f;

		body.createFixture(fixDef);
	}

	public Rectangle getCalculatedBaseBox() {
		return calculatedBaseBox;
	}

	public Vec2 getB2Size() {
		return b2Size;
	}

	public void setB2Size(Vec2 b2Size) {
		this.b2Size = b2Size;
	}
}
