package innards.math.linalg;

import java.io.*;
import java.io.Serializable;
import java.util.*;

import innards.debug.Debug;
import innards.math.BaseMath;
import innards.math.random.UniformRandomVariable;

/**
   Represents a quaternion, and provides methods for operations on the quaternion group. <b>Note:</b> some methods only operate on some subset of that group (such as the unit quaternions.) These will be made explicit.<P>

  In the documentation for this class, "H" represents the full quaternion group which allows adds, and "Q" is used for the unit quaternions.  We will use R^3 to denote
  the pure quaternions (quaternions whose scalar components are zero.) <P>

  Strictly speaking, this is not exactly right, since it's actually a bivector. Although the vector is dual, the imaginaries are implicitly dealt with in the operators.<P>

  Quick terminology glossary:<P>
  <ul>
  <li>  H: Full quaternion group of any magnitude except zero.
  <li>  Q: Unit quaternions, Q subset of H.
  <li>  SO(3): Special Orthogonal 3x3 matrices, the rotation group in R^3.
  <li>  R^n: Euclidean n-space
  <li>  S^3: A hypersphere of 3 internal DOF embedded in R^4.
  <li>  w: The scalar component of a quaternion.
  <li>  x, y, z: the three elements of the vector component of a quaternion.
  </ul>
  @author Michael Patrick Johnson <aries@media.mit.edu>
 */

public final class Quaternion implements Cloneable, Serializable
{
	private float[] rep= new float[4];

	transient private float[] rep1= rep;
	transient private float[] rep2= null;
	private int rep_ptr= 0;

	private static final int W= 0;
	private static final int X= 1;
	private static final int Y= 2;
	private static final int Z= 3;
	private static final double EPSILON= 1.0e-6;

	private Vec3 tempVec3= null;

	/** Creates the identity quaternion, {1, 0, 0, 0}. */
	public Quaternion()
	{
		this(1.0f, 0.0f, 0.0f, 0.0f);
		rep1 = rep;
	}

	/** 
	    Constructs a quaternion using <code>w</code> as the scalar part and <code>[x, y, z]</code> as the vector part. 
	 @param w The scalar component.
	 @param x The vector component's first element.
	 @param y The vector component's second element.
	 @param z The vector component's third element.
	 */
	public Quaternion(double w, double x, double y, double z)
	{
		rep1 = rep;
		setValue(w, x, y, z);
		initSwapBuffer();
	}
	/** 
	    Constructs a quaternion using <code>w</code> as the scalar part and <code>[x, y, z]</code> as the vector part. 
	 @param w The scalar component.
	 @param x The vector component's first element.
	 @param y The vector component's second element.
	 @param z The vector component's third element.
	 */
	public Quaternion(float w, float x, float y, float z)
	{
		rep1 = rep;
		setValue(w, x, y, z);
		initSwapBuffer();
	}

	/**
	   Constructs a quaternion that represents a rotation by an <code>angle</code> around an
	   <code>axis</code>. ( Not unique; negating both <code>axis</code> and <code>angle</code>
	   results in the same rotation. )
	   <P>
	   Creates <code>q = cos(angle/2) + sin(angle/2) (axis / ||axis||)</code>.
	 */
	public Quaternion(Vec3 axis, double angle)
	{
		rep1 = rep;
		fromAxisAngle(axis, angle);
		initSwapBuffer();
	}

	/**
	   Copy constructor.
	 */
	public Quaternion(Quaternion q)
	{
		rep1 = rep;
		fromQuaternion(q);
		initSwapBuffer();
	}

	/**
	   Creates a Quaternion from a Rotation object.
	 */
	public Quaternion(Rotation r)
	{
		rep1 = rep;
		r.toQuaternion(this);
		initSwapBuffer();
	}

	/**
	    Creates the quaternion that rotates vector <code>from</code> to vector <code>to</code>.
	    That is, <code>q.rotateVec(from) == to</code> (modulo magnitude).
	    <P>
	    <B>Undefined</B> for (from == -to)!   from == to is fine.
	 */
	public Quaternion(Vec3 from, Vec3 to)
	{
		rep1 = rep;
		fromFromTo(from, to);
		initSwapBuffer();
	}

	/**
	    Constructs from a Vec with elements [ w x y z ].
	    @see #Quaternion(double, double, double, double)
	 */
	public Quaternion(Vec v)
	{
		rep1 = rep;
		setValue(v.get(0), v.get(1), v.get(2), v.get(3));
		initSwapBuffer();
	}

	/**
	    Makes a pure imaginary quaterion (w = 0) from a Vec3 with elements [x y z].
	 */
	public Quaternion(Vec3 v)
	{
		rep1 = rep;
		fromVec3(v);
		initSwapBuffer();
	}

	/**
	   Interprets a double array as a rotation matrix 
	   (<code>mat[row index][column index]</code>), and makes a quaternion from it. 
	   If mat is larger than 3x3, only its upper-left 3x3 elements are read. 
	   Assumes the rotation acts on column vectors, ie y = Rx.
	   <P>
	   Behavior is <B>undefined</B> if the matrix isn't really a rotation matrix. 
	 */
	public Quaternion(double[][] mat)
	{
		rep1 = rep;

		// this is scary, but hey, it's math, it needs to be fast.

		double tr, s;

		tr= mat[0][0] + mat[1][1] + mat[2][2];
		if (tr > 0.0)
		{
			s= Math.sqrt(tr + 1.0);
			rep[W]= (float) (s * 0.5);
			s= 0.5 / s;

			rep[X]= (float) (s * (mat[2][1] - mat[1][2]));
			rep[Y]= (float) (s * (mat[0][2] - mat[2][0]));
			rep[Z]= (float) (s * (mat[1][0] - mat[0][1]));
		} else
		{
			int[] nxt= { 1, 2, 0 };
			int i, j, k;
			// translated from Watt who uses 3 as the scalar slot,
			// and thinks his code is elegant cuz he uses X Y and Z and not 0 1 2
			// bozo.  :)
			i= 0;
			if (mat[1][1] > mat[0][0])
				i= 1;
			if (mat[2][2] > mat[i][i])
				i= 2;
			j= nxt[i];
			k= nxt[j];
			s= Math.sqrt((mat[i][i] - (mat[j][j] + mat[k][k])) + 1.0);
			// the i+1 in the rep deref
			// is cuz we define the rep differently than watt
			rep[i + 1]= (float) (s * 0.5);
			s= 0.5 / s;
			rep[W]= (float) (s * (mat[k][j] - mat[j][k]));
			rep[j + 1]= (float) (s * (mat[j][i] + mat[i][j]));
			rep[k + 1]= (float) (s * (mat[k][i] + mat[i][k]));
		}

	}

	/**
	   Constructs a Quaternion from a rotation matrix. If the matrix is larger than 3x3, only 
	   its upper-left 3x3 elements are read. 
	   Assumes the rotation acts on column vectors, ie y = Rx.
	   <P>
	   Behavior is <B>undefined</B> if the matrix isn't really a rotation matrix. 
	   @see #fromMatrix
	 */
	public Quaternion(Matrix m)
	{
		rep1 = rep;
		fromMatrix(m);
	}

	/**
	   Returns <code>"[ w:" + w() + " x:" + x() + " y:" + y() + " z:" + z() + "]" </code>
	 */
	public String toString()
	{
		return "[ w:" + w() + " x:" + x() + " y:" + y() + " z:" + z() + "]";
	}

	/**
	   Sets the quaternion to represent the rotation by <code>angle</code> around 
	   <code>axis</code>. Note that negating both the axis and angle results in the same 
	   quaternion on the opposite side of the sphere.
	   <P>
	   Explicitly, q = cos(angle/2) + sin(angle/2) (axis / ||axis||).

	    @see #Quaternion(Vec3, double)
	 */
	public void setValue(Vec3 axis, double angle)
	{
		fromAxisAngle(axis, angle);
	}

	/**
	   Sets this quaternion to represent the rotation in <code>r</code>.
	    @see #Quaternion(Rotation)
	 */
	public void setValue(Rotation r)
	{
		r.toQuaternion(this);
	}

	/** 
	    Copies q into this.
	 */
	public void setValue(Quaternion q)
	{
		rep[W]= q.w();
		rep[X]= q.x();
		rep[Y]= q.y();
		rep[Z]= q.z();
	}

	/** 
	    Sets the scalar part of this quaternion to <code>w</code> and the vector part to <code>[x, y, z]</code>.
	 */
	public void setValue(double w, double x, double y, double z)
	{
		rep[W]= (float) w;
		rep[X]= (float) x;
		rep[Y]= (float) y;
		rep[Z]= (float) z;
	}
	/** 
	    Sets the scalar part of this quaternion to <code>w</code> and the vector part to <code>[x, y, z]</code>.
	 */
	public void setValue(float w, float x, float y, float z)
	{
		rep[W]= (float) w;
		rep[X]= (float) x;
		rep[Y]= (float) y;
		rep[Z]= (float) z;
	}

