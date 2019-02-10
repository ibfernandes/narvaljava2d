package engine.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import demo.Game;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.Component;
import engine.entity.component.MoveComponent;
import engine.entity.component.RenderComponent;
import engine.entity.componentModels.BasicEntity;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.noise.FastNoise;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Chunk implements Serializable {

	private int x, y;
	private int chunkWidth, chunkHeight;
	private int textureWidth, textureHeight;
	private int mapWidth, mapHeight;
	public static final int NOISE_DIVISOR = 5;
	private static final float PERLIN_BOUNDARIES = (float) Math.sqrt(3.0/4.0);
	private HashMap<Long, ArrayList<Component>> componentsOfEntities = new HashMap<>();
	private transient Random random = new Random();
	private transient EntityManager em;
	private float perlinNoise[][];
	private float whiteNoise[][];
	private float fractalNoise[][];
	private transient FastNoise fastNoise = new FastNoise();
	private Rectangle boundingBox = new Rectangle(0, 0, 0, 0);

	public Chunk(int x, int y, int chunkWidth, int chunkHeight, int mapWidth, int mapHeight, EntityManager em) {
		this.em = em;
		this.x = x;
		this.y = y;
		this.chunkWidth = chunkWidth;
		this.chunkHeight = chunkHeight;
		this.mapWidth = mapWidth;
		this.mapHeight = mapHeight;

		boundingBox.x = x * chunkWidth;
		boundingBox.y = y * chunkHeight;
		boundingBox.width = chunkWidth;
		boundingBox.height = chunkHeight;

		textureWidth = chunkWidth / NOISE_DIVISOR;
		textureHeight = chunkHeight / NOISE_DIVISOR;

		perlinNoise = new float[textureWidth][textureHeight];
		whiteNoise = new float[textureWidth][textureHeight];
		fractalNoise = new float[textureWidth][textureHeight];
		fastNoise.SetSeed(12345);
	}


	public float noise(FastNoise fastNoise, float x, float y) {
		float noise = 0;

		fastNoise.SetFrequency(0.0005f); // The lesser the frequency, bigger the islands
		noise += fastNoise.GetPerlin(x, y); // First octave

		fastNoise.SetFrequency(0.005f);
		noise += .1 * fastNoise.GetPerlin(x, y); // Second octave

		return noise;
	}

	public void generateNoiseAndTerrain() {
		float d;
		float a = 0.15f;
		float b = 0.9f;
		float c = 2f;

		int coordX = this.x * chunkWidth / NOISE_DIVISOR;
		int coordY = this.y * chunkHeight / NOISE_DIVISOR;

		int constX = coordX;
		int constY = coordY;

		for (int y = 0; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {
				coordX = constX + x;
				coordY = constY + y;

				d = 2 * Math.max(Math.abs((float) coordX / mapWidth - (float) (mapWidth / 2) / mapWidth),
						Math.abs((float) coordY / mapHeight - (float) (mapHeight / 2) / mapHeight));

				perlinNoise[x][y] = noise(fastNoise, coordX / 3f, coordY);
				whiteNoise[x][y] = fastNoise.GetWhiteNoise(coordX, coordY);
				fractalNoise[x][y] = fastNoise.GetPerlinFractal(coordX / 4, coordY);

				perlinNoise[x][y] = perlinNoise[x][y] + a - b * (float) Math.pow(d, c);
				
				if (whiteNoise[x][y] > 0.9999
						&& perlinNoise[x][y] > -.1) {


					generateRandomTree(x, y);
				} else if (whiteNoise[x][y] > 0.99
						&& perlinNoise[x][y] > -.1) {

					generateRandomGroundVegetation(x, y);
				}
			}
		}
	}

	public Entity generateRandomTree(int x, int y) {
		Vec2 orientation;
		Vec2 position = new Vec2(x * NOISE_DIVISOR + this.x * chunkWidth, y * NOISE_DIVISOR + this.y * chunkHeight);
		if (random.nextBoolean())
			orientation = new Vec2(0, 0);
		else
			orientation = new Vec2(1, 0);

		AnimationStateManager asm = new AnimationStateManager();

		Animation an;
		an = new Animation("tree", -1);

		an.setFrames(1, new Vec2(0, 0), new Vec2(64, 64));
		asm.addAnimation("idle_1", an);
		asm.changeStateTo("idle_1");

		Entity e = BasicEntity.generate(em, "grassRenderer", position, null, orientation, new Vec2(740, 612), asm,
				new Rectangle(0.0f, 0.99f, 1.0f, 0.1f));
		e.setChunkX(this.x);
		e.setChunkY(this.y);
		return e;
	}

	public Entity generateRandomGroundVegetation(int x, int y) {
		Vec2 position = new Vec2(x * NOISE_DIVISOR + this.x * chunkWidth, y * NOISE_DIVISOR + this.y * chunkHeight);

		AnimationStateManager asm = new AnimationStateManager();

		Animation an;
		an = new Animation("grass", -1);
		if (whiteNoise[x][y] > 0.9995)
			an = new Animation("flower_red", -1);
		else if (whiteNoise[x][y] > 0.9991)
			an = new Animation("flower_blue", -1);
		else
			an = new Animation("flower", -1);

		an.setFrames(1, new Vec2(0, 0), new Vec2(10, 8));
		asm.addAnimation("idle_1", an);
		asm.changeStateTo("idle_1");

		Entity e = Game.getSelf().getEm().newEntity();

		RenderComponent rc = new RenderComponent(e.getID());
		rc.setSize(new Vec2(30, 24));
		rc.setColor(new Vec4(1, 1, 1, 1));
		rc.setAnimations(asm);
		rc.setRenderPosition(position);
		rc.setRenderer("grassRenderer");
		rc.setBaseBox(new Rectangle(0.0f, 0.9f, 1.0f, 0.1f));
		Game.getSelf().getEm().addComponentTo(e, rc);

		BasicComponent pc = new BasicComponent(e.getID());
		pc.setPosition(position);
		pc.setSize(new Vec2(30, 24));
		Game.getSelf().getEm().addComponentTo(e, pc);

		ArrayList<Component> components = new ArrayList<>();
		components.add(rc);
		components.add(pc);
		componentsOfEntities.put(e.getID(), components);
		e.setChunkX(this.x);
		e.setChunkY(this.y);
		return e;
	}

	public void transferAllEntitiesToEntityManager() {
		HashMap<Long, ArrayList<Component>> newHashMap = new HashMap<>();
		
		for (Long l : componentsOfEntities.keySet()) {
			Entity e = Game.getSelf().getEm().newEntity();
			e.setChunkX(this.x);
			e.setChunkY(this.y);

			for (Component c : componentsOfEntities.get(l)) {
				c.setEntityID(e.getID());
				Game.getSelf().getEm().addComponentTo(e, c);
			}

			newHashMap.put(e.getID(), componentsOfEntities.get(l));
		}
		
		componentsOfEntities.clear();
	}
	
	private void copyEntityAndComponentsFromEntityManager(Entity e) {
		ArrayList<Component> compos = Game.getSelf().getEm().getAllComponentsOfEntity(e);
		componentsOfEntities.put(e.getID(),compos);
	}
	
	public void getEntitiesFromEntityManager() {
		componentsOfEntities.clear();
		
		for(Entity e: Game.getSelf().getEm().getAllEntities()) {
			if(!e.shouldSave())
				continue;
			
			if(Game.getSelf().getEm().getFirstComponent(e, MoveComponent.class)==null) {
				if(e.getChunkX()==this.x && e.getChunkY()==this.y) {
					copyEntityAndComponentsFromEntityManager(e);
				}
			}else {
				BasicComponent bc = Game.getSelf().getEm().getFirstComponent(e, BasicComponent.class);
				if(bc.getBoundingBox().intersects(getBoundingBox())) {
					copyEntityAndComponentsFromEntityManager(e);
				}
			}
		}
	}

	public void removeAllEntitiesFromEntityManager() {
		for (Long l : componentsOfEntities.keySet()) {
			Game.getSelf().getEm().removeEntity(l);
		}
	}

	public Vec2i getPosition() {
		return new Vec2i(x, y);
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public String getFileName() {
		return x + "_" + y + ".chunk";
	}

	public static String getFileName(int x, int y) {
		return x + "_" + y + ".chunk";
	}

	public static String getID(int x, int y) {
		return x + "_" + y;
	}

	public Rectangle getBoundingBox() {
		return boundingBox;
	}

	public float[][] getPerlinNoise() {
		return perlinNoise;
	}

	public void setPerlinNoise(float[][] perlinNoise) {
		this.perlinNoise = perlinNoise;
	}
}
