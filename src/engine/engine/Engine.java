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
	public static final int TARGET_UPDATES = 60;
	public static final float TARGET_DT = 1f/(float)TARGET_UPDATES; //TODO: use double instead of float
	private float accumulator = 0;
	private static Engine self;
	private long higher = 0;
	private long lower = Long.MAX_VALUE;
	private long avg = 0;
	private int longestDelay = 0;
	private float alphaInterpolator = 1;
	
	private long higherRender = 0;
	private long avgRender = 0;

	private Engine() {}
	
	public static Engine getSelf() {
		if(self==null) 
			self = new Engine();
		
		return self;
	}
	
	/**
	 * Attach a window only and only once.
	 * @param w
	 */
	public void attachWindow(Window w) {
		if(window==null)
			window = w;
	}
	
	private void init() {
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
	
	private void initAudioSystem() {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		long device = alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
	}
	
	private int update() {
		int count = 0;
		currentFrame = System.nanoTime(); 
		deltaTime = currentFrame - lastFrame;
		lastFrame = currentFrame;
		
		float deltaTimeMiliSeconds = (float)deltaTime/(float)SECOND;
		
		if(deltaTimeMiliSeconds> TARGET_DT*(TARGET_UPDATES*0.1f))
			deltaTimeMiliSeconds = TARGET_DT*(TARGET_UPDATES*0.1f);
		
		accumulator += deltaTimeMiliSeconds;
			
		while (accumulator > TARGET_DT) {
			count++;
            accumulator -= TARGET_DT;
		}
		
		alphaInterpolator = accumulator / TARGET_DT;
		
		for(int i=0; i<count;i++) {
			long previous = System.nanoTime();
			
			glfwPollEvents();
			GSM.getSelf().update(TARGET_DT);
			PhysicsEngine.getSelf().update(TARGET_DT);
			
			long elapsed = System.nanoTime() - previous;
			if(elapsed>higher)
				higher = elapsed;
			else if(elapsed<lower)
				lower = elapsed;
			
			avg +=elapsed;
		}
		
		if(count>longestDelay)
			longestDelay = count;
		return count;
	}
	
	public void variableUpdate() {
		GSM.getSelf().variableUpdate(alphaInterpolator);
	}
	
	private void render() {
		long previous = System.nanoTime();

		GSM.getSelf().render();
		glfwSwapBuffers(window.getId());
		
		long elapsed = System.nanoTime() - previous;
		if(elapsed>higherRender)
			higherRender = elapsed;
		avgRender +=elapsed;
	}
	
	@Override
	public void run() {
		init();
		lastSecond = System.nanoTime();
		
		while(!glfwWindowShouldClose(window.getId())) {
			updates += update();
			
			variableUpdate();
			render();
			fps++;
			
			if((currentFrame-lastSecond)>SECOND) {
				
				System.out.printf(
								"\n==============================="+
								"\nUPS:\t"+updates+
								"\nFPS:\t"+fps+'\n'+
								"\nUPS AVG:\t"+(float)(avg/updates)/MILISECOND+"ms"+
								"\nUPS pike:\t"+(float)higher/MILISECOND+"ms"+
								"\nUPS pike calls:\t"+longestDelay+'\n'+
								"\nFPS AVG:\t"+(float)(avgRender/fps)/MILISECOND+"ms"+
								"\nFPS pike:\t"+(float)higherRender/MILISECOND
								
						);
				
				lastSecond = System.nanoTime();
				updates = 0;
				fps = 0;
				lower = Long.MAX_VALUE;
				higher = 0;
				avg = 0;
				longestDelay = 0;
				
				avgRender = 0;
				higherRender = 0;
			}
			
		}
	}

	public Window getWindow() {
		return window;
	}

	public float getAccumulator() {
		return accumulator;
	}

	public float getAlphaInterpolator() {
		return alphaInterpolator;
	}
}
