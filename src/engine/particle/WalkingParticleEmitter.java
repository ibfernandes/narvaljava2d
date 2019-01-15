package engine.particle;

import java.util.ArrayList;
import java.util.Random;

import engine.engine.Engine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.ParticleComponent;
import engine.entity.component.RenderComponent;
import engine.entity.system.BodySystem;
import engine.entity.system.ControllerSystem;
import engine.entity.system.MoveSystem;
import engine.entity.system.RenderSystem;
import engine.entity.system.SystemManager;
import engine.utilities.ResourceManager;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

public class WalkingParticleEmitter extends ParticleEmitter{
	
	private Vec2 position;
	private int offset;
	private Vec2 previousPosition;
	private Random r = new Random();
	private org.jbox2d.common.Vec2 speed = new org.jbox2d.common.Vec2(0,0);
	private int maxParticles = 20;
	private Entity particles[];
	private ArrayList<Integer> freeParticles = new ArrayList<>();
	
	@Override
	public void init() {
		this.hasLifeTime = false;
		particles = new Entity[maxParticles];
		
		for(int i=0; i<maxParticles; i++) {
			Entity particle = Game.getSelf().getEm().newEntity();
			
			Vec2 size = new Vec2(15,15);
			Vec2 pos = new Vec2(20 + position.x + 40*r.nextFloat(), position.y + offset);
			
			RenderComponent rc = new RenderComponent(particle.getID());
			rc.setSize(size);
			rc.setColor(new Vec4(0.55,0.5,0.4,1));
			rc.setRenderPosition(pos);
			rc.setRenderer("cubeRenderer");
			Game.getSelf().getEm().addComponentTo(particle, rc);

			BasicComponent pc = new BasicComponent(particle.getID());
			pc.setPosition(pos);
			pc.setSize(size);
			Game.getSelf().getEm().addComponentTo(particle, pc);
			
			MoveComponent mc = new MoveComponent(particle.getID());
			mc.velocity = 200*r.nextFloat();
			mc.direction = new Vec2((1 - 2*r.nextInt(2))*r.nextFloat(),-1*r.nextFloat());
			Game.getSelf().getEm().addComponentTo(particle, mc);
			
			ParticleComponent pac = new ParticleComponent(particle.getID());
			pac.startTime = System.nanoTime();
			pac.lifeTime = 400;
			Game.getSelf().getEm().addComponentTo(particle, pac);
			
			particles[i]= particle;
		}
	}
	
	public void setAnchor(Vec2 anchor, int offset) {
		this.offset = offset;
		this.position = anchor;
		this.previousPosition = new Vec2(anchor.x, anchor.y);
	}
	
	@Override
	public void update(float deltaTime) {
		if((int)position.x != (int)previousPosition.x || (int)position.y != (int)previousPosition.y) {
			createParticle();
			createParticle();
			createParticle();
		}
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		
		
		for(int i=0; i< maxParticles; i++) {
			ParticleComponent pc = (ParticleComponent) Game.getSelf().getEm().getFirstComponent(particles[i], ParticleComponent.class);
			if ((System.nanoTime() - pc.startTime)/Engine.MILISECOND > pc.lifeTime) {
				freeParticles.add(i);
				RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(particles[i], RenderComponent.class);
				rc.setDisabled(true);
			}
		}
	}
	
	private void createParticle() {
		if(!freeParticles.isEmpty()) {
			int index = freeParticles.get(0);
			Vec2 pos = new Vec2(20 + position.x + 40*r.nextFloat(), position.y + offset);
			
			RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(particles[index], RenderComponent.class);
			rc.setRenderPosition(pos);
			rc.setDisabled(false);
			
			BasicComponent pc = (BasicComponent) Game.getSelf().getEm().getFirstComponent(particles[index], BasicComponent.class);
			pc.setPosition(pos);
			
			MoveComponent mc = (MoveComponent) Game.getSelf().getEm().getFirstComponent(particles[index], MoveComponent.class);
			mc.direction = new Vec2((1 - 2*r.nextInt(2))*r.nextFloat(),-1*r.nextFloat());
			
			ParticleComponent pac = (ParticleComponent) Game.getSelf().getEm().getFirstComponent(particles[index], ParticleComponent.class);
			pac.startTime = System.nanoTime();
			
			freeParticles.remove(0);
		}
	}

	@Override
	public void render() {
	
	}

}
