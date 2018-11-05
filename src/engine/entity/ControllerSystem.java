package engine.entity;

import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.ControllerComponent;
import gameStates.Game;

public class ControllerSystem extends ComponentSystem{


	public ControllerSystem(Game context) {
		super(context);
		
	}

	@Override
	public void update(float dt) {
		for(Component c: context.getEm().getAllComponents(ControllerComponent.class)) {
				ControllerComponent bc = (ControllerComponent) c;
				bc.controller.update(dt, bc.getEntityID(), context);
		}
	}

	@Override
	public void render() {
		for(Component c: context.getEm().getAllComponents(ControllerComponent.class)) {
			ControllerComponent bc = (ControllerComponent) c;
			bc.controller.renderDebug();
	}
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
