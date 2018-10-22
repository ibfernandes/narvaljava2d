package engine.utilities;

import java.util.ArrayList;

import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.RenderComponent;
import engine.geometry.Rectangle;
import engine.logic.GameObject;

public class QuadTree {
	public static final int NODE_CAPACITY = 4;
	private Rectangle boundary;
	private EntityManager em;
	private ArrayList<Entity> gameObjects = new ArrayList<>();
	private QuadTree northWest; //esq
	private QuadTree northEast; //dir
	private QuadTree southEast; 
	private QuadTree southWest; 
	
	/**
	 * For better result boundary's width and height should be divisible by two
	 * @param boundary
	 */
	public QuadTree(Rectangle boundary, EntityManager em) {
		this.em = em;
		this.boundary = boundary;
	}
	
	/**
	 * Returns true if object is inserted successfully and false if it's out of bounds
	 * @param o
	 * @return
	 */
	public boolean insert(Entity e) {
		RenderComponent rc = (RenderComponent) em.getFirstComponent(e, RenderComponent.class);
		if(rc==null) {
			//System.err.println("RenderComponent can't be null");
			return false;
			}
		
		if(!boundary.intersects(rc.getBoundingBox()))
			return false;
		
		if(gameObjects.size()< NODE_CAPACITY) {
			gameObjects.add(e);
			return true;
		}
		
		if(northWest==null)
			subDivide();
		
		if(northWest.insert(e)) return true;
		if(northEast.insert(e)) return true;
		if(southEast.insert(e)) return true;
		if(southWest.insert(e)) return true;
		
		return false;
	}
	
	public void subDivide() {
		Rectangle nw = new Rectangle(boundary.x, boundary.y, boundary.width/2, boundary.height/2);
		Rectangle ne = new Rectangle(boundary.x + boundary.width/2, boundary.y, boundary.width/2, boundary.height/2);
		Rectangle se = new Rectangle(boundary.x + boundary.width/2, boundary.y + boundary.height/2, boundary.width/2, boundary.height/2);
		Rectangle sw = new Rectangle(boundary.x, boundary.y + boundary.height/2,boundary.width/2, boundary.height/2);
		
		northWest = new QuadTree(nw,em);
		northEast = new QuadTree(ne,em);
		southEast = new QuadTree(se,em);
		southWest = new QuadTree(sw,em);
	}
	
	public ArrayList<Entity> queryRange(Rectangle range) {
		ArrayList<Entity> pointsInRange = new ArrayList<>();
		
		if(!boundary.intersects(range))
			return pointsInRange;
		
		for(int p=0; p<gameObjects.size(); p++) {
			RenderComponent rc = (RenderComponent) em.getFirstComponent(gameObjects.get(p), RenderComponent.class);
			
			if(range.intersects(rc.getBoundingBox()))
					pointsInRange.add(gameObjects.get(p));
		}
		
		if(northWest==null)
			return pointsInRange;
		
		pointsInRange.addAll( northWest.queryRange(range) );
		pointsInRange.addAll( northEast.queryRange(range) );
		pointsInRange.addAll( southEast.queryRange(range) );
		pointsInRange.addAll( southWest.queryRange(range) );
		
		return pointsInRange;
	}
}
