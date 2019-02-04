package engine.input;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;
import java.nio.FloatBuffer;

import glm.vec._2.Vec2;

public class JoystickControl implements Control {
	public boolean up, right, down, left;
	public static final int LEFT_THUMB_STICK = 0, RIGHT_THUMB_STICK = 1;

	/**
	 * Returns either LEFT_THUMB_STICK or RIGHT_THUMB_STICK direction vector.
	 * 
	 * @param button
	 * @return
	 */
	public Vec2 getThumbDirection(int button) {
		FloatBuffer b = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
		if (b == null)
			return null;

		if (button == LEFT_THUMB_STICK) {
			float xAxis = b.get(0);
			float yAxis = b.get(1);
			return new Vec2(xAxis, yAxis);
		} else if (button == RIGHT_THUMB_STICK) {
			float xAxis = b.get(2);
			float yAxis = b.get(3);
			return new Vec2(xAxis, yAxis);
		}

		return null;
	}

	@Override
	public boolean isKeyReleased(int key) {
		return false;

	}

	@Override
	public boolean isKeyPressed(int key) {
		return false;
	}
	
	public boolean isThereAJoystick() {
		return (glfwGetJoystickName(GLFW_JOYSTICK_1)==null) ? false : true;
	}

}
