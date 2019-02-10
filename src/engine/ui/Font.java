package engine.ui;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.FontFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.system.MemoryUtil;

import engine.graphic.Texture;
import engine.renderer.TextureBatchRenderer;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

import static java.awt.Font.MONOSPACED;
import static java.awt.Font.PLAIN;
import static java.awt.Font.TRUETYPE_FONT;

/**
* This class contains a font texture for drawing text.
*
* Based on Heiko's Brumme work.
*/
public class Font {

   private final Map<Character, Glyph> glyphs;
   private final Texture texture;
   private int fontHeight;
   private Vec2 boxSize = new Vec2(0,0);
   private Vec2 position = new Vec2();
   private Vec2 size = new Vec2();
   private Vec4 spriteFrame = new Vec4();
   private Vec2 orientation = new Vec2(0,1);
   

   /**
    * Creates a default font with monospaced glyphs and default size 128.
    *
    * @param antiAliasing
    */
   public Font(boolean antiAliasing) {
       this(new java.awt.Font(MONOSPACED, PLAIN, 128), antiAliasing);
   }

   /**
    * Creates a Font from an input stream.
    *
    * @param in   
    * @param size 
    * @param antiAliasing 
    * @throws FontFormatException 
    * @throws IOException  
    */
   public Font(InputStream in, int size, boolean antiAliasing) throws FontFormatException, IOException {
       this(java.awt.Font.createFont(TRUETYPE_FONT, in).deriveFont(PLAIN, size), antiAliasing);
   }

   /**
    * Creates a anti-aliased font from an AWT Font.
    *
    * @param font The AWT Font
    */
   public Font(java.awt.Font font) {
       this(font, true);
   }

   /**
    * Creates a font from an AWT Font.
    *
    * @param font
    * @param antiAliasing
    */
   public Font(java.awt.Font font, boolean antiAliasing) {
       glyphs = new HashMap<>();
       texture = createFontTexture(font, antiAliasing);
   }

