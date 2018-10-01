package engine.input;


import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import engine.engine.Engine;


public class MouseControl extends GLFWCursorPosCallback implements Control{
	int previousState = -1;
	
	@Override
	public boolean isKeyReleased(int key) {
		int state = glfwGetMouseButton(Engine.getSelf().getWindow().getId(), key);
		
		if (state == GLFW_RELEASE) 
			return true;
		else
			return false;
	}

	@Override
	public boolean isKeyPressed(int key) {
		int state = glfwGetMouseButton(Engine.getSelf().getWindow().getId(), key);
		
		if (state == GLFW_RELEASE &&  previousState== GLFW_PRESS) {
			previousState=state;
			return true;
		}else {
			previousState=state;
			return false;
		}
	}


	@Override
	public void invoke(long window, double xpos, double ypos) {
	}
}
