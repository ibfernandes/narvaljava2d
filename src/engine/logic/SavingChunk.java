package engine.logic;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.Future;

public class SavingChunk {
	private Future<Integer> promise;
	private ByteBuffer buffer = ByteBuffer.allocate(ChunkManager.CHUNK_BUFFER_SIZE);
	private Path filePath;
	private Chunk c;
	private boolean hasStarted = false;

	public SavingChunk(String path, Chunk chunk) {
		c = chunk;
		filePath = Paths.get(path);
	}
	
	public void save() {
		hasStarted = true;
		try {
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeObject(c);
			out.flush();

			buffer.put(bos.toByteArray());
			buffer.flip();

			promise = fileChannel.write(buffer, 0);

			buffer.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean isDone() {
		return (promise==null)? false : promise.isDone();
	}

	public Chunk getChunk() {
		return c;
	}

	public void setChunk(Chunk c) {
		this.c = c;
	}

	public boolean hasStarted() {
		return hasStarted;
	}
}
