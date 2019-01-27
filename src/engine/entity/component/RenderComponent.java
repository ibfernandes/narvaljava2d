package engine.entity.component;

import engine.geometry.Rectangle;
import engine.logic.AnimationStateManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class RenderComponent extends Component implements Comparable<RenderComponent> {
	private Vec2 renderPosition = new Vec2(0, 0);
	private Vec2 lastRenderPosition = new Vec2(0, 0);
	private String texture;
	private Vec2 orientation = new Vec2(0, 0);
	private Vec2 size = new Vec2(0, 0);
	private Vec2 skew = new Vec2(0, 0);
	private Vec4 color = new Vec4(1, 1, 1, 1);
	private AnimationStateManager animations;
	private float rotation;
	private Rectangle calculatedBaseBox = new Rectangle(0, 0, 0, 0);
	private Rectangle baseBox = new Rectangle(0, 0, 0, 0);
	private boolean disabled = false;
	private String Renderer = "";

	public RenderComponent(long entityID) {
		super(entityID);
	}

	/**
	 * Rectangle coordinates must be set normalized [0,1]. It uses
	 * RenderComponent.calculateBaseBox to later calculate its real position and size.
	 * 
	 * @param baseBox
	 */
	public void setBaseBox(Rectangle baseBox) {
		this.baseBox = baseBox;
	}

	/**
	 * Calculates and return the base box.
	 * 
	 * @return
	 */
	public Rectangle calculateBaseBox() {

		calculatedBaseBox.x = renderPosition.x + size.x * baseBox.x;
		calculatedBaseBox.y = renderPosition.y + size.y * baseBox.y;

		calculatedBaseBox.width = size.x * baseBox.width;
		calculatedBaseBox.height = size.y * baseBox.height;

		return calculatedBaseBox;
	}

	/**
	 * Return the center point relative to the bounding box.
	 * 
	 * @return
	 */
	public Vec2 getCenterPoint() {
		return new Vec2(renderPosition.x + size.x/ 2, renderPosition.y + size.y / 2);
	}

	/**
	 * Returns the render position. The one used to interpolate between updates.
	 * 
	 * @return
	 */
	public Vec2 getRenderPosition() {
		return renderPosition;
	}

	public void setRenderPosition(Vec2 renderPosition) {
		lastRenderPosition.x = this.renderPosition.x;
		lastRenderPosition.y = this.renderPosition.y;
		this.renderPosition = renderPosition;
	}

	public void setRenderPosition(float x, float y) {
		lastRenderPosition.x = this.renderPosition.x;
		lastRenderPosition.y = this.renderPosition.y;
		this.renderPosition.x = x;
		this.renderPosition.y = y;
	}

	public String getTexture() {
		return texture;
	}

	public void setTexture(String texture) {
		this.texture = texture;
	}

	/**
	 * Returns the orientation of this object. The value is 0 when it faces the same
	 * direction as the image, and 1 when it's flipped.
	 * 
	 * @return
	 */
	public Vec2 getOrientation() {
		return orientation;
	}

	/**
	 * Sets the orientation of this object. The value must be 0 when it faces the
	 * same direction as the image, and 1 when it's flipped.
	 * 
	 * @return
	 */
	public void setOrientation(Vec2 orientation) {
		this.orientation = orientation;
	}

	/**
	 * Gets final rendering size.
	 * 
	 * @return
	 */
	public Vec2 getSize() {
		return size;
	}

	/**
	 * Sets final rendering size.
	 * 
	 * @param size
	 */
	public void setSize(Vec2 size) {
		this.size = size;
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

	public AnimationStateManager getAnimations() {
		return animations;
	}

	public void setAnimations(AnimationStateManager animations) {
		this.animations = animations;
	}

	public float getRotation() {
		return rotation;
	}

	public void setRotation(float rotation) {
		this.rotation = rotation;
	}

	/**
	 * Returns if this object should or not be rendered.
	 * 
	 * @return
	 */
	public boolean isDisabled() {
		return disabled;
	}

	/**
	 * Sets if this object should or not be rendered.
	 * 
	 * @return
	 */
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	/**
	 * Compares this object's base box y axis with @param rc. Returns -1 when this <
	 * obj, 0 when this = obj and 1 when this > obj.
	 */
	@Override
	public int compareTo(RenderComponent rc) {
		Rectangle r = calculateBaseBox();

		if (r.y > rc.calculateBaseBox().y)
			return 1;
		if (r.y == rc.calculateBaseBox().y)
			return 0;
		if (r.y < rc.calculateBaseBox().y)
			return -1;

		return 0;
	}

	public String getRenderer() {
		return Renderer;
	}

	public void setRenderer(String renderer) {
		Renderer = renderer;
	}

	public Vec2 getLastRenderPosition() {
		return lastRenderPosition;
	}

	public void setLastRenderPosition(Vec2 lastRenderPosition) {
		this.lastRenderPosition = lastRenderPosition;
	}
}
