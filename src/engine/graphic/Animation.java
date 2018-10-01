package engine.graphic;

import java.io.Serializable;

import engine.engine.Engine;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class Animation implements Serializable{
	private Vec4 frames[];
	private int currentFrame = 0;
	private String texture;
	private long frameDuration = -1;
	private long startTime = System.nanoTime();
	private boolean playedOnce = false;

	/**
	 * Frame duration in milliseconds
	 * @param texture
	 * @param frameDurantion
	 */
	public Animation(String texture, long frameDuration) { //TODO: pass a vector of frame times instead of a linear one
		this.texture = texture;
		this.frameDuration = frameDuration;
	}
	
	/**
	 * Get frames from a spritesheet horizontally;
	 * @param quantity
	 * @param offset
	 * @param size
	 */
	public void setFrames(int quantity, Vec2 offset, Vec2 size) {
	
		frames = new Vec4[quantity];
		float width = ResourceManager.getSelf().getTexture(texture).getWidth();
		float height = ResourceManager.getSelf().getTexture(texture).getHeight(); //TODO: there's something wrong in here that is making ()
		
		for(int i= 0; i< quantity; i++){
			frames[i] = new Vec4(
						((float)i*size.x + offset.x)/width,
						(offset.y)/height,
						(size.x)/width,
						(size.y)/height
					);
		}
		
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
		
		if(frameDuration>0) {
			long elapsed = (System.nanoTime() - startTime) / Engine.MILISECOND;
			
			if(elapsed > frameDuration) {
				currentFrame++;
				startTime = System.nanoTime();
			}
			if(currentFrame == frames.length) {
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
