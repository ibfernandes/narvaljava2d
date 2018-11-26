package engine.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.Future;

import engine.engine.Engine;
import engine.engine.Settings;
import engine.entity.EntityManager;

public class ChunkMap {
	private HashMap<Integer, HashMap<Integer, Chunk>> chunks; //TODO: could change to a matrix and keep shifting it
	private int seed;
	private String mapPath;
	private int mapSize = 0; //How many chunks are stored now.
	private ArrayList<ReadingChunk> loadingChunks = new ArrayList<>();
	private ArrayList<ReadingChunk> loadingChunksToRemove = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunks = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunksToRemove = new ArrayList<>();
	private ArrayList<Chunk> chunksToSave = new ArrayList<>();
	private EntityManager em;
	
	public static final int CHUNK_WIDTH = 1920;
	public static final int CHUNK_HEIGHT = 1080;
	public static final int CHUNK_BUFFER_SIZE = ((CHUNK_WIDTH*CHUNK_HEIGHT)/CHUNK_WIDTH)*1000;
	public static final int MAP_WIDTH = 60000;
	public static final int MAP_HEIGHT = 60000;
	
	public ChunkMap(int seed, EntityManager em) {
		this.em = em;
		chunks = new HashMap<>();
		this.seed = seed;
		mapPath = Settings.mapsFolder+seed+File.separator;
	}
	
	private void put(Chunk chunk) {
		if(chunks.get(chunk.getX())==null)
			chunks.put(chunk.getX(), new HashMap<Integer,Chunk>());
		
		if(chunks.get(chunk.getX()).put(chunk.getY(), chunk)==null)
			mapSize++;
	}
	
	/**
	 * If file already on RAM, returns a simple get(). Else, loads from disk, saves on RAM and then return.
	 * @param x
	 * @param y
	 * @return
	 */
	public Chunk get(int x, int y) {
		if(chunks.get(x)==null || chunks.get(x).get(y)==null)
			if(chunkExistsOnDisk(x,y))
				loadFromFile(x,y);
			else
				chunksToSave.add(new Chunk(x,y,CHUNK_WIDTH, CHUNK_HEIGHT, MAP_WIDTH, MAP_HEIGHT,em));
		
		return (chunks.get(x)==null)? null : chunks.get(x).get(y);
	}
	
	public void update() {
		for( ReadingChunk r: loadingChunks)
			if(r.isDone()) {
				loadingChunksToRemove.add(r);
				try {
					put(r.getChunk());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		
		for(ReadingChunk r: loadingChunksToRemove)
			loadingChunks.remove(r);
		
		
		if(!chunksToSave.isEmpty() && savingChunks.isEmpty()) {
			
			chunksToSave.get(0).generateNoise();
			chunksToSave.get(0).generateTerrain();
			put(chunksToSave.get(0));
			saveFile(chunksToSave.get(0));
			
			chunksToSave.remove(0);
		}
		
		for( SavingChunk s: savingChunks)
			if(s.isDone()) {
				savingChunksToRemove.add(s);
			}
		
		for(SavingChunk toRemove: savingChunksToRemove)
			savingChunks.remove(toRemove);
	}
	
	public void loadFromFile(int x, int y) {

		long start = System.nanoTime();
		
		ReadingChunk r = new ReadingChunk(mapPath+Chunk.getFileName(x, y), Chunk.getID(x, y));
		loadingChunks.add(r);
		
		System.out.println("\nreadFile: \t"+x+"_"+y+" \t"+(System.nanoTime()-start)/Engine.MILISECOND+"ms");
	}
	
	public void saveFile(Chunk chunk) {
		long start = System.nanoTime();
		
		System.out.println("---------");
		System.out.println("savefile START");
		
		
		//SavingChunk r = new SavingChunk(mapPath+Chunk.getFileName(chunk.getX(), chunk.getY()), chunk);
		//savingChunks.add(r);
		
    	System.out.println("savefile END: \t\t"+chunk.getFileName()+" \t"+(System.nanoTime()-start)/Engine.MILISECOND+"ms");
    	System.out.println("---------");
	}
	
	public boolean chunkExistsOnDisk(int x, int y) {
		//return false;
		return (new File(mapPath+Chunk.getFileName(x, y)).exists());
	}
	
	public boolean chunkExists(int x, int y) {
		//check both on hashMap and directory
		if(chunks.get(x)!=null && chunks.get(x).get(y)!=null)
			return true;
		else
			return false;
	}

}
