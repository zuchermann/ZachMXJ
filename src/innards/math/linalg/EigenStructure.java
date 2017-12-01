/** 
  This class performs an eigenvector decomposition as specified in
  Numerial Recipes in C.  For an nxn symmetric matrix A, the nxn 
  matrix of eigenvectors V and n eigenvalues D are computed: A V = V D.
  
  @author Andy Wilson <drew@media.mit.edu> 
*/

package innards.math.linalg;

import innards.math.linalg.*;

public class EigenStructure
{
	Matrix eigenvectors;
	Vec eigenvalues;
	int n;

	/**
	   Initializes the internal rep to a square matrix of the specified dimension.
	*/
	public EigenStructure(int nin)
	{
		n= nin;
		eigenvectors= new Matrix(n, n);
		eigenvalues= new Vec(n);

	}

	/**
	   Initializes this to the eigen-decomposition of the provided square matrix, leaving the matrix unchanged.
	*/
	public EigenStructure(Matrix a) throws EigenStructureException, DimensionMismatchException
	{
		this(a.numRows());
		if (a.numRows() != a.numColumns())
			throw new DimensionMismatchException("EigenStructure(): matrix must be square.");
		performEigenDecomp(a);
	}

	/**
	   Performs the eigen-decomposition of the provided square matrix, leaving the matrix unchanged.
	*/
	public void performEigenDecomp(Matrix a) throws EigenStructureException
	{
		if ((a.numRows() != a.numColumns()) || (a.numRows() != n))
			throw new DimensionMismatchException("EigenStructure(): matrix must be square.");

		// jacobi destroys the matrix to be decomposed, so copy matrix a into 
		// temp storage
		Matrix temp= new Matrix(n, n);
		temp.set(a);

		jacobi(temp.rep, n, eigenvalues.rep, eigenvectors.rep);
	}

	/**
	   Returns the eigenvectors as rows in a Matrix.
	*/
	public Matrix getEigenvectors()
	{
		return new Matrix(eigenvectors);
	}

	/**
	   Returns the eigenvalues as a Vec
	*/
	public Vec getEigenvalues()
	{
		return new Vec(eigenvalues);
	}

	private static double EPS= 0.000000001;

