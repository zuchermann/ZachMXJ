package innards.provider.filter;
import innards.NamedGroup;
import innards.provider.iFilter;

public abstract class BaseButterworth extends NamedGroup implements iFilter
{
	// butterworth requires last two inputs and last one state

	float state= 0;
	float stateMinus1= 0; // as in state at t - 1

	float inputMinus1= 0;
	float inputMinus2= 0;

	/** handles the buffers for the calculions */
	final protected void swap(float newState, float newSample)
	{
		stateMinus1= state;
		state= newState;

		inputMinus2= inputMinus1;
		inputMinus1= newSample;
	}

	final protected void equlibriate(float sample)
	{
		state= stateMinus1= inputMinus1= inputMinus2= sample;
	}
	
	abstract public float filter(float value);

}