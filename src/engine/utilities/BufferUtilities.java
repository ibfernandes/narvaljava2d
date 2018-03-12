package engine.utilities;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import glm.mat._4.Mat4;

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
	
}
