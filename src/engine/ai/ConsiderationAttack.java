package engine.ai;

import engine.logic.GameObject;
import gameStates.Game;

public class ConsiderationAttack implements Consideration{
	private Action a = new Action("attack");
	
	@Override
	public float evaluate(GameObject obj, Game game) {
		for(GameObject o: game.getFinalLayer()) {
			if(obj!=o && o.getGroup()!=null && obj.getGroup()!=o.getGroup() && o.getGroup().equals("player") && obj.getSightBox().intersects(o.getSightBox())) {
				a.setTarget(o);
				return 0.2f;
			}
		}
		return 0;
	}

	@Override
	public Action getAction() {
		return a;
	}


}
