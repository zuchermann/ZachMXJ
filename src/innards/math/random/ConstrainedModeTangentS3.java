package innards.math.random;

/**
   A simple subclass of ModeTangentS3 which also contains the value
   for a probability isocontour of the standard Gaussian in the
   tangent space beyond which the density is defined to be zero. 

   @author aries@media.mit.edu
*/

import innards.math.linalg.*;

import java.io.Serializable;
import java.util.*;

public class ConstrainedModeTangentS3 extends ModeTangentS3 implements Serializable
{
	// in mahaalanobis dist
	private double constraint_dist= 1.0e-4;

	public ConstrainedModeTangentS3()
	{
		super();
	}

	/**
	   Estimate the density from the Quaternions in data.
	*/
	public ConstrainedModeTangentS3(Collection data)
	{
		super(data);
		findEllipse(data);
	}

	public ConstrainedModeTangentS3(Quaternion mean, Matrix tangent_axes, Vec axis_weights, double constraint_mahala_dist)
	{
		super(mean, new Quaternion(tangent_axes), axis_weights);
		this.constraint_dist= constraint_mahala_dist;
	}

	public ConstrainedModeTangentS3(Quaternion mean, Quaternion tangent_space_rotation, Vec tangent_space_variances, double constraint_mahala_dist)
	{
		super(mean, tangent_space_rotation, tangent_space_variances);
		this.constraint_dist= constraint_mahala_dist;
	}

	public ConstrainedModeTangentS3(ModeTangentS3 tem, double radius)
	{
		super(tem);
		setConstraintRadius(radius);
	}

	public ConstrainedModeTangentS3(ConstrainedModeTangentS3 ctem)
	{
		super(ctem);
		setConstraintRadius(ctem.getConstraintRadius());
	}

	public String toString()
	{
		String s= "(ConstrainedModeTangentS3 " + super.toString();
		s += " radius:" + constraint_dist;
		s += " )";
		return s;
	}

	public double getConstraintRadius()
	{
		return constraint_dist;
	}

	/** this is sort of dangerous if you aint paying attention */
	public void setConstraintRadius(double r)
	{
		constraint_dist= r;
	}

	/** use the data to find the right radius.  right now just finds the furthest point and contains it on the boundary.*/
	public void setConstraintRadius(Collection data)
	{
		findEllipse(data);
	}

	/** Changes q so that it is on (just inside to avoid roundoff actually) 
	    the constraint ellipse.  Does not care if it is inside or out of the contour originally.  Useful for moving velocities right to the edge or pulling in a broken constraint.  */
	public void projectQuaternionOntoConstraintEllipse(Quaternion q, Quaternion q_constr)
	{
		Quaternion foo= new Quaternion();
		getSpherizedModeTangentVector(q, foo);
		foo.normalize();
		foo.scale(constraint_dist * 0.99);
		liftSpherizedModeTangentVector(foo, q_constr);
	}

	public boolean isQuaternionInsideConstraintEllipse(Quaternion q)
	{
		// we use the degenerate one here!!
		return (getMahalanobisDistance(q) < constraint_dist);
	}

	/** returns true if it had to project */
	public boolean projectQuaternionOntoEllipseOnlyIfOutside(Quaternion q, Quaternion q_constr)
	{
		if (!isQuaternionInsideConstraintEllipse(q))
		{
			projectQuaternionOntoConstraintEllipse(q, q_constr);
			return true;
		}
		return false;
	}

	/* given our estimate of the density, find the smallest ellipse to hold all data.  Problems with outliers with this version... */
	protected void findEllipse(Collection data)
	{
		Iterator iter= data.iterator();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			double r= getNonDegenerateMahalanobisDistance(q);
			if (r > constraint_dist)
				constraint_dist= r;
		}
	}

	// 
	public static void main(String[] argh)
	{
		test_1();
	}

	// this test will create an object from data, use it to make a gaussian
	// spherical generator, then sample that and see what comes out the other
	// end.  Eventually we will make a gaussian generator that 
	// handles the constraint
	private static void test_1()
	{
		Quaternion mean= new Quaternion(new Vec3(.2, -.12, 0.8), 1.566);
		Quaternion tan_rot= new Quaternion(new Vec3(1.0, 0.0, 0.0), 0.0);
		Vec alpha= new Vec(3);
		alpha.set(0, 0.05);
		alpha.set(1, 0.02);
		alpha.set(2, 0.01);

		GaussianQuaternionRandomVariable gqrv= new GaussianQuaternionRandomVariable(mean, tan_rot, alpha);
		int n= 100;
		Collection data= gqrv.sample(n);
		ConstrainedModeTangentS3 ctem= new ConstrainedModeTangentS3(data);
		System.out.println("Got a radius of: " + ctem.getConstraintRadius());

		int outside= 0;
		Iterator iter= data.iterator();
		System.out.println("Now check that radius contains data\n");
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			double r= ctem.getMahalanobisDistance(q);
			if (!ctem.isQuaternionInsideConstraintEllipse(q))
			{
				System.out.println("Got point outside with radius :" + r + " and max is " + ctem.getConstraintRadius());
				outside++;
			}
		}

		System.out.println("Got " + outside + " outside out of " + n + " for a ratio of " + (double) outside / (double) n);

		System.out.println("Now generate new data from the gaussian and see if we get outliers in a shitload of data.  We should get some.\n");

		n= 10000;
		data= gqrv.sample(n);
		iter= data.iterator();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			double r= ctem.getMahalanobisDistance(q);
			if (!ctem.isQuaternionInsideConstraintEllipse(q))
			{
				System.out.println("Got point outside with radius :" + r + " and max is " + ctem.getConstraintRadius());
				outside++;
			}
		}
		System.out.println("Got " + outside + " outside out of " + n + " for a ratio of " + (double) outside / (double) n);

		System.out.println("Now we project them if they are outside, then recheck to see how many are out.");

		iter= data.iterator();
		Quaternion q_proj= new Quaternion();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			ctem.projectQuaternionOntoEllipseOnlyIfOutside(q, q_proj);
			q.setValue(q_proj);
		}

		System.out.println("And then we count outliers again ");

		outside= 0;
		iter= data.iterator();
		while (iter.hasNext())
		{
			Quaternion q= (Quaternion) iter.next();
			double r= ctem.getMahalanobisDistance(q);
			if (!ctem.isQuaternionInsideConstraintEllipse(q))
			{
				System.out.println("Got point outside with radius :" + r + " and max is " + ctem.getConstraintRadius());
				outside++;
			}
		}
		System.out.println("Got " + outside + " outside out of " + n + " for a ratio of " + (double) outside / (double) n);
	}
}
