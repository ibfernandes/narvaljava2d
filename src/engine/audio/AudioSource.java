package engine.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.AL11.*;

import engine.utilities.BufferUtilities;
import glm.vec._2.Vec2;

public class AudioSource {
	private int sourcePointer;
	private float alMaxDistance;
	private Vec2 pos;
	
	/**
	 * Pass in the buffer pointer which is the audio file already in a buffer.
	 * @param bufferPointer
	 */
	public AudioSource(int bufferPointer, Vec2 pos, float maxDistance) {
		this.pos = pos;
		sourcePointer = alGenSources();

		//Assign the sound we just loaded to the source
		alSourcei(sourcePointer, AL_BUFFER, bufferPointer); //TODO: check if not null
		
		alSourcef(sourcePointer, AL_PITCH, 2f); //sound speed
		alSourcef(sourcePointer,AL_GAIN, 1f);	//volume
		alSource3f(sourcePointer, AL_POSITION, pos.x, pos.y , 0f);
		alSource3f(sourcePointer, AL_VELOCITY, 0, 0 , 0.1f);

		
		alDistanceModel(AL_EXPONENT_DISTANCE);
		alSourcef(sourcePointer, AL_ROLLOFF_FACTOR, 2.5f); 					//Quanto maior o Roll off factor, mais rapido o volume decai
		alSourcef(sourcePointer, AL_REFERENCE_DISTANCE, maxDistance/2); 	//Reference distance is when the gain will be ONE, or Volume = 100%. From this point on it'll decrease below 1
		alSourcef(sourcePointer, AL_MAX_DISTANCE, maxDistance); 			//Quando o volume se torna 0
	}
	
	public void setMaxDistance(float distance) {
		alMaxDistance = distance;
		alSourcef(sourcePointer, AL_MAX_DISTANCE, alMaxDistance);
	}
	

	public void play() {
		alSourcePlay(sourcePointer);
	}
	
	public int getSourcePointer() {
		return sourcePointer;
	}
	
	public void update() {
		
	}
}
