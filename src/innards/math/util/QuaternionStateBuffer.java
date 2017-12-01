package innards.math.util;


import innards.math.linalg.*;
import innards.*;

/** 
    QuaternionStateBuffer.

    Stores the state ("position" and derivative) of a quaternion as
    well as offering support for access to the previous value and a
    local buffer. 
*/
public class QuaternionStateBuffer extends NamedObject
{
  private Quaternion last_committed_value = new Quaternion();
  private Quaternion current_value = new Quaternion();
  private QuaternionNumericalDerivative deriv = new QuaternionTwoSampleDerivative();
  private double last_commit_time = 0.0;

    /**
       Constructor.
       @param name The name of this NamedObject.
    */
  public QuaternionStateBuffer(String name)
  {
    super(name);
  }

    /**
       Copies the last committed value to input.
    */
  public void getCommittedValue(Quaternion q)
  {
    q.setValue(last_committed_value);
  }

    /**
       Copies the current (uncommitted) to input.
    */
  public void getCurrentValue(Quaternion q)
  {
    q.setValue(current_value);
  }

    /**
       Sets the current value to <code>q</code>. Committed value is unaffected.
    */
  public void setCurrentValue(Quaternion q)
  {
    current_value.setValue(q);
  }

    /**
       Calculates the numerical derivative at the time of the last committed value and copies it to <code>dq</code>.
      @see #setDerivativeFunction
    */
  public void getCommittedDerivative(Quaternion dq)
  {
    deriv.getDerivative(dq);
  }

  /**
     Calculates the numerical derivative using the current value, the last committed value, and the supplied differential time. 
      @param dt Time difference between the last committed value and the current value.
      @param dq Answer written to this parameter.
      @see #setDerivativeFunction
  */
  public void getCurrentDerivative(double dt, Quaternion dq)
  {
    deriv.calcDerivativeWithoutAddingSample(current_value,
					    dt,
					    dq);
  }

    /**
       Commits the current value, with an associated time <code>t</code>.
    */
  public void commitState(double t)
  {
    double dt = t - last_commit_time;
    last_commit_time = t;
    //deriv.addSample(current_value, dt);
    last_committed_value.setValue(current_value);
  }

    /**
       Sets the QuaternionNumericalDerivative implementor used to calculate derivatives.
    */
  public void setDerivativeFunction(QuaternionNumericalDerivative
				    deriv_func)
  {
    deriv = deriv_func;
  }

    /**
       Clears the state.
    */
  public void clearState()
  {
    last_committed_value.setIdentity();
    current_value.setIdentity();
    last_commit_time = 0.0;
    deriv.init();
  }
}

