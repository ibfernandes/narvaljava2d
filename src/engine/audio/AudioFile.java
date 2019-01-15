package engine.audio;

import static org.lwjgl.openal.AL10.*;

import java.nio.ShortBuffer;

public class AudioFile {
	private int bufferPointer;
	
	public AudioFile(ShortBuffer rawAudioBuffer, int sampleRate, int format) {
		bufferPointer = alGenBuffers();
		alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);
	}
	
	public int getBufferPointer() {
		return bufferPointer;
	}
	
	public void destroy() {
		alDeleteBuffers(bufferPointer);
	}
	
}
