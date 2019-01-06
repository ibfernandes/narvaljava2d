package engine.entity.component;

public class ParticleComponent  extends Component{
	public long startTime; //in nanoSeconds
	public long lifeTime = 0; //in miliseconds
	
	
	public ParticleComponent(long entityID) {
		super(entityID);
	}

}
