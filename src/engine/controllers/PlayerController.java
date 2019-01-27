package engine.controllers;

import java.util.Arrays;

import org.lwjgl.glfw.GLFW;

import demo.Game;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.states.GSM;
import engine.utilities.ArraysExt;
import glm.vec._2.Vec2;

public class PlayerController extends Controller {
	private Vec2 faceLeft = new Vec2(1, 0);
	private Vec2 faceRight = new Vec2(0, 0);
	private boolean directions[] = new boolean[4];

	public boolean attackFinished(RenderComponent rc) {
		if (rc.getAnimations().getCurrentAnimationName() == "attacking"
				&& rc.getAnimations().getCurrentAnimation().hasPlayedOnce())
			return true;
		if (rc.getAnimations().getCurrentAnimationName() != "attacking")
			return true;
		return false;
	}

	@Override
	public void renderDebug() {
	}

	@Override
	public void update(float deltaTime, long entityID, Game context) {

		Arrays.fill(directions, false);

		RenderComponent rc = (RenderComponent) context.getEm().getFirstComponent(entityID, RenderComponent.class);
		MoveComponent mc = (MoveComponent) context.getEm().getFirstComponent(entityID, MoveComponent.class);

		Vec2 dir = new Vec2(0, 0);

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_W)) {
			dir.y = -1;
			directions[0] = true;
		}

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_S)) {
			dir.y = 1;
			directions[1] = true;
		}

		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_A)) {
			dir.x = -1;

			rc.setOrientation(faceLeft);
			directions[2] = true;
		}
		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_D)) {
			dir.x = 1;

			rc.setOrientation(faceRight);
			directions[3] = true;
		}

		mc.setDirection(dir);
		// mc.setDirection(GSM.getSelf().getJoystick().getThumbDirection(JoystickControl.LEFT_THUMB_STICK));

		if (ArraysExt.areAllElementsEqualTo(directions, false)) {
			rc.getAnimations().changeStateTo("idle_1");
		}

	}
}
