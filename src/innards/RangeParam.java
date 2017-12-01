package innards;

import java.util.Random;

public class RangeParam extends Param<Float> {

	float min;
	float max;
	
	public RangeParam(float min, float max) {
		super((min + max)/2f);
		this.min = min;
		this.max = max;
	}
	
	public float min() {
		return min;
	}
	
	public float max() {
		return max;
	}
	
	public float range() {
		return max - min;
	}
	
	public void setMin(float min) {
		this.min = min;
	}
	
	public void setMax(float max) {
		this.max = max;
	}
	
	public void set(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	public float sample(Random random) {
		return min + random.nextFloat()*range();
	}
	
	public void setSample(Random random) {
		set(sample(random));
	}

}
