package innards.math.linalg;

/**
  Signals when an inplace multiply is attempted.
  @see Matrix
  */
public class InPlaceMatrixMultException extends RuntimeException
{
    /** 
	Constructor without message.
    */
  public InPlaceMatrixMultException()
    {
      super();
    		
		
}

    /**
       Constructor with message.
    */
  public InPlaceMatrixMultException(String s)
    {
      super(s);
    }
	
	
}

