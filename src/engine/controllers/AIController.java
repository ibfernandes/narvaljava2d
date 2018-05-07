package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.ai.AEstrela;
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
			
			int startX = (int) (context.getCamera().getX() - object.getBaseBox().getX())*-1/context.graphDivisor;
			int startY =  (int) (context.getCamera().getY() - object.getBaseBox().getY())*-1/context.graphDivisor; 
			int endX =  (int) (context.getCamera().getX() - a.getTarget().getBaseBox().getX())*-1/context.graphDivisor;
			int endY = (int) (context.getCamera().getY() - a.getTarget().getBaseBox().getY())*-1/context.graphDivisor;
			
			if(timeNow - before >= Engine.SECOND/10) {
				Arrays.fill(directions, false);
				before = System.nanoTime();
				currentStep = 0;
				
				AEstrela as = new AEstrela();
				pathAstar = as.calculatePath(new Vec2i(startX, startY), new Vec2i(endX, endY-4), context.generateGraph(object));
				
				
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
				
				//AStar as = new AStar();
				//List<Vec2i> pathAstar = as.findPath(new Vec2i(startX, startY), new Vec2i(endX, endY), 128/context.divisor, 20/context.divisor, context.generateGraph(object)); 
	
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

		if(pathAstar!=null) {
			for(Anode state: pathAstar) {
			//	ResourceManager.getSelf().getCubeRenderer().render(new Vec2(context.getCamera().getX()*-1 + state.pos.x*context.graphDivisor, context.getCamera().getY()*-1 + state.pos.y*context.graphDivisor), new Vec2(8,8), 0, new Vec3(0,1,0));
			}
		}
	}


}
