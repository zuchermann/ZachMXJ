package innards.math.linalg;

import java.io.Serializable;
import innards.math.random.*;
import java.io.*;

//   FIXME: Add doublebuffering and concat functions.

/**
  Double-precision matrix. Works with Vec for vector math. <P>

  Some methods throw runtime exceptions for dimension
  mismatch or inplace multiplication errors. (Runtime exceptions do not need to be caught)
  
  <P>
  SingularMatrixException, on the other hand, must be handled.
  
  @see innards.math.linalg.Vec
  @see innards.math.linalg.LUDState
  @see innards.math.linalg.SingularMatrixException
  @see innards.math.linalg.DimensionMismatchException
  @see innards.math.linalg.InPlaceMatrixMultException 
*/

public class Matrix implements Cloneable, Serializable
{
	/* 
	   Rep
	*/
	// package scope for efficiency in other impls in this package
	double[][] rep;
	public final static double TINY= 1.0e-11;
	public final static double CLOSE_ENOUGH_TO_ZERO= 1.0e-11;

	/*
	  Constructors
	  */

	/** 
	Constructs a 1x1 zero matrix.
	*/
	public Matrix()
	{
		this(1, 1);
	}

	/** 
	Constructs a (<code>rows</code> x <code>cols</code>) zero matrix
	@param rows the number of rows 
	@param cols the numbers of columns 
	*/
	public Matrix(int rows, int cols)
	{
		rep= new double[rows][cols];
	}

	/** 
	  Copy constructor.
	*/
	public Matrix(Matrix m)
	{
		rep= copyRep(m.rep);
	}

	/** 
	  Constructs a matrix by copying from a 2-dimensional array of floats, 
	  interpreted as a row-major matrix (e.g. <code>a[row index][column index]</code>
	  */
	public Matrix(double[][] a)
	{
		rep= copyRep(a);
	}
	
	public Matrix(float [][] a)
	{
		rep= copyRep(a);
	}

	/**
	   Convenience method that calls clone, but saves the hassle of casting from an Object to a Matrix.
	*/
	public Matrix copy()
	{
		Matrix m= new Matrix();
		m.rep= Matrix.copyRep(rep);
		return m;
	}

