package engine.controllers;

import java.util.Arrays;
import java.util.Random;

import demo.Game;
import engine.ai.Action;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.utilities.Commons;
import engine.utilities.Timer;
import glm.vec._2.Vec2;

public class WanderController extends Controller {
	private Random r = new Random();
	private long startTime = System.nanoTime();
	private float tick = r.nextFloat();
	private boolean directions[] = new boolean[4];
	private Vec2 faceLeft = new Vec2(1, 0);
	private Vec2 faceRight = new Vec2(0, 0);
	private ConsiderationTree ct = new ConsiderationTree();
	private BodyComponent bc;
	private RenderComponent rc;
	private BasicComponent pc;
	private MoveComponent mc;
	public static final int TICKS_PER_SECOND = 4;
	private Vec2 direction = new Vec2();

	public WanderController() {
		ct.addConsideration(new ConsiderationWander());
		ct.addConsideration(new ConsiderationAttack());
	}

	@Override
	public void update(float deltaTime, long thisEntityID, Game context) {
		Action selectedAction = ct.calculateAction(thisEntityID, context.getEm());

		bc = context.getEm().getFirstComponent(thisEntityID, BodyComponent.class);
		rc = context.getEm().getFirstComponent(thisEntityID, RenderComponent.class);
		pc = context.getEm().getFirstComponent(thisEntityID, BasicComponent.class);
		mc = context.getEm().getFirstComponent(thisEntityID, MoveComponent.class);

		if (selectedAction.getActionName() == "wander") {
			long now = System.nanoTime();

			if ((now - startTime) > Timer.SECOND * 4 * tick + Timer.SECOND) {
				Arrays.fill(directions, false);
				startTime = System.nanoTime();
				changeDirection();
				 changeAnimation();
			}
		} 

	}
	
	private void changeDirection() {
		int dir = r.nextInt(9);
		if(dir==8) {
			direction.x = 0;
			direction.y = 0;
			return;
		}
				
		mc.setDirection(Commons.convertToVector(direction, dir));
	}

	private void changeAnimation() {
		
		if(mc.getDirection().x>0)
			rc.setOrientation(faceRight);
		else if(mc.getDirection().x<0)
			rc.setOrientation(faceLeft);

		if (mc.getDirection().x!=0 || mc.getDirection().y!=0) {
			rc.getAnimations().changeStateTo("walking");
			rc.setSize(rc.getAnimations().getCurrentAnimation().getCurrentFrameSize().mul(4));
		}else{
			rc.getAnimations().changeStateTo("idle_1");
			rc.setSize(rc.getAnimations().getCurrentAnimation().getCurrentFrameSize().mul(4));
		}
	}

	@Override
	public void renderDebug() {

	}

}