	/**
	    Sets the i_th component.
	    <P>
	    The internal order of the quaternion components is considered to be w = q_0, x,y,z = q_1,2,3.
	    <P>
	    One should use the convenience operators for setting components by name rather than index if possible.
	 */
	public void set(int i, double v)
	{
		rep[i]= (float) v;
	}
	/**
	    Sets the i_th component.
	    <P>
	    The internal order of the quaternion components is considered to be w = q_0, x,y,z = q_1,2,3.
	    <P>
	    One should use the convenience operators for setting components by name rather than index if possible.
	 */
	public void set(int i, float v)
	{
		rep[i]= v;
	}

	/**
	   Sets the scalar component.
	 */
	public void setW(float w)
	{
		rep[W]= w;
	}

	/** 
	Sets the first element of the vector component.
	 */
	public void setX(float x)
	{
		rep[X]= x;
	}

	/** 
	Sets the second element of the vector component.
	 */
	public void setY(float y)
	{
		rep[Y]= y;
	}

	/** 
	Sets the third element of the vector component.
	 */
	public void setZ(float z)
	{
		rep[Z]= z;
	}

	/**
	    Copies q into this.
	 */
	public void fromQuaternion(Quaternion q)
	{
		System.arraycopy(q.rep, 0, rep, 0, 4);
	}

	/**
	    Inits this quaternion from the argument array.
	    @param wxyz the components of the quaternion in order.
	 */
	public void fromQuaternion(double[] wxyz)
	{
		rep[W]= (float) wxyz[0];
		rep[X]= (float) wxyz[1];
		rep[Y]= (float) wxyz[2];
		rep[Z]= (float) wxyz[3];
	}

	/**
	    Inits this quaternion from the argument array.
	    @param wxyz the components of the quaternion in order
	 */
	public void fromQuaternion(float[] wxyz)
	{
		rep[W]= wxyz[0];
		rep[X]= wxyz[1];
		rep[Y]= wxyz[2];
		rep[Z]= wxyz[3];
	}

	/**
	    Writes the quaternion into the argument array as <code>{w,x,y,z}</code>.
	    @param qout the argument array.  This must be at least 4 long.
	 */
	public void toQuaternion(double[] qout)
	{
		qout[0]= w();
		qout[1]= x();
		qout[2]= y();
		qout[3]= z();
	}

	/**
	    Writes the quaternion into the argument array as code>{w,x,y,z}</code>.
	    @param qout the argument array.  This must be at least 4 long.
	 */
	public void toQuaternion(float[] qout)
	{
		qout[0]= (float) w();
		qout[1]= (float) x();
		qout[2]= (float) y();
		qout[3]= (float) z();
	}

	/**
	    Normalizes this quaternion (so that <code>w*w + x*x + y*y + z*z = 1.0</code>)<P>
	    <B>Undefined</B> for zero quaternions.
	    @throws QuaternionMathException if the argument has zero mag().
	 */
	public void normalize()
	{
		double m= mag();
		if (m == 0.0)
			throw new QuaternionMathException("Cannot normalize quaternion with zero magnitude.");
		else
			scale(1.0 / m);
	}

	/* Some convenient accessors */
	/**
	   Returns x.
	 */
	public float x()
	{
		return rep[X];
	}
	/**
	   Returns y.
	 */
	public float y()
	{
		return rep[Y];
	}
	/**
	   Returns z.
	 */
	public float z()
	{
		return rep[Z];
	}
	/**
	   Returns w.
	 */
	public float w()
	{
		return rep[W];
	}
	/**
	   Returns the i-th element, where the elements are ordered as [w x y z].
	 */
	public float get(int i)
	{
		return rep[i];
	}

	/**
	    ref() has been deprecated.  Please use get().
	    @deprecated
	 */
	public float ref(int i)
	{
		return rep[i];
	}

	/**
	   Returns true if corresponding elements of <code>this</code> and <code>b</code> are equal.
	 */
	public boolean equals(Quaternion b)
	{
		return ((rep[0] == b.rep[0]) && (rep[1] == b.rep[1]) && (rep[2] == b.rep[2]) && (rep[3] == b.rep[3]));
	}

	/**
	    Adds quaternions: <code>qout = q1 + q2</code>. <P>
	    <b>Note:</b> adding quaternions is not the same as concatenating rotations (see <code>mult</code> for that.) This method is defined over H.
	    @param q1 First quat.
	    @param q2 Second quat.
	    @param qout Quat in H.
	 */
	public static void add(Quaternion q1, Quaternion q2, Quaternion qout)
	{
		int i;
		for (i= 0; i < 4; i++)
			qout.rep[i]= q1.rep[i] + q2.rep[i];
	}

	/**
	    Subtracts the quaternions: <code>qout = q1 - q2</code>. This method is defined over H.
	    @param q1 Quat to subtract from.
	    @param q2 Quat to subtract.
	    @param qout Quat in H. 
	 */
	public static void sub(Quaternion q1, Quaternion q2, Quaternion qout)
	{
		int i;
		for (i= 0; i < 4; i++)
			qout.rep[i]= q1.rep[i] - q2.rep[i];
	}

	/**
	    Performs <code>qout = q1 * w + q2</code> (weighted addition.) <code>q1</code>, <code>q2</code> are unchanged by this method.
	    <P>
	    <B>Note:</B> Adding quaternions is not the same thing as concatenating rotations. Use <code>mult</code> for that.
	    @author marcd
	 */
	public static void add(Quaternion q1, float weight, Quaternion q2, Quaternion qout)
	{
		int i;
		for (i= 0; i < 4; i++)
			qout.rep[i]= q1.rep[i] * weight + q2.rep[i];
	}

	/** 
	    Multiplies the args and returns result in qout; qout = q1 q2.
	    <p>
	    For composing rotations, recall that the quaternion rotation operator associates right to left, so that the product q2 q1 rotates a vector by q1, then q2.
	 */
	public static void mult(Quaternion q1, Quaternion q2, Quaternion qout)
	{
		if (q1 == qout || q2 == qout)
		{
			throw new InPlaceMatrixMultException("cannot multiply quaternions in place!!! Garbage!");
		}

		internalQuatMult(q1.rep, q2.rep, qout.rep);
	}

	/**
	    Left-multiplies by q, <B>stores the result in this</B>, 
	    then returns this.
	    <P>
	    this <- q * this
	    <P>
	    DOES NOT MAKE A NEW OBJECT.
	    MUTATES this.
	    DOES NOT MUTATE q.
	    @return this
	 */
	public Quaternion concatLeft(Quaternion q)
	{
		internalQuatMult(q.rep, this.rep, getOtherRep());
		swapReps();
		return this;
	}

	/**
	    Right-multiplies by q, <B>stores the result in this</B>, 
	    then returns this.
	    <P>
	    this <- this * q
	    <P>
	    DOES NOT MAKE A NEW OBJECT.
	    MUTATES this.
	    DOES NOT MUTATE q.
	    @return this
	 */
	public Quaternion concatRight(Quaternion q)
	{
		internalQuatMult(this.rep, q.rep, getOtherRep());
		swapReps();
		return this;
	}

	/**
	    @deprecated This version has been deprecated since it was not
	    threadsafe.  I have made it threadsafe at the expense of it
	    making garbage.  Please use the non-static version of
	    rotateVec(Vec3, Vec3) which is still not instance threadsafe,
	    but closer, and garbage-free.  
	 */
	public static void rotateVec(Vec3 v_in, Quaternion q, Vec3 v_out)
	{
		Quaternion temp1= new Quaternion(v_in);
		Quaternion temp2= new Quaternion(v_out);

		mult(q, temp1, temp2);
		q.inverse();
		mult(temp2, q, temp1);
		// put it back
		q.inverse();

		//copy back
		temp1.getVector(v_out);
	}

	// temp variables for internal caluclations to avoid garbage.
	// trades space per quat for speed of garbage.

	// static by marc, didn't mean to check it in
	static private float[] temp_quat1= new float[4];
	static private float[] temp_quat2= new float[4];

	/**
	    Rotates the vector v through this quaternion  and puts the result in out.
	    Let this quaternion be called q.  Then v_out = q v_in q^inverse.
	 */
	public void rotateVec(Vec3 v_in, Vec3 v_out)
	{
		// tempQuat.fromVec3(v_in);
		temp_quat1[0]= 0.0f;
		temp_quat1[1]= v_in.x();
		temp_quat1[2]= v_in.y();
		temp_quat1[3]= v_in.z();

		// this double inverse could be sped up -aries
		internalQuatMult(this.rep, temp_quat1, temp_quat2);
		this.inverse();
		internalQuatMult(temp_quat2, this.rep, temp_quat1);
		// put it back
		this.inverse();

		//copy back
		// tempQuat.getVector(v_out);
		v_out.setValue(temp_quat1[1], temp_quat1[2], temp_quat1[3]);
	}

