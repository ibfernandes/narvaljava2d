package engine.geometry;

import java.io.Serializable;

import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class Rectangle implements Serializable {
	public float x,y,width,height;
	public static final float EPSILON = (float)1e-8;
	
	public Rectangle(float x, float y, float width, float height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	/**
     * Determines whether or not this <code>Rectangle</code> and the specified
     * <code>Rectangle</code> intersect. Two rectangles intersect if
     * their intersection is nonempty.
     *
     * @param r the specified <code>Rectangle</code>
     * @return    <code>true</code> if the specified <code>Rectangle</code>
     *            and this <code>Rectangle</code> intersect;
     *            <code>false</code> otherwise.
     */
    public boolean intersects(Rectangle r) {
    	float tw = this.width;
        float th = this.height;
        float rw = r.width;
        float rh = r.height;
        if (rw <= 0 || rh <= 0 || tw <= 0 || th <= 0) {
            return false;
        }
        float tx = this.x;
        float ty = this.y;
        float rx = r.x;
        float ry = r.y;
        rw += rx;
        rh += ry;
        tw += tx;
        th += ty;
        //      overflow || intersect
        return ((rw < rx || rw > tx) &&
                (rh < ry || rh > ty) &&
                (tw < tx || tw > rx) &&
                (th < ty || th > ry));
    }
    

	public boolean fitsEntirely(Rectangle r){
	    if ( (r.x+r.width) < (x+width)
	        && (r.x) > (x)
	        && (r.y) > (y)
	        && (r.y+r.height) < (y+height))
	        return true;
	    else
	        return false;
	}
    
    public boolean intersectsPoint(Vec2 point) {
    	
    	if ( point.x>=this.x && point.x<=this.x+this.width)
    		if(point.y>=this.y && point.y<=this.y + this.height)
    			return true;
    	return false;
    }

    public float getCenterY() {
        return getY() + getHeight() / 2f;
    }
    public float getCenterX() {
        return getX() + getWidth() / 2f;
    }
    public Vec2 getCenter() {
    	return new Vec2(getCenterX(), getCenterY());
    }

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}
	
	public Vec2 getPos() {
		return new Vec2(x,y);
	}
	public float getRadiusX() {
		return width/2f;
	}
	public float getRadiusY() {
		return height/2f;
	}

	public float getWidth() {
		return width;
	}

	public void setWidth(float width) {
		this.width = width;
	}

	public float getHeight() {
		return height;
	}

	public void setHeight(float height) {
		this.height = height;
	}
	
	public Vec2 getSize() {
		return new Vec2(width, height);
	}
}
