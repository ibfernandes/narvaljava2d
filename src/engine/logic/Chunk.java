package engine.logic;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.jbox2d.dynamics.BodyType;

import engine.engine.Engine;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.componentModels.BasicEntity;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.noise.FastNoise;
import engine.noise.FastNoise.Interp;
import engine.noise.FastNoise.NoiseType;
import engine.utilities.BufferUtilities;
import engine.utilities.Color;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import net.jafama.FastMath;

public class Chunk implements Serializable{
	
	private int 	mapRGB[][];
	private int	x,y;
	private transient int lastX, lastY;
	private int chunkWidth, chunkHeight;  //TODO: redundant INFO to save on each chunk
	private int textureWidth, textureHeight;
	private int mapWidth, mapHeight; //TODO: redundant INFO to save on each chunk
	private ArrayList<GameObject> objectLayer;
	public static int NOISE_DIVISOR = 5;
	private HashMap<Float, Entity> objects;
	private transient Random random = new Random();
	private transient EntityManager em;
	private double waterDx = 1;
	private double wetSandDx = 1;
	private float perlinNoise[][];
	private float whiteNoise[][];
	private float fractalNoise[][];
	private FastNoise fastNoise = new FastNoise();
	private ByteBuffer terrainBuffer;
	private double waveVariation = 0.016;
	private Rectangle boundingBox =new Rectangle(0,0,0,0);
	
	//TODO: Should pass/refine rules to map generation
	//TODO: Should get its size from something static final since it'll be the same for all of them. (Instead of saving it to a file for every single Chunk
	public Chunk (int x, int y, int chunkWidth, int chunkHeight, int mapWidth, int mapHeight, EntityManager em) {
		this.em = em;
		this.x = x;
		this.y = y;
		this.lastX = x;
		this.lastY = y;
		this.chunkWidth = chunkWidth;
		this.chunkHeight = chunkHeight;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		
		boundingBox.x = x*chunkWidth;
		boundingBox.y = y*chunkHeight;
		boundingBox.width = chunkWidth;
		boundingBox.height = chunkHeight;
			
		textureWidth = (int) (chunkWidth/NOISE_DIVISOR);
		textureHeight = (int) (chunkHeight/NOISE_DIVISOR);
		
		mapRGB = new int[textureWidth][textureHeight];
		perlinNoise = new float[textureWidth][textureHeight];
		whiteNoise = new float[textureWidth][textureHeight];
		fractalNoise = new float[textureWidth][textureHeight];
		objects = new HashMap<>();
		fastNoise.SetSeed(12345);
		
		terrainBuffer = BufferUtilities.createByteBuffer(new byte[textureWidth * textureHeight * Texture.BYTES_PER_PIXEL]);
	}
	
	public int[][] getTerrain(){
		return mapRGB;
	}
	
	public float noise(FastNoise fastNoise, float x, float y) { //TODO: I could make it parallel and so increase performance[?]
		float noise = 0;
		
		fastNoise.SetFrequency(0.0005f);	 //quanto menor, maior as "ilhas"
		noise +=  fastNoise.GetPerlin(x, y); //first octave
		
		fastNoise.SetFrequency(0.005f);
		noise += .1 * fastNoise.GetPerlin(x, y); // second octave
		
		return noise;
	}
	
