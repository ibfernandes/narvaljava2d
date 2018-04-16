package engine.controllers;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import engine.graphic.Texture;
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
	
	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		Arrays.fill(directions, false); // TODO: for now i could use only one boolean (isMoving)
		float xMove =0;
		float yMove =0;
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W)) {
			yMove = -object.getVelocity()*deltaTime;
			directions[0] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) {
			yMove = object.getVelocity()*deltaTime;
			directions[1] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) {
			xMove = -object.getVelocity()*deltaTime;
			
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("walking");
			directions[2] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) {
			xMove = object.getVelocity()*deltaTime;
			
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("walking");
			directions[3] = true;
		}
		object.move(xMove, yMove);
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			object.getAnimations().changeStateTo("idle_1");
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_E)) {
			
		}
	}

	@Override
	public void renderDebug() {
	}
}
