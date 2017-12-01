package innards.provider;

import innards.math.linalg.Vec;

/**    
    @author marc
    */

public interface iVectorProvider
{
	public void get(Vec inplace);

	/**
	    constructs a Vec with the correct properties (i.e. dimension and algebra, perhaps)
	    this class expects 'inplace' in .get(time,inplace) to be .getClass().isAssignableFrom( ) whatever
	    this returns
	    */
	public Vec construct();
}