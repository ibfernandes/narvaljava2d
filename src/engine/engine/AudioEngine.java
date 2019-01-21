package engine.engine;

import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.alListener3f;

import java.util.HashMap;

import engine.audio.AudioFile;
import engine.audio.AudioSource;
import glm.vec._2.Vec2;

public class AudioEngine {
	private static AudioEngine self;
	private long lastID = 0;
	private HashMap<Long, AudioSource> audio = new HashMap<>();
	
	private AudioEngine() {}
	
	public static AudioEngine getSelf() {
		return (self==null) ? self = new AudioEngine(): self;
	}
	
	public long generateId() {
		return lastID++;
	}
	
	public void setListenerAt(Vec2 pos) {
		alListener3f(AL_POSITION, pos.x, pos.y,0);
	}
	
	public AudioSource getAudioSource(long id) {
		return audio.get(id);
	}
	
	public AudioSource newAudioSource(String audioName, Vec2 position, float range ) {
		AudioSource sc = new AudioSource(generateId(), audioName,position, range);
		audio.put(sc.getId(), sc);
		return sc;
	}
	
	public long addAudioSource(AudioSource audiosc) {
		long id = generateId();
		audio.put(id, audiosc);
		return id;
	}

	public void removeAudioSource(long id) {
		audio.get(id).destroy();
		audio.remove(id);
	}
	
	public void increaseOverallVolumeBy(float percentage) {
		for(long key: audio.keySet()) {
			audio.get(key).setVolume(audio.get(key).getVolume() * (1 + percentage));
		}
	}
	
	public void decreaseOverallVolumeBy(float percentage) {
		for(long key: audio.keySet()) {
			audio.get(key).setVolume(audio.get(key).getVolume() * (1 - percentage));
		}
	}
	
	public void setOverallVolume(float value) {
		for(long key: audio.keySet()) {
			audio.get(key).setVolume(value);
		}
	}
}
