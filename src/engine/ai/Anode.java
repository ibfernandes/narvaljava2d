package engine.ai;

import engine.utilities.Vec2i;

public class Anode implements Comparable<Anode> {
	/**
	 * Distance from initial node.
	 */
	private float gScore = Float.POSITIVE_INFINITY;
	/**
	 * Real distance to the final node.
	 */
	private float fScore = Float.POSITIVE_INFINITY;
	public Anode antecessor;
	public Vec2i pos = new Vec2i(0, 0);

	public Anode() {

	}

	public Anode(int x, int y) {
		pos.x = x;
		pos.y = y;
	}

	public float getgScore() {
		return gScore;
	}

	public void setgScore(float gScore) {
		this.gScore = gScore;
	}

	public float getfScore() {
		return fScore;
	}

	public void setfScore(float fScore) {
		this.fScore = fScore;
	}

	@Override
	public boolean equals(Object a) {
		Anode comp = (Anode) a;

		return (pos.x == comp.pos.x && pos.y == comp.pos.y);
	}

	@Override
	/**
	 * Compares this object's fscore with @param obj. Returns -1 when this < obj, 0
	 * when this = obj and 1 when this > obj.
	 */
	public int compareTo(Anode obj) {
		if (fScore > obj.getfScore())
			return 1;
		if (fScore == obj.getfScore())
			return 0;
		if (fScore < obj.getfScore())
			return -1;

		return 0;
	}
}
