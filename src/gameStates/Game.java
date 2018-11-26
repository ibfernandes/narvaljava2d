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

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL32;


import static org.lwjgl.opengl.GL30.*;

import editor.Editor;
import engine.audio.Audio;
import engine.controllers.AIController;
import engine.controllers.DeerController;
import engine.controllers.PlayerController;
import engine.controllers.StaticNPCController;
import engine.engine.Engine;
import engine.engine.GameState;
import engine.engine.PhysicsEngine;
import engine.entity.BodySystem;
import engine.entity.ControllerSystem;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.MoveSystem;
import engine.entity.RenderSystem;
import engine.entity.SightSystem;
import engine.entity.SystemManager;
import engine.entity.TextSystem;
import engine.entity.component.BodyComponent;
import engine.entity.component.ControllerComponent;
import engine.entity.component.HealthComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.PositionComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.entity.component.TextComponent;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.input.KeyboardControl;
import engine.input.MouseControl;
import engine.logic.AnimationStateManager;
import engine.logic.Camera;
import engine.logic.Chunk;
import engine.logic.ChunkMap;
import engine.logic.GameObject;
import engine.logic.HorizontalPool;
import engine.logic.Timer;
import engine.noise.FastNoise;
import engine.physics.Hit;
import engine.renderer.CubeRenderer;
import engine.renderer.GrassRenderer;
import engine.renderer.ShadowRenderer;
import engine.renderer.TextureRenderer;
import engine.renderer.TextureBatchRenderer;
import engine.ui.Font;
import engine.ui.UIObject;
import engine.utilities.BufferUtilities;
import engine.utilities.Color;
import engine.utilities.MathExt;
import engine.utilities.QuadTree;
import engine.utilities.ResourceManager;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import javafx.scene.image.PixelWriter;
import net.jafama.FastMath;

public class Game extends GameState{
	
	//Layers
	private ArrayList<Entity> movableLayer;
	private ArrayList<Entity> staticLayer;
	private ArrayList<Entity> finalLayer; //composes all other layers into one, so it can be properly sorted and drawn
	private ArrayList<UIObject> UILayer;
	private QuadTree quadTree; // Contains all objects currently on screen
	private Camera camera;
	private Rectangle screenView;
	private Entity player;
	
	//Shadow Map
	private int shadowFBO;
	private int shadowLayerTexture;

	private Timer timer = new Timer();
	private Timer timerWetSand = new Timer();
	private Random random = new Random();
	
	//Map
	private ChunkMap chunkMap;
	private int seed = 12345;

	private int rowSize = (int) (Math.ceil((double)Engine.getSelf().getWindow().getWidth()/(double)ChunkMap.CHUNK_WIDTH)) +2; //chunks along y axis
	private int columnSize = (int) (Math.ceil((double)Engine.getSelf().getWindow().getHeight()/(double)ChunkMap.CHUNK_HEIGHT)) +2; //chunks along x axis
	private Chunk chunksOnScreen[][] = new Chunk[rowSize][columnSize];
	private boolean chunkHasLoaded[][] = new boolean[rowSize][columnSize];
	private Texture texturenOnScreen[][] = new Texture[rowSize][columnSize];
	private int previousCameraGridX;
	private int previousCameraGridY; 
	private int currentCameraGridX;
	private int currentCameraGridY;
	
	//Graph used for pathfinding
	public static final int GRAPH_DIVISOR = 8;
	public int graphSizeX = Engine.getSelf().getWindow().getWidth()/GRAPH_DIVISOR;
	public int graphSizeY = Engine.getSelf().getWindow().getWidth()/GRAPH_DIVISOR;
	public boolean obstacleMap[][];
	
	//Others
	private Vec2 startPoint = new Vec2(48000, 48000);
	//private Vec2 startPoint = new Vec2(6000, 6000);
	
	//Entity System 
	private EntityManager em = new EntityManager();
	private SystemManager sm = new SystemManager();
	
