package gameStates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import engine.engine.Settings;
import engine.logic.Chunk;

public class ChunkMap {
	private HashMap<Integer, HashMap<Integer, Chunk>> chunks;
	private int seed;
	private String mapPath;
	
	public ChunkMap(int seed) {
		chunks = new HashMap<>();
		this.seed = seed;
		mapPath = Settings.mapsFolder+seed+File.separator;
	}
	
	public void put(Chunk chunk) {
		if(chunks.get(chunk.getX())==null)
			chunks.put(chunk.getX(), new HashMap<Integer,Chunk>());
		
		if(chunkExists(chunk)) {
			loadFromFile(chunk.getX(), chunk.getY());
			return;
		}
		
		chunks.get(chunk.getX()).put(chunk.getY(), chunk);
	}
	
	public void update(Chunk chunk) {
		//updates then save chunk, if not exists, create one
	}
	
	public Chunk get(int x, int y) {
		return (chunks.get(x)==null) ? null : chunks.get(x).get(y);
	}
	
	public void loadFromFile(int x, int y) {
		Chunk c;
		
		try {
	         FileInputStream fileIn = new FileInputStream(mapPath+Chunk.getFileName(x, y));
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         c = (Chunk) in.readObject();
	         in.close();
	         fileIn.close();
	      } catch (Exception i) {
	         i.printStackTrace();
	         return;
	      }
		
	}
	
	public void saveFile(Chunk chunk) {
		//TODO: if file exists, check hash to see if it needs udpate
		
		try {
			
			File customDir = new File(mapPath);
			if (customDir.exists() || customDir.mkdirs()) {
		         FileOutputStream fileOut = new FileOutputStream(mapPath+chunk.getFileName());
		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
		         out.writeObject(chunk);
		         out.close();
		         fileOut.close();
			}
	      } catch (IOException i) {
	         i.printStackTrace();
	      }
	}
	
	public void saveEverythingOnDisk() {
		//Saves the whole map on disk
	}
	
	public boolean chunkExists(Chunk chunk) {
		//check both on hashMap and directory
		return false;
	}
	
	public boolean chunkExists(int x, int y) {
		//check both on hashMap and directory
		return false;
	}
}
