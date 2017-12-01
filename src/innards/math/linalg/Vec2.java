package innards.math.linalg;

import java.io.Serializable;
import java.util.Arrays;

/**
  A 2-element vector.<P>

  Indices start at zero.
  */
public final class Vec2 implements Serializable, Cloneable
{
	private float[] rep= new float[2];

	/**
	   Creates a vector with elements set to zero.
	*/
	public Vec2()
	{

	}

	/**
	   Creates a vector with elements <code>[x y]</code>
	*/
	public Vec2(float x, float y)
	{
		setValue(x, y);
	}

	/**
	   Copies the elements of a 2-element Vec.
	*/
	public Vec2(Vec a)
	{
		if (a.dim() != 2)
		{
			error("Vec3(Vec): arg is not dim 2!");
		}
		else
		{
			rep[0]= a.ref(0);
			rep[1]= a.ref(1);
		}
	}
	/**
	   Copy constructor.
	*/
	public Vec2(Vec2 a)
	{
		rep[0]= a.rep[0];
		rep[1]= a.rep[1];
	}
	/**
	   Copies the first two elements of array <code>vec</code>.
	*/
	public Vec2(double[] vec)
	{
		rep[0]= (float)vec[0];
		rep[1]= (float)vec[1];
	}
	/**
	   Copy constructor. Copies the first two elements of vec.
	*/
	public Vec2(float[] vec)
	{
		rep[0]= vec[0];
		rep[1]= vec[1];
	}

	/**
	   Convenience wrapper for clone(). Saves the hassle of casting an Object to Vec2.
	*/
	public Vec2 copy()
	{
		return (Vec2) clone();
	}

	/**
	   Clones this Vec2.
	*/
	public Object clone()
	{
		Vec2 f= new Vec2();
		f.setValue(this);
		return f;
	}

	/** 
	    Returns the <code>i</code>th element.
	*/
	public float ref(int i)
	{
		return rep[i];
	}
	/** 
	    Returns the <code>i</code>th element.
	*/
	public float get(int i)
	{
		return rep[i];
	}
	/**
	   Returns true if <code>otherVec</code> is a <code>Vec</code>, and all elements of <code>this</code> are equal to coresponding elements of <code>otherVec</code>.
	*/
	public boolean equals(Object otherVec)
	{
		if (otherVec instanceof Vec2)
		{
			return equals((Vec2) otherVec);
		}
		else
			return false;
	}
	/**
	   Returns true if all elements of <code>this</code> are equal to coresponding elements of <code>vec</code>.
	*/
	public boolean equals(Vec2 vec)
	{
		return Arrays.equals(vec.rep, rep);
	}

	/**
	  Returns the counterclockwise angle with respect to the positive x axis. 
	*/
	public float getTheta()
	{
		return (float)Math.atan2(rep[1], rep[0]);
	}

	/**
	   Returns the first element.
	*/
	public float getX()
	{
		return rep[0];
	}
	/**
	   Returns the first element.
	*/
	public float x()
	{
		return rep[0];
	}
	/**
	   Returns the second element.
	*/
	public float getY()
	{
		return rep[1];
	}
	/**
	   Returns the second element.
	*/
	public float y()
	{
		return rep[1];
	}

	/** 
	    Returns a two-element array [x, y]
	*/
	public float[] getValue()
	{
		float[] out= new float[2];
		out[0]= rep[0];
		out[1]= rep[1];
		return out;
	}

	/** 
	    Sets the <code>i</code>th element to <code>d</code> 
	*/
	public void set(int i, float d)
	{
		rep[i]= d;
	}

	/**
	   Sets the first element to <code>d</code>
	*/
	public void setX(float d)
	{
		rep[0]= d;
	}

	/**
	   Sets the second element to <code>d</code>
	*/
	public void setY(float d)
	{
		rep[1]= d;
	}

	/**
	   Sets this Vec2's two elements to <code>x</code> and <code>y</code>.
	*/
	public void setValue(float x, float y)
	{
		rep[0]= x;
		rep[1]= y;
	}

