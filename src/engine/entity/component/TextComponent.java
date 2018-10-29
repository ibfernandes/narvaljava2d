package engine.entity.component;

import glm.vec._2.Vec2;
import glm.vec._4.Vec4;

public class TextComponent extends Component{
	public TextComponent(long entityID) {
		super(entityID);
		
	}
	private String text;
	private String fontName;
	private int fontSize;
	private Vec4 fontColor;
	private Vec2 position;
	private boolean disabled = false;
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Vec2 getPosition() {
		return position;
	}
	public void setPosition(Vec2 position) {
		this.position = position;
	}
	public String getFontName() {
		return fontName;
	}
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	public int getFontSize() {
		return fontSize;
	}
	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}
	public Vec4 getFontColor() {
		return fontColor;
	}
	public void setFontColor(Vec4 fontColor) {
		this.fontColor = fontColor;
	}
	public boolean isDisabled() {
		return disabled;
	}
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
}
