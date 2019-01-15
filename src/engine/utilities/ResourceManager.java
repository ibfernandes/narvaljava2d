package engine.utilities;

import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_memory;
import static org.lwjgl.system.MemoryStack.stackMallocInt;
import static org.lwjgl.system.MemoryStack.stackPop;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.libc.LibCStdlib.free;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

import engine.audio.AudioFile;
import engine.audio.AudioSource;
import engine.graphic.Shader;
import engine.graphic.Texture;
import engine.renderer.CubeRenderer;
import engine.renderer.GrassRenderer;
import engine.renderer.Renderer;
import engine.renderer.ShadowRenderer;
import engine.renderer.TextureRenderer;
import engine.ui.Font;
import glm.vec._2.Vec2;

public final class ResourceManager {
	private static ResourceManager self;
	private static HashMap<String, Shader> shaders = new HashMap<String, Shader>();
	private static HashMap<String, Texture> textures = new HashMap<String, Texture>();
	private static HashMap<String, AudioFile> audio = new HashMap<String, AudioFile>();
	private static HashMap<String, Font> fonts = new HashMap<String, Font>();
	private static HashMap<String, Renderer> renderers = new HashMap<String, Renderer>();
	private static CubeRenderer cubeRenderer;
	private static TextureRenderer textureRenderer;
	private static ShadowRenderer shadowRenderer;
	private static GrassRenderer grassRender;

	private ResourceManager() {

	}

	public static ResourceManager getSelf() {
		if (self == null)
			self = new ResourceManager();
		return self;
	}

	/**
	 * File format must be in .OGG
	 * @param audioPath
	 */
	public AudioFile loadAudio(String name, String audioPath) {
		if (audio.containsKey(name))
			return audio.get(name);

		byte[] memory = null;
		try {
			int numberOfBytes = this.getClass().getResourceAsStream("/" + audioPath).available();
			memory = new byte[numberOfBytes];
			this.getClass().getResourceAsStream("/" + audioPath).read(memory, 0, numberOfBytes);
		} catch (IOException e) {
			e.printStackTrace();
		}

		stackPush();
		IntBuffer channelsBuffer = stackMallocInt(1);
		stackPush();
		IntBuffer sampleRateBuffer = stackMallocInt(1);
		
		ShortBuffer rawAudioBuffer = stb_vorbis_decode_memory(BufferUtilities.createByteBuffer(memory), channelsBuffer,
				sampleRateBuffer);

		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		stackPop();
		stackPop();

		int format = -1;
		if (channels == 1) 
			format = AL_FORMAT_MONO16;
		else if (channels == 2) 
			format = AL_FORMAT_STEREO16;
		
		AudioFile audioIns = new AudioFile(rawAudioBuffer,sampleRate, format);
		free(rawAudioBuffer);
		
		return audio.put(name, audioIns);
	}

	public AudioFile getAudio(String name) {
		return audio.get(name);
	}

	public Font loadFont(String name, String fontPath) {
		if (fonts.containsKey(name))
			return fonts.get(name);
		return fonts.put(name, new Font());
	}

	public Font getFont(String name) {
		return fonts.get(name);
	}
	/**
	 * Loads the file @param name from the resources folder
	 * 
	 * @param name
	 * @param vertexShaderPath
	 * @param fragmentShaderFilPath
	 * @param geometryShaderPath
	 * @return
	 */
	public Shader loadShader(String name, String vertexShaderPath, String fragmentShaderFilPath,
			String geometryShaderPath) {
		if (shaders.containsKey(name))
			return shaders.get(name);
		return shaders.put(name, loadShaderFromFile(vertexShaderPath, fragmentShaderFilPath, geometryShaderPath));
	}

	/**
	 * The file must be inside resource folder. (i.e
	 * resources/shaders/pongshader.vs)
	 * 
	 * @param imgPath
	 * @param alpha
	 * @param name
	 * @return
	 */
	public Texture loadTexture(String name, String imgPath) {
		if (textures.containsKey(name))
			return textures.get(name);
		return textures.put(name, loadTextureFromFile(imgPath));
	}

	public Shader getShader(String name) {
		return shaders.get(name);
	}

	public Texture getTexture(String name) {
		return textures.get(name);
	}

	public void clear() {
	}

	/**
	 * The file must be inside resource folder. (i.e shaders/pongshader.vs)
	 * 
	 * @param vertexShaderPath
	 * @param fragmentShaderPath
	 * @param geometryShaderPath
	 * @return
	 */
	private Shader loadShaderFromFile(String vertexShaderPath, String fragmentShaderPath, String geometryShaderPath) {
		String vertexCode = null, fragmentCode = null, geometryCode = null;

		try {
			// Vertex Code
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader bufferedReader;

			bufferedReader = new BufferedReader(
					new InputStreamReader(this.getClass().getResourceAsStream("/" + vertexShaderPath)));
			String line;

			while ((line = bufferedReader.readLine()) != null)
				stringBuilder.append(line).append("\n");

			bufferedReader.close();
			vertexCode = stringBuilder.toString();

			// Fragment Code
			stringBuilder = new StringBuilder();

			bufferedReader = new BufferedReader(
					new InputStreamReader(this.getClass().getResourceAsStream("/" + fragmentShaderPath)));

			while ((line = bufferedReader.readLine()) != null)
				stringBuilder.append(line).append("\n");

			bufferedReader.close();
			fragmentCode = stringBuilder.toString();

			// Geometry Code
			if (geometryShaderPath != null) {
				stringBuilder = new StringBuilder();
				bufferedReader = new BufferedReader(
						new InputStreamReader(this.getClass().getResourceAsStream("/" + geometryShaderPath)));

				while ((line = bufferedReader.readLine()) != null)
					stringBuilder.append(line).append("\n");

				bufferedReader.close();
				geometryCode = stringBuilder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		Shader shader = new Shader();
		shader.compile(vertexCode, fragmentCode, (geometryCode == null) ? null : geometryCode);
		return shader;
	}

	private Texture loadTextureFromFile(String imgPath) {
		Texture t = new Texture("/" + imgPath);
		return t;
	}

	public void setRenderer(String name, Renderer r) {
		renderers.put(name, r);
	}

	public <T extends Renderer> T getRenderer(String name) {
		return (T) renderers.get(name);
	}

	public CubeRenderer getCubeRenderer() {
		return cubeRenderer;
	}

	public void setCubeRenderer(CubeRenderer cubeRenderer) {
		ResourceManager.cubeRenderer = cubeRenderer;
	}

	public TextureRenderer getTextureRenderer() {
		return textureRenderer;
	}

	public void setTextureRenderer(TextureRenderer textureRenderer) {
		ResourceManager.textureRenderer = textureRenderer;
	}

	public ShadowRenderer getShadowRenderer() {
		return shadowRenderer;
	}

	public void setShadowRenderer(ShadowRenderer shadowRenderer) {
		ResourceManager.shadowRenderer = shadowRenderer;
	}

	public void setGrassRenderer(GrassRenderer grassRender) {
		ResourceManager.grassRender = grassRender;
	}

	public GrassRenderer getGrassRenderer() {
		return grassRender;
	}
}