	/** 
	    Copies the elements in <code>xyz</code> into <code>this</code>.
	*/
	public void setValue(double[] xyz)
	{
		if (xyz.length != 2)
		{
			error("Vec2.setValue(double[] xyz): xyz is not length 2");
		}
		rep[0]=(float) xyz[0];
		rep[1]=(float) xyz[1];
	}
	/** 
	    Copies the data in <code>xyz</code> to <code>this</code>.
	*/
	public void setValue(float[] xyz)
	{
		if (xyz.length != 2)
		{
			error("Vec2.setValue(float[] xyz): xyz is not length 2");
		}
		rep[0]= xyz[0];
		rep[1]= xyz[1];
	}
	/** 
	    Copies the data in <code>p</code> to <code>this</code>.
	*/
	public void setValue(Vec2 p)
	{
		rep[0]= p.rep[0];
		rep[1]= p.rep[1];
	}

	/**
	    Sets the angle of the vector in polar coordinates, leaving magnitude unchanged.
	*/
	public void setTheta(float theta)
	{
		float mag= mag();

		rep[0]= mag * (float)Math.cos(theta);
		rep[1]= mag * (float)Math.sin(theta);
	}

	/** 
	Sets the values of this vector using polar coordinates.
	*/
	public void setPolar(float r, float theta)
	{
		rep[0]= r * (float)Math.cos(theta);
		rep[1]= r * (float)Math.sin(theta);
	}

	/**
	   Rotates this vector counterclockwise by theta.
	*/

	public void rotateBy(float theta)
	{
		float r0= rep[0] * (float)Math.cos(theta) - rep[1] * (float)Math.sin(theta);
		rep[1]= rep[0] *(float) Math.sin(theta) + rep[1] * (float)Math.cos(theta);
		rep[0]= r0;
	}

	/* methods */

	/**
	   Returns the square length of this vector.
	*/
	public float magSquared()
	{
		return rep[0] * rep[0] + rep[1] * rep[1];
	}

	/**
	   Returns the length of this vector.
	*/
	public float mag()
	{
		return (float)Math.sqrt(magSquared());
	}

	/** 
	    Sets the length of this vector to the given value. Equivalent to <br>
	    <code>this.normalize(); this.scale(newMag)</code>
	*/
	public void setMagnitude(float newMag)
	{
		normalize();
		scale(newMag);
	}

	/** 
	    Returns distance from otherPoint to this point. Equivalent to
	    <code>otherPoint.sub(this).mag()</code>  
	*/
	public float distanceFrom(Vec2 otherPoint)
	{
		return otherPoint.sub(this).mag();
	}

	/**
	   Returns a normalized version of this vector.
	*/
	public Vec2 direction()
	{
		Vec2 vdir= this.copy();
		vdir.normalize();

		return vdir;
	}

	/**
	   Returns <code>this dot b</code>, the dot product.
	*/
	public float dot(Vec2 b)
	{
		return (rep[0] * b.rep[0] + rep[1] * b.rep[1]);
	}

	/**
	   Returns the angle between <code>this</code> and <code>b</code>.
	*/
	public float angleBetween(Vec2 b)
	{
		float amag= this.mag();
		float bmag= b.mag();
		if (amag == 0.0f || bmag == 0.0f)
			return 0.0f;

		float d= this.dot(b);
		d /= (amag * bmag);
		return(float) innards.math.BaseMath.acos(d);
	}

	/**
	   Same as angleBetween(), except that if the closest rotation from <code>this</code> 
	   to <code>b</code> is clockwise, the returned angle is negative.
	   @see #angleBetween(Vec2)
	*/

	public float signedAngleBetween(Vec2 b)
	{
		float angle= angleBetween(b);

		// 2d 'cross' product z component

		float z= this.rep[0] * b.rep[1] - this.rep[1] * b.rep[0];

		return angle * (z < 0 ? -1 : 1);
	}

	/**
	   Returns a string representation.
	*/
	public String toString()
	{
		String s= "[ " + rep[0] + " \t" + +rep[1] + " \t" + "]";
		return s;
	}

	/**
	   Scales this vector by <code>d</code>.
	*/
	public void scale(float d)
	{
		rep[0] *= d;
		rep[1] *= d;
	}

	/**
	   Sets the elements of this vector to zero.
	*/
	public void zero()
	{
		scale(0.0f);
	}

