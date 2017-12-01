package innards.math.rbf;

import java.io.*;

/**
   This RadialBasisFunction is a simple triangular hat function.  It has a peak of 1.0 at <code>r = 0</code>, then decays to zero at <code>r = support radius</code>, where <code>support radius</code> is user-specified.( the function is zero outside of r = support radius. )   
*/

//	derivativeAt call added by naimad

public class HatBasis implements RadialBasisFunction, Serializable
{
    private double support = 1.0;

    /**
       Constructor. Sets support radius to 1.0.
    */
    public HatBasis()
    {
    }

    /**
       Constructor.
    */
    public HatBasis(double support)
    {
	this.support = support;
    }

    /**
       Returns the support radius, or the smallest <code>r</code> such that <code>evaluate(r) == 0</code>.
    */
    public double getSupport()
    {
	return support;
    }

    /**
       Sets the support radius, or the smallest <code>r</code> such that <code>evaluate(r) == 0</code>.
    */
    public void setSupport(double support)
    {
	this.support = support;
    }

    public double evaluate(double r)
    {
	if (r < 0.0) r = Math.abs(r);
	if (r > support) return 0.0;
	return 1.0 - r/support;
    }

    /**
       Sets the support radius, or the smallest <code>r</code> such that <code>evaluate(r) == 0</code>.
    */
    public void setCharacteristicWidth(double alpha)
    {
	setSupport(alpha);
    }

    /**
       Returns the support radius, or the smallest <code>r</code> such that <code>evaluate(r) == 0</code>.
    */
    public double getCharacteristicWidth()
    {
	return getSupport();
    }
  
    public double derivativeAt(double r) {
	if (r > support) return 0.0;
	else return -1.0/support;
    }
  
}
