package engine.entity;

public class Entity {
	private int id;
	private String name;
	
	public Entity(int id) {
		this.id = id;
	}
	
	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
