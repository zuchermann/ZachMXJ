package innards.math.linalg;

import java.io.*;

import innards.debug.Debug;
import innards.math.random.UniformRandomVariable;

/** 
    This class performs an SVD (Singular Value Decomposition) on an
    mxn matrix, storing the results internally. This allows for solving the
    <code>x</code> in <code>A*x = b</code> for different <code>b</code> without
    recalculating the SVD each time.<P>
    SVD decomposes an mxn matrix A into a product of three matrices,
    <P>
    <code>A = U W V'</code>,<P>
    where U is an mxn column-orthogonal matrix, W is nxn diagonal matrix 
    with positive entries (w1 .. wn), and V is an nxn column-orthogonal matrix. 
    The apostrophe denotes transpose.<P>
    The orthogonal columns of U whose corresponding wi in W are nonzero form an 
    orthonormal basis of the range of A. Similarly, the columns of V (rows of V') 
    whose corresponding wi are nonzero form an orthonormal basis vectors of the 
    null-space of A.<P>

    SVD algorithm implemented as specified in Numerical Recipes In C, Section 2.9.

    @author Michael Patrick Johnson <aries@media.mit.edu>
*/
public class SVD
{
	// NRIC uses 30 always, probably good enough.
	private int max_iters= 30;
	private static double EPSILON= 1.0e-11;
	private double singular_threshold= 1.0e-6;

	/**
	   The default singular threshold is defined as the largest singular value w in W multiplied by <code>SINGULAR_VALUE_TOLERANCE</code>. The singular threshold is the value below which a singular value w is considered ill-conditioned and is interpreted to be zero when calling solve().
	   @see #solve(Vec, Vec)
	*/
	final public static double SINGULAR_VALUE_TOLERANCE= 1.0e-6;

	// this is U
	private Matrix rangeBasis;

	// this is W
	private Vec singularValues;

	//  the threshholded singular values
	private Vec threshSingularValues;

	// this is V'
	private Matrix nullspaceBasis;

	/** 
	    Creates an SVD object that can hold an mxn decomposiiton. 
	*/
	public SVD(int m, int n)
	{
		rangeBasis= new Matrix(m, n);
		singularValues= new Vec(n);
		threshSingularValues= new Vec(n);
		nullspaceBasis= new Matrix(n, n);
	}

	/**
	   Creates an SVD object that holds the decomposition of matrix <code>a</code>.
	*/
	public SVD(Matrix a) throws SVDException
	{
		this(a.numRows(), a.numColumns());
		performSVD(a);
	}

	/** 
	    Returns the number of rows.
	*/
	final public int numRows()
	{
		return rangeBasis.numRows();
	}

	/** 
	    Returns the number of columns.
	*/
	final public int numColumns()
	{
		return rangeBasis.numColumns();
	}

	/** 
	    Returns a copy of <code>U</code>, the basis matrix for the range.  The columns
	    of <code>U</code> whose same-numbered elements in <code>W</code> are nonzero form an
	    orthonormal basis for the range of the decomposed matrix.  
	*/
	public Matrix getRangeBasisMatrix()
	{
		Matrix range= new Matrix(numRows(), numColumns());
		getRangeBasisMatrix(range);
		return range;
	}

	/** 
	    Copies <code>U</code>, the basis matrix for the range, into the argument. The columns
	    of <code>U</code> whose same-numbered elements in <code>W</code> are nonzero form an
	    orthonormal basis for the range of the decomposed matrix.  
	    @param range the matrix to store the basis matrix in.
	    @exception DimensionMismatchException if the argument is not mxn.
	*/
	public void getRangeBasisMatrix(Matrix range) throws DimensionMismatchException
	{
		try
		{
			range.set(rangeBasis);
		}
		catch (DimensionMismatchException e)
		{
			throw new DimensionMismatchException(
				"SVD.getRangeBasisMatrix: required "
					+ numRows()
					+ "x"
					+ numColumns()
					+ " matrix and got "
					+ range.numRows()
					+ "x"
					+ range.numColumns());
		}
	}

