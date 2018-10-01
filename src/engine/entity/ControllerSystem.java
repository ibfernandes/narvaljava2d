package engine.entity;

import engine.entity.component.Component;
import engine.entity.component.ControllerComponent;

public class ControllerSystem extends ComponentSystem{

	public ControllerSystem(EntityManager em) {
		super(em);
		
	}

	@Override
	public void update(float dt) {
		for(Entity e: em.getAllEntitiesWithComponent(ControllerComponent.class)) {
			for(Component c: em.getComponent(e,ControllerComponent.class)) {
				ControllerComponent bc = (ControllerComponent) c;
				bc.controller.update(dt, e, em);
			}
		}
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
