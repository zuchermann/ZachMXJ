package innards.provider.realtime;

import innards.*;


/** good for testing things
*/
public class TickClockSource
	implements ClockSource, iUpdateable
	
{
	double t;
	
	public TickClockSource()
	{
	}
	
	public void tick(){t++;}
	
	public void update(){this.t = t;}

	public double getTime()
	{
		return t;
	}
}