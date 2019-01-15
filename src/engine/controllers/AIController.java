package engine.controllers;

import java.util.ArrayList;
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
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;
import engine.geometry.Segment;
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
	private BasicComponent pc;
	private MoveComponent mc;
	
	private Game gameContext;
	private long lastEntityID;
	
	public static final int TICKS_PER_SECOND = 4;
	
	private ArrayList<Segment> segments = new ArrayList<>();
	private ArrayList<Integer> direction = new ArrayList<>();
	
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
		pc = context.getEm().getFirstComponent(entityID, BasicComponent.class);
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
			
			
			float aa = targetRC.getRenderPosition().x - rc.getRenderPosition().x;
			float bb = targetRC.getRenderPosition().y - rc.getRenderPosition().y;
			float dist = (float) Math.sqrt(aa*aa + bb*bb);
			/*if(dist<200) {
				bc.body.setLinearVelocity(new  org.jbox2d.common.Vec2(0, 0));
				return;
			}*/
			
			int endX = (int) (context.getCamera().getX() - baseBox.getX())*-1/context.GRAPH_DIVISOR;
			int endY =  (int) (context.getCamera().getY() -baseBox.getY())*-1/context.GRAPH_DIVISOR; 
			int startX =  (int) (context.getCamera().getX() - targetRC.getCalculatedBaseBox().getX())*-1/context.GRAPH_DIVISOR;
			int startY = (int) (context.getCamera().getY() - targetRC.getCalculatedBaseBox().getY())*-1/context.GRAPH_DIVISOR;
			// Calcula o caminho do target até o agente, pois o retorno do calculatePath é invertido, de forma que o caminho final
			//recebido fica do agente até o target.
			
			if(timeNow - before >= Engine.SECOND*1/TICKS_PER_SECOND) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				currentStep = 0;
				
				
				AStar as = new AStar();
				pathAstar = as.calculatePath(new Vec2i(startX, startY-3), new Vec2i(endX, endY), context.getPointOfViewCollisionGraph(baseBox));
				
				segments = new ArrayList<>();
				direction = new ArrayList<>();
				
				int currentDirection = -1;
				int actualDirection = -1;
				int indexStart = 0;
				deslocation = 0;
				currentSegmentIndex = 0;
				
				if(pathAstar!=null) {
					if(pathAstar.size()>=2) {
						
						currentDirection = getGridSide(pathAstar.get(0), pathAstar.get(1));
						for(int x = 2; x<pathAstar.size(); x++) {
							
							actualDirection = getGridSide(pathAstar.get(x-1), pathAstar.get(x));
							
							if(currentDirection != actualDirection) { // signifca que mudamos o sentido do movimento
								Segment s = new Segment(
										Game.getSelf().getCamera().getX()+pathAstar.get(indexStart).pos.x*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getY()+pathAstar.get(indexStart).pos.y*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getX()+pathAstar.get(x).pos.x*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getY()+pathAstar.get(x).pos.y*context.GRAPH_DIVISOR
										);
								s.setLength((float) Math.sqrt(
										Math.pow(
												Game.getSelf().getCamera().getX()+pathAstar.get(indexStart).pos.x*context.GRAPH_DIVISOR
												- Game.getSelf().getCamera().getX()+pathAstar.get(x).pos.x *context.GRAPH_DIVISOR, 2) 
										+ 
										Math.pow(
												Game.getSelf().getCamera().getY()+pathAstar.get(indexStart).pos.y*context.GRAPH_DIVISOR
												- Game.getSelf().getCamera().getY()+pathAstar.get(x).pos.y *context.GRAPH_DIVISOR, 2))
										);
								
								segments.add(s);
								
								direction.add(currentDirection);
								currentDirection = actualDirection;
								indexStart = x;
							}
							
							if(x==pathAstar.size()-1 && currentDirection == actualDirection) { //Estou na última iteração e não teve uma mudança de direção, também adiciona essa reta
								Segment s = new Segment(
										Game.getSelf().getCamera().getX()+pathAstar.get(indexStart).pos.x*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getY()+pathAstar.get(indexStart).pos.y*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getX()+pathAstar.get(x).pos.x*context.GRAPH_DIVISOR,
										Game.getSelf().getCamera().getY()+pathAstar.get(x).pos.y*context.GRAPH_DIVISOR
										);
								s.setLength((float) Math.sqrt(
										Math.pow(
												Game.getSelf().getCamera().getX()+pathAstar.get(indexStart).pos.x*context.GRAPH_DIVISOR
												- Game.getSelf().getCamera().getX()+pathAstar.get(x).pos.x *context.GRAPH_DIVISOR, 2) 
										+ 
										Math.pow(
												Game.getSelf().getCamera().getY()+pathAstar.get(indexStart).pos.y*context.GRAPH_DIVISOR
												- Game.getSelf().getCamera().getY()+pathAstar.get(x).pos.y *context.GRAPH_DIVISOR, 2))
										);
								direction.add(currentDirection);
								segments.add(s);
							}
						}
					}
				}
			}
			
			
			/*if(  ((pc.getPosition().x - pc.getPreviousPosition().x >context.GRAPH_DIVISOR  || pc.getPosition().y - pc.getPreviousPosition().y >context.GRAPH_DIVISOR) 
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
				
			}*/
			
			
			
			if(!segments.isEmpty()) {
				moveAlongLine(pc,bc, targetRC.getRenderPosition(), 1);
			}

			//move(entityID, deltaTime);
		}
		
	}
	
	float deslocation = 0;
	int currentSegmentIndex = 0;
	boolean hasReachedEnd = false;
	Vec2 endPoint = null;
	
	private void moveAlongLine(BasicComponent object, BodyComponent bc, Vec2 targetRC, float deltaTime) {
		float moveDistance =mc.getVelocity()*deltaTime;
		float needToWalk = moveDistance;
		
		
		Segment currentSegment = segments.get(currentSegmentIndex);
		hasReachedEnd = false;
		
		while(needToWalk>currentSegment.getLength()) {
			needToWalk -= currentSegment.getLength();
			currentSegmentIndex++;
			
			if(currentSegmentIndex>segments.size()-1) {
				endPoint = currentSegment.getPointAtNormalized(1f);
				hasReachedEnd = true;
				currentSegmentIndex--;
				break;
			}
				
			currentSegment = segments.get(currentSegmentIndex);
		}
		
		if(!hasReachedEnd) {
			float delta= needToWalk/currentSegment.getLength(); 
			endPoint = currentSegment.getPointAtNormalized(delta);
			deslocation = delta * currentSegment.getLength();
			currentSegment.setStart(currentSegment.getPointAtNormalized(delta));
			currentSegment.setLength( currentSegment.getLength() - deslocation);
		}

		
		//endPoint.x = endPoint.x + context.getCamera().getX();
		//endPoint.y = endPoint.y + context.getCamera().getY() - object.getSize().y + object.getBaseBox().height;
		endPoint.x = endPoint.x ;
		endPoint.y = endPoint.y;
	
		
		org.jbox2d.common.Vec2 direction = new org.jbox2d.common.Vec2(0,0);
		direction.x = endPoint.x -bc.getCalculatedBaseBox().getPos().x ;
		direction.y = endPoint.y -bc.getCalculatedBaseBox().getPos().y ;


		direction.normalize();
	
		
		direction.x = (mc.velocity/Engine.getSelf().TARGET_UPDATES)*direction.x;
		direction.y = (mc.velocity/Engine.getSelf().TARGET_UPDATES)*direction.y;

		bc.body.setLinearVelocity(direction);
		//object.setPosition(endPoint);
	}
	
	public int getGridSide(Anode a, Anode b) {
		boolean direction[] = new boolean[4];
		
		if(a.pos.x>b.pos.x)
			direction[GameObject.RIGHT] = true;
		else if(a.pos.x<b.pos.x)
			direction[GameObject.LEFT] = true;
		if(a.pos.y> b.pos.y)
			direction[GameObject.BOTTOM] = true;
		else if(a.pos.y<b.pos.y) 
			direction[GameObject.TOP] = true;
		
		if(direction[GameObject.RIGHT] && direction[GameObject.TOP])
			return GameObject.TOP_DIAGONAL_RIGHT;
		
		if(direction[GameObject.LEFT] && direction[GameObject.TOP])
			return GameObject.TOP_DIAGONAL_LEFT;
		
		if(direction[GameObject.RIGHT] && direction[GameObject.BOTTOM])
			return GameObject.BOTTOM_DIAGONAL_RIGHT;
		
		if(direction[GameObject.LEFT] && direction[GameObject.BOTTOM])
			return GameObject.BOTTOM_DIAGONAL_LEFT;
		
		if(direction[GameObject.RIGHT])
			return GameObject.RIGHT;
		
		if(direction[GameObject.LEFT])
			return GameObject.LEFT;
		
		if(direction[GameObject.BOTTOM])
			return GameObject.BOTTOM;
		
		if(direction[GameObject.TOP])
			return GameObject.TOP;
		
		return -1;
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
		//bc.body.setLinearVelocity(speed);
		
	}

	@Override
	public void renderDebug() {
		
		SightComponent sm = Game.getSelf().getEm().getFirstComponent(lastEntityID, SightComponent.class);
		RenderComponent rc =  Game.getSelf().getEm().getFirstComponent(lastEntityID, RenderComponent.class);
		Rectangle r = sm.calculateSightView(rc.getRenderPosition());
		ResourceManager.getSelf().getCubeRenderer().render(new Vec2(r.x, r.y), new Vec2(r.width,r.height), 0, new Vec3(1,1,1));
		
		if(pathAstar!=null) {
			ResourceManager.getSelf().getCubeRenderer().render(
					new Vec2(endPoint), new Vec2(8,8), 0, new Vec3(1,0,0));	
			ResourceManager.getSelf().getCubeRenderer().render(
					new Vec2(bc.getCalculatedBaseBox().getPos()), new Vec2(8,8), 0, new Vec3(0,0,1));	
			
			
			for(Anode state: pathAstar) {
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2(gameContext.getCamera().getX() + state.pos.x*gameContext.GRAPH_DIVISOR, gameContext.getCamera().getY() + state.pos.y*gameContext.GRAPH_DIVISOR), new Vec2(8,8), 0, new Vec3(0,0,0));		
			}
			for(Segment s: segments) {
				ResourceManager.getSelf().getCubeRenderer().render(
						new Vec2(s.getStart()), new Vec2(8,8), 0, new Vec3(1,0,0));		
				ResourceManager.getSelf().getCubeRenderer().render(
						new Vec2(s.getEnd()), new Vec2(8,8), 0, new Vec3(0,0,1));		
			}
		}
	}



}
