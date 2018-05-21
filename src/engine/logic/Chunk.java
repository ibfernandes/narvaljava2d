package engine.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import engine.engine.Engine;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.noise.FastNoise;
import engine.utilities.Color;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;
import graphic.ASM;
import net.jafama.FastMath;

public class Chunk implements Serializable{
	
	private int 	mapRGB[][];
	private int	x,y;
	private int chunkWidth, chunkHeight;  //TODO: redundant INFO to save on each chunk
	private int textureWidth, textureHeight;
	private int mapWidth, mapHeight; //TODO: redundant INFO to save on each chunk
	private ArrayList<GameObject> objectLayer;
	private int noiseDivisor = 5;
	private static final long serialVersionUID = 1L;
	HorizontalPool grassPool = new HorizontalPool(500);
	//private Rectangle boundingBox;
	
	//Should pass rules to map generation
	//Should get its size from something static final since it'll be the same for all of them.
	public Chunk (int x, int y, int chunkWidth, int chunkHeight, int mapWidth, int mapHeight) { 
		this.x = x;
		this.y = y;
		this.chunkWidth = chunkWidth;
		this.chunkHeight = chunkHeight;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;
		
		//boundingBox = new Rectangle(x,y,chunkWidth, chunkHeight);
		
		textureWidth = (int) (chunkWidth/noiseDivisor);
		textureHeight = (int) (chunkHeight/noiseDivisor);
		
		mapRGB = new int[textureWidth][textureHeight];
		
		//long start = System.nanoTime();
		generateTerrain();
		//System.out.println("\n generateTerrain: "+(System.nanoTime()-start)/Engine.MILISECOND);
		//generateGameObjectLayers();
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
	
	private void generateTerrain() {
		float d;
		float a = 0.15f;
		float b = 0.9f;
		float c = 2f;
		float perlinNoise[][] = new float[textureWidth][textureHeight];
		float whiteNoise[][] = new float[textureWidth][textureHeight];
		float fractalNoise[][] = new float[textureWidth][textureHeight];
		Random random = new Random();
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
					mapRGB[x][y] = Color.ESMERALDA; //esmeralda
					if(fractalNoise[x][y]>0.2)
						mapRGB[x][y] = Color.DARKED_ESMERALDA;
				}
				if(perlinNoise[x][y]<=-.1)  //preenche tudo com água
					mapRGB[x][y] = 	Color.TURKISH; //turquesa
				
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
					mapRGB[x][y] = 	(255<<24) | (26<<16) | (188<<8) | (156); //turquesa
				
				//NOTA: valores crescem para baixo
		
		
				//create scnearion elements

				//TODO: the pool is jsut growing without limit. Need to fix that.
				if(whiteNoise[x][y]>0.9999 && (mapRGB[x][y] == Color.ESMERALDA || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(grassPool.contains(whiteNoise[x][y]))
						continue;
					
					GameObject o = new GameObject(); //TODO: Should optimize this so i don't need to create an object every time.
					o.setSize(new Vec2(512,512));
					o.setVelocity(0);
					o.setColor(new Vec4(1,1,1,1));
					if(random.nextBoolean())
						o.setOrientation(new Vec2(0,0));
					else
						o.setOrientation(new Vec2(1,0));
					o.setBaseBox(new Vec2(512, 16));
					o.setSkew(new Vec2(0,0));
					o.setPosition(new Vec2(x*noiseDivisor + this.x*chunkWidth,y*noiseDivisor + this.y*chunkHeight)); //TODO: fix that to a proper interval
					ASM asm = new ASM(); //TODO: setTexutre not working?!
					
					Animation an;
					an = new Animation("tree", -1);
					
					an.setFrames(1, new Vec2(0,0), new Vec2(64,64)); // TODO: cuting lastline´, something to with squared size?
					asm.addAnimation("idle_1", an);
					asm.changeStateTo("idle_1");
					o.setAnimations(asm);
					
					grassPool.add(o, whiteNoise[x][y]);
				}else if(whiteNoise[x][y]>0.999 && (mapRGB[x][y] == Color.ESMERALDA || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if(grassPool.contains(whiteNoise[x][y]))
						continue;
					GameObject o = new GameObject(); //TODO: Should optimize this so i don't need to create an object every time.
					o.setSize(new Vec2(60,40));
					o.setVelocity(0);
					o.setColor(new Vec4(1,1,1,1));
					if(random.nextBoolean())
						o.setOrientation(new Vec2(0,0));
					else
						o.setOrientation(new Vec2(1,0));
					o.setBaseBox(new Vec2(60, 16));
					o.setSkew(new Vec2(0,0));
					o.setPosition(new Vec2(x*noiseDivisor + this.x*chunkWidth,y*noiseDivisor + this.y*chunkHeight));
					ASM asm = new ASM(); //TODO: setTexutre not working?!
					
					Animation an;
					if(whiteNoise[x][y]>0.9995)
						an = new Animation("flower_red", -1);
					else if(whiteNoise[x][y]>0.9991)
						an = new Animation("flower_blue", -1);
					else
						an = new Animation("flower", -1);
					
					an.setFrames(1, new Vec2(0,0), new Vec2(12,12)); // TODO: cuting lastline´, something to with squared size?
					asm.addAnimation("idle_1", an);
					asm.changeStateTo("idle_1");
					o.setAnimations(asm);
					
					grassPool.add(o, whiteNoise[x][y]);
				}
			
			}
		}
	}
	
	private void generateStaticLayer() {
		
	}
	private void generateMovableLayer() {
		
	}
	
	private void generateGameObjectLayers() {
		generateStaticLayer();
		generateMovableLayer();
	}
	
	public ArrayList<GameObject> getStaticLayer(){
		return grassPool.getPool();
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
		return x+"_"+y+".chunck";
	}
	
	public static String getFileName(int x, int y) {
		return x+"_"+y+".chunck";
	}

	/*public Rectangle getBoundingBox() {
		return boundingBox;
	}*/
	
}