	/**
	    Mutates v by rotating it with this quaternion.
	    v <- q v q^inverse
	    <P>
	    Garbage free.
	 */
	public void rotateVec(Vec3 v)
	{
		// tempQuat.fromVec3(v_in);
		temp_quat1[0]= 0.0f;
		temp_quat1[1]= v.x();
		temp_quat1[2]= v.y();
		temp_quat1[3]= v.z();

		internalQuatMult(this.rep, temp_quat1, temp_quat2);
		this.inverse();
		internalQuatMult(temp_quat2, this.rep, temp_quat1);
		// put it back
		this.inverse();

		//copy back
		// tempQuat.getVector(v_out);
		v.setValue(temp_quat1[1], temp_quat1[2], temp_quat1[3]);
	}

	/**
	 *
	 * transforms qIn into this system. e.g. returns
	 * qOut = this^inverse qIn this
	 */
	public void transformInto(Quaternion qIn, Quaternion qOut)
	{
		this.inverse();
		internalQuatMult(this.rep, qIn.rep, temp_quat2);
		this.inverse();
		internalQuatMult(temp_quat2, this.rep, qOut.rep);
	}

	/**
	    Negates the quaternion q to produce -q, which is same rotation in SO(3),
	    but better for some slerps
	 */
	public void negate()
	{
		rep[0] *= -1.0;
		rep[1] *= -1.0;
		rep[2] *= -1.0;
		rep[3] *= -1.0;
	}

	/**
	    Puts the inverse of the quaternion into <code>qinv</code>
	    Has to divide out the magnitude to ensure that it works over all quaternions.
	    If q is known to be unit, then use <code>conjugate</code>, since the conjugate is the inverse for unit q.
	    <P>
	    @see #conjugate()
	 */
	public static void inverse(Quaternion q, Quaternion qinv)
	{
		double mag= q.mag();
		if (mag == 0.0)
			throw new QuaternionMathException("Inverse of zero quaternion does not exist");

		qinv.rep[W]= q.rep[W];
		qinv.rep[X]= -q.rep[X];
		qinv.rep[Y]= -q.rep[Y];
		qinv.rep[Z]= -q.rep[Z];

		qinv.scale(1.0 / mag);
	}

	/** 
	    Inverts this quaternion.
	 */
	public void inverse()
	{
		inverse(this, this);
	}

	/**
	    Same as inverse(), but some people like to use the active verb as the mutating version.
	 */
	public void invert()
	{
		inverse(this, this);
	}

	/**
	    @deprecated
	    setIdentity is preferred now.
	    @see #setIdentity()
	 */
	public void makeIdentity()
	{
		setValue(1.0, 0.0, 0.0, 0.0);
	}

	/**
	    Sets the quaternion to be the identity unit quaternion, {1, 0, 0, 0}.
	 */
	public void setIdentity()
	{
		makeIdentity();
	}

	/**
	    Writes the conjugate of quaternion <code>q</code> to <code>qout</code>.<P>
	    Let <P>
	    q = (w,x,y,z).<P>
	    Then<P>
	    qconj = (w,-x,-y,-z).
	 */
	public static void conjugate(Quaternion q, Quaternion qconj)
	{
		qconj.rep[W]= q.rep[W];
		qconj.rep[X]= -q.rep[X];
		qconj.rep[Y]= -q.rep[Y];
		qconj.rep[Z]= -q.rep[Z];
	}

	/**
	    Conjugates this quaternion.
	 */
	public void conjugate()
	{
		conjugate(this, this);
	}

	/**
	    Scales this quaternion by s.
	 */
	public void scale(double s)
	{
		rep[W] *= s;
		rep[X] *= s;
		rep[Y] *= s;
		rep[Z] *= s;
	}

	/**
	   Changes this into the zero quaternion
	 */
	public void zero()
	{
		scale(0.0);
	}

	/**
	    Sets this from a Vec containing at least 4 elements, assuming  v = [w x y z ...]
	    @param v Vec(4) containing [w, x, y, z, ...]
	 */
	public void fromVec(Vec v)
	{
		rep[W]= v.get(0);
		rep[X]= v.get(1);
		rep[Y]= v.get(2);
		rep[Z]= v.get(3);
	}

	/**
	    Returns this quaternion's components as a four-element Vec, [w x y z].
	    @return A Vec containing [w,x,y,z] of this.
	 */
	public Vec toVec()
	{
		Vec v= new Vec(4);
		toVec(v);
		return v;
	}

	/**
	    Puts this quaternion's components into the first four elements of <code>v</code> in the order w, x, y, z.
	    @param v Vec which must be of length >= 4.
	 */
	public void toVec(Vec v)
	{
		if (v.dim() != 4)
			throw new ArrayIndexOutOfBoundsException("quaternion needs Vec(4)");
		v.set(0, rep[0]);
		v.set(1, rep[1]);
		v.set(2, rep[2]);
		v.set(3, rep[3]);
	}

	/**
	    Interprets the vector v as a pure imaginary quaternion (zero scalar).
	    this <- (0, v.x(), v.y(), v.z().
	 */
	public void fromVec3(Vec3 v)
	{
		rep[W]= 0.0f;
		rep[X]= v.x();
		rep[Y]= v.y();
		rep[Z]= v.z();
	}

	/**
	    Copies the vector part of the quaternion (imaginary part)
	    to the argument v.
	    @param v contains [x,y,z] of quaternion on return.
	 */
	public void toVec3(Vec3 v)
	{
		getVector(v);
	}

	/**
	    Returns the scalar part of the quaternion, q.w().
	    Added to be consistent with toVec.
	    @see #toVec(Vec)
	    @see #toVec3(Vec3)
	 */
	public double getScalar()
	{
		return rep[W];
	}

	/**
	   Copies the vector part of the quaternion in v as [x,y,z]
	 */
	public void getVector(Vec3 v)
	{
		v.setValue(rep[X], rep[Y], rep[Z]);
	}

	/**
	    Converts the quaternion into a rotation matrix in SO(3).
	    Fills in the upper 3x3 of m with this matrix.
	    Note that this is designed to be a column vector rotation matrix.
	    ie. y = R x, where R is in SO(3) and x and y are in R(3).
	 */
	public void toMatrix(Matrix m)
	{

		double s= 2.0 / mag();
		double xs= s * x();
		double ys= s * y();
		double zs= s * z();
		double wx= w() * xs;
		double wy= w() * ys;
		double wz= w() * zs;
		double xx= x() * xs;
		double xy= x() * ys;
		double xz= x() * zs;
		double yy= y() * ys;
		double yz= y() * zs;
		double zz= z() * zs;

		// these are transposed in place from Watt to make matrices column vector
		m.set(0, 0, 1.0 - (yy + zz));
		m.set(1, 0, xy + wz);
		m.set(2, 0, xz - wy);

		m.set(0, 1, xy - wz);
		m.set(1, 1, 1.0 - (xx + zz));
		m.set(2, 1, yz + wx);

		m.set(0, 2, xz + wy);
		m.set(1, 2, yz - wx);
		m.set(2, 2, 1.0 - (xx + yy));

		// this is a Rotation, so the other fields must be valid.
	}

	public void toMatrixArray(double[][] array) {
		double s= 2.0 / mag();
		double xs= s * x();
		double ys= s * y();
		double zs= s * z();
		double wx= w() * xs;
		double wy= w() * ys;
		double wz= w() * zs;
		double xx= x() * xs;
		double xy= x() * ys;
		double xz= x() * zs;
		double yy= y() * ys;
		double yz= y() * zs;
		double zz= z() * zs;

		array[0][0] = 1.0 - (yy + zz);
		array[1][0] = xy + wz;
		array[2][0] = xz - wy;

		array[0][1] = xy - wz;
		array[1][1] = 1.0 - (xx + zz);
		array[2][1] = yz + wx;

		array[0][2] = xz + wy;
		array[1][2] = yz - wx;
		array[2][2] = 1.0 - (xx + yy);
	}

