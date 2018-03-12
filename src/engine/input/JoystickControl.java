package engine.input;

import static org.lwjgl.glfw.GLFW.GLFW_JOYSTICK_1;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickAxes;
import static org.lwjgl.glfw.GLFW.glfwGetJoystickName;

import java.nio.FloatBuffer;

public class JoystickControl implements Control{ //TODO I need to put some work in here
	public boolean up,right,down,left;
	
	@Override
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
	
}
