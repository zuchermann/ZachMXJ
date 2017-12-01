package innards.math.random;

/** 
    Exception for estimations where singualr directions (zero
    variance) are discovered since this is often dangerous for
    sampling. 
    
    extends runtime since it is used all over and the person might want to
    ignore this.  I need to deal with it in the future a bit better.
    
    @author Michael Patrick Johnson <aries@media.mit.edu>
*/

public class SingularDataException extends RuntimeException
{
	public SingularDataException(String s)
	{
		super(s);
	}
}