	/**
	    Loads the quaternion from a 3x3 or 4x4 rotation matrix (assumes the top left of the 4x4 is the rotation.)
	    <P>
	    Does not mutate argument.
	    Assumes y = R x defines the rotation.
	 */
	public void fromMatrix(Matrix rot)
	{
		if (rot.numRows() < 3 || rot.numRows() > 4 || rot.numColumns() < 3 || rot.numColumns() > 4)
			throw new DimensionMismatchException("Can only set quaternion from 3x3 or 4x4 matrix");

		// this is scary, but hey, it's math, it needs to be fast.
		double[][] mat= rot.rep;

		double tr, s;

		tr= mat[0][0] + mat[1][1] + mat[2][2];
		if (tr > 0.0)
		{
			s= Math.sqrt(tr + 1.0);
			rep[W]= (float) (s * 0.5);
			s= 0.5 / s;

			rep[X]= (float) (s * (mat[2][1] - mat[1][2]));
			rep[Y]= (float) (s * (mat[0][2] - mat[2][0]));
			rep[Z]= (float) (s * (mat[1][0] - mat[0][1]));
		} else
		{
			int[] nxt= { 1, 2, 0 };
			int i, j, k;
			// translated from Watt who uses fucking 3 as the scalar slot, jerk
			// and thinks his code is elegant cuz he uses X Y and Z and not 0 1 2
			// bozo.
			i= 0;
			if (mat[1][1] > mat[0][0])
				i= 1;
			if (mat[2][2] > mat[i][i])
				i= 2;
			j= nxt[i];
			k= nxt[j];
			s= Math.sqrt((mat[i][i] - (mat[j][j] + mat[k][k])) + 1.0);
			// the i+1 in the rep deref
			// is cuz we define the rep differently than watt
			rep[i + 1]= (float) (s * 0.5);
			s= 0.5 / s;
			rep[W]= (float) (s * (mat[k][j] - mat[j][k]));
			rep[j + 1]= (float) (s * (mat[j][i] + mat[i][j]));
			rep[k + 1]= (float) (s * (mat[k][i] + mat[i][k]));
		}

	}

	/** 
	   Sets the quaternion to represent the rotation by <code>angle</code> around 
	   <code>axis</code>. <code>axis</code> need not be normalized. Note that negating 
	   both the axis and angle results in the same quaternion on the opposite side of 
	   the sphere.
	   <P>
	   Explicitly, q = cos(angle/2) + sin(angle/2) (axis / ||axis||).

	    @see #Quaternion(Vec3, double)
	 */
	public void fromAxisAngle(Vec3 axis, double angle)
	{
		double m= axis.mag();
		//    axis.normalize();
		angle /= 2.0;
		double s= Math.sin(angle);
		double c= Math.cos(angle);
		rep[W]= (float) (c);
		rep[X]= (float) (axis.x() * (s / m));
		rep[Y]= (float) (axis.y() * (s / m));
		rep[Z]= (float) (axis.z() * (s / m));
	}

	/**
	    Interprets the input array as {x, y, z, theta}, where x, y, z are the elements of a vector along the rotation axis, and theta is the rotation angle around that axis. The x, y, z vector need not be normailized.
	    @see #fromAxisAngle(Vec3, double)
	 */
	public void fromAxisAngle(double[] axis_angle)
	{
		ensureTempVec();
		tempVec3.setValue((float) axis_angle[0], (float) axis_angle[1], (float) axis_angle[2]);
		fromAxisAngle(tempVec3, axis_angle[3]);
	}

	/**
	  Interprets the input array as {x, y, z, theta}, where x, y, z are the elements of a vector along the rotation axis, and theta is the rotation angle around that axis. The x, y, z vector need not be normailized.
	  @see #fromAxisAngle(Vec3, double)
	 */
	public void fromAxisAngle(float[] axis_angle)
	{
		ensureTempVec();
		tempVec3.setValue(axis_angle[0], axis_angle[1], axis_angle[2]);
		fromAxisAngle(tempVec3, axis_angle[3]);
	}

	/**
	    Interprets the quaternion as a rotation by an angle around an axis. Stores the axis in <code>axis</code> and returns the angle.
	    @param axis contains the axis of this on return
	    @return the angle of this
	 */
	public double toAxisAngle(Vec3 axis)
	{
		double a1= BaseMath.acos(rep[W]);
		double angle= 2.0f * a1;

		/*
		   if(angle > Math.PI)
		   angle -= 2.0*Math.PI;
		   else if(angle < -Math.PI)
		   angle += 2.0*Math.PI;
		 */

		// normalize the axis portion
		if (angle < 1.0e-6)
		{
			axis.setValue(1, 0, 0);
		} else
		{
			axis.setValue(rep[X], rep[Y], rep[Z]);
			axis.normalize();
		}
		return angle;
	}

	/**
	    Interprets the quaternion as a rotation by an angle a around an axis represented by a vector [x y z]. Stores both in <axis_angle> as [x y z a].
	    @param axis_angle contains [x y z angle] on exit
	 */
	public void toAxisAngle(double[] axis_angle)
	{
		double a1= BaseMath.acos(rep[W]);
		double angle= 2.0f * a1;

		/*
		   if(angle > Math.PI)
		   angle -= 2.0*Math.PI;
		   else if(angle < -Math.PI)
		   angle += 2.0*Math.PI;
		 */

		axis_angle[0]= rep[X];
		axis_angle[1]= rep[Y];
		axis_angle[2]= rep[Z];
		axis_angle[3]= angle;

		// normalzie the axis part
		double mag= Math.sqrt(axis_angle[0] * axis_angle[0] + axis_angle[1] * axis_angle[1] + axis_angle[2] * axis_angle[2]);
		if (mag > EPSILON)
		{
			axis_angle[0] /= mag;
			axis_angle[1] /= mag;
			axis_angle[2] /= mag;
		} else // make a zeero action axis angle
		{
			axis_angle[0]= 1.0;
			axis_angle[1]= 0.0;
			axis_angle[2]= 0.0;
			axis_angle[3]= 0.0;
		}

	}

	/** 
	    Single-precision version of toAxisAngle.
	    @see #toAxisAngle(double[])
	 */
	public void toAxisAngle(float[] axis_angle)
	{
		double a1= BaseMath.acos(rep[W]);
		double angle= 2.0f * a1;
		/*
		  if(angle > Math.PI)
		  angle -= 2.0*Math.PI;
		  else if(angle < -Math.PI)
		  angle += 2.0*Math.PI;
		 */
		axis_angle[0]= (float) rep[X];
		axis_angle[1]= (float) rep[Y];
		axis_angle[2]= (float) rep[Z];
		axis_angle[3]= (float) angle;

		// normalzie the axis part
		double mag= Math.sqrt(axis_angle[0] * axis_angle[0] + axis_angle[1] * axis_angle[1] + axis_angle[2] * axis_angle[2]);
		if (mag > EPSILON)
		{
			axis_angle[0] /= (float) mag;
			axis_angle[1] /= (float) mag;
			axis_angle[2] /= (float) mag;
		} else // make a zeero action axis angle
		{
			axis_angle[0]= 1.0f;
			axis_angle[1]= 0.0f;
			axis_angle[2]= 0.0f;
			axis_angle[3]= 0.0f;
		}

	}

	/**
	    Turns this into a quaternion that rotates vector <code>from</code> to vector <code>to</code>.
	    That is, <code>q.rotateVec(from) == to</code> (modulo magnitude).
	    <P>
	    <B>Undefined</B> for (from == -to)!   from == to is fine.
	 */
	//      Sets this quaternion with the constraint that it should rotate from into to.  On exit,  q.rotateVec(from) == to.

	//      This implementation gets bad as from --> -to, although from --> to ==> this --> the identity 1, as expected.  The reason is that there are an infinite number of ways to to get to the negative of a vector through a rotation.  
	// There is a better implementation in Game Programming Gems, I think, which should get put in here soon.  --aries

	public void fromFromTo(Vec3 from, Vec3 to)
	{
		Quaternion qfrom= new Quaternion(0.0, from.x(), from.y(), from.z());
		Quaternion qto= new Quaternion(0.0, to.x(), to.y(), to.z());
		qfrom.normalize();
		qto.normalize();
		qto.conjugate();

		Quaternion.mult(qto, qfrom, this);
		this.normalize();
		this.sqrt();
	}

	/**
	   Sets this to its square root as defined by the quaternion exponential.
	   @see #pow
	 */
	public void sqrt()
	{
		//setW(w() + 1.0);
		//normalize();

		pow(0.5);
	}

	/**
	    Raises this to the power of x.
	    Same as exp(x * ln(this)).
	    Same as scaling the vector component by x.
	 */
	public void pow(double x)
	{
		ln(this, this);
		rep[X] *= x;
		rep[Y] *= x;
		rep[Z] *= x;
		exp(this, this);
	}

