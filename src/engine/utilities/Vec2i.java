package engine.utilities;


public class Vec2i implements Comparable<Vec2i>{
	public int x,y;

	public Vec2i(int x, int y)  {
		this.x = x;
		this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	@Override
	// -1 this < obj
	// 0 this = obj
	// 1 this > obj
	public int compareTo(Vec2i o) {
	
		if (x == o.x && y == o.y)
			return 0;
		
		return -1;
	}
}
