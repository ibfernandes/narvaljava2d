package engine.logic;

import java.util.ArrayList;

public class HorizontalPool {
	private int poolSize = 0;
	private double id;
	private ArrayList<GameObject> pool;
	private ArrayList<Double> ids;
	
	public HorizontalPool(int poolSize) {
		this.poolSize = poolSize;
		pool = new ArrayList<>();
		ids = new ArrayList<>();

	}
	
	/**
	 * if there's already an object with given x and y then it'll not be added
	 * @param o
	 */
	public void add(GameObject o, double id) {
		if(contains(id))
			return;
			
		if(pool.size()>poolSize) {
			pool.remove(0);
			ids.remove(0);
		}
		
		pool.add(o);
		ids.add(id);
	}
	
	public boolean contains(float x, float y) {
		for(GameObject o: pool)
			if(o.getX()==x && o.getY()==y)
				return true;
		return false;
	}
	
	public boolean contains(double id) { //TODO: em algum momento esses id's podem colidir!
		for(Double d: ids)
			if(d==id)
				return true;
		return false;
	}
	
	public ArrayList<GameObject> getPool(){
		return pool;
	}
}
