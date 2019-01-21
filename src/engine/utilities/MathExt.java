package engine.utilities;

import glm.vec._2.Vec2;

public class MathExt {
	
	/**
	 * Calculates the distance between two vectors.
	 * 
	 * @param vecA
	 * @param vecB
	 * @return
	 */
	public static float calculateDistance(Vec2 vecA, Vec2 vecB) {
		return (float) Math.sqrt(
				Math.pow(vecA.x - vecB.x, 2)
				+
				Math.pow(vecA.y - vecB.y, 2)
				);
	}
	
	/**
	 * Calculates the angle in degrees between two vectors.
	 * 
	 * @param vecA
	 * @param vecB
	 * @return
	 */
	public static float calculateAngle(Vec2 vecA, Vec2 vecB) {
		return (float) Math.toDegrees(Math.atan2(vecA.y - vecB.y, vecA.x - vecB.x));
	}
	
	public static float clamp(float value, float min, float max) {
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
	}

	/**
	 * Returns -1 if @param value is negative and 1 if it's positive.
	 * 
	 * @param value
	 * @return
	 */
	public static int sign(float value) {
		if (value < 0)
			return -1;
		else
			return 1;
	}

	/**
	 * Returns true if @param value belongs to closed interval [start, end] and
	 * false otherwise.
	 * 
	 * @param value
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean belongsToInterval(float value, float start, float end) {
		return (value >= start && value <= end);
	}

	/**
	 * Returns true if @param value belongs to the open interval (start, end) and
	 * false otherwise.
	 * 
	 * @param value
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean belongsToOpenInterval(float value, float start, float end) {
		return (value > start && value < end);
	}

	/**
	 * Check if interval [start2,end2] is contained inside [start1,end1] i.e
	 * start1<= C <= end1 
	 * start2 <= C <= end2
	 * 
	 * @param start1
	 * @param end1
	 * @param start2
	 * @param end2
	 * @return
	 */
	public static boolean intervalIntersect(float start1, float end1, float start2, float end2) {
		return (start1 <= end2 && start2 <= end1);
	}

	/**
	 * Check if interval (start2,end2) is contained inside (start1,end1) i.e 
	 * start1 < C < end1 
	 * start2 < C < end2
	 * 
	 * @param start1
	 * @param end1
	 * @param start2
	 * @param end2
	 * @return
	 */
	public static boolean openIntervalIntersect(float start1, float end1, float start2, float end2) {
		return (start1 < end2 && start2 < end1);
	}
}
