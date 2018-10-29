package engine.entity;

import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.TextComponent;
import engine.entity.component.UIComponent;
import engine.utilities.ResourceManager;
import glm.vec._4.Vec4;

public class TextSystem extends ComponentSystem{

	public TextSystem(EntityManager em) {
		super(em);
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void variableUpdate(float alpha) {
		
	}

	@Override
	public void render() {
		for(Component c: em.getAllComponents(TextComponent.class)) {
			TextComponent tc = (TextComponent) em.getFirstComponent(c.getParentEntityID(), TextComponent.class);
			if(!tc.isDisabled() && tc.getText()!=null)
				ResourceManager.getSelf().getFont(tc.getFontName()).render(tc.getText(), tc.getPosition().x, tc.getPosition().y, tc.getFontColor());
		}
	}

}
