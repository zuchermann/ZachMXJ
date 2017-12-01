package innards.provider.filter;

public class ButterworthBandReject extends BaseButterworth
{

	float alpha;
	float beta;
	float gamma;
	float delta;

	public ButterworthBandReject(float centerFrequency, float sampleRate, float Q)
	{
		design(centerFrequency, sampleRate, Q);
	}

	public void design(float centerFrequency, float sampleRate, float Q)
	{
		float theta= (float)(2.0 * Math.PI * centerFrequency / sampleRate);

		beta= (float)(0.5 * ((1 - Math.tan(theta / (2 * Q))) / (1 + Math.tan(theta / (2 * Q)))));
		alpha= (float)(0.5 + beta);
		delta= (float)(2 * Math.cos(theta));
		gamma=(float)( (0.5 + beta) * delta / 2);

		beta *= 2;
		gamma *= 2;
		alpha *= 2;
	}

	public float filter(float input)
	{
		float newState= alpha * (input - delta * inputMinus1 + inputMinus2) + gamma * state - beta * stateMinus1;
		swap(newState, input);
		return newState;
	}

}