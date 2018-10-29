package engine.ai;

import engine.controllers.KeyBoardBindings;
import engine.engine.Engine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.RenderComponent;
import engine.logic.GameObject;
import gameStates.GSM;
import gameStates.Game;

public class ConsiderationTalk implements Consideration{
	private Action a = new Action("talk");
	
	@Override
	public float evaluate(long entityID, EntityManager context) {
		RenderComponent rc = (RenderComponent) context.getFirstComponent(entityID, RenderComponent.class);
		
		for(Entity e: context.getAllEntities()) {
			
			RenderComponent rce = (RenderComponent) context.getFirstComponent(e, RenderComponent.class);
			
			if(GSM.getSelf().getKeyboard().isKeyPressed(KeyBoardBindings.INTERACTION_KEY) && e.getName()!=null && e.getName().equals("player") && rce.getBoundingBox().intersects(rc.getBoundingBox())) {
				a.setTarget(e);
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
