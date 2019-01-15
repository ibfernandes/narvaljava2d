package engine.entity.system;

import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.TextComponent;
import engine.entity.component.UIComponent;
import engine.utilities.ResourceManager;
import gameStates.Game;
import glm.vec._4.Vec4;

public class TextSystem extends ComponentSystem{


	public TextSystem(Game context) {
		super(context);
		
	}

	@Override
	public void update(float dt) {
	}

	@Override
	public void variableUpdate(float alpha) {
		
	}

	@Override
	public void render() {
		for(Component c: context.getEm().getAllComponents(TextComponent.class)) {
			TextComponent tc = (TextComponent) context.getEm().getFirstComponent(c.getEntityID(), TextComponent.class);
			if(!tc.isDisabled() && tc.getText()!=null)
				ResourceManager.getSelf().getFont(tc.getFontName()).render(tc.getText(), tc.getPosition().x, tc.getPosition().y, tc.getFontColor());
		}
	}

}
