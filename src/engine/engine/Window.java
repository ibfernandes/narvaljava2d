package engine.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.opengl.GL;

public class Window {
	private int width;
	private int height;
	private String name;
	private long id;
	
	
	public Window(int width, int height, String name) {
		this.width = width;
		this.height = height;
		this.name = name;
		
	}
	
	public void init() {
		if (!glfwInit()) 
			System.err.println("Could not initialize GLFW.");
		
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
	    
	    id = glfwCreateWindow(width, height, name,  NULL,  NULL);
	    glfwMakeContextCurrent(id);
	    
	    glfwSetWindowPos(id, 2000, 60); //TODO: [NOTE]Set it to my second monitor (for debug purposes)
	    glfwShowWindow(id);
		GL.createCapabilities();
	    
	    glViewport(0,0, width, height);
	    glEnable(GL_CULL_FACE);
	    glEnable(GL_BLEND);
	    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
}