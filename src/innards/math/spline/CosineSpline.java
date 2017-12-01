package innards.math.spline;
import java.lang.Math;


/**
 One-dimensional Cosine Spline Interpolation.

	Written by : gielniak

 */

public class CosineSpline {

	public static double evalCosineSplinePoint(double u_index, double start, double finish) {
		//assert(u >= 0);
		//assert(u <= 1);
		if(u_index>1 || u_index<0){
			System.out.println("you passed in bunk that is outside [0,1] to CosineSpline: answer may not be valid");
		}
		   double mu2;

		   mu2 = (1-Math.cos(u_index*Math.PI))/2;
		   return(start*(1-mu2)+finish*mu2);
	}

}