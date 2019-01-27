package engine.entity;

import java.util.ArrayList;
import java.util.HashMap;

import engine.entity.component.Component;

public class EntityManager {
	private long lastID = 0;
	private ArrayList<Entity> entities = new ArrayList<>();
	private HashMap<String, ArrayList<Component>> componentsByClass = new HashMap<>();
	private HashMap<Long, ArrayList<Component>> componentsOf = new HashMap<>();
	private long playerID;
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

	/**
	 * Adds a component to an entity.
	 * 
	 * @param entity
	 * @param component
	 */
	public void addComponentTo(Entity entity, Component component) {

		if (componentsOf.get(entity.getID()) == null)
			componentsOf.put(entity.getID(), new ArrayList<>());

		if (componentsByClass.get(component.getClass().getName()) == null)
			componentsByClass.put(component.getClass().getName(), new ArrayList<>());

		componentsOf.get(entity.getID()).add(component);
		componentsByClass.get(component.getClass().getName()).add(component);
	}

	/**
	 * Gets an Array List of Components that are an instance of the class @param
	 * cls. Returns an empty Array List if there's none.
	 * 
	 * @param entityID
	 * @param cls
	 * @return
	 */
	public ArrayList<Component> getComponent(long entityID, Class<?> cls) {
		ArrayList<Component> comps = new ArrayList<>();

		for (Component cp : componentsOf.get(entityID))
			if (cls.isInstance(cp))
				comps.add(cp);

		return comps;
	}

	/**
	 * Gets an Array List of Components that are an instance of the class @param
	 * cls. Returns an empty Array List if there's none.
	 * 
	 * @param e
	 * @param c
	 * @return
	 */
	public ArrayList<Component> getComponent(Entity e, Class<?> cls) {
		ArrayList<Component> comps = new ArrayList<>();

		for (Component cp : componentsOf.get(e.getID()))
			if (cls.isInstance(cp))
				comps.add(cp);

		return comps;
	}

	/**
	 * Gets the first component that is an instance of the class @param cls. Returns
	 * null if there's none.
	 * 
	 * @param entity
	 * @param cls
	 * @return
	 */
	public <T extends Component> T getFirstComponent(Entity entity, Class<?> cls) {
		if (entity == null || cls == null || componentsOf.get(entity.getID()) == null)
			return null;
		for (Component cp : componentsOf.get(entity.getID())) {
			if (cp == null)
				return null;
			if (cls.isInstance(cp))
				return (T) cp;
		}
		return null;
	}

	/**
	 * Gets the first component that is an instance of the class @param cls. Returns
	 * null if there's none.
	 * 
	 * @param id
	 * @param cls
	 * @return
	 */
	public <T extends Component> T getFirstComponent(long id, Class<?> cls) {
		for (Component cp : componentsOf.get(id))
			if (cls.isInstance(cp))
				return (T) cp;

		return null;
	}

	/**
	 * Removes and deletes this entity.
	 * 
	 * @param id
	 */
	public void removeEntity(Long id) {
		for (String s : componentsByClass.keySet()) {
			Component toRemove = null;

			for (Component c : componentsByClass.get(s))
				if (c.getEntityID() == id)
					toRemove = c;

			componentsByClass.get(s).remove(toRemove);
		}

		int removeIndex = -1;
		for (int i = 0; i < entities.size(); i++) {
			if (entities.get(i).getID() == id)
				removeIndex = i;
		}

		if (removeIndex >= 0)
			entities.remove(removeIndex);
		componentsOf.remove(id);
	}

	/**
	 * Returns all entities.
	 * 
	 * @return
	 */
	public ArrayList<Entity> getAllEntities() {
		return entities;
	}

	/**
	 * Gets all components that are an instance of @param c
	 * 
	 * @param c
	 * @return
	 */
	public <T extends Component> ArrayList<T> getAllComponents(Class c) {
		return (ArrayList<T>) componentsByClass.get(c.getName());
	}

	public ArrayList<Entity> getAllEntitiesWithComponent(Class c) {
		ArrayList<Entity> ents = new ArrayList<>();

		for (Entity e : entities) {
			if (componentsOf.get(e.getID()) == null)
				continue;
			for (Component cp : componentsOf.get(e.getID()))
				if (c.isInstance(cp)) {
					ents.add(e);
					continue;
				}
		}
		return ents;
	}

}
