package innards.signal;
 
/**
 * The point of this is to have change limiting that is invariant to framerate. 
 * The velocity and acceleration are calculated from system time rather than c6 ticks. 
 * 
 * @author cchao
 *
 */
public class TimeBasedSetter { 
	float x, v, a, target;
	long t; 
	
	float fpsLimit = 200f; 
	
	public TimeBasedSetter(float start, float v) {
		this(start, v, 0);
	}
	
	public TimeBasedSetter(float start, float v, float a) {
		this.x = start; 
		this.v = v;
		this.a = a;
        this.t = System.currentTimeMillis();
	}
	
	public void resetTime() {
		t = System.currentTimeMillis();
	}
	
	public void setTarget(float value) {
		target = value;
	}
	
	public void setVelocity(float v) {
		this.v = v;
	}
	
	public void setAcceleration(float a) {
		this.a = a;
	}
	
	public void setValue(float value) {
		x = value;
	}
	
	public void setFpsLimit(float limit) {
		fpsLimit = limit;
	}
	
	public boolean reached() { 
		return x == target;
	} 
	
	public float getTarget() {
		return target;
	}
	
	public float getValue() {
		return x;
	}
	
	public float getFilteredPosition() {
		long now = System.currentTimeMillis();
		float dt = (float)(now-t)/1000f; 
		
		// Don't allow really tiny intervals, since this messes up math.
		if (dt < 1f/fpsLimit) {
			return x;
		}
		
		if (x != target) {
			float dist = Math.abs(target-x);
			float dir = Math.signum(target-x); 
			float newDist = v*dt + a*dt*dt;
			if (newDist > dist) {
				x = target;
			} else {
				x += dir*newDist;
			} 
		} 
		t = now;
		return x;
	}
	
	public float getFilteredPosition(float customV, float customA, float customDT) {
		long now = System.currentTimeMillis();
		//float dt = (float)(now-t)/1000f;
		float dt = customDT;
		
		// Don't allow really tiny intervals, since this messes up math.
		if (dt < 1f/fpsLimit) {
			return x;
		}
		
		if (x != target) {
			float dist = Math.abs(target-x);
			float dir = Math.signum(target-x); 
			float newDist = customV*dt + customA*dt*dt;
			if (newDist > dist) {
				x = target;
			} else {
				x += dir*newDist;
			} 
		} 
		t = now;
		return x;
	}
	
	public float getFilteredPosition(float customDT) {
		long now = System.currentTimeMillis();
		//float dt = (float)(now-t)/1000f;
		float dt = customDT;
		
		// Don't allow really tiny intervals, since this messes up math.
		if (dt < 1f/fpsLimit) {
			return x;
		}
		
		if (x != target) {
			float dist = Math.abs(target-x);
			float dir = Math.signum(target-x); 
			float newDist = v*dt + a*dt*dt;
			if (newDist > dist) {
				x = target;
			} else {
				x += dir*newDist;
			} 
		} 
		t = now;
		return x;
	}
	
	public String toString() {
		String str = "TimeBasedSetter: ";
		str += "target = " + target + ", ";
		str += "current = " + x;
		return str;
	}
}
