package engine.particle;

public abstract class ParticleEmitter {
	public int lifeTime = 0; // in miliseconds
	public boolean hasLifeTime = true;
	public long startTime = 0;
	public int particleCount = 0;
	public abstract void init();
	public abstract void update(float deltaTime);
	public abstract void render();
}