	/** @deprecated
	    Create a perpendicular vector to the passed one. Returns true if
	    it was possible to do so, false otherwise (and output vector is
	    left unchanged). */
	private boolean makePerpendicularTo(Vec3 src, Vec3 dst)
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
	    Returns the angular difference between p and q, or between p and
	    -q, whichever is smaller.  That is the angle that is slerped
	    through.  angleToSlerpThrough can be considered as a distance between
	    quaternions that takes into account the antipodal
	    symmetry.
	 */
	public static double angleToSlerpThrough(Quaternion p, Quaternion q)
	{
		// mag of p - q
		double pqx= p.x() - q.x();
		double pqy= p.y() - q.y();
		double pqz= p.z() - q.z();
		double pqw= p.w() - q.w();
		double pqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		// mag of p - (-q)
		pqx= p.x() + q.x();
		pqy= p.y() + q.y();
		pqz= p.z() + q.z();
		pqw= p.w() + q.w();
		double pmqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		boolean negated= false;

		// if -q is closer to p, use it for slerp to go around shortest arc
		if (pmqdot < pqdot)
		{
			q.negate();
			negated= true;
		}

		double cosom= p.x() * q.x() + p.y() * q.y() + p.z() * q.z() + p.w() * q.w();

		// might not be unity magnitude, so protect acos domian
		if (cosom > 1.0)
			cosom= 1.0;
		if (cosom < -1.0)
			cosom= -1.0;

		// put it back where it was
		if (negated)
			q.negate();

		return BaseMath.acos(cosom);
	}

	/**
	    Same as slerp, just makes me feel better to call this one
	 */
	public static void slurp(Quaternion p, Quaternion q, double t, Quaternion qt)
	{
		slerp(p, q, t, qt);
	}

	/**
	   Performs a spherical linear interpolation between p and q.
	   angle between p and q must not be greater than PI or it may go
	   backwards!  In other words, it will choose the shorter arc to
	   traverse.  Uses t in [0,1] as the interpolation parameter.  The
	   interpolated result is placed in qt. Note: t is the percentage of q,
	   (1-t) is percentage of p. Of course, it also can be used for t not in this interval for extrapolation around the entire great circle specified by the two quaternions.
	   The implementaiton is from Shoemake.
	   @param p Starting point of interpolation.
	   @param q Destination of interpolation.
	   @param t Interpolation parameter [0..1]
	   @param qt The interpolated quaternion is stored here.
	 */

	public static boolean slerp(Quaternion p, Quaternion q, double t, Quaternion qt) throws QuaternionMathException
	{

		if (p == qt || q == qt)
		{
			throw new QuaternionMathException("slerp: Cannot do in place interpolation.");
		}

		double omega, cosom, sinom, sclp, sclq;
		boolean negated= false;

		// mag of p - q
		double pqx= p.x() - q.x();
		double pqy= p.y() - q.y();
		double pqz= p.z() - q.z();
		double pqw= p.w() - q.w();
		double pqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		// mag of p - (-q)
		pqx= p.x() + q.x();
		pqy= p.y() + q.y();
		pqz= p.z() + q.z();
		pqw= p.w() + q.w();
		double pmqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		// if -q is closer to p, use it for slerp to go around shortest arc
		if (pmqdot < pqdot)
		{
			q.negate();
			negated= true;
		}

		cosom= p.x() * q.x() + p.y() * q.y() + p.z() * q.z() + p.w() * q.w();

		if ((1.0 + cosom) > EPSILON)
		{
			if ((1.0 - cosom) > EPSILON)
			{
				omega= BaseMath.acos(cosom);
				sinom= Math.sin(omega);
				sclp= Math.sin((1.0 - t) * omega) / sinom;
				sclq= Math.sin(t * omega) / sinom;
				//				System.out.println(" small angle 2 approx used");
			} else
				// if small angle, then use linear, close enough.
			{
				//				System.out.println(" small angle 1 approx used");
				sclp= 1.0 - t;
				sclq= t;
			}
			int i;
			for (i= 0; i < 4; i++)
				qt.rep[i]= (float) ((sclp * p.rep[i]) + (sclq * q.rep[i]));
		} else
		{
			qt.rep[X]= -p.rep[Y];
			qt.rep[Y]= p.rep[X];
			qt.rep[Z]= -p.rep[W];
			qt.rep[W]= p.rep[Z];
			sclp= Math.sin((1.0 - t) * (Math.PI / 2.0));
			sclq= Math.sin(t * (Math.PI / 2.0));
			qt.rep[X]= (float) ((sclp * p.rep[X]) + (sclq * qt.rep[X]));
			qt.rep[Y]= (float) ((sclp * p.rep[Y]) + (sclq * qt.rep[Y]));
			qt.rep[Z]= (float) ((sclp * p.rep[Z]) + (sclq * qt.rep[Z]));
		}

		// be nice enough to put it back in case caller cares
		if (negated)
			q.negate();
		return negated;

	}

	/**
	   Performs a slerp between p and q as with slerp, but does not choose the shortest arc between p and q if one of the antipodes is closer.  This is used in the implemenation of squad since the math there is done on the quaternion group and not on SO(3).
	   @see #slerp(Quaternion, Quaternion, double, Quaternion)
	   @param p Starting point of interpolation.
	   @param q Destination of interpolation.
	   @param t Interpolation parameter [0..1]
	   @param qt The interpolated quaternion is stored here.
	 */
	public static void nonFlippingSlerp(Quaternion p, Quaternion q, double t, Quaternion qt)
	{
		double omega, cosom, sinom, sclp, sclq;
		cosom= p.x() * q.x() + p.y() * q.y() + p.z() * q.z() + p.w() * q.w();

		if ((1.0 + cosom) > EPSILON)
		{
			if ((1.0 - cosom) > EPSILON)
			{
				omega= BaseMath.acos(cosom);
				sinom= Math.sin(omega);
				sclp= Math.sin((1.0 - t) * omega) / sinom;
				sclq= Math.sin(t * omega) / sinom;
			} else
				// if small angle, then use linear, close enough.
			{
				sclp= 1.0 - t;
				sclq= t;
			}
			int i;
			for (i= 0; i < 4; i++)
				qt.rep[i]= (float) ((sclp * p.rep[i]) + (sclq * q.rep[i]));
		} else
		{
			qt.rep[X]= -p.rep[Y];
			qt.rep[Y]= p.rep[X];
			qt.rep[Z]= -p.rep[W];
			qt.rep[W]= p.rep[Z];
			sclp= Math.sin((1.0 - t) * (Math.PI / 2.0));
			sclq= Math.sin(t * (Math.PI / 2.0));
			qt.rep[X]= (float) ((sclp * p.rep[X]) + (sclq * qt.rep[X]));
			qt.rep[Y]= (float) ((sclp * p.rep[Y]) + (sclq * qt.rep[Y]));
			qt.rep[Z]= (float) ((sclp * p.rep[Z]) + (sclq * qt.rep[Z]));
		}

	}

	/**
	   Performs a slerp between p and q using the exponential form of the equations instead of the geometric construction of shoemake.  Just for clarity.  Not sure if it is faster of not.  Like nonFlippingSlerp, this version does not flip antipodes on the closer arc.
	   @param p Starting point of interpolation.
	   @param q Destination of interpolation.
	   @param t Interpolation parameter [0..1]
	   @param qt The interpolated quaternion is stored here.
	 */
	public static void powerSlerp(Quaternion p, Quaternion q, double t, Quaternion qt)
	{
		qt.setValue(p);
		qt.conjugate();
		qt.concatRight(q);
		Quaternion.power(qt, t, qt);
		qt.concatLeft(p);
	}

	/** Squad does cubic spline interpolation on the sphere using the
	    quadrangle construction, hence squad.  For a full description of
	    how this works, see: _Advanced Animation and Rendering
	    Techniques_ by Watt & Watt, pp. 365--368.

	    <P>

	    Squad is implemented as three slerps between the arguments.
	    Basically, the 4 quaternion arguments define a spherical
	    quadrangle.  As the parameter sweeps from 0 to 1, the spline
	    will interpolate from q0 to q1 smoothly as a spherical cubic.
	    The middle two quaternions, a and b, define the incoming and
	    outgoing tangents of the spline, so care should be taken to
	    match these up in a piecewise interpolation.  Shoemake discusses
	    good ways to choose these as averages of the tangents of the
	    surrounding points.

	    <P>

	    It is important that the points given are on the same local
	    hemisphere (replced by their negative if not) lest the slerp
	    estimate the tangents incorrectly, leading to exceesive or
	    incorrect velocity matching.

	    <P>
	    None of q0, a, b, q1 are mutated

	    @param q0 the first endpoint which be the value for alpha = 0.
	    @param a first intermediate point that implicitly defines
	    tangent from q0.
	    @param b second intermediate point that implicitly defines
	    tangent at q1.
	    @param q1 the final endpoint which is the value for alpha = 1.
	    @param alpha the interpolation parameter which ranges from
	    [0,1].
	    @param qtemp1 a temporary quaternion for internal calculations to
	    avoid garbage.  The value on exit is indeterminate.
	    @param qtemp2 a temporary quaternion for internal calculations to
	    avoid garbage.  The value on exit is indeterminate.
	    @param qout the interpolated quaternion, output of squad.
	 */
	public static void squad(Quaternion q0, Quaternion a, Quaternion b, Quaternion q1, double alpha, Quaternion qtemp1, Quaternion qtemp2, Quaternion qout)
	{
		// might use nonFlippingSlerp as well, but the power one works.
		//should speed check between them later.  I imagine nonflippingslerp is faster.
		powerSlerp(q0, q1, alpha, qtemp1);
		powerSlerp(a, b, alpha, qtemp2);
		powerSlerp(qtemp1, qtemp2, 2.0 * alpha * (1.0 - alpha), qout);
//		nonFlippingSlerp(q0, q1, alpha, qtemp1);
//		nonFlippingSlerp(a, b, alpha, qtemp2);
//		nonFlippingSlerp(qtemp1, qtemp2, 2.0 * alpha * (1.0 - alpha), qout);
	}

