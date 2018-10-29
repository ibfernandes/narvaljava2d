package engine.entity;

public class Entity {
	private long id;
	private String name;
	
	public Entity(long id) {
		this.id = id;
	}
	
	public long getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
