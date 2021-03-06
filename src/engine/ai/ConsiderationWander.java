package engine.ai;

import engine.entity.EntityManager;

public class ConsiderationWander implements Consideration {
	private Action a = new Action("wander");

	@Override
	public float evaluate(long entityID, EntityManager game) {
		return 0.1f;
	}

	@Override
	public Action getAction() {
		return a;
	}

}
