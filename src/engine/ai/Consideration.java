package engine.ai;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.logic.GameObject;
import gameStates.Game;

public interface Consideration {
	float evaluate(Entity obj, EntityManager game);
	Action getAction();
}
