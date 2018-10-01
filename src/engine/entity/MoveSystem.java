package engine.entity;

import java.util.Arrays;

import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import glm.vec._2.Vec2;

public class MoveSystem extends ComponentSystem{
	
	private org.jbox2d.common.Vec2 speed = new org.jbox2d.common.Vec2(0,0);
	
	public MoveSystem(EntityManager em) {
		super(em);
	}

	@Override
	public void update(float dt) {
		for(Entity e: em.getAllEntitiesWithComponent(MoveComponent.class)) {
			
			for(Component c: em.getComponent(e,MoveComponent.class)) {
				MoveComponent mc = (MoveComponent) c;
				PositionComponent pc= (PositionComponent) em.getFirstComponent(e, PositionComponent.class);
				RenderComponent rc = (RenderComponent) em.getFirstComponent(e, RenderComponent.class);
				
				if(pc==null) {
					System.err.println("Missing PositionComponent");
					return;
				}
				
				pc.setPreviousPosition(pc.getPosition());
				
				speed.x = (mc.speed/Engine.getSelf().TARGET_UPDATES)*mc.direction.x;
				speed.y = (mc.speed/Engine.getSelf().TARGET_UPDATES)*mc.direction.y;
				
				//if it doesn't have a physics body
				if(em.getComponent(e,BodyComponent.class).isEmpty()) {
					pc.getPosition().x += speed.x;
					pc.getPosition().y += speed.y;
				}else {
					for(Component c2: em.getComponent(e,BodyComponent.class)) {
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
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
		for(Entity e: em.getAllEntitiesWithComponent(MoveComponent.class)) {
			RenderComponent rc = (RenderComponent) em.getFirstComponent(e, RenderComponent.class);
			PositionComponent pc= (PositionComponent) em.getFirstComponent(e, PositionComponent.class);
			
			rc.setRenderPosition(pc.getPosition().x*alpha + pc.getPosition().x * (1f - alpha), pc.getPosition().y*alpha + pc.getPosition().y * (1f - alpha));
		}
	}

}
