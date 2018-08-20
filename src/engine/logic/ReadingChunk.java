package engine.logic;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

import engine.engine.Engine;

public class ReadingChunk {
	private Future<Integer> promise;
	private ByteBuffer buffer = ByteBuffer.allocate(1024*1000);
	private String id;
	
	public ReadingChunk(String path, String id) {
		Path filePath = Paths.get(path);
		this.id = id;
		
		try {
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
					filePath, StandardOpenOption.READ, StandardOpenOption.READ);
			 
		    promise = fileChannel.read(buffer, 0);
		    //promise.get();
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean isDone() {
		return promise.isDone();
	}
	
	/**
	 * If the object is already read then returns it if not returns null
	 * @return
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public Chunk getChunk() throws ClassNotFoundException, IOException {
		if(isDone()) {
			buffer.flip();
		    byte[] data = new byte[buffer.limit()];
	        buffer.get(data);
	        
		    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(data));
		    return (Chunk) in.readObject();
		}
		return null;
	}
}
