package innards.provider.filter;
import java.io.*;

public class ButterworthLowpass extends BaseButterworth
{

	float alpha;
	float beta;
	float gamma;

	public ButterworthLowpass(float centerFrequency, float sampleRate, float d)
	{
		design(centerFrequency, sampleRate, d);
	}

	public void design(float centerFrequency, float sampleRate, float d)
	{
		float theta= (float)(2.0 * Math.PI * centerFrequency / sampleRate);

		beta=(float)( 0.5 * ((1 - d / (2 * Math.sin(theta))) / (1 + d / (2 * Math.sin(theta)))));
		gamma= (float)((0.5 + beta) * Math.cos(theta));
		alpha=(float)( (0.5 + beta - gamma) / 4);

		beta *= 2;
		gamma *= 2;
		alpha *= 2;

	}

	public float filter(float input)
	{
		float newState= alpha * (input + 2 * inputMinus1 + inputMinus2) + gamma * state - beta * stateMinus1;
		swap(newState, input);
		return newState;
	}

	static public void main(String[] a)
	{
		try
		{
			PrintWriter out= new PrintWriter(new FileOutputStream(new File("/users/marc/Documents/response.txt")));

			ButterworthBandpass pass= new ButterworthBandpass(5000, 44100, 50);
			ButterworthBandReject reg= new ButterworthBandReject(5000, 44100, 50);

			ButterworthLowpass low= new ButterworthLowpass(100, 44100, 0.01f);
			ButterworthHighpass high= new ButterworthHighpass(100, 44100, 0.01f);

			for (int c= 1; c < 400; c++)
			{
				float a1= 0;
				float a2= 0;
				float a3= 0;
				float a4= 0;
				float aa= 0;

				for (int i= 0; i < 44100; i++)
				{
					float t= (float)i / 44100.0f;
					float s= (float)Math.sin(2 * Math.PI * t * c * 100);

					//s = 1;

					a1 += Math.pow(pass.filter(s), 2);
					a2 += Math.pow(reg.filter(s), 2);
					a3 += Math.pow(low.filter(s), 2);
					a4 += Math.pow(high.filter(s), 2);

					aa += s * s;
				}

				a1= (float)Math.sqrt(a1 / aa);
				a2=(float) Math.sqrt(a2 / aa);
				a3= (float)Math.sqrt(a3 / aa);
				a4= (float)Math.sqrt(a4 / aa);

				System.out.println(" c = " + (c * 100) + " \t\t pass = " + a1 + " \t\t reg = " + a2 + " \t\t low = " + a3 + " \t\t high = " + a4);
				out.println(a1 + " " + a2 + " " + a3 + " " + a4);
			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

}