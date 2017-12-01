package innards.provider;

/**    
    @author marc
    */

public class ConstantFloatProvider implements iFloatProvider
{
	private float constant;

	public ConstantFloatProvider(float c)
	{
		constant= c;
	}

	public float evaluate()
	{
		return constant;
	}

	public float setConstant(float f)
	{
		float o = constant;
		constant= f;
		return constant;
	}
}