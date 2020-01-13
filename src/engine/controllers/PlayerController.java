package engine.controllers;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import demo.Game;
import engine.audio.AudioSource;
import engine.engine.AudioEngine;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.input.JoystickControl;
import engine.states.GSM;
import engine.utilities.ArraysExt;
import glm.vec._2.Vec2;

public class PlayerController extends Controller {
	private Vec2 faceLeft = new Vec2(1, 0);
	private Vec2 faceRight = new Vec2(0, 0);
	private boolean directions[] = new boolean[4];
	private AudioSource walkingSound;
	
	public PlayerController() {
		walkingSound = AudioEngine.getSelf().newAudioSource("foot_steps_grass", new Vec2(0,0), 100);
		walkingSound.setPitch(2.2f);
		walkingSound.setVolume(0.1f);
	}

	public boolean attackFinished(RenderComponent rc) {
		if (rc.getAnimations().getCurrentAnimationName() == "attacking"
				&& rc.getAnimations().getCurrentAnimation().hasPlayedOnce())
			return true;
		if (rc.getAnimations().getCurrentAnimationName() != "attacking")
			return true;
		return false;
	}

	@Override
	public void renderDebug() {
	}

	@Override
	public void update(float deltaTime, long entityID, Game context) {

		RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(entityID, RenderComponent.class);
		MoveComponent mc = (MoveComponent) context.getEm().getFirstComponent(entityID, MoveComponent.class);

		Vec2 dir = new Vec2(0, 0);

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W))
			dir.y = -1;

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) 
			dir.y = 1;

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) 
			dir.x = -1;
		
		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) 
			dir.x = 1;

		mc.setDirection(dir);
		if(GSM.getSelf().getJoystick().isThereAJoystick()) {
			dir = GSM.getSelf().getJoystick().getThumbDirection(JoystickControl.LEFT_THUMB_STICK);
			
			if(Math.abs(dir.x) < 0.25)
				dir.x = 0;
			if(Math.abs(dir.y) < 0.25)
				dir.y = 0;
			
			mc.setDirection(dir);
		}
		
		
		if(dir.x>0)
			rc.setOrientation(faceRight);
		else if(dir.x<0)
			rc.setOrientation(faceLeft);
		
		if(rc.getAnimations().getCurrentAnimationName().equals("attacking") && rc.getAnimations().getCurrentAnimation().hasPlayedOnce()) {
			rc.getAnimations().getCurrentAnimation().setPlayedOnce(false);
		}else if(rc.getAnimations().getCurrentAnimationName().equals("attacking")) {
			return;
		}

		if (dir.x!=0 || dir.y!=0) {
			walkingSound.setPosition(rc.getCenterPoint());
			if(!walkingSound.isPlaying()) {
				walkingSound.play();
			}
			rc.getAnimations().changeStateTo("walking");
			rc.setSize(rc.getAnimations().getCurrentAnimation().getCurrentFrameSize().mul(4));
		}else{
			rc.getAnimations().changeStateTo("idle_1");
			rc.setSize(rc.getAnimations().getCurrentAnimation().getCurrentFrameSize().mul(4));
			if(walkingSound.isPlaying()) {
				walkingSound.pause();
			}
		}
		
		if(GSM.getSelf().getMouse().isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_1)){
			rc.getAnimations().changeStateTo("attacking");
			rc.setSize(rc.getAnimations().getCurrentAnimation().getCurrentFrameSize().mul(4));
		}

	}
}
