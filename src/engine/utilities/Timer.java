package engine.utilities;

public class Timer {
	private long startTime;
	/**
	 * Duration in miliseconds.
	 */
	private long duration;

	/**
	 * A second measured in nanoseconds. 10^9
	 */
	public static final long SECOND = 1000000000L; // 10^9
	/**
	 * A millisecond measured in nanoseconds. 10^6
	 */
	public static final long MILLISECOND = 1000000L;

	/**
	 * Instantiates this class setting the start time as now.
	 */
	public Timer() {
		startTime = System.nanoTime();
	}

	/**
	 * Instantiates this class setting the start time as now and duration as @param
	 * duration in milliseconds.
	 * 
	 * @param duration
	 */
	public Timer(long duration) {
		this(System.nanoTime(), duration);
	}

	/**
	 * Instantiates this class setting the start time as now and duration as @param
	 * duration in milliseconds.
	 * 
	 * @param startTime
	 * @param duration
	 */
	public Timer(long startTime, long duration) {
		this.duration = duration;
		this.startTime = startTime;
	}

	/**
	 * Sets the start time in nanoseconds.
	 * 
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	/**
	 * Sets the duration of this timer in milliseconds.
	 * 
	 * @param duration
	 */
	public void setDurationInMiliseconds(long duration) {
		this.duration = duration;
	}

	/**
	 * If it has elapsed "duration" time since its start then returns true, if not
	 * returns false.
	 * 
	 * @return
	 */
	public boolean hasElapsed() {
		if ((System.nanoTime() - startTime) / MILLISECOND > duration)
			return true;
		else
			return false;
	}

	/**
	 * Resets timer start time.
	 */
	public void reset() {
		startTime = System.nanoTime();
	}

	/**
	 * Resets the timer start time considering the residue since its last reset. The
	 * residue won't be larger than the duration, if it's then a modulo is applied.
	 */
	public void resetWithResidousTime() {
		long durationNano = duration * MILLISECOND;
		long residue = (System.nanoTime() - startTime) - durationNano;
		startTime = System.nanoTime() - (residue%durationNano);
	}

	/**
	 * Returns the elapsed delta since start time.
	 * 
	 * @return
	 */
	public float getElapsedDelta() {
		long elapsed = (System.nanoTime() - startTime) / MILLISECOND;
		float delta = (float) elapsed / (float) duration;
		return delta;
	}
}
