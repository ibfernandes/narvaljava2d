package game;


import org.lwjgl.opengl.GL;

import editor.Editor;
import engine.engine.Engine;
import engine.engine.Window;
import engine.logic.Chunk;
import gameStates.ChunkMap;

public class Main {
	public static void main(String args[]) {
		Window w;
		w = new Window(1920,1080,"Engine Early Alpha");
		
		Engine.getSelf().attachWindow(w);
		
		
		//Editor ed = new Editor();

		//ed.run();
		
		//Thread t = new Thread(e);
		//t.start();
		
		//Editor ed = new Editor();

		Thread tr = new Thread(Engine.getSelf());
		tr.start();

		
		//new MainFrame("http://www.google.com", OS.isLinux(), false);
	}
}
