package engine.geometry;

import glm.vec._2.Vec2;

public class Segment {
	private float length;
	private Vec2  start;
	private Vec2  end;
	
	public Segment(Vec2 start, Vec2 end) {
		this.start = start;
		this.end = end;
	}
	
	public Segment(float ax, float ay, float bx, float by) {
		this(new Vec2(ax,ay), new Vec2(bx, by));
	}

	public float getLength() {
		return length;
	}

	public void setLength(float length) {
		this.length = length;
	}
	
	/**
	 * delta [0,length]
	 * @param delta
	 * @return
	 */
	public Vec2 getPointAt(float delta) { //TODO check if delta doesnt pass length
		  float ratio = delta/length;
		  float x = ratio*end.x + (1.0f - ratio)*start.x;
		  float y = ratio*end.y + (1.0f - ratio)*start.y;
		  
		  return new Vec2(x,y);
	}
	
	/**
	 * delta [0,1]
	 * @param delta
	 * @return
	 */
	public Vec2 getPointAtNormalized(float delta) { //TODO check if delta doesnt pass 1
		  float x = delta*end.x + (1.0f - delta)*start.x;
		  float y = delta*end.y + (1.0f - delta)*start.y;
		  
		  return new Vec2(x,y);
	}

	public Vec2 getStart() {
		return start;
	}

	public void setStart(Vec2 start) {
		this.start = start;
	}

	public Vec2 getEnd() {
		return end;
	}

	public void setEnd(Vec2 end) {
		this.end = end;
	}
	
}
