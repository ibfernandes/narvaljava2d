package engine.utilities;

import glm.vec._2.Vec2;

public class Commons {
	public static final int TOP = 0, RIGHT = 1, BOTTOM = 2, LEFT = 3;
	public static final int TOP_DIAGONAL_LEFT = 4, TOP_DIAGONAL_RIGHT = 5, BOTTOM_DIAGONAL_RIGHT = 6,
			BOTTOM_DIAGONAL_LEFT = 7;

	/**
	 * Returns the movement direction using its static final int values ranging from
	 * 0 to 7.
	 * 
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static int calculateDirection8way(float x, float y, float x2, float y2) {
		boolean direction[] = new boolean[4];

		if (x > x2)
			direction[RIGHT] = true;
		else if (x < x2)
			direction[LEFT] = true;
		if (y > y2)
			direction[BOTTOM] = true;
		else if (y < y2)
			direction[TOP] = true;

		if (direction[RIGHT] && direction[TOP])
			return TOP_DIAGONAL_RIGHT;

		if (direction[LEFT] && direction[TOP])
			return TOP_DIAGONAL_LEFT;

		if (direction[RIGHT] && direction[BOTTOM])
			return BOTTOM_DIAGONAL_RIGHT;

		if (direction[LEFT] && direction[BOTTOM])
			return BOTTOM_DIAGONAL_LEFT;

		if (direction[RIGHT])
			return RIGHT;

		if (direction[LEFT])
			return LEFT;

		if (direction[BOTTOM])
			return BOTTOM;

		if (direction[TOP])
			return TOP;

		return -1;
	}
	
	public static Vec2 convertToVector(Vec2 vector, int direction) {
		switch(direction) {
			case TOP:
				vector.x = 0;
				vector.y = 1;
				return vector;
			case RIGHT:
				vector.x = 1;
				vector.y = 0;
				return vector;
			case BOTTOM:
				vector.x = 0;
				vector.y = -1;
				return vector;
			case LEFT:
				vector.x = -1;
				vector.y = 0;
				return vector;
			case TOP_DIAGONAL_LEFT:
				vector.x = -1;
				vector.y = 1;
				return vector;
			case TOP_DIAGONAL_RIGHT:
				vector.x = 1;
				vector.y = 1;
				return vector;
			case BOTTOM_DIAGONAL_RIGHT:
				vector.x = 1;
				vector.y = -1;
				return vector;
			case BOTTOM_DIAGONAL_LEFT:
				vector.x = -1;
				vector.y = -1;
				return vector;
		}
		return vector;
	}
	
	public static Vec2 calculateDirection(float x, float y, float x2, float y2) {
		if(x-x2 == 0 && y-y2==0)
			return new Vec2(0,0);
		return new Vec2(x-x2, y-y2).normalize();
	}
	
	
}
