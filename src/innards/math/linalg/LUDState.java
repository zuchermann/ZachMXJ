package innards.math.linalg;

/**
  Represents a matrix's LU decomposition, which can be used to back-substitute
  to solve <code>Ax = b</code> for x.<P> 
  The same LUDState may be used in multiple solves with different <code>b</code>.<P>
  <B>This class is immutable.</B>
  @see innards.math.linalg.Matrix@decomposeLU()
*/

public class LUDState
{
	/*
	  Rep
	************/

	/**
	   Should be made private.
	*/
	protected Matrix lud;

	/**
	   Should be made private.
	*/
	protected int[] permutation;

	/**
	   Should be made private.
	*/
	protected int d;

	/** 
	Protected constructor.  Used by the Matrix class in its decomposeLU method. 
	@see innards.math.linalg.Matrix#decomposeLU()
	*/
	protected LUDState(Matrix m, int[] permutation, int din)
	{
		lud= m;
		this.permutation= permutation;
		d= din;

	}

	/**
	   Back-substitutes to solve for <code>x</code> in <code>A * x = b</code>. 
	    
	   @return x
	*/
	public Vec backSub(Vec b)
	{
		Vec x= new Vec(b.dim());
		backSub(b, x);
		return x;
	}

	/**
	   Back-substitutes to solve for <code>x</code> in <code>A * x = b</code>. 
	   <code>x</code> and <code>b</code> may be the same object.
	
	   @param b input. The product of <code>A</code> and <code>x</code>.
	   @param x output. Solution stored here.  Must match in dimension.
	   @exception DimensionMismatchException when x.dim() != b.dim()
	
	*/
	public void backSub(Vec b, Vec x)
	{
		if (b.dim() != x.dim())
		{
			throw new DimensionMismatchException("backSub(): x and b must be same dimension");
		}

		/* if same, no need! */
		if (x != b)
			x.copyFrom(b);

		int i, ii= -1, ip, j;
		double sum;
		int n= lud.numRows();

		for (i= 0; i < n; i++)
		{
			ip= permutation[i];
			sum= x.rep[ip];
			x.rep[ip]= x.rep[i];
			if (ii > -1)
			{
				// aries shoudl the j<=i-1 be <?
				for (j= ii; j <= i - 1; j++)
					sum -= lud.rep[i][j] * x.rep[j];
			} else if (sum != 0.0)
			{
				ii= i;
			}

			x.rep[i]= (float) sum;
		}

		for (i= n - 1; i >= 0; i--)
		{
			sum= x.rep[i];
			for (j= i + 1; j < n; j++)
				sum -= lud.rep[i][j] * x.rep[j];
			x.rep[i]= (float) (sum / lud.rep[i][i]);
		}
	}

	public void backSub(float[] b, float[] x)
	{
		int i, ii= -1, ip, j;
		double sum;
		int n= lud.numRows();

		for (i= 0; i < n; i++)
		{
			ip= permutation[i];
			sum= x[ip];
			x[ip]= x[i];
			if (ii > -1)
			{
				// aries shoudl the j<=i-1 be <?
				for (j= ii; j <= i - 1; j++)
					sum -= lud.rep[i][j] * x[j];
			} else if (sum != 0.0)
			{
				ii= i;
			}

			x[i]= (float) sum;
		}

		for (i= n - 1; i >= 0; i--)
		{
			sum= x[i];
			for (j= i + 1; j < n; j++)
				sum -= lud.rep[i][j] * x[j];
			x[i]= (float) (sum / lud.rep[i][i]);
		}
	}

	/**
	   Calculates the determinant from the LU decomposition.
	*/
	public double determinant()
	{
		double out= d;
		for (int j= 0; j < lud.numRows(); j++)
			out *= lud.ref(j, j);
		return out;
	}

	/*
	  Plastic wrap below
	********************/
	protected static void error(String s)
	{
		System.err.println("(LUDState): " + s);
	}

}
