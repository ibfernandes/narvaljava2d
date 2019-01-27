package engine.input;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import engine.engine.Engine;
import glm.vec._2.Vec2;

public class MouseControl extends GLFWCursorPosCallback implements Control {
	private int previousState = -1;
	private Vec2 cursorPos = new Vec2(0, 0);

	/**
	 * Returns true when @param key is released.
	 */
	@Override
	public boolean isKeyReleased(int key) {
		int state = glfwGetMouseButton(Engine.getSelf().getWindow().getId(), key);

		if (state == GLFW_RELEASE)
			return true;
		else
			return false;
	}

	/**
	 * Returns true when @param key was being pressed and is now released.
	 */
	@Override
	public boolean isKeyPressed(int key) {
		int state = glfwGetMouseButton(Engine.getSelf().getWindow().getId(), key);

		if (state == GLFW_RELEASE && previousState == GLFW_PRESS) {
			previousState = state;
			return true;
		} else {
			previousState = state;
			return false;
		}
	}

	/**
	 * Updates mouse cursor position. Its position is relative to the screen.
	 */
	@Override
	public void invoke(long window, double xpos, double ypos) {
		cursorPos.x = (float) xpos;
		cursorPos.y = (float) ypos;
	}

	/**
	 * Gets mouse cursor position. Its position is relative to the screen.
	 */
	public Vec2 getCursorPos() {
		return cursorPos;
	}
}
