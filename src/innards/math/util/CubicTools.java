package innards.math.util;

import innards.math.linalg.*;

/**
   A class of static methods for performing cubic interpolation.
    */
public class CubicTools
{

	/**
	   Yields a new float array for each thread that calls tmp.get(). Like a thread-safe static variable. Should be private.
	*/
	static ThreadLocal tmp= new ThreadLocal()
	{
		protected Object initialValue()
		{
			return new float[4];
		}
	};

	/**
	   Performs cubic interpolation of a scalar function given four local samples. 
	   @param alpha The blending parameter. <code>alpha = 0</code> yields <code>now</code>, <code>alpha = 1</code> yields <code>next</code>.
	   @param before The first in a sequence of four local samples.
	   @param now The second local sample.
	   @param next The third local sample.
	   @param after The fourth local sample.
	*/
	static public float cubic(float alpha, float before, float now, float next, float after)
	{
		float[] tmp= (float[]) CubicTools.tmp.get();
		H(alpha, tmp);
		float t1= (next - before) / 2;
		float t2= (after - now) / 2;
		return tmp[0] * now + tmp[1] * next + tmp[2] * t1 + tmp[3] * t2;
	}

	/**
	   Performs cubic interpolation of a 3-dimensional function given four local samples.
	*/
	static public void cubic(float alpha, Vec3 before, Vec3 now, Vec3 next, Vec3 after, Vec3 out)
	{
		out.set(0, cubic(alpha, before.get(0), now.get(0), next.get(0), after.get(0)));
		out.set(1, cubic(alpha, before.get(1), now.get(1), next.get(1), after.get(1)));
		out.set(2, cubic(alpha, before.get(2), now.get(2), next.get(2), after.get(2)));
	}

	/**
	   Calculates the interpolation coefficient for the first sample.
	*/
	static public float H0(float alpha)
	{
		float alpha2= alpha * alpha;
		float alpha3= alpha * alpha2;
		return 2 * alpha3 - 3 * alpha2 + 1;
	}

	/**
	   Calculates the interpolation coefficient for the second sample.
	*/
	static public float H1(float alpha)
	{
		float alpha2= alpha * alpha;
		float alpha3= alpha * alpha2;
		return -2 * alpha3 + 3 * alpha2;
	}

	/**
	   Calculates the interpolation coefficient for the third sample.
	*/
	static public float H2(float alpha)
	{
		float alpha2= alpha * alpha;
		float alpha3= alpha * alpha2;
		return alpha3 - 2 * alpha2 + alpha;
	}

	/**
	   Calculates the interpolation coefficient for the fourth sample.
	*/
	static public float H3(float alpha)
	{
		float alpha2= alpha * alpha;
		float alpha3= alpha * alpha2;
		return alpha3 - alpha2;
	}

	/**
	   Calculates the interpolation coefficients for the four samples. The cubic-interpolated value for a given alpha is given by:<br>
	   <code>out[0]*1st_sample + out[1]*2nd_sample + out[2]*3rd_sample + out[3]*4th_sample</code> 
	*/
	static public void H(float alpha, float[] out)
	{
		float alpha2= alpha * alpha;
		float alpha3= alpha * alpha2;
		out[0]= 2 * alpha3 - 3 * alpha2 + 1;
		out[1]= -2 * alpha3 + 3 * alpha2;
		out[2]= alpha3 - 2 * alpha2 + alpha;
		out[3]= alpha3 - alpha2;
	}

	/**
	   Test drive method.
	*/
	static public void main(String[] s)
	{
		for (int i= 0; i < 11; i++)
		{
			System.out.println(cubic(i / 10.0f, 2, 1, 2, 1));
		}
	}
}
