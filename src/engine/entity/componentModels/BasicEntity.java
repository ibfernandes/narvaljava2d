package engine.entity.componentModels;

import engine.controllers.PlayerController;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.ControllerComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.logic.AnimationStateManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class BasicEntity {
	
	public static Entity generate(EntityManager em, String renderer, Vec2 position, String texture, Vec2 orientation, Vec2 size, AnimationStateManager animations, Rectangle baseBox) {
		Entity e = em.newEntity();

		RenderComponent rc = new RenderComponent(e.getID());
		rc.setSize(size);
		rc.setColor(new Vec4(1,1,1,1));
		rc.setAnimations(animations);
		rc.setRenderPosition(position);
		rc.setRenderer(renderer);
		rc.setBaseBox(baseBox);
		em.addComponentTo(e, rc);

		BasicComponent pc = new BasicComponent(e.getID());
		pc.setPosition(position);
		pc.setSize(size);
		em.addComponentTo(e, pc);
		
		return e;
	}
}
