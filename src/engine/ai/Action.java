package engine.ai;

import engine.entity.Entity;
import engine.logic.GameObject;

public class Action {
	private String actionName;
	private long targetEntityID;
	
	public Action(String actionName) {
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public long getTarget() {
		return targetEntityID;
	}

	public void setTarget(long targetEntityID) {
		this.targetEntityID = targetEntityID;
	}

}
