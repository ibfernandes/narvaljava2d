package engine.graphic;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL12;

import engine.utilities.BufferUtilities;

public class Texture {
	private int id;
	private BufferedImage textureImage;
	/**
	 * 4 bytes per pixel. 1 byte per channel. RGBA format.
	 */
	public static final int BYTES_PER_PIXEL = 4;
	private int width, height;

	/**
	 * Sets @param width and @param height and generates texture ID.
	 * 
	 * @param width
	 * @param height
	 */
	public Texture(int width, int height) {
		this.width = width;
		this.height = height;
		this.id = glGenTextures();
	}
	
	public Texture() {
		this(glGenTextures());
	}

	/**
	 * Instantiates texture with @param id.
	 * 
	 * @param id
	 */
	public Texture(int id) {
		this.id = id;
	}

	/**
	 * Loads texture from /resources + @param path.
	 * 
	 * @param path
	 */
	public Texture(String path) {
		id = glGenTextures();

		try {
			textureImage = ImageIO.read(getClass().getResourceAsStream(path));
			width = textureImage.getWidth();
			height = textureImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();
	}

	/**
	 * Loads texture from @param inputstream.
	 * 
	 * @param inputstream
	 */
	public Texture(InputStream inputstream) {
		id = glGenTextures();

		try {
			textureImage = ImageIO.read(inputstream);
			width = textureImage.getWidth();
			height = textureImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();
	}

	/**
	 * Loads texture from array of pixels with anti-aliasing off.
	 * 
	 * @param pixels
	 */
	public Texture(int[][] pixels) {
		this(pixels, false);
	}
	
	/**
	 * Loads texture from array of pixels.
	 * 
	 * @param pixels
	 */
	public Texture(int[][] pixels, boolean antiAlias) {
		id = glGenTextures();

		createAndSendBuffer(pixels, antiAlias);
	}

	public Texture(ByteBuffer buffer, int width, int height, boolean antiAlias) {
		this.width = width;
		this.height = height;
		generateTextureFromBuffer(buffer, width, height, antiAlias);
	}

	public void createAndSendBuffer(int[][] pixels, boolean antiAlias) {

		ByteBuffer buffer = BufferUtilities
				.createByteBuffer(new byte[pixels.length * pixels[0].length * BYTES_PER_PIXEL]);

		for (int y = 0; y < pixels[0].length; y++) {
			for (int x = 0; x < pixels.length; x++) {
				// RGBA Format
				int pixel = pixels[x][y];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();

		generateTextureFromBuffer(buffer, pixels.length, pixels[0].length, antiAlias);
	}

	public void generateTextureFromBuffer(ByteBuffer buffer, int width, int height, boolean antiAlias) {
		glBindTexture(GL_TEXTURE_2D, id);

		// Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		if (antiAlias) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		// Send texel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);

		glBindTexture(GL_TEXTURE_2D, 0);
		textureImage = null;
	}
	
	/**
	 * Generates a float buffer texture using GL_RED format.
	 * 
	 * @param buffer
	 * @param width
	 * @param height
	 * @param antiAlias
	 */
	public void generateFloatTextureFromBuffer(FloatBuffer buffer, int width, int height, boolean antiAlias) {
		glBindTexture(GL_TEXTURE_2D, id);

		// Setup wrap mode
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		if (antiAlias) {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else {
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		}

		// Send texel data to OpenGL
		glTexImage2D(GL_TEXTURE_2D, 0, GL_R32F, width, height, 0, GL_RED, GL_FLOAT, buffer);

		glBindTexture(GL_TEXTURE_2D, 0);
		textureImage = null;
	}

	private void init() {
		int[] pixels = new int[textureImage.getWidth() * textureImage.getHeight()];
		textureImage.getRGB(0, 0, textureImage.getWidth(), textureImage.getHeight(), pixels, 0,
				textureImage.getWidth());

		ByteBuffer buffer = BufferUtilities
				.createByteBuffer(new byte[textureImage.getWidth() * textureImage.getHeight() * BYTES_PER_PIXEL]);

		for (int y = 0; y < textureImage.getHeight(); y++) {
			for (int x = 0; x < textureImage.getWidth(); x++) {
				int pixel = pixels[y * textureImage.getWidth() + x];
				// RGBA Format
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();

		generateTextureFromBuffer(buffer, textureImage.getWidth(), textureImage.getHeight(), false);
	}

	public void bind() {
		glBindTexture(GL_TEXTURE_2D, id);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getId() {
		return id;
	}
}
