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
import engine.entity.component.BodyComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.TextComponent;
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
	private Entity button;
	private Entity talkingBox;
	
	public StaticNPCController() {
		ct.addConsideration(new ConsiderationTalk());
		
	}


	@Override
	public void renderDebug() {

	}
	
	private void createDialogButton(Entity parent, EntityManager em) {
		button = em.newEntity();
		PositionComponent pc = new PositionComponent();
		RenderComponent rc = new RenderComponent();
		
		Vec2 parentPos = ((PositionComponent) em.getFirstComponent(parent, PositionComponent.class)).getPosition();
		pc.setPosition(parentPos);
		em.addComponentTo(button, pc);
		
		rc.setSize(new Vec2(32,32));
		rc.setColor(new Vec4(1,1,1,1));
		rc.setOrientation(new Vec2(0,0));
		rc.setRenderPosition(parentPos);
		//TODO: renderPos
		
		ASM asm = new ASM();
		Animation an = new Animation("e_button", -1);
		an.setFrames(1, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", an);
		
		asm.changeStateTo("idle_1");
		
		rc.setAnimations(asm);
		em.addComponentTo(button, rc);
	}
	
	private void createDialogBox(Entity parent, EntityManager em) {
		talkingBox = em.newEntity();
		
		Vec2 parentPos = ((PositionComponent) em.getFirstComponent(parent, PositionComponent.class)).getPosition();
		
		TextComponent tc = new TextComponent();
		tc.setFontColor(new Vec4(1,0,0,1));
		tc.setFontName("monospace");
		tc.setText("È     ÈText Component text! È");
		tc.setFontSize(230);
		tc.setPosition(parentPos);
		tc.setDisabled(true);
		
		em.addComponentTo(talkingBox, tc);
	}

	@Override
	public void update(float deltaTime, Entity object, EntityManager context) {
	
		Action a = ct.calculateAction(object, context);
		RenderComponent rc = (RenderComponent) context.getFirstComponent(object, RenderComponent.class);
		BodyComponent bc = (BodyComponent) context.getFirstComponent(object, BodyComponent.class);
		
		if(button==null) {
			createDialogButton(object, context);
			createDialogBox(object, context);
		}
		
		TextComponent tc = (TextComponent) context.getFirstComponent(talkingBox, TextComponent.class);
		//if(a.getActionName().equals("talk")) {
			
			tc.setDisabled(false);
		//}else {
		//	tc.setDisabled(true);
		//}
		
		boolean flagShowButton = false;
		boolean flagRemove = false;
		
			
		for(Entity e: context.getAllEntities()){
			RenderComponent erc = (RenderComponent) context.getFirstComponent(e, RenderComponent.class);
			
			if(e.getName()!=null && e.getName().equals("player") && rc.getBoundingBox().intersects(erc.getBoundingBox())) {
				if(!flagShowButton) {
					flagShowButton = true;
					break;
				}
			}else {
				flagShowButton = false;
				break;
			}
		}
		
		if(flagShowButton) {
			((RenderComponent)context.getFirstComponent(button, RenderComponent.class)).setDisabled(false);
		}else
			((RenderComponent)context.getFirstComponent(button, RenderComponent.class)).setDisabled(true);
		
	}


}
