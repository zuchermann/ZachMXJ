package innards.provider.realtime;

import innards.*;
import innards.provider.iFloatProvider;

import java.util.*;

/**
    timestamped things come in, density is estimated
    */
public class MultiResolutionDensityEstimator
    implements iFloatProvider
{
	ClockSource source;
	double timeScale;
	int numScales;
	ArrayList theScales;
	
	public MultiResolutionDensityEstimator(ClockSource source, double timeScale, int numScales)
	{
		this.timeScale = timeScale;
		this.numScales = numScales;
		this.source = source;
		theScales = new ArrayList(numScales);
		
		for(int i=0;i<numScales;i++) theScales.add(new LinkedList());
	}
	
	double lastEventAt = -1;

	// gives an event here
	public void give()
	{
		double now = source.getTime();
		lastEventAt = now;
		// add an event to the scales list
		List top = (List)theScales.get(0);
		top.add(new Double(now));
	}
	
	protected void roll(double now)
	{
		List top = (List)theScales.get(0);
		// now push things backwards
		for(int i=0;i<theScales.size();i++)
		{
			for(int n=0;n<top.size();n++)
			{
				Double event = (Double)top.get(n);
				double at = event.doubleValue();
				if (at<now-timeScale*(i+1))
				{
					top.remove(0);
					n--;
					if (i<theScales.size()-1)
					{
						((List)theScales.get(i+1)).add(event);
					}
				}
				else
				{
					break;
				}
			}
			if (i<theScales.size()-1)
			{
				top = (List)theScales.get(i+1);
			}
		}
	}
	
	public float evaluate()
	{
		if (lastEventAt == -1) return 0;
		// first bin goes from lastEventAt-timeScale out to 't'
		double now = source.getTime();

		roll(now);
		
		double avg = 0;
		double totalWeight = 0;
		int tot = 0;
		for(int i=0;i<theScales.size();i++)
		{
			double length = now - (lastEventAt-timeScale*(i+1));
			tot+=((List)theScales.get(i)).size();
			
			//System.out.println(" bin:"+i+" has "+((List)theScales.get(i)).size());
			
			double densityHere = tot/length;
			double weight;
			
			totalWeight += weight = (theScales.size()+1)-i;
			avg += weight*densityHere;
		}
		avg /= totalWeight;
		
		return (float) avg;
	}
	
	static public void main(String[] s)
	{
		TickClockSource source = new TickClockSource();
		MultiResolutionDensityEstimator estimator = new MultiResolutionDensityEstimator(source, 20, 5);
		
		for(int i=0;i<1000;i++)
		{
			if (i<800) if (i%3==0) estimator.give();
			source.tick();
			System.out.println(estimator.evaluate());
		}
	}
	
}
