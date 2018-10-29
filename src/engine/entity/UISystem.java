package engine.entity;

import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.TextComponent;
import engine.entity.component.UIComponent;
import gameStates.GSM;

public class UISystem extends ComponentSystem{

	public UISystem(EntityManager em) {
		super(em);
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void variableUpdate(float alpha) {
		for(Component c: em.getAllComponents(UIComponent.class)) {
			UIComponent uic = (UIComponent) em.getFirstComponent(c.getParentEntityID(), UIComponent.class);
			
			if(uic.getRenderComponent().getBoundingBox().intersectsPoint(GSM.getSelf().getMouse().getCursorPos())) {
				System.out.println("Hover");
			}
		}
	}

	@Override
	public void render() {
	}

}
