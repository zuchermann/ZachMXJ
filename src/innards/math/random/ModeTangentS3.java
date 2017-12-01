package innards.math.random;

/**
   ModeTangentS3

   An approximate Bingham second order probability distribution on the
   hypersphere S^3 in R^4.  Essentially defines spherical ellipsoids.
   One way to think about it is as a normal Gaussian in the embedding
   space which is singular along one of the axes and intersected with
   the surface of the sphere, but I am not sure this is exactly right yet.  

   This is used for modelling antipodally symmetric probability on the
   sphere, which is exactly the probability of the space of
   directions, or rotations in R^3, or projective 3-space.

   Currently it is not truly a valid density as it does not integrate
   over the sphere to 1, but to some arbitrary constant that I need to
   figure out how to compute (ostensibly by doing the integral or
   implementing the confluent hypergeometric function of matrix
   argument).

   Also, there may be problems for degenerate densities which do not
   have 3 DOFs due to roundoff error.  Need to think of a way to get
   around this with some sort of epsilon region falloff to zero in
   stead of any deviation from the subspace.

   Officially, the algorithm used here is to find the mode of the
   data, transform all the data to the mode, then map into the tangent
   space there, then do a normal 3D PCA on the resultant vector space.
   Have not proven how this relates to the normal bingham method yet.

   @author Michael Patrick Johnson <aries@media.mit.edu> 

*/

import innards.debug.Debug;
import innards.math.linalg.*;

import java.io.Serializable;
import java.util.*;

public class ModeTangentS3 implements Serializable
{
	// to avoid roundoff error with degenerate stuff, zero variance set to this.
	private final static double MIN_VARIANCE= 1.0e-6;

	private Quaternion tangent_space_rotation= new Quaternion();
	private double[] axis_variances= new double[3];
	private Quaternion mode= new Quaternion();
	private Quaternion mode_conj= new Quaternion();

	private Quaternion quat_temp= new Quaternion();

	// fix me
	private double normalizing_constant= 1.0;

	/** 
	    for serialize mostly, but also creates a uniform density
	 */
	public ModeTangentS3()
	{
		init();
	}

	public ModeTangentS3(ModeTangentS3 bing)
	{
		setValue(bing);
	}

	/**
	   Estimate the density from the Quaternions in data.
	*/
	public ModeTangentS3(Collection data)
	{
		estimateDensity(data);
	}

	public ModeTangentS3(Quaternion mean, Matrix tangent_axes, Vec axis_weights)
	{
		this(mean, new Quaternion(tangent_axes), axis_weights);
	}

	public ModeTangentS3(Quaternion mean, Quaternion tangent_space_rotation, Vec tangent_space_variances)
	{
		init();
		for (int i= 0; i < 3; i++)
		{
			axis_variances[i]= tangent_space_variances.get(i);
		}
		this.tangent_space_rotation.setValue(tangent_space_rotation);
		this.mode.setValue(mean);

		this.mode_conj.setValue(mean);
		this.mode_conj.conjugate();

		normalizing_constant= 1.0; // errr this is bad
	}

	public String toString()
	{
		String s= "(ModeTangentS3 mode:" + mode + " ";
		s += "tan_rot:" + tangent_space_rotation + " ";
		s += "var:" + axis_variances[0] + " " + axis_variances[1] + " " + axis_variances[2];
		s += " )";
		return s;
	}

	public void setValue(ModeTangentS3 bing)
	{
		init();
		for (int i= 0; i < 3; i++)
		{
			axis_variances[i]= bing.axis_variances[i];
		}
		tangent_space_rotation.setValue(bing.tangent_space_rotation);
		mode.setValue(bing.mode);
		mode_conj.setValue(bing.mode_conj);
		normalizing_constant= bing.normalizing_constant;
	}

	/** Place the mode of this distribution into the argument.
	    Recall that both mode and -mode are valid.  No constraint on
	    which you get is specified.  
	*/
	public void getMode(Quaternion mode)
	{
		mode.setValue(this.mode);
	}

	public double getTangentAxisVariance(int i)
	{
		return axis_variances[i];
	}

	public void getTangentAxisVariances(Vec v)
	{
		for (int i= 0; i < 3; i++)
			v.set(i, axis_variances[i]);
	}

	public void getTangentSpaceRotation(Quaternion q)
	{
		q.setValue(tangent_space_rotation);
	}

	/**
	   Return the value of the distribution at the given point.
	   CURRENTLY this does return a normalized value until I can figure out how to calculate the confluent hypergeometric function of matrix argument.
	   Also, notice that for a degenerate distribution this will return 0 if the point is in a degenerate place.
	   So for now, this is a model, but not probability, though the mahalanobis is still quite useful and any properties that use the logdensity.
	*/
	public double getValue(Quaternion q)
	{
		double d= getMahalanobisDistance(q);
		return Math.exp(-0.5 * d * d);
	}

