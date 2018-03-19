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
import engine.audio.Audio;
import engine.controllers.AIController;
import engine.controllers.PlayerController;
import engine.engine.GameState;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.logic.Camera;
import engine.logic.GameObject;
import engine.logic.HorizontalPool;
import engine.logic.Timer;
import engine.noise.FastNoise;
import engine.utilities.BufferUtilities;
import engine.utilities.ResourceManager;
import gameObjects.Player;
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

public class Game extends GameState{
	
	//Layers
	private ArrayList<GameObject> movableLayer;
	private ArrayList<GameObject> staticLayer;
	private ArrayList<GameObject> finalLayer;
	private Camera camera;
	
	//Shadow Map
	int shadowFBO;
	int shadowLayerTexture;
	
	private void initShadowLayer() {
		shadowFBO = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);
		
		shadowLayerTexture = GL11.glGenTextures();
		
		GL11.glBindTexture(GL_TEXTURE_2D, shadowLayerTexture);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, 1280, 720, 0, GL11.GL_RGBA, GL_UNSIGNED_BYTE, 0); //TODO: get window size
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
	
	@Override
	public void init() {
		initShadowLayer();
		
		//==================================
		//Loads all shaders
		//==================================
		ResourceManager.getSelf().loadShader("sprite", 
							"shaders/sprite.vs", 
							"shaders/sprite.fs", 
							null);
		ResourceManager.getSelf().loadShader("cube", 
							"shaders/cube.vs",
							"shaders/cube.fs",
							null);
		ResourceManager.getSelf().loadShader("animation", 
							"shaders/animation.vs",
							"shaders/animation.fs",
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

		Mat4 projection = new Mat4();
		projection = projection.ortho(0, 1280f, 720f, 0, -1f, 1f); //TODO: should get width and height from window
		
		camera = new Camera();
		
		//Set uniforms
		//ResourceManager.getSelf().getShader("sprite").use().setInteger("image", 0);
		//ResourceManager.getSelf().getShader("sprite").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("cube").use();
		ResourceManager.getSelf().getShader("cube").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("animation").use();
		ResourceManager.getSelf().getShader("animation").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("projection", projection);
		
		//==================================
		//Start renderers
		//==================================
		TextureRenderer t = new TextureRenderer(ResourceManager.getSelf().getShader("animation"));
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
		
		//==================================
		//Creates other GameObjects
		//==================================
		/*GameObject shadow = new GameObject(	new Vec2(200,200), 		//Pos
				new Vec2(128,128), 		//size
				450, 					//Velocity
				new Vec3(0,0,0),		//Color
				0, 						//Rotation
				true,					//isSolid
				null
				);
		shadow.setOrientation(new Vec2(0,1));
		shadow.setSkew(new Vec2(0,0));
		originalSize = shadow.getSize();
		shadow.setAnimations(asm);*/
		
		
		
		GameObject grass = new GameObject(	new Vec2(0,0), 		//Pos
				new Vec2(1280,720), 		//size
				0, 					//Velocity
				new Vec4(1,1,1,1),		//Color
				0, 						//Rotation
				true,					//isSolid
				"grass",
				null
				);
		//movableLayer.add(grass);
		
		//==================================
		//Create enemies
		//==================================
		createClerics(8);
		
		//==================================
		//Create props
		//==================================
		//createGrass(25);
		//createWheat(6);
		createProps(13);
		
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
		//==================================
		//Creates player
		//==================================
		PlayerController playerController =  new PlayerController();
		
		GameObject player = new GameObject();
		player.setSize(new Vec2(128,128));
		player.setVelocity(1200);
		player.setColor(new Vec4(1,1,1,1));
		player.setController(playerController);
		player.setOrientation(new Vec2(0,0));
		player.setBaseBox(new Vec2(128, 20));
		player.setSkew(new Vec2(0,0));
		player.setPosition(new Vec2(7550,7320));
		
		ASM asm = new ASM();
		
		Animation a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
		asm.addAnimation("idle_2", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
		asm.addAnimation("walking", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
		asm.addAnimation("attacking", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
		asm.addAnimation("dying", a);
		
		asm.changeStateTo("idle_1");
		
		player.setAnimations(asm);
		movableLayer.add(player);
		
	
		
		camera.setFocusOn(player);
		camera.move(-7000, -7000);
		//==================================
		//Loads all Audio
		//==================================
		//Audio audio = new Audio();
		
		
		//==================================
		//Tests
		//==================================
		
		timerWetSand.setDegree(260);
		
	}
	
	FastNoise n = new FastNoise();
	int seed = new Random().nextInt(2000);

	public double noise(float x, float y) { //TODO: I could make it parallel and so increase performance[?]
		
		double noise = 0;
		n.SetSeed(seed);

		n.SetFrequency(0.0005f); //quanto menor, maior as "ilhas"
		noise +=  n.GetPerlin(x, y); //one octave
		
		n.SetFrequency(0.005f);
		noise += .1 * n.GetPerlin(x, y); // second octave
		
		return noise;
	}

	
	int divisor = 6;
	int perlinWidth = 1280/divisor;
	int perlinHeight = 720/divisor;
	
	double noiseArr[][] = new double[perlinWidth][perlinHeight];
	double whiteNoise[][] = new double[perlinWidth][perlinHeight];
	
	double fractalNoise[][] = new double[perlinWidth][perlinHeight];
	int 	rgb[][] = new int[perlinWidth][perlinHeight];
	
	HorizontalPool grassPool = new HorizontalPool(600);
	
	Texture terrain;
	double a = 0.15;
	double b =0.9;
	double c = 2;
	double d = 0;
	Timer timerTest = new Timer();
	Timer timerWetSand = new Timer();
	
	int map_width = 12000 ;
	int map_height = 12000 ;
	int esmeralda = (255<<24) | (56<<16) | (204<<8) | (113);
	int darkedEsmeralda = (255<<24) | (52<<16) | (200<<8) | (109);
	
	public void generateTerrain() { 
		timerTest.setDuration(Timer.SECOND*8);
		timerWetSand.setDuration(Timer.SECOND*8);
		
		for(int y=0; y<perlinHeight; y++) {
			for(int x=0; x<perlinWidth;x++) {
				int coordX = ((int)camera.getX()*-1)+x*divisor;
				int coordY =  ((int)camera.getY()*-1)+y*divisor;
				
				d = 2*Math.max(Math.abs((float)coordX/map_width - (float)12000/map_width), Math.abs((float)coordY/map_height - (float)12000/map_height)); //as the distance must be normlized,
				// i simply normalize the data before calculating the distance

				noiseArr[x][y] = noise(coordX/3,coordY);
				whiteNoise[x][y] = n.GetWhiteNoise(coordX, coordY); //TODO: it's generating diff noise everytime i move?!
				
				noiseArr[x][y] = noiseArr[x][y] + a - b*Math.pow(d, c);

				fractalNoise[x][y] = n.GetPerlinFractal(coordX/4, coordY*2);
			}
		}
		
		double dx = Math.sin(Math.toRadians(timerTest.getDegree()));
		double dxWet = Math.sin(Math.toRadians(timerWetSand.getDegree()));
		boolean syncWave = false;
		
		
		
		
		for(int y=0; y<perlinHeight; y++) {
			for(int x=0; x<perlinWidth;x++) {

				
				if(dxWet>dx)
					syncWave = false;
				else
					syncWave = true;
				
				
				if(noiseArr[x][y]>-.1 ) {	//land
					rgb[x][y] = esmeralda; //esmeralda
					if(fractalNoise[x][y]>0.2) {
						rgb[x][y] = darkedEsmeralda; //
					}
					
					
				
				}
				
				if(noiseArr[x][y]<=-.1) {	//sand
					rgb[x][y] =  (255<<24) | (244<<16) | (234<<8) | (187); //ARGB
					if(whiteNoise[x][y]>0)
						rgb[x][y] =  (255<<24) | (234<<16) | (224<<8) | (167); //ARGB
				}
				
				if(noiseArr[x][y]<-.230 + dxWet*.016)	//wet sand
					rgb[x][y] = (255<<24) | (224<<16) | (214<<8) | (167); //ARGB
				
				if(noiseArr[x][y]<-.230 + dx*.016)	//espuma
					rgb[x][y] = (255<<24) | (255<<16) | (255<<8) | (255); //ARGB
				
				if(noiseArr[x][y]<-.244 + dx*.016)	//espuma back
					rgb[x][y] = (255<<24) | (22<<16) | (160<<8) | (133); //green sea
				
				if(noiseArr[x][y]<=-.266 + dx*.016)  //water
					rgb[x][y] = 	(255<<24) | (26<<16) | (188<<8) | (156); //turquesa
	
				
				
				//valores crescem para baixo
			}
		}
		
		for(int y=0; y<720-3; y++) {
			for(int x=0; x<1280-3; x++) {
				int convertedX = x/divisor;
				int convertedY = y/divisor;
				
				if(whiteNoise[convertedX][convertedY]>0.9999 && (rgb[convertedX][convertedY] == esmeralda || rgb[convertedX][convertedY] == darkedEsmeralda)) {
					GameObject o = new GameObject(); //TODO: Should optimize this so i don't need to create an object every time.
					o.setSize(new Vec2(256,256));
					o.setVelocity(0);
					o.setColor(new Vec4(0.5,1,0.9,1));
					o.setOrientation(new Vec2(0,0));
					o.setBaseBox(new Vec2(16, 16));
					o.setSkew(new Vec2(0,0));
					o.setPosition(new Vec2(x + (camera.getX()*-1),y + (camera.getY()*-1)));
					ASM asm = new ASM(); //TODO: setTexutre not working?!
					
					Animation a = new Animation("wheat", -1);
					a.setFrames(1, new Vec2(0,0), new Vec2(32,32));
					asm.addAnimation("idle_1", a);
					asm.changeStateTo("idle_1");
					o.setAnimations(asm);
					
					grassPool.add(o, whiteNoise[convertedX][convertedY]);
				}
			}
		}
		
		terrain = new Texture(rgb);
		
	}
	
	public void createClerics(int qtd) {
		Random r = new Random();
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setSize(new Vec2(128,128));
			o.setVelocity(200);
			o.setColor(new Vec4(1,1,1,1));
			o.setRotation(0);
			o.setSkew(new Vec2(0,0));
			o.setOrientation(new Vec2(0,0));
			o.setBaseBox(new Vec2(128, 20));
			o.setPosition(new Vec2(7900+r.nextInt(500),7900+r.nextInt(500)));
			
			
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

	@Override
	public void render() {
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);// makes OpenGL reading data from your "framebuffer"

		glClearColor(1,1,1,0);
		glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		
		for(GameObject o: movableLayer) {
			ResourceManager.getSelf().getShadowRenderer().render(o);
		}
		
		for(GameObject o: staticLayer) {
			ResourceManager.getSelf().getShadowRenderer().render(o);
		}
		
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClearColor(1,1,1,1);
		glClear(GL_COLOR_BUFFER_BIT);
		
		/*ResourceManager.getSelf().getTextureRenderer().render(ResourceManager.getSelf().getTexture("terrain").getId(), new Vec2(0,0),
				new Vec2(16000,16000), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,1), new Vec2(0,0));*/
		
		ResourceManager.getSelf().getTextureRenderer().render(terrain.getId(),new Vec2(camera.getX()*-1,camera.getY()*-1),
				new Vec2(1280,720), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,0), new Vec2(0,0));
		
		ResourceManager.getSelf().getTextureRenderer().render(shadowLayerTexture, new Vec2(camera.getX()*-1,camera.getY()*-1),
				new Vec2(1280,720), 0, new Vec4(1,1,1,0.2), new Vec4(0,0,1,1), new Vec2(0,1), new Vec2(0,0));
		
		for(GameObject o: grassPool.getPool())
			ResourceManager.getSelf().getGrassRenderer().render(o);
		
		for(GameObject o: staticLayer) {
			o.render();
		}
		
		for(GameObject o: movableLayer) {
			//o.renderDebug();
			o.render();
		}
		
		
	}

	boolean shouldInc = true;
	@Override
	public void update(float deltaTime) {
		
		timerTest.update();
		timerWetSand.update();
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setFloat("dx", (float) Math.sin(Math.toRadians(timerTest.getDegree()))*4f);
		generateTerrain();
		
		for(GameObject o: movableLayer)
			o.update(deltaTime);
		
		for(GameObject o: grassPool.getPool())
			o.update(deltaTime);
		
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
			o.update(deltaTime);
		}
		
		
		for(int i=0; i<movableLayer.size(); i++) {
			for(int k=i+1;k<movableLayer.size();k++) {
				if(movableLayer.get(i).checkBaseBoxCollisionAABB(movableLayer.get(k))) //TODO: dividir os espa�os usando uma quadtree
					movableLayer.get(i).resolveCollision(movableLayer.get(k));
			}
		}
		
		Collections.sort(movableLayer); //TODO: get a better sort method
										// excluir os objetos fora da tela. n�o precisa dar sort neles. s� return.
		Collections.sort(staticLayer);
		
		
		camera.update(deltaTime);

	}
	

}
