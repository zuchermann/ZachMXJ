package innards.math.linalg;

/**
   Thrown when an eigenvector decomposition (e.g. <code>EigenStructure.jacobi()</code>)
   fails to converge.
*/
public class EigenStructureException extends Exception
{

	/**
	   Messageless constructor.
	*/
	public EigenStructureException(String s)
	{
		super(s);
	}

	/**
	   Constructor with message.
	*/
	public EigenStructureException()
	{
		super();
	}

}
