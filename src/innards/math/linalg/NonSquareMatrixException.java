package innards.math.linalg;

/**
   Thrown by methods expecting a square matrix.
*/
public class NonSquareMatrixException extends Exception
{

    /**
       Constructor without message.
    */
    public NonSquareMatrixException()
    {
	super();
  		
		
    }

    /**
       Constructor with message.
    */
    public NonSquareMatrixException(String s)
    {
	super(s);
    }

	
	
}
