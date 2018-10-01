package engine.utilities;

public class Color {
	//8 bits per channel
	//TODO: Should use at least 13 bit as per ref: https://www.essentialmath.com/GDC2015/VanVerth_Jim_DoingMathwRGB.pdf
	public static final int ESMERALDA = (255<<24) | (56<<16) | (204<<8) | (113);  //ARGB
	public static final int DARKED_ESMERALDA = (255<<24) | (52<<16) | (200<<8) | (109);
	public static final int WHITE = (255<<24) | (255<<16) | (255<<8) | (255);
	public static final int TURKISH = (255<<24) | (26<<16) | (188<<8) | (156);
	public static final int BLACK = (255<<24) | (0<<16) | (0<<8) | (0);
	public static final int GRASS_GROUND =  convertHexTo8bit("64726a", 255);
	public static final int GRASS_GROUND_LIGHTER =  convertHexTo8bit("6b766f", 255);
	public static final int GRASS_GROUND_DIRTY =  convertHexTo8bit("657069", 255);
	public static final int SAND_GROUND =  convertHexTo8bit("eaecc3", 255);
	public static final int OCEAN_GROUND =  convertHexTo8bit("7b8691", 255);
	
	public static int convertHexTo8bit(String hex, int alpha) {
		int conversion = Integer.parseInt(hex,16);
		conversion = (alpha<<24) | conversion;
		return conversion;
	}
}
