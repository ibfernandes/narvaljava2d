package game;


import org.lwjgl.opengl.GL;

import editor.Editor;
import engine.engine.Engine;
import engine.engine.Window;
import engine.logic.Chunk;
import engine.logic.ChunkMap;
import engine.utilities.ResourceManager;
public class Main {
	public static void main(String args[]) {
		Window w;
		w = new Window(1280,720,"Engine Early Alpha");
		
		Engine.getSelf().attachWindow(w);
		
		
		//Editor ed = new Editor();

		//ed.run();
		
		//Thread t = new Thread(e);
		//t.start();
		
		//Editor ed = new Editor();

		
		 String filePath = "/SourceSansPro.ttf";

		 //new TruetypeFont(24,filePath).run("STB Truetype Demo");
	     //TruetypeFont test = new TruetypeFont(24,filePath);
	      //test.run("a");
		
		Thread tr = new Thread(Engine.getSelf());
		tr.start();

		
		//new MainFrame("http://www.google.com", OS.isLinux(), false);
	}
}
