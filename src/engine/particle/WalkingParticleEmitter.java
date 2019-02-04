package engine.particle;

import java.util.ArrayList;
import java.util.Random;

import demo.Game;
import engine.engine.Engine;
import engine.entity.Entity;
import engine.entity.component.BasicComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.ParticleComponent;
import engine.entity.component.RenderComponent;
import engine.utilities.Timer;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class WalkingParticleEmitter extends ParticleEmitter{
	
	private Vec2 position;
	private int offset;
	private Vec2 previousPosition;
	private Random r = new Random();
	private int maxParticles = 10;
	private long particlesId[];
	private ArrayList<Integer> freeParticles = new ArrayList<>();
	private Timer t = new Timer(100);
	
	@Override
	public void init() {
		this.hasLifeTime = false;
		particlesId = new long[maxParticles];
		
		for(int i=0; i<maxParticles; i++) {
			Entity particle = Game.getSelf().getEm().newEntity();
			float fl = r.nextFloat();
			
			Vec2 size = new Vec2(8*fl,8*fl);
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
			mc.setVelocity(100*r.nextFloat());
			mc.setDirection(new Vec2((1 - 2*r.nextInt(2))*r.nextFloat(),-1*r.nextFloat()));
			Game.getSelf().getEm().addComponentTo(particle, mc);
			
			ParticleComponent pac = new ParticleComponent(particle.getID());
			pac.setStartTime( System.nanoTime());
			pac.setLifeTime(500);
			Game.getSelf().getEm().addComponentTo(particle, pac);
			
			particlesId[i]= particle.getID();
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
			if(t.hasElapsed()) {
				createParticle();
				t.reset();
			}
		}
		previousPosition.x = position.x;
		previousPosition.y = position.y;
		
		
		for(int i=0; i< maxParticles; i++) {
			ParticleComponent pc = (ParticleComponent) Game.getSelf().getEm().getFirstComponent(particlesId[i], ParticleComponent.class);
			if ((System.nanoTime() - pc.getStartTime())/Timer.MILLISECOND > pc.getLifeTime()) {
				
				if(!freeParticles.contains(i)) {
					freeParticles.add(i);
					RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(particlesId[i], RenderComponent.class);
					rc.setDisabled(true);
				}
			}
		}
	}
	
	private void createParticle() {
		if(!freeParticles.isEmpty()) {
			int index = freeParticles.get(0);
			Vec2 pos = new Vec2(20 + position.x + 40*r.nextFloat(), position.y + offset);
			
			
			RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(particlesId[index], RenderComponent.class);
			rc.setRenderPosition(pos);
			rc.setDisabled(false);
			
			BasicComponent pc = (BasicComponent) Game.getSelf().getEm().getFirstComponent(particlesId[index], BasicComponent.class);
			pc.setPosition(pos);
			
			MoveComponent mc = (MoveComponent) Game.getSelf().getEm().getFirstComponent(particlesId[index], MoveComponent.class);
			mc.setDirection( new Vec2((1 - 2*r.nextInt(2))*r.nextFloat(),-1*r.nextFloat()));
			
			ParticleComponent pac = (ParticleComponent) Game.getSelf().getEm().getFirstComponent(particlesId[index], ParticleComponent.class);
			pac.setStartTime(System.nanoTime());
			
			freeParticles.remove(0);
		}
	}

	@Override
	public void render() {
	
	}

}
