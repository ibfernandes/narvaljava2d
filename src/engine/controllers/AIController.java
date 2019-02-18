package engine.controllers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.lwjgl.glfw.GLFW;

import demo.Game;
import engine.ai.AStar;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;
import engine.geometry.Segment;
import engine.input.JoystickControl;
import engine.renderer.CubeRenderer;
import engine.states.GSM;
import engine.utilities.ArraysExt;
import engine.utilities.Commons;
import engine.utilities.ResourceManager;
import engine.utilities.Timer;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class AIController extends Controller {
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4];
	private Vec2 faceLeft = new Vec2(1, 0);
	private Vec2 faceRight = new Vec2(0, 0);
	private ConsiderationTree ct = new ConsiderationTree();
	private List<Anode> pathAstar;
	private BodyComponent bc;
	private RenderComponent rc;
	private BasicComponent pc;
	private MoveComponent mc;
	private Game gameContext;
	private long lastEntityID;
	private int currentSegmentIndex = 0;
	private boolean hasReachedEnd = false;
	private Vec2 endPoint = null;
	private long previous = System.nanoTime();
	private ArrayList<Segment> segments = new ArrayList<>();
	private ArrayList<Integer> direction = new ArrayList<>();
	private AStar as = new AStar();
	public static final int TICKS_PER_SECOND = 4;

	public AIController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, long thisEntityID, Game context) {
		gameContext = context;
		lastEntityID = thisEntityID;
		Action selectedAction = ct.calculateAction(thisEntityID, context.getEm());

		bc = context.getEm().getFirstComponent(thisEntityID, BodyComponent.class);
		rc = context.getEm().getFirstComponent(thisEntityID, RenderComponent.class);
		pc = context.getEm().getFirstComponent(thisEntityID, BasicComponent.class);
		mc = context.getEm().getFirstComponent(thisEntityID, MoveComponent.class);

		Rectangle baseBox = bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize());
		Vec2 centerPoint = bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize()).getPos();

		if (selectedAction.getActionName() == "wander") {
			pathAstar = null;

			long now = System.nanoTime();

			if ((now - startTime) > Timer.SECOND * 4 * tick + Timer.SECOND) {
				Arrays.fill(directions, false);
				startTime = System.nanoTime();
				int currentMove = r.nextInt(5);
				if (currentMove < 4)
					directions[currentMove] = true;
			}

		} else if (selectedAction.getActionName() == "attack") {

			RenderComponent targetRC = context.getEm().getFirstComponent(selectedAction.getTarget(),
					RenderComponent.class);

			float aa = targetRC.getRenderPosition().x - rc.getRenderPosition().x;
			float bb = targetRC.getRenderPosition().y - rc.getRenderPosition().y;
			float dist = (float) Math.sqrt(aa * aa + bb * bb);

			if (dist < 50) {
				bc.body.setLinearVelocity(new org.jbox2d.common.Vec2(0, 0));
				return;
			}

			// Calcula o caminho do target até o agente, pois o retorno do calculatePath é
			Vec2i endPoint = convertWorldCoordsToNodeCoords(centerPoint.x, centerPoint.y);
			Vec2i startPoint = convertWorldCoordsToNodeCoords(targetRC.calculateBaseBox().getX(),
					targetRC.calculateBaseBox().getY());
			startPoint.y = startPoint.y - 6;

			if (System.nanoTime() - previous >= Timer.SECOND * 1 / TICKS_PER_SECOND) {
				Arrays.fill(directions, false);
				
				as = new AStar();
				pathAstar = as.calculatePath(startPoint, endPoint, context.getPointOfViewCollisionGraph(baseBox));

				segments = new ArrayList<>();
				direction = new ArrayList<>();

				int currentDirection = -1;
				int actualDirection = -1;
				int indexStart = 0;
				currentSegmentIndex = 0;

				if (pathAstar != null) {
					if (pathAstar.size() >= 2) {

						currentDirection = Commons.calculateDirection8way(pathAstar.get(0).pos.x,
								pathAstar.get(0).pos.y, pathAstar.get(1).pos.x, pathAstar.get(1).pos.y);

						for (int x = 2; x < pathAstar.size(); x++) {

							actualDirection = Commons.calculateDirection8way(pathAstar.get(x - 1).pos.x,
									pathAstar.get(x - 1).pos.y, pathAstar.get(x).pos.x, pathAstar.get(x).pos.y);

							// Means that the direction has changed
							if (currentDirection != actualDirection) {
								Vec2 start = convertNodeCoordsToWorldCoords(pathAstar.get(indexStart).pos);
								Vec2 actualX = convertNodeCoordsToWorldCoords(pathAstar.get(x).pos);

								Segment s = new Segment(start.x, start.y, actualX.x, actualX.y);

								s.setLength((float) Math
										.sqrt(Math.pow(start.x - actualX.x, 2) + Math.pow(start.y - actualX.y, 2)));
								segments.add(s);

								direction.add(currentDirection);
								currentDirection = actualDirection;
								indexStart = x;
							}

							// Last iteration with no direction change so it also adds this segment
							if (x == pathAstar.size() - 1 && currentDirection == actualDirection) {
								Vec2 start = convertNodeCoordsToWorldCoords(pathAstar.get(indexStart).pos);
								Vec2 actualX = convertNodeCoordsToWorldCoords(pathAstar.get(x).pos);

								Segment s = new Segment(start.x, start.y, actualX.x, actualX.y);

								s.setLength((float) Math
										.sqrt(Math.pow(start.x - actualX.x, 2) + Math.pow(start.y - actualX.y, 2)));

								direction.add(currentDirection);
								segments.add(s);
							}
						}
					}
				}
				previous = System.nanoTime();
			}

			if (!segments.isEmpty()) {
				moveAlongLine(centerPoint, targetRC.getRenderPosition());
			} else {
				mc.setDirection(new Vec2(0, 0));
			}
		}

	}

	public Vec2i convertWorldCoordsToNodeCoords(float x, float y) {
		return new Vec2i((int) ((Game.getSelf().getCamera().getX() - x) * -1 / Game.GRAPH_DIVISOR),
				(int) ((Game.getSelf().getCamera().getY() - y) * -1 / Game.GRAPH_DIVISOR));
	}

	public Vec2 convertNodeCoordsToWorldCoords(Vec2i node) {
		return new Vec2(Game.getSelf().getCamera().getX() + node.x * Game.GRAPH_DIVISOR,
				Game.getSelf().getCamera().getY() + node.y * Game.GRAPH_DIVISOR);
	}

	private void moveAlongLine(Vec2 baseBoxCenterPoint, Vec2 targetRC) {
		float needToWalk = (mc.getVelocity() / Engine.TARGET_UPDATES) * 1.66f;

		Segment currentSegment = segments.get(currentSegmentIndex);
		hasReachedEnd = false;

		while (needToWalk > currentSegment.getLength()) {
			needToWalk -= currentSegment.getLength();
			currentSegmentIndex++;

			if (currentSegmentIndex > segments.size() - 1) {
				endPoint = currentSegment.getPointAtNormalized(1f);
				hasReachedEnd = true;
				currentSegmentIndex--;
				break;
			}

			currentSegment = segments.get(currentSegmentIndex);
		}

		if (!hasReachedEnd) {
			endPoint = currentSegment.getPointAt(needToWalk);
			currentSegment.setStart(currentSegment.getPointAt(needToWalk));
			currentSegment.setLength(currentSegment.getLength() - needToWalk);
		}

		endPoint = currentSegment.getEnd();

		Vec2 direction = new Vec2(0, 0);
		direction.x = endPoint.x - baseBoxCenterPoint.x;
		direction.y = endPoint.y - baseBoxCenterPoint.y;
		mc.setDirection(direction);
		changeAnimation();
	}
	
	private void changeAnimation() {
		
		if(mc.getDirection().x>0)
			rc.setOrientation(faceRight);
		else if(mc.getDirection().x<0)
			rc.setOrientation(faceLeft);

		if (mc.getDirection().x!=0 || mc.getDirection().y!=0) {
			rc.getAnimations().changeStateTo("walking");
		}else{
			rc.getAnimations().changeStateTo("idle_1");
		}
	}

	@Override
	public void renderDebug() {

		SightComponent sm = Game.getSelf().getEm().getFirstComponent(lastEntityID, SightComponent.class);
		RenderComponent rc = Game.getSelf().getEm().getFirstComponent(lastEntityID, RenderComponent.class);
		Rectangle r = sm.calculateSightView(rc.getRenderPosition());
		//((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(r.x, r.y),
		//		new Vec2(r.width, r.height), 0, new Vec4(1, 1, 1, 0.3f));

		if (pathAstar != null) {

			for (Anode state : pathAstar) {
				((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(
						new Vec2(gameContext.getCamera().getX() + state.pos.x * Game.GRAPH_DIVISOR,
								gameContext.getCamera().getY() + state.pos.y * Game.GRAPH_DIVISOR),
						new Vec2(8, 8), 0, new Vec4(0, 0, 0, 1));
			}
			for (Segment s : segments) {
				ResourceManager.getSelf().getFont("sourcesanspro").render(String.valueOf(s.getLength()), s.getStart().x,
						s.getStart().y, new Vec4(1, 1, 1, 1));
				((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(s.getStart()),
						new Vec2(8, 8), 0, new Vec4(1, 0, 0, 1));
				((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(s.getEnd()),
						new Vec2(8, 8), 0, new Vec4(0, 0, 1, 1));
			}

			((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(endPoint),
					new Vec2(8, 8), 0, new Vec4(1, 1, 0, 1));
		}
		
		//renderNodesExpanded();
	}
	
	private void renderNodesExpanded() {
		if(as!=null) {
			for (Anode state : as.getClosedSet()) {
				((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(
						new Vec2(gameContext.getCamera().getX() + state.pos.x * Game.GRAPH_DIVISOR,
								gameContext.getCamera().getY() + state.pos.y * Game.GRAPH_DIVISOR),
						new Vec2(8, 8), 0, new Vec4(0, 0, 0, 1));
			}
		}
	}

}
