package engine.entity.system;

import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.SightComponent;
import gameStates.Game;

public class SightSystem extends ComponentSystem{

	public SightSystem(Game context) {
		super(context);
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void variableUpdate(float alpha) {
	}

	@Override
	public void render() {

	}

}
