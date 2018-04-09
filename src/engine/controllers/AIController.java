package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.ai.Action;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.DStarLite;
import engine.ai.State;
import engine.engine.Engine;
import engine.logic.GameObject;
import engine.utilities.ArraysExt;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;

public class AIController extends Controller{
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4]; 
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private ConsiderationTree ct = new ConsiderationTree();
	private DStarLite pathFinder = new DStarLite();
	private long before = System.nanoTime();
	
	
	public AIController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, GameObject object, Game context) {
				
		Action a = ct.calculateAction(object, context);
		
		if(a.getActionName()=="wander") {
			
			long now = System.nanoTime();
			
			if((now - startTime)>Engine.SECOND*4*tick + Engine.SECOND) {
				Arrays.fill(directions, false);
				startTime = System.nanoTime();
				int currentMove = r.nextInt(5);
				if(currentMove<4)
					directions[currentMove] = true;
			}
			move(object, deltaTime);
			
		}else if(a.getActionName()=="attack"){
			
			long timeNow = System.nanoTime();
			
			if(timeNow - before >= Engine.SECOND) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				
				int startX = (int) (context.camera.getX()*-1 - object.getBaseBox().getX())*-1/context.divisor;
				int startY =  (int) (context.camera.getY()*-1 - object.getBaseBox().getY())*-1/context.divisor; 
			
				pathFinder.initImpassibleCells(context.obstacleMap); 
				pathFinder.init(
						 startX,
						 startY,
						 (int) (context.camera.getX()*-1 - a.getTarget().getBaseBox().getX())*-1/context.divisor,
						 (int) (context.camera.getY()*-1 - a.getTarget().getBaseBox().getY())*-1/context.divisor);
				pathFinder.replan();
				List<State> path = pathFinder.getPath();
				State s = path.get(1);
		
				if(s.x>startX)
					directions[GameObject.RIGHT] = true;
				else if(s.x<startX)
					directions[GameObject.LEFT] = true;
				if(s.y> startY)
					directions[GameObject.BOTTOM] = true;
				else if(s.y<startY) 
					directions[GameObject.TOP] = true;
			
			}
			move(object, deltaTime);
		}
		
	}
	
	private void move(GameObject object, float deltaTime) {
			
		float xMove = 0;
		float yMove = 0;
		
		if(directions[GameObject.TOP]) {
			yMove = -object.getVelocity()*deltaTime;
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.BOTTOM]) {
			yMove = object.getVelocity()*deltaTime;
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.RIGHT]) {
			xMove = object.getVelocity()*deltaTime;
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.LEFT]) {
			xMove = -object.getVelocity()*deltaTime;
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("walking");
		}
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			object.getAnimations().changeStateTo("idle_1");
		}
		
		object.move(xMove, yMove);
	}


}