   /**
    * Creates a font texture from specified AWT font.
    *
    * @param font   
    * @param antiAlias
    * @return Texture 
    */
   private Texture createFontTexture(java.awt.Font font, boolean antiAlias) {
       int imageWidth = 0;
       int imageHeight = 0;

       /* Start at char #32, because ASCII 0 to 31 are just control codes */
       for (int i = 32; i < 256; i++) {
           if (i == 127) 
               /* ASCII 127 is the DEL control code, so we can skip it */
               continue;
           
           char c = (char) i;
           BufferedImage ch = createCharImage(font, c, antiAlias);
           if (ch == null) 
               /* If char image is null that font does not contain the char */
               continue;
           
           imageWidth += ch.getWidth();
           imageHeight = Math.max(imageHeight, ch.getHeight());
       }

       fontHeight = imageHeight;

       BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
       Graphics2D g = image.createGraphics();
       
       int x = 0;

       /* Create image for the standard chars */
       for (int i = 32; i < 256; i++) {
           if (i == 127) 
               continue;
           
           char c = (char) i;
           BufferedImage charImage = createCharImage(font, c, antiAlias);
           
           if (charImage == null) 
               continue;

           int charWidth = charImage.getWidth();
           int charHeight = charImage.getHeight();

           /* Create glyph and draw char on image */
           Glyph ch = new Glyph(charWidth, charHeight, x, image.getHeight() - charHeight, 0f);
           g.drawImage(charImage, x, 0, null);
           x += ch.width;
           glyphs.put(c, ch);
       }

       /* Flip image Horizontally to get the origin to bottom left */
       AffineTransform transform = AffineTransform.getScaleInstance(1f, -1f);
       transform.translate(0, -image.getHeight());
       AffineTransformOp operation = new AffineTransformOp(transform, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
       image = operation.filter(image, null);

       /* Get charWidth and charHeight of image */
       int width = image.getWidth();
       int height = image.getHeight();

       /* Get pixel data of image */
       int[] pixels = new int[width * height];
       image.getRGB(0, 0, width, height, pixels, 0, width);

       /* Put pixel data into a ByteBuffer */
       ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
       for (int i = 0; i < height; i++) {
           for (int j = 0; j < width; j++) {
               /* Pixel as RGBA: 0xAARRGGBB */
               int pixel = pixels[i * width + j];
               /* Red component 0xAARRGGBB >> 16 = 0x0000AARR */
               buffer.put((byte) ((pixel >> 16) & 0xFF));
               /* Green component 0xAARRGGBB >> 8 = 0x00AARRGG */
               buffer.put((byte) ((pixel >> 8) & 0xFF));
               /* Blue component 0xAARRGGBB >> 0 = 0xAARRGGBB */
               buffer.put((byte) (pixel & 0xFF));
               /* Alpha component 0xAARRGGBB >> 24 = 0x000000AA */
               buffer.put((byte) ((pixel >> 24) & 0xFF));
           }
       }
       buffer.flip();

       /* Create texture */
       Texture fontTexture = new Texture(buffer, width, height, antiAlias);
       MemoryUtil.memFree(buffer);
       return fontTexture;
   }

   /**
    * Creates a char image from specified AWT font.
    *
    * @param font 
    * @param c 
    * @param antiAlias
    * @return Char image
    */
   private BufferedImage createCharImage(java.awt.Font font, char c, boolean antiAlias) {
       /* Creating temporary image to extract character size */
       BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
       Graphics2D g = image.createGraphics();
       
       if (antiAlias) 
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       
       g.setFont(font);
       FontMetrics metrics = g.getFontMetrics();
       g.dispose();

       int charWidth = metrics.charWidth(c);
       int charHeight = metrics.getHeight();

       if (charWidth == 0) 
           return null;

       /* Create image for holding the char */
       image = new BufferedImage(charWidth, charHeight, BufferedImage.TYPE_INT_ARGB);
       g = image.createGraphics();
       
       if (antiAlias) 
           g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       
       g.setFont(font);
       g.setPaint(java.awt.Color.RED);
       g.drawString(String.valueOf(c), 0, metrics.getAscent());
       g.dispose();
       return image;
   }

   /**
    * Gets the width of the specified text.
    *
    * @param text The text
    * @return Width of text
    */
   public int getWidth(CharSequence text) {
       int width = 0;
       int lineWidth = 0;
       for (int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if (c == '\n') {
               /* Line end, set width to maximum from line width and stored
                * width */
               width = Math.max(width, lineWidth);
               lineWidth = 0;
               continue;
           }
           if (c == '\r') {
               /* Carriage return, just skip it */
               continue;
           }
           Glyph g = glyphs.get(c);
           lineWidth += g.width;
       }
       width = Math.max(width, lineWidth);
       return width;
   }

   /**
    * Gets the height of the specified text.
    *
    * @param text The text
    * @return Height of text
    */
   public int getHeight(CharSequence text) {
       int height = 0;
       int lineHeight = 0;
       for (int i = 0; i < text.length(); i++) {
           char c = text.charAt(i);
           if (c == '\n') {
               /* Line end, add line height to stored height */
               height += lineHeight;
               lineHeight = 0;
               continue;
           }
           if (c == '\r') 
               continue;
           
           Glyph g = glyphs.get(c);
           lineHeight = Math.max(lineHeight, g.height);
       }
       height += lineHeight;
       return height;
   }

   /**
    * Draw text at the specified position.
    *
    * @param text 
    * @param x 
    * @param y 
    * @param c
    */
   public void render(CharSequence text, float x, float y, Vec4 color) {
       int textHeight = getHeight(text);

       float drawX = x;
       float drawY = y;
       if (textHeight > fontHeight) {
           drawY += textHeight - fontHeight;
       }
       
       TextureBatchRenderer batchRenderer = ResourceManager.getSelf().getRenderer("batchTextureRenderer");
       batchRenderer.start(texture);

       for (int i = 0; i < text.length(); i++) {
           char ch = text.charAt(i);
           if (ch == '\n') {
               /* Line feed, set x and y to draw at the next line */
               drawY += fontHeight;
               drawX = x;
               continue;
           }
           
           if (ch == '\r') 
               continue;
           
           Glyph g = glyphs.get(ch);
          
           spriteFrame.x = (float)g.x/(float)texture.getWidth();
           spriteFrame.y = (float)g.y/(float)texture.getHeight();
           spriteFrame.z = (float)g.width/(float)texture.getWidth();
           spriteFrame.w = (float)g.height/(float)texture.getHeight();
           
           position.x = drawX;
           position.y = drawY;
           
           size.x = g.width*.2f;
           size.y = g.height*.2f;
           
           batchRenderer.render(position, size, 0f, color, spriteFrame, orientation);
           
           drawX += g.width*.2f;
       }
       
		batchRenderer.end();
   }

	public Vec2 getBoxSize() {
		return boxSize;
	}
	
	public void setBoxSize(Vec2 boxSize) {
		this.boxSize = boxSize;
	}
}