	public void generateNoise() {
		float d;
		float a = 0.15f;
		float b = 0.9f;
		float c = 2f;
		
		int coordX = ((this.x*chunkWidth)/NOISE_DIVISOR)  ; // getx + x*divisor //TODO MAKE X*TEXTWIDTH
		int coordY = ((this.y*chunkHeight)/NOISE_DIVISOR)  ; //
		//int coordX = ((this.x)/NOISE_DIVISOR);
		//int coordY = ((this.y)/NOISE_DIVISOR);
		
		int constX = coordX;
		int constY = coordY;
		
		for(int y=0; y<textureHeight; y++) {
			for(int x=0; x<textureWidth;x++) {
				coordX = constX + x;
				coordY = constY + y;
				
				d = 2*Math.max(Math.abs((float)coordX/mapWidth - (float)(mapWidth/2)/mapWidth), Math.abs((float)coordY/mapHeight - (float)(mapHeight/2)/mapHeight)); //as the distance must be normlized,
				// i simply normalize the data before calculating the distance

				perlinNoise[x][y] = noise(fastNoise,coordX/3f,coordY);
				whiteNoise[x][y] = fastNoise.GetWhiteNoise(coordX, coordY); 
				fractalNoise[x][y] = fastNoise.GetPerlinFractal(coordX/4, coordY); 
				
				perlinNoise[x][y] = perlinNoise[x][y] + a - b*(float)Math.pow(d, c);
			}
		}
	}
	
