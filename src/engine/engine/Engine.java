package engine.engine;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import engine.input.JoystickControl;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.states.GSM;
import engine.utilities.Timer;

public class Engine implements Runnable {
	private Window window;
	private long deltaTime, currentFrame, lastFrame, lastSecond;
	private int updates = 0, maxFPS = 0;
	public static final long MEGABYTE = 1000000L;
	public static final int TARGET_UPDATES = 60;
	public static final float TARGET_UPDATES_DT = 1f / TARGET_UPDATES;
	private float accumulator = 0;
	private static Engine self;
	private long higher = 0;
	private long lower = Long.MAX_VALUE;
	private long avg = 0;
	private int longestDelay = 0;
	private float alphaInterpolator = 1;
	private long higherRender = 0;
	private long avgRender = 0;

	private Engine() {
	}

	public static Engine getSelf() {
		return (self == null) ? self = new Engine() : self;
	}

	/**
	 * Attaches a window only once.
	 * 
	 * @param w
	 */
	public void attachWindow(Window w) {
		if (window == null)
			window = w;
	}

	private void init() {
		window.init();
		initAudioSystem();

		GSM.getSelf().changeStateTo(GSM.GAME_STATE);

		KeyboardControl keyboard = new KeyboardControl();
		MouseControl mouse = new MouseControl();
		JoystickControl joystick = new JoystickControl();

		glfwSetKeyCallback(window.getId(), keyboard);
		glfwSetCursorPosCallback(window.getId(), mouse);

		GSM.getSelf().setKeyboard(keyboard);
		GSM.getSelf().setMouse(mouse);
		GSM.getSelf().setJoystick(joystick);
	}

	private void initAudioSystem() {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		long device = alcOpenDevice(defaultDeviceName);

		int[] attributes = { 0 };
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		AL.createCapabilities(ALC.createCapabilities(device));
	}
	
	/**
	 * This method is called around <code>TARGET_UPDATES</code> times per second.
	 * 
	 * @return
	 */
	private int update() {
		int count = 0;
		currentFrame = System.nanoTime();
		deltaTime = currentFrame - lastFrame;
		lastFrame = currentFrame;

		float deltaTimeMiliSeconds = (float) deltaTime / (float) Timer.SECOND;

		if (deltaTimeMiliSeconds > TARGET_UPDATES_DT * (TARGET_UPDATES * 0.1f))
			deltaTimeMiliSeconds = TARGET_UPDATES_DT * (TARGET_UPDATES * 0.1f);

		accumulator += deltaTimeMiliSeconds;

		while (accumulator > TARGET_UPDATES_DT) {
			count++;
			accumulator -= TARGET_UPDATES_DT;
		}

		alphaInterpolator = accumulator / TARGET_UPDATES_DT;

		for (int i = 0; i < count; i++) {
			long previous = System.nanoTime();

			glfwPollEvents();
			GSM.getSelf().update(TARGET_UPDATES_DT);
			PhysicsEngine.getSelf().update(TARGET_UPDATES_DT);

			long elapsed = System.nanoTime() - previous;
			if (elapsed > higher)
				higher = elapsed;
			else if (elapsed < lower)
				lower = elapsed;

			avg += elapsed;
		}

		if (count > longestDelay)
			longestDelay = count;
		return count;
	}
	
	/**
	 * This method is called as many times as render().
	 */
	public void variableUpdate() {
		GSM.getSelf().variableUpdate(alphaInterpolator);
	}
	
	private void render() {
		long previous = System.nanoTime();

		GSM.getSelf().render();
		glfwSwapBuffers(window.getId());

		long elapsed = System.nanoTime() - previous;
		if (elapsed > higherRender)
			higherRender = elapsed;
		avgRender += elapsed;
	}

	@Override
	public void run() {
		init();
		lastSecond = System.nanoTime();

		while (!glfwWindowShouldClose(window.getId())) {
			updates += update();

			variableUpdate();
			render();
			maxFPS++;

			if ((currentFrame - lastSecond) > Timer.SECOND) {

				long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
				usedMemory = usedMemory / MEGABYTE;
				System.out.printf("\n===============================" + "\nUPS:\t" + updates + "\nFPS:\t" + maxFPS + '\n'
						+ "\nUPS AVG:\t" + (float) (avg / updates) / Timer.MILLISECOND + "ms" + "\nUPS pike:\t"
						+ (float) higher / Timer.MILLISECOND + "ms" + "\nUPS pike calls:\t" + longestDelay + '\n'
						+ "\nFPS AVG:\t" + (float) (avgRender / maxFPS) / Timer.MILLISECOND + "ms" + "\nFPS pike:\t"
						+ (float) higherRender / Timer.MILLISECOND + "\n" + "\nMem. usage:\t" + usedMemory + " MB"

				);

				lastSecond = System.nanoTime();
				updates = 0;
				maxFPS = 0;
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