	/**
	   Fills in the argument vector with the singular values of the decomposed matrix. These can be made into a diaginal matrix W using Matrix.diagonalMatrix(Vec);
	   
	   @param w the vector to fill in.
	   @exception DimensionMismatchException if w.size() < numColumns().
	*/
	public void getSingularValues(Vec w) throws DimensionMismatchException
	{
		if (w.size() < numColumns())
		{
			throw new DimensionMismatchException("SVD.getSingularValues: expected Vec(" + numColumns() + ") and got Vec(" + w.size() + ")");
		}
		w.set(singularValues);
	}

	/**
	   Returns a Vec containing the singular values of the decomposed matrix. These can be made into a diaginal matrix W using Matrix.diagonalMatrix( getSingularValues() );
	*/
	public Vec getSingularValues()
	{
		Vec w= new Vec(numColumns());
		getSingularValues(w);
		return w;
	}

	/**
	   Writes the null-space matrix V into the argument. Note this is V, <b>not</b> V'.
	   @param nullspace the matrix to fill in
	   @exception DimensionMismatchException if nullspace is not nxn.
	*/
	public void getNullspaceBasisMatrix(Matrix nullspace)
	{
		try
		{
			nullspace.set(nullspaceBasis);
		}
		catch (DimensionMismatchException e)
		{
			int n= rangeBasis.numColumns();
			throw new DimensionMismatchException(
				"SVD.getNullspaceValues(): expected " + n + "x" + n + " and got " + nullspace.numRows() + "x" + nullspace.numColumns());

		}
	}

	/**
	 Returns a copy of the nullspace matrix V.  Note that this is V, NOT V'.
	 @param nullspace the matrix to fill in
	 @exception DimensionMismatchException if nullspace is not nxn.
	*/
	public Matrix getNullspaceBasisMatrix()
	{
		Matrix nullspace= new Matrix(numColumns(), numColumns());
		getNullspaceBasisMatrix(nullspace);
		return nullspace;
	}

	/**
	   Gets the number of iterations to be tried in performSVD() before giving up and throwing an SVDException.  Default is 30.
	*/
	public int getMaxIters()
	{
		return max_iters;
	}

	/**
	   Sets the number of iterations to be tried in performSVD() before giving up and throwing an SVDException.  Default is 30.
	*/
	public void setMaxIters(int max_iters)
	{
		this.max_iters= max_iters;
	}

	/**
	   Returns the condition number of the matrix of this
	   decomposition.  Formally, the condition number is the ratio of
	   the largest singualar value to the smallest.  If it is positive
	   infinity, the matrix is singular.  If its reciprocal is close to
	   machine precision (about 1.0e-12) then the matrix is
	   ill-conditioned.
	*/
	public double conditionNumber()
	{
		double max= singularValues.max();
		double min= singularValues.min();

		// does this do the right thing for min = 0 or what?
		return max / min;
	}

	/**
	   Places the jth column of the null-space matrix V into vector <code>nullspace</code>. This vector is only a basis vector of the null-space if the jth singular value (jth element of W) is nonzero. 
	*/
	public void getNullspaceVector(int j, Vec nullspace)
	{
		Debug.doAssert(nullspace.size() >= numColumns(), "nullspace vector too small.");
		for (int i= 0; i < numColumns(); i++)
			nullspace.set(i, nullspaceBasis.get(i, j));
	}

	/**
	   Peforms the SVD of the given matrix.  The matrix is left untouched and the SVD object will contain a valid SVD unless an exception is thrown.
	   @param a the mxn matrix to decompose.  m >= n.  If not, use Matrix.resize(m,m) to make a zero-padded square.
	   @exception SVDException if there is an error in the SVD process.
	*/
	public void performSVD(Matrix a) throws SVDException
	{
		int m= a.numRows();
		int n= a.numColumns();
		/* if (m < n)
		   throw new SVDException("SVD.performSVD(): m < n.  Make matrix square by zero padding with zero rows.");
		*/
		// make sure the size are correct.
		rangeBasis.resize(m, n);
		singularValues.resize(n);
		nullspaceBasis.resize(n, n);

		// set this matrix to a since svdcmp destroys it.
		rangeBasis.set(a);

		svdcmp(rangeBasis.rep, m, n, singularValues.rep, nullspaceBasis.rep);

		// pick a reasonable default for the tolerance. 
		setSingularThresholdAsTolerance(SINGULAR_VALUE_TOLERANCE);
	}

	/**
	   Returns the singular threshold. The singular threshold is the value below which a singular value w is considered
	   ill-conditioned and is set to zero.
	*/
	public double getSingularThreshold()
	{
		return singular_threshold;
	}

