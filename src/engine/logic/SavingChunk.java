package engine.logic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

import engine.engine.Engine;

public class SavingChunk {
	private Future<Integer> promise;
	private ByteBuffer buffer = ByteBuffer.allocate(ChunkMap.CHUNK_BUFFER_SIZE);
	private String id;
	
	public SavingChunk(String path, Chunk chunk) {
		Path filePath = Paths.get(path);
		this.id = chunk.getID(chunk.getX(), chunk.getY());


		//Thread t = new Thread() {
		//	public void run() {
				try {
				AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(
						filePath, StandardOpenOption.WRITE, StandardOpenOption.CREATE);
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ObjectOutputStream out = new ObjectOutputStream(bos);
				
				long start = System.nanoTime();
				out.writeObject(chunk);
				out.flush();
				//System.out.println("writeObject: \t"+chunk.getFileName()+" \t"+(System.nanoTime()-start)/Engine.MILISECOND+"ms");
				
				start = System.nanoTime();
				buffer.put(bos.toByteArray());
				buffer.flip();
				//System.out.println("bufferPut: \t"+chunk.getFileName()+" \t"+(System.nanoTime()-start)/Engine.MILISECOND+"ms");
				
			    promise = fileChannel.write(buffer, 0);
			    
			    
			    //promise.get();
			    buffer.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
		//	}
		//};
		//t.start();
	}
	
	public boolean isDone() {
		return promise.isDone();
	}
}
