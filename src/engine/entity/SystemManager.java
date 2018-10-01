package engine.entity;

import java.util.ArrayList;

public class SystemManager {
	
	private ArrayList<ComponentSystem> systems = new ArrayList<>();
	
	public void addSystem(ComponentSystem cs) {
		systems.add(cs);
	}
	
	public void update(float dt) {
		for(ComponentSystem cs: systems)
			cs.update(dt);
	}
	
	public void variableUpdate(float alpha) {
		for(ComponentSystem cs: systems)
			cs.variableUpdate(alpha);
	}
	
	public void render() {
		for(ComponentSystem cs: systems)
			cs.render();
	}
}