	/**
	   Sets the singular threshold. The singular threshold is the value below which a 
	   singular value w is considered ill-conditioned and is set to zero.
	*/
	public void setSingularThreshold(double thresh)
	{
		singular_threshold= thresh;
	}

	/**
	   Sets the singular theshold to be a fraction (<code>tolerance</code>) of the maximum singular value. The singular threshold is the value below which a singular value w is considered
	   ill-conditioned and is set to zero. By default, the threshold is set to:<P>
	   <code>SINGULAR_VALUE_TOLERANCE * getSingularValues.max()</code>.
	*/
	public void setSingularThresholdAsTolerance(double tolerance)
	{
		setSingularThreshold(singularValues.max() * tolerance);
	}

	/**
	   Given that performSVD has been called on a matrix <code>A</code> and
	   <code>this</code> contains a valid decomposition, <code>solve</code> 
	   performs a backsubstitution to find the x in <code>A*x = b</code> that 
	   solves the system. For the overdetermined case (m>n), there may be no 
	   solution.  In this case it finds the nearest solution is a least squares sense.
	   
	   <P>
	   
	   In the case of an ill-conditioned matrix, it is desirable to
	   remove basis elements which are close to the nullspace.  This is
	   done by setting small singular values to zero.  This threshold is
	   often set to be a tolerance times the largest singular value.
	   The method setSingularThreshold() can be used to set the the
	   threshold value.  By default, it is set to a default fraction of
	   the largest singular value after an SVD is performed.  Notice
	   that this threshold is not noticable in the value of
	   getSingularValues().  It is used internally to solve only.
	   
	   @param b the right hand side.  Must be size() == m.
	   @param x the x is placed in this object.  It will be resize to n
	   if it is not.
	   @exception DimensionMismatchException if b.size() != numRows().
	   @see #setSingularThreshold(double)
	   @see #setSingularThresholdAsTolerance(double)
	*/
	public void solve(Vec b, Vec x) throws DimensionMismatchException
	{
		if (b.size() != numRows())
			throw new DimensionMismatchException("SVD.solve: expected b vector of dimension " + numRows() + "and got one of dimension " + b.size());

		// make sure it is good size
		x.resize(numColumns());

		// recache the threshholded values
		thresholdSingularValues();

		double s= 0.0;
		int n= numColumns();
		int m= numRows();
		Vec tmp= new Vec(n);
		for (int j= 0; j < n; j++)
		{
			s= 0.0;
			double w= threshSingularValues.get(j);
			// only add in vector if non-zero singular value
			// calculate Winv U'b
			if (w > 0.0)
			{

				for (int i= 0; i < m; i++)
					s += rangeBasis.get(i, j) * b.get(i);

				s /= w;
			}
			tmp.set(j, s);
		}

		for (int j= 0; j < n; j++)
		{
			s= 0.0;
			for (int jj= 0; jj < n; jj++)
				s += nullspaceBasis.get(j, jj) * tmp.get(jj);
			x.set(j, s);
		}
	}

	/**
	   Calculates the residual vector r. For a system of equations expressed as<BR>
	   <BLOCKQUOTE><code>A * x = b</code>, </BLOCKQUOTE><BR>
	   the residual is  <BR>
	   <BLOCKQUOTE><code>r = (A * x) - b</code>.</BLOCKQUOTE><P>
	   @param a the original matrix 
	   @param x the solution vector
	   @param b the right hand side
	   @param r the residual Ax-b is placed in here.
	   @exception DimensionMismatchException if the dimensions of
	   themultiply are wrong.
	*/
	public static void calculateResidual(Matrix a, Vec x, Vec b, Vec r) throws DimensionMismatchException
	{
		r.resize(a.numColumns());
		Matrix.mult(a, x, r);
		Vec.sub(r, b, r);
	}

	/* ********************************************
	   Private Parts
	************************************** */

	private void thresholdSingularValues()
	{
		int i;
		int n= singularValues.size();
		for (i= 0; i < n; i++)
		{
			if (singularValues.get(i) < singular_threshold)
				threshSingularValues.set(i, 0.0);
			else
				threshSingularValues.set(i, singularValues.get(i));
		}
	}