	/** 
	    Return the mahalanobis distance.  Note that this might
	    return POSITIVE_INFINITY if the asked point is in a degenerate
	    direction.  
	*/
	public double getMahalanobisDistance(Quaternion q)
	{
		getSpherizedModeTangentVector(q, quat_temp);
		return quat_temp.mag();
	}

	/** 
	    Return the squared mahalanobis distance for a quaternion living in the non-singular subspace.  
	
	    Note that this will
	    project the q onto the nearest point that is not degenerate in
	    the case of a degenerate variance (one or more variances are
	    zero), then use the mahalanobis distance for the other directions.
	    
	    If the degenerate one is wanted, then call that.
	*/
	public double getNonDegenerateMahalanobisDistance(Quaternion q)
	{
		getUnweightedTangentVector(q, quat_temp);
		double root2= Math.sqrt(2.0);
		for (int i= 1; i < 4; i++)
		{
			double sigma= Math.sqrt(axis_variances[i - 1]);
			// sketchy.  If the point is invalid (degenerate) then dist would be infinite.  So I project out those directions.
			if (sigma >= MIN_VARIANCE)
				sigma= 1.0 / (sigma * root2);
			else
				sigma= 0.0;

			quat_temp.set(i, quat_temp.get(i) * sigma);
		}
		return quat_temp.mag();
	}

	/**
	   Return the arclength to the mode, unweighted by the 
	   variances.
	*/
	public double getUnweightedDistanceToMode(Quaternion q)
	{
		return Quaternion.distAngular(q, mode);
	}

	public void getSpherizedModeTangentVector(Quaternion q, Quaternion q_tan)
	{
		double root2= Math.sqrt(2.0);
		getUnweightedTangentVector(q, q_tan);
		for (int i= 1; i < 4; i++)
		{
			double sigma= Math.sqrt(axis_variances[i - 1]);
			q_tan.set(i, q_tan.get(i) / (sigma * root2));
		}
	}

	public void getUnweightedTangentVector(Quaternion q, Quaternion q_tan)
	{
		q_tan.setValue(q);
		flipToModeSide(q_tan);
		q_tan.concatLeft(mode_conj);
		Quaternion.ln(q_tan, q_tan);
		// now we have a tangent vector at the identity.

		// rotate it into principal axes using inverse of rotation
		tangent_space_rotation.inverse();
		q_tan.concatLeft(tangent_space_rotation);
		// put it back
		tangent_space_rotation.inverse();
		q_tan.concatRight(tangent_space_rotation);

	}

	/** 
	    Put the tangent vector at the back onto the sphere at the right
	    spot.  An obvious invariant on the rep is that
	    liftSpherizedModeTangentVector() is the inverse of
	    getSpherizedTangentVector().  
	*/
	public void liftSpherizedModeTangentVector(Quaternion q_tan, Quaternion q)
	{
		double root2= Math.sqrt(2.0);
		q.setValue(q_tan);
		// undo the sigma weighting to make elliptical.
		for (int i= 1; i < 4; i++)
		{
			double sigma= Math.sqrt(axis_variances[i - 1]);
			q.set(i, q.get(i) * sigma * root2);
		}

		// undo the principal axis rotation.
		Vec3 x= new Vec3();
		q.toVec3(x);
		tangent_space_rotation.rotateVec(x);
		q.fromVec3(x);

		// back to group
		Quaternion.exp(q, q);

		// and back to mode
		q.concatLeft(mode);
	}

	/** 
	    Estimate the density estimates from the given data.  Not sure if
	    Maximum Likelihood yet.  Gotta do some proofs.  Currently I find
	    the mode in the distance metric of the sphere, then use the
	    quaternion representative of the antipodal equivalence on the
	    mode side (since the mode direction is also arbitrary).  I then
	    map the points into the tangent space at the mode.  Then I do a
	    normal PCA in the tangent space to get the vectors and use the
	    eigenvalues as the variances.
	*/
	public void estimateDensity(Collection data)
	{
		Quaternion.centroid(new Vector(data), mode);
		mode.normalize();
		convertDataToLocalHemisphere(data, mode);
		mode_conj.setValue(mode);
		mode_conj.conjugate();

		int n= data.size();
		Quaternion tan= new Quaternion();
		Matrix scatter= new Matrix(3, 3);

		Iterator iter= data.iterator();
		while (iter.hasNext())
		{
			tan.setValue((Quaternion) iter.next());
			tan.concatLeft(mode_conj);
			Quaternion.ln(tan, tan);
			for (int i= 0; i < 3; i++)
			{
				for (int j= 0; j < 3; j++)
				{
					scatter.set(i, j, scatter.get(i, j) + tan.get(i + 1) * tan.get(j + 1));
				}
			}
		}
		scatter.scale(1.0 / n);
		try
		{
			EigenStructure eig= new EigenStructure(scatter);
			// turn the rotation matrix into a quat.
			tangent_space_rotation.fromMatrix(eig.getEigenvectors());
			tangent_space_rotation.normalize();

			Vec eig_vals= eig.getEigenvalues();
			axis_variances[0]= eig_vals.get(0);
			axis_variances[1]= eig_vals.get(1);
			axis_variances[2]= eig_vals.get(2);
		}
		catch (EigenStructureException e)
		{
			e.printStackTrace();
			Debug.doAssert(false, "Bad eigenvalue decomp.  Not sure what the do.");
		}
		for (int i= 0; i < 3; i++)
		{
			if (axis_variances[i] < MIN_VARIANCE)
				axis_variances[i]= MIN_VARIANCE;
		}
	}

