package engine.entity.system;

import demo.Game;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.utilities.MathExt;
import glm.vec._2.Vec2;

public class MoveSystem extends ComponentSystem {

	private org.jbox2d.common.Vec2 speed = new org.jbox2d.common.Vec2(0, 0);

	public MoveSystem(Game context) {
		super(context);

	}

	@Override
	public void update(float dt) {

		for (Entity e : Game.getSelf().getEntitiesOnScreen()) {

			MoveComponent mc = (MoveComponent) context.getEm().getFirstComponent(e, MoveComponent.class);
			if (mc == null)
				continue;
			BasicComponent pc = (BasicComponent) context.getEm().getFirstComponent(e, BasicComponent.class);
			RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(e, RenderComponent.class);
			pc.setPreviousPosition(pc.getPosition());

			speed.x = mc.getVelocity();
			speed.y = mc.getVelocity();

			if (mc.getDirection().x != 0 || mc.getDirection().y != 0) {
				mc.setDirection(mc.getDirection().normalize());
				speed.x *= mc.getDirection().x * mc.getDirection().x * MathExt.sign(mc.getDirection().x);
				speed.y *= mc.getDirection().y * mc.getDirection().y * MathExt.sign(mc.getDirection().y);
			} else {
				speed.x = 0;
				speed.y = 0;
			}

			if (context.getEm().getFirstComponent(e, BodyComponent.class) == null) {
				speed.x = speed.x / Engine.TARGET_UPDATES;
				speed.y = speed.y / Engine.TARGET_UPDATES;

				pc.getPosition().x += speed.x;
				pc.getPosition().y += speed.y;
			} else {
				BodyComponent bc = (BodyComponent) context.getEm().getFirstComponent(e, BodyComponent.class);
				if (bc.body == null)
					continue;

				Vec2 box_2d_speed = PhysicsEngine.convertPixelsToMeters(speed.x, speed.y);
				bc.body.setLinearVelocity(new org.jbox2d.common.Vec2(box_2d_speed.x, box_2d_speed.y));

				Vec2 pixelsPos = PhysicsEngine.convertMetersToPixels(bc.body.getPosition().x - bc.getB2Size().x / 2,
						bc.body.getPosition().y - bc.getB2Size().y / 2);
				Vec2 pos = bc.calculatePosition(pixelsPos, rc.getSize());
				pc.setPosition(pos);
			}
		}
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
		for (Entity e : context.getEm().getAllEntitiesWithComponent(MoveComponent.class)) {
			RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(e, RenderComponent.class);
			BasicComponent pc = (BasicComponent) context.getEm().getFirstComponent(e, BasicComponent.class);

			rc.setRenderPosition(pc.getPosition().x * alpha + pc.getPosition().x * (1f - alpha),
					pc.getPosition().y * alpha + pc.getPosition().y * (1f - alpha));
		}
	}

}
