package graphic;

import java.util.HashMap;

import engine.graphic.Animation;



public class ASM {

	private Animation currentAnimation;
	private HashMap<String, Animation> animations;
	
	/**
	 * [A]nimation [S]tate [M]anager
	 */
	public ASM() {
		animations = new HashMap<String, Animation>();
	}
	
	public void changeStateTo(String state) {

		if(animations.get(state)!=null && currentAnimation!=animations.get(state))
			currentAnimation = animations.get(state);
		
		if(animations.get(state)==null)
			System.err.println("Animation "+state+" doesn't exists");
	}
	public Animation getCurrentAnimation() {
		return currentAnimation;
	}
	
	public Animation getAnimation(String name) {
		return animations.get(name);
	}
	
	public void addAnimation(String name, Animation animation) {
		animations.put(name, animation);
	}
}
