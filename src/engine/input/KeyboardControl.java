package engine.input;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;

public class KeyboardControl extends GLFWKeyCallback implements  Control {
	private boolean keys[] = new boolean[512];

	@Override
	public void invoke(long window, int key, int scancode, int action, int mods) {
		keys[key] = action != GLFW.GLFW_RELEASE;
	}

	@Override
	public boolean isKeyPressed(int key) {
		return keys[key];
	}

	@Override
	public boolean isKeyReleased(int key) {
		return keys[key];
	}
}
