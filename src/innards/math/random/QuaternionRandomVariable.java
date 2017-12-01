package innards.math.random;

import innards.math.linalg.Quaternion;

import java.io.Serializable;

/**

   An interface for creating random variables on the Quaternion
   hypersphere in R^4 which draw from various distributions.  Each
   subclass should implement a different distribution.
  
  @author Michael Patrick Johnson <aries@media.mit.edu>

*/

public interface QuaternionRandomVariable extends Serializable
{

	/** sample from the variable into q, garbage free */
	public void sample(Quaternion q);

	/**
	   Makes a new one quaternion and rteturns it.
	*/
	public Quaternion sample();

}
