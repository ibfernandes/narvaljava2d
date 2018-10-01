package graphic;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import engine.graphic.Animation;



public class ASM implements Serializable{

	private String currentAnimation;
	private int currentAnimationType;
	private Queue<Animation> animationsQueue;
	private Queue<Integer> animationsQueueType;
	private HashMap<String, Animation> animations;
	public static int LOOP = 0;
	public static int PLAY_ONCE = 0;
	
	/**
	 * [A]nimation [S]tate [M]anager
	 */
	public ASM() {
		animations = new HashMap<String, Animation>();
		animationsQueue = new LinkedList<>();
		animationsQueueType = new LinkedList<>();
	}
	
	public void changeStateTo(String state) {
		changeStateTo(state, LOOP);
	}
	
	public void changeStateTo(String state, int playType) {
		
		if(animations.get(state)!=null && currentAnimation!=state) {
			currentAnimation = state;
			currentAnimationType = playType;
		}
		
		//if(animations.get(state)==null)
		//	System.err.println("Animation "+state+" doesn't exists"); 
		//TODO: Use a default animation to express error on screen?
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
