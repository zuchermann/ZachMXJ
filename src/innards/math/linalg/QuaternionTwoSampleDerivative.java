package innards.math.linalg;

import innards.*;


  /** 
    
    For a quaternion function q(t), this class calculates the derivative qdot(t0), 
    given two close samples q(t0) and q(t0+dt). <P>
    
    The rest of this header describes the math used.<P>
    The following formula is used:
    <P>
    qdot(t0) = q(t0)*ln(w), 
    <P>
    where ln(w) = (1/dt)ln(q_conj(t0)*q(t0+dt))
    <P>
    "q_conj" is the conjugate of q. The rest of this class description derives the above formula.
    <P>
    Given q(t0) and q(t0+dt), the rotation between them is constructed by 
    by <P>
    r = q_conj(t0) * q(t0+dt). <P>
    Note that for unit quaternions, q_conj also q's inverse.
    Assuming q(t0) and q(t0+dt) are unit quaternions that are close together, then r is 
    near the identity quaternion I.  The great arc (geodesic path over the quaternion sphere)
    passing through I and r is defined by the parametrized equation
    r^t, where ^ denotes exponentiation.  
    
    <P>
    

    
    Since the path between the unit quaternions q(t0) and q(t0+dt) is geodesic,
    q(t0+dt) is a constant-axis rotation of q(t0) in the 4-d quaternion sphere. 
    (Think of a globe with an arrow pointing from the center of the globe to Boston.
    Imagine another arrow from the center of the globe pointing to Seattle, a city at
    roughly the same latitude. Trace a geodesic path from Boston to Seattle. Now if 
    you moved the end of the Boston arrow along this path, you would be rotating the 
    Boston arrow around a fixed axis, in this case the north-south axis.)

    Coming back to our derivation, we have a fixed axis in the quaternion sphere around 
    which q(t0) rotates slightly to become q(t0+dt). We then assume that the
    angular velocity around this axis is constant between the samples.
    Since <P>
    r = q_conj(t0) * q(t0+dt), <P>
    then<P>
    q_(t0+delta) = q_(t0)* r^(1). <P>
    Assuming we are
    looking for a constant angular velocity at this point, we assume
    there is some tiny rotation parametrized by time that we add on to
    the point we are at.  Call this tiny rotation w:<P>
    w = [cos(omega*t), N sin(omega*t)]<P> 
    which defines a constant angular velocity rotation parametrized by t, 
    around axis (unit vector) N. Since we know that r, the tiny
    rotation between samples, is the amount of rotation in time dt,
    we have: <P>
    r = w^(dt)<P> 
    This means that <P>
    ln(r) = delta * ln(w),<P>
    which means that <P>
    (1/dt) ln(r) = ln(w) <P>
    and <P>
    w = r^(1/delta). <P>

    Given this, we know that q(t) for t near t0 can be written as <P>
    q(t) = q_(t0) * w^(t-t0).  <P>
    This defines a constant angular velocity curve
    from q_(t0) forward in time by t at constant angular velocity
    given by w.  Taking the derivative of this,
    <P>
    (d/dt)q(t) = (d/dt)q_(t0) w^t 
    <P>
    = q_(t0) * (d/dt) w^t 
    <P>
    = q_(t0) * w^(t-t0) ln(w).
    <P>

    Evaluating this at t0 gives q(t0) ln(w), which is the derivative
    at q_(t0), what we desire. 

    <P>
    In order to integrate forward a small timestep from the point
    q((t0) which has derivative qdot(t0), merely invert the above
    formula.  Specifically, a small timestep will add a small rotation
    to the current point, ie q((t0+dt) ~= q((t0) p(dt) where p(dt) is
    a small rotation parametrized by the timestep.  To find this, do
    the following:  q*_(t0) qdot_(t0) = ln(w).  Therefore, p(dt) =
    exp(ln(w) * dt) since ln(w) = V omega, dt * ln(w) = V omega dt = 
    V theta.  So exp(V theta) is the incremental rotation to add.
    QED.  The astute will notice that ln(w) is a tangent space
    attached to the point q((t0) (in this case) which means it is
    linear and Euclidean.  The more astute will notice that is is 1/2 *
    Omega where Omega is world coords angular velocity vector.
    
    <P>
    
    The above is MOSTLY right, but what we actually do is set the
    derivative at the last point to this estimate.  I.e. the avar must
    contain the derivative of the CURRENT point, which we assume is
    also the same as the last point, therefore this class returns the
    derivative at the LAST SAMPLE ADDED.
    
    <P>        As a short aside, note that r^t = exp(t * ln(r)) where ln(q) =
    ln([cos(theta), V sin(theta)]) = V theta where V denotes a vector.
    Therefore ln(q) is pure imaginary.  The inverse of ln(q),
    specifically exp(q), is therefore exp(q) = exp(V theta) =
    [cos(theta), V sin(theta)].  Notice that exponentiation is only
    defined for pure imaginary quaternions! (maybe this is not true in
    general, but this is all I use here).
   
    <P>
    
   @author Michael Patrick Johnson <aries@media.mit.edu> 
*/

public class QuaternionTwoSampleDerivative extends NamedObject
  implements QuaternionNumericalDerivative
{
  private Quaternion last_quat = new Quaternion();
  private Quaternion now_quat = new Quaternion();
  private Quaternion deriv = new Quaternion();
  private Quaternion temp_deriv = new Quaternion();

  public QuaternionTwoSampleDerivative(String name) {super(name);}

  public QuaternionTwoSampleDerivative(){super("(unnamed)");}

  /** Wipes the state */
  public void init()
  {
    last_quat.setIdentity();
    now_quat.setIdentity();
    deriv.setValue(0.0, 0.0, 0.0, 0.0);
    temp_deriv.setValue(0.0, 0.0, 0.0, 0.0);
  }

  /** 
      Adds a new sample ( {quaternion, time} pair). This will
      update the derivative to be the best guess of the derivative at
      this new point.
      <P>
      @param sample the float array which contains the quaternion rep
      of the sample.
      @param t time the sample is taken at.
  */
  public void addSample(Quaternion sample, double dt)
  {
    calcDeriv(sample, dt, deriv);

    now_quat.setValue(sample);
    // store back alst one
    last_quat.setValue(now_quat);
  }

    /**
       Writes the derivative, calculated from the added samples, into <code>dq</code>.
    */
  public void getDerivative(Quaternion dq)
  {
    dq.setValue(deriv);
  }

    /**
       Calculates the derivative from the added samples and the provided sample <code>q</code>, without adding <code>q</code>.
    */
  public void calcDerivativeWithoutAddingSample(Quaternion q, double dt, Quaternion dq)
  {
    calcDeriv(q, dt, dq);
  }


  private void calcDeriv(Quaternion new_q, double dt, Quaternion dq)
  {
 // System.out.println(" !!!! ! ! !");
    if (dt <= 1.0e-5)
    {
      // if samples are same time, derivative is zero to avoid div by
      // zero
      dq.scale(0.0);
    }
    else
    {
      // find rotation between samples
      last_quat.conjugate();
      Quaternion.mult(last_quat, new_q, dq);
      // replace state
      last_quat.conjugate();


      // go into tangent space and scale time
      Quaternion.ln(dq, dq);
      dq.scale(1.0/dt);

      // multiply by location to place tangent space on sphere.
      // yes, we use the new point!
      temp_deriv.setValue(dq);
      Quaternion.mult(new_q, temp_deriv, dq);
    }
  }
}
