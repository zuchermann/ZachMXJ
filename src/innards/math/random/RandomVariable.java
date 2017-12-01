package innards.math.random;

import java.io.Serializable;

/**
  An abstract class for creating random variables which draw from various distributions.  Each subclass should implement a different distribution.
  */
public abstract class RandomVariable implements Serializable
{
	protected static Random generator= new Random();

	public RandomVariable()
	{

	}

	/** sample from the variable */
	public abstract double sample();

	/** seed the generator.  THIS AFFECTS ALL VARIABLES. */
	public static void seed(long newSeed)
	{
		generator.setSeed(newSeed);
	}

	/*
	  public static void autoSeed()
	  {
	  
	  }
	  */

	public Object clone()
	{
		try
		{
			return super.clone();
		}
		catch (CloneNotSupportedException e)
		{
			return null;
		}
	}

}