	/* 
	   Computes all eigenvalues and eigenvectors of a real symmetric matrix <code>a</code>.
	   On output, elements of <code>a</code> above the diagonal are destroyed,
	   <code>d</code> returns the eigenvalues of <code>a</code>, and <code>v</code>
	   is a matrix whose columns are the eigenvectors of A. Taken from NRC, 
	   p. 467, with some modifications.
	*/
	private void jacobi(double[][] a, int n, float[] d, double[][] v) throws EigenStructureException
	{
		int j, iq, ip, i, nrot;
		double tresh, theta, tau, t, sm, s, h, g, c;

		double b[]= new double[n];
		double z[]= new double[n];

		for (ip= 0; ip < n; ip++)
		{
			/* Initialize the identity matrix */
			for (iq= 0; iq < n; ++iq)
				v[ip][iq]= 0.0;
			v[ip][ip]= 1.0;
			/* Initailize b and d to be diagonal of a */
			b[ip]= d[ip]= (float)a[ip][ip];
			z[ip]= 0.0;
		}

		nrot= 0;
		for (i= 0; i < 50; i++)
		{
			/* Sum off-diagonal elements */
			sm= 0.0;
			for (ip= 0; ip < n - 1; ip++)
				for (iq= ip + 1; iq < n; iq++)
					sm += Math.abs(a[ip][iq]);

			/*  If we have converged return. */
			if (sm == 0.0)
				return;

			/* tresh is different on first three iterations...*/
			tresh= (i < 3) ? 0.2 * sm / (n * n) : 0.0;

			for (ip= 0; ip < n - 1; ip++)
			{
				for (iq= ip + 1; iq < n; ++iq)
				{
					g= 100.0 * Math.abs(a[ip][iq]);

					/* After four sweeps, skip the rotation if the off-diagonal element is small */
					/* This test is taken directly from the text and looks a little suspect to me... */

					if (i > 3 && g < EPS)
						a[ip][iq]= 0.0;

					else if (Math.abs(a[ip][iq]) > tresh)
					{
						h= d[iq] - d[ip];
						if (g < EPS)
							t= (Math.abs(a[ip][iq]) > EPS) ? (a[ip][iq]) / h : 0.0;
						else
						{
							theta= (Math.abs(h) < EPS) ? 0.0 : 0.5 * h / (a[ip][iq]);
							t= 1.0 / (Math.abs(theta) + Math.sqrt(1.0 + theta * theta));
							if (theta < 0.0)
								t= -t;
						}
						c= 1.0 / Math.sqrt(1.0 + t * t);
						s= t * c;
						tau= s / (1.0 + c);

						h= t * a[ip][iq];
						z[ip] -= h;
						z[iq] += h;
						d[ip] -= h;
						d[iq] += h;
						a[ip][iq]= 0.0;

						for (j= 0; j < ip; j++)
						{
							//ROTATE(a,i,j, k, l,n)
							//ROTATE(a,j,ip,j,iq,n);
							g= a[j][ip];
							h= a[j][iq];
							a[j][ip]= g - s * (h + g * tau);
							a[j][iq]= h + s * (g - h * tau);
						}
						for (j= ip + 1; j < iq; j++)
						{
							//ROTATE(a,i ,j,k,l, n)
							//ROTATE(a,ip,j,j,iq,n);
							g= a[ip][j];
							h= a[j][iq];
							a[ip][j]= g - s * (h + g * tau);
							a[j][iq]= h + s * (g - h * tau);
						}
						for (j= iq + 1; j < n; j++)
						{
							//ROTATE(a,i, j,k, l,n)
							//ROTATE(a,ip,j,iq,j,n);
							g= a[ip][j];
							h= a[iq][j];
							a[ip][j]= g - s * (h + g * tau);
							a[iq][j]= h + s * (g - h * tau);
						}
						for (j= 0; j < n; j++)
						{
							//ROTATE(a,i,j, k,l, n)
							//ROTATE(v,j,ip,j,iq,n);
							g= v[j][ip];
							h= v[j][iq];
							v[j][ip]= g - s * (h + g * tau);
							v[j][iq]= h + s * (g - h * tau);
						}
						++nrot;
					}
				}
			}
			for (ip= 0; ip < n; ++ip)
			{
				b[ip] += z[ip];
				d[ip]= (float)b[ip];
					z[ip]= 0.0;
				}
		}

		/* Failed to converge!! What to do ??? */
		throw new EigenStructureException("EigenStructure.jacobi(): failed to converge.");

		// Java don't do macros, so here you go:
		//#define ROTATE(a, i, j, k, l, n) g=a[i*n+j]; h=a[k*n+l]; a[i*n+j] = g-s*(h+g*tau); \
		//a[k*n+l] = h+s*(g-h*tau);
	}

	public static void main(String[] args)
	{
		Matrix m= new Matrix(3, 3);
		m.set(0, 0, 0.0794);
		m.set(0, 1, 0.0049);
		m.set(0, 2, 0.0022);

		m.set(1, 0, 0.0049);
		m.set(1, 1, 0.0778);
		m.set(1, 2, -0.0062);

		m.set(2, 0, 0.0022);
		m.set(2, 1, -0.0062);
		m.set(2, 2, 0.0737);

		try
		{
			EigenStructure e= new EigenStructure(m);
			System.out.println(e.getEigenvectors());
			System.out.println();
			System.out.println(e.getEigenvalues());

		} catch (Exception e)
		{
			System.exit(-1);
		}

		/*
		  >> C
		  
		  C =
		  
		  0.0794    0.0049    0.0022
		  0.0049    0.0778   -0.0062
		  0.0022   -0.0062    0.0737
		  
		  >> [V,D] = eig(C)
		  
		  V =
		  
		  0.5964   -0.7104    0.3736
		  0.7409    0.3082   -0.5967
		  -0.3087   -0.6327   -0.7102
		  
		  
		  D =
		  
		  0.0844         0         0
		  0    0.0792         0
		  0         0    0.0674
		  
		  >>
		*/

	}

}
