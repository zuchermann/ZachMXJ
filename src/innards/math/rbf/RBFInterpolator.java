package innards.math.rbf;

	
/**	
	A structure for solving the multivariate interpolation problem
	using RBFs.  This assumes a linear combination of RBFs
	augmented by a simple first order linear fit. 
	
	@author Michael Patrick Johnson <aries@media.mit.edu>
*/		

import java.io.*;
import java.util.Vector;

import innards.debug.Debug;
import innards.math.linalg.*;

public class RBFInterpolator implements Serializable
{
static final long serialVersionUID = -4958373802920387817L;
    private Vector examples = new Vector();
    private Vector rbf_weights;
    private Vector linear_weights;
    private int input_dimension;
    private int output_dimension;
    private RadialBasisFunction rbf;
    
    /**
       Constructor. All data is copied to internal representations.
       @param input_dimension the number of dimensions in the domain.
       @param output_dimension the number of dimensions in the range.
       @param inputs the domain points to interpolate between, given as a Vector of Vecs.
       @param outputs the function values corresponding to the points in <code>inputs</code>. Given as a Vector of Vecs. Must contain the same number of Vecs as <code>inputs</code>.
    */
    public RBFInterpolator(int input_dimension, 
			   int output_dimension,
			   Vector inputs, 
			   Vector outputs, 
			   RadialBasisFunction rbf)
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
	rbf_weights = new Vector(outputDimension());
	for (i=0; i<outputDimension(); i++)
	    rbf_weights.addElement(new Vec(numExamples()));
	linear_weights = new Vector(outputDimension());
	for (i=0; i<outputDimension(); i++)
	    linear_weights.addElement(new Vec(inputDimension() + 1));
	this.rbf = rbf;
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
	    y.set(j, interpolateLinear(x,j) + interpolateRadial(x,j));
    }

    /**
       Evaluates a particular dimension of the function at point <code>x</code>.
       @param x The point to evaluate the function at.
       @param component The dimension of the function value to return.
       @return The <code>component</code>-th dimension of <code>f(x)</code>
    */
    public double interpolateComponent(Vec x, int component)
    {
	return interpolateLinear(x,component) + interpolateRadial(x,component);
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

    private double interpolateRadial(Vec x, int j)
    {
	Vec w = getRadialWeights(j);
	double sum = 0.0;
	for (int i=0; i<numExamples(); i++)
	    sum += w.get(i) * rbf.evaluate(getExample(i).distanceFromExample(x));
	return sum;
    }
    
    private Vec getRadialWeights(int output)
    {
	return (Vec)rbf_weights.elementAt(output);
    }

    private Vec getLinearWeights(int output)
    {
	return (Vec)linear_weights.elementAt(output);
    }
    
    private void findWeights()
    {
	Vector residuals = solveLinearPortion(linear_weights);
	solveRadialPortion(rbf_weights, residuals);
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

    /** solve the radial weights for the residual r. */
    private void solveRadialPortion(Vector weights, Vector residuals)
    {
	SVD svd = null;
	try
	    {
		Matrix a = makeRadialDesignMatrix();
		svd = new SVD(a);
	    }
	catch (Exception e)
	    {
		Debug.doAssert(false,"what?" + e);
	    }
	
	for(int j=0; j<outputDimension(); j++)
	    {
		Vec r = (Vec)residuals.elementAt(j);
		Vec c = (Vec)weights.elementAt(j);
		svd.solve(r, c);
	    }
    }

    private Matrix makeRadialDesignMatrix()
    {
	int i,j;
	int n = numExamples();
	Matrix a = new Matrix(n,n);
	for(i=0; i<n; i++)
	    for (j=0; j<n; j++)
		a.set(i, j, 
		      rbf.evaluate(getExample(i).distanceFromExample(getInputs(j))));
	
	return a;
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

    /** this is an inner class for storing examples */
    public static class Example implements Serializable
    {
    	static final long serialVersionUID = -7873495106647562179L;
	private Vec x;
	private Vec y;
	private Vec d;
	
	/** this constructor copies storage of x */
	public Example(Vec x, Vec y)
	{
	    this.x = x.copy();
	    this.y = y.copy();
	    this.d = new Vec(x.dim());
	}

	public Vec getInput()
	{
	    return x;
	}

	public Vec getOutput()
	{
	    return y;
	}

	public double distanceFromExample(Vec new_x)
	{
	    Vec.sub(new_x, x, d);
	    return d.mag();
	}
    }

    /*
      Ayyayay. Return a jacobian matrix?
      This will be of dimensions output_dim X input_dim
      interpretable as a gradient for each dimension (one in each column)
	
      Say you were interpolating in n dimensions on an x,y input space. It would be
	
      (  x1		x2		...		xn  )
      (  y1		y2		...		yn  )
	
	
    */

    /**
       Returns the Jacobian matrix of the function, evaluated at point <code>in</code>.
       @param in The domain point to evaluate the gradient at.
       @return A matrix of the form:<P><code>
       ( dy1/dx1,  dy1/dx2,  ...,  dyN/dx1 )
       ( ...                               )
       ( dyN/dx1,  dyN/dx2,  ...,  dyN/dxM )</code>
    */ 
    public Matrix gradientAt(Vec in) {
    	
    	Matrix m = new Matrix(input_dimension, output_dimension);
    	Vec total = new Vec(input_dimension);
    	Vec temp = new Vec(input_dimension);
    	for (int c=0; c < output_dimension; c++) {
    		
	    //we need to calculate a gradient function...
	    Vec w = getLinearWeights(c);
	    for (int d=0; d < input_dimension; d++) {
		total.set(d, w.get(d+1));
	    }
    		
	    //add in examples...
	    double mag;
	    w = getRadialWeights(c);
	    for (int n=0; n < examples.size(); n++) {
		Example exn = (Example)examples.elementAt(n);
		Vec pos = exn.getInput();
		Vec.sub(pos, in, temp);
		mag = temp.mag();
		temp.normalize();
		temp.scale(w.get(n) * rbf.derivativeAt(mag));	//this also needs to be scaled by weight.
		Vec.add(temp, total, total);
	    }
    		
	    for (int d=0; d < input_dimension; d++) {
		m.set(d, c, total.get(d));
	    }
    	}
    	return m;
    }


    /**
       Test driver.
    */
    public static void main(String argh[])
    {
	RadialBasisFunction func = new GaussianBasis(1.0);
	Vector inputs = new Vector();
	Vector outputs = new Vector();
	loadData(argh[0], inputs, outputs);
	int d = ((Vec)inputs.elementAt(0)).size();
	int o = ((Vec)outputs.elementAt(0)).size();
    	
	System.out.println("Got data.");
	for (int i=0; i<inputs.size(); i++)
	    {
	        System.out.println("" + inputs.elementAt(i) + " = " + outputs.elementAt(i));
	    }
    	
	RBFInterpolator rbf = new RBFInterpolator(d, o, inputs, outputs, func);
    	
	double t = 0.0;
	double dt = .01;
	int num = (int)2+(int)((int)10.0/dt);
	double[] y = new double[num];
	double[] x = new double[num];
	int i;
	System.out.println("Interpolating...");
	Vec vx = new Vec(1);
	Vec vy = new Vec(1);
	for (t = 0.0, i=0; t <= 10.0; t += dt, i++)
	    {
	        x[i] = t;
	        vx.set(0, t);
	        rbf.interpolate(vx, vy);
	        y[i] = vy.get(0);
	    }

	try
	    {
	        Writer out = new FileWriter(argh[1]);
	        for (int j=0; j<i; j++)
		    {
			out.write("" + x[j] + " " + y[j] + "\n");
		    }
	        out.close();
	    }
	catch (Exception e)
	    {
	        System.out.println("oof! : " + e);
	    }

			  
    }

    // convenience method for main()
    private static void loadData(String filename, Vector inputs, Vector outputs)
    {
	try
	    {
		FileReader in = new FileReader(filename);
		StreamTokenizer tokenizer = new StreamTokenizer(in);
		tokenizer.parseNumbers();
		tokenizer.eolIsSignificant(false);
		tokenizer.slashSlashComments(true);

		tokenizer.nextToken();
		int n = (int)tokenizer.nval;
		inputs.removeAllElements();
		for (int i=0; i<n; i++)
		    {
			tokenizer.nextToken();
			double x = tokenizer.nval;
			tokenizer.nextToken();
			double y = tokenizer.nval;
			Vec vx = new Vec(1);
			vx.set(0, x);
			inputs.addElement(vx);
			Vec vy = new Vec(1); 
			vy.set(0, y);
			outputs.addElement(vy);
		    }
		in.close();
	    }
	catch (Exception e)
	    {
		System.err.println("dammit! " + e);
	    }
    }
}
