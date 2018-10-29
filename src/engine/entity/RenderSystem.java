package engine.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.renderer.TextureRendererV2;
import engine.utilities.ResourceManager;

public class RenderSystem extends ComponentSystem{
	
	private ArrayList<RenderComponent> components = new ArrayList<>();
	private HashMap<String, RenderComponent> batches = new HashMap<>(); //TODO: change it to one single huge texture atlas.
	
	public RenderSystem(EntityManager em) {
		super(em);
	}

	public void update(float dt) {
		components = em.getAllComponents(RenderComponent.class);
		Collections.sort(components);

		for(RenderComponent c: components) {
			if(c.getAnimations()!=null)
				c.getAnimations().getCurrentAnimation().update();
		}
	}
	
	public void prepareBatches() {
		
	}
	
	public void render() {

		/*for(RenderComponent rc: components) {
			if(!rc.isDisabled()) {
				ResourceManager.getSelf().getTextureRenderer().render(
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
						rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
				
			}
		}*/
		
		renderv2();
		
	}
	
	public void renderv2() {
		TextureRendererV2  t2 = ResourceManager.getSelf().getRenderer("textureRendererv2");
		
		/*for(RenderComponent rc: components)
			t2.render(
				ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
				ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
				rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
		*/
		
		
		t2.start();
		
		for(RenderComponent rc: components) {
			if(!rc.isDisabled()) {
				t2.render(rc);
			}
		}
		
		t2.end();
	}

	@Override
	public void variableUpdate(float alpha) {
	}
}
