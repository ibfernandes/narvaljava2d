package engine.graphic;
import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.opengl.GL12;

import engine.utilities.BufferUtilities;


public class Texture {
	private int id;
	private BufferedImage textureImage;
	public static final int BYTES_PER_PIXEL = 4; //ARGB Model
	private int width, height;
	
	public Texture(int width, int height) {
		this.width = width;
		this.height = height;
		this.id = glGenTextures();
	}
	
	public Texture(int id) {
		this.id = id;
	}
	
	public Texture(String path) {
		id = glGenTextures();
		
		try {
			textureImage = ImageIO.read(getClass().getResourceAsStream(path));
			width  = textureImage.getWidth();
			height = textureImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		init();
	}
	
	public Texture(InputStream is) {
		id = glGenTextures();
		
		try {
			textureImage = ImageIO.read(is);
			width  = textureImage.getWidth();
			height = textureImage.getHeight();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		init();
	}
	
	public Texture(int[][] pixels) {
		id = glGenTextures();
		int[] pixelsAux = new int[pixels.length * pixels[0].length];
		for(int y=0;y<pixels[0].length;y++) {
			for(int x=0;x<pixels.length;x++) {
				pixelsAux[x + y*pixels.length] = pixels[x][y];
			}
		}

        ByteBuffer buffer = BufferUtilities.createByteBuffer(
        							new byte[pixels.length * pixels[0].length * BYTES_PER_PIXEL]
        						); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < pixels[0].length; y++){
            for(int x = 0; x < pixels.length; x++){
                int pixel = pixelsAux[y * pixels.length + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     		// Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      		// Green component
                buffer.put((byte) (pixel & 0xFF));              	// Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    		// Alpha component. Only for RGBA
            }
        }

        buffer.flip();
        
        generateTexture(buffer, pixels.length, pixels[0].length, false);
   }
	
	public void createAndSendBuffer(int[][] pixels) {
		int[] pixelsAux = new int[pixels.length * pixels[0].length];
		for(int y=0;y<pixels[0].length;y++) {
			for(int x=0;x<pixels.length;x++) {
				pixelsAux[x + y*pixels.length] = pixels[x][y];
			}
		}

        ByteBuffer buffer = BufferUtilities.createByteBuffer(
        							new byte[pixels.length * pixels[0].length * BYTES_PER_PIXEL]
        						); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < pixels[0].length; y++){
            for(int x = 0; x < pixels.length; x++){
                int pixel = pixelsAux[y * pixels.length + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     		// Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      		// Green component
                buffer.put((byte) (pixel & 0xFF));              	// Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    		// Alpha component. Only for RGBA
            }
        }

        buffer.flip();
        
        generateTexture(buffer, pixels.length, pixels[0].length, false);
	}
	
	public void createAndSendBuffer(ByteBuffer buffer) { //TODO: NOT WORKING, BUFF ORDER WRONG
		//int[] pixelsAux = new int[pixels.length * pixels[0].length];
		//buffer.clear();
		
		/*for(int y=0;y<pixels[0].length;y++) {
			for(int x=0;x<pixels.length;x++) {
				pixelsAux[x + y*pixels.length] = pixels[x][y];
			}
		}*/

        /*for(int y = 0; y < pixels[0].length; y++){
            for(int x = 0; x < pixels.length; x++){
            	// int pixel = pixelsAux[y * pixels.length + x];
            	int pixel = pixels[x][y];
                buffer.put((byte) ((pixel >> 16) & 0xFF));     		// Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      		// Green component
                buffer.put((byte) (pixel & 0xFF));              	// Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    		// Alpha component. Only for RGBA
            }
        }*/

        //buffer.flip();
        
        generateTexture(buffer, this.width, this.height, false);
	}
	
	public Texture(ByteBuffer buffer, int width, int height, boolean antiAlias) {
		this.width  = width;
		this.height = height;
		generateTexture(buffer, width, height, antiAlias);
	}
	
	public void generateTexture(ByteBuffer buffer, int width, int height, boolean antiAlias) {
		glBindTexture(GL_TEXTURE_2D, id); //Bind texture ID
        
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        
        if(antiAlias) {
        	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); 
        	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        }else {
        	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); 
        	glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        }
        	
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        
        glBindTexture(GL_TEXTURE_2D, 0); //Unbind texture
        textureImage = null; //'free' BufferedImage
	}
	
	private void init() {
		int[] pixels = new int[textureImage.getWidth() * textureImage.getHeight()];
		textureImage.getRGB(0, 0, textureImage.getWidth(), textureImage.getHeight(), pixels, 0, textureImage.getWidth());
        ByteBuffer buffer = BufferUtilities.createByteBuffer(
        							new byte[textureImage.getWidth() * textureImage.getHeight() * BYTES_PER_PIXEL]
        						); //4 for RGBA, 3 for RGB
        
        for(int y = 0; y < textureImage.getHeight(); y++){
            for(int x = 0; x < textureImage.getWidth(); x++){
                int pixel = pixels[y * textureImage.getWidth() + x];
            
                buffer.put((byte) ((pixel >> 16) & 0xFF));     		// Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));      		// Green component
                buffer.put((byte) (pixel & 0xFF));              	// Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));    		// Alpha component. Only for RGBA
            }
        }

        buffer.flip();

        glBindTexture(GL_TEXTURE_2D, id); //Bind texture ID
        
        //Setup wrap mode
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); //GL_LINEAR for smooth
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        
        //Send texel data to OpenGL
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, textureImage.getWidth(), textureImage.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        
        glBindTexture(GL_TEXTURE_2D, 0); //Unbind texture
        textureImage = null; //'free' BufferedImage
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
