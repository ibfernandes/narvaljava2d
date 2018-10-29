package engine.controllers;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.logic.GameObject;
import gameStates.Game;

public abstract class Controller {
	public abstract void update(float deltaTime, long entityID, EntityManager em);
	public abstract void renderDebug();
	//public abstract void handleInteraction();
}
