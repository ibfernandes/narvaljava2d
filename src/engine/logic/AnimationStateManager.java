package engine.logic;

import java.io.Serializable;
import java.util.HashMap;
import engine.graphic.Animation;

public class AnimationStateManager implements Serializable {

	private String currentAnimation;
	private int currentAnimationType;
	private HashMap<String, Animation> animations;
	public static final int LOOP = 0;
	public static final int PLAY_ONCE = 0;

	/**
	 * [A]nimation [S]tate [M]anager
	 */
	public AnimationStateManager() {
		animations = new HashMap<String, Animation>();
	}

	/**
	 * Changes the actual state to @param state using the default play type LOOP.
	 * 
	 * @param state
	 */
	public void changeStateTo(String state) {
		changeStateTo(state, LOOP);
	}

	/**
	 * Changes the actual state to @param state using @playType.
	 * 
	 * @param state
	 * @param playType
	 */
	public void changeStateTo(String state, int playType) {
		if (animations.get(state) != null && currentAnimation != state) {
			currentAnimation = state;
			this.currentAnimationType = playType;
		}
	}

	public Animation getCurrentAnimation() {
		return animations.get(currentAnimation);
	}

	public String getCurrentAnimationName() {
		return currentAnimation;
	}

	public Animation getAnimation(String name) {
		return animations.get(name);
	}

	public void addAnimation(String name, Animation animation) {
		animations.put(name, animation);
	}
}
