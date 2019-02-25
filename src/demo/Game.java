package demo;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Random;

import org.jbox2d.dynamics.BodyType;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import static org.lwjgl.opengl.GL30.*;

import engine.controllers.AIController;
import engine.controllers.PlayerController;
import engine.controllers.StaticNPCController;
import engine.engine.AudioEngine;
import engine.engine.Engine;
import engine.engine.EngineSettings;
import engine.engine.GameState;
import engine.engine.PhysicsEngine;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.BodyComponent;
import engine.entity.component.ControllerComponent;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.entity.component.SightComponent;
import engine.entity.system.BodySystem;
import engine.entity.system.ControllerSystem;
import engine.entity.system.MoveSystem;
import engine.entity.system.RenderSystem;
import engine.entity.system.SystemManager;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.logic.AnimationStateManager;
import engine.logic.Camera;
import engine.logic.Chunk;
import engine.logic.ChunkManager;
import engine.particle.ParticleEngine;
import engine.particle.WalkingParticleEmitter;
import engine.renderer.CubeRenderer;
import engine.renderer.GrassRenderer;
import engine.renderer.ShadowRenderer;
import engine.renderer.TextureRenderer;
import engine.ui.Font;
import engine.renderer.TextureBatchRenderer;
import engine.utilities.ArraysExt;
import engine.utilities.BufferUtilities;
import engine.utilities.QuadTree;
import engine.utilities.ResourceManager;
import engine.utilities.Timer;
import glm.mat._4.Mat4;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;
import glm.vec._4.Vec4;
import net.jafama.FastMath;

public class Game extends GameState {

	// Layers
	private QuadTree quadTree;
	private Camera camera;
	private Rectangle screenView;
	private Entity player;

	// Shadow Map
	private int shadowFBO;
	private int shadowLayerTexture;

	private Timer timer;
	private Timer seconds = new Timer(1000);

	// Map
	private ChunkManager chunkMap;

	private int rowSize = (int) (Math
			.ceil((double) Engine.getSelf().getWindow().getWidth() / (double) ChunkManager.CHUNK_WIDTH)) + 2; // chunks
																												// along
																												// y
																												// axis
	private int columnSize = (int) (Math
			.ceil((double) Engine.getSelf().getWindow().getHeight() / (double) ChunkManager.CHUNK_HEIGHT)) + 2; // chunks
																												// along
																												// x
																												// axis
	private Chunk chunksOnScreen[][] = new Chunk[rowSize][columnSize];
	private Texture texturenOnScreen[][] = new Texture[rowSize][columnSize];
	private int previousCameraGridX;
	private int previousCameraGridY;
	private int currentCameraGridX;
	private int currentCameraGridY;
	private FloatBuffer chunkTextureFloatBuffer;

	// Graph used for pathfinding
	public static final int GRAPH_DIVISOR = 8;
	public int graphSizeX = Engine.getSelf().getWindow().getWidth() / GRAPH_DIVISOR;
	public int graphSizeY = Engine.getSelf().getWindow().getWidth() / GRAPH_DIVISOR;
	public boolean obstacleMap[][];

	// Others
	private Vec2 startPoint = new Vec2(48000 + 500, 48000 - 2000);

	// Entity System
	private EntityManager em = new EntityManager();
	private SystemManager sm = new SystemManager();

	private static Game self;
	

	private Game() {
	};

	public static Game getSelf() {
		return (self == null) ? self = new Game() : self;
	}

