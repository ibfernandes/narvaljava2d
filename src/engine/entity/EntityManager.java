package engine.entity;

import java.util.ArrayList;
import java.util.HashMap;

import engine.engine.PhysicsEngine;
import engine.entity.component.Component;

public class EntityManager {
	private long lastID = 0;
	private ArrayList<Entity> entities = new ArrayList<>();
	private HashMap<String, ArrayList<Component>> componentsByClass = new HashMap<>();
	private HashMap <Long, ArrayList<Component>> componentsOf = new HashMap<>();
	private EntityManager self;
	private int maxEntities = 20000;
	
	/*private EntityManager() {}*/
	
	/*public EntityManager getSelf() {
		return (self==null) ? self = new EntityManager(): self;
	}*/

	
	public long generateID() {
		return lastID++;  //TODO: What if this ID is already taken
	}
	
	public Entity newEntity() {
		//System.out.println(entities.size());
		
		long id = generateID();
		Entity e = new Entity(id);
		if(id>maxEntities)
			entities.set((int) (id%maxEntities),e);
		else
			entities.add(e);
		return e;
	}
	
	public void addComponentTo(Entity e, Component c) {

		if(componentsOf.get(e.getID())==null)
			componentsOf.put(e.getID(), new ArrayList<>());
		
		if(componentsByClass.get(c.getClass().getName())==null)
			componentsByClass.put(c.getClass().getName(), new ArrayList<>());
		
		componentsOf.get(e.getID()).add(c);
		componentsByClass.get(c.getClass().getName()).add(c);
	}
	
	public ArrayList<Component> getComponent(long entityID, Class c) {
		ArrayList<Component> comps= new ArrayList<>();
		
		for(Component cp: componentsOf.get(entityID))
			if(c.isInstance(cp))
				comps.add(cp);
		
		return comps;
	}
	
	public <T extends Component> T getFirstComponent(Entity e, Class c) {
		for(Component cp: componentsOf.get(e.getID()))
			if(c.isInstance(cp))
				return (T) cp;
		
		return null;
	}
	
	public <T extends Component> T getFirstComponent(long id, Class c) {
		for(Component cp: componentsOf.get(id))
			if(c.isInstance(cp))
				return (T) cp;
		
		return null;
	}
	
	public void removeEntity(Entity e) {
		componentsOf.remove(e.getID());
	}
	
	public ArrayList<Entity> getAllEntities() {
		return entities;
	}
	
	public <T extends Component> ArrayList<T> getAllComponents(Class c){
		return (ArrayList<T>) componentsByClass.get(c.getName());
	}
	
	public ArrayList<Entity> getAllEntitiesWithComponent(Class c) {
		ArrayList<Entity> ents = new ArrayList<>();
		
		for(Entity e: entities)
			for(Component cp: componentsOf.get(e.getID()))
				if(c.isInstance(cp)) {
					ents.add(e);
					continue;
				}
					
		return ents;
	}

}
