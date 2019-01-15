package engine.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import engine.ai.AEstrela;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationRace;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.DStarLite;
import engine.ai.State;
import engine.engine.Engine;
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

public class ImprovedAIController extends Controller{
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4]; 
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private ConsiderationTree ct = new ConsiderationTree();
	private long before = System.nanoTime();
	private List<State> path;
	private List<Anode> pathAstar;
	private Game context;
	private int currentStep;
	private Vec2 currentPoint;
	private ArrayList<Segment> segments = new ArrayList<>();;
	private ArrayList<Integer> direction = new ArrayList<>();;
	
	
	private float stepSize = 0.1f;
	float actualNodeindex;
	float nextNodeindex;
	Vec2 nextNode;
	
	public ImprovedAIController() {
		actualNodeindex = 0;
		nextNodeindex = stepSize;

	}
	
	/*
	 * returns on which side of a the node b is.
	 */
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
	
	
	Vec2 cameraPreviousPosition; 
	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		this.context = context;
		Action a = ct.calculateAction(object, context);
		
		if(nextNode == null) { //calculates the first nextNode (1)
			nextNode = context.getSplineRealCoord(nextNodeindex, true);
		}
		
		if(a.getActionName()=="race"){//TODO: set pathfinding on another process
			
			long timeNow = System.nanoTime();
			
			/*int startX = (int) (context.getCamera().getX() - object.getBaseBox().x)*-1/context.graphDivisor;
			int startY =  (int) (context.getCamera().getY() - object.getBaseBox().y)*-1/context.graphDivisor; 
			int endX =  (int) (context.getCamera().getX() - nextNode.x)*-1/context.graphDivisor;
			int endY = (int) (context.getCamera().getY() - nextNode.y)*-1/context.graphDivisor;*/
			int startX = (int) (object.getBaseBox().x)/context.graphDivisor;
			int startY =  (int) (object.getBaseBox().y)/context.graphDivisor; 
			int endX =  (int) (nextNode.x)/context.graphDivisor;
			int endY = (int) (nextNode.y)/context.graphDivisor;
			
			//System.out.println(startX);
			//System.out.println(endX);
			
			Arrays.fill(directions, false);
			
			if(timeNow - before >= Engine.SECOND*1 || hasReachedEnd) {
				
				cameraPreviousPosition = context.getCamera().getPos();
				
				before = System.nanoTime();
				currentStep = 0;
				
				AEstrela as = new AEstrela();
				pathAstar = as.calculatePath(new Vec2i(endX, endY-4), new Vec2i(startX, startY), context.generateGraph(object)); //TODO: calculate camera as an offest, not as actual pos
				segments = new ArrayList<>();
				direction = new ArrayList<>();
				
				int currentDirection = -1;
				int actualDirection = -1;
				int indexStart = 0;
				deslocation = 0;
				currentSegmentIndex = 0;
				
				if(pathAstar!=null)
				for(int x = 1; x<pathAstar.size(); x++) {
					
					actualDirection = getGridSide(pathAstar.get(x-1), pathAstar.get(x));
					
					if(currentDirection != actualDirection) { // signifca que mudamos o sentido do movimento
						Segment s = new Segment(
								pathAstar.get(indexStart).pos.x*context.graphDivisor, pathAstar.get(indexStart).pos.y*context.graphDivisor,
								pathAstar.get(x).pos.x*context.graphDivisor, pathAstar.get(x).pos.y*context.graphDivisor
								);
						s.setLength((float) Math.sqrt(
								Math.pow(pathAstar.get(indexStart).pos.x*context.graphDivisor - pathAstar.get(x).pos.x *context.graphDivisor, 2) 
								+ 
								Math.pow(pathAstar.get(indexStart).pos.y*context.graphDivisor - pathAstar.get(x).pos.y *context.graphDivisor, 2))
								);
						
						segments.add(s);
						
						direction.add(currentDirection);
						currentDirection = actualDirection;
						indexStart = x;
					}
					
					if(x==pathAstar.size()-1 && currentDirection == actualDirection) { //Estou na última iteração e não teve uma mudança de direção, também adiciona essa reta
						Segment s = new Segment(
								pathAstar.get(indexStart).pos.x*context.graphDivisor, pathAstar.get(indexStart).pos.y*context.graphDivisor,
								pathAstar.get(x).pos.x*context.graphDivisor, pathAstar.get(x).pos.y*context.graphDivisor
								);
						s.setLength((float) Math.sqrt(
								Math.pow(pathAstar.get(indexStart).pos.x*context.graphDivisor - pathAstar.get(x).pos.x *context.graphDivisor, 2) 
								+ 
								Math.pow(pathAstar.get(indexStart).pos.y*context.graphDivisor - pathAstar.get(x).pos.y *context.graphDivisor, 2))
								);
						direction.add(currentDirection);
						segments.add(s);
					}
				}
			}

			if(Math.sqrt( Math.pow(nextNode.y - object.getPosition().y, 2) + Math.pow( nextNode.x - object.getPosition().x, 2) )<=300) {
				actualNodeindex = nextNodeindex;
				nextNodeindex = (actualNodeindex+stepSize) % ((float)context.trackPoints.size());

				nextNode = context.getSplineRealCoord(nextNodeindex, true);
				nextNode.y += r.nextFloat()*300f * ((r.nextFloat()<50)? 1 : -1);
			}
			
			//move(object, deltaTime);
			if(!segments.isEmpty()) {
				moveAlongLine(object, deltaTime);
			}
		}
		
	}
	
	float deslocation = 0;
	int currentSegmentIndex = 0;
	boolean hasReachedEnd = false;
	
	private void moveAlongLine(GameObject object, float deltaTime) {
		
		float moveDistance = object.getVelocity()*deltaTime;
		float needToWalk = moveDistance;
		Vec2 endPoint = null;
		
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
		endPoint.y = endPoint.y - object.getSize().y + object.getBaseBox().height;
		
		object.moveDirectlyTo(endPoint.x, endPoint.y );//TODO: problem, i not calculating the previous position
		changeAnimation(object, currentSegmentIndex);
	}
	
	public int invertDirection(int direction) {
		if(direction==GameObject.TOP) 
			return GameObject.BOTTOM;
		if(direction==GameObject.BOTTOM) 
			return GameObject.TOP;
		if(direction==GameObject.RIGHT) 
			return GameObject.LEFT;
		if(direction==GameObject.LEFT) 
			return GameObject.RIGHT;
		
		if(direction==GameObject.TOP_DIAGONAL_RIGHT) 
			return GameObject.BOTTOM_DIAGONAL_LEFT;
		if(direction==GameObject.BOTTOM_DIAGONAL_LEFT) 
			return GameObject.TOP_DIAGONAL_RIGHT;
		if(direction==GameObject.BOTTOM_DIAGONAL_RIGHT) 
			return GameObject.TOP_DIAGONAL_LEFT;
		if(direction==GameObject.TOP_DIAGONAL_LEFT) 
			return GameObject.BOTTOM_DIAGONAL_RIGHT;
		
		return -1;
	}
	
	/**
	 * A
	 * @param object
	 * @param index
	 */
	private void changeAnimation(GameObject object, int index) {
		int dir = direction.get(index);
		int direction = invertDirection(dir);
		
		if(direction==GameObject.TOP) {
			object.getAnimations().changeStateTo("car_cima");
		}
		if(direction==GameObject.BOTTOM) {
			object.getAnimations().changeStateTo("car_baixo");
		}
		if(direction==GameObject.RIGHT) {
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("car_lado");
		}
		if(direction==GameObject.LEFT) {
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_lado");
		}
		
		if(direction==GameObject.TOP_DIAGONAL_RIGHT) {
			object.getAnimations().changeStateTo("car_diagonalcima");
		}
		if(direction==GameObject.TOP_DIAGONAL_LEFT) {
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_diagonalcima");
		}
		
		if(direction==GameObject.BOTTOM_DIAGONAL_RIGHT) {
			object.getAnimations().changeStateTo("car_diagonalbaixo");
		}
		
		if(direction==GameObject.BOTTOM_DIAGONAL_LEFT) {
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_diagonalbaixo");
		}
	}
	
	private void move(GameObject object, float deltaTime) {
			
		float xMove = 0;
		float yMove = 0;
		
		if(directions[GameObject.TOP]) {
			yMove = -object.getVelocity()*deltaTime;
			object.getAnimations().changeStateTo("car_cima");
		}
		if(directions[GameObject.BOTTOM]) {
			yMove = object.getVelocity()*deltaTime;
			object.getAnimations().changeStateTo("car_baixo");
		}
		if(directions[GameObject.RIGHT]) {
			xMove = object.getVelocity()*deltaTime;
			object.setOrientation(faceRight);
			object.getAnimations().changeStateTo("car_lado");
		}
		if(directions[GameObject.LEFT]) {
			xMove = -object.getVelocity()*deltaTime;
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_lado");
		}
		
		if(directions[GameObject.RIGHT] && directions[GameObject.TOP]) {
			object.getAnimations().changeStateTo("car_diagonalcima");
		}
		if(directions[GameObject.LEFT] && directions[GameObject.TOP]) {
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_diagonalcima");
		}
		
		if(directions[GameObject.RIGHT] && directions[GameObject.BOTTOM]) {
			object.getAnimations().changeStateTo("car_diagonalbaixo");
		}
		
		if(directions[GameObject.LEFT] && directions[GameObject.BOTTOM]) {
			object.setOrientation(faceLeft);
			object.getAnimations().changeStateTo("car_diagonalbaixo");
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
				ResourceManager.getSelf().getCubeRenderer().render(new Vec2( state.pos.x*context.graphDivisor, state.pos.y*context.graphDivisor), new Vec2(8,8), 0, new Vec3(0,1,0));
			}
		}
	}

	@Override
	public void update(float deltaTime, long entityID, Game context) {
	}


}
