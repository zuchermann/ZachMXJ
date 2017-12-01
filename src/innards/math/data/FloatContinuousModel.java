package innards.math.data;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

public class FloatContinuousModel extends ContinuousModel<Float> {

	Vector<Float> data = new Vector<Float>();
 
	float mean = 0;
	float variance = 0;
	
	Random random = new Random();

	public void add(Float value) {
		data.add(value);
		calculateStats();
	}
	
	public void addAll(Collection<Float> values) {
		data.addAll(values);
		calculateStats();
		
	}
	
	void calculateStats() {
		float sum = 0;
		for (float d : data) {
			sum += d;
		}
		sum /= (float)data.size();
		mean = sum;
		
		sum = 0;
		for (float d : data) {
			sum += (d-mean)*(d-mean);
		}
		sum /= (float)data.size();
		variance = sum;
	}
	
	public Float mean() {
		return mean;
	}

	public float variance() {
		return variance;
	}
	
	public float[] variances() {
		return new float[] {variance};
	}

	public Float sample() {
		return (float)(random.nextGaussian()*stdev() + mean);
	}
	
	public boolean fits(Float value) {
		return Math.abs(value-mean) <= stdevs*stdev();
	}
	
	public Vector<Float> data() {
		return data;
	}
	
	public float distance(Float value) {
		return distance(value, mean);
	}
	
	public float distance(Float value1, Float value2) {
 		float d = value1-value2;
 		if (variance > 0) {
 			return (float)Math.sqrt(d*d/variance);
 		} else {
 			if (d == 0) {
 				return 0;
 			} else {
 				return Float.MAX_VALUE;
 			}
 		}
	}
	
	public Type type() {
		return Type.Float;
	}
	
	public float valueDistance(Float value1, Float value2) {
		return Math.abs(value1 - value2);
	}

	public String toString() {
		return "FloatModel: {mean: " + mean() + ", var: " + variance() + "}";
	}
	
	public ContinuousModel<Float> duplicate() {
		ContinuousModel<Float> copy = new FloatContinuousModel();
		copy.addAll(data);
		return copy;
	}

}
