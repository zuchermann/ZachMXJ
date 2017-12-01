package innards.math.linalg;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A 3-element vector.
 * <P>
 * 
 * Indices start at zero.
 */
public final class Vec3 implements Serializable, Cloneable {
	private float[] rep = new float[3];
	
	public final static Vec3 UNIT_X = new Vec3(1.0, 0.0, 0.0);
	public final static Vec3 UNIT_Y = new Vec3(0.0, 1.0, 0.0);
	public final static Vec3 UNIT_Z = new Vec3(0.0, 0.0, 1.0);
	
	/**
	 * Elements initialized to zero.
	 */
	public Vec3() {
	}
	/**
	 * Creates a Vec3 with elements <code>[x y z]</code>
	 */
	public Vec3(float x, float y, float z) {
		setValue(x, y, z);
	}

	/**
	 * Creates a Vec3 with elements <code>[x y z]</code>
	 */
	public Vec3(double x, double y, double z) {
		setValue((float) x, (float) y, (float) z);
	}
	/**
	 * Copy constructor (copies the first 3 elements).
	 */
	public Vec3(Vec a) {
		if (a.dim() != 3) {
			error("Vec3(Vec): arg is not dim 3!");
		} else {
			rep[0] = (float) a.ref(0);
			rep[1] = (float) a.ref(1);
			rep[2] = (float) a.ref(2);
		}
	}
	/**
	 * Copy constructor.
	 */
	public Vec3(Vec3 a) {
		rep[0] = a.rep[0];
		rep[1] = a.rep[1];
		rep[2] = a.rep[2];
	}
	/**
	 * Copy constructor (copies the first 3 elements).
	 */
	public Vec3(float[] vec) {
		rep[0] = vec[0];
		rep[1] = vec[1];
		rep[2] = vec[2];
	}

	/**
	 * Convenience method that calls clone(), but saves the trouble of casting from an Object to a Vec.
	 */
	public Vec3 copy() {
		return (Vec3) clone();
	}

	/**
	 * Clones this Vec3.
	 */
	public Object clone() {
		Vec3 f = new Vec3();
		f.setValue(this);
		return f;
	}

	/**
	 * Copies the data in <code>v</code> to <code>this</code>.
	 */
	public void fromVec(Vec v) {
		if (v.dim() != 3)
			throw new DimensionMismatchException("Setting Vec3 from Vec not of dim 3");
		rep[0] = (float) v.get(0);
		rep[1] = (float) v.get(1);
		rep[2] = (float) v.get(2);
	}
	/**
	 * Copies the data in <code>this</code> to <code>v</code>.
	 */
	public void toVec(Vec v) {
		v.fromVec3(this);
	}

	/**
	 * Returns true if <code>otherVec</code> is a <code>Vec</code>, and all elements of <code>this</code> are equal to coresponding elements of <code>otherVec</code>.
	 */
	public boolean equals(Object otherVec) {
		if (otherVec instanceof Vec3) {
			return equals((Vec3) otherVec);
		} else
			return false;
	}

    public int hashCode() {
        int result = 3;
        for (int i = 0; i < rep.length; i++) {
            result = 131*result + Float.floatToIntBits(rep[i]);
        }
        return result;
    }

	/**
	 * Returns the <code>i</code> th element.
	 */
	public float ref(int i) {
		return rep[i];
	}
	/**
	 * Returns the <code>i</code> th element.
	 */
	public float get(int i) {
		return rep[i];
	}

	/**
	 * Returns the first element.
	 */
	public float getX() {
		return rep[0];
	}
	/**
	 * Returns the second element.
	 */
	public float getY() {
		return rep[1];
	}
	/**
	 * Returns the third element.
	 */
	public float getZ() {
		return rep[2];
	}
	/**
	 * Returns the first element.
	 */
	public float x() {
		return rep[0];
	}
	/**
	 * Returns the second element.
	 */
	public float y() {
		return rep[1];
	}
	/**
	 * Returns the third element.
	 */
	public float z() {
		return rep[2];
	}

	/**
	 * Returns a copy of this vector's elements as a 3-element array.
	 */
	public float[] getValue() {
		float[] out = new float[3];
		out[0] = rep[0];
		out[1] = rep[1];
		out[2] = rep[2];
		return out;
	}

