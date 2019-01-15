package engine.entity.system;

import engine.entity.EntityManager;
import engine.entity.component.Component;
import engine.entity.component.ControllerComponent;
import engine.entity.component.HealthComponent;

public class HealthSystem extends ComponentSystem{
	
	public HealthSystem(EntityManager em) {
		super(em);
	}

	public void update(float dt) {
		for(Component c: em.getAllComponents(HealthComponent.class)) {
	        
	        HealthComponent health = (HealthComponent) c;
	        
	        if (!health.isAlive) return;
	        if (health.maxHP == 0) return;
	        if (health.currentHP <= 0) {
	            health.isAlive = false;
	        }
        }
	}

	@Override
	public void render() {
	}

	@Override
	public void variableUpdate(float alpha) {
	}
}