	/**
	   The natural logarithm of the quaternion.  This is defined to be
	   the pure vector [0, omega N] if q = [cos (omega),  sin(omega) N].
	   <B>Note that this is not a unit quaternion!!!</B> This is useful 
	   as an intermediate step for certain functions on unit quaternions.
	   doing other functions of unit quaternions, however.
	   <P>
	   Notice that ln(-1) is undefined in the limit, so we
	   define it as ln(1) = 0 since 1 ~ -1.
	   <P>
	   @param q the quaternion to take the ln of.
	   @param lnq the natural log of q is put here. lnq = ln(q);
	 */
	public static void ln(Quaternion q, Quaternion lnq)
	{
		double omega= BaseMath.acos(q.w());
		lnq.rep[W]= 0.0f;

		// using sinc is much more robust.
		double sinc= BaseMath.sinc(omega);

		// this is to avoid div by zeros near PI, which cause huge roundoffs
		if (Math.abs(sinc) < EPSILON)
			sinc= EPSILON;

		lnq.rep[X]= (float) (q.rep[X] / sinc);
		lnq.rep[Y]= (float) (q.rep[Y] / sinc);
		lnq.rep[Z]= (float) (q.rep[Z] / sinc);

		/*
		  if (Math.abs(sin_omega) < EPSILON)
		  {
		  // avoid div by zero set it to the zero vector
		  lnq.rep[X] = 0.0;  lnq.rep[Y] = 0.0; lnq.rep[Z] = 0.0;
		  }
		  else
		  {
		  staticTempVec3.setValue(q.rep[X], q.rep[Y], q.rep[Z]);
		  staticTempVec3.normalize();
		  staticTempVec3.scale(omega);
		  lnq.rep[X] = staticTempVec3.x();
		  lnq.rep[Y] = staticTempVec3.y();
		  lnq.rep[Z] = staticTempVec3.z();
		  }
		 */
	}

	/**
	   Exponentiation of a pure vector quaternion.  Exp([0, omega n_hat]) =
	   [cos (omega), sin(omega) n_hat].  Notice that the input quaternion q does not
	   need to be a unit quaternion and probably will not be!
	   <P>
	   @return a unit quaternion which expq = exp(q).

	   @param q the quaternion (must have q.w() = 0) to exponentiate
	   @param expq the exponentiated quaternion
	 */
	public static void exp(Quaternion q, Quaternion expq) throws QuaternionMathException
	{
		if (Math.abs(q.w()) > 1.0e-12)
			throw new QuaternionMathException("exp(q) must have q.w() == 0.0.  Got: " + q.toString());

		double omega= q.mag();
		double sinc_omega= BaseMath.sinc(omega);

		expq.rep[W]= (float) Math.cos(omega);
		expq.rep[X]= (float) (q.rep[X] * sinc_omega);
		expq.rep[Y]= (float) (q.rep[Y] * sinc_omega);
		expq.rep[Z]= (float) (q.rep[Z] * sinc_omega);
		expq.normalize();
	}

	/**
	   Convenience method that calls <code>clone</code>, but casts it to a Quaternion.
	 */
	public Quaternion copy()
	{
		return (Quaternion) clone();
	}

