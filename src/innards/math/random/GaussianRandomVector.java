package innards.math.random;

import innards.math.linalg.*;

import java.io.Serializable;
import java.util.*;

/**
   GaussianRandomVector
   <P>
   This class implements an n-dimensional Gaussian distribution in
   Cartesian space (R^n).  In other words, implements p(x; mu, K)
   where K is the covariance matrix with decomposition R' Sigma R = K.
   <P>
   It simply uses the fact that Gaussians factor so that a scalar
   Gaussian can be used along each component direction.

   @author Michael Patrick Johnson <aries@media.mit.edu>
*/
public class GaussianRandomVector implements Serializable
{
	private static final double DETERMINANT_MINIMUM= 1.0e-16;
	private static final double SIGMA_MINIMUM= 1.0e-12;

	// the covariance rotation part in R' S R
	private Matrix covariance_rotation;
	private Matrix covariance_inverse; // useful to keep around

	// the S part of R S R'
	private Vec covariance_sigma_squared;

	// the mean of the distribution
	private Vec mean;

	transient private GaussianRandomVariable[] gaussian_rv;
	transient private Vec cache_x= null;
	transient private Vec cache_y= null;
	transient private double normalizing_constant= 1.0;

	/**
	    Creates a default GaussianRandomVector, which is N(0,I), a zero mean, identity covariance distribution.
	*/
	public GaussianRandomVector()
	{
		createDefault();
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	/**
	    Creates the random vector with the given mean and covariance.  Dimensions of the mean imply the dimensions of the constructed object.  If mean.dim() == n, then covariance_matrix must be nxn.  Also, covariance is assumed to be symmetric positive semi-definite.
	*/
	public GaussianRandomVector(Vec mean, Matrix covariance_matrix)
	{
		setMean(mean);
		decomposeAndSetCovariance(covariance_matrix);
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	/**
    Creates the random vector with the given mean and covariance.  Dimensions of the mean imply the dimensions of the constructed object.  If mean.dim() == n, then covariance_matrix must be nxn.  Also, covariance is assumed to be symmetric positive semi-definite.
	 */
	public GaussianRandomVector(GaussianRandomVector grv)
	{
		this(new Vec(grv.mean), grv.getCovarianceRotation());
	}

	/**
	    Creates the GaussianRandomVector from the mean
	    vector, given the principal axes in a column matrix which is orthogonal, so
	    columns form an orthonormal basis for the ellipsoidal contours.
	    The variances are the variances associated with each column vector respectively.
	    If the axes_matrix is U, and D = diag(principal_variances), then the
	    covariance matrix K = U D U'.
	*/
	public GaussianRandomVector(Vec mean, Matrix principal_axes, Vec principal_variances)
	{
		int d= mean.dim();
		int r= principal_axes.numRows();
		int c= principal_axes.numColumns();
		int n= principal_variances.dim();

		if (r != d || c != d || n != d)
			throw new DimensionMismatchException("Mean, Rotation, and Variances must match in dimesionality");

		this.mean= new Vec(mean);
		this.covariance_rotation= new Matrix(principal_axes);
		this.covariance_sigma_squared= new Vec(principal_variances);
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	/**
	   Creates the gaussian random vector from a collection of data. Each
	   element of the collection is assumed to be a vector all of the
	   same dimension.  A maximum-likelihood estimate is then created
	   for the distribution from this data by creating the sample
	   covariance matrix.
	*/
	public GaussianRandomVector(Collection examples)
	{
		calculateMaximumLikelihoodEstimate(examples);
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	/**
	    Creates the GRV from data as well, but often data is more conveniently stored in a matrix of column data.  Hence, if column_data is mxn, then the data is of dimension m and there are n examples.  The resulting GRV will be of dimension m.
	*/
	public GaussianRandomVector(Matrix column_data)
	{
		calculateMaximumLikelihoodEstimate(column_data);
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	/** Returns the value of the normalized density at the given point.
	    THIS IS NOT A PROBABILITY -- it is a density and applies to the
	    differential vector dx.  The probability of the point being in
	    that neighborhood is then P(x) = getValue(x) dx.  This for a
	    gaussian is an analytically unsolveable equation which leads to
	    the error function erf.  I might add erf stuff later in a
	    subclass, but for now it is up to you.  Also, I think this will
	    produce incorrect values for singular data that is not treated
	    first somehow by removing the offending basis elements.
	*/
	public double getValue(Vec x) throws SingularDataException
	{
		double d= getMahalanobisDistance(x);
		return normalizing_constant * Math.exp(-.5 * d * d);
	}

	// ---------------------- ARIES EDIT LINE

	/**
	    Mahalanobis distance from the mean divides out the variances
	    from the components to "sphereize" the data.  The distance is
	    then is units of std dev's away from the mean.  Specifically, it
	    finds the sqrt of the "upstairs" portion of the gaussian
	    exponential.  Mathematically, it finds || S^{-1/2} R' (x - mean)
	    ||
	    @throws SingularDataException when the inverse of the variances is illdefined (near zero)
	*/
	public double getMahalanobisDistance(Vec x) throws SingularDataException
	{
		int d= x.dim();
		if (d != mean.getDimension())
			throw new DimensionMismatchException();

		makeVecCache();
		Vec.sub(x, mean, cache_x);
		Matrix.mult(covariance_inverse, cache_x, cache_y);

		// dividee out the variance for the mahalanobis distance
		for (int i= 0; i < d; i++)
		{
			double sigma= Math.sqrt(covariance_sigma_squared.get(i));
			if (sigma < SIGMA_MINIMUM)
				throw new SingularDataException("Basis element " + i + " in the data has a vanishing variance.");
			cache_y.set(i, cache_y.get(i) / sigma);
		}
		return cache_y.mag();
	}

	/** Create a new sample according to the distribution and place it in x.
	    x must be the same size as the data dimension or an exception is thrown.
	*/
	public void sample(Vec x) throws DimensionMismatchException
	{
		int d= x.getDimension();
		if (d != mean.dim())
		{
			throw new DimensionMismatchException();
		}

		// use the decoupled single gaussians for it.
		// create them if needed
		createScalarGaussiansIfNeeded();
		makeVecCache();

		// sample in decoupled axes
		for (int i= 0; i < getDimension(); i++)
		{
			double xi= getScalarGaussian(i).sample();
			cache_x.set(i, xi);
		}

		// rotate answer and store in x, then add mean on
		Matrix.mult(covariance_rotation, cache_x, x);
		Vec.add(x, mean, x);
	}

	public Vec sample()
	{
		Vec x= new Vec(getDimension());
		sample(x);
		return x;
	}

	public int getDimension()
	{
		return mean.dim();
	}

	public void setMean(Vec m)
	{
		mean= new Vec(m);
	}

	public void setCovarianceVariance(Vec sigma_squared)
	{
		covariance_sigma_squared= new Vec(sigma_squared);
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	public void setCovarianceRotation(Matrix rot)
	{
		covariance_rotation= new Matrix(rot);
	}

	public Vec getMean()
	{
		return new Vec(mean);
	}

	public void getMean(Vec m)
	{
		m.setValue(mean);
	}

	public Vec getCovarianceVariance()
	{
		return new Vec(covariance_sigma_squared);
	}

	/** get the decoupled variances */
	public void getCovarianceVariance(Vec sigma_squared)
	{
		sigma_squared.setValue(covariance_sigma_squared);
	}

	/** get the ith decoupled variance (sigma^2 == variance) */
	public double getCovarianceVariance(int i)
	{
		return covariance_sigma_squared.get(i);
	}

	public double getCovarianceStdDev(int i)
	{
		return Math.sqrt(getCovarianceVariance(i));
	}

	public Matrix getCovarianceRotation()
	{
		return new Matrix(covariance_rotation);
	}

	public void getCovarianceRotation(Matrix rot)
	{
		rot.set(covariance_rotation);
	}

	public String toString()
	{
		String s= "GaussianRandomVector {\n";
		s= s + "mean {" + mean + "}\n";
		s= s + "variance_rotation {" + covariance_rotation + "}\n";
		s= s + "variance_variance {" + covariance_sigma_squared + "}\n";
		s= s + "}\n";
		return s;
	}

	// below here there be helpers

	protected void recomputeNormalizingConstantAndCovarianceInverse()
	{
		recomputeNormalizingConstant();
		recomputeCovarianceInverse();
	}

	/** clearly this is not correct for a singular covariance,
	    which is why getValue shouldn't be used. I decided not to bother
	with the whole pseudoinverse thing. */
	protected void recomputeCovarianceInverse()
	{
		covariance_inverse= new Matrix(covariance_rotation);
		covariance_inverse.transpose();
	}

	protected void recomputeNormalizingConstant() throws SingularDataException
	{
		double alpha= 1.0;
		int n= getDimension();
		// thios is det(covariance)
		for (int i= 0; i < n; i++)
			alpha *= getCovarianceVariance(i);

		alpha= Math.sqrt(alpha);
		double pi_part= 2 * Math.PI;
		pi_part= Math.pow(pi_part, n / 2.0);
		alpha *= pi_part;

		if (alpha <= DETERMINANT_MINIMUM)
			throw new SingularDataException("Covariance is singular.  Please handle the offensive basis elements by removing them or forcing the variances to a lower bound.");

		alpha= 1.0 / alpha;

		normalizing_constant= alpha;
	}

	protected void createScalarGaussiansIfNeeded()
	{
		if (gaussian_rv == null)
		{
			gaussian_rv= new GaussianRandomVariable[getDimension()];
			for (int i= 0; i < getDimension(); i++)
			{
				double sigma_squared= getCovarianceVariance(i);
				gaussian_rv[i]= new GaussianRandomVariable(0, sigma_squared);
			}
		}
	}

	protected GaussianRandomVariable getScalarGaussian(int i)
	{
		createScalarGaussiansIfNeeded();
		return gaussian_rv[i];
	}

	protected void calculateMaximumLikelihoodEstimate(Collection examples)
	{
		Matrix D= makeDataMatrixFromExamples(examples);
		calculateMaximumLikelihoodEstimate(D);
	}

	protected void calculateMaximumLikelihoodEstimate(Matrix D)
	{
		int n= D.numColumns();
		Vec m= calculateMean(D);
		setMean(m);
		subtractMean(D, m);

		// then the outer product and SVD
		Matrix D_trans= new Matrix(D);
		D_trans.transpose();
		Matrix K= D.postMult(D_trans);
		K.scale(1.0 / (n - 1));
		decomposeAndSetCovariance(K);
	}

	protected Vec calculateMean(Matrix data)
	{
		int n= data.numColumns();
		int d= data.numRows();
		Vec m= new Vec(d);
		Vec x= new Vec(d);
		m.zero();
		// I think this recurrence relation approach is better for many n
		for (int i= 0; i < n; i++)
		{
			data.getColumn(i, x);
			m.scale((double) i / (double) (i + 1));
			x.scale(1.0 / (i + 1));
			Vec.add(m, x, m);
		}
		return m;
	}

	protected void subtractMean(Matrix data, Vec m)
	{
		int n= data.numColumns();
		int d= data.numRows();

		Vec x= new Vec(d);
		for (int ix= 0; ix < n; ix++)
		{
			data.getColumn(ix, x);
			Vec.sub(x, m, x);
			data.setColumn(ix, x);
		}
	}

	/** given collection of Vec with same dimension */
	protected Matrix makeDataMatrixFromExamples(Collection examples)
	{
		int n= examples.size();

		if (n == 0)
			return null;

		Iterator iter= examples.iterator();

		// must be one due to check above
		Vec x= (Vec) iter.next();
		int d= x.getDimension();
		// the matrix we fill in with columns of data
		Matrix data= new Matrix(d, n);

		// set the first column to first point
		data.setColumn(0, x);
		int i= 1;
		while (iter.hasNext())
		{
			x= (Vec) iter.next();
			int local_d= x.dim();
			if (local_d != d)
				throw new DimensionMismatchException();
			data.setColumn(i, x);
			i++;
		}
		return data;
	}

	/**
	    Assumes that the matrix is just the covariance portion without the mean
	    Performs a PCA on it to find the rotated coordinate system
	    Defined as the rotation and sigmas and sets this to have those
	    values.
	 */
	protected void decomposeAndSetCovariance(Matrix covariance)
	{
		int n= covariance.numRows();
		covariance_rotation= new Matrix(n, n);
		covariance_sigma_squared= new Vec(n);

		// in an SVD of a real symmetric matrix,
		// this should give us the right decomp as eig, but
		// handle singular directions (data) better.
		try
		{
			SVD svd= new SVD(covariance);

			// these are variances... need sqrt for sigma (std dev)
			svd.getSingularValues(covariance_sigma_squared);

			svd.getRangeBasisMatrix(covariance_rotation);
		}
		catch (SVDException e)
		{
			e.printStackTrace();
			return;
		}
		recomputeNormalizingConstantAndCovarianceInverse();
	}

	private void makeVecCache()
	{
		if (cache_x == null || cache_x.size() < getDimension())
		{
			cache_x= new Vec(getDimension());
		}

		if (cache_y == null || cache_y.size() < getDimension())
		{
			cache_y= new Vec(getDimension());
		}

	}

	/** create a default zero mean, unit variance, unrotated distribution
	 of only one dimension */
	protected void createDefault()
	{
		int d= 1;

		setMean(new Vec(d));

		Vec sigma= new Vec(d);
		for (int i= 0; i < d; i++)
			sigma.set(i, 1.0);
		setCovarianceVariance(sigma);

		Matrix rot= new Matrix(d, d);
		rot.identity();
		setCovarianceRotation(rot);

	}

	/** Test driver for checking that this works, mainly
	    that my transposes are all good. */
	public static void main(String argh[])
	{
		GaussianRandomVariable grv_x= new GaussianRandomVariable(0, 1);
		GaussianRandomVariable grv_y= new GaussianRandomVariable(0, 4);

		double theta= 0.9;
		Matrix rot_theta= new Matrix(2, 2);
		double cx= Math.cos(theta);
		double sx= Math.sin(theta);
		rot_theta.set(0, 0, cx);
		rot_theta.set(0, 1, sx);
		rot_theta.set(1, 0, -sx);
		rot_theta.set(1, 1, cx);

		Vec m= new Vec(2);
		m.set(0, 5);
		m.set(1, -8);

		int n= 10000;
		int i;
		Vec x= new Vec(2);
		LinkedList list= new LinkedList();
		for (i= 0; i < n; i++)
		{
			x.set(0, grv_x.sample());
			x.set(1, grv_y.sample());
			Vec y= rot_theta.postMult(x);
			Vec.add(y, m, y);
			list.add(y);
		}
		GaussianRandomVector gvec= new GaussianRandomVector(list);
		System.out.println("mean: " + gvec.getMean());
		System.out.println("rot: " + gvec.getCovarianceRotation());
		System.out.println("sigma: " + gvec.getCovarianceVariance());

		// sample the estimated
		LinkedList gen_data= new LinkedList();
		for (i= 0; i < n; i++)
		{
			gen_data.add(gvec.sample());
		}
		GaussianRandomVector gvec_est= new GaussianRandomVector(gen_data);
		System.out.println("Est mean was: " + gvec_est.getMean());
		System.out.println("Est rot was: " + gvec_est.getCovarianceRotation());
		System.out.println("Est sigma was: " + gvec_est.getCovarianceVariance());

		// now test the mahalanobis and density crap.
		// we just sample ddata and return its density value.
		// sample the estimated dist and then get value.
		// easy to check by cutting loading into matlab cuz I am in a hurry
		System.out.println("Begin Matlab Data Of Sampled Density (should look gaussian.");
		for (i= 0; i < 1000; i++)
		{
			x= (Vec) gen_data.get(i);
			System.out.println(x.get(0) + " " + x.get(1) + " " + gvec.getValue(x));
		}
	}
}
