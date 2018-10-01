package engine.entity;

import java.util.ArrayList;
import java.util.HashMap;

import engine.engine.PhysicsEngine;
import engine.entity.component.Component;

public class EntityManager {
	private int lastID = 0;
	private ArrayList<Entity> entities = new ArrayList<>();
	private HashMap <Integer, ArrayList<Component>> componentsOf = new HashMap<>();
	private EntityManager self;
	
	/*private EntityManager() {}*/
	
	/*public EntityManager getSelf() {
		return (self==null) ? self = new EntityManager(): self;
	}*/

	
	public int generateID() {
		return lastID++;
	}
	
	public Entity newEntity() {
		Entity e = new Entity(generateID());
		entities.add(e);
		return e;
	}
	
	public void addComponentTo(Entity e, Component c) {
		if(componentsOf.get(e.getID())==null)
			componentsOf.put(e.getID(), new ArrayList<>());
		
		componentsOf.get(e.getID()).add(c);
	}
	
	public ArrayList<Component> getComponent(Entity e, Class c) {
		ArrayList<Component> comps= new ArrayList<>();
		
		for(Component cp: componentsOf.get(e.getID()))
			if(c.isInstance(cp))
				comps.add(cp);
		
		return comps;
	}
	
	public Component getFirstComponent(Entity e, Class c) {
		ArrayList<Component> comps= new ArrayList<>();
		
		for(Component cp: componentsOf.get(e.getID()))
			if(c.isInstance(cp))
				return cp;
		
		return null;
	}
	
	public void removeEntity(Entity e) {
		componentsOf.remove(e.getID());
	}
	
	public ArrayList<Entity> getAllEntities() {
		return entities;
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
