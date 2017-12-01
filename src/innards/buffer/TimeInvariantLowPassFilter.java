package innards.buffer;

import innards.iClock_Keys;
import innards.debug.Debug;
import innards.math.BaseMath;
import innards.namespace.context.CTree;

/**
 * This class is similar to LowPassFilter, but it filters as if it has been
 * called on every tick, whether or not this is actually the case. You should
 * use this filter when you want something that interpolates between calls.
 * 
 * @see innards.buffer.LowPassFilter
 * @author synchar
 *
 */
public class TimeInvariantLowPassFilter extends LowPassFilter implements iClock_Keys.Write
{
	float prevTime;
	
	
	public TimeInvariantLowPassFilter(float alpha, float beta, int size)
	{
		super(alpha, beta, size);
	}
	
	/**
	 * overrides the method in LowPassFilter. This method blends each of the
	 * oldVal with newVal using filterConst as the blending constant, but takes
	 * how much time has elapsed since the last call into account.
	 * @param filterConst
	 * @param oldVal the previous value returned by the filter
	 * @param newVal the new destination value
	 * @param lastPassed the last value that was passed into the filter
	 * @return float the filtered result
	 * @see innards.buffer.LowPassFilter#calculateResult
	 */
	protected float calculateResult(float filterConst, float oldVal, float newVal, float lastPassed)
	{
		float retVal;
		
		//one timestep - usually a fraction of a second
		float timestep = CTree.getFloat(TIMESTEP_KEY);
		//time in seconds
		float time = CTree.getFloat(TIME_KEY);
		//change in time
		float deltaTime = time - prevTime;
		Debug.doAssert(deltaTime >= 0, "TimeInvariantLowPassFilter thinks time is going backwards!");
		prevTime -= time;
		
		//we're going to raise the filter to the time elapsed in seconds divided by the size of a timestep
		//(e.g if a timestep is 1/30 and 5 seconds have elapsed exponent = 5/1/30 = 5 * 30
		float exponent = (deltaTime/timestep);
		
		//if the destination hasn't changed since last time 
		if ((lastPassed - newVal) <= BaseMath.epsilon)
		{
			filterConst = (float) Math.pow(filterConst, exponent);
			retVal = oldVal * filterConst + (newVal * (1.0f - filterConst));		
		}
		
		else 
		{
			//assume we were going towards lastPassed until the last timestep
			float const1 = (float) Math.pow(filterConst, exponent - 1);
			retVal = oldVal * const1 + (lastPassed * (1.0f - const1));
			//we were going towards the new destination for the last timestep
			retVal += oldVal * filterConst + (newVal * (1.0f - filterConst));
		}
		return retVal;
	
	}

}
