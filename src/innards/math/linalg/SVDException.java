/**

   Exception class for SVD routines.
   
   @author Michael Patrick Johnson <aries@media.mit.edu>
   @see SVD
 */

package innards.math.linalg;

public class SVDException extends Exception
{
  
    /** 
	Constructor with message.
    */
  public SVDException(String s)
  {
    super(s);
  }
  
    /**
       Constructor without message.
    */
  public SVDException()
  {
    super();
  }

}