	/**
	   Returns a deep copy of <code>this</code>.
	 */
	public Object clone()
	{
		try
		{
			Quaternion q= (Quaternion) super.clone();
			q.rep= new float[4];
			System.arraycopy(rep, 0, q.rep, 0, 4);
			return q;
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	   Calculates q to the t power ( q^t ) by using exponential form:
	   <P>
	   q^t = exp(t * ln(q));
	 */
	public static void power(Quaternion q, double t, Quaternion qout)
	{
		Quaternion.ln(q, qout);
		qout.scale(t);
		try
		{
			Quaternion.exp(qout, qout);
		} catch (QuaternionMathException e)
		{
			// force the issue
			qout.rep[W]= 0.0f;
			try
			{
				Quaternion.exp(qout, qout);
			} catch (QuaternionMathException singularity)
			{
				System.err.println("The universe is in disarray.  I give up.");
				System.exit(0);
			}
		}
	}

	/**
	    The magnitude of a quaternion.
	    sqrt(q.conjugate() * q) = magnitude.
	 */
	public double mag()
	{
		return Math.sqrt(rep[0] * rep[0] + rep[1] * rep[1] + rep[2] * rep[2] + rep[3] * rep[3]);
	}

	/**
	   Returns the squared magnitude (same result as mag()*mag()).
	 */
	public double magSquared()
	{
		return (rep[0] * rep[0] + rep[1] * rep[1] + rep[2] * rep[2] + rep[3] * rep[3]);
	}

	/**
	    Standard inner product defined in embedding space of R^4
	    If p and q are unit quaternions, then <P>
	    p dot q = cos(theta/2),<P>
	    Where theta is the angle between the quaterion vectors in R^4.
	 */
	public static double dot(Quaternion p, Quaternion q)
	{
		return p.rep[X] * q.rep[X] + p.rep[Y] * q.rep[Y] + p.rep[Z] * q.rep[Z] + p.rep[W] * q.rep[W];
	}

	/**
	    Returns a valid distance metric between two quaternions.
	    <p>
	    dist(p,p) = 0
	    <P>
	    dist(p,q) = dist(q,p)
	    <P>
	    dist(p,q) + dist(q,r) >=      dist(p,r)
	    <P>

	    Defn: dist(p,q) = 1 - cos(theta)
	    where theta is the angle between quaternions, <B>not to be confused with the angle between rotations or rotation axes</B>.
	    Be careful of double covering.  This doesn't tell distance between rotations in SO(3), just points on S^3.
	 */
	public static double distCos(Quaternion p, Quaternion q)
	{
		return 1.0 - dot(p, q);
	}

	/**
	   Distance between two quats as the arclength of the shortest great
	   circle arc containing them.
	   Specifically, d = acos(dot(p,q)).
	 */
	public static double distAngular(Quaternion p, Quaternion q)
	{
		double ct= dot(p, q);
		return BaseMath.acos(ct);
	}


	/**
	 * Is this wrong of me?  it seems that distAngular is 1/2 the dist in radians.
	 * so i added this -jg
	 */
	public static double distAngularRadians(Quaternion p, Quaternion q)
	{
		double da = distAngular(p,q);
		return 2*da;
	}

	private Quaternion temp= null;
	/**
	   Calculates <code>ln(ref.conjugate() * this)</code> and writes it into <code>log</code>. This performs a logarithmic map of this quaternion to the tangent space centered at <code>ref</code>, and writes the resulting tangent vector into <code>log</code>.
	   <P>
	   If ref is meant to be on the same hemisphere as this, (such as when using logmap as a distance metric), it is the users responsibility to make sure ref is on the same hemisphere as this.

	   @param ref The quaternion to take the map w.r.t.
	   @param log the resulting log (tangent) vector at ref of this.
	   @see #expmap(Quaternion, Vec3, Quaternion)
	 */
	public void logmap(Quaternion ref, Vec3 log)
	{
		if (temp == null)
			temp= new Quaternion();

		temp.setValue(ref);
		temp.conjugate();
		temp.concatRight(this);
		Quaternion.ln(temp, temp);
		log.set(0, temp.x());
		log.set(1, temp.y());
		log.set(2, temp.z());
	}

	/**
	   Calculates <code>ln(ref.conjugate() * this)</code> and writes it into <code>log</code>. This performs a logarithmic map of this quaternion to the tangent space centered at <code>ref</code>, and writes the resulting tangent vector into  the vector component of the quaternion <code>log</code>.
	   <P>
	   If ref is meant to be on the same hemisphere as this, (such as when using logmap as a distance metric), it is the users responsibility to make sure ref is on the same hemisphere as this.

	   @param ref The quaternion to take the map w.r.t.
	   @param log a quaternion in which to write the log (tangent) vector at ref of this. {0, ln.x, ln.y, ln.z}
	   @see #expmap(Quaternion, Quaternion, Quaternion)
	 */
	public void logmap(Quaternion ref, Quaternion log)
	{
		if (temp == null)
			temp= new Quaternion();

		temp.setValue(ref);
		temp.conjugate();
		temp.concatRight(this);
		Quaternion.ln(temp, temp);
		log.setX(temp.x());
		log.setY(temp.y());
		log.setZ(temp.z());
		log.setW(0.0f);
	}

	/**
	 * Returns <code>q_out = mult(ref, exp(log))</code>. Performs an exponential map of a
	 * pure imaginary quaternion (0 scalar component, represented by a Vec3,)
	 * into the unit quaternion group at the reference quaternion ref.<P>
	 *
	 *  Note this is the inverse of logmap.
	 */
	public static void expmap(Quaternion ref, Vec3 log, Quaternion q_out)
	{
		if (ref == q_out)
			throw new IllegalArgumentException("Expmap cannout have same ref as output!");
		q_out.fromVec3(log);
		Quaternion.exp(q_out, q_out);
		q_out.concatLeft(ref);
	}

	/**
	 * Performs <code>q_out = mult(ref, exp(log))</code>. Performs an exponential map of a
	 * pure imaginary quaternion (0 scalar component, represented by a Vec3,)
	 * into the unit quaternion group at the reference quaternion ref.<P>
	 *
	 *  Note this is the inverse of logmap.
	 */
	public static void expmap(Quaternion ref, Quaternion log, Quaternion q_out)
	{
		if (ref == q_out)
			throw new IllegalArgumentException("Expmap cannout have same ref as output!");
		q_out.setValue(log);
		Quaternion.exp(q_out, q_out);
		q_out.concatLeft(ref);
	}

	/**
	    Given a list of quaternions, calculates their centroid and stores it in 
	    <code>centroid</code>. 
	    The centroid is defined as the quaternion
	    that minimizes the sum of squared distances over SO(3) to the
	    examples.  <P>

	    If examples are distributed over the full
	    sphere, this will give the centroid assuming opposite
	    quaternions are identical. <P>

	    To do this, the system solves
	    the constrained minimation problem which results in the linear
	    system <P>x' A x - lamba x' x for the maximum.  The max should be the
	    eigenvector of A with the largest corresponding eigenvalue.
	 */
	public static void centroid(List examples, Quaternion centroid)
	{
		int num_examples= examples.size();

		// make a data matrix with examples as columns
		Matrix data= new Matrix(4, num_examples);
		for (int j= 0; j < num_examples; j++)
		{
			for (int i= 0; i < 4; i++)
			{
				Quaternion qj= (Quaternion) examples.get(j);
				data.set(i, j, qj.get(i));
			}
		}
		Matrix data_transpose= data.makeTranspose();

		// the inertia matrix carries the cross product sums over examples
		Matrix inertia= new Matrix(4, 4);
		Matrix.mult(data, data_transpose, inertia);

		try
		{
			SVD svd= new SVD(inertia);
			Vec singular_values= new Vec(4);
			svd.getSingularValues(singular_values);
			int max_ind= 0;
			for (int i= 1; i < 4; i++)
			{
				if (singular_values.get(i) > singular_values.get(max_ind))
					max_ind= i;
			}
			Matrix range= svd.getRangeBasisMatrix();
			for (int i= 0; i < 4; i++)
			{
				centroid.set(i, range.get(i, max_ind));
			}
		} catch (SVDException e)
		{
			e.printStackTrace();
			Debug.doAssert(false, "Quaternion Wedged. " + e);
		}

		replaceWithIdentityCanonicalQuaternionIfNecessary(centroid);
	}

	/**
	   @deprecated
	   Deprecated by aries.  Use replaceWithShortestArcRepresentativeIfNecessary instead.
	   if you have to flip 'maybeNegate' to be as close to 'fixed' as possible
	   returns true if we actually mutate 'maybeNegate' - marc
	   @see #replaceWithShortestArcRepresentativeIfNecessary(Quaternion, Quaternion)
	 */
	public static boolean sameSide(Quaternion fixed, Quaternion maybeNegate)
	{

		// this code marc thinks is faster (and we spend a surprisingly large amount of time in this code)

		double z= fixed.x() * maybeNegate.x() + fixed.y() * maybeNegate.y() + fixed.z() * maybeNegate.z() + fixed.w() * maybeNegate.w();
		if (z < 0)
		{
			maybeNegate.negate();
			return true;
		}
		return false;

		/*    // mag of p - q
		double pqx = fixed.x() - maybeNegate.x();
		double pqy = fixed.y() - maybeNegate.y();
		double pqz = fixed.z() - maybeNegate.z();
		double pqw = fixed.w() - maybeNegate.w();
		double pqdot = pqx*pqx + pqy*pqy + pqz*pqz + pqw*pqw;

		// mag of p - (-q)
		pqx = fixed.x() + maybeNegate.x();
		pqy = fixed.y() + maybeNegate.y();
		pqz = fixed.z() + maybeNegate.z();
		pqw = fixed.w() + maybeNegate.w();
		double pmqdot = pqx*pqx + pqy*pqy + pqz*pqz + pqw*pqw;

		// if -q is closer to p, use it for slerp to go around shortest arc
		if (pmqdot < pqdot)
		  {
		 maybeNegate.negate();
		return true;
		  }
		  return false;*/
	}

	private static Quaternion qeye;
	/* 
	    convenience for identity ref:
	    replaceWithIdentityCanonicalQuaternionIfNecessary(q, new Quaternion()); 
	 */

	/**
	   If <code>q</code> is not on the same hemisphere as the identity quaternion, flips <code>q</code> and returns <code>true</code>. Otherwise, does nothing and returns <code>false</code>.
	 */
	public static boolean replaceWithIdentityCanonicalQuaternionIfNecessary(Quaternion q)
	{
		if (qeye == null)
			qeye= new Quaternion();
		return replaceWithShortestArcRepresentativeIfNecessary(q, qeye);
	}

	/**
	    Forces q to be on the same hemisphere of S^3 as ref by negating q if it is on the other hemisphere.  Defined over unit quaternions Q.
	    @param q the quaternion to negate if required
	    @param ref a reference quaternion that defines the hemisphere of S^3 that we desire the closest of {q,-q} of.
	    @return whether q was negated or not
	 */
	public static boolean replaceWithShortestArcRepresentativeIfNecessary(Quaternion q, Quaternion ref)
	{
		Quaternion p= ref;

		// if the same quat, return
		if (q == ref)
			return false;

		// mag of p - q
		double pqx= p.x() - q.x();
		double pqy= p.y() - q.y();
		double pqz= p.z() - q.z();
		double pqw= p.w() - q.w();
		double pqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		// mag of p - (-q)
		pqx= p.x() + q.x();
		pqy= p.y() + q.y();
		pqz= p.z() + q.z();
		pqw= p.w() + q.w();
		double pmqdot= pqx * pqx + pqy * pqy + pqz * pqz + pqw * pqw;

		boolean negated= false;

		// if -q is closer to p, use it for slerp to go around shortest arc
		if (pmqdot < pqdot)
		{
			q.negate();
			negated= true;
		}

		return negated;
	}

	/**
	    Mutates the examples in the list so that they are all on the
	    hemisphere of S^3 centered around <code>ref</code>.
	    <P>
	    <B>Note:</B> Only defined over unit quaternions.
	 */
	public static void hemispherizeExamples(List examples, Quaternion ref)
	{
		boolean done= false;
		Quaternion centroid= new Quaternion();
		Quaternion neg_centroid= new Quaternion();

		Quaternion.centroid(new Vector(examples), centroid);
		neg_centroid.setValue(centroid);
		neg_centroid.negate();

		if (Quaternion.distAngular(neg_centroid, ref) < Quaternion.distAngular(centroid, ref))
			centroid.negate();
		// now centroid is the hemisphere closest to the ref
		// so flip all quats to be nearest to this centroid

		Iterator iter= examples.iterator();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			double theta= Quaternion.distAngular(q, centroid);
			q.negate();
			if (theta < Quaternion.distAngular(q, centroid)) // if other is closer
			{
				q.negate(); // put it back
			} else
			{
				// otherwise leave it flipped
				//System.out.println("Flipped example.");
			}
		}
	}

	/** 
	    Example test code. Only tests a few things though, the rest were tested in TestQuaternion 
	 */
	public static void main(String[] argh)
	{
		// random rotatiions
		// COMMENTED OUT BY MARC BECAUSE WITHOUT THE ROTATION3D CONSTRUCTORS, THIS WOULDN'T COMPILE - FIXME
		UniformRandomVariable urv= new UniformRandomVariable(-1.0, 1.0);
		/* Vec3 v = new Vec3(4.5, 2.3, -1.4);
		Vec3 v_from_rot = r21.transformPoint(v);
		Vec3 v_from_quat = q21.rotateVec(v);


		System.out.println("v: " + v);
		System.out.println("R21 v: " + v_from_rot);
		System.out.println("Q21 v: " + v_from_quat);
		 */
		Vec3 fromVec= new Vec3(2, 1, 3);
		fromVec.normalize();
		Vec3 toVec= new Vec3(0, 0, -1);
		Quaternion fromTo= new Quaternion(fromVec, toVec);
		Vec3 outVec= new Vec3();
		fromTo.rotateVec(fromVec, outVec);
		System.out.println("Rotation from fromVec = " + fromVec + " to toVec = " + toVec + " yields " + outVec + " (should be equal to toVec)");
		fromVec= new Vec3(-3, 1, 4);
		fromVec.normalize();
		toVec= new Vec3(2, 2, -1);
		toVec.normalize();
		fromTo= new Quaternion(fromVec, toVec);
		fromTo.rotateVec(fromVec, outVec);
		System.out.println("Rotation from fromVec = " + fromVec + " to toVec = " + toVec + " yields " + outVec + " (should be equal to toVec)");
		fromVec= new Vec3(1, 0, 0);
		fromVec.normalize();
		toVec= new Vec3(1, 0, 0);
		toVec.normalize();
		fromTo= new Quaternion(fromVec, toVec);
		fromTo.rotateVec(fromVec, outVec);
		System.out.println("Rotation from fromVec = " + fromVec + " to toVec = " + toVec + " yields " + outVec + " (should be equal to toVec)");
		fromVec= new Vec3(1, 0, 0);
		fromVec.normalize();
		toVec= new Vec3(-1, 0, 0);
		toVec.normalize();
		fromTo= new Quaternion(fromVec, toVec);
		fromTo.rotateVec(fromVec, outVec);
		System.out.println("Rotation from fromVec = " + fromVec + " to toVec = " + toVec + " yields " + outVec + " (should be equal to toVec)");

		Quaternion q666= new Quaternion(.8, .4, .2, -.5);
		q666.normalize();
		Quaternion q777= new Quaternion(q666);
		Quaternion.ln(q666, q666);
		Quaternion.exp(q666, q666);
		q777.negate();
		Quaternion.add(q777, q666, q777);
		System.out.println("exp(ln(" + q777 + ")) == " + q666);

		Quaternion p= new Quaternion(new Vec3(urv.sample(), urv.sample(), urv.sample()));
		Quaternion.exp(p, p);
		Quaternion p_orig= new Quaternion(p);

		Quaternion q= new Quaternion(new Vec3(urv.sample(), urv.sample(), urv.sample()));
		Quaternion.exp(q, q);
		Quaternion q_inv= new Quaternion(q);
		q_inv.invert();

		spew("p: " + p + "  q: " + q);
		p.concatLeft(q);
		spew("p.concatLeft(q) = " + p);

		p.concatLeft(q_inv);
		spew("p.concatLeft(q_inverse) = " + p);
		spew("And should equal " + p_orig);

		p.setValue(p_orig);
		spew("p = " + p + "   q = " + q);
		p.concatRight(q);
		spew("p.concatRight(q) = " + p);

		p.concatRight(q_inv);
		spew("p.concatRight(q_inv) = " + p);
		spew("And should be " + p_orig);

		p.setValue(p_orig);
		spew(" p = " + p + "  q = " + q);
		p.concatRight(q).concatRight(q_inv).concatLeft(q).concatLeft(q_inv);
		spew("p.concatRight(q).concatRight(q_inv).concatLeft(q).concatLeft(q_inv) = " + p);
		spew("And should be " + p_orig);

		// test the centroid
		Quaternion centroid= new Quaternion();
		int num_examples= 10;
		Vector examples= new Vector(num_examples);
		for (int i= 0; i < num_examples; i++)
		{
			Quaternion qi= new Quaternion();
			for (int k= 0; k < 4; k++)
				qi.set(k, urv.sample());
			qi.normalize();
			examples.addElement(qi);
			System.out.println("Example " + i + " " + qi + "  mag " + qi.mag());
		}
		Quaternion.centroid(examples, centroid);
		System.out.println("Centroid " + centroid);
		System.out.println("Centroid mag: " + centroid.mag());
		double epsilon= 1.0;
		double centroid_error= findError(centroid, examples);
		System.out.println("Centroid error: " + centroid_error);
		for (int t= 0; t < 10000000; t++)
		{
			Quaternion perturbation= new Quaternion();
			for (int i= 0; i < 4; i++)
				perturbation.set(i, urv.sample() * epsilon);
			perturbation.normalize();
			Quaternion new_q= new Quaternion();
			Quaternion.mult(perturbation, centroid, new_q);
			double error= findError(new_q, examples);
			if (error < centroid_error)
			{
				System.out.println("Smaller error than centroid! " + error);
			}
		}

	}

	private static void spew(String s)
	{
		System.out.println(s);
	}

	protected static double findError(Quaternion q, Vector examples)
	{
		int num_examples= examples.size();
		double sum= 0.0;
		for (int i= 0; i < num_examples; i++)
		{
			Quaternion example= (Quaternion) examples.elementAt(i);
			double angle= Quaternion.angleToSlerpThrough(example, q);
			sum += Math.sin(angle) * Math.sin(angle);
		}
		return sum;
	}

	/* support for concat into operations */

	private int getOtherRepIndex()
	{
		return ((rep_ptr == 0) ? (1) : (0));
	}

	private float[] getOtherRep()
	{
		int idx= getOtherRepIndex();
		float[] which_rep= ((idx == 0) ? (rep1) : rep2);
		if (idx == 1 && rep2 == null)
		{
			rep2= new float[4];
			which_rep= rep2;
		}
		return which_rep;
	}

	private void swapReps()
	{
		rep= getOtherRep();
		rep_ptr= getOtherRepIndex();
	}

	private static void internalQuatMult(float[] q1, float[] q2, float[] qout)
	{
		if (q1 == qout || q2 == qout)
		{
			throw new InPlaceMatrixMultException("cannot multiply quaternions in place!!! Garbage!");
		}

		qout[0]= q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2] - q1[3] * q2[3];
		qout[1]= q1[1] * q2[0] + q1[0] * q2[1] - q1[3] * q2[2] + q1[2] * q2[3];
		qout[2]= q1[2] * q2[0] + q1[3] * q2[1] + q1[0] * q2[2] - q1[1] * q2[3];
		qout[3]= q1[3] * q2[0] - q1[2] * q2[1] + q1[1] * q2[2] + q1[0] * q2[3];

	}

