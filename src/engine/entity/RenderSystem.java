package engine.entity;

import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.utilities.ResourceManager;

public class RenderSystem extends ComponentSystem{

	public RenderSystem(EntityManager em) {
		super(em);
	}

	public void update(float dt) {
		for(Entity e: em.getAllEntitiesWithComponent(RenderComponent.class)) {
			for(Component c: em.getComponent(e,RenderComponent.class)) {
				RenderComponent rc = (RenderComponent) c;
				if(rc.getAnimations()!=null)
					rc.getAnimations().getCurrentAnimation().update();
			}
		}
	}
	public void render() {
		for(Entity e: em.getAllEntitiesWithComponent(RenderComponent.class)) {
			for(Component c: em.getComponent(e,RenderComponent.class)) {
				
				RenderComponent rc = (RenderComponent) c;
				ResourceManager.getSelf().getTextureRenderer().render(
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
						rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
			}
		}
		
	}

	@Override
	public void variableUpdate(float alpha) {
	}
}
