package innards.math.random;

import innards.math.linalg.Quaternion;

import java.io.Serializable;

/**
   
   ConstrainedGaussianQuaternionRandomVariable 

A subclass of the GaussianQuaternionRandomVariable in parallel to the
sublassing of ModeTangentS3 to ConstrainedModeTangentS3.

A GQRV which will never return a sample out the isocontour specified
by the constraint radius, which is specified in terms of the
Mahalanobis distance (standard deviations).

@see ModeTangentS3
@see ConstrainedModeTangentS3

*/
public class ConstrainedGaussianQuaternionRandomVariable extends GaussianQuaternionRandomVariable implements Serializable, QuaternionRandomVariable
{
	private ConstrainedModeTangentS3 ctem= null;

	public ConstrainedGaussianQuaternionRandomVariable(ConstrainedModeTangentS3 ctem)
	{
		// the super stores it as a ModeTangent reference, which loses the constraint, so we keep constraint separate.  Yeah, I am lazy.
		super(ctem);
		this.ctem= ctem;
	}

	/**
	   @param radius is the value for the constraint isocontour beyond which p.d.f. = 0.
	*/
	public ConstrainedGaussianQuaternionRandomVariable(ModeTangentS3 tem, double radius)
	{
		super(tem);
		ctem= new ConstrainedModeTangentS3(tem, radius);
	}

	public void setConstraintRadius(double r)
	{
		ctem.setConstraintRadius(r);
	}

	public double getConstraintRadius()
	{
		return ctem.getConstraintRadius();
	}

	/** override sample to reject any points generated outside of radius */
	public void sample(Quaternion q)
	{
		// if the radius is too small, just return the mode since rejection sampling sucks
		// really should do some linear noise here maybe
		if (getConstraintRadius() < 1.0e-2)
		{
			ctem.getMode(q);
		}

		else
		{
			do
			{
				super.sample(q);
			}
			while (!ctem.isQuaternionInsideConstraintEllipse(q));
		}
	}

	public Quaternion sample()
	{
		Quaternion q= new Quaternion();
		sample(q);
		return q;
	}
}
