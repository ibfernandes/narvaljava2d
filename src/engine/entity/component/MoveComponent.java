package engine.entity.component;

import glm.vec._2.Vec2;

public class MoveComponent extends Component{
	public MoveComponent(long entityID) {
		super(entityID);
		
	}
	public float speed = 0;
	public Vec2 direction = new Vec2(0,0);
}
