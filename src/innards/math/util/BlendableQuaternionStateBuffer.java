package innards.math.util;

import innards.debug.ANSIColorUtils;
import innards.math.linalg.*;

/**
   Like QuaternionStateBuffer, but spherically blends the quaternions added via setCurrentValue. The blends are done by taking the exponential map of the quaternions, and doing a weighted sum with provided weights (default weight = 1). CommitState commits the value of this "summed quaternion". Contains methods to override all previously added samples and set the current value without blending.
    @see QuaternionStateBuffer
    @author marc
*/

public class BlendableQuaternionStateBuffer extends QuaternionStateBuffer
{
	protected Quaternion last_committed_value= new Quaternion();
	private Vec3 accumulated_value= new Vec3();
	private Quaternion cache_accumulated_value= new Quaternion();
	private Vec3 cache_accumulated_valueV= new Vec3();
	private Quaternion current_value= new Quaternion();
	private double accumulation= 0.0;
	;

	private QuaternionNumericalDerivative deriv= new QuaternionTwoSampleDerivative();
	private double last_commit_time= 0.0;

	private boolean dirty= true;

	/**
	   Constructor.
	   @param name The name of this NamedObject.
	*/
	public BlendableQuaternionStateBuffer(String name)
	{
		super(name);
		//report(" boo");
	}

	/**
	   Copies the last committed value into <code>q</code>.
	*/
	public void getCommittedValue(Quaternion q)
	{
		q.setValue(last_committed_value);
	}

	/**
	   Copies the weighted average of all quaternions stored so far via setCurrentValue.
	*/
	public void getCurrentValue(Quaternion q)
	{
		/*if (!dirty)
		{
		    q.setValue(current_value);
		    return;
		}*/
		if (onlyFirstOne)
		{
			q.setValue(onlyOne);
			return;
		}
		if (accumulation == 0)
		{
			q.setValue(last_committed_value);
			return;
		} else
		{
			cache_accumulated_valueV.setValue(accumulated_value);
			cache_accumulated_valueV.scale((float) (1 / accumulation));
			cache_accumulated_value.fromVec3(cache_accumulated_valueV);
			
			//System.out.println(" getCurrentValue:"+ANSIColorUtils.green(""+accumulation)+" "+ANSIColorUtils.yellow(""+cache_accumulated_valueV));
			Quaternion.exp(cache_accumulated_value, current_value);
			q.setValue(current_value);
		}
	}

	/**
	   Adds a sample with blending weight 1.0
	*/
	public void setCurrentValue(Quaternion q)
	{
		//current_value.setValue(q);
		this.setCurrentValue(q, 1);
	}
	Quaternion tempQuat= new Quaternion();

	// marc changes this so that if you only write into it once, it
	// doesn't bother to take the ln of it only to take the exp of it
	Quaternion onlyOne= new Quaternion();
	double onlyOneAmount= 0;
	boolean hasFirstOne= false;
	boolean onlyFirstOne= false;

	/**
	   Adds a weighted sample.
	   @param q The sample.
	   @param am The blending weight of q.
	*/
	public void setCurrentValue(Quaternion q, double am)
	{
		/*
		if (this.getName().equals("R_ear"))
		{
		    System.out.println(" set bqsb: R_ear = "+q+":"+am);
		}
		*/
		if (!hasFirstOne)
		{
			onlyFirstOne= true;
			hasFirstOne= true;
			onlyOne.setValue(q);
			onlyOneAmount= am;
			return;
		}
		if (onlyFirstOne)
		{
			onlyFirstOne= false;
			setCurrentValueImpl(onlyOne, onlyOneAmount);
			setCurrentValueImpl(q, am);
			return;
		}

		setCurrentValueImpl(q, am);
	}

	/**
	   Implementation of setCurrentValue.
	*/
	protected void setCurrentValueImpl(Quaternion q, double am)
	{
		dirty= true;
		//report(getName() + "setting value <"+q+","+am+">");
		boolean negated= false;
		if (accumulation == 0.0)
		{
			tempQuat.setValue(1.0, 0.0, 0.0, 0.0);
		} else
			getCurrentValue(tempQuat); //this call sucks, dude!
		
		negated = Quaternion.replaceWithShortestArcRepresentativeIfNecessary(q,tempQuat);
		//if (negated) System.out.println(ANSIColorUtils.red(" negated "));
		
		Quaternion.ln(q, cache_accumulated_value);
		cache_accumulated_value.toVec3(cache_accumulated_valueV);
		cache_accumulated_valueV.scale((float) am);
		accumulation += am;
		Vec3.add(accumulated_value, cache_accumulated_valueV, accumulated_value);
		if (negated)
			q.negate();
	}

	public void getCommittedDerivative(Quaternion dq)
	{
		deriv.getDerivative(dq);
	}

	public void getCurrentDerivative(double dt, Quaternion dq)
	{
		deriv.calcDerivativeWithoutAddingSample(current_value, dt, dq);
	}

	/**
	   Ignores previously stored samples, forcing the current value to be the exponential map of <code>q</code>, scaled by <code>withWeight</code>.
	*/
	public void forceCurrentValue(Quaternion q, double withWeight)
	{
		/*
		if (this.getName().equals("R_ear"))
		{
		    System.out.println(" force bqsb: R_ear = "+q+":"+withWeight);
		}
		*/
		accumulated_value.zero();
		accumulation= 0;
		this.hasFirstOne= false;
		this.setCurrentValue(q, withWeight);
	}

	/**
	   Commits the current value, with associated time <code>t</code>.
	*/
	public void commitState(double t)
	{
		double dt= t - last_commit_time;
		last_commit_time= t;
		/*
		        if (!hasFirstOne)
		        {
		
		        }
		        if (hasFirstOne && onlyFirstOne)
		        {
		            current_value.setValue(onlyOne);
		            last_committed_value.setValue(onlyOne);
		            hasFirstOne = false;
		            if (blendWeight>0.0) this.setCurrentValue(last_committed_value,blendWeight);
		
		            return;
		        }
		  */

		//deriv.addSample(current_value, dt);
		getCurrentValue(current_value);
		//if (accumulation >1.0) report(" \n\n\n\n\n\n\n\n\n\n\n\n accumulation > 1.0 actually "+accumulation+" \n\n\n\n\n\n\n\n\n\n\n");
		//System.out.println(getName()+": currentValue = "+current_value+" debugValue = "+debugCache+" diff = "+Quaternion.distAngular(current_value,debugCache)+" accumulation = "+accumulation);
		accumulation= 0;
		accumulated_value.zero();
		dirty= false;
		last_committed_value.setValue(current_value);

		hasFirstOne= false;
		if (blendWeight > 0.0)
			this.setCurrentValue(last_committed_value, blendWeight);

		//deriv.addSample(last_committed_value, dt);
	}

	/**
	   Setting this to some nonzero value intorduces low-pass filtering. After this is set to a nonzero value, any added quaternions will be blended with the last committed value, with the committed value having this blend weight.
	*/
	static public double blendWeight= 0.0; //0.1;

	public void setDerivativeFunction(QuaternionNumericalDerivative deriv_func)
	{
		deriv= deriv_func;
	}

	public void clearState()
	{
		last_committed_value.setIdentity();
		current_value.setIdentity();
		last_commit_time= 0.0;
		deriv.init();
	}
}
