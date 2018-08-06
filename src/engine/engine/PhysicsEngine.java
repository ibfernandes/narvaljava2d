package engine.engine;

import org.jbox2d.dynamics.World;

public class PhysicsEngine {
	private static PhysicsEngine self;
	private World world;
	public static final org.jbox2d.common.Vec2 GRAVITY = new org.jbox2d.common.Vec2(0,0);
	public static final int VELOCITY_ITERATIONS = 6;
	public static final int POSITION_ITERATIONS  = 2;
	public static final int BOX2D_SCALE_FACTOR = 100;
	
	private PhysicsEngine() {
		world = new World(GRAVITY);
	}
	
	public static PhysicsEngine getSelf() {
		return (self==null) ? self = new PhysicsEngine(): self;
	}
	
	public void update() {
		world.step(1f/(float)Engine.TARGET_UPDATES, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}
	
	public World getWorld() {
		return world;
	}
}
