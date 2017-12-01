package innards.math.random;

/**
   GaussianQuaternionRandomVariable

   Uses the ModeTangentS3 structure in order to estimate and sample
   from a Gaussian distribution for quaternion data mapped into the
   tangent space at the mean.  Since the tangent space at the mean as
   mapped by the logmap is locally Euclidean and the map preserves
   distance metric and angle, the vector (linear) gaussian vector
   model is valid to use here for a R^3 gaussian, rather than a R^4
   Gaussian (zero mean) as in the case of the Bingham distribution.  I
   need to implement some Bingham distribution stuff soon.  There, the
   distribution is singular due to the constraint of unity on the
   quaternion magnitude, which is why I prefer this one based on the
   local linearization by the exponential map (Lie algebra) of SU(2).

   @author aries@media.mit.edu
*/

import innards.math.linalg.*;

import java.io.Serializable;
import java.util.*;

public class GaussianQuaternionRandomVariable implements Serializable, QuaternionRandomVariable
{
	// holds the prob data structure
	private ModeTangentS3 tem;

	// holds the vector space version for sampling only
	private GaussianRandomVector grv;

	/** Given the Collection of Quaternion data, create a GQRV estimated
	 *  from the data.  
	 */

	public GaussianQuaternionRandomVariable(Collection data)
	{
		tem= new ModeTangentS3(data);
		grv= createGaussianRandomVectorFromModeTangentS3(tem);
	}

	/**
	   Use the ModeTangentS3 argument as the probability density for this GQRV.  
	*/
	public GaussianQuaternionRandomVariable(ModeTangentS3 tem)
	{
		this.tem= new ModeTangentS3(tem);
		grv= createGaussianRandomVectorFromModeTangentS3(tem);
	}

	/**
	   Create a copy of the GQRV.
	*/
	public GaussianQuaternionRandomVariable(GaussianQuaternionRandomVariable gqrv)
	{
		this.tem= new ModeTangentS3(gqrv.tem);
		grv= createGaussianRandomVectorFromModeTangentS3(tem);
	}

	/**
	   Create a ModeTangentS3 given the arguments and use it as the
	   density of this GQRV.
	
	   @param mean the quaternion mean (in SU(2))
	   @param tangent_space_axes the principal axes of the tangent space
	   (logmapped at mean) gaussian form the columns of this 3x3 matrix
	   in SO(3).
	   @param axis_weights a Vec(3) which contains the weights of the
	   principal axes.  These should be considered as variances in the
	   tangent space with respect to the quaternion distance metric
	   (twice that of the SO(3) due to 2-1 covering).
	*/
	public GaussianQuaternionRandomVariable(Quaternion mean, Matrix tangent_space_axes, Vec axis_weights)
	{
		this.tem= new ModeTangentS3(mean, tangent_space_axes, axis_weights);
		grv= createGaussianRandomVectorFromModeTangentS3(tem);
	}

	/**
	   Same as Matrix tangent_space_axes version, except a quaternion is
	   used to describe the the axes since they are a rotation in SO(3).
	   @see GaussianQuaternionRandomVariable(Quaternion,Matrix,Vec)
	   
	*/
	public GaussianQuaternionRandomVariable(Quaternion mean, Quaternion tangent_space_axes, Vec axis_weights)
	{
		this.tem= new ModeTangentS3(mean, tangent_space_axes, axis_weights);
		grv= createGaussianRandomVectorFromModeTangentS3(tem);
	}

	/**
	   Return a new Collection of n Quaternion's sampled from this GQRV.
	*/
	public Collection sample(int n)
	{
		Collection data= new LinkedList();
		for (int i= 0; i < n; i++)
		{
			Quaternion q= new Quaternion();
			sample(q);
			data.add(q);
		}
		return data;
	}

	/**
	   Create one sample and put it in  q.
	*/
	public void sample(Quaternion q)
	{
		createAuxIfNeeded();

		// generate from the grv and then lift to sphere and add mean 
		Vec w= grv.sample();
		q.setW(0.0f);
		q.setX(w.get(0));
		q.setY(w.get(1));
		q.setZ(w.get(2));

		// lift 
		Quaternion.exp(q, q);

		// add mean
		Quaternion mean= new Quaternion();
		tem.getMode(mean);
		q.concatLeft(mean);
	}

	/**
	   Return a new Quaternion sampled from the distribution.
	 */
	public Quaternion sample()
	{
		Quaternion q= new Quaternion();
		sample(q);
		return q;
	}

	public void getMean(Quaternion mean)
	{
		tem.getMode(mean);
	}

