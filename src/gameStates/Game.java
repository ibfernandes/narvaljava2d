package gameStates;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;

import static org.lwjgl.openal.AL10.*;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL32;


import static org.lwjgl.opengl.GL30.*;

import editor.Editor;
import engine.ai.DStarLite;
import engine.audio.Audio;
import engine.controllers.AIController;
import engine.controllers.PlayerController;
import engine.engine.Engine;
import engine.engine.GameState;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.logic.Camera;
import engine.logic.GameObject;
import engine.logic.HorizontalPool;
import engine.logic.Timer;
import engine.noise.FastNoise;
import engine.physics.Hit;
import engine.utilities.BufferUtilities;
import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import graphic.ASM;
import graphic.CubeRenderer;
import graphic.GrassRenderer;
import graphic.ShadowRenderer;
import graphic.TextureRenderer;
import javafx.scene.image.PixelWriter;
import net.jafama.FastMath;

public class Game extends GameState{
	
	//Layers
	private ArrayList<GameObject> movableLayer;
	private ArrayList<GameObject> staticLayer;
	private ArrayList<GameObject> regionLayer;
	private ArrayList<GameObject> finalLayer; //composes all other layers into one, so it can be properly sorted and drawn
	private Camera camera;
	private Rectangle screenView;
	private GameObject player;
	
	//Shadow Map
	private int shadowFBO;
	private int shadowLayerTexture;
	
	//Map PCG
	private FastNoise fastNoise = new FastNoise();
	private int seed = new Random().nextInt(2000);
	
	private float noiseDivisor = 5f;
	private int noiseWidth  = (int) ((float)Engine.getSelf().getWindow().getWidth()/noiseDivisor);
	private int noiseHeight = (int) ((float)Engine.getSelf().getWindow().getHeight()/noiseDivisor);
	
	private float perlinNoise[][] = new float[noiseWidth][noiseHeight];
	private float whiteNoise[][] = new float[noiseWidth][noiseHeight];
	private float fractalNoise[][] = new float[noiseWidth][noiseHeight];
	
	private int 	noiseRGB[][] = new int[noiseWidth][noiseHeight];
	
	private HorizontalPool grassPool = new HorizontalPool(500);
	
	//Island formula used along with Noise generation.
	private Texture terrain;
	private float a = 0.15f;
	private float b = 0.9f;
	private float c = 2f;
	private float d = 0f;
	private Timer timer = new Timer();
	private Timer timerWetSand = new Timer();
	private Random random = new Random();
	
	private int map_width = 60000;
	private int map_height = 60000;
	public static final int ESMERALDA = (255<<24) | (56<<16) | (204<<8) | (113); //TODO: Move to a Color class;
	public static final int DARKED_ESMERALDA = (255<<24) | (52<<16) | (200<<8) | (109);
	public static final int WHITE = (255<<24) | (255<<16) | (255<<8) | (255);
	public static final int TURKISH = (255<<24) | (26<<16) | (188<<8) | (156);
	
	//Graph
	public int graphDivisor = 8;
	public int graphSizeX = Engine.getSelf().getWindow().getWidth()/graphDivisor;
	public int graphSizeY = Engine.getSelf().getWindow().getWidth()/graphDivisor;
	public boolean obstacleMap[][];
	
