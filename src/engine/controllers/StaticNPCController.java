package engine.controllers;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.jbox2d.dynamics.BodyType;
import org.lwjgl.glfw.GLFW;

import engine.ai.AStar;
import engine.ai.Action;
import engine.ai.Anode;
import engine.ai.Consideration;
import engine.ai.ConsiderationAttack;
import engine.ai.ConsiderationTalk;
import engine.ai.ConsiderationTree;
import engine.ai.ConsiderationWander;
import engine.ai.State;
import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.graphic.Animation;
import engine.logic.GameObject;
import engine.ui.UIObject;
import engine.utilities.ArraysExt;
import engine.utilities.ResourceManager;
import engine.utilities.Vec2i;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import graphic.ASM;

public class StaticNPCController extends Controller{

	private ConsiderationTree ct = new ConsiderationTree();
	private GameObject button;
	private UIObject talkingBox;
	
	public StaticNPCController() {
		ct.addConsideration(new ConsiderationTalk());
	}

	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		Action a = ct.calculateAction(object, context);
		
		if(a.getActionName().equals("talk")) {
			talkingBox = new UIObject();
			talkingBox.setPosition(new Vec2(object.getPosition().x + object.getSize().x/2 - button.getSize().x/2 + 90, object.getPosition().y - button.getSize().y -15));
			talkingBox.setFont("monospace");
			talkingBox.setText("Oi =]");
		}
		
		boolean flagAdd = false;
		boolean flagRemove = false;
		
		if(button==null) {
			button = new GameObject();
			button.setSize(new Vec2(32,32));
			button.setBaseBox(new Vec2(32,32));
			button.setVelocity(0);
			button.setColor(new Vec4(1,1,1,1));
			button.setController(null);
			button.setOrientation(new Vec2(0,0));
			button.setPosition(new Vec2(object.getPosition().x + object.getSize().x/2 - button.getSize().x/2, object.getPosition().y - button.getSize().y -15));
			
			ASM asm = new ASM();
			Animation an = new Animation("e_button", -1);
			an.setFrames(1, new Vec2(0,0), new Vec2(32,32));
			asm.addAnimation("idle_1", an);
			
			asm.changeStateTo("idle_1");
			
			button.setAnimations(asm);
			
			
		}
		
		for(GameObject o: context.getFinalLayer()) {
			if(o.getGroup()!=null && o.getGroup().equals("player") && object.getInterationBox().intersects(o.getInterationBox())) {
				if(!context.getStaticLayer().contains(button)) {
					flagAdd = true;
					break;
				}
			}else if(o.getGroup()!=null && o.getGroup().equals("player") && context.getStaticLayer().contains(button)) {
				flagRemove = true;
				break;
			}
		}
		
		if(flagAdd) {
			context.getStaticLayer().add(button);
			context.getUiLayer().add(talkingBox);

		}else if(flagRemove) {
			context.getStaticLayer().remove(button);
			context.getUiLayer().remove(talkingBox);
		}
			
		
	}

	@Override
	public void renderDebug() {

	}

	@Override
	public void update(float deltaTime, Entity object, EntityManager context) {
	}


}
