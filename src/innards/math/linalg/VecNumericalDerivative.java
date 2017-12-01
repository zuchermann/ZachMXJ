package innards.math.linalg;

import java.io.*;

/**
   An interface for classes that calculate the numerical derivative of a Vec function, given local samples of the function.
*/
public interface VecNumericalDerivative extends Serializable
{
    /**
       Adds a sample.
    */

  public void addSample(Vec x, double t);
   /**
       Calculates the derivative from added samples.
    */
  public void getDerivative(Vec deriv);
    /**
       Calculates a derivative without adding samples to this.
    */
  public void calcDerivativeWithoutAddingSample(Vec x, double dt, Vec dx);
    /**
       Clears all added samples and wipes the state.
    */
  public void init();
}