	/**
	 * uses this tmp storage, can be null.
	 * 
	 * @param tmp
	 * @return float[]
	 */
	public float[] getValue(float[] tmp) {
		if (tmp == null)
			tmp = new float[3];
		System.arraycopy(rep, 0, tmp, 0, 3);
		return tmp;
	}
	
	public double[] getDoubleValue() {
		double[] out = new double[3];
		out[0] = rep[0];
		out[1] = rep[1];
		out[2] = rep[2];
		return out;
	}

	/**
	 * Sets the <code>i</code> th element to <code>d</code>
	 * 
	 * @return this
	 */
	public Vec3 set(int i, float d) {
		rep[i] = d;
		return this;
	}
	/**
	 * Sets the first element to <code>d</code>
	 * 
	 * @return this
	 */
	public Vec3 setX(float d) {
		rep[0] = d;
		return this;
	}
	/**
	 * Sets the second element to <code>d</code>
	 * 
	 * @return this
	 */
	public Vec3 setY(float d) {
		rep[1] = d;
		return this;
	}
	/**
	 * Sets the third element to <code>d</code>
	 * 
	 * @return this
	 */
	public Vec3 setZ(float d) {
		rep[2] = d;
		return this;
	}
	/**
	 * Sets this Vec3's three elements to <code>x</code>,<code>y</code>, and <code>z</code>.
	 * 
	 * @return this
	 */
	public Vec3 setValue(double x, double y, double z) {
		rep[0] = (float)x;
		rep[1] = (float)y;
		rep[2] = (float)z;
		return this;
	}
	/**
	 * Sets this Vec3's three elements to <code>x</code>,<code>y</code>, and <code>z</code>.
	 * 
	 * @return this
	 */
	public Vec3 setValue(float x, float y, float z) {
		rep[0] = x;
		rep[1] = y;
		rep[2] = z;
		return this;
	}
	/**
	 * Copies the elements in <code>xyz</code> to <code>this</code>.
	 * 
	 * @return this
	 */
	public Vec3 setValue(float[] xyz) {
		if (xyz.length != 3) {
			error("Vec3.setValue(float[] xyz): xyz is not length 3");
		}
		rep[0] = xyz[0];
		rep[1] = xyz[1];
		rep[2] = xyz[2];
		return this;
	}
	/**
	 * Copies the elements in <code>xyz</code> to <code>this</code>.
	 * 
	 * @return this
	 */
	public Vec3 setValue(double[] xyz) {
		if (xyz.length != 3) {
			error("Vec3.setValue(float[] xyz): xyz is not length 3");
		}
		rep[0] = (float) xyz[0];
		rep[1] = (float) xyz[1];
		rep[2] = (float) xyz[2];
		return this;
	}
	/**
	 * Copies the elements in <code>p</code> to <code>this</code>.
	 * 
	 * @return this
	 */
	public Vec3 setValue(Vec3 p) {
		rep[0] = p.rep[0];
		rep[1] = p.rep[1];
		rep[2] = p.rep[2];
		return this;
	}

	/* methods */
	/**
	 * Returns the square length of this vector.
	 */
	public float magSquared() {
		return rep[0] * rep[0] + rep[1] * rep[1] + rep[2] * rep[2];
	}
	/**
	 * Returns the length of this vector.
	 */
	public float mag() {
		return (float) Math.sqrt(magSquared());
	}

	/**
	 * Sets the magnitude of this vector to the given value. Equivalent to <br><code>this.normalize(); this.scale(newMag)</code>
	 */
	public void setMagnitude(float newMag) {
		normalize();
		scale(newMag);
	}

	/**
	 * Returns distance from otherPoint to this point. Equivalent to <code>otherPoint.sub(this).mag()</code>
	 */
	public float distanceFrom(Vec3 otherPoint) {
		float dx = otherPoint.rep[0]-rep[0];		
		float dy = otherPoint.rep[1]-rep[1];		
		float dz = otherPoint.rep[2]-rep[2];		
		return (float)Math.sqrt(dx*dx+dy*dy+dz*dz);
		
//		return (float) otherPoint.sub(this).mag();
	}

