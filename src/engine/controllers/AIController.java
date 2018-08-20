package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.ai.AStar;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.State;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.logic.GameObject;
import engine.utilities.ArraysExt;
import engine.utilities.ResourceManager;
import engine.utilities.Vec2i;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class AIController extends Controller{
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4]; 
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private ConsiderationTree ct = new ConsiderationTree();
	private long before = System.nanoTime();
	private List<Anode> pathAstar;
	private Game context;
	private int currentStep;
	
	public AIController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		this.context = context;
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
			
		}else if(a.getActionName()=="attack"){//TODO: set pathfinding on another process
			
			long timeNow = System.nanoTime();
			
			int startX = (int) (context.getCamera().getX() - object.getBaseBox().getX())*-1/context.graphDivisor;
			int startY =  (int) (context.getCamera().getY() - object.getBaseBox().getY())*-1/context.graphDivisor; 
			int endX =  (int) (context.getCamera().getX() - a.getTarget().getBaseBox().getX())*-1/context.graphDivisor;
			int endY = (int) (context.getCamera().getY() - a.getTarget().getBaseBox().getY())*-1/context.graphDivisor;
			
			if(timeNow - before >= Engine.SECOND/10) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				currentStep = 0;
				
				AStar as = new AStar();
				pathAstar = as.calculatePath(new Vec2i(startX, startY), new Vec2i(endX, endY-4), context.getPointOfViewCollisionGraph(object));
			
			}
			
			
			if(  ((object.getPosition().x - object.getPreviousPosition().x >context.graphDivisor  || object.getPosition().y - object.getPreviousPosition().y >context.graphDivisor) 
					|| currentStep==0 )&& pathAstar!=null && pathAstar.size()>0 ) {
				int buffer = pathAstar.size()-2 - currentStep++;
				int i = (buffer < 0)? 0: buffer;
				Anode pass = pathAstar.get(i);
				
				if(pass.pos.x>startX)
					directions[GameObject.RIGHT] = true;
				else if(pass.pos.x<=startX)
					directions[GameObject.LEFT] = true;
				if(pass.pos.y> startY)
					directions[GameObject.BOTTOM] = true;
				else if(pass.pos.y<=startY) 
					directions[GameObject.TOP] = true;
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
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.BOTTOM]) {
			yMove = object.getVelocity()*deltaTime;
			speed.y = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.RIGHT]) {
			xMove = object.getVelocity()*deltaTime;
			speed.x = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.LEFT]) {
			xMove = -object.getVelocity()*deltaTime;
			speed.x = -object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("walking");
		}
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			object.getAnimations().changeStateTo("idle_1");
		}
		object.getBody().setLinearVelocity(speed);
	}

	@Override
	public void renderDebug() {

		if(pathAstar!=null) {
			for(Anode state: pathAstar) {
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2(context.getCamera().getX() + state.pos.x*context.graphDivisor, context.getCamera().getY() + state.pos.y*context.graphDivisor), new Vec2(8,8), 0, new Vec3(0,1,0));
			}
		}
	}


}
