package demo;

import org.jbox2d.dynamics.BodyType;

import engine.controllers.AIController;
import engine.controllers.WanderController;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.ControllerComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.logic.AnimationStateManager;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Generator {
	private static Generator self;
	
	public static Generator getSelf() {
		return (self == null) ? self = new Generator() : self;
	}
	
	public void generateBirds(float x, float y, int quantity) {
		
		for(int i=0; i<quantity; i++) {
			Entity e = Game.getSelf().getEm().newEntity();
			AnimationStateManager asm = new AnimationStateManager();
	
			Animation a = new Animation("terrain_atlas", 1000);
			a.setFrames(ResourceManager.getSelf().getSpriteFrame("redbird_idle"));
			asm.addAnimation("idle_1", a);
	
			a = new Animation("terrain_atlas", 150);
			a.setFrames(ResourceManager.getSelf().getSpriteFrame("redbird_flying"));
			asm.addAnimation("walking", a);
			
			a = new Animation("terrain_atlas", 30);
			a.setFrames(ResourceManager.getSelf().getSpriteFrame("redbird_idle"));
			asm.addAnimation("attacking", a);
	
			asm.changeStateTo("idle_1");
	
			Vec2 pos = new Vec2(x, y);
			Vec2 size = new Vec2(10,8).mul(4f);
			
			RenderComponent rc = new RenderComponent(e.getID());
			rc.setSize(size);
			rc.setColor(new Vec4(1, 1, 1, 1));
			rc.setAnimations(asm);
			rc.setBaseBox(new Rectangle(0f, 0.9f, 1.0f, 0.1f));
			rc.setRenderer("textureBatchRenderer");
			rc.setRenderPosition(pos);
	
			Game.getSelf().getEm().addComponentTo(e, rc);
	
			ControllerComponent cp = new ControllerComponent(e.getID());
			cp.setController(new WanderController());
			Game.getSelf().getEm().addComponentTo(e, cp);
	
			BasicComponent pc = new BasicComponent(e.getID());
			pc.setPosition(pos);
			pc.setSize(size);
			Game.getSelf().getEm().addComponentTo(e, pc);
	
			MoveComponent mc = new MoveComponent(e.getID());
			mc.setVelocity(150);
			Game.getSelf().getEm().addComponentTo(e, mc);
	
			BodyComponent bc = new BodyComponent(e.getID());
			bc.setBaseBox(new Rectangle(0f, 0.8f, 1.0f, 0.2f));
			bc.calculateBaseBox(pos, size);
			bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
			Game.getSelf().getEm().addComponentTo(e, bc);
		}
	}
	
	public void createBoxes(Vec2 startPoint) {
		float basicOffset = 0;
		Vec2 size = new Vec2(12 * 5, 15 * 5);
		Vec2 positions[] = { new Vec2(startPoint.x + basicOffset, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -3, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -2, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -1, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 3, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 4, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 5, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 6, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 2),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 3),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 4) };

		for (int i = 0; i < positions.length; i++) {
			Entity box = Game.getSelf().getEm().newEntity();

			AnimationStateManager asm = new AnimationStateManager();
			Animation a = new Animation("wooden_box", -1);
			a.setFrames(1, new Vec2(0, 0), new Vec2(12, 15));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");

			RenderComponent rc = new RenderComponent(box.getID());
			rc.setSize(size);
			rc.setColor(new Vec4(1, 1, 1, 1));
			rc.setAnimations(asm);
			rc.setRenderer("textureRenderer");
			rc.setRenderPosition(positions[i]);
			rc.setBaseBox(new Rectangle(0f, 0.6f, 1.0f, 0.4f));
			Game.getSelf().getEm().addComponentTo(box, rc);

			BasicComponent pc = new BasicComponent(box.getID());
			pc.setSize(size);
			pc.setPosition(positions[i]);
			Game.getSelf().getEm().addComponentTo(box, pc);

			BodyComponent bc = new BodyComponent(box.getID());
			bc.setBaseBox(new Rectangle(0f, 0.6f, 1.0f, 0.4f));
			bc.calculateBaseBox(positions[i], size);
			bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
			Game.getSelf().getEm().addComponentTo(box, bc);
		}
	}
}