	/**
	 * Returns a normalized <B>copy <B>of this vector.
	 */
	public Vec3 direction() {
		Vec3 vdir = this.copy();
		vdir.normalize();

		return vdir;
	}
	/**
	 * Returns <code>this dot b</code>, the dot product.
	 */
	public float dot(Vec3 b) {
		return (rep[0] * b.rep[0] + rep[1] * b.rep[1] + rep[2] * b.rep[2]);
	}
	/**
	 * Returns the angle between <code>this</code> and <code>b</code>.
	 */
	public float angleBetween(Vec3 b) {
		double amag = this.mag();
		double bmag = b.mag();
		if (amag == 0.0 || bmag == 0.0)
			return 0.0f;

		double d = this.dot(b);
		d /= (amag * bmag);
		return (float) innards.math.BaseMath.acos(d);
	}

	/**
	 * Returns <b>a new Vec3 <b>the cross product <code>this cross b</code>
	 * <P>
	 * 
	 * @exception IllegalArgumentException
	 *                     if <code>this</code> or <code>b</code> is not of dimension 3.
	 */
	public Vec3 cross(Vec3 b) throws IllegalArgumentException {
		Vec3 out = new Vec3();
		cross(b, out);
		return out;
	}

	/**
	 * Calculates the cross product <code>out</code>=<code>this</code> cross <code>b</code>.
	 * 
	 * @return out - autoallocated on null
	 */
	public Vec3 cross(Vec3 b, Vec3 out) throws IllegalArgumentException {
		if (out == null)
			out = new Vec3();
		float x = rep[1] * b.rep[2] - rep[2] * b.rep[1];
		float y = -rep[0] * b.rep[2] + rep[2] * b.rep[0];
		float z = rep[0] * b.rep[1] - rep[1] * b.rep[0];

		out.rep[0] = x;
		out.rep[1] = y;
		out.rep[2] = z;
		return out;
	}
	/**
	 * Calculates the cross product out =<code>a</code> cross <code>b</code>.
	 * <P>
	 * It is acceptable for <code>a</code> to be <code>b</code> or <code>out</code>, or both.
	 * 
	 * @exception IllegalArgumentException
	 *                     if this or b is not of dimension 3.
	 * @return out - autoallocated on null
	 */
	static public Vec3 cross(Vec3 a, Vec3 b, Vec3 out) throws IllegalArgumentException {
		if (out == null)
			out = new Vec3();
		a.cross(b, out);
		return out;
	}

	/**
	 * Returns the triple product <code>a dot (b cross c)</code>. This is the (signed) volume of the parrelelpiped abc.
	 * <P>
	 * 
	 * Creates a temporary Vec3 in the process.
	 */

	static public float triple(Vec3 a, Vec3 b, Vec3 c) {
		Vec3 cr = b.cross(c, null);
		return cr.dot(a);
	}

	/**
	 * Removes the component of this vector that's parallel to <code>direction</code>, e.g:
	 * <P>
	 * <t><code>this = this - norm(direction) dot this</code>.
	 * 
	 * @return this
	 */
	public Vec3 projectOut(Vec3 direction) {
		float magDir = direction.mag();

		float dot = this.dot(direction) / (magDir * magDir);

		rep[0] = rep[0] - dot * direction.rep[0];
		rep[1] = rep[1] - dot * direction.rep[1];
		rep[2] = rep[2] - dot * direction.rep[2];
		return this;
	}
	/**
		 * Removes all but component of this vector that's parallel to <code>direction</code>, e.g:
		 * <P>
		 * <t><code>this = this - norm(direction) dot this</code>.
		 * 
		 * @return this
		 */
	public Vec3  projectOnto(Vec3 direction) {
		float d = this.dot(direction)/direction.mag();
		rep[0] = direction.rep[0]*d;
		rep[1] = direction.rep[1]*d;
		rep[2] = direction.rep[2]*d;
		return this;
	}

	/**
	 * Returns a String representation.
	 */
	public String toString() {
		String s = "[ " + rep[0] + " \t" + +rep[1] + " \t" + +rep[2] + " \t" + "]";
		return s;
	}

	/**
	 * d</code>
	 * 
	 * @return this;
	 */
	public Vec3 scale(float d) {
		rep[0] *= d;
		rep[1] *= d;
		rep[2] *= d;
		return this;
	}