	/** This is the version 2.04 of the Numerical Recipes in C SVD,
	    Java-ized and made 0-based by me. 
	    @param a the matrix to be SVD'd, m>=n (zero pad if not).  This matrix is DESTROYED and replaced with <code>U</code>.
	    @param m rows of a.
	    @param n columns of a.
	    @param w the singular values are placed here.
	    @param v the v matrix is placed in here.     
	*/
	private void svdcmp(double[][] a, int m, int n, float[] w, double[][] v) throws SVDException
	{
		boolean flag= false;
		int i, its, j, jj, k;
		int l= 0;
		int nm= 0;
		double anorm, c, f, g, h, s, scale, x, y, z;

		double[] rv1= new double[n];
		g= scale= anorm= 0.0;
		for (i= 0; i < n; i++)
		{
			l= i + 1;
			rv1[i]= scale * g;
			g= s= scale= 0.0;
			if (i < m)
			{
				for (k= i; k < m; k++)
					scale += Math.abs(a[k][i]);
				if (scale != 0.0)
				{
					for (k= i; k < m; k++)
					{
						a[k][i] /= scale;
						s += a[k][i] * a[k][i];
					}
					f= a[i][i];
					g= -sign(Math.sqrt(s), f);
					h= f * g - s;
					a[i][i]= f - g;
					for (j= l; j < n; j++)
					{
						for (s= 0.0, k= i; k < m; k++)
							s += a[k][i] * a[k][j];
						f= s / h;
						for (k= i; k < m; k++)
							a[k][j] += f * a[k][i];
					}
					for (k= i; k < m; k++)
						a[k][i] *= scale;
				}
			}
			w[i]= (float)(scale * g);
			g= s= scale= 0.0;
			if (i < m && i != (n - 1))
			{
				for (k= l; k < n; k++)
					scale += Math.abs(a[i][k]);
				if (scale != 0.0)
				{
					for (k= l; k < n; k++)
					{
						a[i][k] /= scale;
						s += a[i][k] * a[i][k];
					}
					f= a[i][l];
					g= -sign(Math.sqrt(s), f);
					h= f * g - s;
					a[i][l]= f - g;
					for (k= l; k < n; k++)
						rv1[k]= a[i][k] / h;
					for (j= l; j < m; j++)
					{
						for (s= 0.0, k= l; k < n; k++)
							s += a[j][k] * a[i][k];
						for (k= l; k < n; k++)
							a[j][k] += s * rv1[k];
					}
					for (k= l; k < n; k++)
						a[i][k] *= scale;
				}
			}
			anorm= Math.max(anorm, (Math.abs(w[i]) + Math.abs(rv1[i])));
		}
		for (i= n - 1; i >= 0; i--)
		{
			if (i < n - 1)
			{
				if (g != 0.0)
				{
					for (j= l; j < n; j++)
						v[j][i]= (a[i][j] / a[i][l]) / g;
					for (j= l; j < n; j++)
					{
						for (s= 0.0, k= l; k < n; k++)
							s += a[i][k] * v[k][j];
						for (k= l; k < n; k++)
							v[k][j] += s * v[k][i];
					}
				}
				for (j= l; j < n; j++)
					v[i][j]= v[j][i]= 0.0;
			}
			v[i][i]= 1.0;
			g= rv1[i];
			l= i;
		}
		for (i= (Math.min(m, n) - 1); i >= 0; i--)
		{
			l= i + 1;
			g= w[i];
			for (j= l; j < n; j++)
				a[i][j]= 0.0;
			if (g != 0.0)
			{
				g= 1.0 / g;
				for (j= l; j < n; j++)
				{
					for (s= 0.0, k= l; k < m; k++)
						s += a[k][i] * a[k][j];
					f= (s / a[i][i]) * g;
					for (k= i; k < m; k++)
						a[k][j] += f * a[k][i];
				}
				for (j= i; j < m; j++)
					a[j][i] *= g;
			}
			else
				for (j= i; j < m; j++)
					a[j][i]= 0.0;
			a[i][i] += 1.0;
		}
		for (k= n - 1; k >= 0; k--)
		{
			for (its= 1; its <= max_iters; its++)
			{
				flag= true;
				for (l= k; l >= 0; l--)
				{
					nm= l - 1;
					/* NOTE Next line augemented by aries.  They say in the book
					   that rv1[1] == 0.0, so I assume the next line is assumed to be true but maybe some roundoff creeps in and fucks us so I check for the case where nm == -1 as well, otherwise we index arrays at -1 below.  I hope this is safe.
					*/
					if ((double) (Math.abs(rv1[l]) + anorm) == anorm || nm == -1)
					{
						flag= false;
						break;
					}
					if ((double) (Math.abs(w[nm]) + anorm) == anorm)
						break;
				}
				if (flag)
				{
					c= 0.0;
					s= 1.0;
					for (i= l; i <= k; i++)
					{
						f= s * rv1[i];
						rv1[i]= c * rv1[i];
						if ((double) (Math.abs(f) + anorm) == anorm)
							break;
						g= w[i];
						h= pythag(f, g);
						w[i]= (float)h;
						h= 1.0 / h;
						c= g * h;
						s= -f * h;
						for (j= 0; j < m; j++)
						{
							y= a[j][nm];
							z= a[j][i];
							a[j][nm]= y * c + z * s;
							a[j][i]= z * c - y * s;
						}
					}
				}
				z= w[k];
				if (l == k)
				{
					if (z < 0.0)
					{
						w[k]= (float)-z;
						for (j= 0; j < n; j++)
							v[j][k]= -v[j][k];
					}
					break;
				}
				if (its == max_iters)
					throw new SVDException("SVD.svdcmp(): no convergence after " + max_iters + " iterations.  Whassup wi' dat?");
				x= w[l];
				nm= k - 1;
				y= w[nm];
				g= rv1[nm];
				h= rv1[k];
				f= ((y - z) * (y + z) + (g - h) * (g + h)) / (2.0 * h * y);
				g= pythag(f, 1.0);
				f= ((x - z) * (x + z) + h * ((y / (f + sign(g, f))) - h)) / x;
				c= s= 1.0;
				for (j= l; j <= nm; j++)
				{
					i= j + 1;
					g= rv1[i];
					y= w[i];
					h= s * g;
					g= c * g;
					z= pythag(f, h);
					rv1[j]= z;
					c= f / z;
					s= h / z;
					f= x * c + g * s;
					g= g * c - x * s;
					h= y * s;
					y *= c;
					for (jj= 0; jj < n; jj++)
					{
						x= v[jj][j];
						z= v[jj][i];
						v[jj][j]= x * c + z * s;
						v[jj][i]= z * c - x * s;
					}
					z= pythag(f, h);
					w[j]= (float)z;
					if (z != 0.0)
					{
						z= 1.0 / z;
						c= f * z;
						s= h * z;
					}
					f= c * g + s * y;
					x= c * y - s * g;
					for (jj= 0; jj < m; jj++)
					{
						y= a[jj][j];
						z= a[jj][i];
						a[jj][j]= y * c + z * s;
						a[jj][i]= z * c - y * s;
					}
				}
				rv1[l]= 0.0;
				rv1[k]= f;
				w[k]= (float)x;
			}
		}
	}

