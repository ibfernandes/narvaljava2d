package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jbox2d.dynamics.BodyType;
import org.lwjgl.glfw.GLFW;

import engine.ai.AStar;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTalk;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.State;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.graphic.Animation;
import engine.logic.GameObject;
import engine.ui.UIObject;
import engine.utilities.ArraysExt;
import engine.utilities.ResourceManager;
import engine.utilities.Vec2i;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import graphic.ASM;

public class DeerController extends Controller{

	private ConsiderationTree ct = new ConsiderationTree();
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4]; 
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private long before = System.nanoTime();
	private List<Anode> pathAstar;
	private Game context;
	private int currentStep;
	
	public DeerController() {
		ct.addConsideration(new ConsiderationWander());
	}

	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		Action a = ct.calculateAction(object, context);
		
		if(a.getActionName()=="wander") {
			pathAstar = null;
			
			long now = System.nanoTime();
			
			if((now - startTime)>Engine.SECOND*4*tick + Engine.SECOND) {
				Arrays.fill(directions, false);
				startTime = System.nanoTime();
				int currentMove = r.nextInt(5);
				if(currentMove<4)
					directions[currentMove] = true;
			}
			move(object, deltaTime);
			
		}
		
	}
	
	private void move(GameObject object, float deltaTime) {
		
		float xMove = 0;
		float yMove = 0;
		org.jbox2d.common.Vec2  speed = new  org.jbox2d.common.Vec2(0, 0);
		
		if(directions[GameObject.TOP]) {
			yMove = -object.getVelocity()*deltaTime;
			speed.y = -object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.getAnimations().changeStateTo("running");
		}
		if(directions[GameObject.BOTTOM]) {
			yMove = object.getVelocity()*deltaTime;
			speed.y = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.getAnimations().changeStateTo("running");
		}
		if(directions[GameObject.RIGHT]) {
			xMove = object.getVelocity()*deltaTime;
			speed.x = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("running");
		}
		if(directions[GameObject.LEFT]) {
			xMove = -object.getVelocity()*deltaTime;
			speed.x = -object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("running");
		}
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			object.getAnimations().changeStateTo("idle_1");
		}
		object.getBody().setLinearVelocity(speed);
	}

	@Override
	public void renderDebug() {

	}

	@Override
	public void update(float deltaTime, Entity object, EntityManager context) {
	}


}
