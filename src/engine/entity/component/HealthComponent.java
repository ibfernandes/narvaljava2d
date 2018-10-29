package engine.entity.component;

public class HealthComponent extends Component{
	public HealthComponent(long entityID) {
		super(entityID);
		
	}
	public float currentHP;
	public float maxHP;
	public boolean isAlive;
}
