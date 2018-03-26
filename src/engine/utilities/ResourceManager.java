package engine.utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import engine.audio.Audio;
import engine.graphic.Shader;
import engine.graphic.Texture;
import graphic.CubeRenderer;
import graphic.GrassRenderer;
import graphic.ShadowRenderer;
import graphic.TextureRenderer;

public final class ResourceManager {
	private static ResourceManager self;
	private static HashMap<String,  Shader> shaders = new HashMap<String, Shader>();
	private static HashMap<String,  Texture> textures = new HashMap<String, Texture>();
	private static HashMap<String,  Audio> audio = new HashMap<String, Audio>();
	private static CubeRenderer cubeRenderer;
	private static TextureRenderer textureRenderer;
	private static ShadowRenderer shadowRenderer;
	private static GrassRenderer grassRender;
	
	private ResourceManager() {
		
	}
	
	public static ResourceManager getSelf() {
		if(self == null) 
			self = new ResourceManager();
		return self;
	}
	
	public Audio loadAudio(String name, String audioPath) {
		if(audio.containsKey(name))
			return audio.get(name);
		return audio.put(name, new Audio(audioPath));
	}
	
	public Audio getAudio(String name) {
		return audio.get(name);
	}
	
	public Shader loadShader(String name, String vertexShaderPath, String fragmentShaderFilPath, String geometryShaderPath) {
		if(shaders.containsKey(name))
			return shaders.get(name);
		return shaders.put(name, loadShaderFromFile(vertexShaderPath, fragmentShaderFilPath, geometryShaderPath));
	}
	
	/**
	 * The file must be inside resource folder. (i.e resources/shaders/pongshader.vs)
	 * @param imgPath
	 * @param alpha
	 * @param name
	 * @return
	 */
	public Texture loadTexture(String name, String imgPath) {
		if(textures.containsKey(name))
			return textures.get(name);
		return textures.put(name, loadTextureFromFile(imgPath));
	}
	
	public Shader getShader(String name) {
		return shaders.get(name);
	}
	
	public Texture getTexture(String name) {
		return textures.get(name);
	}
	
	public void clear() {}
	
	/**
	 * The file must be inside resource folder. (i.e shaders/pongshader.vs)
	 * @param vertexShaderPath
	 * @param fragmentShaderPath
	 * @param geometryShaderPath
	 * @return
	 */
	private Shader loadShaderFromFile(String vertexShaderPath, String fragmentShaderPath, String geometryShaderPath) {
		String vertexCode = null, fragmentCode = null, geometryCode = null;
		
		try {
			//Vertex Code
			StringBuilder stringBuilder = new StringBuilder();
			BufferedReader bufferedReader;
			
			bufferedReader = new BufferedReader(new FileReader("./resources/"+vertexShaderPath)); //
			String line;
			
			while((line = bufferedReader.readLine())!=null)
				stringBuilder.append(line).append("\n");
			
			bufferedReader.close();
			vertexCode = stringBuilder.toString();
			
			//Fragment Code
			stringBuilder = new StringBuilder();
			bufferedReader = new BufferedReader(new FileReader("./resources/"+fragmentShaderPath));
			
			while((line = bufferedReader.readLine())!=null)
				stringBuilder.append(line).append("\n");
			
			bufferedReader.close();
			fragmentCode = stringBuilder.toString();
			
			//Geometry Code
			if(geometryShaderPath!=null){
				stringBuilder = new StringBuilder();
				bufferedReader = new BufferedReader(new FileReader("./resources/"+geometryShaderPath));
				
				while((line = bufferedReader.readLine())!=null)
					stringBuilder.append(line).append("\n");
				
				bufferedReader.close();
				geometryCode = stringBuilder.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Shader shader = new Shader();
		shader.compile(vertexCode, fragmentCode, (geometryCode==null)? null : geometryCode);
		return shader;
	}
	
	private Texture loadTextureFromFile(String imgPath) {
		Texture t = new Texture("/"+imgPath); 
		return t;
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
