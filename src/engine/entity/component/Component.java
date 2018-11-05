package engine.entity.component;

public abstract class Component{
	private long entityID;
	
	public Component(long entityID) {
		this.entityID = entityID;
	}
	
	public long getEntityID() {
		return entityID;
	}
}
