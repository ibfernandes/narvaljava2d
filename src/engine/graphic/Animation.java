package engine.graphic;

import java.io.Serializable;

import engine.engine.Engine;
import engine.utilities.ResourceManager;
import engine.utilities.Timer;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Animation implements Serializable {
	private Vec4 frames[];
	private int currentFrame = 0;
	private String texture;
	private long frameDurations[];
	private long frameDuration = -1;
	private long startTime = System.nanoTime();
	private boolean playedOnce = false;

	/**
	 * Sets the texture from where to extract the frames and each frame's even
	 * duration in milliseconds.
	 * 
	 * @param texture
	 * @param frameDurantion
	 */
	public Animation(String texture, long frameDuration) {
		this.texture = texture;
		if(ResourceManager.getSelf().getTexture(texture)==null)
			throw new Error("Texture wasn't loaded");
		
		this.frameDuration = frameDuration;
	}

	/**
	 * Sets the texture from where to extract the frames and each frame's duration
	 * in milliseconds.
	 * 
	 * @param texture
	 * @param frameDurations
	 */
	public Animation(String texture, long frameDurations[]) {
		this.texture = texture;
		if(ResourceManager.getSelf().getTexture(texture)==null)
			throw new Error("Texture wasn't loaded");
		
		this.frameDurations = frameDurations;
	}

	/**
	 * Sets frames horizontally and evenly spaced from a texture (sprite sheet). All
	 * values are normalized using Texture.width and Texture.height.
	 * 
	 * @param quantity
	 * @param offset
	 * @param size
	 */
	public void setFrames(int quantity, Vec2 offset, Vec2 size) {
		frames = new Vec4[quantity];
		float width = ResourceManager.getSelf().getTexture(texture).getWidth();
		float height = ResourceManager.getSelf().getTexture(texture).getHeight();

		for (int i = 0; i < quantity; i++) {
			frames[i] = new Vec4((i * size.x + offset.x) / width, (offset.y) / height, (size.x) / width,
					(size.y) / height);
		}
	}

	/**
	 * Sets frames horizontally and evenly spaced from a texture (sprite sheet). All
	 * values are normalized using Texture.width and Texture.height.
	 * 
	 * @param texture
	 * @param quantity
	 * @param offsetX
	 * @param offsetY
	 * @param sizeX
	 * @param sizeY
	 * @return
	 */
	public static Vec4[] generateFrames(String texture, int quantity, float offsetX, float offsetY, float sizeX,
			float sizeY) {
		Vec4 frames[] = new Vec4[quantity];

		float width = ResourceManager.getSelf().getTexture(texture).getWidth();
		float height = ResourceManager.getSelf().getTexture(texture).getHeight();

		for (int i = 0; i < quantity; i++) {
			frames[i] = new Vec4((i * sizeX + offsetX) / width, (offsetY) / height, (sizeX) / width, (sizeY) / height);
		}

		return frames;
	}

	/**
	 * Sets this animation's frames.
	 * 
	 * @param frames
	 */
	public void setFrames(Vec4 frames[]) {
		this.frames = frames;
	}

	public Vec4 getFrame(int index) {
		return frames[index];
	}

	public Vec4 getCurrentFrame() {
		return frames[currentFrame];
	}

	public String getTexture() {
		return texture;
	}

	public void update() {

		if (frameDuration > 0) {
			long elapsed = (System.nanoTime() - startTime) / Timer.MILLISECOND;

			if (elapsed > frameDuration) {
				currentFrame++;
				startTime = System.nanoTime();
			}
			if (currentFrame == frames.length) {
				currentFrame = 0;
				playedOnce = true;
			}
		} else if (frameDurations != null) {
			long elapsed = (System.nanoTime() - startTime) / Timer.MILLISECOND;

			if (elapsed > frameDurations[currentFrame]) {
				currentFrame++;
				startTime = System.nanoTime();
			}
			if (currentFrame == frames.length) {
				currentFrame = 0;
				playedOnce = true;
			}
		}
	}

	public boolean hasPlayedOnce() {
		return playedOnce;
	}

	public void setPlayedOnce(boolean playedOnce) {
		this.playedOnce = playedOnce;
	}
}
