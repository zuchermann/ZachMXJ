package innards.math.spline;
import java.lang.Math;
import java.lang.*;


/**
 One-dimensional Cardinal Spline interpolator.

 Does linear interpolation between first two and last two points.
 Originially written in C by Rob, using the equations found on page 325 in Hearn and Baker's book

 Not very optimized.

 Could easily be ported to a multidimensional version.

 Usage:

 double [] result = Spline.splineResample(points, outPoints);

 */

public class Spline {
	public static void main(String[] args) {
		double[] fish = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0};
		int outPoints = 200;
		double [] result = Spline.splineResample(fish, outPoints);

		System.out.println("result:");
		for (int i = 0; i < result.length; i++) {
			System.out.println("" + result[i]);
		}
	}

	double[] term = new double[4];
	public double evalCardinalSplinePoint(double p0, double p1, double p2, double p3, double u, double tension) {
		//assert(u >= 0);
		//assert(u <= 1);
		double pu;

		// Let's split this up to save our sanities.
		double s = (1-tension)/2;
		//double[] term = new double[4];
		term[0] = (((-1)*s*Math.pow(u,3)) + (2*s*Math.pow(u,2)) - (s*u));
		term[1] = (((2-s)*Math.pow(u,3)) + ((s-3)*Math.pow(u,2)) + 1);
		term[2] = (((s-2)*Math.pow(u,3)) + ((3-2*s)*Math.pow(u,2)) + (s*u));
		term[3] = ((s*Math.pow(u,3)) - (s*Math.pow(u,2)));

		pu = p0*term[0] + p1*term[1] + p2*term[2] + p3*term[3];

		return pu;
	}

	public double EvalCardinalSplineAt(double[] p, double atValue, double tension) {

		double floor = Math.floor(atValue);
		int i = (int)floor;
		double u = atValue - floor;
		i--;
		if (i < 0) {
			// linear interp on first two points
			return p[1] * u + p[0] * (1-u);
		}
		else if (i >= p.length - 3) {
			// linear interp on last two points
			if (u == 0) return p[p.length - 1]; // last point of series
			return p[p.length-1] * u + p[p.length - 2] * (1-u);
		}
		// cardinal spline the rest
		return evalCardinalSplinePoint(p[i], p[i+1], p[i+2], p[i+3], u, tension);

	}


	/*
	* Draws a Cardinal spline.
	*/
	double[] drawCardinalSpline(double[]p, int outpoints, double tension)
	{
		int np = p.length;
		double[]output = new double[outpoints];

		for (int i = 0; i < outpoints; i++) {
			double atValue = ((double)i / (double)(outpoints-1)) * np;
			output[i] = EvalCardinalSplineAt(p, atValue, tension);

		}
		int currentIndex = 0;

		return output;
	}

	public static double[] splineResample(double[] points, int outPoints) {
		return new Spline().drawCardinalSpline(points, outPoints, 1);
	}



}




