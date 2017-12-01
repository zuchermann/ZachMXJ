package innards.provider.realtime;

import innards.*;
import innards.provider.iFloatProvider;

/**
    timestamped things come in, density is estimated, rather badly
    */
public class DensityEstimator
    implements iFloatProvider
{
	double filterConstant;
	ClockSource source;
	
	public DensityEstimator(ClockSource source, double filterConstant)
	{
		this.filterConstant = filterConstant;
		this.source = source;
	}
	
	double lastEventAt;
	boolean hasEvent = false;
	boolean hasTwoEvents = false;
	
	double averageDensity = 0;
	
	// gives an event here
	public void give()
	{
		if (hasEvent && hasTwoEvents)
		{
			double now = source.getTime();
			double diff = now-lastEventAt;
			averageDensity = filterConstant*averageDensity + (1-averageDensity)*diff;
		}
		else if (!hasEvent && !hasTwoEvents)
		{
			lastEventAt = source.getTime();
			hasEvent = true;
			hasTwoEvents = false;
		}
		else if (hasEvent && !hasTwoEvents)
		{
			double now = source.getTime();
			double diff = now-lastEventAt;
			averageDensity = diff;
			hasEvent = true;
			hasTwoEvents = true;
		}
	}
	
	public float evaluate()
	{
		return (float) averageDensity;
	}
	
	
}