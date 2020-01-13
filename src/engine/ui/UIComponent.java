package engine.ui;

public abstract class UIComponent {
	private float x,y;
	
	public UIComponent(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}
	
	public void setPos(float x, float y) {
		
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public abstract void update();
	public abstract void render();
}
