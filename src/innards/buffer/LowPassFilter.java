package innards.buffer;

import innards.debug.Debug;
import innards.math.linalg.*;
import innards.provider.iFloatProvider;

/**
 * A simple LowPassFilter that uses a filter constant to blend previous values
 * with incoming values. because we often want to filter Vec3s etc. using the
 * same LowPassFilter for all the floats contained, this filter has an array of
 * values and applies the same filter equation and constant to blend each of
 * them with their previous values. You must set the number of items you wish to
 * filter in the constructor - it cannot be varied after the construction of the
 * filter. You insert values using the various version of<code> put()
 * </code>which returns the filtered result.
 * 
 * This filter does not take elapsed time since the last time put() was called
 * in to account. If you want a filter that interpolates between calls, use 
 * <code> TimeInvariantLowPassFilter </code>
 * 
 * @see innards.buffer.TimeInvariantLowPassFilter
 */
public class LowPassFilter implements iFloatProvider
{
	protected float alpha;
	protected float beta;
	
	/**
	 * the previously filtered values (these are the values that will be blended
	 * with)
	 */
	protected float[] values;
	/** 
	 * the previously passed in values
	 */
	protected float[] lastPassed;
	protected boolean initialized;

	/**
	 * Constructor for LowPassFilter.
	 * @param alpha  filter constant for increasing values
	 * @param beta  filter constant for decreasing values
	 * @param size an int representing the number of floats to be filtered (for
	 * instance, if you'll be calling the Vec3 version of <code> put() </code>
	 * you'd provide a size of 3) 
	 */
	public LowPassFilter(float alpha, float beta, int size)
	{
		super();
		this.alpha = alpha;
		this.beta = beta;
		initialized = false;
		values = new float[size];
		lastPassed = new float[size];
	}
	
	/**
	 * put takes in an array of value, filters them, and returns the filtered
	 * result. 
	 * 
	 */
	public float[] put(float[] newVals)
	{
		Debug.doAssert(newVals.length == values.length, "low pass filter given an array of new values to filter of a different length than current array of values");
		
		for (int i = 0; i < values.length; i++)
		{
			lastPassed[i] = newVals[i];
		}
		
		filter(newVals);
		for (int i = 0; i < values.length; i++)
		{
			newVals[i] = values[i];
		}
		return newVals;
	}
	
	/**
	 * convenience version of put that takes and returns a Vec.
	 * 
	 */
	public Vec put(Vec vec) 
	{
		float[] newVals = vec.toArray();
		newVals = put(newVals);
		return new Vec(newVals);		
	}
	/**
		 * convenience version of put that takes and returns a Vec3.
		 * 
		 */
	public Vec3 put(Vec3 vec3)
	{
		float[] newVals = vec3.getValue();
		newVals = put(newVals);
		return new Vec3(newVals);
		
	}
	/**
	* convenience version of put that takes and returns a float.
	 * 
	 */
	public float put(float f)
	{
		float[] newVal = new float[1];
		newVal[1] = f;
		newVal = put(newVal);
		return newVal[1];
	}
	
	/**
	 * does the actual filtering
	 * 
	 */
	protected void filter(float[] newVals) 
	{        
		if (! initialized)
		{
			initialized = true;
		}
		
		else
		{
			for (int i = 0; i < values.length; i++)
			{
				float oldVal = values[i];
				float newVal = newVals[i];
				float deltaVal = newVal - oldVal;
				
				
				//if the value's the same, you don't need to blend
				if (deltaVal == 0)
				{
					return;
				}
				
				float result;
				
				//if value is increasing, use alpha as the filter constant
				if (deltaVal > 0 )
				{
					result = calculateResult(alpha, oldVal, newVal, lastPassed[i]);
				}
				
				//otherwise, use beta
				else 
				{
					result = calculateResult(beta, oldVal, newVal, lastPassed[i]);
				}
				
				values[i] = result;
			}
		}
	}
	
	/**
	 * 
	 * This method should be overridden in order to take things like the size of
	 * a timestep and the amount of time that has passed since the last time
	 * filter was called into account. here, it just returns the value it was
	 * passed.
	 * 
	 * @see innards.buffer.timeInvariantLowPassFilter
	 */
	protected float calculateResult(float filterConst, float oldVal, float newVal, float lastPassed)
	{
		float retVal = oldVal * filterConst + (newVal * (1.0f - filterConst));
		return retVal;
		
	}
	
	/**
	 * resets the filter so that the previous values to be blended with are
	 * cleared
	 */
	public void reset() 
	{
		initialized = false;		
	}
	
	/**
	 * allows the filter to be an iFloatProvider. returns the last value for the
	 * first item in the values array (you should probably only be using this if
	 * you have an array of length 1)
	 * @see innards.provider.iFloatProvider#evaluate()
	 */
	public float evaluate()
	{
		return values[1];
	}

}
