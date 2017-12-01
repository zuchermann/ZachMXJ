package innards.math.random;

import innards.math.linalg.*;

import java.io.Serializable;

/**
   
   EulerQuaternionRandomVariable
   
   Uses a uniform sampling on a range of the XYZ Euler set and creates
   a quaternion from it.  Not very correct, as uniform over all angles
   does NOT generate a uniform distribution on the group.  Good for
   quick tests though.

   @author Michael Patrick Johnson <aries@media.mit.edu> */



public class EulerQuaternionRandomVariable 
  implements QuaternionRandomVariable, Serializable
{
  
  private UniformRandomVariable xrv;
  private UniformRandomVariable yrv;
  private UniformRandomVariable zrv;
  
  private Quaternion quat_temp = new Quaternion();
  
  private static Vec3 X = new Vec3(1, 0, 0);
  private static Vec3 Y = new Vec3(0, 1, 0);
  private static Vec3 Z = new Vec3(0, 0, 1);
  

  /** Uniform over -PI..PI for each factor. */
  public EulerQuaternionRandomVariable()
  {
    this(-Math.PI, Math.PI,
	 -Math.PI, Math.PI,
	 -Math.PI, Math.PI);
  }

  public EulerQuaternionRandomVariable(double x_min,
				       double x_max,
				       double y_min, 
				       double y_max,
				       double z_min, 
				       double z_max)
  {
    xrv = new UniformRandomVariable(x_min, x_max);
    yrv = new UniformRandomVariable(y_min, y_max);
    zrv = new UniformRandomVariable(z_min, z_max);
  }
  
  public Quaternion sample()
  {
    Quaternion q = new Quaternion();
    sample(q);
    return q;
  }

  public void sample(Quaternion q)
  {
    q.setValue(Z, zrv.sample());
    quat_temp.setValue(Y, yrv.sample());
    q.concatLeft(quat_temp);
    quat_temp.setValue(X, xrv.sample());
    q.concatLeft(quat_temp);
  }

}

