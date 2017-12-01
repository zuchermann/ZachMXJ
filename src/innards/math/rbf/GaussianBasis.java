package innards.math.rbf;

/**
   Calculates a Gaussian Radial Basis Function of the form:<P>
   <code>exp( (-r*r) / width)</code>, <P>
   where width is supplied by the user.
   
*/
//   derivativeAt call added by naimad


import java.io.*;

public final class GaussianBasis implements RadialBasisFunction, Serializable
{
    private double width = 1.0;
    
    /**
       Constructor. Sets width to 1.0.
    */
    public GaussianBasis() { }

    /**
       Constructor.
       @param width The standard deviation of the Gaussian.
    */
    public GaussianBasis(double width)
    {
	this.width = width;
    }

    /**
       Returns the standard deviation.
    */
    public double getWidth()
    {
	return width;
    }

    /**
       Sets the standard deviation.
    */
    public void setWidth(double width)
    {
	this.width = width;
    }
  
    /**
       Sets the standard deviation.
    */
  public void setCharacteristicWidth(double alpha)
  {
    setWidth(alpha);
  }

    /**
       Returns the standard deviation.
    */
  public double getCharacteristicWidth()
  {
    return getWidth();
  }

  public double evaluate(double r)
    {
    r = Math.abs(r);
    return Math.exp((-r*r)/width);
    }
    
    
    public double derivativeAt(double r) {
    	return evaluate(r)*(-2.0*r)/width;
    }

    /**
       Test driver method.
    */
    public static void main(String [] args) {
        double distance = 1;
        GaussianBasis gbf = new GaussianBasis(distance *10);
        for(double i = 0; i < 3; i+= 0.2) System.out.println("i " + i + " result " + gbf.evaluate(i));
        System.out.println("at distance " + gbf.evaluate(distance));
        
    }
}