	@Override
	public void init() {
		initShadowLayer();
		
		screenView = new Rectangle(0,0,Engine.getSelf().getWindow().getWidth(),Engine.getSelf().getWindow().getHeight());
		chunkMap = new ChunkMap(seed,em);
		
		//==================================
		//Loads all shaders
		//==================================
		ResourceManager.getSelf().loadShader("cube", 
							"shaders/cube.vert",
							"shaders/cube.frag",
							null);
		ResourceManager.getSelf().loadShader("texture", 
							"shaders/texture.vert",
							"shaders/texture.frag",
							null);
		ResourceManager.getSelf().loadShader("shadow", 
							"shaders/shadow.vert",
							"shaders/shadow.frag",
							null);
		ResourceManager.getSelf().loadShader("grass", 
							"shaders/grass.vert",
							"shaders/grass.frag",
							null);
		ResourceManager.getSelf().loadShader("texturev2", 
				"shaders/texturev2.vert",
				"shaders/texturev2.frag",
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
		ResourceManager.getSelf().loadTexture("e_button", 
				"sprites/e_button.png");
		ResourceManager.getSelf().loadTexture("yellow_bird", 
				"sprites/yellow_bird.png");
		ResourceManager.getSelf().loadTexture("black_bird", 
				"sprites/black_bird.png");
		ResourceManager.getSelf().loadTexture("red_bird", 
				"sprites/red_bird.png");
		ResourceManager.getSelf().loadTexture("orange_bird", 
				"sprites/orange_bird.png");
		ResourceManager.getSelf().loadTexture("blue_bird", 
				"sprites/blue_bird.png");
		ResourceManager.getSelf().loadTexture("tree2", 
				"sprites/tree2.png");
		ResourceManager.getSelf().loadTexture("deer", 
				"sprites/deer.png");
		
		//==================================
		//Loads all Audio
		//==================================
		ResourceManager.getSelf().loadAudio("ocean_waves","audio/ocean_waves.ogg" );
		//ResourceManager.getSelf().playAudio("ocean_waves", startPoint, 1000);
		
		//==================================
		//Loads all fonts
		//==================================
		ResourceManager.getSelf().loadFont("monospace","NOT IMPLEMENTED" );//TODO Implement font loading from file

		//==================================
		//Set all Uniforms
		//==================================
		Mat4 projection = new Mat4();
		projection = projection.ortho(0, Engine.getSelf().getWindow().getWidth(), Engine.getSelf().getWindow().getHeight(), 0, -1f, 1f);

		ResourceManager.getSelf().getShader("cube").use();
		ResourceManager.getSelf().getShader("cube").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setMat4("projection", projection);
		ResourceManager.getSelf().getShader("texture").setPointLight(0, new Vec3(startPoint.x, startPoint.y, 300), new Vec3(1,1,1), new Vec3(1,0,0), 1f, 0.001f, 0.000002f);
		ResourceManager.getSelf().getShader("texture").setFloat("dayTime", 1f);
		ResourceManager.getSelf().getShader("texture").setVec3("ambientColor", new Vec3(0,0,0));
		//DON'T FORGET TO ALSO CHANGE ON CAMERA
		ResourceManager.getSelf().getShader("texturev2").use();
		ResourceManager.getSelf().getShader("texturev2").setMat4("projection", projection);
		ResourceManager.getSelf().getShader("texturev2").setPointLight(0, new Vec3(startPoint.x, startPoint.y, 300), new Vec3(1,1,1), new Vec3(1,0,0), 1f, 0.001f, 0.000002f);
		ResourceManager.getSelf().getShader("texturev2").setFloat("dayTime", 1f);
		ResourceManager.getSelf().getShader("texturev2").setVec3("ambientColor", new Vec3(0,0,0));
		
		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("projection", projection);
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("projection", projection);
		
		//==================================
		//Start renderers
		//==================================
		TextureRenderer t = new TextureRenderer(ResourceManager.getSelf().getShader("texture"));
		TextureBatchRenderer t2 = new TextureBatchRenderer(ResourceManager.getSelf().getShader("texturev2"));
		CubeRenderer r = new CubeRenderer(ResourceManager.getSelf().getShader("cube"));
		ShadowRenderer s = new ShadowRenderer(ResourceManager.getSelf().getShader("shadow"));
		GrassRenderer g = new GrassRenderer(ResourceManager.getSelf().getShader("grass"));
		
		ResourceManager.getSelf().setRenderer("textureRenderer", t);
		ResourceManager.getSelf().setRenderer("cubeRenderer", r);
		ResourceManager.getSelf().setRenderer("shadowRenderer", s);
		ResourceManager.getSelf().setRenderer("grassRenderer", g);
		ResourceManager.getSelf().setRenderer("textureRendererv2", t2);
		ResourceManager.getSelf().setTextureRenderer(t);
		ResourceManager.getSelf().setCubeRenderer(r);
		ResourceManager.getSelf().setShadowRenderer(s);
		ResourceManager.getSelf().setGrassRenderer(g);
		
		//==================================
		//Instantiate Layers
		//==================================
		movableLayer = new ArrayList<>();
		staticLayer = new ArrayList<>();
		finalLayer = new ArrayList<>();
		UILayer = new ArrayList<>();

		//==================================
		//Create GameObjects
		//==================================
		//createClerics(1);
		//createBirds(30);
	
		//==================================
		//Creates player
		//==================================
		player = em.newEntity();
		em.setPlayerID(player.getID());
		player.setName("player");

		AnimationStateManager asm = new AnimationStateManager();
		
		Animation a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
		asm.addAnimation("idle_2", a);
		
		a = new Animation("rogue", 100);
		a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
		asm.addAnimation("walking", a);
		
		a = new Animation("rogue", 16);
		a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
		asm.addAnimation("attacking", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
		asm.addAnimation("dying", a);
		
		asm.changeStateTo("idle_1");
		
		Vec2 size= new Vec2(128,128);
		Rectangle baseBoxProportions = new Rectangle(0f,0.8f,1.0f,0.2f);
		
		RenderComponent rc = new RenderComponent(player.getID());
		rc.setSize(size);
		rc.setColor(new Vec4(1,1,1,1));
		rc.setAnimations(asm);
		rc.setBaseBox(baseBoxProportions);
		rc.setRenderer("textureRenderer");
		rc.setRenderPosition(startPoint);
		
		em.addComponentTo(player, rc);

		ControllerComponent cp = new ControllerComponent(player.getID());
		cp.controller = new PlayerController();
		em.addComponentTo(player, cp);
		
		MoveComponent mc = new MoveComponent(player.getID());
		mc.velocity = 600;
		em.addComponentTo(player, mc);
		
		PositionComponent pc = new PositionComponent(player.getID());
		pc.setPosition(startPoint);
		em.addComponentTo(player, pc);
		
		BodyComponent bc = new BodyComponent(player.getID());
		bc.setBaseBox(baseBoxProportions);
		bc.calculateBaseBox(startPoint, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
		em.addComponentTo(player, bc);
				
		//==================================
		//Camera
		//==================================
		camera = new Camera(em);
		camera.setFocusOn(player);
		camera.move(-startPoint.x, -startPoint.y);
		
		//==================================
		//Tests
		//==================================
		timerWetSand.setDegree(260);
		timer.setDuration(timer.SECOND*8);
		timerWetSand.setDuration(timer.SECOND*80);
		//ResourceManager.getSelf().playAudio("ocean_waves", player.getPosition(), 3000);

		Entity npc = em.newEntity();

		asm = new AnimationStateManager();
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
		asm.addAnimation("idle_2", a);
		
		a = new Animation("rogue", 100);
		a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
		asm.addAnimation("walking", a);
		
		a = new Animation("rogue", 16);
		a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
		asm.addAnimation("attacking", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
		asm.addAnimation("dying", a);
		
		asm.changeStateTo("idle_1");
		
		Vec2 pos = new Vec2(startPoint.x+300, startPoint.y);
		rc = new RenderComponent(npc.getID());
		rc.setSize(new Vec2(128,128));
		rc.setColor(new Vec4(1,1,1,1));
		rc.setAnimations(asm);
		rc.setRenderer("textureRenderer");
		rc.setDisabled(true);
		rc.setRenderPosition(pos);
		
		em.addComponentTo(npc, rc);

		cp = new ControllerComponent(npc.getID());
		cp.controller = new StaticNPCController();
		em.addComponentTo(npc, cp);
		
		pc = new PositionComponent(npc.getID());
		pc.setPosition(pos);
		em.addComponentTo(npc, pc);
		
		bc = new BodyComponent(npc.getID());
		bc.setBaseBox(new Rectangle(0f,0.8f,1.0f,0.2f));
		bc.calculateBaseBox(pos, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
		em.addComponentTo(npc, bc);
		
		
		//Following npc
		Entity followingNpc = em.newEntity();

		asm = new AnimationStateManager();
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,0), new Vec2(32,32));
		asm.addAnimation("idle_1", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,32), new Vec2(32,32));
		asm.addAnimation("idle_2", a);
		
		a = new Animation("rogue", 100);
		a.setFrames(10, new Vec2(0,64), new Vec2(32,32));
		asm.addAnimation("walking", a);
		
		a = new Animation("rogue", 16);
		a.setFrames(10, new Vec2(0,96), new Vec2(32,32));
		asm.addAnimation("attacking", a);
		
		a = new Animation("rogue", 150);
		a.setFrames(10, new Vec2(0,128), new Vec2(32,32));
		asm.addAnimation("dying", a);
		
		asm.changeStateTo("idle_1");
		
		pos = new Vec2(startPoint.x+200, startPoint.y-350);
		rc = new RenderComponent(followingNpc.getID());
		rc.setSize(new Vec2(128,128));
		rc.setColor(new Vec4(1,1,1,1));
		rc.setAnimations(asm);
		rc.setRenderer("textureRenderer");
		rc.setRenderPosition(pos);
		
		em.addComponentTo(followingNpc, rc);

		cp = new ControllerComponent(followingNpc.getID());
		cp.controller = new AIController();
		em.addComponentTo(followingNpc, cp);
		
		pc = new PositionComponent(followingNpc.getID());
		pc.setPosition(pos);
		em.addComponentTo(followingNpc, pc);
		
		mc = new MoveComponent(followingNpc.getID());
		mc.velocity = 200;
		em.addComponentTo(followingNpc, mc);
		
		SightComponent sc = new SightComponent(followingNpc.getID());
		sc.setViewSize(1400, 1000);
		em.addComponentTo(followingNpc, sc);
		
		bc = new BodyComponent(followingNpc.getID());
		bc.setBaseBox(new Rectangle(0f,0.8f,1.0f,0.2f));
		bc.calculateBaseBox(pos, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
		em.addComponentTo(followingNpc, bc);
		
		
		createBoxes(); 
		
		//==================================
		// Entity Systems
		//==================================
		
		MoveSystem ms = new MoveSystem(this);
		BodySystem bs = new BodySystem(this);
		ControllerSystem cs = new ControllerSystem(this);
		RenderSystem rs = new RenderSystem(this);
		TextSystem ts = new TextSystem(this);
		SightSystem ss = new SightSystem(this);
		
		sm.addSystem(ms);
		
		sm.addSystem(cs);
		sm.addSystem(rs);
		sm.addSystem(bs);
		sm.addSystem(ts);
		sm.addSystem(ss);
		
		previousCameraGridX =  -1; 
		previousCameraGridY =  -1; 
		currentCameraGridX = (int) (camera.getX()/ChunkMap.CHUNK_WIDTH); 
		currentCameraGridY = (int) (camera.getY()/ChunkMap.CHUNK_HEIGHT);
		initQuadTree(screenView);
	}
	
	private void initQuadTree(Rectangle screenView) {
		quadTree = new QuadTree(screenView, em);
		
		for(Entity e: em.getAllEntitiesWithComponent(BodyComponent.class)) {
			quadTree.insert(e);
			
		}
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
	
	public void createBoxes() {
		float basicOffset = 0;
		Vec2 size = new Vec2(12*5,15*5);
		Vec2 positions[] = {
				new Vec2(startPoint.x + basicOffset,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*-3,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*-2,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*-1,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*2,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*3,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*4,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*5,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*6,startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x*2,startPoint.y + size.y),
				new Vec2(startPoint.x + basicOffset + size.x*2,startPoint.y+ size.y*2),
				new Vec2(startPoint.x + basicOffset + size.x*2,startPoint.y+ size.y*3),
				new Vec2(startPoint.x + basicOffset + size.x*2,startPoint.y+ size.y*4)
				};

		for(int i=0; i< positions.length; i++) {
			Entity box = em.newEntity();
	
			AnimationStateManager asm = new AnimationStateManager();
			Animation a = new Animation("wooden_box", -1);
			a.setFrames(1, new Vec2(0,0), new Vec2(12,15));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");

			RenderComponent rc = new RenderComponent(box.getID());
			rc.setSize(size);
			rc.setColor(new Vec4(1,1,1,1));
			rc.setAnimations(asm);
			rc.setRenderer("textureRenderer");
			rc.setRenderPosition(positions[i]);
			rc.setBaseBox(new Rectangle(0f,0.6f,1.0f,0.4f));
			em.addComponentTo(box, rc);
	
			PositionComponent pc = new PositionComponent(box.getID());
			pc.setPosition(positions[i]);
			em.addComponentTo(box, pc);

			
			BodyComponent bc = new BodyComponent(box.getID());
			bc.setBaseBox(new Rectangle(0f,0.6f,1.0f,0.4f));
			bc.calculateBaseBox(positions[i], size);
			bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
			em.addComponentTo(box, bc);
		}
	}
	
	public void createBirds(int qtd) {
		for(int i =0; i<qtd; i++) {
			GameObject o = new GameObject();
			o.setGroup("birds");
			int var = random.nextInt(10);
			o.setSize(new Vec2(160 -var,160- var));
			o.setVelocity(200);
			o.setColor(new Vec4(1,1,1,1));
			o.setBaseBox(new Vec2(0, 0));
			o.setPosition(new Vec2(startPoint.x+ i*300,startPoint.y));
			o.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
			
			AIController ai =  new AIController();
			//o.setController(ai);

			String bird = "";
			String options[] = {"blue_bird","black_bird", "red_bird", "yellow_bird", "orange_bird"};
			int dice = random.nextInt(options.length);
			
			AnimationStateManager asm = new AnimationStateManager();
			Animation a = new Animation(options[dice], -1);
			a.setFrames(1, new Vec2(0,0), new Vec2(9,9));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");
			
			o.setAnimations(asm);
			
			//movableLayer.add(o);
		}
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
			o.setPosition(new Vec2(startPoint.x + r.nextInt(300),startPoint.y + r.nextInt(300)));
			o.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
			
			AIController ai =  new AIController();
			o.setController(ai);
			
			AnimationStateManager asm = new AnimationStateManager();
			
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
			
			//movableLayer.add(o);
		}
	}
	
	public void generateCollisionGraph() {
		obstacleMap = new boolean[graphSizeX][graphSizeY];

		for(Entity e: quadTree.queryRange(screenView)) {
			BodyComponent bc = ((BodyComponent)(em.getFirstComponent(e, BodyComponent.class)));
			RenderComponent rc = ((RenderComponent)(em.getFirstComponent(e, RenderComponent.class)));
			
			if(rc==null)
				continue;
			
			Rectangle baseBox = bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize());
			
			int coordXMapS = (int) (-camera.getX() + baseBox.getX())/GRAPH_DIVISOR;
			int coordYMapS = (int) (-camera.getY() + baseBox.getY())/GRAPH_DIVISOR;
			int sizeX = (int) (baseBox.width/GRAPH_DIVISOR) + 1;
			int sizeY = (int) (baseBox.height/GRAPH_DIVISOR) +1;
			
			
			if(coordXMapS<0 || coordYMapS<0) //TODO: Should consider the INTERVAL, not just the start and fisnish poitn
				continue;
			
			for(int y=coordYMapS; y< coordYMapS+sizeY; y++)
				for(int x=coordXMapS; x<coordXMapS + sizeX; x++)
					if(x<obstacleMap.length && y<obstacleMap[0].length)
						obstacleMap[x][y] = true; 	// true onde ta bloqueado
		}
	}
	
	public boolean[][] getPointOfViewCollisionGraph(Rectangle baseBox) {
		boolean temp[][] =  new boolean[graphSizeX][graphSizeY];
		temp = obstacleMap.clone();

		int coordXMapS = (int) (- camera.getX() + baseBox.getX())/GRAPH_DIVISOR;
		int coordYMapS = (int) (- camera.getY() + baseBox.getY())/GRAPH_DIVISOR;
		int sizeX = (int) (baseBox.width/GRAPH_DIVISOR) + 1;
		int sizeY = (int) (baseBox.height/GRAPH_DIVISOR) +1;
		
		if(coordXMapS<0 || coordYMapS<0) //TODO: Should consider the INTERVAL, not just the start and fisnish poitn
			return temp;
		
		for(int y=coordYMapS; y< coordYMapS+sizeY; y++)
			for(int x=coordXMapS; x<coordXMapS + sizeX; x++)
				if(x<temp.length && y<temp[0].length)
					temp[x][y] = false;
		
		return temp;
	}

	public boolean isOnScreen(GameObject o) {
		return (screenView.intersects(o.getBoundingBox()));
	}

	public void generateChunks() { 
		currentCameraGridX = (int) (camera.getX()/ChunkMap.CHUNK_WIDTH) ; 
		currentCameraGridY = (int) (camera.getY()/ChunkMap.CHUNK_HEIGHT) ;
		
		if(currentCameraGridX!=previousCameraGridX || currentCameraGridY!=previousCameraGridY) {
			previousCameraGridX = currentCameraGridX;
			previousCameraGridY = currentCameraGridY;
			
			chunkHasLoaded = new boolean[rowSize][columnSize];
			
			for(int y=0; y<chunksOnScreen[0].length; y++) {
				for(int x=0; x<chunksOnScreen.length; x++) {
					chunksOnScreen[x][y] = chunkMap.get(currentCameraGridX + x -1, currentCameraGridY + y -1);
				}
			}
		}
		
		for(int y=0; y<chunksOnScreen[0].length; y++) 
			for(int x=0; x<chunksOnScreen.length; x++) 
				if(chunkMap.chunkExists(currentCameraGridX + x -1, currentCameraGridY + y -1) && !chunkHasLoaded[x][y]) {
					chunkHasLoaded[x][y] = true;
					chunksOnScreen[x][y] = chunkMap.get(currentCameraGridX + x -1, currentCameraGridY + y -1);
					texturenOnScreen[x][y] = new Texture(chunksOnScreen[x][y].getTerrain());
				}
	}
	
	public void renderShadow() {
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);// makes OpenGL reading data from your "framebuffer"

		glClearColor(1,1,1,0);
		glClear(GL11.GL_COLOR_BUFFER_BIT);
		
		//TODO: impl. entity to shadow's system
		//for(Entity e: quadTree.queryRange(screenView)) 
			//ResourceManager.getSelf().getShadowRenderer().render(o);
	}
	
	Chunk showCase = new Chunk(0,0,ChunkMap.CHUNK_WIDTH, ChunkMap.CHUNK_HEIGHT, ChunkMap.MAP_WIDTH, ChunkMap.MAP_HEIGHT,em);
	Texture terrainTexture = new Texture(ChunkMap.CHUNK_WIDTH/Chunk.NOISE_DIVISOR,ChunkMap.CHUNK_HEIGHT/Chunk.NOISE_DIVISOR);
	
	@Override
	public void render() {
		
		//renderShadow();
		//glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClearColor(1,1,1,1);
		
		glClear(GL_COLOR_BUFFER_BIT);
		
		//TODO: Versão dinâmica só pra poder gerar o show case
		/*showCase.setX((int) (camera.getX() / ChunkMap.CHUNK_WIDTH) );
		showCase.setY((int) (camera.getY() / ChunkMap.CHUNK_HEIGHT) );
		showCase.setWaterDx(FastMath.sin(Math.toRadians(timer.getDegree())));
		showCase.setWetSandDx(FastMath.sin(Math.toRadians(timerWetSand.getDegree())));
	
		showCase.generateTerrain();
				ResourceManager.getSelf().getTextureRenderer().render( new Texture(showCase.getTerrain()).getId(),
						new Vec2(showCase.getX()*ChunkMap.CHUNK_WIDTH, showCase.getY()*ChunkMap.CHUNK_HEIGHT),
						new Vec2(ChunkMap.CHUNK_WIDTH, ChunkMap.CHUNK_HEIGHT), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,0), new Vec2(0,0));*/
		
		
		 //SHOW CASE CHUNK SYSTEM
		  //NAO ESQUECER DE MUDAR AS COORDS no Chunk.java*
		/*  showCase.setX((int) camera.getX());
		showCase.setY((int) camera.getY());
		showCase.setWaterDx(FastMath.sin(Math.toRadians(timer.getDegree())));
		showCase.setWetSandDx(FastMath.sin(Math.toRadians(timerWetSand.getDegree())));
		
		showCase.generateNoise();
		showCase.generateTerrain();
		
		terrainTexture.createAndSendBuffer(showCase.getTerrain());
	
		ResourceManager.getSelf().getTextureRenderer().render( terrainTexture.getId(),
				new Vec2(camera.getX(), camera.getY()),
				new Vec2(ChunkMap.CHUNK_WIDTH, ChunkMap.CHUNK_HEIGHT), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,0), new Vec2(0,0));*/
		
		
		generateChunks();
		for(int y=0; y<chunksOnScreen[0].length; y++) 
			for(int x=0; x<chunksOnScreen.length; x++) 
				if(texturenOnScreen[x][y]!=null && chunksOnScreen[x][y]!=null && chunksOnScreen[x][y].getBoundingBox().intersects(screenView)) {
					chunksOnScreen[x][y].setWaterDx(FastMath.sin(Math.toRadians(timer.getDegree())));
					chunksOnScreen[x][y].setWetSandDx(FastMath.sin(Math.toRadians(timerWetSand.getDegree())));
					
					chunksOnScreen[x][y].generateTerrain();
					
					texturenOnScreen[x][y].createAndSendBuffer(chunksOnScreen[x][y].getTerrain());
					
					ResourceManager.getSelf().getTextureRenderer().render(texturenOnScreen[x][y].getId(),
							new Vec2(chunksOnScreen[x][y].getX()*ChunkMap.CHUNK_WIDTH, chunksOnScreen[x][y].getY()*ChunkMap.CHUNK_HEIGHT),
							new Vec2(ChunkMap.CHUNK_WIDTH, ChunkMap.CHUNK_HEIGHT), 0, new Vec4(1,1,1,1), new Vec4(0,0,1,1), new Vec2(0,0), new Vec2(0,0));
				}
	//	ResourceManager.getSelf().getTextureRenderer().render(shadowLayerTexture, new Vec2(camera.getX(),camera.getY()),
		//		Engine.getSelf().getWindow().getSize(), 0, new Vec4(1,1,1,0.2), new Vec4(0,0,1,1), new Vec2(0,1), new Vec2(0,0));
		
		
	//	for(Entity e: finalLayer) {
		//	BodyComponent bc = (BodyComponent) em.getFirstComponent(e, BodyComponent.class);
		//	RenderComponent rc = (RenderComponent) em.getFirstComponent(e, RenderComponent.class);
			
			//if(!bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize()).intersects(screenView))
				//continue;
			
			//TODO: draw debug
			//o.renderDebug();
			/*if(o.getController()!=null)
				o.getController().renderDebug();
			
			if(o.getGroup()!=null && o.getGroup().equals("vegetation"))
				ResourceManager.getSelf().getGrassRenderer().render(o);
			else
				o.render();*/
		//}
		
		if(obstacleMap!=null)
			for(int y=0; y< obstacleMap[0].length; y++)
				for(int x=0; x< obstacleMap.length; x++) {
					
					int rx = (int) (camera.getX() + x*GRAPH_DIVISOR);
					int ry = (int) (camera.getY() + y*GRAPH_DIVISOR);
					
					//if(obstacleMap[x][y])
					//	ResourceManager.getSelf().getCubeRenderer().render(new Vec2(rx, ry), new Vec2(18,18), 0, new Vec3(1,0,1));
					//if(!obstacleMap[x][y])
					//	ResourceManager.getSelf().getCubeRenderer().render(new Vec2(rx, ry), new Vec2(18,18), 0, new Vec3(0,1,0));
				}
	    
		sm.render();
	
		
	}
	@Override
	public void variableUpdate(float alpha) {
		chunkMap.update();
		
		//for(Entity o: quadTree.queryRange(screenView))  //TODO: verify only objects on screen
		//	o.variableUpdate(alpha);
		
		sm.variableUpdate(alpha);
		camera.variableUpdate(alpha);
	}
	
	@Override
	public void update(float deltaTime) {
		PositionComponent ppc= em.getFirstComponent(player, PositionComponent.class);
		alListener3f(AL_POSITION, ppc.getPosition().x, ppc.getPosition().y,0);
		
		timer.update();
		timerWetSand.update();
		
		float sin = (float) Math.sin(Math.toRadians(timer.getDegree()))*1f;
		sin = (sin<0) ? sin*-1: sin;
		
		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setFloat("dx", sin);
		
		
		ResourceManager.getSelf().getShader("texture").use();
		sin = (float) Math.sin(Math.toRadians(timerWetSand.getDegree()))*1f;
		ResourceManager.getSelf().getShader("texture").setFloat("dayTime", 1*1);
		//ResourceManager.getSelf().getShader("texture").setVec3("ambientColor", new Vec3(0.42f*1/sin,0.06f*1/sin,0.5176f*1/sin));
		
		//generateChunks();
		
		finalLayer.clear();
		
		
		
		
		
		//finalLayer.addAll(em.getAllEntities());
		//finalLayer.addAll(staticLayer);
		//finalLayer.add(player);
		//Collections.sort(finalLayer); //TODO: get a better sort method
										// excluir os objetos fora da tela. não precisa dar sort neles. só return.
		
		//for(GameObject o: quadTree.queryRange(screenView))  //TODO: verify only objects on screen
			//o.update(deltaTime, this);
		
		sm.update(deltaTime);
		
		camera.update(deltaTime); //TODO: why should it be after all obj update?
		
		
		screenView.x = camera.getX();
		screenView.y = camera.getY();
		initQuadTree(screenView);
		generateCollisionGraph();
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}


	public ArrayList<UIObject> getUiLayer() {
		return UILayer;
	}

	public ArrayList<Entity> getStaticLayer() {
		return staticLayer;
	}
	
	/**
	 * Do not modify the finalLayer. It's always mutating in order to the system's work
	 * @return
	 */
	public ArrayList<Entity> getFinalLayer() {
		return finalLayer;
	}

	public QuadTree getQuadTree() {
		return quadTree;
	}

	public void removeObject(GameObject g) {
		if(g.getBody()!=null)
			PhysicsEngine.getSelf().getWorld().destroyBody(g.getBody());
		movableLayer.remove(g);
	}

	public EntityManager getEm() {
		return em;
	}

	public Rectangle getScreenView() {
		return screenView;
	}

}