	@Override
	public void init() {
		initShadowLayer();
		screenView = new Rectangle(0,0,Engine.getSelf().getWindow().getWidth(),Engine.getSelf().getWindow().getHeight());
		
		//==================================
		//Loads all shaders
		//==================================
		ResourceManager.getSelf().loadShader("cube", 
							"shaders/cube.vs",
							"shaders/cube.fs",
							null);
		ResourceManager.getSelf().loadShader("texture", 
							"shaders/texture.vs",
							"shaders/texture.fs",
							null);
		ResourceManager.getSelf().loadShader("shadow", 
							"shaders/shadow.vs",
							"shaders/shadow.fs",
							null);
		ResourceManager.getSelf().loadShader("grass", 
							"shaders/grass.vs",
							"shaders/grass.fs",
							null);
		
		//==================================
		//Loads all textures
		//==================================
		ResourceManager.getSelf().loadTexture("rogue", 
				"sprites/rogue.png");
		ResourceManager.getSelf().loadTexture("rogue_normal", 
				"sprites/rogue_normal.png");
		ResourceManager.getSelf().loadTexture("cleric", 
				"sprites/cleric.png");
		ResourceManager.getSelf().loadTexture("cleric_normal", 
			"sprites/cleric_normal.png");
		ResourceManager.getSelf().loadTexture("ranger", 
				"sprites/ranger.png");
		ResourceManager.getSelf().loadTexture("grass", 
				"sprites/grass.png");
		ResourceManager.getSelf().loadTexture("wheat", 
				"sprites/wheat.png");
		ResourceManager.getSelf().loadTexture("wooden_chair", 
				"sprites/wooden_chair.png");
		ResourceManager.getSelf().loadTexture("wooden_box", 
				"sprites/wooden_box.png");
		ResourceManager.getSelf().loadTexture("cube", 
				"sprites/cube.png");
		ResourceManager.getSelf().loadTexture("bonfire", 
				"sprites/bonfire.png");
		ResourceManager.getSelf().loadTexture("flower", 
				"sprites/flower.png");
		ResourceManager.getSelf().loadTexture("flower_blue", 
				"sprites/flower_blue.png");
		ResourceManager.getSelf().loadTexture("flower_red", 
				"sprites/flower_red.png");
		ResourceManager.getSelf().loadTexture("tree", 
				"sprites/tree.png");
		ResourceManager.getSelf().loadTexture("house", 
				"sprites/house.png");
		ResourceManager.getSelf().loadTexture("ranger_swimming", 
				"sprites/ranger_swimming.png");
		
		//==================================
		//Loads all Audio
		//==================================
		ResourceManager.getSelf().loadAudio("ocean_waves","audio/ocean_waves.ogg" );

		//==================================
		//Set all Uniforms
		//==================================
		Mat4 projection = new Mat4();
		projection = projection.ortho(0, Engine.getSelf().getWindow().getWidth(), Engine.getSelf().getWindow().getHeight(), 0, -1f, 1f);

		ResourceManager.getSelf().getShader("cube").use();
		ResourceManager.getSelf().getShader("cube").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("projection", projection);
		
		//==================================
		//Start renderers
		//==================================
		TextureRenderer t = new TextureRenderer(ResourceManager.getSelf().getShader("texture"));
		CubeRenderer r = new CubeRenderer(ResourceManager.getSelf().getShader("cube"));
		ShadowRenderer s = new ShadowRenderer(ResourceManager.getSelf().getShader("shadow"));
		GrassRenderer g = new GrassRenderer(ResourceManager.getSelf().getShader("grass"));
		
		ResourceManager.getSelf().setTextureRenderer(t);
		ResourceManager.getSelf().setCubeRenderer(r);
		ResourceManager.getSelf().setShadowRenderer(s);
		ResourceManager.getSelf().setGrassRenderer(g);
		
		//==================================
		//Instantiate Layers
		//==================================
		movableLayer = new ArrayList<>();
		staticLayer = new ArrayList<>();
		regionLayer = new ArrayList<>();
		finalLayer = new ArrayList<>();
		
		//==================================
		//Creates other GameObjects
		//==================================

		
		//==================================
		//Create enemies
		//==================================
		createClerics(0);
		
		//==================================
		//Create props
		//==================================
		//createGrass(25);
		//createWheat(6);
		//createProps(13);
		
		//Create fire
		GameObject bonfire = new GameObject();
		bonfire.setSize(new Vec2(64,64));
		bonfire.setVelocity(0);
		bonfire.setColor(new Vec4(1,1,1,1));
		bonfire.setOrientation(new Vec2(0,0));
		bonfire.setSkew(new Vec2(0,0));
		bonfire.setPosition(new Vec2(600,350));
		
		ASM asmb = new ASM();
		
		Animation ab = new Animation("bonfire", 100);
		ab.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asmb.addAnimation("idle_1", ab);
		
		asmb.changeStateTo("idle_1");
		
		bonfire.setAnimations(asmb);
		
		staticLayer.add(bonfire);
		
		
		GameObject house = new GameObject();
		house.setSize(new Vec2(520,660));
		house.setVelocity(1200);
		house.setColor(new Vec4(1,1,1,1));
		house.setController(null);
		house.setOrientation(new Vec2(0,0));
		house.setBaseBox(new Vec2(520, 300));
		house.setSkew(new Vec2(0,0));
		house.setPosition(new Vec2(46000, 46000));
		house.setTexture("house");
		staticLayer.add(house);
		
		//==================================
		//Creates player
		//==================================
		PlayerController playerController =  new PlayerController();
		
		player = new GameObject();
		player.setSize(new Vec2(128,128));
		player.setVelocity(1200);
		player.setColor(new Vec4(1,1,1,1));
		player.setController(playerController);
		player.setOrientation(new Vec2(0,0));
		player.setBaseBox(new Vec2(128, 20));
		player.setSightBox(new Vec2(512, 512));
		player.setSkew(new Vec2(0,0));
		player.setPosition(new Vec2(45000,45000));
		
		ASM asm = new ASM();
		
		Animation a = new Animation("ranger", 150);
		a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", a);
		
		a = new Animation("ranger", 150);
		a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
		asm.addAnimation("idle_2", a);
		
		a = new Animation("ranger", 150);
		a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
		asm.addAnimation("walking", a);
		
		a = new Animation("ranger", 150);
		a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
		asm.addAnimation("attacking", a);
		
		a = new Animation("ranger", 150);
		a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
		asm.addAnimation("dying", a);
		
		asm.changeStateTo("idle_1");
		
		player.setAnimations(asm);
		//movableLayer.add(player);
		
		//==================================
		//Camera
		//==================================
		camera = new Camera();
		camera.setFocusOn(player);
		camera.move(-35000, -35000);
		
		//==================================
		//Tests
		//==================================
		timerWetSand.setDegree(260);
		//ResourceManager.getSelf().playAudio("ocean_waves", player.getPosition(), 3000);
	}
	
