package engine.input;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.GLFW.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import engine.Engine;
import state.GSM;

public class MouseControl extends GLFWCursorPosCallback implements MouseListener, MouseMotionListener, Control{
	public Point lastPoint = new Point();
	public boolean clicked = false;
	@Override
	public void mouseClicked(MouseEvent e) {
		//scalePoint(e.getPoint());
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		//scalePoint(e.getPoint());
	}

	@Override
	public void mouseExited(MouseEvent e) {
		//scalePoint(e.getPoint());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		clicked = false;
		//scalePoint(e.getPoint());
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		clicked = true;
		scalePoint(e.getPoint());	
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		//scalePoint(e.getPoint());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		clicked = false;
		scalePoint(e.getPoint());
	}

	private void scalePoint(Point p){
		//System.out.printf("(%d, %d)\n",p.x,p.y);
		lastPoint.x = (int) ((float)p.x*ASM.scaleX);
		lastPoint.y = (int) ((float)p.y*ASM.scaleY);
	}

	@Override
	public void update() {
		DoubleBuffer b1 = BufferUtils.createDoubleBuffer(1);
		DoubleBuffer b2 = BufferUtils.createDoubleBuffer(1);
		glfwGetCursorPos(Engine.windowGL, b1, b2);
		scalePoint(new Point((int)b1.get(0),(int)b2.get(0)));
		
		int state = glfwGetMouseButton(Engine.windowGL, GLFW_MOUSE_BUTTON_LEFT);
		if (state == GLFW_PRESS) {
			clicked = true;
		}else{
			clicked = false;
		}
		
		//System.out.println("x : " + b1.get(0) + ", y = " + b2.get(0));
	}

	@Override
	public void invoke(long window, double xpos, double ypos) {
	}

	@Override
	public void callback(long args) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close(){
		// TODO Auto-generated method stub
		
	}

}
