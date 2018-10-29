package engine.ai;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.logic.GameObject;
import gameStates.Game;

public interface Consideration {
	float evaluate(long EntityID, EntityManager game);
	Action getAction();
}