	private void initShadowLayer() {
		shadowFBO = glGenFramebuffers();
		
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);
		
		shadowLayerTexture = GL11.glGenTextures();
		
		GL11.glBindTexture(GL_TEXTURE_2D, shadowLayerTexture);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, Engine.getSelf().getWindow().getWidth(), Engine.getSelf().getWindow().getHeight(), 0, GL11.GL_RGBA, GL_UNSIGNED_BYTE, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

        //Setup texture scaling filtering
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST); //GL_LINEAR for smooth
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, shadowLayerTexture, 0);
		
		if(glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.err.println("ERROR: Shadow layer FBO.");
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}
	
	public float noise(float x, float y) { //TODO: I could make it parallel and so increase performance[?]
		float noise = 0;
		fastNoise.SetSeed(12345);
		
		fastNoise.SetFrequency(0.0005f);	 //quanto menor, maior as "ilhas"
		noise +=  fastNoise.GetPerlin(x, y); //first octave
		
		fastNoise.SetFrequency(0.005f);
		noise += .1 * fastNoise.GetPerlin(x, y); // second octave
		
		return noise;
	}

	public void generateTerrain() { 
		timer.setDuration(Timer.SECOND*8);
		timerWetSand.setDuration(Timer.SECOND*8);
		
		for(int y=0; y<noiseHeight; y++) {
			for(int x=0; x<noiseWidth;x++) {
				int coordX = (int)(camera.getX()/noiseDivisor) + x ; // getx + x*divisor
				int coordY = (int)(camera.getY()/noiseDivisor) + y ; //
				
				d = 2*Math.max(Math.abs((float)coordX/map_width - (float)(map_width/2)/map_width), Math.abs((float)coordY/map_height - (float)(map_height/2)/map_height)); //as the distance must be normlized,
				// i simply normalize the data before calculating the distance

				perlinNoise[x][y] = noise(coordX/3f,coordY);
				whiteNoise[x][y] = fastNoise.GetWhiteNoise(coordX, coordY); 
				fractalNoise[x][y] = fastNoise.GetPerlinFractal(coordX/4, coordY); 
				
				perlinNoise[x][y] = perlinNoise[x][y] + a - b*(float)Math.pow(d, c);
	
				double dx = FastMath.sin(Math.toRadians(timer.getDegree()));
				double dxWet = FastMath.sin(Math.toRadians(timerWetSand.getDegree()));
				
				if(perlinNoise[x][y]>-.1 ) { 		//land
					noiseRGB[x][y] = ESMERALDA; //esmeralda
					if(fractalNoise[x][y]>0.2)
						noiseRGB[x][y] = DARKED_ESMERALDA;
				}
				if(perlinNoise[x][y]<=-.1)  //preenche tudo com água
					noiseRGB[x][y] = 	TURKISH; //turquesa
				
				if(perlinNoise[x][y]<=-.1) {	//sand
					noiseRGB[x][y] =  (255<<24) | (244<<16) | (234<<8) | (187); //ARGB
					if(whiteNoise[x][y]>0)
						noiseRGB[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(perlinNoise[x][y]<-.230 + dxWet*.016) {	//wet sand
					noiseRGB[x][y] = (255<<24) | (224<<16) | (214<<8) | (167); //ARGB
					if(whiteNoise[x][y]>0)
						noiseRGB[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(perlinNoise[x][y]<-.230 + dx*.016) 	//espuma
					noiseRGB[x][y] = WHITE; //ARGB
				
				if(perlinNoise[x][y]<-.244 + dx*.016) { 	//espuma back
						noiseRGB[x][y] = (255<<24) | (22<<16) | (160<<8) | (133); //green se
						if(perlinNoise[x][y]>-.2445 + dx*.016) {
							if(whiteNoise[x][y]<0.2f)
								noiseRGB[x][y] = WHITE;
						}
				}
				
				if(perlinNoise[x][y]<=-.266 + dx*.016)  //water
					noiseRGB[x][y] = 	(255<<24) | (26<<16) | (188<<8) | (156); //turquesa
				
				//NOTA: valores crescem para baixo
		
		
				//create scnearion elements

				//TODO: the pool is jsut growing without limit. Need to fix that.
				if(whiteNoise[x][y]>0.9999 && (noiseRGB[x][y] == ESMERALDA || noiseRGB[x][y] == DARKED_ESMERALDA)) {
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
					o.setPosition(new Vec2(x*noiseDivisor + (camera.getX()),y*noiseDivisor + (camera.getY()))); //TODO: fix that to a proper interval
					ASM asm = new ASM(); //TODO: setTexutre not working?!
					
					Animation a;
					a = new Animation("tree", -1);
					
					a.setFrames(1, new Vec2(0,0), new Vec2(64,64)); // TODO: cuting lastline´, something to with squared size?
					asm.addAnimation("idle_1", a);
					asm.changeStateTo("idle_1");
					o.setAnimations(asm);
					
					grassPool.add(o, whiteNoise[x][y]);
				}else if(whiteNoise[x][y]>0.999 && (noiseRGB[x][y] == ESMERALDA || noiseRGB[x][y] == DARKED_ESMERALDA)) {
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
					o.setPosition(new Vec2(x*noiseDivisor + (camera.getX()),y*noiseDivisor + (camera.getY())));
					ASM asm = new ASM(); //TODO: setTexutre not working?!
					
					Animation a;
					if(whiteNoise[x][y]>0.9995)
						a = new Animation("flower_red", -1);
					else if(whiteNoise[x][y]>0.9991)
						a = new Animation("flower_blue", -1);
					else
						a = new Animation("flower", -1);
					
					a.setFrames(1, new Vec2(0,0), new Vec2(12,12)); // TODO: cuting lastline´, something to with squared size?
					asm.addAnimation("idle_1", a);
					asm.changeStateTo("idle_1");
					o.setAnimations(asm);
					
					grassPool.add(o, whiteNoise[x][y]);
				}
			
			}
		}
		
		terrain = new Texture(noiseRGB); //TODO: not create
		
	}
	
	public void createClerics(int qtd) {
		Random r = new Random();
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setGroup("cleric");
			o.setSize(new Vec2(128,128));
			o.setVelocity(200);
			o.setColor(new Vec4(1,1,1,1));
			o.setSightBox(new Vec2(3000, 3000));
			o.setRotation(0);
			o.setSkew(new Vec2(0,0));
			o.setOrientation(new Vec2(0,0));
			o.setBaseBox(new Vec2(128, 20));
			//o.setPosition(new Vec2(35350+r.nextInt(500),35500+r.nextInt(500)));
			o.setPosition(new Vec2(38000,35700));
			
			AIController ai =  new AIController();
			o.setController(ai);
			
			ASM asm = new ASM();
			
			Animation a = new Animation("cleric", 150);
			a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
			asm.addAnimation("idle_1", a);
			
			a = new Animation("cleric", 150);
			a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
			asm.addAnimation("idle_2", a);
			
			a = new Animation("cleric", 150);
			a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
			asm.addAnimation("walking", a);
			
			a = new Animation("cleric", 150);
			a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
			asm.addAnimation("attacking", a);
			
			a = new Animation("cleric", 150);
			a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
			asm.addAnimation("dying", a);
			
			asm.changeStateTo("idle_1");
			
			o.setAnimations(asm);
			
			movableLayer.add(o);
		}
	}
	
	public void createGrass(int qtd) {
		Random r = new Random();
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setSize(new Vec2(256,256));
			o.setVelocity(0);
			o.setColor(new Vec4(1,1,1,1));
			o.setRotation(0);
			o.setSkew(new Vec2(0,0));
			o.setOrientation(new Vec2(0,0));
			o.setBaseBox(new Vec2(128, 20));
			o.setPosition(new Vec2(200+r.nextInt(500),100+r.nextInt(500)));
			
			o.setRotation((float) Math.toRadians(-180));
			o.setOrientation(new Vec2(0,1));
			o.setSkew(new Vec2(r.nextInt(40),0));

			ASM asm = new ASM();
			
			Animation a = new Animation("grass", -1);
			a.setFrames(1, new Vec2(0,0), new Vec2(32,32));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");
			
			o.setAnimations(asm);
			
			staticLayer.add(o);
		}
	}
	
	public void createWheat(int qtd) {
		Random r = new Random();
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setSize(new Vec2(256,256));
			o.setVelocity(0);
			o.setColor(new Vec4(1,1,1,1));
			o.setRotation(0);
			o.setSkew(new Vec2(0,0));
			o.setOrientation(new Vec2(0,0));
			o.setBaseBox(new Vec2(128, 20));
			o.setPosition(new Vec2(200+r.nextInt(500),100+r.nextInt(500)));
			
			o.setRotation((float) Math.toRadians(-180));
			o.setOrientation(new Vec2(0,1));
			o.setSkew(new Vec2(r.nextInt(40),0));

			ASM asm = new ASM();
			
			Animation a = new Animation("wheat", -1);
			a.setFrames(1, new Vec2(0,0), new Vec2(32,32));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");
			
			o.setAnimations(asm);
			
			staticLayer.add(o);
		}
	}
	
	public void createProps(int qtd) {
		Random r = new Random();
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setSize(new Vec2(128,128));
			o.setVelocity(0);
			o.setColor(new Vec4(1,1,1,1));
			o.setRotation(0);
			o.setSkew(new Vec2(0,0));
			o.setOrientation(new Vec2(0,0));
			o.setBaseBox(new Vec2(128, 20));
			o.setPosition(new Vec2(200+r.nextInt(500),100+r.nextInt(500)));
			
			o.setRotation((float) Math.toRadians(-180));
			o.setOrientation(new Vec2(0,1));

			ASM asm = new ASM();
			
			String tex = "";
			switch(r.nextInt(2)) {
				case 0:
					tex = "wooden_chair";
				break;
				case 1:
					tex = "wooden_box";
				break;
			}
			
			
			Animation a = new Animation(tex, -1);
			a.setFrames(1, new Vec2(0,0), new Vec2(32,32));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");
			
			o.setAnimations(asm);
			
			staticLayer.add(o);
		}
	}
	
	public boolean[][] generateGraph(GameObject obj) {
		obstacleMap = new boolean[graphSizeX][graphSizeY];
		
		for(GameObject o: finalLayer) {
			
			if(obj==o)
				continue;

			int coordXMapS = (int) (camera.getX() - o.getBaseBox().getX())/graphDivisor;
			int coordYMapS = (int) (camera.getY() - o.getBaseBox().getY())/graphDivisor;
			int sizeX = (int) (o.getBaseBox().width/graphDivisor) + 1;
			int sizeY = (int) (o.getBaseBox().height/graphDivisor) +1;
			
			if(coordXMapS<0 || coordYMapS<0) //TODO: Should consider the INTERVAL, not just the start and fisnish poitn
				continue;
			
			for(int y=coordYMapS; y< coordYMapS+sizeY; y++)
				for(int x=coordXMapS; x<coordXMapS + sizeX; x++)
					if(x<obstacleMap.length && y<obstacleMap[0].length)
						obstacleMap[x][y] = true; 	// true onde ta bloqueado
		}
		
		return obstacleMap;
	}

	public boolean isOnScreen(GameObject o) {
		return (screenView.intersects(o.getBoundingBox()));
	}
	
	@Override
	public void render() {
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);// makes OpenGL reading data from your "framebuffer"

		glClearColor(1,1,1,0);
		glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		for(GameObject o: finalLayer) 
			ResourceManager.getSelf().getShadowRenderer().render(o);
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClearColor(1,1,1,1);
		glClear(GL_COLOR_BUFFER_BIT);
		
		ResourceManager.getSelf().getTextureRenderer().render(terrain.getId(),new Vec2((int)camera.getX(),(int)camera.getY()),
				new Vec2(Engine.getSelf().getWindow().getSize()), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,0), new Vec2(0,0));
		ResourceManager.getSelf().getTextureRenderer().render(shadowLayerTexture, new Vec2(camera.getX(),camera.getY()),
				Engine.getSelf().getWindow().getSize(), 0, new Vec4(1,1,1,0.2), new Vec4(0,0,1,1), new Vec2(0,1), new Vec2(0,0));

		for(GameObject o: finalLayer) {
			o.renderDebug();
			if(o.getController()!=null)
				o.getController().renderDebug();
			
			if(grassPool.getPool().contains(o))
				ResourceManager.getSelf().getGrassRenderer().render(o);
			else
				o.render();
		}
		
		if(obstacleMap!=null)
			for(int y=0; y< obstacleMap[0].length; y++)
				for(int x=0; x< obstacleMap.length; x++) {
					
					int rx = (int) (camera.getX() + x*graphDivisor);
					int ry = (int) (camera.getY() + y*graphDivisor);
					
					//if(obstacleMap[x][y])
						//ResourceManager.getSelf().getCubeRenderer().render(new Vec2(rx, ry), new Vec2(8,8), 0, new Vec3(1,0,0));
				}
	}

	boolean shouldInc = true;
	@Override
	public void update(float deltaTime) {
		//generateGraph();
	
		alListener3f(AL_POSITION, camera.getX(),camera.getY(),0); //TODO: change to players Position instead of camera.
		
		
		timer.update();
		timerWetSand.update();
		ResourceManager.getSelf().getShader("grass").use();
		float sin = (float) Math.sin(Math.toRadians(timer.getDegree()))*1f;
		ResourceManager.getSelf().getShader("grass").setFloat("dx", (sin<0) ? sin*-1: sin);
		generateTerrain();
		/*player.update(deltaTime, this);
		for(GameObject o: movableLayer)
			o.update(deltaTime, this);
		
		for(GameObject o: grassPool.getPool())
			o.update(deltaTime, this);
		
		for(GameObject o: staticLayer) {
			float inc = 0;
			
			float aux = o.getSkew().x;
			
			if(aux<-30)
				shouldInc = true;
			if(aux>30)
				shouldInc = false;
			
			if(shouldInc)
				inc = 0.5f;
			else
				inc = -.5f;
	
			//o.setSkew(new Vec2(o.getSkew().x+inc, o.getSkew().y));
			o.update(deltaTime, this);
		}*/
		
		finalLayer.clear();
		finalLayer.addAll(movableLayer);
		finalLayer.addAll(staticLayer);
		finalLayer.addAll(grassPool.getPool());
		finalLayer.add(player);
		Collections.sort(finalLayer); //TODO: get a better sort method
										// excluir os objetos fora da tela. não precisa dar sort neles. só return.
		Hit h;
		/*for(int i=0; i<finalLayer.size(); i++) {
			for(int k=i+1;k<finalLayer.size();k++) {
				h = finalLayer.get(i).getBaseBox().intersectAABB(finalLayer.get(k).getBaseBox());
				if(h!=null) {
					//finalLayer.get(k).getBaseBox().x = h.pos.x;
					//finalLayer.get(k).getBaseBox().y = h.pos.y;
					finalLayer.get(k).move(h.delta.x, h.delta.y);
				}
				//if(finalLayer.get(i).checkBaseBoxCollisionAABB(finalLayer.get(k))) //TODO: dividir os espaços usando uma quadtree
					//finalLayer.get(i).resolveCollision(finalLayer.get(k));
			}
		}*/
		player.update(deltaTime, this);
		for(int i=0; i<finalLayer.size(); i++) { //TODO: verify only objects on screen
			
			//if(!isOnScreen(finalLayer.get(i)))
				//continue;
			if(finalLayer.get(i)==player)
				continue;
			
			finalLayer.get(i).update(deltaTime, this);
			
			Rectangle rec = new Rectangle(
					player.getPreviousPosition().x, 
					player.getPreviousPosition().y + player.getSize().y - player.getBaseBox().height,
					player.getBaseBox().width,
					player.getBaseBox().height);
			

			h = finalLayer.get(i).getBaseBox().sweepIntersectsAABB(
					rec,
					new Vec2(player.getPosition().x - player.getPreviousPosition().x,
							player.getPosition().y - player.getPreviousPosition().y));
			
			if(h!=null) {
				//player.move(h.delta.x, h.delta.y);
				boolean xAxisBlocked = false;
				boolean yAxisBlocked = false;
				int xMoving = player.getMovingDirectionX();
				int yMoving = player.getMovingDirectionY();
				
				//TODO:the problem is here

				player.moveDirectlyTo(h.pos.x - player.getBaseBox().getRadiusX(), 
						h.pos.y - player.getBaseBox().getRadiusY() - (player.getSize().y - player.getBaseBox().height));
				
				
				if( MathExt.openIntervalIntersect(
						player.getBaseBox().y, 
						player.getBaseBox().y+player.getBaseBox().height,
						finalLayer.get(i).getBaseBox().y,
						finalLayer.get(i).getBaseBox().y+finalLayer.get(i).getBaseBox().height))
					xAxisBlocked = true;


				if(MathExt.openIntervalIntersect(
						player.getBaseBox().x, 
						player.getBaseBox().x+player.getBaseBox().width,
						finalLayer.get(i).getBaseBox().x,
						finalLayer.get(i).getBaseBox().x+finalLayer.get(i).getBaseBox().width))
					yAxisBlocked = true;

				
				if(!xAxisBlocked && xMoving==player.RIGHT)
					player.move(player.getVelocity()*deltaTime, 0);
				if(!xAxisBlocked && xMoving==player.LEFT)
					player.move(-player.getVelocity()*deltaTime, 0);
				if(!yAxisBlocked && yMoving==player.TOP)
					player.move(0, -player.getVelocity()*deltaTime);
				if(!yAxisBlocked && yMoving==player.BOTTOM)
					player.move(0, player.getVelocity()*deltaTime);
			}
		}
		
		camera.update(deltaTime); //TODO: why should it be after all obj update?
		screenView.x = camera.getX();
		screenView.y = camera.getY();
		
		int coordXMap = Math.abs((int) (camera.getX() - player.getBaseBox().getCenterX())); // TODO: it'll get an error when the object is outsied the camera Width and height view
		int coordYMap = Math.abs((int) (camera.getY() - player.getBaseBox().getCenterY()));

		if(noiseRGB[(int) ((float)coordXMap/noiseDivisor)][(int) ((float)coordYMap/noiseDivisor)]==TURKISH) {
			player.setTexture("ranger_swimming");
			player.setVelocity(1200);
		}else {
			player.setTexture(null);
			player.setVelocity(1200);
		}
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public ArrayList<GameObject> getFinalLayer() {
		return finalLayer;
	}
}
