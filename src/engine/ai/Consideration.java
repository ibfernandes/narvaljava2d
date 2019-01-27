package engine.ai;

import engine.entity.EntityManager;

public interface Consideration {
	float evaluate(long EntityID, EntityManager game);
	Action getAction();
}
