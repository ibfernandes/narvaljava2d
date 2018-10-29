package engine.logic;

import java.io.Serializable;
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
import engine.noise.FastNoise;
import engine.renderer.ASM;
import engine.utilities.Color;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import net.jafama.FastMath;

public class Chunk implements Serializable{
	
	private int 	mapRGB[][];
	private int	x,y;
	private int chunkWidth, chunkHeight;  //TODO: redundant INFO to save on each chunk
	private int textureWidth, textureHeight;
	private int mapWidth, mapHeight; //TODO: redundant INFO to save on each chunk
	private ArrayList<GameObject> objectLayer;
	private int noiseDivisor = 5;
	private HashMap<Float, Entity> objects;
	private transient Random random = new Random();
	private transient EntityManager em;
	
	//TODO: Should pass/refine rules to map generation
	//TODO: Should get its size from something static final since it'll be the same for all of them. (Instead of saving it to a file for every single Chunk
	public Chunk (int x, int y, int chunkWidth, int chunkHeight, int mapWidth, int mapHeight, EntityManager em) {
		this.em = em;
		this.x = x;
		this.y = y;
		this.chunkWidth = chunkWidth;
		this.chunkHeight = chunkHeight;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
			
		textureWidth = (int) (chunkWidth/noiseDivisor);
		textureHeight = (int) (chunkHeight/noiseDivisor);
		
		mapRGB = new int[textureWidth][textureHeight];
		objects = new HashMap<>();
	}
	
	public int[][] getTerrain(){
		return mapRGB;
	}
	
	
	public float noise(FastNoise fastNoise, float x, float y) { //TODO: I could make it parallel and so increase performance[?]
		float noise = 0;
		fastNoise.SetSeed(12345);
		
		fastNoise.SetFrequency(0.0005f);	 //quanto menor, maior as "ilhas"
		noise +=  fastNoise.GetPerlin(x, y); //first octave
		
		fastNoise.SetFrequency(0.005f);
		noise += .1 * fastNoise.GetPerlin(x, y); // second octave
		
		return noise;
	}
	
	public void generateTerrain() {
		float d;
		float a = 0.15f;
		float b = 0.9f;
		float c = 2f;
		float perlinNoise[][] = new float[textureWidth][textureHeight];
		float whiteNoise[][] = new float[textureWidth][textureHeight];
		float fractalNoise[][] = new float[textureWidth][textureHeight];
		FastNoise fastNoise = new FastNoise();
		
		for(int y=0; y<textureHeight; y++) {
			for(int x=0; x<textureWidth;x++) {
				int coordX = ((this.x*chunkWidth)/noiseDivisor) + x ; // getx + x*divisor //TODO MAKE X*TEXTWIDTH
				int coordY = ((this.y*chunkHeight)/noiseDivisor) + y ; //
				
				
				d = 2*Math.max(Math.abs((float)coordX/mapWidth - (float)(mapWidth/2)/mapWidth), Math.abs((float)coordY/mapHeight - (float)(mapHeight/2)/mapHeight)); //as the distance must be normlized,
				// i simply normalize the data before calculating the distance

				perlinNoise[x][y] = noise(fastNoise,coordX/3f,coordY);
				whiteNoise[x][y] = fastNoise.GetWhiteNoise(coordX, coordY); 
				fractalNoise[x][y] = fastNoise.GetPerlinFractal(coordX/4, coordY); 
				
				perlinNoise[x][y] = perlinNoise[x][y] + a - b*(float)Math.pow(d, c);
	
				//double dx = FastMath.sin(Math.toRadians(timer.getDegree()));
				//double dxWet = FastMath.sin(Math.toRadians(timerWetSand.getDegree()));
				double dx = 0;
				double dxWet = 0;
				
				
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
				
				if(perlinNoise[x][y]<-.230 + dxWet*.016) {	//wet sand
					mapRGB[x][y] = (255<<24) | (224<<16) | (214<<8) | (167); //ARGB
					if(whiteNoise[x][y]>0)
						mapRGB[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(perlinNoise[x][y]<-.230 + dx*.016) 	//espuma
					mapRGB[x][y] = Color.WHITE; //ARGB
				
				if(perlinNoise[x][y]<-.244 + dx*.016) { 	//espuma back
					mapRGB[x][y] = (255<<24) | (22<<16) | (160<<8) | (133); //green se
						if(perlinNoise[x][y]>-.2445 + dx*.016) {
							if(whiteNoise[x][y]<0.2f)
								mapRGB[x][y] = Color.WHITE;
						}
				}
				
				if(perlinNoise[x][y]<=-.266 + dx*.016)  //water
					mapRGB[x][y] = Color.TURKISH; //turquesa
				
				//NOTA: valores crescem para baixo
		
		
				//create scnearion elements

				//TODO: the pool is jsut growing without limit. Need to fix that.
				if(whiteNoise[x][y]>0.9999 && (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(objects.containsKey(whiteNoise[x][y]))
						continue;
					
					//objects.put(whiteNoise[x][y], generateRandomTree(x,y));
				}else if(whiteNoise[x][y]>0.99 && (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(objects.containsKey(whiteNoise[x][y]))
						continue;
					
					objects.put(whiteNoise[x][y], generateRandomGroundVegetation(x,y));
				}
			}
		}
	}
	
	public Entity generateRandomTree(int x, int y) {
		Vec2 orientation;
		Vec2 position = new Vec2(x*noiseDivisor + this.x*chunkWidth, y*noiseDivisor + this.y*chunkHeight);
		if(random.nextBoolean())
			orientation = new Vec2(0,0);
		else
			orientation = new Vec2(1,0);
		
		ASM asm = new ASM(); //TODO: setTexutre not working?!
		
		Animation an;
		an = new Animation("tree", -1);
		
		an.setFrames(1, new Vec2(0,0), new Vec2(67,51)); // TODO: cuting lastline´, something to with squared size?
		asm.addAnimation("idle_1", an);
		asm.changeStateTo("idle_1");
		
		
		Entity e = BasicEntity.generate(em, "grassRenderer", position, null, orientation, new Vec2(740, 612), asm); //TODO: i'll need to make sure that every time i load a chunk all id's are RE-generated so they're UNIQUE
		
		return e;
	}
	
	public Entity generateRandomGroundVegetation(int x, int y) {
		Vec2 orientation;
		Vec2 position = new Vec2(x*noiseDivisor + this.x*chunkWidth, y*noiseDivisor + this.y*chunkHeight);
		if(random.nextBoolean())
			orientation = new Vec2(0,0);
		else
			orientation = new Vec2(1,0);
		
		ASM asm = new ASM(); //TODO: setTexutre not working?!
		
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
		
		
		Entity e = BasicEntity.generate(em, "grassRenderer", position, null, orientation, new Vec2(30,24), asm); //TODO: i'll need to make sure that every time i load a chunk all id's are RE-generated so they're UNIQUE
		
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
}
