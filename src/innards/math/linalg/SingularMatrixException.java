package innards.math.linalg;

/**
   Thrown in cases where a singular matrix is inadmissible (e.g. when inverting a matrix).
*/
public class SingularMatrixException extends Exception
{

    /** 
	Constructor without message.
    */
  public SingularMatrixException()
  {
    super();
  		
		
}

    /**
       Constructor with message.
    */
  public SingularMatrixException(String s)
  {
    super(s);
  }

	
	
}
