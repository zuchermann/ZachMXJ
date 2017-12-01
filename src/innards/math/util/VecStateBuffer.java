package innards.math.util;

import innards.math.linalg.*;
/** 
    VecStateBuffer.

    Stores the state (value and derivative) of a Vec as
    well as offering support for access to the previous value and a
    local buffer. 
*/
public class VecStateBuffer 
{
  private Vec last_committed_value;
  private Vec current_value;
  private VecNumericalDerivative deriv;
  private double last_commit_time = 0.0;

    /**
       Constructor.
       @param n The dimensionality of the Vecs to be stored.
    */
  public VecStateBuffer(int n)
  {
    initState(n);
  }
       /**
       Copies the last committed value to input.
    */
  public void getCommittedValue(Vec v)
  {
    v.setValue(last_committed_value);
  }
    /**
       Copies the current (uncommitted) to input.
    */
  public void getCurrentValue(Vec v)
  {
    v.setValue(current_value);
  }
    /**
       Sets the current value to <code>v</code>. Committed value is unaffected.
    */
  public void setCurrentValue(Vec v)
  {
    current_value.setValue(v);
  }
      /**
       Calculates the numerical derivative at the time of the last committed value and copies it to <code>dv</code>.
      @see #setDerivativeFunction
    */
  public void getCommittedDerivative(Vec dv)
  {
    deriv.getDerivative(dv);
  }
  /**
     Calculates the numerical derivative using the current value, the last committed value, and the supplied differential time. 
      @param dt Time difference between the last committed value and the current value.
      @param dx Answer written to this parameter.
      @see #setDerivativeFunction
  */
  public void getCurrentDerivative(double dt, Vec dx)
  {
    deriv.calcDerivativeWithoutAddingSample(current_value, dt, dx);
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
       Sets the VecNumericalDerivative implementor used to calculate derivatives.
    */
  public void setDerivativeFunction(VecNumericalDerivative
				    deriv_func)
  {
    deriv = deriv_func;
  }
      /**
       Clears the state.
    */
  public void clearState()
  {
    last_committed_value.zero();
    current_value.zero();
    last_commit_time = 0.0;
    deriv.init();
  }


  private void initState(int n)
  {
    current_value = new Vec(n);
    last_committed_value = new Vec(n);
    deriv = new VecTwoSampleDerivative(n);
  }
}

