package engine.utilities;

public class MathExt {
	/**
	 * min, max inclusive;
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static float clamp(float value, float min, float max) {
		if(value<min)
			return min;
		else if(value>max)
			return max;
		else
			return value;
	}
	
	public static int sign(float value) {
		if(value<0)
			return -1;
		else 
			return 1;
	}
	
	/**
	 * If value belongs to closed interval [start, end]
	 * @param value
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean belongsToInterval(float value, float start, float end) {
		return (value>= start && value<=end);
	}
	
	/**
	 * If value belongs to open interval (start, end)
	 * @param value
	 * @param start
	 * @param end
	 * @return
	 */
	public static boolean belongsToOpenInterval(float value, float start, float end) {
		return (value> start && value<end);
	}
	
	/**
	 * Check if interval [start2,end2] is contained inside [start1,end1]
	 * i.e
	 * start1 <= C <= end1
	 * start2 <= C <= end2
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
	 * Check if interval (start2,end2) is contained inside (start1,end1)
	 * i.e
	 * start1 < C < end1
	 * start2 < C < end2
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
