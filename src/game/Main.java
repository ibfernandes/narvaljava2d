package game;


import org.lwjgl.opengl.GL;

import editor.Editor;
import engine.engine.Engine;
import engine.engine.Window;
import tests.MainFrame;

public class Main {
	public static void main(String args[]) {
		Window w;
		w = new Window(1280,720,"Engine Early Alpha");
		
		Engine e;
		e = new Engine(w);
		
		Editor ed = new Editor();

		//ed.run();
		
		//Thread t = new Thread(e);
		//t.start();
		
		//Editor ed = new Editor();

		Thread tr = new Thread(e);
		tr.start();
		
		//new MainFrame("http://www.google.com", OS.isLinux(), false);
	}
}
