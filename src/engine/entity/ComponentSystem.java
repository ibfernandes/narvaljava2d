package engine.entity;

import gameStates.Game;

public abstract class ComponentSystem {
	protected Game context;
	
	public ComponentSystem(Game context) {
		this.context = context;
	}
	
	public abstract void update(float dt);
	public abstract void variableUpdate(float alpha);
	public abstract void render();
}
