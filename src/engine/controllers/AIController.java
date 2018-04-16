package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.ai.AEstrela;
import engine.ai.AStar;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.DStarLite;
import engine.ai.State;
import engine.engine.Engine;
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
	private DStarLite pathFinder = new DStarLite();
	private long before = System.nanoTime();
	private List<State> path;
	private List<Anode> pathAstar;
	private Game context;
	
	public AIController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		this.context = context;
		Action a = ct.calculateAction(object, context);
		
		if(a.getActionName()=="wander") {
			path = null;
			
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
			
			if(timeNow - before >= Engine.SECOND/6) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				
				int startX = (int) (context.camera.getX()*-1 - object.getBaseBox().getX())*-1/context.graphDivisor;
				int startY =  (int) (context.camera.getY()*-1 - object.getBaseBox().getY())*-1/context.graphDivisor; 
				int endX =  (int) (context.camera.getX()*-1 - a.getTarget().getBaseBox().getX())*-1/context.graphDivisor;
				int endY = (int) (context.camera.getY()*-1 - a.getTarget().getBaseBox().getY())*-1/context.graphDivisor;
				
				AEstrela as = new AEstrela();
				pathAstar = as.calculatePath(new Vec2i(startX, startY), new Vec2i(endX, endY), context.obstacleMap);
				
				
			/*pathFinder.init(
						 startX,
						 startY,
						 endX,
						 endY); //TODO: should use updateGoal and upStart
				pathFinder.initBlockingCells(context.generateGraph(object)); 
				pathFinder.replan();
			
				path = pathFinder.getPath();
				
				
				if(path.size()>1) {
					State s = path.get(1);
			
					if(s.x>startX)
						directions[GameObject.RIGHT] = true;
					else if(s.x<startX)
						directions[GameObject.LEFT] = true;
					if(s.y> startY)
						directions[GameObject.BOTTOM] = true;
					else if(s.y<startY) 
						directions[GameObject.TOP] = true;
					
					for(State state: path) {
						state.x = (int) (context.camera.getX()*-1 + state.x*context.graphDivisor);
						state.y = (int) (context.camera.getY()*-1 + state.y*context.graphDivisor);
					}
				}*/
				
				/*AStar as = new AStar();
				List<Vec2i> pathAstar = as.findPath(new Vec2i(startX, startY), new Vec2i(endX, endY), 128/context.divisor, 20/context.divisor, context.generateGraph(object)); 
				
				if(pathAstar!=null && pathAstar.size()>1) {
					Vec2i s = pathAstar.get(1);
					
					if(s.x>startX)
						directions[GameObject.RIGHT] = true;
					else if(s.x<startX)
						directions[GameObject.LEFT] = true;
					if(s.y> startY)
						directions[GameObject.BOTTOM] = true;
					else if(s.y<startY) 
						directions[GameObject.TOP] = true;
					
					for(State state: path) {
						state.x = (int) (context.camera.getX()*-1 + state.x*context.graphDivisor);
						state.y = (int) (context.camera.getY()*-1 + state.y*context.graphDivisor);
					}
				}*/
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

	@Override
	public void renderDebug() {
		if(path!=null)
			for(State state: path) {
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2(state.x, state.y), new Vec2(8,8), 0, new Vec3(0,1,0));
			}
		if(pathAstar!=null) {
			System.out.println(pathAstar.size());
			for(Anode state: pathAstar) {
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2(context.camera.getX()*-1 + state.pos.x*context.graphDivisor, context.camera.getY()*-1 + state.pos.y*context.graphDivisor), new Vec2(8,8), 0, new Vec3(0,1,0));
			}
		}
	}


}
