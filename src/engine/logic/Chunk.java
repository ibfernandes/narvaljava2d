package engine.logic;

import java.io.Serializable;

import engine.utilities.Vec2i;

public class Chunk implements Serializable{
	
	private int 	mapRGB[][];
	private int	x,y;
	
	//Should pass rules to map generation
	//Should get its size from something static final since it'll be the same for all of them.
	public Chunk (int x, int y, int chunkWidth, int chunkHeight) { 
		
		this.x = x;
		this.y = y;
		mapRGB = new int[chunkWidth][chunkHeight];
		generateMap();
		generateGameObjectLayers();
		
	}
	
	public String getFileName() {
		return x+"_"+y+".chunck";
	}
	
	public static String getFileName(int x, int y) {
		return x+"_"+y+".chunck";
	}
	
	private void generateMap() {
		//generates the map using a rule.
	}
	
	private void generateStaticLayer() {
		
	}
	private void generateMovableLayer() {
		
	}
	
	private void generateGameObjectLayers() {
		generateStaticLayer();
		generateMovableLayer();
	}
	
	public Vec2i getPosition() {
		return new Vec2i(x,y);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
}
