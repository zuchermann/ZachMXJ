package innards.math.linalg;


/** 
    Class for storing rotations. May be defined by various representations.
    Also converts between representations.
*/
import java.io.*;


public final class Rotation 
  implements Cloneable, Serializable
{
  private Quaternion rep = new Quaternion();

    /**
       Creates an identity rotation (a rotation that doesn't do anything.)
     */
  public Rotation()
  {
    
  }
    /**
       Constructs a rotation from a quaternion representation.
    */
  public Rotation(Quaternion q)
  {
    rep.fromQuaternion(q);
  }

    /**
       Constructs a rotation from an axis-angle representation.
    */
  public Rotation(Vec3 axis, double angle)
  {
    rep.fromAxisAngle(axis, angle);
  }

    /**
       Copy constructor.
    */
  public Rotation(Rotation r)
  {
    rep.fromQuaternion(r.rep);
  }
  
    /**
       Constructs a rotation from an axis-angle representation. Interprets the input vector as {x, y, z, theta}, where theta is the angle, and x,y,z are the elements of a vector along the axis. The vector need not be normalized.
    */
  public Rotation(double[] axis_angle)
  {
    rep.fromAxisAngle(axis_angle);
  }

    /**
       Constructs a rotation from an axis-angle representation. Interprets the input vector as {x, y, z, theta}, where theta is the angle, and x,y,z are the elements of a vector along the axis. The vector need not be normalized.
    */
  public Rotation(float[] axis_angle)
  {
    rep.fromAxisAngle(axis_angle);
  }

  /** 
      Creates the rotation which rotates vector <code>from</code> into vector <code>to</code>.
  */
  public Rotation(Vec3 from, Vec3 to)
  {
    rep.fromFromTo(from, to);
  }


  /** 
      Exports this Rotation to a Quaternion representation.
  */
  public void toQuaternion(Quaternion q)
  {
    q.setValue(rep);
  }


  /** 
      Exports this Rotation to an axis-angle representation. Modifies <code>axis</code> to be a unit vector that points along the axis of rotation, and returns the angle.
      @param axis A Vec3 into which the rotation axis is stored.
      @return The rotation angle.
  */
  public double toAxisAngle(Vec3 axis)
  {
    return rep.toAxisAngle(axis);
  }
  
  /** 
      Exports this Rotation to an axis-angle representation. Stores a vector [x y z] pointing along the axis and the rotation angle theta in <code>axis_angle</code> as [ x y z theta ] 
      @param axis_angle [x y z theta]
  */
  public void toAxisAngle(double[] axis_angle)
  {
    rep.toAxisAngle(axis_angle);
  }
  
  /** 
      Exports this Rotation to an axis-angle representation. Stores a vector [x y z] pointing along the axis and the rotation angle theta in <code>axis_angle</code> as [ x y z theta ] 
      @param axis_angle [x y z theta]
  */
  public void toAxisAngle(float[] axis_angle)
  {
    rep.toAxisAngle(axis_angle);
  }
  
      /**
       Sets this Rotation to encode the rotation represented by a quaternion.
    */
  public void setValue(Quaternion q)
  {
    rep.fromQuaternion(q);
  }
    /**
       Sets this to be the rotation represented by an axis and an angle.
    */
  public void setValue(Vec3 axis, double angle)
  {
    rep.fromAxisAngle(axis, angle);
  }


    /**
       Sets this to be the rotation represented by an axis and an angle. The axis and angle are stored in the input array as [x, y, z, theta].
       @param [x, y, z, theta]
    */
  public void setValue(double[] axis_angle)
  {
    rep.fromAxisAngle(axis_angle);
  }

  private double[] axis_angle_cache = new double[4];

    /**
       Sets this to be the rotation represented by an axis and an angle. The axis and angle are stored in the input array as [x, y, z, theta].
       @param [x, y, z, theta]
    */
  public void setValue(float[] axis_angle)
  {
    for(int i=0; i<4; i++) axis_angle_cache[i] = axis_angle[i];
    setValue(axis_angle_cache);
  }


     /**
       Sets this Rotation to encode the rotation represented by a quaternion.
       @param q0 The scalar component of the quaternion.
       @param q1 The first element of the vector component of the quaternion.
       @param q2 The second element of the vector component of the quaternion.
       @param q3 The third element of the vector component of the quaternion.
    */
  public void setValue(double q0, double q1, double q2, double q3)
  {
    rep.setValue(q0,q1,q2,q3);
  }

    /**
       Sets this Rotation to be equivalent to Rotation <code>r</code>.
    */
  public void setValue(Rotation r)
  {
    rep.fromQuaternion(r.rep);
  }

    /** 
      Creates the rotation which rotates vector <code>from</code> into vector <code>to</code>.
  */
  public void setValue(Vec3 from, Vec3 to)
  {
    rep.fromFromTo(from, to);
  }

  /** 
      Concatenates the rotations <code>r</code> and <code>s</code> and outputs the product to <code>out</code>. Note that <code>out.rotateVector(x)</code> rotates x by <code>s</code> first, then <code>r</code>.
  */
  public static void concat(Rotation r, Rotation s, Rotation out)
  {
    Quaternion.mult(r.rep, s.rep, out.rep);    
  }

  /** 
      Concatenates the rotations <code>r</code> and <code>s</code> and returns the product as a new Rotation. Note that <code>concat(r,s).rotateVector(x)</code> rotates x by <code>s</code> first, then <code>r</code>.
  */
  public static Rotation concat(Rotation r, Rotation s)
  {
    Rotation out = new Rotation();
    Quaternion.mult(r.rep, s.rep, out.rep);
    return out;
  }

    /** 
      Concatenates the Rotation <code>r</code> with <code>this</code> and returns the product as a new Rotation. Note that <code>premult(r).rotateVector(x)</code> rotates x by <code>this</code> first, then <code>r</code>.
  */
  public Rotation preMult(Rotation r)
  {
    Rotation out = new Rotation();
    Quaternion.mult(r.rep, this.rep, out.rep);
    return out;
  }

      /** 
      Concatenates the Rotation <code>this</code> with <code>r</code> and returns the product as a new Rotation. Note that <code>postmult(r).rotateVector(x)</code> rotates x by <code>r</code> first, then <code>this</code>.
    */
  public Rotation postMult(Rotation r)
  {
    Rotation out = new Rotation();
    Quaternion.mult(this.rep, r.rep, out.rep);
    return out;
  }

    /**
       Rotates vector <code>v</code>, storing the result in <code>out</code>.
    */
  public void rotateVector(Vec3 v, Vec3 out)
  {
    rep.rotateVec(v,out);
  }

    /**
       Rotates a vector.
    */
  public void rotateVector(Vec3 v)
  {
    rep.rotateVec(v);
  }
  

  /* useful methods */

  /** 
      Renormalizes the internal quaternion representation to be a unit quaternion (valid rotation). Useful for processes, such as Euler integration, where numerical drift is a risk.
  */
  public void renormalize()
  {
    rep.normalize();
  }


  //-------------------------------------------
  //   DEPRECATED!
  /**
     Sets this Rotation to be its inverse (reverse-direction rotation.)
     <P>
     @deprecated By Chris Kline as of CVS version 1.10, because it's too damn
     confusing; replaced by <code>invert()</code>.
  */
  public void inverse()
  {
    Quaternion.conjugate(rep, rep);
  }
  //-------------------------------------------

  
    /**
     Sets this Rotation to be its inverse (reverse-direction rotation.)
   */
  public void invert()
  {
    Quaternion.conjugate(rep, rep);
  }


  //-------------------------------------------
  //   DEPRECATED!
  /**
      Places the inverse (reverse-direction) rotation into <code>r</code>.
     <P>
     @deprecated By Chris Kline as of CVS version 1.10, because it's too damn
     confusing; replaced by <code>getInverse(Rotation)</code>.
  */
  public void inverse(Rotation r)
  {
    Quaternion.conjugate(rep, r.rep);
  }
  //-------------------------------------------

  
  /** 
      Returns the inverse (reverse-direction) rotation.
  */
  public Rotation getInverse()
  {
    Rotation r = copy();
    r.invert();
    return r;
  }

    /** 
      Places the inverse (reverse-direction) rotation into <code>r</code>.
  */
  public void getInverse(Rotation r)
  {
    Quaternion.conjugate(rep, r.rep);
  }
    
    /**
       Returns a deep copy of this Rotation.
    */
  public Object clone()
  {
    try
      {
	Rotation r = (Rotation)super.clone();
	r.rep = rep.copy();
	return r;
      }
    catch (CloneNotSupportedException e)
      {
	e.printStackTrace();
	return null;
      }
  }

    /**
       Convenience wrapper around clone(). Saves the hassle of casting an Object to a Rotation.
    */
  public Rotation copy()
  {
    return (Rotation)clone();
  }

    /**
       Returns a String representation.
    */
  public String toString()
  {
    Vec3 axis = new Vec3();
    double angle = toAxisAngle(axis);
    return new String("[angle = " + angle +
		      "\taxis = (" + axis.x() + "\t" + axis.y() + "\t" + axis.z()
		      +")]");
  }
  
  
}






