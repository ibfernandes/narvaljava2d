package engine.controllers;

import demo.Game;

public abstract class Controller {
	public abstract void update(float deltaTime, long entityID, Game context);
	public abstract void renderDebug();
}
