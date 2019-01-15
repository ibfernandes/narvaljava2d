package engine.input;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;

import java.nio.FloatBuffer;

import org.lwjgl.glfw.GLFWJoystickCallback;

import glm.vec._2.Vec2;

public class JoystickControl  implements Control{ //TODO I need to put some work in here
	public boolean up,right,down,left;
	public static final int LEFT_THUMB_STICK  = 0,
							RIGHT_THUMB_STICK = 1;
	
	
	public void update() {
			FloatBuffer b = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
			float asisX = b.get(0);
			float asisY = b.get(1);
			
			if(asisX>0.5)
				right = true;
			else if(asisX<-0.5)
				left = true;
			else if(asisX==0){
				right = false;
				left = false;
			}
			
			if(asisY>0.5)
				up = true;
			else if(asisY<-0.5)
				down = true;
			else if(asisY==0){
				up = false;
				down = false;
			}

	}
	
	public Vec2 getThumbDirection(int button) {
		FloatBuffer b = glfwGetJoystickAxes(GLFW_JOYSTICK_1);
		if(b==null)
			return null;
		
		if(button == LEFT_THUMB_STICK) {
			float xAxis = b.get(0);
			float yAxis = b.get(1);
			return new Vec2(xAxis, yAxis);
		}else if(button == RIGHT_THUMB_STICK) {
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

}
