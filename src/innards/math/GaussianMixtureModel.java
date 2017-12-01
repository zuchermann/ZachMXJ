package innards.math;

import java.util.Random;

import innards.math.linalg.*;
import innards.math.random.GaussianRandomVector;
import innards.math.iProbabilityDistribution;

/**
 * @author mattb
 */
public class GaussianMixtureModel implements iProbabilityDistribution
{
	protected GaussianRandomVector[] classes;
	protected double[] classPriors;
	
	public GaussianMixtureModel(GaussianRandomVector[] classes, double[] classPriors)
	{
		this.classes = classes;
		this.classPriors = classPriors;
		if (classes.length != classPriors.length)
		{
			throw new DimensionMismatchException("number of class priors must match the number of classes");
		}
	}
	
	public GaussianRandomVector[] getClasses()
	{
		return classes;
	}
	
	public double[] getClassPriors()
	{
		return classPriors;
	}
	
	/**
	 * returns the density of the distribution at the specified point
	 */
	public double evaluate(Vec point)
	{
		double p = 0;
		for (int i=0; i<classes.length; i++)
		{
			p += classes[i].getValue(point) * classPriors[i];
		}
		return p;
	}
	
	protected static Random rand = null;
	
	/**
	 * draw a random sample from this distribution
	 */
	public void sample(Vec v)
	{
		if (rand == null)
		{
			rand = new Random();
		}
		
		double r = rand.nextDouble();
		int classIndex;
		for (classIndex = 0; classIndex < classes.length-1; classIndex++)
		{
			r -= classPriors[classIndex];
			if (r < 0)
			{
				break;
			}
		}
		
		classes[classIndex].sample(v);
	}
	
	public String toString()
	{
		String s= "GaussianMixtureModel {\n";
		for (int i=0; i<classes.length; i++)
		{
			s = s + "prior <"+classPriors[i]+">:\n"+classes[i];
		}
		s = s + "}\n";
		return s;
	}
}
