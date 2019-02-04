package engine.logic;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import demo.Game;
import engine.entity.Entity;
import engine.entity.EntityManager;
import engine.entity.component.BasicComponent;
import engine.entity.component.Component;
import engine.entity.component.RenderComponent;
import engine.entity.componentModels.BasicEntity;
import engine.geometry.Rectangle;
import engine.graphic.Animation;
import engine.graphic.Texture;
import engine.noise.FastNoise;
import engine.utilities.BufferUtilities;
import engine.utilities.ByteBufferExt;
import engine.utilities.Color;
import engine.utilities.MathExt;
import engine.utilities.Vec2i;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class Chunk implements Serializable {

	private int mapRGB[][];
	private int x, y;
	private int chunkWidth, chunkHeight;
	private int textureWidth, textureHeight;
	private int mapWidth, mapHeight;
	public static final int NOISE_DIVISOR = 5;
	private static final float PERLIN_BOUNDARIES = (float) Math.sqrt(3.0/4.0);
	private HashMap<Float, Entity> objects;
	private HashMap<Long, ArrayList<Component>> componentsOfEntities = new HashMap<>();
	private transient Random random = new Random();
	private transient EntityManager em;
	private double waterDx = 1;
	private double wetSandDx = 1;
	private float perlinNoise[][];
	private float whiteNoise[][];
	private float fractalNoise[][];
	private transient FastNoise fastNoise = new FastNoise();
	private ByteBufferExt terrainBuffer;
	private double waveVariation = 0.016;
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

		mapRGB = new int[textureWidth][textureHeight];
		perlinNoise = new float[textureWidth][textureHeight];
		whiteNoise = new float[textureWidth][textureHeight];
		fractalNoise = new float[textureWidth][textureHeight];
		objects = new HashMap<>();
		fastNoise.SetSeed(12345);

		terrainBuffer = new ByteBufferExt(
				BufferUtilities.createByteBuffer(new byte[textureWidth * textureHeight * Texture.BYTES_PER_PIXEL]));
	}

	public int[][] getTerrain() {
		return mapRGB;
	}

	public float noise(FastNoise fastNoise, float x, float y) {
		float noise = 0;

		fastNoise.SetFrequency(0.0005f); // The lesser the frequency, bigger the islands
		noise += fastNoise.GetPerlin(x, y); // First octave

		fastNoise.SetFrequency(0.005f);
		noise += .1 * fastNoise.GetPerlin(x, y); // Second octave

		return noise;
	}

	public void generateNoise() {
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
			}
		}
	}

	public void generateTerrain() {
		terrainBuffer.getBytebuffer().clear();

		for (int y = 0; y < textureHeight; y++) {
			for (int x = 0; x < textureWidth; x++) {

				// NOTE: values grow downwards
				if (perlinNoise[x][y] > -.1) { // land
					mapRGB[x][y] = Color.GRASS_GROUND; // esmeralda
					if (fractalNoise[x][y] > 0.2)
						mapRGB[x][y] = Color.GRASS_GROUND_LIGHTER;
				}

				if (perlinNoise[x][y] <= -.1) // preenche tudo com água
					mapRGB[x][y] = Color.OCEAN_GROUND; // turquesa

				if (perlinNoise[x][y] <= -.1) { // sand
					mapRGB[x][y] = (255 << 24) | (244 << 16) | (234 << 8) | (187); // ARGB
					if (whiteNoise[x][y] > 0)
						mapRGB[x][y] = (255 << 24) | (234 << 16) | (224 << 8) | (167); // ARGB
				}

				if (perlinNoise[x][y] < -.230 + wetSandDx * waveVariation) { // wet sand
					mapRGB[x][y] = (255 << 24) | (224 << 16) | (214 << 8) | (167); // ARGB
					if (whiteNoise[x][y] > 0)
						mapRGB[x][y] = (255 << 24) | (234 << 16) | (224 << 8) | (167); // ARGB
				}

				if (perlinNoise[x][y] < -.230 + waterDx * waveVariation) // espuma
					mapRGB[x][y] = Color.WHITE; // ARGB

				if (perlinNoise[x][y] < -.244 + waterDx * waveVariation) { // espuma back
					mapRGB[x][y] = (255 << 24) | (22 << 16) | (160 << 8) | (133); // green se
					if (perlinNoise[x][y] > -.2445 + waterDx * waveVariation) {
						if (whiteNoise[x][y] < 0.2f)
							mapRGB[x][y] = Color.WHITE;
					}
				}

				if (perlinNoise[x][y] <= -.266 + waterDx * waveVariation) // water
					mapRGB[x][y] = Color.TURKISH; // turquesa

				if (whiteNoise[x][y] > 0.9999
						&& (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if (objects.containsKey(whiteNoise[x][y]))
						continue;

					objects.put(whiteNoise[x][y], generateRandomTree(x, y));
				} else if (whiteNoise[x][y] > 0.99
						&& (mapRGB[x][y] == Color.GRASS_GROUND || mapRGB[x][y] == Color.DARKED_ESMERALDA)) {
					if (objects.containsKey(whiteNoise[x][y]))
						continue;

					objects.put(whiteNoise[x][y], generateRandomGroundVegetation(x, y));
				}
				
				mapRGB[x][y] = mapNoiseToGreyScale(perlinNoise[x][y]);
	
				
				// RGBA color
				terrainBuffer.getBytebuffer().put((byte) ((mapRGB[x][y] >> 16) & 0xFF));
				terrainBuffer.getBytebuffer().put((byte) ((mapRGB[x][y] >> 8) & 0xFF));
				terrainBuffer.getBytebuffer().put((byte) (mapRGB[x][y] & 0xFF));
				terrainBuffer.getBytebuffer().put((byte) ((mapRGB[x][y] >> 24) & 0xFF));
			}
		}
		terrainBuffer.getBytebuffer().flip();
	}
	
	private int mapNoiseToGreyScale(float value) {
		float result;
		
		if(value<0) {
			value = value * -1;
			result = value * 0 + (PERLIN_BOUNDARIES - value) * 127;
		}else {
			result = value * 128 + (PERLIN_BOUNDARIES - value) * 255;
		}
		int RGB = (int) result;

		return 255 <<24 | RGB << 16 | RGB << 8 | RGB;
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

		return e;
	}

	public void addAllEntitiesToEntityManager() {
		HashMap<Long, ArrayList<Component>> newHashMap = new HashMap<>();

		for (Long l : componentsOfEntities.keySet()) {
			Entity e = Game.getSelf().getEm().newEntity();

			for (Component c : componentsOfEntities.get(l))
				Game.getSelf().getEm().addComponentTo(e, c);

			newHashMap.put(e.getID(), componentsOfEntities.get(l));
		}

		componentsOfEntities = newHashMap;
	}

	public void removeAllEntities() {
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
		return terrainBuffer.getBytebuffer();
	}

	public void setTerrainBuffer(ByteBuffer terrainBuffer) {
		this.terrainBuffer.setBytebuffer(terrainBuffer);
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
