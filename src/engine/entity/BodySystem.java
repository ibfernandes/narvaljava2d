package engine.entity;

import engine.engine.PhysicsEngine;
import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class BodySystem extends ComponentSystem{

	public BodySystem(EntityManager em) {
		super(em);
		
	}

	@Override
	public void update(float dt) {
		for(Entity e: em.getAllEntitiesWithComponent(BodyComponent.class)) {
			for(Component c: em.getComponent(e,BodyComponent.class)) {
				BodyComponent bc = (BodyComponent) c;
			}
		}
	}

	@Override
	public void render() {
		for(Entity e: em.getAllEntitiesWithComponent(BodyComponent.class)) {
			for(Component c: em.getComponent(e,BodyComponent.class)) {
				BodyComponent bc = (BodyComponent) c;
				RenderComponent rc = (RenderComponent) em.getFirstComponent(e, RenderComponent.class);
				ResourceManager.getSelf().getCubeRenderer().render(bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize()), 0, new Vec3(1,1,0));
			}
		}
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