	public void getTangentSpaceRotation(Quaternion r)
	{
		tem.getTangentSpaceRotation(r);
	}

	public void getTangentSpaceRotation(Matrix r)
	{
		Quaternion q= new Quaternion();
		tem.getTangentSpaceRotation(q);
		q.toMatrix(r);
	}

	/** 
	    Get the variance along the ith tangent space axis
	*/
	public double getTangentAxisVariance(int i)
	{
		return tem.getTangentAxisVariance(i);
	}

	public void getTangentAxisVariances(Vec variances)
	{
		tem.getTangentAxisVariances(variances);
	}

	/** 
	    Get the ith tangent axis and put it into x.
	 */
	public void getTangentAxis(int i, Vec x)
	{
		Matrix m= grv.getCovarianceRotation();
		m.getColumn(i, x);
	}

	public String toString()
	{
		String s= "GaussianQuaternionRandomVariable {\n";
		s= s + "tem {" + tem + "}\n";
		s= s + "grv {" + grv + "}\n";
		s= s + "}\n}";
		return s;
	}

	/* Private Benjamin */

	protected void createAuxIfNeeded()
	{
		if (grv == null)
		{
			grv= createGaussianRandomVectorFromModeTangentS3(tem);
		}
	}

	/** creates a gaussian random vector which represents the zero mean variances in the tangent space.  For sampling only. */
	protected GaussianRandomVector createGaussianRandomVectorFromModeTangentS3(ModeTangentS3 mts)
	{
		Vec mean= new Vec(3);
		mean.zero();

		Matrix U= new Matrix(3, 3);
		Quaternion q= new Quaternion();
		mts.getTangentSpaceRotation(q);
		q.toMatrix(U);

		Vec v= new Vec(3);
		v.set(0, mts.getTangentAxisVariance(0));
		v.set(1, mts.getTangentAxisVariance(1));
		v.set(2, mts.getTangentAxisVariance(2));

		return new GaussianRandomVector(mean, U, v);
	}

	public ModeTangentS3 getModeTangentS3()
	{
		return tem;
	}

	/** 
	    O Lord God Almighty, how dost I tempt this class? 
	 */
	public static void main(String argh[])
	{
		Quaternion mean= new Quaternion(new Vec3(.3, -.8, -.1), -.87);
		Matrix U= new Matrix(3, 3);
		Quaternion tan_axes= new Quaternion(new Vec3(.36, -.23, .7), 0.7874665);
		Vec3 x_axis= new Vec3(1, 0, 0);
		Vec3 y_axis= new Vec3(0, 1, 0);
		Vec3 z_axis= new Vec3(0, 0, 1);
		tan_axes.rotateVec(x_axis);
		tan_axes.rotateVec(y_axis);
		tan_axes.rotateVec(z_axis);

		Vec axis= new Vec(3);
		x_axis.toVec(axis);
		U.setColumn(0, axis);

		y_axis.toVec(axis);
		U.setColumn(1, axis);

		z_axis.toVec(axis);
		U.setColumn(2, axis);

		// check to see we get the same!
		Matrix V= new Matrix(3, 3);
		tan_axes.toMatrix(V);

		System.out.println("U rotation is columns of transformed axes: " + U);
		System.out.println("V rotation is q.toMatrix(): " + V);
		System.out.println("We shall use the U for the rest...");

		Vec alpha= new Vec(3);
		alpha.set(0, .4);
		alpha.set(1, .1);
		alpha.set(2, .2);

		GaussianQuaternionRandomVariable gqrv= new GaussianQuaternionRandomVariable(mean, U, alpha);
		int n= 200000;
		LinkedList data= new LinkedList();
		for (int i= 0; i < n; i++)
		{
			data.add(gqrv.sample());
		}

		GaussianQuaternionRandomVariable gqrv2= new GaussianQuaternionRandomVariable(data);
		Vec alpha2= new Vec(3);
		Matrix U2= new Matrix(3, 3);
		Quaternion mean2= new Quaternion();

		gqrv2.getMean(mean2);
		gqrv2.getTangentSpaceRotation(U2);
		gqrv2.getTangentAxisVariances(alpha2);

		System.out.println("Original mean: " + mean);
		System.out.println("Estimated mean: " + mean2);

		System.out.println("Original Tan Axes: " + U);
		System.out.println("Estimated Tan Axes: " + U2);

		System.out.println("Original Tan Vars: " + alpha);
		System.out.println("Estimated Tan Vars: " + alpha2);

		System.out.println("qrbv is: " + gqrv);
		System.out.println("qrbv_est is: " + gqrv2);

	}
}
