package innards.math.util;

import innards.*;
import innards.math.linalg.*;

/** 
    Vec3StateBuffer.

    Stores the state (value and derivative) of a Vec3 as
    well as offering support for access to the previous value and a
    local buffer. 
    
*/
public class Vec3StateBuffer extends NamedObject
{
	protected Vec3 last_committed_value;
	protected Vec3 current_value;
	protected double last_commit_time = 0.0;
	/**
	   Constructor.
	   @param name The name of this NamedObject
	*/
	public Vec3StateBuffer(String name)
	{
		super(name);
		initState();
	}

	/**
	   Constructor. Uses "Vec3StateBuffer" as the name for this NamedObject.
	   @param name The name of this NamedObject
	*/
	public Vec3StateBuffer()
	{
		this("Vec3StateBuffer");
	}

	/**
	Copies the last committed value to input.
	*/
	public void getCommittedValue(Vec3 v)
	{
		v.setValue(last_committed_value);
	}
	/**
	   Copies the current (uncommitted) to input.
	*/
	public void getCurrentValue(Vec3 v)
	{
		v.setValue(current_value);
	}
	/**
	 Sets the current value to <code>v</code>. Committed value is unaffected.
	*/
	public void setCurrentValue(Vec3 v)
	{
		current_value.setValue(v);
	}

	/**
	 Calculates the numerical derivative at the time of the last committed value and copies it to <code>dv</code>.
	@see #setDerivativeFunction
	*/
	public void getCommittedDerivative(Vec3 dv)
	{
		throw new IllegalArgumentException(" not implemented");
	}

	/**
	   Calculates the numerical derivative using the current value, the last committed value, and the supplied differential time. 
	    @param dt Time difference between the last committed value and the current value.
	    @param dv Answer written to this parameter.
	    @see #setDerivativeFunction
	*/
	public void getCurrentDerivative(double dt, Vec3 dv)
	{
		throw new IllegalArgumentException(" not implemented");
	}

	/**
	   Commits the current value, with an associated time <code>t</code>.
	*/
	public void commitState(double t)
	{
		double dt = t - last_commit_time;
		last_commit_time = t;
		//deriv.addSample(current_value, dt);
		last_committed_value.setValue(current_value);
	}

	/**
	   Sets the Vec3NumericalDerivative implementor used to calculate derivatives.
	*/
	public void setDerivativeFunction(Vec3NumericalDerivative deriv_func)
	{
		throw new IllegalArgumentException(" not implemented");
	}

	/**
	   Clears the state.
	*/
	public void clearState()
	{
		last_committed_value.scale(0.0f);
		current_value.scale(0.0f);
		last_commit_time = 0.0;
//		deriv.init();
	}

	private void initState()
	{
		current_value = new Vec3();
		last_committed_value = new Vec3();
//		deriv = new Vec3TwoSampleDerivative();
	}

}
