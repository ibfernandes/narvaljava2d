package engine.entity.component;

public class ParticleComponent extends Component {
	/**
	 * startTime in nanoseconds.
	 */
	private long startTime;
	/**
	 * lifeTime in milliseconds.
	 */
	private long lifeTime = 0;

	public ParticleComponent(long entityID) {
		super(entityID);
	}

	public long getStartTime() {
		return startTime;
	}

	/**
	 * Sets @param startTime in nanoseconds.
	 * 
	 * @param startTime
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	/**
	 * Sets @param lifeTiem in milliseconds.
	 */
	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

}
