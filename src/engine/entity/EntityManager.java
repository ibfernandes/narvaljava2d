package engine.entity;

import java.util.ArrayList;
import java.util.HashMap;

import engine.engine.PhysicsEngine;
import engine.entity.component.Component;
import engine.entity.component.RenderComponent;

public class EntityManager {
	private long lastID = 0;
	private ArrayList<Entity> entities = new ArrayList<>();
	private HashMap<String, ArrayList<Component>> componentsByClass = new HashMap<>();
	private HashMap <Long, ArrayList<Component>> componentsOf = new HashMap<>();
	private long playerID;
	private EntityManager self;
	public static final int MAX_ENTITIES = 6000;
	
	public long generateID() {
		return lastID++;
	}
	
	public Entity newEntity() {
		long id = generateID();
		Entity e = new Entity(id);
		
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
	public ArrayList<Component> getComponent(Entity e, Class c) {
		ArrayList<Component> comps= new ArrayList<>();
		
		for(Component cp: componentsOf.get(e.getID()))
			if(c.isInstance(cp))
				comps.add(cp);
		
		return comps;
	}
	
	public <T extends Component> T getFirstComponent(Entity e, Class c) {
		if(e==null || c ==null || componentsOf.get(e.getID())==null)
			return null;
		for(Component cp: componentsOf.get(e.getID())) {
			if(cp==null)
				return null;
			if(c.isInstance(cp))
				return (T) cp;
		}
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
	
	public void removeEntity(Long id) {
		
		for(String s: componentsByClass.keySet()) {
			Component toRemove = null;
			
			for(Component c: componentsByClass.get(s))
				if(c.getEntityID()==id)
					toRemove = c;
			
			componentsByClass.get(s).remove(toRemove);
		}
		
		int removeIndex = -1;
		for(int i=0; i<entities.size();i++) {
			if(entities.get(i).getID()==id)
				removeIndex = i;
		}
		
		if(removeIndex>=0)
			entities.remove(removeIndex);
		componentsOf.remove(id);
	}
	
	public ArrayList<Entity> getAllEntities() {
		return entities;
	}
	
	public <T extends Component> ArrayList<T> getAllComponents(Class c){
		return (ArrayList<T>) componentsByClass.get(c.getName());
	}
	
	public ArrayList<Entity> getAllEntitiesWithComponent(Class c) {
		ArrayList<Entity> ents = new ArrayList<>();
		
		for(Entity e: entities) {
			if(componentsOf.get(e.getID())==null)
				continue;
			for(Component cp: componentsOf.get(e.getID()))
				if(c.isInstance(cp)) {
					ents.add(e);
					continue;
				}
		}	
		return ents;
	}

	public long getPlayerID() {
		return playerID;
	}

	public void setPlayerID(long playerID) {
		this.playerID = playerID;
	}

}
