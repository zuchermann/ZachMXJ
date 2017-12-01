package innards.math.util;

import innards.math.linalg.*;

/**
   A Vec3StateBuffer where the "current value" is a weighted blend of asynchronously added samples.
*/
public class BlendableVec3StateBuffer extends Vec3StateBuffer
{
	private Vec3 accumulated_value = new Vec3();
	private double accumulation = 0;

//	private Vec3NumericalDerivative deriv;
	private double last_commit_time = 0.0;
	boolean committedYet = false;

	/**
	   Constructor.
	   @param name The name of this NamedObject
	*/
	public BlendableVec3StateBuffer(String name)
	{
		super(name);
		initState();
	}

	public void getCommittedValue(Vec3 v)
	{
		v.setValue(last_committed_value);
	}

	public void getCurrentValue(Vec3 v)
	{
		if (accumulation != 0)
			calcCurrentValue(v);
		else
			getCommittedValue(v);
	}

	public void calcCurrentValue(Vec3 v)
	{
		if (accumulation == 0)
			v.zero();
		else
		{
			v.setValue(accumulated_value).scale((float) (1/accumulation));
		}
	}

	/**
	   Adds the sample <code>v</code> with a default weight of 1.0. Committed value is unaffected.
	*/
	public void setCurrentValue(Vec3 v)
	{
		accumulated_value.setValue(v);
		accumulation = 1;
	}

	private Vec3 _cache_setCurrentValue = new Vec3();

	/**
	   Adds the sample <code>v</code> with a blending weight <code>b</code>.
	*/
	public void setCurrentValue(Vec3 v, double b)
	{
		_cache_setCurrentValue.setValue(v);
		_cache_setCurrentValue.scale((float) b);
		accumulated_value = accumulated_value.add(_cache_setCurrentValue);
		accumulation+=b;
	}


	public void commitState(double t)
	{
		double dt = t - last_commit_time;
		last_commit_time = t;
//		deriv.addSample(current_value, dt);
		if (accumulation > 0)
		{
			calcCurrentValue(last_committed_value);
			accumulation = 0;
			accumulated_value.zero();
			committedYet = true;
		}

	}

	public void clearState()
	{
		last_committed_value.scale(0.0f);
		current_value.scale(0.0f);
		last_commit_time = 0.0;
		accumulation = 0;
		accumulated_value.zero();
	}

	private void initState()
	{
		current_value = new Vec3();
		last_committed_value = new Vec3();
		accumulated_value = new Vec3();
	}
}
