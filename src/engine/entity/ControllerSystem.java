package engine.entity;

import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.ControllerComponent;

public class ControllerSystem extends ComponentSystem{

	public ControllerSystem(EntityManager em) {
		super(em);
		
	}

	@Override
	public void update(float dt) {
		for(Component c: em.getAllComponents(ControllerComponent.class)) {
				ControllerComponent bc = (ControllerComponent) c;
				bc.controller.update(dt, bc.getParentEntityID(), em);
		}
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
