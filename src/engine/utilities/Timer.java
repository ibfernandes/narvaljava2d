package engine.utilities;

public class Timer {
	private long startTime;
	private long reverseTimeStart;
	/**
	 * Duration in miliseconds.
	 */
	private long duration;
	private boolean reverse = false;
	private float reverseDeltaStart = 0;

	/**
	 * A second measured in nanoseconds. 10^9
	 */
	public static final long SECOND = 1000000000L; // 10^9
	/**
	 * A millisecond measured in nanoseconds. 10^6
	 */
	public static final long MILLISECOND = 1000000L;

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

	public long getStartTime() {
		return startTime;
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
		startTime = System.nanoTime() - (residue % durationNano);
	}

	/**
	 * Starts counting backwards from now() to startTime.
	 */
	public void reverse() {
		if (!reverse) {
			reverseTimeStart = System.nanoTime();
			reverseDeltaStart = getElapsedDelta();
		} else {
			startTime = System.nanoTime();
			reverseDeltaStart = getElapsedDelta();
		}

		reverse = !reverse;
	}

	public boolean isReversed() {
		return reverse;
	}

	/**
	 * Returns the elapsed delta since start time. In reverse mode it decreases its
	 * value. Elapsed delta is always in the range of [0,1].
	 * 
	 * @return
	 */
	public float getElapsedDelta() {
		long elapsed;

		if (reverse)
			elapsed = (System.nanoTime() - reverseTimeStart) / MILLISECOND;
		else
			elapsed = (System.nanoTime() - startTime) / MILLISECOND;

		float delta = (float) elapsed / (float) duration;

		if (reverse)
			delta = (reverseDeltaStart - delta);
		else
			delta = reverseDeltaStart + delta;

		if (delta >= 1)
			delta = 1;

		if (delta <= 0)
			delta = 0;

		return delta;
	}
}
