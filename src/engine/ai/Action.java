package engine.ai;

import engine.entity.Entity;
import engine.logic.GameObject;

public class Action {
	private String actionName;
	private Entity target;
	
	public Action(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

}
