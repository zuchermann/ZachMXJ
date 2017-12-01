package innards;

import innards.namespace.context.ContextTree;

/**
 * a Clock maintains and sets the time and timestep in the local context.<br>
 * the World should have one.
 * @author synchar
 */
public class Clock implements iUpdateable, iClock_Keys.Write
{	
	protected float time;
	protected float timestep;
	
	public Clock(float startTime, float timestep)
	{
		this.time = startTime;
		this.timestep = timestep;
		setTimeAndTimestep();
	}

	protected float getTime()
	{
		return time;
	}
	
	protected float getTimestep()
	{
		return timestep;
	}

	/**
	 * @see innards.iUpdateable#update()
	 */
	public void update()
	{
		time += timestep;
		setTimeAndTimestep();	
	}
	
	protected void setTimeAndTimestep()
	{
		ContextTree.set(TIME_KEY, getTime());
		ContextTree.set(TIMESTEP_KEY, getTimestep());
	}
}
