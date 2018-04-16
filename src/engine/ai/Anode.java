package engine.ai;

import engine.utilities.Vec2i;

public class Anode {
	private float gScore; // distance from start
	private float fScore; //distance from end
	public Anode cameFrom;
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
		
		return (pos.x==comp.pos.x && pos.y==comp.pos.y);
	}
}
