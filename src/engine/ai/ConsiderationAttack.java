package engine.ai;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;

public class ConsiderationAttack implements Consideration {
	private Action a = new Action("attack");

	@Override
	public float evaluate(long obj, EntityManager em) {
		SightComponent sm = em.getFirstComponent(obj, SightComponent.class);
		if(sm==null)
			return 0;
		RenderComponent rc = em.getFirstComponent(obj, RenderComponent.class);
		Rectangle r = sm.calculateSightView(rc.getRenderPosition());

		for (Entity e : em.getAllEntities()) {
			if (e.getName() != null && e.getName().equals("player")) {
				BasicComponent prc = em.getFirstComponent(e, BasicComponent.class);

				if (r.intersects(prc.getBoundingBox())) {
					a.setTarget(e.getID());
					return 0.2f;
				}
			}
		}

		return 0;
	}

	@Override
	public Action getAction() {
		return a;
	}

}
