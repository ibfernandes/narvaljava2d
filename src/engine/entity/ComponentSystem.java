package engine.entity;

public abstract class ComponentSystem {
	protected EntityManager em;
	
	public ComponentSystem(EntityManager em) {
		this.em = em;
	}
	
	public abstract void update(float dt);
	public abstract void variableUpdate(float alpha);
	public abstract void render();
}
