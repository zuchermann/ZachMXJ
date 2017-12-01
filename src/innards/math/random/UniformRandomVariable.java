package innards.math.random;

import java.io.Serializable;

/**
  Implements a uniform random variable over an interval.
  Samples are in the half-closed interval [low, high)
  */
public class UniformRandomVariable extends RandomVariable implements Cloneable, Serializable
{

	private double lowBound;
	private double highBound;

	/**********************
	  Constructors
	  *********************/

	/** interval is [0, 1) */
	public UniformRandomVariable()
	{
		super();
		lowBound= 0.0;
		highBound= 1.0;

	}

	/** 
	  interval is [l, h)
	  @param l low bound
	  @param h high bound
	  */
	public UniformRandomVariable(double l, double h)
	{
		super();
		lowBound= l;
		highBound= h;
	}

	public String toString()
	{
		String s= "{ low " + lowBound + "  high " + highBound + " }";
		return s;
	}

	/*********************
	  Mutators
	  ********************/

	/** change the interval */
	public void setBounds(double l, double h)
	{
		lowBound= l;
		highBound= h;
	}

	/**********************
	  Observers
	  *********************/

	/** sample from the interval */
	public double sample()
	{
		double ret;

		ret= super.generator.nextDouble();
		double tmp= ret * (highBound - lowBound) + lowBound;
		//      System.out.println("tmp was " + tmp);
		return tmp;
	}

	/** copy the object */
	public UniformRandomVariable copy()
	{
		return (UniformRandomVariable) clone();
	}

	public Object clone()
	{

		Object foo= super.clone();
		return foo;
	}

	protected static void parseUsage(String s)
	{
		System.err.println("UniformRandomVariable: parse expected { low <low_val> high <high_val> } but got " + s);
	}

	public static void main(String args[])
	{

		int x;
		int size= 1000;
		double storedValues[]= new double[size];
		double mean= 0.0;
		double var= 0.0;

		GaussianRandomVariable gaussianR= new GaussianRandomVariable(5.0, 9.0);
		RandomVariable.seed(199);
		for (x= 0; x < size; x++)
		{
			storedValues[x]= gaussianR.sample();
			mean += storedValues[x];
			System.out.println(gaussianR.sample());
		}
		mean /= size;
		for (x= 0; x < size; x++)
		{
			var += (storedValues[x] - mean) * (storedValues[x] - mean);
		}
		var /= (size + 1);

		System.out.println(mean);
		System.out.println(var);
	}

}
