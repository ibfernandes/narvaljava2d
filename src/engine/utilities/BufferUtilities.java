package engine.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.lwjgl.BufferUtils;

import game.Main;
import glm.mat._4.Mat4;
import sun.nio.ch.IOUtil;

public class BufferUtilities {
	public static ByteBuffer createByteBuffer(byte[] array) {
		ByteBuffer result = ByteBuffer.allocateDirect(array.length).order(ByteOrder.nativeOrder());
		result.put(array).flip();
		return result;
	} 
	
	public static FloatBuffer createFloatBuffer(int size){
		return BufferUtils.createFloatBuffer(size);
	}
	
	public static IntBuffer createIntBuffer(int size){
		return BufferUtils.createIntBuffer(size);
	}
	
	public static FloatBuffer createFloatBuffer(float[] vertices){
		FloatBuffer buffer = createFloatBuffer(vertices.length);
		
		for(int i = 0; i < vertices.length; i++){
			buffer.put(vertices[i]);
		}

		buffer.flip();
		return buffer;
	}
	
	public static IntBuffer createIntBuffer(int values[]){
		IntBuffer buffer = createIntBuffer(values.length);
		 buffer.put(values);
		 buffer.flip();
		 return buffer;
	}
	
	public static FloatBuffer createFloatBuffer(Mat4 mat){
		FloatBuffer buffer = createFloatBuffer(4*4);
		
		buffer.put(mat.m00);	buffer.put(mat.m01);	buffer.put(mat.m02);	buffer.put(mat.m03);
		buffer.put(mat.m10);	buffer.put(mat.m11);	buffer.put(mat.m12);	buffer.put(mat.m13);
		buffer.put(mat.m20);	buffer.put(mat.m21);	buffer.put(mat.m22);	buffer.put(mat.m23);
		buffer.put(mat.m30);	buffer.put(mat.m31);	buffer.put(mat.m32);	buffer.put(mat.m33);
		
		

		buffer.flip();
		return buffer;
	}
	
	private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
	}
	
	  /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer = null;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                    ;
                }
            }
        } else {
            try {
                InputStream source = (new Main()).getClass().getResourceAsStream(resource);
                
            		if(source==null)
            			System.out.println("worng");
            		
                ReadableByteChannel rbc = Channels.newChannel(source);
            
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }catch(Exception e) {
            	e.printStackTrace();
            }
        }

        buffer.flip();
        return buffer.slice();
    }

}
	

