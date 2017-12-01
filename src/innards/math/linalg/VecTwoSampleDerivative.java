package innards.math.linalg;

/**
   Implements VecNumericalDerivative. Calculates Vec derivatives given two samples and a <code>dt</code>.
*/ 
public class VecTwoSampleDerivative
  implements VecNumericalDerivative
{
  private Vec last_x;
  private Vec now_x;
  private Vec dx;

    /**
     * Constructor.
     * @param n the dimension of the vectors to be differentiated.
     */
  public VecTwoSampleDerivative(int n)
  {
    last_x = new Vec(n);
    now_x = new Vec(n);
    dx = new Vec(n);
  }


    /**
       Clears all stored samples and wipes the state.
     */
  public void init()
  {
    last_x.scale(0.0);
    now_x.scale(0.0);
    dx.scale(0.0);
  }

    /**
      Adds a sample.
    */
  public void addSample(Vec x, double dt)
  {
    calcDeriv(x, dt, this.dx);
    now_x.setValue(x);
    last_x.setValue(now_x);
  }

    /**
       Calculates the derivative from added samples.
    */
  public void getDerivative(Vec dx)
  {
    dx.setValue(this.dx);
  }



    // FIXME check this - marc
  private void calcDeriv(Vec x, double dt, Vec dx)
  {
    // if no time passed essentially, ignore this sample.
    if (dt < 1.0e6)
    {
      return;
    }
    
    Vec.sub(x, last_x, dx);
    dx.scale(1.0/dt);
  }

    /**
       <code>dx = (x - <most recent sample>)/dt</code><P>
     */
    // FIXME check this - marc
  public void calcDerivativeWithoutAddingSample(Vec x, double dt, Vec dx)
  {
    Vec.sub(x, last_x, dx);
    dx.scale(1.0/dt);  
  }
}