	private final static double pythag(double a, double b)
	{
		double absa, absb;
		absa= Math.abs(a);
		absb= Math.abs(b);
		if (absa > absb)
			return absa * Math.sqrt(1.0 + square(absb / absa));
		else
			return (absb == 0.0 ? 0.0 : absb * Math.sqrt(1.0 + square(absa / absb)));
	}

	private final static double square(double x)
	{
		return x * x;
	}

	private final static double sign(double a, double b)
	{
		if (b >= 0.0)
		{
			return (Math.abs(a));
		}
		else
		{
			return (-Math.abs(a));
		}
	}

	/* ***************************
	   Where is our man Chuck Yeager?
	   *********************** */

	/** 
	    This test driver does a monte carlo simulation to test whether
	    SVD is maintaining its two conditions: the calculated bases must be orthonormal,
	    and the decomposition must remultiply into A within a roundoff tolerance.
	*/
	public static void main(String argh[])
	{
		UniformRandomVariable urv= new UniformRandomVariable(1, 100);
		UniformRandomVariable entry_urv= new UniformRandomVariable(-100, 100);
		int tests= 10000;

		for (int k= 0; k < tests; k++)
		{
			int m= (int) urv.sample();
			int n= (int) urv.sample();
			Matrix a= new Matrix(m, n);

			// make a random matrix
			for (int i= 0; i < m; i++)
				for (int j= 0; j < n; j++)
					a.set(i, j, entry_urv.sample());

			// zero pad
			if (m < n)
				a.resize(n, n);

			SVD svd= null;
			try
			{
				svd= new SVD(a);
			}
			catch (Exception e)
			{
				System.out.println(e);
			}

			Matrix u= svd.getRangeBasisMatrix();
			Matrix w= new Matrix(n, n);
			// System.out.println("singular: " + svd.getSingularValues());
			w.setDiagonal(svd.getSingularValues());
			Matrix v= svd.getNullspaceBasisMatrix();
			Matrix v_trans= v.makeTranspose();
			Matrix tmp= u.postMult(w);
			Matrix a_prime= tmp.postMult(v_trans);

			// System.out.println(a_prime);
			// find errors.  Roundoff should be small.
			Matrix.sub(a, a_prime, a_prime);
			double rms= 0.0;
			double max= 0.0;
			for (int i= 0; i < m; i++)
			{
				for (int j= 0; j < n; j++)
				{
					double aij= a_prime.get(i, j);
					rms += aij * aij;
					if (Math.abs(aij) > max)
						max= aij;
				}
			}
			rms /= m * n;
			rms= Math.sqrt(rms);
			System.out.println("Doing: " + m + "+" + n);
			// System.out.println(a.toString());
			// System.out.println("rms(A - A') = " + rms);
			// System.out.println("Max(Abs(A - A')) = " + max);
			if (rms > 1.0e-10)
			{
				System.out.println("Got RMS jump! " + rms);
				System.out.println(svd.getSingularValues());
				// dumpState(a, u, w, v, "err.log");
			}
			if (max > 1.0e-10)
			{
				System.out.println("Got max jump! " + max);
				System.out.println(svd.getSingularValues());
				dumpState(rms, max, a, u, w, v, "err.log");
			}

			// check orthonormality
			Matrix u_trans= u.makeTranspose();
			Matrix utu= u_trans.postMult(u);
			Matrix id= new Matrix(n, n);
			id.identity();
			// find errors.  Roundoff should be small.
			Matrix.sub(utu, id, utu);
			rms= 0.0;
			max= 0.0;
			for (int i= 0; i < n; i++)
			{
				for (int j= 0; j < n; j++)
				{
					double aij= utu.get(i, j);
					rms += aij * aij;
					if (Math.abs(aij) > max)
						max= aij;
				}
			}
			rms /= n * n;
			rms= Math.sqrt(rms);
			if (rms > 1.0e-10 || max > 1.0e-10)
			{
				System.out.println("Not orthogonal.");
				System.out.println("rms(U' U - I) = " + rms + "\nMax(Abs(U' U - I)) = " + max);
				dumpState(rms, max, a, u, w, v, "crap");
			}

			Matrix vtv= v_trans.postMult(v);
			Matrix.sub(vtv, id, vtv);
			rms= 0.0;
			max= 0.0;
			for (int i= 0; i < n; i++)
			{
				for (int j= 0; j < n; j++)
				{
					double aij= vtv.get(i, j);
					rms += aij * aij;
					if (Math.abs(aij) > max)
						max= aij;
				}
			}
			rms /= n * n;
			rms= Math.sqrt(rms);
			if (rms > 1.0e-10 || max > 1.0e-10)
			{
				System.out.println("Not orthogonal.");
				System.out.println("rms(V' V - I) = " + rms + "\nMax(Abs(V' V - I)) = " + max);
				dumpState(rms, max, a, u, w, v, "crap");
			}
			//System.out.println("*******************************");
		}

		while (true)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Exception e)
			{

			}
		}
	}

	/**
	   Writes the matrix into a MATLAB .mat file, with a path and filename specified by a String.
	*/
	public static void writeMatrix(Matrix a, String s)
	{
		try
		{
			Writer file= new OutputStreamWriter(new FileOutputStream(s));
			file.write(a.toString());
			file.write("\n");
			file.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void dumpState(double rms, double max, Matrix a, Matrix u, Matrix w, Matrix v, String filename)
	{
		writeMatrix(a, "data/a.mat");
		writeMatrix(u, "data/u.mat");
		writeMatrix(w, "data/w.mat");
		writeMatrix(v, "data/v.mat");
		System.exit(0);
	}
}
