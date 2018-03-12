package engine.controllers;

import engine.logic.GameObject;

public abstract class Controller {
	public abstract void update(float deltaTime, GameObject object);
}
