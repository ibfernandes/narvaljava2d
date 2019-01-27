package engine.entity.system;

import demo.Game;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.component.BodyComponent;
import engine.entity.component.RenderComponent;
import glm.vec._2.Vec2;

public class BodySystem extends ComponentSystem {

	public BodySystem(Game context) {
		super(context);
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void render() {
		for (Entity e : Game.getSelf().getEntitiesOnScreen()) {
			BodyComponent bc = (BodyComponent) Game.getSelf().getEm().getFirstComponent(e, BodyComponent.class);
			RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(e, RenderComponent.class);
			if (bc == null)
				continue;

			Vec2 sizePos = PhysicsEngine.convertMetersToPixels(bc.getB2Size().x, bc.getB2Size().y);
			Vec2 pixelsPos = PhysicsEngine.convertMetersToPixels(bc.body.getPosition().x - bc.getB2Size().x / 2,
					bc.body.getPosition().y - bc.getB2Size().y / 2);

//			((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(pixelsPos, sizePos, 0,
//					new Vec4(1, 0, 0, 1));
		}
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
