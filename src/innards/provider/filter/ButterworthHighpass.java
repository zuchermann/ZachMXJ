package innards.provider.filter;

public class ButterworthHighpass extends BaseButterworth
{

	float alpha;
	float beta;
	float gamma;

	public ButterworthHighpass(float centerFrequency, float sampleRate, float d)
	{
		design(centerFrequency, sampleRate, d);
	}
	public void design(float centerFrequency, float sampleRate, float d)
	{
		float theta=(float)( 2.0 * Math.PI * centerFrequency / sampleRate);

		beta= (float)(0.5 * ((1 - d / (2 * Math.sin(theta))) / (1 + d / (2 * Math.sin(theta)))));
		gamma= (float)((0.5 + beta) * Math.cos(theta));
		alpha=(float)( (0.5 + beta + gamma) / 4);

		beta *= 2;
		gamma *= 2;
		alpha *= 2;

	}

	public float filter(float input)
	{
		float newState= alpha * (input - 2 * inputMinus1 + inputMinus2) + gamma * state - beta * stateMinus1;
		swap(newState, input);
		return newState;
	}

}