package innards.math.linalg;

/**
   Interface for classes that calculate a numerical derivative of a quaternion function given  local samples from the function.
*/
public interface QuaternionNumericalDerivative
{
    /**
       Adds a sample at distance dt from the last sample. <P>
       (If this is used to add the first sample, dt is ignored.)
    */
    public void addSample(Quaternion x, double dt);

    /**
       Calculates the derivative from previously added samples.
    */
    public void getDerivative(Quaternion deriv);

    /**
       Calculates a derivative from the input arguments, ignoring any stored samples.
    */
    public void calcDerivativeWithoutAddingSample(Quaternion q, double dt, Quaternion dq);

    /**
       Clears all stored samples and wipes the state.
    */
    public void init();
}
