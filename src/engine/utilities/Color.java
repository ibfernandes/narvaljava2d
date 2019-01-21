package engine.utilities;

import java.util.ArrayList;

public class Color {
	public static final int ESMERALDA = (255 << 24) | (56 << 16) | (204 << 8) | (113); // ARGB
	public static final int DARKED_ESMERALDA = (255 << 24) | (52 << 16) | (200 << 8) | (109);
	public static final int WHITE = (255 << 24) | (255 << 16) | (255 << 8) | (255);
	public static final int TURKISH = (255 << 24) | (26 << 16) | (188 << 8) | (156);
	public static final int BLACK = (255 << 24) | (0 << 16) | (0 << 8) | (0);
	public static final int GRASS_GROUND = convertHexTo8bit("64726a", 255);
	public static final int GRASS_GROUND_LIGHTER = convertHexTo8bit("6b766f", 255);
	public static final int GRASS_GROUND_DIRTY = convertHexTo8bit("657069", 255);
	public static final int SAND_GROUND = convertHexTo8bit("eaecc3", 255);
	public static final int OCEAN_GROUND = convertHexTo8bit("7b8691", 255);

	/**
	 * Converts @param color in format Hexadecimal to a 32 bit RGBA integer with 8
	 * bits per channel.
	 * 
	 * @param hex
	 * @param alpha
	 * @return
	 */
	public static int convertHexTo8bit(String color, int alpha) {
		int conversion = Integer.parseInt(color, 16);
		conversion = (alpha << 24) | conversion;
		return conversion;
	}

	/**
	 * Decomposes @param color into 4 channels of 8 bits and returns an ArrayList
	 * with these 4 channels following the pattern RGBA.
	 * 
	 * @param color
	 * @return
	 */
	public static ArrayList<Integer> decomposeColor(int color) {
		int red, green, blue, alpha;
		ArrayList<Integer> colors = new ArrayList<>();

		red = color >> 16 & 0x0000FF;
		green = color >> 8 & 0x0000FF;
		blue = color & 0x0000FF;
		alpha = color >> 24 & 0x0000FF;

		colors.add(red);
		colors.add(green);
		colors.add(blue);
		colors.add(alpha);
		return colors;
	}
}
