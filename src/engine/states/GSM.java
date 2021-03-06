package engine.states;

import demo.Game;
import engine.engine.GameState;
import engine.input.JoystickControl;
import engine.input.KeyboardControl;
import engine.input.MouseControl;

public final class GSM {
	private static GSM self;
	private GameState actualState;
	private KeyboardControl keyboard;
	private MouseControl mouse;
	private JoystickControl joystick;
	public static final int GAME_STATE = 0;
	public static final int MENU_STATE = 1;
	public static int CURRENT_STATE = GAME_STATE;

	private GSM() {
	}

	public static GSM getSelf() {
		if (self == null)
			self = new GSM();
		return self;
	}

	public void changeStateTo(int state) {
		switch (state) {
		case GAME_STATE:
			CURRENT_STATE = state;
			actualState = Game.getSelf();
			actualState.init();
			break;
		default:
			break;
		}
	}

	public void render() {
		actualState.render();
	}

	public void update(float deltaTime) {
		actualState.update(deltaTime);
	}

	public void variableUpdate(float deltaTime) {
		actualState.variableUpdate(deltaTime);
	}

	public void setKeyboard(KeyboardControl keyboard) {
		this.keyboard = keyboard;
	}

	public void setMouse(MouseControl mouse) {
		this.mouse = mouse;
	}

	public MouseControl getMouse() {
		return mouse;
	}

	public KeyboardControl getKeyboard() {
		return keyboard;
	}

	public JoystickControl getJoystick() {
		return joystick;
	}

	public void setJoystick(JoystickControl joystick) {
		this.joystick = joystick;
	}
}
