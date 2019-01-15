package engine.utilities;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

public class ByteBufferExt implements Serializable{
	private ByteBuffer bytebuffer;
	
	public ByteBufferExt(ByteBuffer bytebuffer) {
		this.bytebuffer = bytebuffer;
	}

	public ByteBuffer getBytebuffer() {
		return bytebuffer;
	}

	public void setBytebuffer(ByteBuffer bytebuffer) {
		this.bytebuffer = bytebuffer;
	}
	
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException{      
		int bufferSize = aInputStream.readInt();
        byte[] buffer = new byte[bufferSize];
        aInputStream.read(buffer, 0, bufferSize);
        this.bytebuffer = ByteBuffer.wrap(buffer, 0, bufferSize);
    }
 
    private void writeObject(ObjectOutputStream aOutputStream) throws IOException{
    	aOutputStream.writeInt(bytebuffer.capacity());
    	byte[] arr = new byte[bytebuffer.remaining()];
    	bytebuffer.get(arr);
    	aOutputStream.write(arr);
    }
}