	/**
	   Normalizes this vector.
	*/
	public void normalize()
	{
		float d= mag();
		if (d != 0.0f)
			scale(1.0f / d);
	}

	/**
	   Returns <code>this + b</code>
	*/
	public Vec2 add(Vec2 b)
	{
		Vec2 out= new Vec2();
		add(this, b, out);
		return out;
	}
	/**
	   Returns <code>this - b</code>
	*/
	public Vec2 sub(Vec2 b)
	{
		Vec2 out= new Vec2();
		sub(this, b, out);
		return out;
	}

	/**
	   Performs a weighted linear interpolation between <code>from</code> and <code>to</code> and places the result in <code>out</code>.
	   @param from The start point.
	   @param alpha The weighting. <code>alpha = 0</code> results in <code>out == from</code>. <code>alpha = 1</code> results in <code>out == to</code>.
	*/
	public static void lerp(Vec2 v1, Vec2 v2, float alpha, Vec2 interp)
	{
		float a1= 1.0f - alpha;
		interp.setValue(a1 * v1.x() + alpha * v2.x(), a1 * v1.y() + alpha * v2.y());
	}

	/**
	   Adds <code>a</code> and <code>b</code> and stores the sum in <code>out</code>.
	   It is safe for <code>a == out</code> or <code>b == out</code> (or both).
	*/
	public static void add(Vec2 a, Vec2 b, float w, Vec2 out)
	{
		out.rep[0]= a.rep[0] + w * b.rep[0];
		out.rep[1]= a.rep[1] + w * b.rep[1];
	}

	/**
	   <code>out = a + b*w</code>
	*/
	public static void add(Vec2 a, float w, Vec2 b, Vec2 out)
	{
		add(a, b, w, out);
	}

	/**
	   <code>out = a + b</code>
	 */
	public static void add(Vec2 a, Vec2 b, Vec2 out)
	{
		out.rep[0]= a.rep[0] + b.rep[0];
		out.rep[1]= a.rep[1] + b.rep[1];
	}

	/**
	  Calculates <code>a - b</code> and stores into <code>out</code>.
	*/
	public static void sub(Vec2 a, Vec2 b, Vec2 out)
	{
		out.rep[0]= a.rep[0] - b.rep[0];
		out.rep[1]= a.rep[1] - b.rep[1];
	}

	/** 
	Convenience method to print to System.err
	*/
	protected static void error(String s)
	{
		System.err.println("(Vec2):" + s);
	}
	/**
	   Convenience wrapper around <code>System.out.println(String s)</code>
	*/
	protected static void report(String s)
	{
		System.out.println("(Vec2):" + s);
	}

	/**
	   Throws an Error.
	*/
	protected static void punt(String s)
	{
		error(s);
		throw new Error(s);
	}

	/**
	   Test drive method.
	*/
	public static void main(String argh[])
	{
		Vec2 a= new Vec2(1.0f, 0.0f);
		Vec2 b= new Vec2(0.0f, 1.0f);
		report("a: " + a);
		report("b: " + b);
		report("a dot b: " + a.dot(b));
		report("angle between: " + a.angleBetween(b));
		report("a + b: " + a.add(b));
		report("a - b: " + a.sub(b));
		Vec2 d= a.copy();
		report("d = a.copy(): " + d);
		d.scale(2.0f);
		report("d.scale(2.0f)" + d);
		report("d.mag() " + d.mag());
		report("d.magSquared() " + d.magSquared());
		d.normalize();
		report("d.normalize(): " + d);
		d.zero();
		report("d.zero(): " + d);
		d.setValue(1.0f, 1.0f);
		report("d.setValue(1.0f, 1.0f): " + d);
		report("d.mag(): " + d.mag());
		float[] foo= d.getValue();
		String s= "[";
		for (int i= 0; i < 3; i++)
			s= s + " " + foo[i];
		s= s + "]";
		report("d.getValue(): " + s);

		report("Testing copy...");
		Vec2 x= a.copy();
		report("a: " + a);
		report("x = a.copy(): " + x);
		a.set(0, 5.0f);
		report("a.set(0, 5.0f): a=: " + a);
		report("x should not have changed: " + x);
		punt("Testing punt, should cause fatal exception.");
	}

}
