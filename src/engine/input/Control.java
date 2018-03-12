package engine.input;

public interface Control {
	public abstract boolean isKeyReleased(int key);
	public abstract boolean isKeyPressed(int key);
}
