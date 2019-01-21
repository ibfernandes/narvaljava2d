package engine.controllers;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.graphic.Texture;
import engine.input.JoystickControl;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.logic.GameObject;
import engine.utilities.ArraysExt;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class PlayerController  extends Controller{
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private boolean directions[] = new boolean[4]; 
	
	public boolean attackFinished(GameObject object) {
		if(object.getAnimations().getCurrentAnimationName()=="attacking" && object.getAnimations().getCurrentAnimation().hasPlayedOnce())
			return true;
		if(object.getAnimations().getCurrentAnimationName()!="attacking")
			return true;
		return false;
	}

	@Override
	public void renderDebug() {
	}

	@Override
	public void update(float deltaTime, long entityID, Game context) {
		
		Arrays.fill(directions, false); // TODO: for now i could use only one boolean (isMoving)
		float xMove =0;
		float yMove =0;

		RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(entityID, RenderComponent.class);
		MoveComponent mc = (MoveComponent) context.getEm().getFirstComponent(entityID, MoveComponent.class);
		
		Vec2 dir = new Vec2(0,0);
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W)) {
			dir.y = -1;
			directions[0] = true;
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) {
			dir.y = 1;
			directions[1] = true;
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) {
			dir.x = -1;
			
			rc.setOrientation(faceLeft);
			directions[2] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) {
			dir.x = 1;
			
			rc.setOrientation(faceRight);
			directions[3] = true;
		}
		
		mc.direction = dir;
		//mc.direction = GSM.getSelf().getJoystick().getThumbDirection(JoystickControl.LEFT_THUMB_STICK);

		if(ArraysExt.areAllElementsEqualTo(directions, false)){
			rc.getAnimations().changeStateTo("idle_1");
		}

	}
}
