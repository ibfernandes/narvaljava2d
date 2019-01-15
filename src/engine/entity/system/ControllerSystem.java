package engine.entity.system;

import engine.entity.Entity;
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
		for(Entity e: Game.getSelf().getEntitiesOnScreen()) {
				ControllerComponent bc = (ControllerComponent) Game.getSelf().getEm().getFirstComponent(e, ControllerComponent.class);
				if(bc==null)
					continue;
				bc.controller.update(dt, bc.getEntityID(), context);
		}
	}

	@Override
	public void render() {
		
		for(Entity e: Game.getSelf().getEntitiesOnScreen()) {
			ControllerComponent bc = (ControllerComponent) Game.getSelf().getEm().getFirstComponent(e, ControllerComponent.class);
			if(bc==null)
				continue;
			bc.controller.renderDebug();
		}

	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
