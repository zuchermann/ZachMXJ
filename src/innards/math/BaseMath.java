package innards.math;

import innards.provider.iFloatProvider;

import java.text.NumberFormat;

/**
   BaseMath is meant to be an extension of java.lang.Math to cover
   issues they have and also to add some functionality.
   Unfortunately, java.lang.Math is final, so I only add functions
   which I care about and people should use them where they matter.

   @author Michael Patrick Johnson <aries@media.mit.edu>
*/

public class BaseMath
{
	public final static float epsilon= 1e-8f;

	/**
	     Clamps the value to the specified range.
	   @param val the value to clamp
	   @param low the lower value that will replace val if val < low.
	   @param high the upper value that will replace val if val > high.
	   @return the clamped value in the range [low, high].
	*/
	public static final double clamp(double val, double low, double high)
	{
		if (val > high)
			return high;
		if (val < low)
			return low;
		return val;
	}

	/** 
	Input-checking inverse cosine. Clamps the input to
	the domain of acos (i.e. [-1..1]) before evaluating, instead of returning
	a silent NaN like java.lang.Math.acos. Useful when there's a fear of 
	rounding errors pushing the arg beyond 1 or -1.
	*/
	public static final double acos(double cos_theta)
	{
		return Math.acos(clamp(cos_theta, -1.0, 1.0));
	}

	/** 
	Input-checking inverse sine. Clamps the input to
	the domain of acos [-1..1] before evaluating, instead of returning
	a silent NaN like java.lang.Math.asin. Useful when there's a fear of 
	rounding errors pushing the arg beyond 1 or -1.
	*/
	public static final double asin(double sin_theta)
	{
		return Math.asin(clamp(sin_theta, -1.0, 1.0));
	}

	/**
	a sign safe version of pow, _always_ monotically increasing for positive pow
	*/
	static public double safePow(double x, double pow)
	{
		int sign= pow < 0 ? -1 : 1;
		double ret= Math.pow(x * sign, pow) * sign;
		return ret;
	}

	static final double sinc_lookup[]=
		{
			1.0000000000000000e+000,
			9.9932880203694100e-001,
			9.9731682984747660e-001,
			9.9396894386629840e-001,
			9.8929322928435600e-001,
			9.8330097279963240e-001,
			9.7600663017022280e-001,
			9.6742778467129840e-001,
			9.5758509658600000e-001,
			9.4650224388831540e-001,
			9.3420585430350560e-001,
			9.2072542895852920e-001,
			9.0609325786111200e-001,
			8.9034432747150920e-001,
			8.7351622065555020e-001,
			8.5564900933114440e-001,
			8.3678514014298590e-001,
			8.1696931352166510e-001,
			7.9624835650368560e-001,
			7.7467108970794610e-001,
			7.5228818888200670e-001,
			7.2915204144787160e-001,
			7.0531659849201880e-001,
			6.8083722265795170e-001,
			6.5577053241160130e-001,
			6.3017424316041650e-001,
			6.0410700571592420e-001,
			5.7762824259689140e-001,
			5.5079798267594950e-001,
			5.2367669467663940e-001,
			4.9632512003028100e-001,
			4.6880410560287790e-001,
			4.4117443680140560e-001,
			4.1349667156634410e-001,
			3.8583097575317760e-001,
			3.5823696039983620e-001,
			3.3077352136970930e-001,
			3.0349868185094890e-001,
			2.7646943818232700e-001,
			2.4974160946396760e-001,
			2.2336969139787360e-001,
			1.9740671478835150e-001,
			1.7190410911627600e-001,
			1.4691157158367040e-001,
			1.2247694200637710e-001,
			9.8646083912710340e-002,
			7.5462772185011250e-002,
			5.2968587559005830e-002,
			3.1202818272899560e-002,
			1.0202369134296560e-002,
			-9.9983217516106900e-003,
			-2.9367358374493620e-002,
			-4.7875454139870700e-002,
			-6.5495990953028560e-002,
			-8.2205069927258430e-002,
			-9.7981553605101640e-002,
			-1.1280709960888980e-001,
			-1.2666618566462450e-001,
			-1.3954612597107600e-001,
			-1.5143707891381250e-001,
			-1.6233204615157880e-001,
			-1.7222686312997410e-001,
			-1.8112018110459860e-001,
			-1.8901344078269100e-001,
			-1.9591083771866030e-001,
			-2.0181927962473900e-001,
			-2.0674833578317200e-001,
			-2.1071017877082060e-001,
			-2.1371951873072370e-001,
			-2.1579353044794820e-001,
			-2.1695177350889340e-001,
			-2.1721610584403640e-001,
			-2.1661059097383420e-001,
			-2.1516139929608260e-001,
			-2.1289670377041100e-001,
			-2.0984657037171240e-001,
			-2.0604284369911690e-001,
			-2.0151902814057680e-001,
			-1.9631016500519630e-001,
			-1.9045270604607760e-001,
			-1.8398438380563720e-001,
			-1.7694407922304120e-001,
			-1.6937168694961350e-001,
			-1.6130797882274430e-001,
			-1.5279446595199010e-001,
			-1.4387325987267890e-001,
			-1.3458693322243740e-001,
			-1.2497838039463620e-001,
			-1.1509067861981620e-001,
			-1.0496694992174230e-001,
			-9.4650224388831680e-002,
			-8.4183305194373640e-002,
			-7.3608635790207540e-002,
			-6.2968169688401420e-002,
			-5.2303243234022990e-002,
			-4.1654451759341330e-002,
			-3.1061529495821900e-002,
			-2.0563233605102720e-002,
			-1.0197232673846340e-002,
			-3.8980430910514780e-017 };

