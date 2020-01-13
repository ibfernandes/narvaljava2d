package engine.ui;

import java.util.ArrayList;

import engine.renderer.UIRenderer;
import engine.utilities.ResourceManager;

public class Panel {
	private float x,y;
	private ArrayList<UIComponent> components;
	
	public Panel() {
		components = new ArrayList<>();
	}
	
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	
	public void move(float x, float y) {
		
	}
	public ArrayList<UIComponent> getComponents() {
		return components;
	}
	
	public void addComponent(UIComponent component) {
		components.add(component);
	}
	
	public void setComponents(ArrayList<UIComponent> components) {
		this.components = components;
	}
	
	//Look up for special cases as dragging and key shortcuts
	public void update() {
		for(UIComponent c: components) {
			//if intersects Component, update it
			c.update();
		}
	}
	
	public void render() {
		((UIRenderer) ResourceManager.getSelf().getRenderer("uiRenderer")).start("ui_atlas");
		for(UIComponent c: components) 
			c.render();
		((UIRenderer) ResourceManager.getSelf().getRenderer("uiRenderer")).end();
	}
}
