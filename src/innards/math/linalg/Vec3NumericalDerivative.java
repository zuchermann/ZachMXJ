package innards.math.linalg;

/**
   An interface for classes that calculate the numerical derivative of a Vec3 function, given local samples of the function.
*/
public interface Vec3NumericalDerivative 
{
    /**
       Adds a sample.
    */
  public void addSample(Vec3 x, double t);
   /**
       Calculates the derivative from added samples.
    */
  public void getDerivative(Vec3 deriv);

    /**
       Calculates a derivative without adding samples to this.
    */
  public void calcDerivativeWithoutAddingSample(Vec3 x, double dt, Vec3 dx);

    /**
       Clears all added samples and wipes the state.
    */
  public void init();
}
