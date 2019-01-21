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
	private String id;

	public SavingChunk(String path, Chunk chunk) {
		Path filePath = Paths.get(path);
		this.id = Chunk.getID(chunk.getX(), chunk.getY());

		try {
			AsynchronousFileChannel fileChannel = AsynchronousFileChannel.open(filePath, StandardOpenOption.WRITE,
					StandardOpenOption.CREATE);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeObject(chunk);
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
		return promise.isDone();
	}
}
