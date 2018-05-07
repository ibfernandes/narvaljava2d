package engine.geometry;

import engine.physics.Hit;
import engine.utilities.MathExt;
import engine.utilities.ResourceManager;
import glm.vec._2.Vec2;
import glm.vec._3.Vec3;

public class Rectangle {
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
    
    /**
     * Implements SAT. source: http://noonat.github.io/intersect/#aabb-vs-aabb
     * @param box
     * @return
     */
    public Hit intersectAABB(Rectangle box) {
    	
    	Vec2 thisCenter = getCenter();
    	Vec2 boxCenter = box.getCenter();
    	
    	float dx = boxCenter.x - thisCenter.x;
    	float px = (box.getRadiusX() + getRadiusX()) - Math.abs(dx);

    	if(px<=0 )
    		return null;
    	
    	float dy = boxCenter.y - thisCenter.y;
    	float py = (box.getRadiusY() + getRadiusY()) - Math.abs(dy);
    	
    	if(py<=0)
    		return null;
    	
    	Hit hit = new Hit();
    	if(px<py) {
    		float sx = MathExt.sign(dx);
    		hit.delta.x = px*sx;
    		hit.normal.x = sx;
    		hit.pos.x = thisCenter.x + (getRadiusX() * sx);
    		hit.pos.y = boxCenter.y;
    	}else {
    		float sy = MathExt.sign(dy);
    		hit.delta.y = py * sy;
    		hit.normal.y = sy;
    		hit.pos.x = boxCenter.x;
    		hit.pos.y = thisCenter.y + (getRadiusY() * sy);
    	}
    		
    	
    	return hit;
    }
    
    
    public Hit sweepIntersectsAABB(Rectangle box, Vec2 delta) { // delta: last point?
       	Hit hit = null;
    	float sweepTime = 0;
    	Vec2 sweepPos = new Vec2();
    	Vec2 thisCenter = getCenter();
    	Vec2 boxCenter = box.getCenter();
    	
    	if(delta.x==0 && delta.y==0) {
    		hit = intersectAABB(box);
    		
    		if(hit != null)
    			sweepTime = hit.time = 0;
    		else
    			sweepTime = 1;
    		
    		return hit;
    	}
    	
    	hit = intersectSegment(box.getCenter(), delta, box.getRadiusX(), box.getRadiusY());
    	
    	if(hit!=null) {

    		sweepTime = MathExt.clamp(hit.time - EPSILON, 0, 1);
    		sweepPos.x = boxCenter.x + delta.x * sweepTime; 
    		sweepPos.y = boxCenter.y + delta.y * sweepTime; 
    		Vec2 direction = new Vec2(delta.x, delta.y);
    		direction = direction.normalize();
    		hit.pos.x = MathExt.clamp(hit.pos.x + direction.x*box.getRadiusX(),
    				thisCenter.x - getRadiusX(),
    				thisCenter.x + getRadiusX());
    		hit.pos.y = MathExt.clamp(hit.pos.y + direction.y*box.getRadiusY(),
    				thisCenter.y - getRadiusY(), 
    				thisCenter.y + getRadiusY());
    		
    		hit.pos.x = sweepPos.x;
    		hit.pos.y = sweepPos.y;
    		
    	}else {
    		//Se não deu hit, movimenta normal (posso retornar um null aqui)
    		sweepPos.x = boxCenter.x + delta.x;
    		sweepPos.y = boxCenter.y + delta.y;
    		sweepTime = 1 ;
    	}
    	
    	
    	return hit;
    		
    }
    
    public boolean intersectsPoint(Vec2 point) {
    	
    	if ( point.x>this.x && point.x<this.x+this.width)
    		if(point.y>this.y && point.y<this.y + this.height)
    			return true;
    	return false;
    }
    
    //TODO: didn't test this yet
    //post = start, delta = END VARIANTION 
    public Hit intersectSegment(Vec2 pos, Vec2 delta, float padX, float padY) {

    	float scaleX, scaleY;
    	scaleX = 1 / ((delta.x==0)? 1: delta.x);
    	scaleY = 1 / ((delta.y==0)? 1: delta.y);
    	
    	/*if(delta.x==0)
    		scaleX =0;
    	else
    		scaleX = 1/ delta.x;
    	
    	if(delta.y==0)
    		scaleY =0;
    	else
    		scaleY = 1/ delta.y;*/
    	
    	float signX = MathExt.sign(scaleX);
    	float signY = MathExt.sign(scaleY);
    	
    	Vec2 thisCenter = getCenter();

    	
    	float nearTimeX = (thisCenter.x - signX * (getRadiusX() + padX) - pos.x) * scaleX; // change getCenter()
    	float nearTimeY = (thisCenter.y - signY * (getRadiusY() + padY) - pos.y) * scaleY;
    	
    	float farTimeX = (thisCenter.x + signX * (getRadiusX() + padX) - pos.x) * scaleX;
    	float farTimeY = (thisCenter.y + signY * (getRadiusY() + padY) - pos.y) * scaleY;
    	
    	if(nearTimeX > farTimeY || nearTimeY > farTimeX)
    		return null;
    	
    	float nearTime = (nearTimeX>nearTimeY)? nearTimeX : nearTimeY;
    	float farTime = (farTimeX< farTimeY)? farTimeX : farTimeY;
    	
    	if(nearTime >= 1 || farTime<=0)
    		return null;
    	
    	Hit hit = new Hit();
    	hit.time = MathExt.clamp(nearTime, 0, 1);
    	if(nearTimeX > nearTimeY) {
    		hit.normal.x = -signX;
    		hit.normal.y = 0;
    	}else {
    		hit.normal.x = 0;
    		hit.normal.y = -signY;
    	}
    	
    	hit.delta.x = hit.time * delta.x;
    	hit.delta.y = hit.time * delta.y;
    	
    	hit.pos.x = pos.x + hit.delta.x;
    	hit.pos.y = pos.y + hit.delta.y;

    	return hit;
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
}
