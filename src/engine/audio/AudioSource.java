package engine.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;

import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;

public class AudioSource {
	private long id;
	private int sourcePointer;
	private float alMaxDistance;
	private float pitch;
	private Vec2 pos;
	private float volume;
	private boolean isPlaying = false;

	public AudioSource(long id, String audioFileName, Vec2 pos, float maxDistance) {
		this.id = id;
		this.pos = pos;
		this.volume = 1f;
		sourcePointer = alGenSources();

		alSourcei(sourcePointer, AL_BUFFER, ResourceManager.getSelf().getAudio(audioFileName).getBufferPointer());

		alSourcef(sourcePointer, AL_PITCH, 1f); // sound speed
		alSourcef(sourcePointer, AL_GAIN, volume); // volume
		alSource3f(sourcePointer, AL_POSITION, pos.x, pos.y, 0f);
		alSource3f(sourcePointer, AL_VELOCITY, 0, 0, 0.1f);

		alDistanceModel(AL_EXPONENT_DISTANCE);
		alSourcef(sourcePointer, AL_ROLLOFF_FACTOR, 2.5f); // Quanto maior o Roll off factor, mais rapido o volume decai
		alSourcef(sourcePointer, AL_REFERENCE_DISTANCE, maxDistance / 2); // Reference distance is when the gain will be
																			// ONE, or Volume = 100%. From this point on
																			// it'll decrease below 1
		alSourcef(sourcePointer, AL_MAX_DISTANCE, maxDistance); // Quando o volume se torna 0
	}

	public void setVolume(float volume) {
		this.volume = volume;
		alSourcef(sourcePointer, AL_GAIN, volume);
	}

	public float getVolume() {
		return volume;
	}

	public void setPosition(Vec2 pos) {
		this.pos = pos;
		alSource3f(sourcePointer, AL_POSITION, pos.x, pos.y, 0f);
	}
	
	public Vec2 getPosition() {
		return pos;
	}

	public void setMaxDistance(float distance) {
		alMaxDistance = distance;
		alSourcef(sourcePointer, AL_MAX_DISTANCE, alMaxDistance);
	}

	public void play() {
		isPlaying = true;
		alSourcePlay(sourcePointer);
	}

	public void pause() {
		isPlaying = false;
		alSourcePause(sourcePointer);
	}

	public int getSourcePointer() {
		return sourcePointer;
	}

	public void destroy() {
		alDeleteSources(sourcePointer);
	}

	public long getId() {
		return id;
	}

	public boolean isPlaying() {
		return isPlaying;
	}

	public float getPitch() {
		return pitch;
	}

	public void setPitch(float pitch) {
		alSourcef(sourcePointer, AL_PITCH, pitch);
		this.pitch = pitch;
	}
}
