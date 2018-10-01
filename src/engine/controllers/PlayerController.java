package engine.controllers;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.logic.GameObject;
import engine.utilities.ArraysExt;
import gameStates.GSM;
import gameStates.Game;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class PlayerController  extends Controller{
	private Vec2 faceLeft = new Vec2(1,0);
	private Vec2 faceRight = new Vec2(0,0);
	private boolean directions[] = new boolean[4]; 
	
	@Override
	public void update(float deltaTime, GameObject object, Game context) {
		Arrays.fill(directions, false); // TODO: for now i could use only one boolean (isMoving)
		float xMove =0;
		float yMove =0;
		
		if(object.getAnimations().getCurrentAnimationName()=="attacking" && object.getAnimations().getCurrentAnimation().hasPlayedOnce())
			for(GameObject g: context.getQuadTree().queryRange(object.getInterationBox()))
				if(!g.equals(object))
					context.removeObject(g);
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W)) {
			yMove = -object.getVelocity()*deltaTime;
			directions[0] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) {
			yMove = object.getVelocity()*deltaTime;
			directions[1] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) {
			xMove = -object.getVelocity()*deltaTime;
			
			object.setOrientation(faceLeft);
			if(attackFinished(object))
				object.getAnimations().changeStateTo("walking");
			directions[2] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) {
			xMove = object.getVelocity()*deltaTime;
			
			if(attackFinished(object))
				object.getAnimations().changeStateTo("walking");
			object.setOrientation(faceRight);
			directions[3] = true;
		}
		
		if(GSM.getSelf().getMouse().isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {

			object.getAnimations().changeStateTo("attacking");
			object.getAnimations().getCurrentAnimation().setPlayedOnce(false);
		}
		
		org.jbox2d.common.Vec2  speed = new  org.jbox2d.common.Vec2(0, 0);
		if(xMove>0) {
			speed.x = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
		}else if(xMove<0) {
			speed.x = -object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
		}
		
		if(yMove>0) {
			speed.y = object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
		}else if(yMove<0) {
			speed.y = -object.getVelocity()/PhysicsEngine.BOX2D_SCALE_FACTOR;
		}
		
		object.getBody().setLinearVelocity(speed);
		
		//object.move(xMove, yMove);
		
		
		if(ArraysExt.areAllElementsEqual(directions, false)){
			if(attackFinished(object))
				object.getAnimations().changeStateTo("idle_1");
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_E)) {
			
		}
	}
	
	public boolean attackFinished(GameObject object) {
		if(object.getAnimations().getCurrentAnimationName()=="attacking" && object.getAnimations().getCurrentAnimation().hasPlayedOnce())
			return true;
		if(object.getAnimations().getCurrentAnimationName()!="attacking")
			return true;
		return false;
	}

	@Override
	public void renderDebug() {
	}

	@Override
	public void update(float deltaTime, Entity e, EntityManager context) {
		
		Arrays.fill(directions, false); // TODO: for now i could use only one boolean (isMoving)
		float xMove =0;
		float yMove =0;
		
		
		RenderComponent rc = (RenderComponent) context.getComponent(e, RenderComponent.class).get(0);
		MoveComponent mc = (MoveComponent) context.getComponent(e, MoveComponent.class).get(0);
		
		Vec2 dir = new Vec2(0,0);
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W)) {
			dir.y = -1;
			directions[0] = true;
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) {
			dir.y = 1;
			directions[1] = true;
		}
		
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) {
			dir.x = -1;
			
			rc.setOrientation(faceLeft);
			directions[2] = true;
		}
		if(GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) {
			dir.x = 1;
			
			rc.setOrientation(faceRight);
			directions[3] = true;
		}
		
		mc.direction = dir;

		if(ArraysExt.areAllElementsEqual(directions, false)){
			rc.getAnimations().changeStateTo("idle_1");
		}

	}
}
