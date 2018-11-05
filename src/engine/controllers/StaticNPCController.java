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
import engine.renderer.ASM;
import engine.ui.UIObject;
import engine.utilities.ArraysExt;
import engine.utilities.ResourceManager;
import engine.utilities.Vec2i;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;

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
	
	private void createDialogButton(long parent, EntityManager em) {
		button = em.newEntity();
		PositionComponent pc = new PositionComponent(button.getID());
		RenderComponent rc = new RenderComponent(button.getID());
		
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
	
	private void createDialogBox(long parent, EntityManager em) {
		talkingBox = em.newEntity();
		
		Vec2 parentPos = ((PositionComponent) em.getFirstComponent(parent, PositionComponent.class)).getPosition();
		
		TextComponent tc = new TextComponent(talkingBox.getID());
		tc.setFontColor(new Vec4(1,0,0,1));
		tc.setFontName("monospace");
		tc.setText("Teste dialogo");
		tc.setFontSize(230);
		tc.setPosition(parentPos);
		tc.setDisabled(true);
		
		em.addComponentTo(talkingBox, tc);
	}

	@Override
	public void update(float deltaTime, long EntityID, Game context) {
	
		Action a = ct.calculateAction(EntityID, context.getEm());
		RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(EntityID, RenderComponent.class);
		BodyComponent bc = (BodyComponent) context.getEm().getFirstComponent(EntityID, BodyComponent.class);
		
		if(button==null) {
			createDialogButton(EntityID, context.getEm());
			createDialogBox(EntityID, context.getEm());
		}
		
		TextComponent tc = (TextComponent) context.getEm().getFirstComponent(talkingBox, TextComponent.class);
		if(a.getActionName().equals("talk")) {
			
			tc.setDisabled(false);
		}else {
			tc.setDisabled(true);
		}
		
		boolean flagShowButton = false;
		boolean flagRemove = false;
		
			
		for(Entity e: context.getEm().getAllEntities()){
			RenderComponent erc = (RenderComponent) context.getEm().getFirstComponent(e, RenderComponent.class);
			
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
			((RenderComponent)context.getEm().getFirstComponent(button, RenderComponent.class)).setDisabled(false);
		}else
			((RenderComponent)context.getEm().getFirstComponent(button, RenderComponent.class)).setDisabled(true);
		
	}


}
