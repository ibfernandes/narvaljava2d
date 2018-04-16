package engine.engine;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.opengl.GL11.*;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;

import engine.input.KeyboardControl;
import engine.input.MouseControl;
import gameStates.GSM;

public class Engine implements Runnable{
	private Window window;
	private long deltaTime, currentFrame, lastFrame, lastSecond;
	private int updates = 0, fps = 0;
	private KeyboardControl keyboard;
	private MouseControl mouse;
	public static final long SECOND = 1000000000L; //10^9
	public static final long MILISECOND = 1000000L;//10^6
	
	
	public Engine(Window w) {
		window = w;
	}
	
	public void initAudioSystem() {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		long device = alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
	}
	
	public void init() {
		window.init();
		initAudioSystem();
		
		GSM.getSelf().changeStateTo(GSM.GAME_STATE);
		
		keyboard = new KeyboardControl();
		mouse = new MouseControl();
		
		glfwSetKeyCallback(window.getId(), keyboard);
		glfwSetCursorPosCallback(window.getId(), mouse);
		
		GSM.getSelf().setKeyboard(keyboard);
		GSM.getSelf().setMouse(mouse);
	}
	
	public void update() {
		currentFrame = System.nanoTime(); 
		deltaTime = currentFrame - lastFrame;
		lastFrame = currentFrame;
		
		glfwPollEvents();
		
		GSM.getSelf().update((float)deltaTime/(float)SECOND);
	}
	
	public void render() {
		//glClearColor(1,0,1,1);
		//glClear(GL_COLOR_BUFFER_BIT);
		
		GSM.getSelf().render();
		glfwSwapBuffers(window.getId());
	}
	
	@Override
	public void run() {
		init();
		lastSecond = System.nanoTime();
		
		while(!glfwWindowShouldClose(window.getId())) {
			update();
			updates++;
			
			render();
			fps++;
			
			if((currentFrame-lastSecond)>SECOND) {
				
				System.out.printf(
								"\n==============================="+
								"\nUPS:\t"+updates+
								"\nFPS:\t"+fps
						);
				
				lastSecond = System.nanoTime();
				updates = 0;
				fps = 0;
			}
			
		}
	}
}