	/** 
	    Flips each data element in q to force them all into the local
	    hemisphere which contains hemi and is defined by the great circle
	    perpendicular to hemi.  
	*/
	private static void convertDataToLocalHemisphere(Collection data, Quaternion hemi)
	{
		Quaternion nq= new Quaternion();
		Iterator iter= data.iterator();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			nq.setValue(q);
			nq.negate();
			if (Quaternion.distAngular(nq, hemi) < Quaternion.distAngular(q, hemi))
			{
				q.setValue(nq);
			}
		}
	}

	private void flipToModeSide(Quaternion q)
	{
		double d= Quaternion.distAngular(q, mode);
		q.negate();
		double nd= Quaternion.distAngular(q, mode);
		if (d <= nd) // if unflipped is closer, put it back
		{
			q.negate();
		}
		// else leave it.
	}

	/**
	   Initialize the structure to the uniform density by having the mode through the identity, the coordinate x,y,z axes as the others, and variances infinite along the axes.
	*/
	private void init()
	{
		for (int i= 0; i < 3; i++)
			axis_variances[i]= Double.POSITIVE_INFINITY;
	}

	public static void main(String[] argh)
	{
		System.out.println("Got here\n");
		test_2();
		System.out.println("Did test\n");
	}

	private static void test_2()
	{
		/*
		  double x_min = -Math.PI/4;
		  double x_max = Math.PI/4;
		  double y_min = -Math.PI/6;
		  double y_max = Math.PI/6;
		  double z_min = -Math.PI/8;
		  double z_max = Math.PI/8;
		*/

		double x_min= -Math.PI / 3;
		double x_max= Math.PI / 3;
		double y_min= -Math.PI / 4;
		double y_max= 0.0;
		double z_min= 0.0;
		double z_max= Math.PI / 4;

		Collection data= makeRandomQuats(100000, x_min, x_max, y_min, y_max, z_min, z_max);

		ModeTangentS3 bing= new ModeTangentS3(data);
		Quaternion tan_rot= new Quaternion();
		Quaternion mode_est= new Quaternion();
		bing.getMode(mode_est);
		bing.getTangentSpaceRotation(tan_rot);
		double x_var_est= bing.getTangentAxisVariance(0);
		double y_var_est= bing.getTangentAxisVariance(1);
		double z_var_est= bing.getTangentAxisVariance(2);

		Debug.doReport("ModeTangentS3", "Estimated rotation in tan space: " + tan_rot);
		Debug.doReport("ModeTangentS3", "Estimated vars: [" + x_var_est + " " + y_var_est + " " + z_var_est + "]  and \n mode_est " + mode_est);

		Quaternion q= makeRandomQuat(x_min, x_max, y_min, y_max, z_min, z_max);
		Quaternion q_tan= new Quaternion();
		Quaternion q_lifted= new Quaternion();
		bing.getSpherizedModeTangentVector(q, q_tan);
		Debug.doReport("ModeTangentS3", "Random q : " + q);
		Debug.doReport("ModeTangentS3", "Spherized Tan Vec is: " + q_tan);
		bing.liftSpherizedModeTangentVector(q_tan, q_lifted);
		Debug.doReport("ModeTangentS3", "Relifted spherized Tan Vec is: " + q_lifted);
		Debug.doReport("ModeTangentS3", "Should be same as " + q);

		Debug.doReport("ModeTangentS3", "Est Mode should have mahalo 0 : is " + bing.getMahalanobisDistance(mode_est));

		Quaternion mode= new Quaternion(1, 0, 0, 0);
		Debug.doReport("ModeTangentS3", "Real Mode " + mode + " should have mahalo near 0 : is " + bing.getMahalanobisDistance(mode));

		for (int i= 0; i < 100; i++)
		{
			q= makeRandomQuat(x_min, x_max, y_min, y_max, z_min, z_max);
			Debug.doReport("ModeTangentS3", "q = " + q + " has mahalo " + bing.getMahalanobisDistance(q));
		}

		for (int i= 0; i < 100; i++)
		{
			q= makeRandomQuat(-x_min, x_min, -y_min, y_min, -z_min, z_min);
			Debug.doReport("ModeTangentS3", "q = " + q + " has mahalo " + bing.getMahalanobisDistance(q));
		}
	}

	private static void test_1()
	{
		UniformRandomVariable urv= new UniformRandomVariable(0.0, Math.PI / 2.0);
		double x_var= urv.sample();
		double y_var= urv.sample();
		double z_var= urv.sample();
		Quaternion mode= new Quaternion(0, 1, 0, 0);
		Collection data= makeRandomQuats(1000, mode, x_var, y_var, z_var);
		ModeTangentS3 bing= new ModeTangentS3(data);
		Quaternion tan_rot= new Quaternion();
		Quaternion mode_est= new Quaternion();
		bing.getMode(mode_est);
		bing.getTangentSpaceRotation(tan_rot);
		double x_var_est= bing.getTangentAxisVariance(0);
		double y_var_est= bing.getTangentAxisVariance(1);
		double z_var_est= bing.getTangentAxisVariance(2);

		Debug.doReport("ModeTangentS3", "Made unrotated Gaussian with variances [" + x_var + " " + y_var + " " + z_var + "] and mode " + mode);
		Debug.doReport("ModeTangentS3", "Estimated rotation in tan space (should be unity): " + tan_rot);
		Debug.doReport("ModeTangentS3", "Estimated vars: [" + x_var_est + " " + y_var_est + " " + z_var_est + "]  and mode " + mode_est);

		Quaternion q= makeRandomQuat(mode, x_var, y_var, z_var);
		Quaternion q_tan= new Quaternion();
		Quaternion q_lifted= new Quaternion();
		bing.getSpherizedModeTangentVector(q, q_tan);
		Debug.doReport("ModeTangentS3", "Random q : " + q);
		Debug.doReport("ModeTangentS3", "Spherized Tan Vec is: " + q_tan);
		bing.liftSpherizedModeTangentVector(q_tan, q_lifted);
		Debug.doReport("ModeTangentS3", " Relifted spherized Tan Vec is: " + q_lifted);
		Debug.doReport("ModeTangentS3", "Should be same as " + q);

		Debug.doReport("ModeTangentS3", "Est Mode should have mahalo 0 : is " + bing.getMahalanobisDistance(mode_est));

		Debug.doReport("ModeTangentS3", "Real Mode should have mahalo near 0 : is " + bing.getMahalanobisDistance(mode));

		for (int i= 0; i < 10; i++)
		{
			q= makeRandomQuat(mode, x_var, y_var, z_var);
			Debug.doReport("ModeTangentS3", "q = " + q + " has mahalo " + bing.getMahalanobisDistance(q));
		}
	}

	/** Make random quats by choosing random angles in the euler set and consing up a quaternion.  Not very nice way to do it, but OK for test. 
	 */
	private static Collection makeRandomQuats(int n, Quaternion mode, double x_var, double y_var, double z_var)
	{
		List data= new LinkedList();
		for (int i= 0; i < n; i++)
		{
			data.add(makeRandomQuat(mode, x_var, y_var, z_var));
		}
		return data;
	}

	private static Collection makeRandomQuats(int n, double x_min, double x_max, double y_min, double y_max, double z_min, double z_max)
	{
		List data= new LinkedList();
		for (int i= 0; i < n; i++)
		{
			data.add(makeRandomQuat(x_min, x_max, y_min, y_max, z_min, z_max));
		}
		return data;
	}

	private static Quaternion makeRandomQuat(double x_min, double x_max, double y_min, double y_max, double z_min, double z_max)
	{
		QuaternionRandomVariable qrv= new EulerQuaternionRandomVariable(x_min, x_max, y_min, y_max, z_min, z_max);
		return qrv.sample();

	}

	private static Quaternion makeRandomQuat(Quaternion mode, double x_var, double y_var, double z_var)
	{

		GaussianRandomVariable xrv= new GaussianRandomVariable(0.0, x_var);
		GaussianRandomVariable yrv= new GaussianRandomVariable(0.0, y_var);
		GaussianRandomVariable zrv= new GaussianRandomVariable(0.0, z_var);
		Quaternion q= new Quaternion(0.0, xrv.sample(), yrv.sample(), zrv.sample());
		Quaternion.exp(q, q);
		q.concatLeft(mode);
		return q;
	}

}
