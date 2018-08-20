package engine.utilities;

import java.util.ArrayList;

import engine.geometry.Rectangle;
import engine.logic.GameObject;

public class QuadTree {
	public static final int NODE_CAPACITY = 4;
	private Rectangle boundary;
	private ArrayList<GameObject> points = new ArrayList<>();
	private QuadTree northWest; //esq
	private QuadTree northEast; //dir
	private QuadTree southEast; 
	private QuadTree southWest; 
	
	/**
	 * For better result boundary's width and height should be divisible by two
	 * @param boundary
	 */
	public QuadTree(Rectangle boundary) {
		this.boundary = boundary;
	}
	
	/**
	 * Returns true if object is inserted successfully and false if not
	 * @param o
	 * @return
	 */
	public boolean insert(GameObject o) {
		if(!boundary.intersectsPoint(o.getPosition()))
			return false;
		
		if(points.size()< NODE_CAPACITY) {
			points.add(o);
			return true;
		}
		
		if(northWest==null)
			subDivide();
		
		if(northWest.insert(o)) return true;
		if(northEast.insert(o)) return true;
		if(southEast.insert(o)) return true;
		if(southWest.insert(o)) return true;
		
		return false;
	}
	
	public void subDivide() {
		Rectangle nw = new Rectangle(boundary.x, boundary.y, boundary.width/2, boundary.height/2);
		Rectangle ne = new Rectangle(boundary.x + boundary.width/2, boundary.y, boundary.width/2, boundary.height/2);
		Rectangle se = new Rectangle(boundary.x + boundary.width/2, boundary.y + boundary.height/2, boundary.width/2, boundary.height/2);
		Rectangle sw = new Rectangle(boundary.x, boundary.y + boundary.height/2,boundary.width/2, boundary.height/2);
		
		northWest = new QuadTree(nw);
		northEast = new QuadTree(ne);
		southEast = new QuadTree(se);
		southWest = new QuadTree(sw);
	}
	
	public ArrayList<GameObject> queryRange(Rectangle range) {
		ArrayList<GameObject> pointsInRange = new ArrayList<>();
		
		if(!boundary.intersects(range))
			return pointsInRange;
		
		for(int p=0; p<points.size(); p++)
			if(range.intersectsPoint(points.get(p).getPosition()))
					pointsInRange.add(points.get(p));
		
		if(northWest==null)
			return pointsInRange;
		
		pointsInRange.addAll( northWest.queryRange(range) );
		pointsInRange.addAll( northEast.queryRange(range) );
		pointsInRange.addAll( southEast.queryRange(range) );
		pointsInRange.addAll( southWest.queryRange(range) );
		
		return pointsInRange;
	}
}
