package engine.entity.system;

import java.util.ArrayList;

import demo.Game;
import engine.entity.Entity;
import engine.entity.component.RenderComponent;
import engine.renderer.TextureBatchRenderer;
import engine.renderer.TextureRenderer;
import engine.renderer.CubeRenderer;
import engine.renderer.GrassRenderer;
import engine.utilities.ResourceManager;
import engine.utilities.Timer;

public class RenderSystem extends ComponentSystem {

	private ArrayList<RenderComponent> components = new ArrayList<>();
	private String lastTexture = "";
	private String lastRenderer = "";

	public RenderSystem(Game context) {
		super(context);
	}

	@Override
	public void update(float dt) {
		components.clear();

		ArrayList<Entity> entitiesOnScreen = Game.getSelf().getEntitiesOnScreen();
		for (Entity e : entitiesOnScreen) {
			RenderComponent c = Game.getSelf().getEm().getFirstComponent(e, RenderComponent.class);
			if (c.isDisabled()) 
				continue;
			
			sortedInsertion(c);

			if (c.getAnimations() != null)
				c.getAnimations().getCurrentAnimation().update();
		}
	}

	private void sortedInsertion(RenderComponent component) {

		if (components.size() == 0) {
			components.add(component);
			return;
		}

		for (int i = components.size() - 1; i >= 0; i--) {
			if (components.get(i).compareTo(component) == -1 || components.get(i).compareTo(component) == 0) {
				components.add(i + 1, component);
				break;
			}
			if (i == 0 && components.get(i).compareTo(component) == 1) {
				components.add(0, component);
			}
		}
	}
	Timer t = new Timer(1000);
	@Override
	public void render() {
		lastTexture = "";
		lastRenderer = "";
		int starts = 0;
		int ends = 0;
		
		for (RenderComponent rc : components) {
			if(lastRenderer.equals("textureBatchRenderer") && !rc.getRenderer().equals("textureBatchRenderer")) {
				if(starts>ends) {
					ends++;
					((TextureBatchRenderer) ResourceManager.getSelf().getRenderer("textureBatchRenderer")).end();
				}
			}
			lastRenderer = rc.getRenderer();
			
			if (rc.getRenderer().equals("textureRenderer")) {
				((TextureRenderer) ResourceManager.getSelf().getRenderer("textureRenderer")).render(
						ResourceManager.getSelf().getTexture(rc.getAnimations().getCurrentAnimation().getTexture()),
						ResourceManager.getSelf()
								.getTexture(rc.getAnimations().getCurrentAnimation().getTexture() + "_normal"),
						rc.getRenderPosition(), rc.getSize(), rc.getRotation(), rc.getColor(),
						rc.getAnimations().getCurrentAnimation().getCurrentFrame(), rc.getOrientation(),
						rc.getSkew());
			}else if (rc.getRenderer().equals("grassRenderer")) {
				((GrassRenderer) ResourceManager.getSelf().getRenderer("grassRenderer")).render(rc);
			} else if (rc.getRenderer().equals("cubeRenderer")) {
				((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(rc);
			}else if (rc.getRenderer().equals("textureBatchRenderer")) {
				if(!lastTexture.equals(rc.getAnimations().getCurrentAnimation().getTexture())) {
					if(starts>ends) {
						ends++;
						((TextureBatchRenderer) ResourceManager.getSelf().getRenderer("textureBatchRenderer")).end();
					}
				}
				
				if(ends>=starts) {
					lastTexture = rc.getAnimations().getCurrentAnimation().getTexture();
					starts++;
					((TextureBatchRenderer) ResourceManager.getSelf().getRenderer("textureBatchRenderer")).start(rc.getAnimations().getCurrentAnimation().getTexture());
				}
				
				((TextureBatchRenderer) ResourceManager.getSelf().getRenderer("textureBatchRenderer")).render(rc);
			}
		}
		
		if(starts>ends) {
			ends++;
			((TextureBatchRenderer) ResourceManager.getSelf().getRenderer("textureBatchRenderer")).end();
		}

		
		if(t.hasElapsed()) {
			t.reset();
			System.out.println("Batches drawed: "+ends);
		}
	}

	@Override
	public void variableUpdate(float alpha) {
	}
}
