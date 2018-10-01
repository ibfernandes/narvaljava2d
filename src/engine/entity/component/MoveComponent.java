package engine.entity.component;

import glm.vec._2.Vec2;

public class MoveComponent implements Component{
	public float speed = 0;
	public Vec2 direction = new Vec2(0,0);
}
