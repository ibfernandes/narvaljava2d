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
import java.util.ArrayList;

import org.lwjgl.BufferUtils;

import demo.Main;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

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
	
	/**
	 * Creates a FloaBuffer from @param values.
	 * 
	 * @param values
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(float[] values){
		FloatBuffer buffer = createFloatBuffer(values.length);
		
		for(int i = 0; i < values.length; i++)
			buffer.put(values[i]);

		buffer.flip();
		return buffer;
	}
	
	/**
	 * Creates a FloaBuffer from @param values.
	 * 
	 * @param values
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(float[][] values){
		FloatBuffer buffer = createFloatBuffer(values.length*values[0].length);
		
		for(int c = 0; c < values[0].length; c++)
			for(int l = 0; l < values.length; l++)
				buffer.put(values[l][c]);

		buffer.flip();
		return buffer;
	}
	
	/**
	 * Creates a ByteBuffer from @param values.
	 * 
	 * @param values
	 * @return
	 */
	public static ByteBuffer create32bitsByteBuffer(float[][] values){
		ByteBuffer buffer = ByteBuffer.allocateDirect(values.length*values[0].length*Float.BYTES).order(ByteOrder.nativeOrder());
		
		for(int l = 0; l < values.length; l++)
			for(int c = 0; c < values[0].length; c++) {
				buffer.putFloat(values[l][c]);
			}
		
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Creates a IntBuffer from @param values.
	 * 
	 * @param values
	 * @return
	 */
	public static IntBuffer createIntBuffer(int values[]){
		IntBuffer buffer = createIntBuffer(values.length);
		 buffer.put(values);
		 buffer.flip();
		 return buffer;
	}
	
	public static FloatBuffer createFloatBuffer(Vec2 vec) {
		 return createFloatBuffer(vec.toFA_());
	}
	
	public static FloatBuffer createFloatBuffer(Vec4 vec) {
		 return createFloatBuffer(vec.toFA_());
	}
	
	
	/**
	 * Creates a FloatBuffer from @param matrices from top row to bottom row. 
	 * 
	 * @param matrices
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(ArrayList<Mat4> matrices){
		FloatBuffer buffer = createFloatBuffer(4*4*matrices.size());
		
		for(Mat4 mat: matrices) {
			buffer.put(mat.m00);	buffer.put(mat.m01);	buffer.put(mat.m02);	buffer.put(mat.m03);
			buffer.put(mat.m10);	buffer.put(mat.m11);	buffer.put(mat.m12);	buffer.put(mat.m13);
			buffer.put(mat.m20);	buffer.put(mat.m21);	buffer.put(mat.m22);	buffer.put(mat.m23);
			buffer.put(mat.m30);	buffer.put(mat.m31);	buffer.put(mat.m32);	buffer.put(mat.m33);
		}
		
		buffer.flip();
		return buffer;
	}
	
	/**
	 * Creates a FloatBuffer from @param mat from top row to bottom row. 
	 * 
	 * @param mat
	 * @return
	 */
	public static FloatBuffer createFloatBuffer(Mat4 mat){
		FloatBuffer buffer = createFloatBuffer(4*4);
		
		buffer.put(mat.m00);	buffer.put(mat.m01);	buffer.put(mat.m02);	buffer.put(mat.m03);
		buffer.put(mat.m10);	buffer.put(mat.m11);	buffer.put(mat.m12);	buffer.put(mat.m13);
		buffer.put(mat.m20);	buffer.put(mat.m21);	buffer.put(mat.m22);	buffer.put(mat.m23);
		buffer.put(mat.m30);	buffer.put(mat.m31);	buffer.put(mat.m32);	buffer.put(mat.m33);

		buffer.flip();
		return buffer;
	}
	
	public static FloatBuffer fillFloatBuffer(FloatBuffer buffer, Mat4 mat){
		buffer.clear();
		
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
     * Loads and read the specified file content in @param resource and returns the raw data as a ByteBuffer.
     *
     * @param resource  
     * @param bufferSize 
     * @return the resource data
     * @throws IOException
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer = null;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while (fc.read(buffer) != -1) {
                }
            }
        } else {
            try {
                InputStream source = (new Main()).getClass().getResourceAsStream(resource);
            		
                ReadableByteChannel rbc = Channels.newChannel(source);
            
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2);
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
	

