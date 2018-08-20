package engine.controllers;

import engine.logic.GameObject;
import gameStates.Game;

public abstract class Controller {
	public abstract void update(float deltaTime, GameObject object, Game context);
	public abstract void renderDebug();
	//public abstract void handleInteraction();
}
