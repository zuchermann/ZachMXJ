package innards.provider.filter;

public class ButterworthBandpass extends BaseButterworth
{

	float alpha;
	float beta;
	float gamma;
	float q;
	
	public ButterworthBandpass(float centerFrequency, float sampleRate, float Q)
	{
		design(centerFrequency, sampleRate, Q);
	}

	public void design(float centerFrequency, float sampleRate, float Q)
	{
		float theta= (float)(2.0 * Math.PI * centerFrequency / sampleRate);

		beta= (float)(0.5 * ((1 - Math.tan(theta / (2 * Q))) / (1 + Math.tan(theta / (2 * Q)))));
		alpha= (float)((0.5 - beta) / 2);
		gamma= (float)((0.5 + beta) * Math.cos(theta));

		beta *= 2;
		gamma *= 2;
		alpha *= 2;

		this.q = Q;

	}

	public float filter(float input)
	{
		float newState= alpha * (input - inputMinus2) + gamma * state - beta * stateMinus1;
		swap(newState, input);
		return newState;
	}

}