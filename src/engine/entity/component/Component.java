package engine.entity.component;

import java.io.Serializable;

public abstract class Component implements Serializable{
	private long entityID;
	
	public Component(long entityID) {
		this.entityID = entityID;
	}
	
	public long getEntityID() {
		return entityID;
	}
}
