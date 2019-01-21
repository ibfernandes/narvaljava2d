package engine.particle;

import java.util.ArrayList;
import engine.engine.Engine;

public class ParticleEngine {
	private static ParticleEngine self;
	private ArrayList<ParticleEmitter> particleEmitters = new ArrayList<>();
	private ArrayList<ParticleEmitter> particlesQueue = new ArrayList<>();
	private ArrayList<ParticleEmitter> particlesToRemove = new ArrayList<>();
	
	private ParticleEngine() {
	}
	
	public static ParticleEngine getSelf() {
		return (self==null) ? self = new ParticleEngine(): self;
	}
	
	public void addParticleEmitter(ParticleEmitter pb) {
		particlesQueue.add(pb);
	}
	
	public void update(float deltaTime) {
		for(ParticleEmitter pb: particlesQueue) {
			pb.init();
			pb.startTime = System.nanoTime();
			particleEmitters.add(pb);
		}
		particlesQueue.clear();
		
		long currentTime = System.nanoTime();
		for(ParticleEmitter pb: particleEmitters) {
			if(pb.hasLifeTime && (currentTime - pb.startTime)/Engine.MILISECOND >= pb.lifeTime) {
				particlesToRemove.add(pb);
				continue;
			}
			pb.update(deltaTime);
		}
		
		for(ParticleEmitter pb: particlesToRemove) {
			particleEmitters.remove(pb);
		}
		
		particlesToRemove.clear();
		
	}
	
	public void render() {
		for(ParticleEmitter pb: particleEmitters)
			pb.render();
	}
}
