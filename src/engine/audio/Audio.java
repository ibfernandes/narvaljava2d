package engine.audio;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import static org.lwjgl.stb.STBVorbis.*;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.Stdlib.free;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class Audio {
	private int sourcePointer;
	
	public Audio(String audioPath) {
		String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
		long device = alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		long context = alcCreateContext(device, attributes);
		alcMakeContextCurrent(context);

		ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);


		String fileName = "resources/"+audioPath;

		//Allocate space to store return information from the function
		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);

		ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(fileName, channelsBuffer, sampleRateBuffer);

		//Retreive the extra information that was stored in the buffers by the function
		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		//Free the space we allocated earlier
		stackPop();
		stackPop();

		//Find the correct OpenAL format
		int format = -1;
		if(channels == 1) {
		    format = AL_FORMAT_MONO16;
		} else if(channels == 2) {
		    format = AL_FORMAT_STEREO16;
		}

		//Request space for the buffer
		int bufferPointer = alGenBuffers();

		//Send the data to OpenAL
		alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate);

		//Free the memory allocated by STB
		free(rawAudioBuffer);


		//Request a source
		sourcePointer = alGenSources();

		//Assign the sound we just loaded to the source
		alSourcei(sourcePointer, AL_BUFFER, bufferPointer);

		//Play the sound
		alSourcePlay(sourcePointer);

		try {
		    //Wait for a second
		    Thread.sleep(4000);
		} catch(InterruptedException ex) {}
		alSourcePlay(sourcePointer);
		
		/*int sourcePointer2 = alGenSources();
		alSourcei(sourcePointer2, AL_BUFFER, bufferPointer);
		alSourcePlay(sourcePointer2);*/
/*
		//Terminate OpenAL
		alDeleteSources(sourcePointer);
		alDeleteBuffers(bufferPointer);
		alcDestroyContext(context);
		alcCloseDevice(device);*/
	}
	
	public void play() {
		
	}
	
	public void stop() {
		alSourceStop(sourcePointer);
	}
	
}
