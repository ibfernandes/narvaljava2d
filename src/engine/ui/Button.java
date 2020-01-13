package engine.ui;

import org.lwjgl.glfw.GLFW;

import engine.geometry.Rectangle;
import engine.renderer.CubeRenderer;
import engine.renderer.UIRenderer;
import engine.states.GSM;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Button extends UIComponent{
	
	private int width, height;
	private Rectangle boundingBox = new Rectangle(0,0,0,0);
	private Vec4 currentColor;
	private Vec4 color;
	private Vec4 hoverColor;
	private boolean isHover = false;
	private String text;

	
	public Button(float x , float y, int width, int height){
		super(x,y);
		this.width = width;
		this.height = height;
		
		boundingBox.width = width;
		boundingBox.height = height;
	}

	@Override
	public void update() {
		boundingBox.x = this.getX();
		boundingBox.y = this.getY();
		
		if(boundingBox.intersects(GSM.getSelf().getMouse().getCursorPos())) {
			isHover = true;
		}else {
			isHover = false;
		}
	}

	
	public void onClick() {
		if(GSM.getSelf().getMouse().isKeyPressed(GLFW.GLFW_MOUSE_BUTTON_2)) {
			
		}
	}

	@Override
	public void render() {
		if(isHover)
			currentColor = hoverColor;
		else
			currentColor = color;
		
		((UIRenderer) ResourceManager.getSelf().getRenderer("uiRenderer")).render(boundingBox.getPos(), boundingBox.getSize(), 0,
				currentColor, new Vec4(0,0,1,1), new Vec2(0,0));
	}

	public Vec4 getColor() {
		return color;
	}

	public void setColor(Vec4 color) {
		this.color = color;
	}

	public Vec4 getHoverColor() {
		return hoverColor;
	}

	public void setHoverColor(Vec4 hoverColor) {
		this.hoverColor = hoverColor;
	}
	
	public void onClickCallback() {
		
	}
}
