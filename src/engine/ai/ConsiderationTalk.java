package engine.ai;

import engine.controllers.KeyBoardBindings;
import engine.engine.Engine;
import engine.logic.GameObject;
import gameStates.GSM;
import gameStates.Game;

public class ConsiderationTalk implements Consideration{
	private Action a = new Action("talk");
	
	@Override
	public float evaluate(GameObject obj, Game context) {
		
		for(GameObject o: context.getFinalLayer()) {
			if(GSM.getSelf().getKeyboard().isKeyPressed(KeyBoardBindings.INTERACTION_KEY) && o.getGroup()!=null && o.getGroup().equals("player") && obj.getInterationBox().intersects(o.getInterationBox())) {
				a.setTarget(o);
				return 1f;
			}
		}
		return 0;
		
	}

	@Override
	public Action getAction() {
		return a;
	}


}
