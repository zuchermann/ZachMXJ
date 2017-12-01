package innards.math.data;

import java.util.Collection;
import java.util.Vector;

public abstract class ContinuousModel<T> {

	public enum Type {Vec3, Quat, Float, Integer, Key, Vec};
    public static float stdevs = 2f;
	
	public abstract void add(T value);
	public abstract void addAll(Collection<T> values);
	public abstract T mean();
	public float stdev() {
		return (float)Math.sqrt(variance());
	}
	public float variance() {
		float sum = 0;
		for (T d : data()) {
			float dist = valueDistance(d, mean());
			sum += dist*dist;
		}
		sum /= (float)data().size();
		return sum;
	}
	public abstract float[] variances();
	public abstract T sample();
	public abstract boolean fits(T value);
	public abstract Vector<T> data();
	public abstract Type type();
	public abstract String toString();
	public abstract ContinuousModel<T> duplicate();
	public abstract float valueDistance(T value1, T value2);
	public abstract float distance(T value);
	public abstract float distance(T value1, T value2);
}
