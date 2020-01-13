package engine.entity.system;

import demo.Game;
import engine.entity.Entity;
import engine.entity.component.ControllerComponent;

public class ControllerSystem extends ComponentSystem {

	public ControllerSystem(Game context) {
		super(context);

	}

	@Override
	public void update(float dt) {
		for (Entity e : Game.getSelf().getEntitiesOnScreen()) {
			ControllerComponent bc = (ControllerComponent) Game.getSelf().getEm().getFirstComponent(e,
					ControllerComponent.class);
			if (bc == null)
				continue;
			bc.getController().update(dt, bc.getEntityID(), context);
		}
	}

	@Override
	public void render() {

		for (Entity e : Game.getSelf().getEntitiesOnScreen()) {
			ControllerComponent bc = (ControllerComponent) Game.getSelf().getEm().getFirstComponent(e,
					ControllerComponent.class);
			if (bc == null)
				continue;
//			bc.getController().renderDebug();
		}

	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
