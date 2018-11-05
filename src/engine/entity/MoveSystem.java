package engine.entity;

import java.util.Arrays;

import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.HealthComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import gameStates.Game;
import glm.vec._2.Vec2;

public class MoveSystem extends ComponentSystem{
	
	public MoveSystem(Game context) {
		super(context);
		
	}

	private org.jbox2d.common.Vec2 speed = new org.jbox2d.common.Vec2(0,0);

	@Override
	public void update(float dt) {
		
		for(Component c: context.getEm().getAllComponents(MoveComponent.class)) {
			
			MoveComponent mc = (MoveComponent) c;
			PositionComponent pc= (PositionComponent) context.getEm().getFirstComponent(c.getEntityID(), PositionComponent.class);
			RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(c.getEntityID(), RenderComponent.class);
			
			if(pc==null) {
				System.err.println("Missing PositionComponent");
				return;
			}
			
			pc.setPreviousPosition(pc.getPosition());
			
			speed.x = (mc.velocity/Engine.getSelf().TARGET_UPDATES)*mc.direction.x;
			speed.y = (mc.velocity/Engine.getSelf().TARGET_UPDATES)*mc.direction.y;
			
			//if it doesn't have a physics body
			if(context.getEm().getComponent(c.getEntityID(),BodyComponent.class).isEmpty()) {
				pc.getPosition().x += speed.x;
				pc.getPosition().y += speed.y;
			}else {
				for(Component c2: context.getEm().getComponent(c.getEntityID(),BodyComponent.class)) {
					BodyComponent bc = (BodyComponent) c2;
					if(bc.body==null)
						continue;
					
					
				
					bc.body.setLinearVelocity(speed);
					Vec2 pixelsPos = PhysicsEngine.convertMetersToPixels(bc.body.getPosition().x, bc.body.getPosition().y);
					Vec2 pos = bc.calculatePosition(pixelsPos, rc.getSize());
					pc.setPosition(pos); //offset up
				}
			}
		}
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
		for(Entity e: context.getEm().getAllEntitiesWithComponent(MoveComponent.class)) {
			RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(e, RenderComponent.class);
			PositionComponent pc= (PositionComponent) context.getEm().getFirstComponent(e, PositionComponent.class);
			
			rc.setRenderPosition(pc.getPosition().x*alpha + pc.getPosition().x * (1f - alpha), pc.getPosition().y*alpha + pc.getPosition().y * (1f - alpha));
		}
	}

}
