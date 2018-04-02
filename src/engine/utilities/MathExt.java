package engine.utilities;

public class MathExt {
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
}
