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

		Thread tr = new Thread(Engine.getSelf());
		tr.start();

	}
}
