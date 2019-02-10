package engine.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.lwjgl.glfw.GLFW;

import demo.Game;
import engine.engine.EngineSettings;
import engine.states.GSM;
import engine.utilities.Vec2i;

public class ChunkManager {
	private HashMap<Integer, HashMap<Integer, Chunk>> chunks;
	private int seed;
	private String mapPath;
	private ArrayList<ReadingChunk> loadingChunks = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunks = new ArrayList<>();
	private ArrayList<Chunk> chunksToGenerateAndSave = new ArrayList<>();
	private ArrayList<ReadingChunk> loadingChunksToRemove = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunksToRemove = new ArrayList<>();
	private ArrayList<Vec2i> loadingOrder = new ArrayList<>();
	private int chunksInMemory = 0;
	private boolean shouldBreak = false;

	public static final int MAX_CHUNKS_IN_MEM = 10;
	public static final int CHUNK_WIDTH = EngineSettings.getSelf().getChunkSizeX();
	public static final int CHUNK_HEIGHT = EngineSettings.getSelf().getChunkSizeY();
	public static final int CHUNK_BUFFER_SIZE = ((CHUNK_WIDTH * CHUNK_HEIGHT) / CHUNK_WIDTH) * 10000;
	public static final int MAP_WIDTH = EngineSettings.getSelf().getMapSizeX();
	public static final int MAP_HEIGHT = EngineSettings.getSelf().getMapSizeY();

	public ChunkManager(int seed) {
		chunks = new HashMap<>();
		this.seed = seed;
		mapPath = EngineSettings.MAPS_FOLDER+ this.seed + File.separator;
	}

	private void put(Chunk chunk) {

		if (chunks.get(chunk.getX()) == null)
			chunks.put(chunk.getX(), new HashMap<Integer, Chunk>());
		
		if (chunks.get(chunk.getX()).put(chunk.getY(), chunk) == null)
			chunksInMemory++;

		loadingOrder.add(new Vec2i(chunk.getX(), chunk.getY()));
	}

	/**
	 * If chunk is already on the RAM, returns it. If it's not then loads from disk,
	 * stores on RAM and then return.
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Chunk get(int x, int y) {
		if (chunks.get(x) == null || chunks.get(x).get(y) == null)
			if (chunkExistsOnDisk(x, y)) {
				ReadingChunk r = loadFromFile(x, y);
				r.read();
				loadingChunks.add(r);
			}else {
				chunksToGenerateAndSave
						.add(new Chunk(x, y, CHUNK_WIDTH, CHUNK_HEIGHT, MAP_WIDTH, MAP_HEIGHT, Game.getSelf().getEm()));
			}

		return (chunks.get(x) == null) ? null : chunks.get(x).get(y);
	}

	public void update() {
		//----------------------------------
		// Manages Excess chunks
		//----------------------------------
		shouldBreak = false;
		
		if(chunksInMemory>MAX_CHUNKS_IN_MEM) {
			for(Integer x: chunks.keySet()) {
				for(Integer y: chunks.get(x).keySet()) {
					if(!Game.getSelf().intersectsScreenView(chunks.get(x).get(y).getBoundingBox())) {
						savingChunks.add(saveFile((chunks.get(x).get(y))));
						chunksInMemory--;
						shouldBreak = true;
					}
					if(shouldBreak)
						break;
				}
				if(shouldBreak)
					break;
			}
		}
		
		//----------------------------------
		// Generate chunks contents
		//----------------------------------

		for(Chunk c: chunksToGenerateAndSave) {
			c.generateNoiseAndTerrain();
			put(c);
		}
		
		chunksToGenerateAndSave.clear();
		
		//----------------------------------
		// Reading block
		//----------------------------------
		loadingChunksToRemove.clear();
		
		for (ReadingChunk r : loadingChunks) {
			if (r.isDone()) {
				loadingChunksToRemove.add(r);
				try {
					put(r.getChunk());
					r.getChunk().transferAllEntitiesToEntityManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		for (ReadingChunk r : loadingChunksToRemove)
			loadingChunks.remove(r);
		
		//----------------------------------
		// Saving block
		//----------------------------------
		savingChunksToRemove.clear();
		
		for (SavingChunk s : savingChunks) {
			if(s.isDone()) {
				savingChunksToRemove.add(s);
			}
			chunks.get(s.getChunk().getX()).remove(s.getChunk().getY());
		}
		
		for (SavingChunk s : savingChunksToRemove) 
			savingChunks.remove(s);
		
		for (SavingChunk s : savingChunks) {
			if(!s.hasStarted()) {
				s.getChunk().getEntitiesFromEntityManager();
				s.getChunk().removeAllEntitiesFromEntityManager();
				s.save();
			}
		}
		
		if (GSM.getSelf().getKeyboard().isKeyPressed(GLFW.GLFW_KEY_Y)) {
			for(Integer x: chunks.keySet()) {
				for(Integer y: chunks.get(x).keySet()) {
					if(!Game.getSelf().intersectsScreenView(chunks.get(x).get(y).getBoundingBox())) {
						savingChunks.add(saveFile((chunks.get(x).get(y))));
					}
				}
			}
		}
		
	}
	
	/**
	 * Returns a container to load this chunk.
	 * 
	 * @param x
	 * @param y
	 */
	public ReadingChunk loadFromFile(int x, int y) {
		ReadingChunk r = new ReadingChunk(mapPath + Chunk.getFileName(x, y), Chunk.getID(x, y));
		return r;
	}
	
	/**
	 * Returns a container to save this chunk.
	 * 
	 * @param chunk
	 */
	public SavingChunk saveFile(Chunk chunk) {
		SavingChunk r = new SavingChunk(mapPath + Chunk.getFileName(chunk.getX(), chunk.getY()), chunk);
		return r;
	}

	public boolean chunkExistsOnDisk(int x, int y) {
		return (new File(mapPath + Chunk.getFileName(x, y)).exists());
	}

	public boolean chunkExists(int x, int y) {
		if (chunks.get(x) != null && chunks.get(x).get(y) != null)
			return true;
		else
			return false;
	}
}
