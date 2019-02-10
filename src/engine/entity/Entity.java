package engine.entity;

import java.io.Serializable;

public class Entity implements Serializable {
	private long id;
	private String name;
	private int chunkX, chunkY;
	private boolean shouldSave = true; 

	public Entity(long id) {
		this.id = id;
	}

	public long getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getChunkX() {
		return chunkX;
	}

	public void setChunkX(int chunkX) {
		this.chunkX = chunkX;
	}

	public int getChunkY() {
		return chunkY;
	}

	public void setChunkY(int chunkY) {
		this.chunkY = chunkY;
	}

	public boolean shouldSave() {
		return shouldSave;
	}

	public void setShouldSave(boolean shouldSave) {
		this.shouldSave = shouldSave;
	}
}
