package innards.math.data;

import java.util.Collection;
import java.util.Random;
import java.util.Vector;

public class IntegerContinuousModel extends ContinuousModel<Integer> {

	Vector<Integer> data = new Vector<Integer>();
 
	float mean = 0;
	float variance = 0;
	
	Random random = new Random();

	public void add(Integer value) {
		data.add(value);
		calculateStats();
	}
	
	public void addAll(Collection<Integer> values) {
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
	
	public Integer mean() {
		return (int)Math.round(mean);
	}

	public float variance() {
		return variance;
	}
	
	public float[] variances() {
		return new float[] {variance};
	}

	public Integer sample() {
		return (int)(random.nextGaussian()*stdev() + mean);
	}
	
	public boolean fits(Integer value) {
		return Math.abs(value-mean) <= stdevs*stdev();
	}
	
	public Vector<Integer> data() {
		return data;
	}
	
	public float distance(Integer value) {
		return distance(value, (int)mean);
	}
	
	public float distance(Integer value1, Integer value2) {
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
	
	public float valueDistance(Integer value1, Integer value2) {
		return Math.abs(value1 - value2);
	}
	
	public Type type() {
		return Type.Integer;
	}

	public String toString() {
		return "IntegerModel: {mean: " + (int)mean() + ", var: " + variance() + "}";
	}
	
	public ContinuousModel<Integer> duplicate() {
		ContinuousModel<Integer> copy = new IntegerContinuousModel();
		copy.addAll(data);
		return copy;
	}

}
