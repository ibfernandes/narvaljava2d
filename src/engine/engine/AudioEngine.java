package engine.engine;

import java.util.HashMap;

import engine.audio.AudioFile;
import engine.audio.AudioSource;
import glm.vec._2.Vec2;

public class AudioEngine {
	private AudioEngine self;
	private Vec2 listenerPosition;
	private long lastID = 0;
	private static HashMap<Long, AudioSource> audio = new HashMap<>();
	
	private AudioEngine() {}
	
	public AudioEngine getSelf() {
		return (self==null) ? self = new AudioEngine(): self;
	}
	
	public long generateId() {
		return lastID++;
	}
	
	public void setListenerAt(Vec2 pos) {
		listenerPosition = pos;
	}
	
	public AudioSource getAudioSource(long id) {
		return audio.get(id);
	}
	
	public long addAudioSource(AudioSource audiosc) {
		long id = generateId();
		audio.put(id, audiosc);
		return id;
	}

	public void removeAudioSource(long id) {
		audio.remove(id);
	}
}
