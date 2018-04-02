package engine.physics;

import engine.logic.GameObject;
import glm.vec._2.Vec2;

public class Hit {
	public GameObject collider;
	public Vec2 pos = new Vec2();
	public Vec2 delta = new Vec2(); //Depth?
	public Vec2 normal = new Vec2();
	public float time;
}
