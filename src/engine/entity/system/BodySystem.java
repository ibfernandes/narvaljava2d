package engine.entity.system;

import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.ControllerComponent;
import engine.entity.component.RenderComponent;
import engine.utilities.ResourceManager;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class BodySystem extends ComponentSystem{


	public BodySystem(Game context) {
		super(context);
	}

	@Override
	public void update(float dt) {
		/*for(Component c: context.getEm().getAllComponents(BodyComponent.class)) {
			BodyComponent bc = (BodyComponent) c;
		}*/
	}

	@Override
	public void render() {
		for(Entity e: Game.getSelf().getEntitiesOnScreen()) {
			BodyComponent bc = (BodyComponent) Game.getSelf().getEm().getFirstComponent(e, BodyComponent.class);
			RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(e, RenderComponent.class);
			if(bc==null)
				continue;
		
			ResourceManager.getSelf().getCubeRenderer().render(bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize()), 0, new Vec3(1,0,0));
		}
	}

	@Override
	public void variableUpdate(float alpha) {
	}

}