	/**
	 * by</code>
	 * 
	 * @return to - autoallocated on null
	 */
	public static Vec3 scale(Vec3 from, float by, Vec3 to) {
		if (to == null)
			to = new Vec3();
		to.rep[0] = by * from.rep[0];
		to.rep[1] = by * from.rep[1];
		to.rep[2] = by * from.rep[2];
		return to;
	}
	/**
	 * Sets all elements in this vector to zero.
	 * 
	 * @return this
	 */
	public Vec3 zero() {
		scale(0.0f);
		return this;
	}

	/**
	 * Scales this vector to be unit length.
	 * 
	 * @return this
	 */
	public Vec3 normalize() {
		float d = mag();
		if (d != 0.0f)
			scale(1.0f / d);
		return this;
	}


    public Vec3 round() {
        Vec3 to = new Vec3 (Math.round(rep[0]), Math.round(rep[1]), Math.round(rep[2]));
        return to;
    }

	/**
	 * Returns true if all elements of <code>this</code> are equal to coresponding elements of <code>b</code>.
	 */
	public boolean equals(Vec3 b) {
		return ((rep[0] == b.rep[0]) && (rep[1] == b.rep[1]) && (rep[2] == b.rep[2]));
	}
	/**
	 * Returns <code>this + b</code>
	 * 
	 * @exception DimensionMismatchException
	 *                     for mismatch dimensions
	 */
	public Vec3 add(Vec3 b) {
		Vec3 out = new Vec3();
		add(this, b, out);
		return out;
	}
	/**
	 * Returns <code>this - b</code>
	 * 
	 * @exception DimensionMismatchException
	 *                     for mismatch dimensions
	 */
	public Vec3 sub(Vec3 b) {
		Vec3 out = new Vec3();
		sub(this, b, out);
		return out;
	}

	/**
	 * w + b
	 * <P>
	 * It is safe for <code>a == out</code> or <code>b == out</code> (or both).
	 * 
	 * @return out - autoallocated on null
	 */
	public static Vec3 add(Vec3 a, float w, Vec3 b, Vec3 out) {
		if (out == null)
			out = new Vec3();
		out.rep[0] = a.rep[0] * w + b.rep[0];
		out.rep[1] = a.rep[1] * w + b.rep[1];
		out.rep[2] = a.rep[2] * w + b.rep[2];
		return out;
	}

	/**
	 * out = a + b
	 * <P>
	 * It is safe for <code>a == out</code> or <code>b == out</code> (or both).
	 * 
	 * @return out - autoallocated on null
	 */
	public static Vec3 add(Vec3 a, Vec3 b, Vec3 out) {
		if (out == null)
			out = new Vec3();
		out.rep[0] = a.rep[0] + b.rep[0];
		out.rep[1] = a.rep[1] + b.rep[1];
		out.rep[2] = a.rep[2] + b.rep[2];
		return out;
	}

	/**
	 * out = a - b
	 * <P>
	 * It is safe for <code>a == out</code> or <code>b == out</code> (or both).
	 * 
	 * @return out - autoallocated on null
	 */
	public static Vec3 sub(Vec3 a, Vec3 b, Vec3 out) {
		if (out == null)
			out = new Vec3();
		out.rep[0] = a.rep[0] - b.rep[0];
		out.rep[1] = a.rep[1] - b.rep[1];
		out.rep[2] = a.rep[2] - b.rep[2];
		return out;
	}

	/**
	 * Performs a weighted linear interpolation between <code>from</code> and <code>to</code> and places the result in <code>out</code>.
	 * 
	 * @param from
	 *                The start point.
	 * @param alpha
	 *                The weighting. <code>alpha = 0</code> results in <code>out == from</code>.<code>alpha = 1</code> results in <code>out == to</code>.
	 * @return interp - autoallocated on null
	 */
	public static Vec3 lerp(Vec3 v1, Vec3 v2, float alpha, Vec3 interp) {
		if (interp == null)
			interp = new Vec3();
		float a1 = 1 - alpha;
		interp.setValue(a1 * v1.x() + alpha * v2.x(), a1 * v1.y() + alpha * v2.y(), a1 * v1.z() + alpha * v2.z());
		return interp;
	}

