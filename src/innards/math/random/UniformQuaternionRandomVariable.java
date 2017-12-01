package innards.math.random;

/**
   As per Shoemake's Gem, we create a spherical gaussian in R4, then
   rejection sample for a point inside the unit sphere (in the solid
   ball).  We also reject 0.0.  Then we normalize it and this will be
   a uniform dist in quat since uniform on hypersphere.
   
   @author Michael Patrick Johnson <aries@media.mit.edu>
*/

import innards.math.linalg.*;

import java.io.Serializable;

public class UniformQuaternionRandomVariable implements QuaternionRandomVariable, Serializable
{
	private GaussianRandomVariable grv= new GaussianRandomVariable(0, 1);

	public UniformQuaternionRandomVariable()
	{
	}

	public Quaternion sample()
	{
		Quaternion q= new Quaternion();
		sample(q);
		return q;
	}

	public void sample(Quaternion q)
	{
		Vec v= new Vec(4);
		do
		{
			v.set(0, grv.sample());
			v.set(1, grv.sample());
			v.set(2, grv.sample());
			v.set(3, grv.sample());
		}
		while (v.mag() > 1.0 || v.mag() < 1.0e-6);

		v.normalize();
		q.setValue(v.get(0), v.get(1), v.get(2), v.get(3));
	}
}
