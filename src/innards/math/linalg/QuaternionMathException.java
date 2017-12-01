package innards.math.linalg;

/**
   Represents a quaternion math error.
*/
public class QuaternionMathException extends RuntimeException
{

    /** 
	Constructor without message.
    */
  QuaternionMathException()
  {
    super();
  }

    /**
       Constructor with message.
    */
  QuaternionMathException(String s)
  {
    super(s);
  }

}
