package engine.ai;

import engine.logic.GameObject;
import gameStates.Game;

public class ConsiderationWander implements Consideration{
	private Action a = new Action("wander");
	
	@Override
	public float evaluate(GameObject obj, Game game) {
		 
		return 0.1f;
		
	}

	@Override
	public Action getAction() {
		return a;
	}


}
