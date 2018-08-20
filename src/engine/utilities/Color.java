package engine.utilities;

public class Color {
	//8 bits per channel
	//TODO: Should use at least 13 bit as per ref: https://www.essentialmath.com/GDC2015/VanVerth_Jim_DoingMathwRGB.pdf
	public static final int ESMERALDA = (255<<24) | (56<<16) | (204<<8) | (113);  //ARGB
	public static final int DARKED_ESMERALDA = (255<<24) | (52<<16) | (200<<8) | (109);
	public static final int WHITE = (255<<24) | (255<<16) | (255<<8) | (255);
	public static final int TURKISH = (255<<24) | (26<<16) | (188<<8) | (156);
	public static final int BLACK = (255<<24) | (0<<16) | (0<<8) | (0);
	
	public static int convertHexTo8bit(String hex) {
		int conversion = Integer.parseInt(hex,16);
		conversion = (255<<24) | conversion;
		return conversion;
	}
}
