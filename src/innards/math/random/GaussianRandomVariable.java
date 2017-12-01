package innards.math.random;

import java.io.Serializable;

/**
  Gaussian (normal) distributed random variable 
  */
public class GaussianRandomVariable extends RandomVariable implements Cloneable, Serializable
{

	private double mean;
	private double variance;
	private int iset;
	private double gset= 0.0;

	/**********************
	  Constructors
	  *********************/

	/** mean: 0  var: 1 */
	public GaussianRandomVariable()
	{
		super();
		mean= 0.0;
		variance= 1.0;
		iset= 0;

	}

	/** mean: m  var: v 
	  @param m mean of distribution
	  @param v variance of distribution 
	  */
	public GaussianRandomVariable(double m, double v)
	{
		super();
		mean= m;
		variance= v;
		iset= 0;
	}

	public String toString()
	{
		String s= "{ mean " + mean + "  variance " + variance + " }";
		return s;
	}

	/**********************
	  Mutators
	  *********************/
	public void setMeanAndVariance(double m, double v)
	{
		mean= m;
		variance= v;
	}

	/**********************
	  Observers
	  *********************/

	public double sample()
	{
		double fac, r, v1, v2;

		if (iset == 0)
		{
			do
			{
				v1= 2.0 * super.generator.nextDouble() - 1.0;
				v2= 2.0 * super.generator.nextDouble() - 1.0;
				r= v1 * v1 + v2 * v2;
			}
			while (r >= 1.0);

			fac= Math.sqrt(-2.0 * Math.log(r) / r);
			gset= v1 * fac;
			iset= 1;
			return (Math.sqrt(variance) * v2 * fac + mean);
		}
		else
		{
			iset= 0;
			return (Math.sqrt(variance) * gset + mean);
		}

	}

	public GaussianRandomVariable copy()
	{
		return (GaussianRandomVariable) clone();
	}

	public Object clone()
	{
		Object foo= super.clone();
		return foo;
	}

	protected static void parseUsage(String s)
	{
		System.err.println("GauusianRandomVariable.parse(String s): expected { mean <mean> variance <variance> }  and got " + s);
	}

}
