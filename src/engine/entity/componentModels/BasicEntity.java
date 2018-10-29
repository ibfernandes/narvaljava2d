package engine.entity.componentModels;

import engine.controllers.PlayerController;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.ControllerComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import engine.graphic.Animation;
import engine.renderer.ASM;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class BasicEntity {
	
	public static Entity generate(EntityManager em, String renderer, Vec2 position, String texture, Vec2 orientation, Vec2 size, ASM animations) {
		Entity e = em.newEntity();

		RenderComponent rc = new RenderComponent(e.getID());
		rc.setSize(size);
		rc.setColor(new Vec4(1,1,1,1));
		rc.setAnimations(animations);
		rc.setRenderPosition(position);

		em.addComponentTo(e, rc);

		PositionComponent pc = new PositionComponent(e.getID());
		pc.setPosition(position);
		em.addComponentTo(e, pc);
		
		return e;
	}
}
