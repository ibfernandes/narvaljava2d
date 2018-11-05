package engine.ai;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BodyComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;
import engine.logic.GameObject;
import gameStates.Game;

public class ConsiderationAttack implements Consideration{
	private Action a = new Action("attack");
	
	@Override
	public float evaluate(long obj, EntityManager em) {
		/*for(GameObject o: game.getFinalLayer()) {
			if(obj!=o && o.getGroup()!=null && obj.getGroup()!=o.getGroup() && o.getGroup().equals("player") && obj.getSightBox().intersects(o.getSightBox())) {
				a.setTarget(o);
				return 0.2f;
			}
		}*/
		
		
		SightComponent sm = em.getFirstComponent(obj, SightComponent.class);
		RenderComponent rc = em.getFirstComponent(obj, RenderComponent.class);
		Rectangle r = sm.calculateSightView(rc.getRenderPosition());
		
		RenderComponent prc = em.getFirstComponent(em.getPlayerID(), RenderComponent.class);
		
		if(r.intersects(prc.getBoundingBox())) {
			a.setTarget(em.getPlayerID());
			return 0.2f;
		}
		
		return 0;
	}

	@Override
	public Action getAction() {
		return a;
	}


}
