package innards.math.util;

	
/**	
	A structure for solving the multivariate interpolation problem
	using using a simple first order linear fit.

	@author Michael Patrick Johnson <aries@media.mit.edu>
	@author mattb
*/		

import java.io.*;
import java.util.Vector;

import innards.debug.Debug;
import innards.math.linalg.*;

import innards.math.rbf.RBFInterpolator.*;

public class LinearInterpolator implements Serializable
{

    private Vector examples = new Vector();
    private Vector linear_weights;
    private int input_dimension;
    private int output_dimension;
    
    /**
       Constructor. All data is copied to internal representations.
       @param input_dimension the number of dimensions in the domain.
       @param output_dimension the number of dimensions in the range.
       @param inputs the domain points to interpolate between, given as a Vector of Vecs.
       @param outputs the function values corresponding to the points in <code>inputs</code>. Given as a Vector of Vecs. Must contain the same number of Vecs as <code>inputs</code>.
    */
    public LinearInterpolator(int input_dimension, 
			   				int output_dimension,
							Vector inputs, 
							Vector outputs)
    {
		Debug.doAssert(inputs.size() == outputs.size(),
					 "Num examples for input vectors and outputs must be the same.");
		
		int n = inputs.size();
		examples = new Vector(n);
		
		int i;
		for (i=0; i<n; i++)
	    {
			examples.addElement(new Example((Vec)inputs.elementAt(i),
							(Vec)outputs.elementAt(i)));
	    }
	      
		this.input_dimension = input_dimension;
		this.output_dimension = output_dimension;
				
		linear_weights = new Vector(outputDimension());
		for (i=0; i<outputDimension(); i++)
		    linear_weights.addElement(new Vec(inputDimension() + 1));
				
		findWeights();
    } 
    
    /**
       Returns the number of dimensions in the domain.
    */
    final public int inputDimension()
    {
    		return input_dimension;
    }
    
    /**
       Returns the number of dimensions in the range.
    */
    final public int outputDimension()
    {
    		return output_dimension;
    }

    /**
       Returns the number of interpolation samples stored.
    */
    final public int numExamples()
    {
    		return examples.size();
    }

    /** 
	Evaluates the function at <code>x</code>, by interpolating between stored examples. Stores the result in <code>y</code>.
	@param x (Input) the domain point to evaluate the function at.
	@param y (Output) the approximate function value at <code>x</code>.
    */
    public void interpolate(Vec x, Vec y)
    {
		for (int j=0; j<outputDimension(); j++)
		    y.set(j, interpolateLinear(x,j));
    }

    /**
       Evaluates a particular dimension of the function at point <code>x</code>.
       @param x The point to evaluate the function at.
       @param component The dimension of the function value to return.
       @return The <code>component</code>-th dimension of <code>f(x)</code>
    */
    public double interpolateComponent(Vec x, int component)
    {
		return interpolateLinear(x,component);
    }
    
    /* private */
    
    private double interpolateLinear(Vec x, int j)
    {
		Vec w = getLinearWeights(j);
		double sum = 0.0;
		for (int i=0; i<inputDimension()+1; i++)
		    sum += w.get(i) * linearBasis(x, i);
		return sum;
    }
    
    private Vec getLinearWeights(int output)
    {
    		return (Vec)linear_weights.elementAt(output);
    }
    
    private void findWeights()
    {
    		Vector residuals = solveLinearPortion(linear_weights);
    }
    
    
    
    /** fill in the d vectors with the linear weights solved for
	with a least squares fit on the linear basis.
	Returns the residual Ld - b of the fit for each output.
    */
    private Vector solveLinearPortion(Vector weights)
    {
	    
		Matrix a = makeLinearDesignMatrix();
		SVD svd = null;
		try
		{
			svd = new SVD(a);
		}
		catch (SVDException e)
		{
			Debug.doAssert(false, "solveLinearPortion: SVDException: " + e);
		}
		catch (DimensionMismatchException e)
		{
			Debug.doAssert(false, "solveLinearPortion: bas dimensions. " + e);
		}
	
		Vector residuals = new Vector(outputDimension());
		for  (int j = 0; j<outputDimension(); j++)
		{
			Vec b = new Vec(numExamples());
			for  (int i=0; i<numExamples(); i++)
			    b.set(i, getOutputs(i).get(j));
			Vec d = (Vec)weights.elementAt(j);
			svd.solve(b, d);
			
			Vec residual = new Vec(b);
			Matrix.mult(a, d, residual);
			Vec.sub(b, residual, residual);
			residuals.addElement(residual);
	    }
		
		return residuals;
    }
    
    private Matrix makeLinearDesignMatrix()
    {
		int n = numExamples();
		int m = inputDimension()+1;
		Matrix a = new Matrix(n,m);
		int i,j;
		for (i=0; i<n; i++)
		    for (j=0; j<m; j++)
			a.set(i,j,linearBasis(getInputs(i), j));
		return a;
    }

    /** this is the linear part.  The ith basis is the i+1th component of
	the input x, with i = 0 being the constant factor 1.0
    */
    private double linearBasis(Vec x, int i)
    {
		if (i == 0)
		    return 1.0;
		else
		    return x.get(i-1);
    }
  
    
    /** return the ith example */
    public Example getExample(int i)
    {
    		return (Example)examples.elementAt(i);
    }
    
    private Vec getInputs(int i)
    {
    		return getExample(i).getInput();
    }

    private Vec getOutputs(int i)
    {
    		return getExample(i).getOutput();
    }
}
