package engine.particle;

import java.util.ArrayList;
import java.util.Queue;

import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.EntityManager;
import glm.vec._2.Vec2;

public class ParticleEngine {
	private static ParticleEngine self;
	private EntityManager em;
	public static int MAX_PARTICLES = 1000;
	private int particlesCount = 0;
	private ArrayList<ParticleEmitter> particles = new ArrayList<>();
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
			//particlesCount +=pb.particleCount;
			pb.startTime = System.nanoTime();
			particles.add(pb);
		}
		particlesQueue.clear();
		
		long currentTime = System.nanoTime();
		for(ParticleEmitter pb: particles) {
			if(pb.hasLifeTime && (currentTime - pb.startTime)/Engine.MILISECOND >= pb.lifeTime) {
				particlesToRemove.add(pb);
				//particlesCount -= pb.particleCount;
				continue;
			}
			pb.update(deltaTime);
		}
		
		for(ParticleEmitter pb: particlesToRemove) {
			particles.remove(pb);
		}
		
		particlesToRemove.clear();
		
	}
	
	public void render() {
		for(ParticleEmitter pb: particles)
			pb.render();
	}
}
