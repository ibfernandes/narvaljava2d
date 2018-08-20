package engine.logic;

public class Timer {
	private long startTime;
	private long currentTime;
	private long lastTime = 0;
	private long elapsedTime;
	private long dx;
	private long duration = 1;
	private double degree = 0;
	private double dxInSeconds;
	private double fraction = 1;
	
	public static final long SECOND = 1000000000L; //10^9
	public static final long MILISECOND = 1000000L;//10^6
	
	public Timer() {
		startTime = System.nanoTime();
	}

	/**
	 * duration of a loop in seconds
	 * @param interval
	 */
	public void setDuration(long duration) {
		this.duration = duration;
		calculateFraction(duration);
	}
	
	/**
	 * fraction at which the degree will increase by 1
	 * @param interval
	 */
	private void calculateFraction(long duration) {
		fraction = (double)duration/(double)SECOND;
	}
	
	public double getDegree() {
		return degree;
	}
	
	public void setDegree(double deg) {
		degree = deg;
	}
	
	public void update() {
		currentTime = System.nanoTime(); 
		dx = currentTime - lastTime;
		dxInSeconds = (double)dx/((double)SECOND*fraction);
		
		lastTime = currentTime;
		elapsedTime = currentTime-startTime; 
		
		degree += 360*dxInSeconds; //TODO: fix
		
		if(degree>=360)
			degree = degree%360;
	}
}
