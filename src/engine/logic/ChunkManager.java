package engine.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import engine.engine.Engine;
import engine.engine.Settings;
import engine.utilities.Vec2i;
import gameStates.Game;

public class ChunkManager {
	private HashMap<Integer, HashMap<Integer, Chunk>> chunks;
	private int seed;
	private String mapPath;
	private int chunksInMemory = 0;
	private ArrayList<ReadingChunk> loadingChunks = new ArrayList<>();
	private ArrayList<ReadingChunk> loadingChunksToRemove = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunks = new ArrayList<>();
	private ArrayList<SavingChunk> savingChunksToRemove = new ArrayList<>();
	private ArrayList<Chunk> chunksToSave = new ArrayList<>();
	private ArrayList<Vec2i> loadingOrder = new ArrayList<>();

	public static final int MAX_CHUNKS_IN_MEM = 10;
	public static final int CHUNK_WIDTH = EngineSettings.getSelf().getChunkSizeX();
	public static final int CHUNK_HEIGHT = EngineSettings.getSelf().getChunkSizeY();
	public static final int CHUNK_BUFFER_SIZE = ((CHUNK_WIDTH * CHUNK_HEIGHT) / CHUNK_WIDTH) * 10000;
	public static final int MAP_WIDTH = EngineSettings.getSelf().getMapSizeX();
	public static final int MAP_HEIGHT = EngineSettings.getSelf().getMapSizeY();

	public ChunkManager(int seed) {
		chunks = new HashMap<>();
		this.seed = seed;
		mapPath = Settings.mapsFolder + this.seed + File.separator;
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
			if (chunkExistsOnDisk(x, y))
				loadFromFile(x, y);
			else
				chunksToSave
						.add(new Chunk(x, y, CHUNK_WIDTH, CHUNK_HEIGHT, MAP_WIDTH, MAP_HEIGHT, Game.getSelf().getEm()));

		return (chunks.get(x) == null) ? null : chunks.get(x).get(y);
	}

	public void update() {

		if (chunksInMemory > MAX_CHUNKS_IN_MEM) {

			for (Vec2i pos : loadingOrder) {
				Chunk ck = chunks.get(pos.x).get(pos.y);

				if (!Game.getSelf().intersectsScreenView(ck.getBoundingBox())) {
					chunks.get(pos.x).get(pos.y).removeAllEntities();
					chunks.get(pos.x).remove(pos.y);
					loadingOrder.remove(pos);
					chunksInMemory--;
					break;
				}
			}
		}

		loadingChunksToRemove.clear();
		for (ReadingChunk r : loadingChunks)
			if (r.isDone()) {
				loadingChunksToRemove.add(r);
				try {
					put(r.getChunk());
					r.getChunk().addAllEntitiesToEntityManager();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		for (ReadingChunk r : loadingChunksToRemove)
			loadingChunks.remove(r);

		if (!chunksToSave.isEmpty() && savingChunks.isEmpty()) {

			chunksToSave.get(0).generateNoise();
			chunksToSave.get(0).generateTerrain();
			put(chunksToSave.get(0));
			saveFile(chunksToSave.get(0));

			chunksToSave.remove(0);
		}

		for (SavingChunk s : savingChunks)
			if (s.isDone()) {
				savingChunksToRemove.add(s);
			}

		for (SavingChunk toRemove : savingChunksToRemove)
			savingChunks.remove(toRemove);

	}

	public void loadFromFile(int x, int y) {

		long start = System.nanoTime();

		ReadingChunk r = new ReadingChunk(mapPath + Chunk.getFileName(x, y), Chunk.getID(x, y));
		loadingChunks.add(r);

		System.out.println(
				"\nreadFile: \t" + x + "_" + y + " \t" + (System.nanoTime() - start) / Engine.MILISECOND + "ms");
	}

	public void saveFile(Chunk chunk) {
		long start = System.nanoTime();

		System.out.println("---------");
		System.out.println("savefile START");

		SavingChunk r = new SavingChunk(mapPath + Chunk.getFileName(chunk.getX(), chunk.getY()), chunk);
		savingChunks.add(r);

		System.out.println("savefile END: \t\t" + chunk.getFileName() + " \t"
				+ (System.nanoTime() - start) / Engine.MILISECOND + "ms");
		System.out.println("---------");
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
