package engine.engine;

import org.jbox2d.dynamics.World;

import glm.vec._2.Vec2;

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
	
	public void update(float deltatime) {
		world.step(deltatime, VELOCITY_ITERATIONS, POSITION_ITERATIONS);
	}
	
	public static Vec2 convertMetersToPixels(float x, float y){
		return new Vec2(Math.floor(x*PhysicsEngine.BOX2D_SCALE_FACTOR), Math.floor(y*PhysicsEngine.BOX2D_SCALE_FACTOR));
	}
	
	public static Vec2 convertPixelsToMeters(float x, float y){
		return new Vec2(x/PhysicsEngine.BOX2D_SCALE_FACTOR, y/PhysicsEngine.BOX2D_SCALE_FACTOR);
	}
	
	public World getWorld() {
		return world;
	}
}