	@Override
	public void init() {
		initShadowLayer();

		screenView = new Rectangle(0, 0, Engine.getSelf().getWindow().getWidth(),
				Engine.getSelf().getWindow().getHeight());
		chunkMap = new ChunkManager(EngineSettings.getSelf().getMapSeed());
		chunkTextureFloatBuffer = BufferUtilities.createFloatBuffer(ChunkManager.CHUNK_WIDTH*ChunkManager.CHUNK_HEIGHT);

		// ==================================
		// Loads all shaders
		// ==================================
		ResourceManager.getSelf().loadShader("cube", "shaders/cube.vert", "shaders/cube.frag", null);
		ResourceManager.getSelf().loadShader("texture", "shaders/texture.vert", "shaders/texture.frag", null);
		ResourceManager.getSelf().loadShader("shadow", "shaders/shadow.vert", "shaders/shadow.frag", null);
		ResourceManager.getSelf().loadShader("grass", "shaders/grass.vert", "shaders/grass.frag", null);
		ResourceManager.getSelf().loadShader("texturev2", "shaders/texturev2.vert", "shaders/texturev2.frag", null);

		// ==================================
		// Loads all textures
		// ==================================
		ResourceManager.getSelf().loadTexture("main_char", "sprites/main_char.png");
		ResourceManager.getSelf().loadTexture("alt_char", "sprites/alt_char.png");
		ResourceManager.getSelf().loadTexture("e_button", "sprites/e_button.png");
		ResourceManager.getSelf().loadTexture("terrain_atlas", "sprites/terrain_atlas.png");
		ResourceManager.getSelf().loadTexture("terrain_atlas_normal", "sprites/terrain_atlas.png");
		
		// ==================================
		// Loads all spriteFrames
		// ==================================
		ResourceManager.getSelf().setSpriteFrame("red_mushroom", Animation.generateFrames("terrain_atlas", 1, 0, 0, 4, 4));
		ResourceManager.getSelf().setSpriteFrame("orange_mushroom", Animation.generateFrames("terrain_atlas", 1, 4, 0, 4, 4));
		ResourceManager.getSelf().setSpriteFrame("grass", Animation.generateFrames("terrain_atlas", 1, 85, 0, 7, 8));
		ResourceManager.getSelf().setSpriteFrame("main_char_idle", Animation.generateFrames("terrain_atlas", 3, 0, 60, 20, 40));
		ResourceManager.getSelf().setSpriteFrame("main_char_walking", Animation.generateFrames("terrain_atlas", 8, 0, 100, 20, 40));

		// ==================================
		// Loads all Audio
		// ==================================
		ResourceManager.getSelf().loadAudio("ocean_waves", "audio/ocean_waves.ogg");
		ResourceManager.getSelf().loadAudio("foot_steps_grass", "audio/foot_steps_grass.ogg");

		// ==================================
		// Loads all fonts
		// ==================================
		ResourceManager.getSelf().loadFont("sourcesanspro", "fonts/SourceSansPro.ttf");

		// ==================================
		// Set all Uniforms
		// ==================================
		Mat4 projection = new Mat4();
		projection = projection.ortho(0, Engine.getSelf().getWindow().getWidth(),
				Engine.getSelf().getWindow().getHeight(), 0, -1f, 1f);

		ResourceManager.getSelf().getShader("cube").use();
		FloatBuffer projectionBuffer = BufferUtilities.createFloatBuffer(4*4);
		projectionBuffer = BufferUtilities.fillFloatBuffer(projectionBuffer, projection);
		
		ResourceManager.getSelf().getShader("cube").setMat4("projection", projectionBuffer);

		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setMat4("projection", projectionBuffer);
		ResourceManager.getSelf().getShader("texture").setPointLight(0,
				new Vec3(startPoint.x - 3000, startPoint.y, 300), new Vec3(1, 1, 1), new Vec3(1, 0, 0), 1f, 0.001f,
				0.000002f);

		ResourceManager.getSelf().getShader("texture").setFloat("dayTime", 1f);
		ResourceManager.getSelf().getShader("texture").setVec3("ambientColor", new Vec3(0, 0, 0));

		ResourceManager.getSelf().getShader("texturev2").use();
		ResourceManager.getSelf().getShader("texturev2").setMat4("projection", projectionBuffer);
		ResourceManager.getSelf().getShader("texturev2").setPointLight(0, new Vec3(startPoint.x, startPoint.y, 300),
				new Vec3(1, 1, 1), new Vec3(1, 0, 0), 1f, 0.001f, 0.000002f);
		ResourceManager.getSelf().getShader("texturev2").setFloat("dayTime", 1f);
		ResourceManager.getSelf().getShader("texturev2").setVec3("ambientColor", new Vec3(0, 0, 0));

		ResourceManager.getSelf().getShader("shadow").use();
		ResourceManager.getSelf().getShader("shadow").setMat4("projection", projectionBuffer);

		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setMat4("projection", projectionBuffer);

		// ==================================
		// Start renderers
		// ==================================
		TextureRenderer t = new TextureRenderer(ResourceManager.getSelf().getShader("texture"));
		TextureBatchRenderer t2 = new TextureBatchRenderer(ResourceManager.getSelf().getShader("texturev2"));
		CubeRenderer r = new CubeRenderer(ResourceManager.getSelf().getShader("cube"));
		ShadowRenderer s = new ShadowRenderer(ResourceManager.getSelf().getShader("shadow"));
		GrassRenderer g = new GrassRenderer(ResourceManager.getSelf().getShader("grass"));

		ResourceManager.getSelf().setRenderer("textureRenderer", t);
		ResourceManager.getSelf().setRenderer("cubeRenderer", r);
		ResourceManager.getSelf().setRenderer("shadowRenderer", s);
		ResourceManager.getSelf().setRenderer("grassRenderer", g);
		ResourceManager.getSelf().setRenderer("textureBatchRenderer", t2);

		// ==================================
		// Creates player
		// ==================================
		float playerSpeed = 600;
		player = em.newEntity();
		player.setName("player");

		AnimationStateManager asm = new AnimationStateManager();

		Animation a = new Animation("terrain_atlas", 1000);
		a.setFrames(ResourceManager.getSelf().getSpriteFrame("main_char_idle"));
		asm.addAnimation("idle_1", a);

		a = new Animation("terrain_atlas", 50);
		a.setFrames(ResourceManager.getSelf().getSpriteFrame("main_char_walking"));
		asm.addAnimation("walking", a);

		asm.changeStateTo("idle_1");

		Vec2 size = new Vec2(80, 160);
		Rectangle baseBoxProportions = new Rectangle(0f, 0.8f, 1.0f, 0.2f);

		RenderComponent rc = new RenderComponent(player.getID());
		rc.setSize(size);
		rc.setColor(new Vec4(1, 1, 1, 1));
		rc.setAnimations(asm);
		rc.setBaseBox(baseBoxProportions);
		rc.setRenderer("textureBatchRenderer");
		rc.setRenderPosition(startPoint);

		em.addComponentTo(player, rc);

		ControllerComponent cp = new ControllerComponent(player.getID());
		cp.setController(new PlayerController());
		em.addComponentTo(player, cp);

		MoveComponent mc = new MoveComponent(player.getID());
		mc.setVelocity(playerSpeed);
		em.addComponentTo(player, mc);

		BasicComponent pc = new BasicComponent(player.getID());
		pc.setSize(size);
		pc.setPosition(startPoint);
		em.addComponentTo(player, pc);

		BodyComponent bc = new BodyComponent(player.getID());
		bc.setBaseBox(baseBoxProportions);
		bc.calculateBaseBox(startPoint, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
		em.addComponentTo(player, bc);

		// ==================================
		// Camera
		// ==================================
		camera = new Camera(em);
		camera.setFocusOn(player);
		camera.move(-startPoint.x, -startPoint.y);

		// ==================================
		// Tests
		// ==================================
		timer = new Timer(8 * 1000);

		Entity npc = em.newEntity();

		asm = new AnimationStateManager();

		a = new Animation("alt_char", 1000);
		a.setFrames(3, new Vec2(0, 0), new Vec2(20, 40));
		asm.addAnimation("idle_1", a);

		a = new Animation("alt_char", 50);
		a.setFrames(8, new Vec2(0, 40), new Vec2(20, 40));
		asm.addAnimation("walking", a);

		asm.changeStateTo("idle_1");

		Vec2 pos = new Vec2(startPoint.x + 300, startPoint.y);
		rc = new RenderComponent(npc.getID());
		rc.setSize(new Vec2(128, 128));
		rc.setColor(new Vec4(1, 1, 1, 1));
		rc.setAnimations(asm);
		rc.setRenderer("textureRenderer");
		rc.setDisabled(true);
		rc.setRenderPosition(pos);

		em.addComponentTo(npc, rc);

		cp = new ControllerComponent(npc.getID());
		cp.setController(new StaticNPCController());
		em.addComponentTo(npc, cp);

		pc = new BasicComponent(npc.getID());
		pc.setPosition(pos);
		pc.setSize(size);
		em.addComponentTo(npc, pc);

		bc = new BodyComponent(npc.getID());
		bc.setBaseBox(new Rectangle(0f, 0.8f, 1.0f, 0.2f));
		bc.calculateBaseBox(pos, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
		em.addComponentTo(npc, bc);

		// Following npc
		/*Entity followingNpc = em.newEntity();
		followingNpc.setName("AI");

		asm = new AnimationStateManager();

		a = new Animation("alt_char", 1000);
		a.setFrames(3, new Vec2(0, 0), new Vec2(20, 40));
		asm.addAnimation("idle_1", a);

		a = new Animation("alt_char", 50);
		a.setFrames(8, new Vec2(0, 40), new Vec2(20, 40));
		asm.addAnimation("walking", a);

		asm.changeStateTo("idle_1");

		pos = new Vec2(startPoint.x + 200, startPoint.y - 350);
		
		rc = new RenderComponent(followingNpc.getID());
		rc.setSize(size);
		rc.setColor(new Vec4(1, 1, 1, 1));
		rc.setAnimations(asm);
		rc.setRenderer("textureRenderer");
		rc.setRenderPosition(pos);

		em.addComponentTo(followingNpc, rc);

		cp = new ControllerComponent(followingNpc.getID());
		cp.setController(new AIController());
		em.addComponentTo(followingNpc, cp);

		pc = new BasicComponent(followingNpc.getID());
		pc.setPosition(pos);
		pc.setSize(size);
		em.addComponentTo(followingNpc, pc);

		mc = new MoveComponent(followingNpc.getID());
		mc.setVelocity(playerSpeed * 0.2f);
		em.addComponentTo(followingNpc, mc);

		SightComponent sc = new SightComponent(followingNpc.getID());
		sc.setViewSize(2200, 2000);
		em.addComponentTo(followingNpc, sc);

		bc = new BodyComponent(followingNpc.getID());
		bc.setBaseBox(new Rectangle(0f, 0.8f, 1.0f, 0.2f));
		bc.calculateBaseBox(pos, size);
		bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.DYNAMIC);
		em.addComponentTo(followingNpc, bc);*/

		// ==================================
		// Entity Systems
		// ==================================

		MoveSystem ms = new MoveSystem(this);
		BodySystem bs = new BodySystem(this);
		ControllerSystem cs = new ControllerSystem(this);
		RenderSystem rs = new RenderSystem(this);

		sm.addSystem(ms);
		sm.addSystem(cs);
		sm.addSystem(rs);
		sm.addSystem(bs);

		previousCameraGridX = -1;
		previousCameraGridY = -1;
		currentCameraGridX = (int) (camera.getX() / ChunkManager.CHUNK_WIDTH);
		currentCameraGridY = (int) (camera.getY() / ChunkManager.CHUNK_HEIGHT);
		generateQuadTree();

		// ==================================
		// Particle Emitters
		// ==================================

		WalkingParticleEmitter emitter = new WalkingParticleEmitter();
		emitter.setAnchor(startPoint, (int) size.y);
		ParticleEngine.getSelf().addParticleEmitter(emitter);
		
		
	}

	private void generateQuadTree() {
		quadTree = new QuadTree(screenView);

		BasicComponent bc;
		for (Entity e : em.getAllEntitiesWithComponent(BasicComponent.class)) {
			bc = em.getFirstComponent(e, BasicComponent.class);

			if (bc.getBoundingBox().intersects(screenView))
				quadTree.insert(e);
		}
	}

	private void initShadowLayer() {
		shadowFBO = glGenFramebuffers();

		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);

		shadowLayerTexture = GL11.glGenTextures();

		GL11.glBindTexture(GL_TEXTURE_2D, shadowLayerTexture);

		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, Engine.getSelf().getWindow().getWidth(),
				Engine.getSelf().getWindow().getHeight(), 0, GL11.GL_RGBA, GL_UNSIGNED_BYTE, 0);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);

		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

		glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, shadowLayerTexture, 0);

		if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
			System.err.println("ERROR: Shadow layer FBO.");

		glBindFramebuffer(GL_FRAMEBUFFER, 0);
	}

	public void createBoxes() {
		float basicOffset = 0;
		Vec2 size = new Vec2(12 * 5, 15 * 5);
		Vec2 positions[] = { new Vec2(startPoint.x + basicOffset, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -3, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -2, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * -1, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 3, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 4, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 5, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 6, startPoint.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 2),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 3),
				new Vec2(startPoint.x + basicOffset + size.x * 2, startPoint.y + size.y * 4) };

		for (int i = 0; i < positions.length; i++) {
			Entity box = em.newEntity();

			AnimationStateManager asm = new AnimationStateManager();
			Animation a = new Animation("wooden_box", -1);
			a.setFrames(1, new Vec2(0, 0), new Vec2(12, 15));
			asm.addAnimation("idle_1", a);
			asm.changeStateTo("idle_1");

			RenderComponent rc = new RenderComponent(box.getID());
			rc.setSize(size);
			rc.setColor(new Vec4(1, 1, 1, 1));
			rc.setAnimations(asm);
			rc.setRenderer("textureRenderer");
			rc.setRenderPosition(positions[i]);
			rc.setBaseBox(new Rectangle(0f, 0.6f, 1.0f, 0.4f));
			em.addComponentTo(box, rc);

			BasicComponent pc = new BasicComponent(box.getID());
			pc.setSize(size);
			pc.setPosition(positions[i]);
			em.addComponentTo(box, pc);

			BodyComponent bc = new BodyComponent(box.getID());
			bc.setBaseBox(new Rectangle(0f, 0.6f, 1.0f, 0.4f));
			bc.calculateBaseBox(positions[i], size);
			bc.createBody(PhysicsEngine.getSelf().getWorld(), BodyType.KINEMATIC);
			em.addComponentTo(box, bc);
		}
	}

	public void generateCollisionGraph() {
		obstacleMap = new boolean[graphSizeX][graphSizeY];

		for (Entity e : quadTree.queryRange(screenView)) {
			BodyComponent bc = ((BodyComponent) (em.getFirstComponent(e, BodyComponent.class)));
			if (bc == null)
				continue;
			RenderComponent rc = ((RenderComponent) (em.getFirstComponent(e, RenderComponent.class)));

			if (rc == null)
				continue;

			Rectangle baseBox = bc.calculateBaseBox(rc.getRenderPosition(), rc.getSize());

			int coordXMapS = (int) (-camera.getX() + baseBox.getX()) / GRAPH_DIVISOR;
			int coordYMapS = (int) (-camera.getY() + baseBox.getY()) / GRAPH_DIVISOR;
			int sizeX = (int) (baseBox.width / GRAPH_DIVISOR) + 1;
			int sizeY = (int) (baseBox.height / GRAPH_DIVISOR) + 1;

			if (coordXMapS < 0 || coordYMapS < 0)
				continue;

			for (int y = coordYMapS; y < coordYMapS + sizeY; y++)
				for (int x = coordXMapS; x < coordXMapS + sizeX; x++)
					if (x < obstacleMap.length && y < obstacleMap[0].length)
						obstacleMap[x][y] = true;
		}
	}

	public boolean[][] getPointOfViewCollisionGraph(Rectangle baseBox) {
		boolean temp[][] = new boolean[graphSizeX][graphSizeY];
		temp = obstacleMap.clone();

		int coordXMapS = (int) (-camera.getX() + baseBox.getX()) / GRAPH_DIVISOR;
		int coordYMapS = (int) (-camera.getY() + baseBox.getY()) / GRAPH_DIVISOR;
		int sizeX = (int) (baseBox.width / GRAPH_DIVISOR) + 1;
		int sizeY = (int) (baseBox.height / GRAPH_DIVISOR) + 1;

		if (coordXMapS < 0 || coordYMapS < 0)
			return temp;

		for (int y = coordYMapS; y < coordYMapS + sizeY; y++)
			for (int x = coordXMapS; x < coordXMapS + sizeX; x++)
				if (x < temp.length && y < temp[0].length)
					temp[x][y] = false;

		return temp;
	}

	public void renderShadow() {
		glBindFramebuffer(GL_FRAMEBUFFER, shadowFBO);

		glClearColor(1, 1, 1, 0);
		glClear(GL11.GL_COLOR_BUFFER_BIT);

		for (Entity e : Game.getSelf().getEntitiesOnScreen()) {
			RenderComponent rc = (RenderComponent) Game.getSelf().getEm().getFirstComponent(e, RenderComponent.class);
			if (rc != null)
				((ShadowRenderer) ResourceManager.getSelf().getRenderer("shadowRenderer")).render(rc,
						new Vec2(48000, 48000));
		}
		glBindFramebuffer(GL_FRAMEBUFFER, 0);
		glClearColor(1, 1, 1, 1);
	}

	@Override
	public void render() {

		renderShadow();
		glClear(GL_COLOR_BUFFER_BIT);

		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setInteger("terrainMode", 1);
		ResourceManager.getSelf().getShader("texture").setFloat("waveDx", (float) FastMath.sin(Math.toRadians(260 * timer.getElapsedDelta())));
		generateChunks();
		renderChunks();
		ResourceManager.getSelf().getShader("texture").use();
		ResourceManager.getSelf().getShader("texture").setInteger("terrainMode", 0);

		((TextureRenderer) ResourceManager.getSelf().getRenderer("textureRenderer")).render(shadowLayerTexture,
				new Vec2(camera.getX(), camera.getY()), Engine.getSelf().getWindow().getSize(), 0,
				new Vec4(1, 1, 1, 0.2f), new Vec4(0, 0, 1, 1), new Vec2(0, 1), new Vec2(0, 0));

		// renderObstacleMap();
		ResourceManager.getSelf().getShader("texturev2").use();
		ResourceManager.getSelf().getShader("texturev2").setFloat("windForce", timer.getElapsedDelta()%1);
		
		sm.render();
		ParticleEngine.getSelf().render();
		camera.render();
	}

	@Override
	public void variableUpdate(float alpha) {
		chunkMap.update();
		sm.variableUpdate(alpha);
		camera.variableUpdate(alpha);
		
		screenView.x = camera.getX();
		screenView.y = camera.getY();
	}

	@Override
	public void update(float deltaTime) {
		BasicComponent ppc = em.getFirstComponent(player, BasicComponent.class);
		AudioEngine.getSelf().setListenerAt(ppc.getPosition());

		float sin = (float) Math.sin(Math.toRadians(260 * timer.getElapsedDelta())) * 1f;
		sin = (sin < 0) ? sin * -1 : sin;

		ResourceManager.getSelf().getShader("grass").use();
		ResourceManager.getSelf().getShader("grass").setFloat("dx", sin);

		ResourceManager.getSelf().getShader("texture").use();
		if (timer.getElapsedDelta() >= 1)
			timer.reverse();
		else if (timer.getElapsedDelta() <= 0.001f)
			timer.reverse();
		sin = (float) Math.sin(Math.toRadians(260 * timer.getElapsedDelta())) * 1f;
		ResourceManager.getSelf().getShader("texture").setFloat("dayTime", 1f * 1);

		sm.update(deltaTime);

		camera.update(deltaTime);

		quadTree.clear();
		generateQuadTree();
		generateCollisionGraph();
		ParticleEngine.getSelf().update(deltaTime);
	}
	
	public void generateChunks() {
		currentCameraGridX = (int) (camera.getX() / ChunkManager.CHUNK_WIDTH);
		currentCameraGridY = (int) (camera.getY() / ChunkManager.CHUNK_HEIGHT);

		if (currentCameraGridX != previousCameraGridX || currentCameraGridY != previousCameraGridY) {
			ArraysExt.shift(chunksOnScreen, previousCameraGridX - currentCameraGridX, previousCameraGridY - currentCameraGridY);
			ArraysExt.shift(texturenOnScreen, previousCameraGridX - currentCameraGridX, previousCameraGridY - currentCameraGridY);

			previousCameraGridX = currentCameraGridX;
			previousCameraGridY = currentCameraGridY;
		}
		

		for (int y = 0; y < chunksOnScreen[0].length; y++)
			for (int x = 0; x < chunksOnScreen.length; x++) {
				int chunkX = currentCameraGridX + x - 1;
				int chunkY= currentCameraGridY + y - 1;
				
				if (chunksOnScreen[x][y]==null && chunkMap.chunkExists(chunkX, chunkY)) {
					chunksOnScreen[x][y] = chunkMap.get( chunkX, chunkY);
					Texture t = new Texture();
					
					t.generateFloatTextureFromBuffer(
							BufferUtilities.fillFloatBuffer(chunkTextureFloatBuffer, chunksOnScreen[x][y].getPerlinNoise()),
							chunksOnScreen[x][y].getPerlinNoise().length,
							chunksOnScreen[x][y].getPerlinNoise()[0].length, false);
					
					texturenOnScreen[x][y] = t;
				}else if(chunksOnScreen[x][y]==null){
					if(!chunkMap.readChunkFromDiskIfExists(chunkX, chunkY)) {
						chunkMap.generateChunkToSave(chunkX, chunkY);
					}
				}
			}
	}

	private void renderChunks() {
		for (int y = 0; y < chunksOnScreen[0].length; y++)
			for (int x = 0; x < chunksOnScreen.length; x++)
				if (texturenOnScreen[x][y] != null && chunksOnScreen[x][y] != null
						&& chunksOnScreen[x][y].getBoundingBox().intersects(screenView)) { //TODO: aumentar screenview
					Vec2 pos = new Vec2(chunksOnScreen[x][y].getX() * ChunkManager.CHUNK_WIDTH * 1.00,
							chunksOnScreen[x][y].getY() * ChunkManager.CHUNK_HEIGHT * 1.00);
					
					((TextureRenderer) ResourceManager.getSelf().getRenderer("textureRenderer")).render(
							texturenOnScreen[x][y].getId(),
							pos,
							new Vec2(ChunkManager.CHUNK_WIDTH, ChunkManager.CHUNK_HEIGHT), 0, new Vec4(1, 1, 1, 1),
							new Vec4(0, 0, 1, 1), new Vec2(0, 0), new Vec2(0, 0));
					
					ResourceManager.getSelf().getFont("sourcesanspro").render("["+chunksOnScreen[x][y].getX() + ", "+ chunksOnScreen[x][y].getY()+"]", pos.x, pos.y, new Vec4(1,1,1,1));
					ResourceManager.getSelf().getFont("sourcesanspro").render(x+", "+y, pos.x + 100, pos.y, new Vec4(1,1,1,1));
				} 
	}

	private void renderObstacleMap() {
		if (obstacleMap != null)
			for (int y = 0; y < obstacleMap[0].length; y++)
				for (int x = 0; x < obstacleMap.length; x++) {

					int rx = (int) (camera.getX() + x * GRAPH_DIVISOR);
					int ry = (int) (camera.getY() + y * GRAPH_DIVISOR);

					if (obstacleMap[x][y])
						((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(rx, ry),
								new Vec2(18, 18), 0, new Vec4(1, 0, 1, 0.2f));
					if (!obstacleMap[x][y])
						((CubeRenderer) ResourceManager.getSelf().getRenderer("cubeRenderer")).render(new Vec2(rx, ry),
								new Vec2(18, 18), 0, new Vec4(0, 1, 0, 0.2f));
				}
	}

	public boolean intersectsScreenView(Rectangle rec) {
		return screenView.intersects(rec);
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}

	public QuadTree getQuadTree() {
		return quadTree;
	}

	public ArrayList<Entity> getEntitiesOnScreen() {
		return quadTree.queryRange(screenView);
	}

	public EntityManager getEm() {
		return em;
	}

	public Rectangle getScreenView() {
		return screenView;
	}

}
