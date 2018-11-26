package engine.entity.component;

import engine.geometry.Rectangle;
import engine.logic.AnimationStateManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class UIComponent extends Component{
	public UIComponent(long entityID) {
		super(entityID);
		
	}

	private RenderComponent rc;
	
	public RenderComponent getRenderComponent() {
		return rc;
	}

	public void setRenderComponent(RenderComponent rc) {
		this.rc = rc;
	}
	
	public void onMouseHover() {
		
	}
	
	public void onMousePress() {
		
	}
	
	public void onMouseRelease() {
		
	}
}
