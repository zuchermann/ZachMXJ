package innards.math.linalg;

/** 
    Runtime exception used to indicate when the dimensions of vector or matrix objects
    are inappropriate for the operation being performed (such as when
    multiplying matrices whose dimensions don't match correctly, or trying to
    copy a larger vector into a smaller one). 
*/
public class DimensionMismatchException extends RuntimeException
{

    /**
       Messageless constructor.
    */
    public DimensionMismatchException()
    {
	super();
    }

    /**
       Constructor with message.
    */
    public DimensionMismatchException(String s)
    {
	super(s);
    }

	
	
}


