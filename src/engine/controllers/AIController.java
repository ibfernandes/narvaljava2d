package engine.controllers;

import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.engine.Engine;
import engine.logic.GameObject;
import gameStates.GSM;
import glm.vec._2.Vec2;

public class AIController extends Controller{
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private int currentMove = 4;
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);

	@Override
	public void update(float deltaTime, GameObject object) {
		long now = System.nanoTime();
		
		if((now - startTime)>Engine.SECOND*4*tick + Engine.SECOND) {
			startTime = System.nanoTime();
			currentMove = r.nextInt(5);
		}
		
		switch(currentMove) {
			
			case 0:
				object.move(0, -object.getVelocity()*deltaTime);
				object.getAnimations().changeStateTo("walking");
				break;
			case 1:
				object.move(0,object.getVelocity()*deltaTime);
				object.getAnimations().changeStateTo("walking");
				break;
			case 2:
				object.move(object.getVelocity()*deltaTime,0);
				object.setOrientation(faceRight);
				object.getAnimations().changeStateTo("walking");
				break;
			case 3:
				object.move(-object.getVelocity()*deltaTime,0);
				object.setOrientation(faceLeft);
				object.getAnimations().changeStateTo("walking");
				break;
			case 4:
				object.getAnimations().changeStateTo("idle_1");
			
		}
	}


}
