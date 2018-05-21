package engine.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class HorizontalPool implements Serializable{
	private int poolSize = 0;
	private HashMap<Double, GameObject> map;
	
	public HorizontalPool(int poolSize) {
		this.poolSize = poolSize;
		map = new HashMap<>();

	}
	
	/**
	 * if there's already an object with given x and y then it'll not be added
	 * @param o
	 */
	public void add(GameObject o, double id) {	
		map.putIfAbsent(id, o);
	}
	
	public boolean contains(double id) {
		return map.containsKey(id);
	}
	
	public ArrayList<GameObject> getPool(){
		return new ArrayList<>(map.values());
	}
}