	/**
	   Returns a deep copy of this Matrix.
	*/
	public Object clone()
	{
		try
		{
			Matrix m= (Matrix) super.clone();
			m.rep= Matrix.copyRep(rep);
			return m;
		} catch (CloneNotSupportedException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/*
	  Observers
	  ***/
	/**
	   Returns number of rows.
	*/
	public final int numRows()
	{
		return rep.length;
	}

	/**
	   Returns number of columns.
	*/
	public final int numColumns()
	{
		if (rep.length == 0)
			return 0;
		return rep[0].length;
	}

	/** 
	  Same as get(i,j) 
	  @exception java.lang.ArrayIndexOutOfBoundsException  if index out of range
	*/
	public final double ref(int i, int j)
	{
		return rep[i][j];
	}

	/**
	  Returns the element in the ith row, jth column.
	  @exception java.lang.ArrayIndexOutOfBoundsException  if index out of range
	  */
	public final double get(int i, int j)
	{
		return rep[i][j];
	}

	/**
	 * Returns the a smaller matrix contained in this one. 
	 * @param i
	 * @param j
	 * @param w
	 * @param h
	 * @return
	 * @exception java.lang.ArrayIndexOutOfBoundsException  if index out of range
	 */
	public final Matrix get(int i0, int j0, int w, int h)
	{
		Matrix sub = new Matrix(w,h);
		for (int i=0; i<w; i++)
			for (int j=0; j<h; j++)
				sub.set(i, j, this.get(i0+i, j0+j));
		return sub;
	}

	/** 
	Returns a copy of the elements in a 2-dimensional double array, where
	double[rows][columns] is the layout of elements.
	*/
	public final double[][] toArray()
	{
		return copyRep(rep);
	}

	/**
	 Returns the contents of the matrix as a string, listing the elements of each row by a space, and separating rows by newlines (<code>\n<\code>). (MATLAB ASCII format)
	*/
	public String toString()
	{
		String s= "";
		for (int i= 0; i < numRows(); i++)
		{
			Vec v= new Vec(rep[i]);
			for (int j= 0; j < v.size(); j++)
			{
				if (j == 0)
					s += v.get(j);
				else
					s += " " + v.get(j);
			}
			s += "\n";
		}
		return s;
	}

	/**
	   Writes the matrix to the specified file in MATLAB's ASCII format. 
	   @param filename The name of the file to be written to.
	 */
	public void writeToMatlabFile(String filename)
	{
		try
		{
			Writer writer= new BufferedWriter(new FileWriter(filename));
			writer.write(toString());
			writer.close();
		} catch (Exception e)
		{
			System.err.println("ABORT: Error writing matrix to file " + filename);
		}
	}

	/*
	  Mutators
	  ********************/

	/** 
	Sets the value of the (i,j)-th element to v.
	@param i row index
	@param j column index
	@param v value to set it to.
	@exception java.lang.ArrayIndexOutOfBoundsException  if index is out of range
	*/
	public final void set(int i, int j, double v)
	{
		rep[i][j]= v;
	}

	/** 
		Sets this matrix's values to equal the elements of the other
		matrix. Matrices' dimensions must match.
		@param m source matrix. 
	 */
	public final void set(Matrix m) throws DimensionMismatchException
	{
		if ((numColumns() != m.numColumns()) || (numRows() != m.numRows()) || (numColumns() == 0) || (numRows() == 0))
		{
			throw new DimensionMismatchException(
				"Matrices' dimensions must match for in-place set. This matrix "
					+ "was "
					+ numRows()
					+ "x"
					+ numColumns()
					+ ", argument given is "
					+ m.numRows()
					+ "x"
					+ m.numColumns());
		}
		int i, j;
		for (i= 0; i < numRows(); i++)
			for (j= 0; j < numColumns(); j++)
				rep[i][j]= m.rep[i][j];
	}

	public final void set(float[][] f)
	{
		rep= copyRep(f);
	}

	/**
	  Transposes this matrix.
	  To create a new Matrix, use the static method transpose.
	  @see Matrix#transpose(Matrix)
	  @see Matrix#makeTranspose()
	*/
	public final void transpose()
	{
		int i, j;
		if (numRows() == numColumns())
		{
			for (i= 0; i < numRows(); i++)
				for (j= 0; j < i; j++)
				{
					double t= rep[i][j];
					rep[i][j]= rep[j][i];
					rep[j][i]= t;
				}
		} else
		{
			// new matrix.
			double[][] new_rep= new double[numColumns()][numRows()];
			for (i= 0; i < numRows(); i++)
				for (j= 0; j < numColumns(); j++)
					new_rep[j][i]= rep[i][j];
			rep= new_rep;
		}
	}

	/**
	  Returns the transpose of this matrix.
	*/
	public final Matrix makeTranspose()
	{
		Matrix m= new Matrix(this);
		m.transpose();
		return m;
	}

	/**
	  Stores the transpose of this matrix in m.
	  
	  @exception java.lang.ArrayIndexOutOfBoundsException  if m is wrong size
	*/
	public final void makeTranspose(Matrix m)
	{
		for (int i= 0; i < numRows(); i++)
			for (int j= 0; j < numColumns(); j++)
			{
				m.rep[j][i]= rep[i][j];
			}
	}

	/**
	   Resizes the matrix to rows, cols.  Truncates if smaller, pads with zeros if larger. Does no work if the size is correct, so can be used without checking.  
	  <P><B>Mutator.</B>
	*/
	public void resize(int rows, int cols)
	{
		if (numRows() == rows && numColumns() == cols)
			return;

		double[][] new_rep= new double[rows][cols];
		for (int i= 0; i < rows; i++)
		{
			for (int j= 0; j < cols; j++)
			{
				if (i < numRows() && j < numColumns())
				{
					new_rep[i][j]= rep[i][j];
				} else
				{
					new_rep[i][j]= 0.0;
				}
			}
		}
		rep= new_rep;
	}

	/**
	  Turns elements "close" to zero into zeros.
	  Specifically, 
	  For all elements e, if abs(e) < CLOSE_ENOUGH_TO_ZERO, set e = 0.0.
	  */
	public void roundZeros()
	{
		int i, j;
		for (i= 0; i < numRows(); i++)
			for (j= 0; j < numColumns(); j++)
				if (Math.abs(rep[i][j]) < CLOSE_ENOUGH_TO_ZERO)
					rep[i][j]= 0.0;
	}

	/**
	  Scales each element in this matrix by d.
	  @param d the scalar to scale by.
	*/
	public final void scale(double d)
	{
		int i, j;
		for (i= 0; i < numRows(); i++)
			for (j= 0; j < numColumns(); j++)
				rep[i][j] *= d;
	}

	/**
	  Returns a scaled version of the current matrix
	  @param d the scalar to scale by.
	*/
	public final Matrix scaled(double d)
	{
		Matrix scaled = new Matrix(this);
		scaled.scale(d);
		return scaled;
	}

	/**
	  Turn this into the identity matrix.
	  If non-square, turns all elements (i,j) where i==j to 1.0, and all others to 0.0
	*/
	public final void identity()
	{
		scale(0.0);

		int i= 0;
		while (i < numRows() && i < numColumns())
		{
			rep[i][i]= 1.0;
			i++;
		}
	}

	/**
	   Returns a column as a new Vec.
	*/
	public Vec getColumn(int c)
	{
		Vec x= new Vec(numRows());
		getColumn(c, x);
		return x;
	}

	/**
	   Copies a column into a Vec.
	*/
	public void getColumn(int c, Vec x)
	{
		for (int i= 0; i < numRows(); i++)
			x.set(i, get(i, c));
	}

	/**
	   Copies a row into a Vec.
	*/
	public void getRow(int r, Vec x)
	{
		for (int i= 0; i < numColumns(); i++)
			x.set(i, get(r, i));
	}

	/**
	   Returns a row as a new Vec.
	*/
	public Vec getRow(int r)
	{
		Vec x= new Vec(numColumns());
		getRow(r, x);
		return x;
	}

	/** 
	Copies values from a Vec to a column. Dimensions must match. 
	*/
	public void setColumn(int c, Vec x)
	{
		int d= x.getDimension();
		int rows= numRows();
		if (rows != d)
		{
			throw new DimensionMismatchException("Vec dimension " + d + " does not match matrix rows.");
		}

		// set the column
		for (int i= 0; i < d; i++)
			set(i, c, x.get(i));

	}

	/** 
	Copies values from a Vec to a row. Dimensions must match. 
	*/
	public void setRow(int row, Vec x)
	{
		int d= x.getDimension();
		int columns= numColumns();
		if (columns != d)
		{
			throw new DimensionMismatchException("Vec dimension " + d + " does not match matrix columns.");
		}

		// set the column
		for (int i= 0; i < d; i++)
			set(row, i, x.get(i));

	}

	/** 
	Turns this matrix into a diagonal matrix with diagonal values specified by the entries in Vec <code>d</code>. Matrix will be resized as necessary to <code>d.size() x d.size</code>.
	*/
	public void setDiagonal(Vec d)
	{
		int n= d.size();
		resize(n, n);
		int i, j;
		for (i= 0; i < n; i++)
		{
			for (j= 0; j < n; j++)
			{
				if (i == j)
					rep[i][j]= d.get(i);
				else
					rep[i][j]= 0.0;
			}
		}
	}

	/**
	  Returns <code>this + b</code> (element-wise matrix addition).
	  There is also a static add method that stores the sum into an existing matrix.
	  
	  @param b matrix to add with <code>this</code>.
	  @return M  A new matrix <code>M = this + b</code>.
	  @exception DimensionMismatchException if <code>this</code> and <code>b</code> do not match in dimension.
	  @see Matrix#add(Matrix,Matrix,Matrix).
	*/
	public Matrix add(Matrix b) throws DimensionMismatchException
	{
		Matrix out= new Matrix(b.numRows(), b.numColumns());
		add(this, b, out);
		return out;
	}

	/**
	   Returns <code>this - b</code> (element-wise matrix subtraction).
	   There is also a static <code>sub</code> method that stores the difference into an existing matrix.
	
	  @param b matrix to subtract.
	  @return M  A new matrix <code>M = this - b</code>.
	  @exception DimensionMismatchException if <code>this</code> and <code>b</code> do not match in dimension.
	  @see Matrix#sub(Matrix,Matrix,Matrix).
	*/
	public Matrix sub(Matrix b) throws DimensionMismatchException
	{
		Matrix out= new Matrix(b.numRows(), b.numColumns());
		sub(this, b, out);
		return out;
	}

	/**
	  Returns <code>a * this</code>.
	  @param a  the vector to premultiply by.
	  @return  <code>v</code> where <code>v = a * this</code>.  <code>v</code> will be a new vector.
	  @exception DimensionMismatchException if <code>this</code> and <code>a</code> do not match in dimension.
	
	  
	  @see Matrix#mult(Vec,Matrix,Vec)
	 */
	public Vec preMult(Vec a) throws DimensionMismatchException
	{
		Vec out= new Vec(numColumns());
		mult(a, this, out);
		return out;
	}

	/**
	  Returns <code>this * a</code>.
	  
	  @param a  the vector to post-multiply by.
	  @return <code>v</code> where <code>v = T * a</code>.  <code>v</code> will be a new vector.
	  @exception DimensionMismatchException if <code>this</code> and <code>a</code> do not match in dimension.
	    
	  @see Matrix#mult(Matrix,Vec,Vec)      
	*/
	public Vec postMult(Vec a) throws DimensionMismatchException
	{
		Vec out= new Vec(numRows());
		mult(this, a, out);
		return out;
	}

	/**
	  Returns <code>A * this</code>
	
	  @param A the matrix to premultiply by.
	  @return <code>M</code> s.t. <code>M = A * this</code>.  <code>M</code> will be a new matrix.
	  @exception DimensionMismatchException if this and A do not match in dimension.
	  
	  @see Matrix#mult(Matrix,Matrix,Matrix)
	  */
	public Matrix preMult(Matrix A) throws DimensionMismatchException
	{
		Matrix out= new Matrix(A.numRows(), this.numColumns());
		mult(A, this, out);
		return out;
	}

	/**
	  Returns <code>this * A</code>
	  
	  @param A the matrix to post-multiply by.
	  @return <code>M</code> s.t. <code>M = this * A</code>.  <code>M</code> will be a new matrix.
	  @exception DimensionMismatchException if this and A do not match in dimension.
	
	  @see Matrix#mult(Matrix,Matrix,Matrix)
	  */
	public Matrix postMult(Matrix A) throws DimensionMismatchException
	{
		Matrix out= new Matrix(this.numRows(), A.numColumns());
		mult(this, A, out);
		return out;
	}

	private void copyRepFrom(Matrix m)
	{
		if (numColumns() != m.numColumns() || numRows() != m.numRows())
		{
			rep= copyRep(m.rep);
		} else // efficient copy
			{
			for (int i= 0; i < numRows(); i++)
				System.arraycopy(m.rep[i], 0, rep[i], 0, numColumns());
		}

	}

	/**
	  Returns an LU decomposition of this matrix as an LUDState object.
	  If the matrix is singular, throws SingularMatrixException.
	  If the matrix is non-square, returns null and reports error to System.err.
	  @exception SingularMatrixException if this is singular.
	*/
	private Matrix m_temp= null;
	private Vec vv_temp= null;
	public LUDState decomposeLU() throws SingularMatrixException
	{
		// do all work on copy
		if (m_temp != null)
			m_temp.copyRepFrom(this);
		else
			m_temp= new Matrix(this);

		if (vv_temp == null)
			vv_temp= new Vec(numRows());

		Matrix m= m_temp; // ref assign, not copy.
		Vec vv= vv_temp;

		/* Stolen  from Numerical Recipes in C, Java-ized */
		int i, imax, j, k;
		double big, dum, sum, temp;
		int n= m.numRows();
		int rows= m.numRows();
		int cols= m.numColumns();

		if (rows != cols)
		{
			error("decomposeLU(): can't LU decompose non-square matrix!");
			return null;
		}
		int[] indx= new int[rows];
		double d= 1.0;
		imax= 0;

		for (i= 0; i < n; i++)
		{
			big= 0.0;
			for (j= 0; j < n; j++)
				if ((temp= Math.abs(m.rep[i][j])) > big)
					big= temp;
			if (big == 0.0)
			{
				// error("Singular matrix in decomposeLU.  Use SVD.  Punting.");
				throw new SingularMatrixException();
				// return null;
			}
			vv.rep[i]= (float) (1.0 / big);
		}
		for (j= 0; j < n; j++)
		{
			for (i= 0; i < j; i++)
			{
				sum= m.rep[i][j];
				for (k= 0; k < i; k++)
					sum -= m.rep[i][k] * m.rep[k][j];
				m.rep[i][j]= sum;
			}
			big= 0.0;
			for (i= j; i < n; i++)
			{
				sum= m.rep[i][j];
				for (k= 0; k < j; k++)
					sum -= m.rep[i][k] * m.rep[k][j];
				m.rep[i][j]= sum;
				if ((dum= vv.rep[i] * Math.abs(sum)) >= big)
				{
					big= dum;
					imax= i;
				}
			}
			if (j != imax)
			{
				for (k= 0; k < n; k++)
				{
					dum= m.rep[imax][k];
					m.rep[imax][k]= m.rep[j][k];
					m.rep[j][k]= dum;
				}
				d= -d;
				vv.rep[imax]= vv.rep[j];
			}
			indx[j]= imax;
			if (m.rep[j][j] == 0.0)
				m.rep[j][j]= TINY;

			if (j != n - 1)
			{
				dum= 1.0 / (m.rep[j][j]);
				for (i= j + 1; i < n; i++)
					m.rep[i][j] *= dum;
			}
		}

		// create the LUDState with the result
		LUDState lud= new LUDState(m, indx, (int) d);
		return lud;
	}

	/**
	  Returns the inverse matrix.
	
	  @exception SingularMatrixException if singular matrix.
	  @exception NonSquareMatrixException if non square
	*/
	public Matrix inverse() throws SingularMatrixException, NonSquareMatrixException
	{
		Matrix inv= new Matrix(this);
		this.inverse(inv);
		return inv;
	}

	/**
	  Writes the inverse matrix to <code>inv</code>.
	  @param inv the matrix to fill the inverse into.
	  @exception SingularMatrixException if singular matrix.
	  @exception NonSquareMatrixException if not square.
	*/
	public void inverse(Matrix inv) throws SingularMatrixException, NonSquareMatrixException
	{
		int rows= numRows();
		int cols= numColumns();
		if (rows != cols)
		{
			// error("Can't invert non-square matrix.");
			throw new NonSquareMatrixException("can't invert non square matrix");
		}
		Vec col= new Vec(rows);
		// copy this into inv to begin
		inv.set(this);
		int i, j;
		int[] indx= new int[rows];

		LUDState lud= decomposeLU();

		for (j= 0; j < rows; j++)
		{
			for (i= 0; i < rows; i++)
			{
				col.rep[i]= 0.0f;
			}
			col.rep[j]= 1.0f;

			lud.backSub(col, col);

			for (i= 0; i < rows; i++)
				inv.rep[i][j]= col.rep[i];
		}
	}

	/*
	Statics
	********************/

	/**
	  Returns the transpose of the argument.
	  
	  @param old the matrix to transpose
	  @return a new matrix which is the transpose of the matrix old.
	*/
	public static Matrix transpose(Matrix old)
	{
		Matrix m= new Matrix(old);
		m.transpose();
		return m;
	}

	/**
	   <code>out = a + b</code><P>.
	   <code>out</code> may be the same object as <code>a</code> or code>b</code>.
	  
	  @param a first matrix
	  @param b second matrix
	  @param out storage for a + b.
	  @exception DimensionMismatchException if args do not match in dimension.
	  
	*/
	public static void add(Matrix a, Matrix b, Matrix out) throws DimensionMismatchException
	{
		if (!checkDims(a, b, out))
		{
			// error("add: can't add dissimilar matrices");
			throw new DimensionMismatchException();
		}

		int i, j;
		for (i= 0; i < a.numRows(); i++)
			for (j= 0; j < a.numColumns(); j++)
				out.rep[i][j]= a.rep[i][j] + b.rep[i][j];
	}

	/**
	  Subtracts matrices s.t.  out = a -  b.
	  All arguments must have the same dimensions, else out is invalid.
	  <B>SAFE</B> for out == a or out == b.
	
	  @param a first matrix
	  @param b second matrix
	  @param out User-allocated object for a - b
	  @exception DimensionMismatchException if args do not match in dimension.
	  */
	public static void sub(Matrix a, Matrix b, Matrix out) throws DimensionMismatchException
	{
		if (!checkDims(a, b, out))
		{
			// error("sub: can't add dissimilar matrices");
			throw new DimensionMismatchException();
		}

		int i, j;
		for (i= 0; i < a.numRows(); i++)
			for (j= 0; j < a.numColumns(); j++)
				out.rep[i][j]= a.rep[i][j] - b.rep[i][j];
	}

	/**
	  Multiplies matrices elementwise (<B>not</B> matrix multiplication) and places the result in <ocde>out</code>.
	  <code>out</code> may be the same object as <code>a</code> or code>b</code>.
	
	  @param a first matrix
	  @param b second matrix
	  @param out User-allocated object for a .* b
	  @exception DimensionMismatchException if args do not match in dimension.
	*/
	public static void elemMult(Matrix a, Matrix b, Matrix out) throws DimensionMismatchException
	{
		if (!checkDims(a, b, out))
		{
			// error("elemMult: can't element-wise multiply dissimilar matrices");
			throw new DimensionMismatchException();
		}

		int i, j;
		for (i= 0; i < a.numRows(); i++)
			for (j= 0; j < a.numColumns(); j++)
				out.rep[i][j]= a.rep[i][j] * b.rep[i][j];
	}

	/**
	  <code>out = v * this</code>.<P>
	  <B>Note:</B> out cannot be the same object as v.  
	     
	  @param v vector to left multiply
	  @param m matrix to multiply by
	  @param out vector where result is stored.  out = v m
	  @exception DimensionMismatchException if args do not match in dimension.
	  @exception InPlaceMatrixMultException if out is one of other args.
	*/
	public static void mult(Vec a, Matrix b, Vec out) throws DimensionMismatchException, InPlaceMatrixMultException
	{
		if (a == out)
		{
			throw new InPlaceMatrixMultException("mult(Vec a, Matrix b, Vec out): out cannot be a!  Punting.");
		}

		if (a.dim() != b.numRows() || out.dim() != b.numColumns())
		{
			throw new DimensionMismatchException(
				"mult(Vec a, Matrix b, Vec out): bad dimensions: " + "1x" + a.dim() + " * " + b.numRows() + "x" + b.numColumns() + " != " + "1x" + out.dim());
		}

		int i, k;
		for (i= 0; i < out.dim(); i++)
		{
			out.rep[i]= 0.0f;
			for (k= 0; k < a.dim(); k++)
				out.rep[i] += a.rep[k] * b.rep[k][i];
		}
	}

	/**
	  <code>out = this * v</code>.<P>
	  <B>Note:</B> out cannot be the same object as v.  
	     
	  @param v vector
	  @param m matrix
	  @param out <code>out = v m</code>
	  @exception DimensionMismatchException if args do not match in dimension.
	  @exception InPlaceMatrixMultException if out is one of other args.
	*/
	public static void mult(Matrix a, Vec b, Vec out) throws DimensionMismatchException, InPlaceMatrixMultException
	{
		if (b == out)
		{
			throw new InPlaceMatrixMultException("mult(Matrix a, Vec b, Vec out): can't multiply in place! Punting.");
		}

		if (a.numColumns() != b.dim() || out.dim() != a.numRows())
		{
			throw new DimensionMismatchException(
				"mult(Matrix a, Vec b, Vec out): bad dimensions: " + a.numRows() + "x" + a.numColumns() + " * " + b.dim() + "x1" + " != " + out.dim() + "x1");
		}

		int i, k;
		for (i= 0; i < out.dim(); i++)
		{
			out.rep[i]= 0.0f;
			for (k= 0; k < b.dim(); k++)
				out.rep[i] += a.rep[i][k] * b.rep[k];
		}
	}

	/**
	  <code>out = a * b</code>,
	  
	  @param a matrix
	  @param b matrix
	  @param out matrix where result is stored.  out = a b
	  @exception DimensionMismatchException if args do not match in dimension.
	  @exception InPlaceMatrixMultException if out == a || out == b
	  */
	public static void mult(Matrix a, Matrix b, Matrix out) throws DimensionMismatchException, InPlaceMatrixMultException
	{
		if (a == out || b == out)
		{
			throw new InPlaceMatrixMultException("mult(Matrix a, Matrix b, Matrix c): can't multiply in place!");
		}

		if (a.numColumns() != b.numRows() || out.numRows() != a.numRows() || out.numColumns() != b.numColumns())
		{
			throw new DimensionMismatchException(
				"mult(Matrix A, Matrix B, "
					+ "Matrix out): bad dimensions on "
					+ "matrix multiply. A is "
					+ a.numRows()
					+ "x"
					+ a.numColumns()
					+ ", B is "
					+ b.numRows()
					+ "x"
					+ b.numColumns()
					+ ", out is "
					+ out.numRows()
					+ "x"
					+ out.numColumns());
		}

		int i, j, k; // i E [0 .. a.rows-1]  j E [0 .. b.cols-1]  k E [0 .. a.cols-1]
		for (i= 0; i < a.numRows(); i++)
			for (j= 0; j < b.numColumns(); j++)
			{
				out.rep[i][j]= 0.0;
				for (k= 0; k < a.numColumns(); k++)
					out.rep[i][j] += a.rep[i][k] * b.rep[k][j];
			}
	}

	/**
	   Returns the (scalar) a*b*c.
	*/
	public static double multTriple(Vec a, Matrix b, Vec c)
	{
		if (a.dim() != b.numRows() || b.numColumns() != c.dim())
		{
			throw new DimensionMismatchException("multTriple dimension mismatch " + a.dim() + "!=" + b.numRows() + " || " + b.numColumns() + "!=" + c.dim());
		}

		double tot= 0;

		for (int i= 0; i < b.numColumns(); i++)
		{
			double m= c.get(i);
			double ptot= 0;
			for (int j= 0; j < b.numRows(); j++)
			{
				ptot += b.get(i, j) * a.get(j);
			}
			tot += ptot * m;
		}
		return tot;
	}

	/**
	  Calculates the determinant.
	*/
	public double determinant() throws NonSquareMatrixException, SingularMatrixException
	{
		int rows= numRows();
		int cols= numColumns();
		if (rows != cols)
		{
			throw new NonSquareMatrixException("Can't compute determinant of non-square matrix.");
		}
		LUDState lud= decomposeLU();
		return lud.determinant();
	}

	/*
	Amyl Nitrate available below! (cyanide)
	***************************/

	/**
	   Just prints a message to System.err. Make private.
	*/
	protected static void error(String s)
	{
		System.err.println("(Matrix): " + s);
	}

	/**
	   Returns <code>true</code> if <code>a</code>, <code>b</code>, and <code>out</code> all have the same dimensions.
	*/
	protected static boolean checkDims(Matrix a, Matrix b, Matrix out)
	{
		return (a.numRows() == b.numRows() && a.numRows() == out.numRows() && a.numColumns() == b.numColumns() && a.numColumns() == out.numColumns());
	}

	/**
	   Sets the values of this matrix to be those in <code>a</code>. Resizes if necessary.
	*/
	protected static double[][] copyRep(double[][] a)
	{
		// make rows array
		double[][] v= new double[a.length][];
		int i;
		for (i= 0; i < a.length; i++)
		{
			v[i]= new double[a[i].length];
			System.arraycopy(a[i], 0, v[i], 0, a[i].length);
		}
		return v;
	}
	
	protected static double[][] copyRep(float[][] a)
	{
		// make rows array
		double[][] v= new double[a.length][];
		int i;
		for (i= 0; i < a.length; i++)
		{
			v[i]= new double[a[i].length];
			//System.arraycopy(a[i], 0, v[i], 0, a[i].length);
			for(int m=0;m<a[i].length;m++)
			{
				v[i][m] = a[i][m];
			}
		}
		return v;
	}

	/**
	   Shorthand for System.out.println(String). Make private.
	*/
	protected static void print(String s)
	{
		System.out.println(s);
	}

	/*
	Chuck Yeager
	*********************/

	/** Test driver for the class */
	public static void main(String[] argh)
	{
		Matrix a= new Matrix(3, 3);
		Matrix b= new Matrix(3, 4);

		int i, j;
		for (i= 0; i < 3; i++)
			for (j= 0; j < 3; j++)
				a.set(i, j, 2 * i + j);

		for (i= 0; i < 3; i++)
			for (j= 0; j < 4; j++)
				b.set(i, j, -2 * i + j);

		print("a: " + a);
		print("b: " + b);
		b.transpose();
		print("b trans: " + b);
		b.transpose();

		Matrix c= null;
		try
		{
			c= a.preMult(b);
		} catch (DimensionMismatchException e)
		{
			System.out.println("Expected dimension mismatch and got it.");
		}

		c= a.postMult(b);

		print("c = ab: " + c);

		Matrix id= new Matrix(4, 4);
		id.identity();
		print("identity: " + id);

		Matrix r1= makeRandomMatrix(6);
		Matrix r1inv= null;
		try
		{
			r1inv= r1.inverse();
		} catch (SingularMatrixException e)
		{
			System.err.println("Matrix " + r1 + " is singular.  Can't invert.");
			System.exit(-1);
		} catch (NonSquareMatrixException e)
		{
			System.err.println("Can't invert non-square.  Implement pseduo-inverse");
			System.exit(-1);
		}
		Matrix id1= r1.postMult(r1inv);
		id1.roundZeros();
		print("r1: " + r1);
		print("r1 * r1 inverse: " + id1);

		Matrix r2= makeRandomMatrix(7);
		Matrix r2inv= null;
		try
		{
			r2inv= r2.inverse();
		} catch (SingularMatrixException e)
		{
			System.err.println("Matrix " + r2 + " is singular.  Can't invert.");
			System.exit(-1);
		} catch (NonSquareMatrixException e)
		{
			System.err.println("Can't invert non-square.  Implement pseduo-inverse");
			System.exit(-1);
		}
		Matrix id2= r2.preMult(r2inv);
		id2.roundZeros();
		print("r2: " + r2);
		print("r2inv * r2: " + id2);

	}

	/**
	   create an n x n matrix of random doubles in the range [-1, 1]
	*/
	public static Matrix makeRandomMatrix(int n)
	{
		return Matrix.makeRandomMatrix(n, -1.0, 1.0);
	}

	/** 
	Create an n by n matrix of random double in the range [lo, hi]
	 */
	public static Matrix makeRandomMatrix(int n, double lo, double hi)
	{
		Matrix m= new Matrix(n, n);
		UniformRandomVariable urv= new UniformRandomVariable(lo, hi);
		int i, j;
		for (i= 0; i < n; i++)
			for (j= 0; j < n; j++)
				m.set(i, j, urv.sample());
		return m;
	}

	/**
	 * @param r
	 * @param c
	 * @param f
	 */
	public void increment(int r, int c, float f)
	{
		rep[r][c]+=f;
	}
	
	public void mult(int r,int c, float f)
	{
		rep[r][c]+=f;
	}

}
