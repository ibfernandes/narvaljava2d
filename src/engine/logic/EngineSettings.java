package engine.logic;

import engine.utilities.Vec2i;

public class EngineSettings {
	private static EngineSettings self;
	private Vec2i windowSize;
	private Vec2i mapSize;
	private Vec2i chunkSize;
	private int mapSeed;
	
	private EngineSettings() {}
	
	public static EngineSettings getSelf() {
		return (self==null) ? self = new EngineSettings(): self;
	}
	
	public int getWindowSizeX() {
		return windowSize.x;
	}
	
	public int getWindowSizeY() {
		return windowSize.y;
	}
	
	public Vec2i getWindowSize() {
		return windowSize;
	}

	public void setWindowSize(Vec2i windowSize) {
		this.windowSize = windowSize;
	}
	
	public void setWindowSize(int x, int y) {
		this.windowSize = new Vec2i(x,y);
	}
	
	public int getMapSizeX() {
		return mapSize.x;
	}
	
	public int getMapSizeY() {
		return mapSize.y;
	}
	
	public Vec2i getMapSize() {
		return mapSize;
	}

	public void setMapSize(Vec2i mapSize) {
		this.mapSize = mapSize;
	}
	
	public void setMapSize(int x, int y) {
		this.mapSize = new Vec2i(x,y);
	}
	
	public int getChunkSizeX() {
		return chunkSize.x;
	}
	
	public int getChunkSizeY() {
		return chunkSize.y;
	}

	public Vec2i getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(Vec2i chunkSize) {
		this.chunkSize = chunkSize;
	}
	public void setChunkSize(int x, int y) {
		this.chunkSize = new Vec2i(x,y);
	}

	public int getMapSeed() {
		return mapSeed;
	}

	public void setMapSeed(int mapSeed) {
		this.mapSeed = mapSeed;
	}
}