	/**
	 * This was copied from a deprecate method in Quaternion, but it seems like it should work?
	 * @param src
	 * @param dst output vec, should be perp to src when method completes if possible
	 * @return true if dst is now perp. to src
	 */
	public static boolean makePerpendicularTo(Vec3 src, Vec3 dst)
	{
		if ((src.x() == 0.0f) && (src.y() == 0.0f) && (src.z() == 0.0f))
			return false;
		if (src.x() != 0.0f)
		{
			if (src.y() != 0.0f)
			{
				dst.setValue(-src.y(), src.x(), 0.0f);
			} else
			{
				dst.setValue(-src.z(), 0.0f, src.x());
			}
		} else
		{
			dst.setValue(1.0f, 0.0f, 0.0f);
		}
		return true;
	}


	/**
	 * Convenience method to print to System.err
	 */
	protected static void error(String s) {
		System.err.println("(Vec3):" + s);
	}
	/**
	 * Convenience wrapper around <code>System.out.println(String s)</code>
	 */
	protected static void report(String s) {
		System.out.println("(Vec3):" + s);
	}

	/**
	 * Throws an Error.
	 */
	protected static void punt(String s) {
		error(s);
		throw new Error(s);
	}

	/**
	 * Test drive method.
	 */
	public static void main(String argh[]) {
		Vec3 a = new Vec3(1.0f, 0.0f, 0.0f);
		Vec3 b = new Vec3(0.0f, 1.0f, 0.0f);
		Vec3 cross = a.cross(b);
		report("a: " + a);
		report("b: " + b);
		report("a cross b: " + cross);
		report("a dot b: " + a.dot(b));
		report("angle between: " + a.angleBetween(b));
		report("a + b: " + a.add(b));
		report("a - b: " + a.sub(b));
		Vec3 d = a.copy();
		report("d = a.copy(): " + d);
		d.scale(2.0f);
		report("d.scale(2.0)" + d);
		report("d.mag() " + d.mag());
		report("d.magSquared() " + d.magSquared());
		d.normalize();
		report("d.normalize(): " + d);
		d.zero();
		report("d.zero(): " + d);
		d.setValue(1, 1, 1);
		report("d.setValue(1.0, 1.0, 1.0): " + d);
		report("d.mag(): " + d.mag());
		float[] foo = d.getValue();
		String s = "[";
		for (int i = 0; i < 3; i++)
			s = s + " " + foo[i];
		s = s + "]";
		report("d.getValue(): " + s);

		report("Testing copy...");
		Vec3 x = a.copy();
		report("a: " + a);
		report("x = a.copy(): " + x);
		a.set(0, 5);
		report("a.set(0, 5.0): a=: " + a);
		report("x should not have changed: " + x);
		punt("Testing punt, should cause fatal exception.");
	}
	/**
	 * replaces each element in _this_ vector with max(element, vec3.element). Returns this
	 */
	public void max(Vec3 vec3) {
		this.rep[0] = Math.max(vec3.rep[0], this.rep[0]);
		this.rep[1] = Math.max(vec3.rep[1], this.rep[1]);
		this.rep[2] = Math.max(vec3.rep[2], this.rep[2]);
	}
	/**
	 * replaces each element in _this_ vector with min(element, vec3.element).Returns this
	 */
	public void min(Vec3 vec3) {
		this.rep[0] = Math.min(vec3.rep[0], this.rep[0]);
		this.rep[1] = Math.min(vec3.rep[1], this.rep[1]);
		this.rep[2] = Math.min(vec3.rep[2], this.rep[2]);
	}

	/**
	 * checks that a vector doesn't have a non number component
	 */
	public boolean isNumber()
	{
		return !(
		        Float.isNaN(rep[0]) ||
		        Float.isNaN(rep[1]) ||
		        Float.isNaN(rep[2]) ||
		        Float.isInfinite(rep[0]) ||
		        Float.isInfinite(rep[1]) ||
		        Float.isInfinite(rep[2])
		        );
	}
	
	public static Vec3 fromString(String sVec) {
		float x = 0, y = 0, z = 0;
		String newStrc = sVec.replace('[', ' ').replace(']', ' ').trim();
		StringTokenizer st = new StringTokenizer(newStrc, "\t");
		int numsOfNums = 0;
		while(st.hasMoreTokens() && numsOfNums < 3) {
			String num = st.nextToken();
			switch(numsOfNums) {
			case 0:
				x = Float.parseFloat(num);
				break;
			case 1:
				y = Float.parseFloat(num);
				break;
			case 2:
				z = Float.parseFloat(num);
				break;
			}
			numsOfNums++;
		}
		return new Vec3(x, y, z);
	}

}
