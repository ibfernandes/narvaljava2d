package game;


import engine.engine.Engine;
import engine.engine.Window;
import engine.logic.EngineSettings;
public class Main {
	public static void main(String args[]) {
		
		EngineSettings.getSelf().setWindowSize(1280, 720);
		EngineSettings.getSelf().setMapSize(60000, 60000);
		EngineSettings.getSelf().setChunkSize(1920, 1080);
		EngineSettings.getSelf().setMapSeed(12345);
		
		Window w;
		w = new Window(EngineSettings.getSelf().getWindowSizeX(),EngineSettings.getSelf().getWindowSizeY(),"Engine Early Alpha");
		
		Engine.getSelf().attachWindow(w);

		Thread tr = new Thread(Engine.getSelf());
		tr.start();
	}
}