	public void generateTerrain() {
		terrainBuffer.clear();

		for(int y=0; y<textureHeight; y++) {
			for(int x=0; x<textureWidth;x++) {
				
				if(perlinNoise[x][y]>-.1 ) { 		//land
					mapRGB[x][y] = Color.GRASS_GROUND; //esmeralda
					if(fractalNoise[x][y]>0.2)
						mapRGB[x][y] = Color.GRASS_GROUND_LIGHTER;
				}
				
				if(perlinNoise[x][y]<=-.1)  //preenche tudo com água
					mapRGB[x][y] = 	Color.OCEAN_GROUND; //turquesa
				
				if(perlinNoise[x][y]<=-.1) {	//sand
					mapRGB[x][y] =  (255<<24) | (244<<16) | (234<<8) | (187); //ARGB
					if(whiteNoise[x][y]>0)
						mapRGB[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(perlinNoise[x][y]<-.230 + wetSandDx*waveVariation) {	//wet sand
					mapRGB[x][y] = (255<<24) | (224<<16) | (214<<8) | (167); //ARGB
					if(whiteNoise[x][y]>0)
						mapRGB[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(perlinNoise[x][y]<-.230 + waterDx*waveVariation) 	//espuma
					mapRGB[x][y] = Color.WHITE; //ARGB
				
				if(perlinNoise[x][y]<-.244 + waterDx*waveVariation) { 	//espuma back
					mapRGB[x][y] = (255<<24) | (22<<16) | (160<<8) | (133); //green se
						if(perlinNoise[x][y]>-.2445 + waterDx*waveVariation) {
							if(whiteNoise[x][y]<0.2f)
								mapRGB[x][y] = Color.WHITE;
						}
				}
				
				if(perlinNoise[x][y]<=-.266 + waterDx*waveVariation)  //water
					mapRGB[x][y] = Color.TURKISH; //turquesa
				
				//NOTA: valores crescem para baixo
		

				//TODO: the pool is jsut growing without limit. Need to fix that.
				if(whiteNoise[x][y]>0.9999 && (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(objects.containsKey(whiteNoise[x][y]))
						continue;

					//objects.put(whiteNoise[x][y], generateRandomTree(x,y));
				}else if(whiteNoise[x][y]>0.99 && (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(objects.containsKey(whiteNoise[x][y]))
						continue;
					
					//objects.put(whiteNoise[x][y], generateRandomGroundVegetation(x,y));
				}
				
				terrainBuffer.put((byte) ((mapRGB[x][y] >> 16) & 0xFF));     		// Red component
                terrainBuffer.put((byte) ((mapRGB[x][y] >> 8) & 0xFF));      		// Green component
                terrainBuffer.put((byte) (mapRGB[x][y] & 0xFF));              	// Blue component
                terrainBuffer.put((byte) ((mapRGB[x][y] >> 24) & 0xFF));    		// Alpha component. Only for RGBA
			}
		}
		
		terrainBuffer.flip();
	}
	
	public Entity generateRandomTree(int x, int y) {
		Vec2 orientation;
		Vec2 position = new Vec2(x*NOISE_DIVISOR + this.x*chunkWidth, y*NOISE_DIVISOR + this.y*chunkHeight);
		//Vec2 position = new Vec2(x*NOISE_DIVISOR + this.x, y*NOISE_DIVISOR + this.y);
		if(random.nextBoolean())
			orientation = new Vec2(0,0);
		else
			orientation = new Vec2(1,0);
		
		AnimationStateManager asm = new AnimationStateManager(); //TODO: setTexutre not working?!
		
		Animation an;
		an = new Animation("tree", -1);
		
		an.setFrames(1, new Vec2(0,0), new Vec2(64,64)); // TODO: cuting lastline´, something to with squared size?
		asm.addAnimation("idle_1", an);
		asm.changeStateTo("idle_1");
		
		
		Entity e = BasicEntity.generate(em, "grassRenderer", position, null, orientation, new Vec2(740, 612), asm,new Rectangle(0.0f,0.99f,1.0f,0.1f)); //TODO: i'll need to make sure that every time i load a chunk all id's are RE-generated so they're UNIQUE
		
		return e;
	}
	
	public Entity generateRandomGroundVegetation(int x, int y) {
		Vec2 orientation;
		Vec2 position = new Vec2(x*NOISE_DIVISOR + this.x*chunkWidth, y*NOISE_DIVISOR + this.y*chunkHeight);
		//Vec2 position = new Vec2(x*NOISE_DIVISOR + this.x, y*NOISE_DIVISOR + this.y);
		if(random.nextBoolean())
			orientation = new Vec2(0,0);
		else
			orientation = new Vec2(1,0);
		
		AnimationStateManager asm = new AnimationStateManager(); //TODO: setTexutre not working?!
		
		Animation an;
		/*if(whiteNoise[x][y]>0.9995)
			an = new Animation("flower_red", -1);
		else if(whiteNoise[x][y]>0.9991)
			an = new Animation("flower_blue", -1);
		else
			an = new Animation("flower", -1);*/
	
		an = new Animation("grass", -1);
		
		//an.setFrames(1, new Vec2(0,0), new Vec2(12,12)); // TODO: cuting lastline´, something to with squared size?
		an.setFrames(1, new Vec2(0,0), new Vec2(10,8)); // TODO: cuting lastline´, something to with squared size?
		asm.addAnimation("idle_1", an);
		asm.changeStateTo("idle_1");
		
		
		
		Entity e = BasicEntity.generate(em, "grassRenderer", position, null, orientation, new Vec2(30,24), asm, new Rectangle(0.0f,0.9f,1.0f,0.1f)); //TODO: i'll need to make sure that every time i load a chunk all id's are RE-generated so they're UNIQUE
		
		return e;
	}
	
	public ArrayList<Entity> getObjects(){
		return new ArrayList<>(objects.values());
	}
	
	public Vec2i getPosition() {
		return new Vec2i(x,y);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public String getFileName() {
		return x+"_"+y+".chunk";
	}
	
	public static String getFileName(int x, int y) {
		return x+"_"+y+".chunk";
	}
	
	public static String getID(int x, int y) {
		return x+"_"+y;
	}

	public void setX(int x) {
		lastX = this.x;
		this.x = x;
	}

	public void setY(int y) {
		lastY = this.y;
		this.y = y;
	}

	public double getWaterDx() {
		return waterDx;
	}

	public void setWaterDx(double waterDx) {
		this.waterDx = waterDx;
	}

	public double getWetSandDx() {
		return wetSandDx;
	}

	public void setWetSandDx(double wetSandDx) {
		this.wetSandDx = wetSandDx;
	}

	public ByteBuffer getTerrainBuffer() {
		return terrainBuffer;
	}

	public void setTerrainBuffer(ByteBuffer terrainBuffer) {
		this.terrainBuffer = terrainBuffer;
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}	
}