	private final static double SINC_EPSILON= .001;

	/** 
	Evaluates sinc(x). Defined as sin(x)/x. Remains valid as x -> 0. (Uses linear interpolation on a fine-grained lookup table.)
	*/
	public static final double sinc(double x)
	{
		double n= sinc_lookup.length;
		double dn= 1.0 / n;

		/* if far enough from zero, it's ok to calculate it */

		if (x <= -SINC_EPSILON || x >= SINC_EPSILON)
		{
			return Math.sin(x) / x;
		}

		if (x == 0.0)
			return 1.0;

		double ax= Math.abs(x);
		int left_idx= (int) ((ax / (2 * Math.PI)) * sinc_lookup.length);
		if (left_idx == sinc_lookup.length - 1)
			return sinc_lookup[sinc_lookup.length - 1];
		int right_idx= left_idx + 1;

		double left_val= left_idx * dn;
		double t= (ax / (2 * Math.PI) - left_val) / dn;
		return (1.0 - t) * sinc_lookup[left_idx] + t * sinc_lookup[right_idx];
	}

	/** 
	The tanh function. Defined as (exp(x)-exp(-x))/(exp(x)+exp(-x))
	@author marc
	*/
	public static final double tanh(double x)
	{

		if (x == Double.NEGATIVE_INFINITY)
			return -1;
		if (x == Double.POSITIVE_INFINITY)
			return 1;

		double temp= Math.exp(-2 * x);
		return (1 - temp) / (1 + temp);
	}

	/**
	   Reads input as a two's complement unsigned byte, returns the numerical value as an int.
	@author marc
	*/
	public static int intify(byte argh)
	{
		int i= (argh < 0 ? ((int) argh) + 256 : (int) argh);
		return i;
	}

	/**
	   Reads input as a two's complement unsigned int, returns the numerical value as a long.
	@author marc
	*/
	public static long longify(int argh)
	{
		long i= (argh < 0 ? ((long) argh) + (long) 4294967296L : (long) argh);
		return i;
	}

	/**
	     Returns the string with this number of decimal places (convenience function).
	@author marc
	  */
	static public String toDP(double number, int i)
	{
		if (_format == null)
		{
			_format= NumberFormat.getInstance();
		}
		if (_lastDigits != i)
		{
			_format.setMaximumFractionDigits(i);
			_format.setMinimumFractionDigits(i);
			_lastDigits= i;
		}

		return _format.format(number);
	}
	static private NumberFormat _format= null;
	static private int _lastDigits= -1;

	/**
	   Mutable version of java.lang.Float; especially useful for hashmaps.
	@author marc
	    */
	static public class MutableFloat extends Number implements iFloatProvider
	{
		public float d;
		public MutableFloat(float d)
		{
			this.d= d;
		}

		public byte byteValue()
		{
			return (byte) d;
		}
		public double doubleValue()
		{
			return d;
		}
		public float floatValue()
		{
			return (float) d;
		}
		public int intValue()
		{
			return (int) d;
		}
		public long longValue()
		{
			return (long) d;
		}
		public short shortValue()
		{
			return (short) d;
		}

		public String toString()
		{
			return "" + d;
		}

		public float evaluate()
		{
			return d;
		}
	}
	/**
	   Hyperbolic secant function. Defined as 1/cosh(x), or 1/(exp(-x)+exp(x)
	@author marc
	    */
	static public double sech(double x)
	{
		double temp= Math.exp(x);
		return temp / (temp * temp + 1);
	}
	/**
	   Returns sech(x)^2
	@author marc
	    */
	static public double sech2(double x)
	{
		double temp= Math.exp(2 * x);
		return temp / (1 + temp * temp + 2 * temp);
	}
	/** 
	 *Returns sqrt(a^2 + b^2) without under/overflow. 
	 **/
	public static double hypot(double a, double b)
	{
		double r;
		if (Math.abs(a) > Math.abs(b))
		{
			r= b / a;
			r= Math.abs(a) * Math.sqrt(1 + r * r);
		} else if (b != 0)
		{
			r= a / b;
			r= Math.abs(b) * Math.sqrt(1 + r * r);
		} else
		{
			r= 0.0;
		}
		return r;
	}

	public static boolean floatEqualTo(float a, float b)
	{
		return (Math.abs(a - b) <= epsilon);
	}
	
	/**
	 * returns error function 
	@author marc
	 */
	public static float erf(float x)
	{
		double t,z,ans;
		z=Math.abs(x);
		t=1.0/(1.0+0.5*z);
		ans=t*Math.exp(-z*z-1.26551223+t*(1.00002368+t*(0.37409196+t*(0.09678418+
		t*(-0.18628806+t*(0.27886807+t*(-1.13520398+t*(1.48851587+
		t*(-0.82215223+t*0.17087277)))))))));
		return (float)(1-(x >= 0.0 ? ans : 2.0-ans));
	}

}
