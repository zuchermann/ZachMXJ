package innards.math;

import innards.math.linalg.Vec;

/**
 * @author mattb
 */
public interface iProbabilityDistribution
{
	/**
	 * returns the density of the distribution at the specified point
	 */
	public double evaluate(Vec point);
}
