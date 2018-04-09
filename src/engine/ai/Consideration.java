package engine.ai;

import engine.logic.GameObject;
import gameStates.Game;

public interface Consideration {
	float evaluate(GameObject obj, Game game);
	Action getAction();
}