	private void initSwapBuffer()
	{

	}

	/**
		added (by marc) to reduce memory footprint of quaternion */
	private void ensureTempVec()
	{
		if (tempVec3 == null)
			tempVec3= new Vec3();
	}

	public String toStringAxisAngle()
	{
		Vec3 t = new Vec3();
		float f = (float)this.toAxisAngle(t);
		return f+" "+t;
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
	{
		ois.defaultReadObject();
		rep1 = rep;
		rep2 = null;
		tempVec3 = null;
		rep_ptr = 0;
	}

	public void toEulerAngle(double [] eulerangles) {
		double test = rep[X]*rep[Y] + rep[Z]*rep[W];

		if (test > 0.499) { // singularity at north pole
			//this.scale(-1);
		}
		if (test < -0.499) { // singularity at south pole
			//this.scale(-1);
		}

		double sqx = rep[X]*rep[X];
		double sqy = rep[Y]*rep[Y];
		double sqz = rep[Z]*rep[Z];
		eulerangles[1] = Math.atan2(2*rep[Y]*rep[W]-2*rep[X]*rep[Z] , 1 - 2*sqy - 2*sqz);
		eulerangles[0] = Math.atan2(2*rep[X]*rep[W]-2*rep[Y]*rep[Z] , 1 - 2*sqx - 2*sqz);
		eulerangles[2] = Math.asin(2*test);
	}

	public void fromEulerAngles(Vec3 eulerangles)
	{
		double c1 = Math.cos(eulerangles.x()/2);
		double s1 = Math.sin(eulerangles.x()/2);
		double c2 = Math.cos(eulerangles.z()/2);
		double s2 = Math.sin(eulerangles.z()/2);
		double c3 = Math.cos(eulerangles.y()/2);
		double s3 = Math.sin(eulerangles.y()/2);

		this.setW((float)(c1*c2*c3 - s1*s2*s3));
		this.setY((float)(c1*c2*s3 + s1*s2*c3));
		this.setX((float)(s1*c2*c3 + c1*s2*s3));
		this.setZ((float)(c1*s2*c3 - s1*c2*s3));
	}

	public void toEulerAngle(Vec3 eulerangles) {
		double [] tmp = new double[3];
		toEulerAngle(tmp);
		eulerangles.setValue(tmp);

		//Quaternion back = new Quaternion();
		//back.fromEulerAngles(eulerangles);
		//System.out.println("Difference in quaternions is " + Quaternion.distAngular(this, back) + " for angles " + eulerangles.getX()*180/Math.PI + ","+ eulerangles.getY()*180/Math.PI + "," + eulerangles.getZ()*180/Math.PI);
	}
}
