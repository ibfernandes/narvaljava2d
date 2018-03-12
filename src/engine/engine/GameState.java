package engine.engine;


public abstract class GameState {
	public abstract void init();
	public abstract void render();
	public abstract void update(float deltaTime);
}
