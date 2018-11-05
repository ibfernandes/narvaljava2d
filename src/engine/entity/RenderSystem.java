package engine.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.renderer.TextureRendererV2;
import engine.renderer.GrassRenderer;
import engine.utilities.ResourceManager;
import gameStates.Game;

public class RenderSystem extends ComponentSystem{
	
	public RenderSystem(Game context) {
		super(context);
		
	}

	private ArrayList<RenderComponent> components = new ArrayList<>();
	private HashMap<String, RenderComponent> batches = new HashMap<>(); //TODO: change it to one single huge texture atlas.


	public void update(float dt) {
		components = context.getEm().getAllComponents(RenderComponent.class);
		Collections.sort(components);

		for(RenderComponent c: components) {
			if(c.getAnimations()!=null)
				c.getAnimations().getCurrentAnimation().update();
		}
	}
	
	public void sortAndPrepareBatches() {
		
	}
	
	public void render() {

		for(RenderComponent rc: components) {
			if(!rc.isDisabled()) {
				if(rc.getRenderer().equals("textureRenderer"))
				ResourceManager.getSelf().getTextureRenderer().render(
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
						rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
				else if(rc.getRenderer().equals("grassRenderer")) {
					((GrassRenderer) ResourceManager.getSelf().getRenderer("grassRenderer")).render(rc);
				}
			}
		}
		
		//renderv2();
		
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
