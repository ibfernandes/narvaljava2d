package engine.entity;

import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.TextComponent;
import engine.entity.component.UIComponent;
import gameStates.GSM;
import gameStates.Game;

public class UISystem extends ComponentSystem{


	public UISystem(Game context) {
		super(context);
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void variableUpdate(float alpha) {
		for(Component c: context.getEm().getAllComponents(UIComponent.class)) {
			UIComponent uic = (UIComponent) context.getEm().getFirstComponent(c.getEntityID(), UIComponent.class);
			
			if(uic.getRenderComponent().getBoundingBox().intersectsPoint(GSM.getSelf().getMouse().getCursorPos())) {
				System.out.println("Hover");
			}
		}
	}

	@Override
	public void render() {
	}

}
