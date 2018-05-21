package gameStates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

import engine.engine.Engine;
import engine.engine.Settings;
import engine.logic.Chunk;

public class ChunkMap {
	private HashMap<Integer, HashMap<Integer, Chunk>> chunks;
	private int seed;
	private String mapPath;
	private int mapSize = 0; //How many chunks are stored now.
	private Thread thread;
	
	public ChunkMap(int seed) {
		chunks = new HashMap<>();
		this.seed = seed;
		mapPath = Settings.mapsFolder+seed+File.separator;
	}
	
	public void put(Chunk chunk) {
		if(chunks.get(chunk.getX())==null)
			chunks.put(chunk.getX(), new HashMap<Integer,Chunk>());
		
		if(chunks.get(chunk.getX()).put(chunk.getY(), chunk)==null)
			mapSize++;
	}
	
	public void putIfAbsent(Chunk chunk) {
		if(chunks.get(chunk.getX())==null)
			chunks.put(chunk.getX(), new HashMap<Integer,Chunk>());
		
		if(chunkExistsOnDisk(chunk.getX(), chunk.getY()))
			if(chunks.get(chunk.getX()).get(chunk.getY())==null) {
				chunks.get(chunk.getX()).put(chunk.getY(), loadFromFile(chunk.getX(), chunk.getY()));
				mapSize++;
				return;
			}
		
		if(chunks.get(chunk.getX()).get(chunk.getY())==null) {
			mapSize++;
			chunks.get(chunk.getX()).put(chunk.getY(), chunk);
		}
	}
	
	public void update(Chunk chunk) {
		//updates then save chunk, if not exists, create one
	}
	
	/**
	 * If file already on RAM, returns a simple get(). Else, loads from disk, saves on RAM and then return.
	 * @param x
	 * @param y
	 * @return
	 */
	public Chunk get(int x, int y) {
		if(chunks.get(x)==null || chunks.get(x).get(y)==null)
			if(chunkExistsOnDisk(x,y))
				put(loadFromFile(x,y));
		
		return (chunks.get(x)==null)? null : chunks.get(x).get(y);
	}
	
	public Chunk loadFromFile(int x, int y) {
		Chunk c;
		
		/*try {
			 File file = new File(mapPath+Chunk.getFileName(x, y));
	         
	        //Get file channel in readonly mode
	        FileChannel fileChannel = new RandomAccessFile(file, "r").getChannel();
	
	         
	        //Get direct byte buffer access using channel.map() operation
	        MappedByteBuffer map = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size());
	        byte[] buffer = new byte[(int) fileChannel.size()];
	        map.get(buffer);
	        
	        
	        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
	        c = (Chunk) in.readObject();
	        in.close();
	        fileChannel.close();
	        
	        return c;
	        
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}*/
		
		
		long start = System.nanoTime();
		try {
	         FileInputStream fileIn = new FileInputStream(mapPath+Chunk.getFileName(x, y));
	         ObjectInputStream in = new ObjectInputStream(fileIn);
	         c = (Chunk) in.readObject();
	         in.close();
	         fileIn.close();
	         
	         System.out.println("\nreadFile: "+(System.nanoTime()-start)/Engine.MILISECOND);
	         return c;
	      } catch (Exception i) {
	         i.printStackTrace();
	         return null;
	      }
		
	}
	
	public void saveFile(Chunk chunk) {
		//TODO: if file exists, check hash to see if it needs udpate
		/*try {
		   File file = new File(mapPath+chunk.getFileName());
	         
	        file.delete();
	 
	        FileChannel fileChannel = new RandomAccessFile(file, "rw").getChannel();
	 
	        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, 4096 * 8 * 8);
	        
	        ByteArrayOutputStream bos = new ByteArrayOutputStream();
	        ObjectOutputStream out = new ObjectOutputStream(bos);
	        out.writeObject(chunk);
	        //out.flush();
	 
	        buffer.put(bos.toByteArray());
		} catch (Exception e) {}*/
		
		
		new Thread() {
	         
	        @Override
	        public void run() {
	        	try {
	    			
	    			File customDir = new File(mapPath);
	    			if (customDir.exists() || customDir.mkdirs()) {
	    		         FileOutputStream fileOut = new FileOutputStream(mapPath+chunk.getFileName());
	    		         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	    		         out.writeObject(chunk);
	    		         out.close();
	    		         fileOut.close();
	    			}
	    	      } catch (IOException i) {
	    	         i.printStackTrace();
	    	      }
	        }
	    }.start();
	}
	
	public void saveEverythingOnDisk() {
		//Saves the whole map on disk
	}
	
	public boolean chunkExistsOnDisk(int x, int y) {
		return (new File(mapPath+Chunk.getFileName(x, y)).exists());
	}
	
	public boolean chunkExists(int x, int y) {
		//check both on hashMap and directory
		if(chunks.get(x)!=null && chunks.get(x).get(y)!=null)
			return true;
		else
			return false;
	}

}
