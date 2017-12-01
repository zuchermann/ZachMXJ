package innards.math.data;

import innards.math.linalg.Vec3;

public class BoundingBox {

	Vec3 min;
	Vec3 max;
	
	public BoundingBox(Vec3 min, Vec3 max) {
		this.min = min;
		this.max = max;
	}
	
	public boolean intersects(BoundingBox other) {
		return ! (minX() >= other.maxX() || maxX() <= other.minX() ||
				 minY() >= other.maxY() || maxY() <= other.minY() || 
				 minZ() >= other.maxZ() || maxZ() <= other.minZ());
	}
	
	public boolean contains(Vec3 point) {
		return xContains(point) && yContains(point) && zContains(point);
	}
	
	public boolean xContains(Vec3 point) {
		return xContains(point.x());
	}
	
	public boolean xContains(float x) {
		return x >= min.x() && x <= max.x();
	}
	
	public boolean yContains(Vec3 point) {
		return yContains(point.y());
	}
	
	public boolean yContains(float y) {
		return y >= min.y() && y <= max.y();
	}
	
	public boolean zContains(Vec3 point) {
		return zContains(point.z());
	}
	
	public boolean zContains(float z) {
		return z >= min.z() && z <= max.z();
	}
	
	public Vec3 getMin() {
		return min;
	}
	
	public Vec3 getMax() {
		return max;
	}
	
	public float minX() {
		return min.x();
	}
	
	public float minY() {
		return min.y();
	}
	
	public float minZ() {
		return min.z();
	}
	
	public float maxX() {
		return max.x();
	}
	
	public float maxY() {
		return max.y();
	}
	
	public float maxZ() {
		return max.z();
	}
	
	public float width() {
		return max.x() - min.x();
	}
	
	public float height() {
		return max.y() - min.y();
	}
	
	public float depth() {
		return max.z() - min.z();
	}
	
	public String toString() {
		return "BoundingBox: " + min + ", " + max;
	}
}
