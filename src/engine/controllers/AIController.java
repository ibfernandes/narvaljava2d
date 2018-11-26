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
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BodyComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;
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
	private int currentStep;
	private BodyComponent bc;
	private RenderComponent rc;
	private PositionComponent pc;
	private MoveComponent mc;
	
	private Game gameContext;
	private long lastEntityID;
	
	public AIController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, long entityID, Game context) {
		gameContext = context;
		lastEntityID = entityID;
		Action a = ct.calculateAction(entityID, context.getEm());
		
		bc = context.getEm().getFirstComponent(entityID, BodyComponent.class);
		rc = context.getEm().getFirstComponent(entityID, RenderComponent.class);
		pc = context.getEm().getFirstComponent(entityID, PositionComponent.class);
		mc = context.getEm().getFirstComponent(entityID, MoveComponent.class);
		Rectangle baseBox = bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize());
		
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
			move(entityID, deltaTime);
			
		}else if(a.getActionName()=="attack"){//TODO: set pathfinding on another process

			long timeNow = System.nanoTime();
			
			RenderComponent targetRC = context.getEm().getFirstComponent(a.getTarget(), RenderComponent.class);
			
			int startX = (int) (context.getCamera().getX() - baseBox.getX())*-1/context.GRAPH_DIVISOR;
			int startY =  (int) (context.getCamera().getY() -baseBox.getY())*-1/context.GRAPH_DIVISOR; 
			int endX =  (int) (context.getCamera().getX() - targetRC.getCalculatedBaseBox().getX())*-1/context.GRAPH_DIVISOR;
			int endY = (int) (context.getCamera().getY() - targetRC.getCalculatedBaseBox().getY())*-1/context.GRAPH_DIVISOR;
			
			
			if(timeNow - before >= Engine.SECOND*1/5) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				currentStep = 0;
				
				
				AStar as = new AStar();
				pathAstar = as.calculatePath(new Vec2i(startX, startY), new Vec2i(endX, endY-3), context.getPointOfViewCollisionGraph(baseBox));
			}
			
			
			if(  ((pc.getPosition().x - pc.getPreviousPosition().x >context.GRAPH_DIVISOR  || pc.getPosition().y - pc.getPreviousPosition().y >context.GRAPH_DIVISOR) 
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
			
			move(entityID, deltaTime);
		}
		
	}
	
	private void move(long entityID, float deltaTime) {
			
		float xMove = 0;
		float yMove = 0;
		org.jbox2d.common.Vec2  speed = new  org.jbox2d.common.Vec2(0, 0);
		
		if(directions[GameObject.TOP]) {
			yMove = -mc.getVelocity()*deltaTime;
			speed.y = -mc.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			rc.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.BOTTOM]) {
			yMove = mc.getVelocity()*deltaTime;
			speed.y = mc.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			rc.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.RIGHT]) {
			xMove = mc.getVelocity()*deltaTime;
			speed.x = mc.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			rc.setOrientation(faceRight);
			rc.getAnimations().changeStateTo("walking");
		}
		if(directions[GameObject.LEFT]) {
			xMove = -mc.getVelocity()*deltaTime;
			speed.x = -mc.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
			rc.setOrientation(faceLeft);
			rc.getAnimations().changeStateTo("walking");
		}
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			rc.getAnimations().changeStateTo("idle_1");
		}
		bc.body.setLinearVelocity(speed);
	}

	@Override
	public void renderDebug() {
		
		SightComponent sm = gameContext.getEm().getFirstComponent(lastEntityID, SightComponent.class);
		RenderComponent rc = gameContext.getEm().getFirstComponent(lastEntityID, RenderComponent.class);
		Rectangle r = sm.calculateSightView(rc.getRenderPosition());
		//ResourceManager.getSelf().getCubeRenderer().render(new Vec2(r.x, r.y), new Vec2(r.width,r.height), 0, new Vec3(1,1,1));
		
		if(pathAstar!=null) {
			
			for(Anode state: pathAstar) {
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2(gameContext.getCamera().getX() + state.pos.x*gameContext.GRAPH_DIVISOR, gameContext.getCamera().getY() + state.pos.y*gameContext.GRAPH_DIVISOR), new Vec2(8,8), 0, new Vec3(0,0,0));
				
			}
		}
	}



}
