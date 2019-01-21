package engine.entity.system;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import engine.entity.Entity;
import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.renderer.TextureBatchRenderer;
import engine.renderer.TextureRenderer;
import engine.renderer.CubeRenderer;
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
		components.clear();
		
		ArrayList<Entity> entitiesOnScreen = Game.getSelf().getEntitiesOnScreen();
		//System.out.println(entitiesOnScreen.size());
		for(Entity e: entitiesOnScreen) {
			RenderComponent c = Game.getSelf().getEm().getFirstComponent(e, RenderComponent.class);
			
			sortedInsertion(c);
			
			if(c.getAnimations()!=null)
				c.getAnimations().getCurrentAnimation().update();
		}
	}
	
	private void sortedInsertion(RenderComponent component) {
		
		if(components.size()==0) {
			components.add(component);
			return;
		}
		
		for(int i= components.size()-1; i>=0; i--) {
			if(components.get(i).compareTo(component)==-1 || components.get(i).compareTo(component)==0) {
				components.add(i+1, component);
				break;
			}
			if(i == 0 && components.get(i).compareTo(component)==1) {
				components.add(0, component);
			}
		}
	}
	
	public void render() {

		for(RenderComponent rc: components) {
			if(!rc.isDisabled()) {
				if(rc.getRenderer().equals("textureRenderer"))
					((TextureRenderer)ResourceManager.getSelf().getRenderer("textureRenderer")).render(
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
						rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
				else if(rc.getRenderer().equals("grassRenderer")) {
					((GrassRenderer) ResourceManager.getSelf().getRenderer("grassRenderer")).render(rc);
				}else if(rc.getRenderer().equals("cubeRenderer")) {
					((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(rc);
				}
			}
		}
		
		//renderv2();
		
	}
	
	public void renderv2() {
		TextureBatchRenderer  t2 = ResourceManager.getSelf().getRenderer("textureRendererv2");
		
		/*for(RenderComponent rc: components)
			t2.render(
				ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
				ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()+"_normal"),
				rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(), rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(), rc.getSkew());
		*/
		
		
		//t2.start();
		
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
