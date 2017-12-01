package innards.math.map;

/**
 * Uses Roger Bagula's unit disk --> unit square mapping.
 * Maps r = [0..1], theta = [real] to x,y = [0..1]
 * http://www.geocities.com/ResearchTriangle/Thinktank/7279/fuzzy_banach.html
 *
 * Group:        Synthetic Characters Group, MIT Media Lab
 * @author mkg
 * @version 1.0
 */

public class DiskToSquareMap {

    public DiskToSquareMap() {
    }

	public static double[] polarDiskToCartesianSquare(double r, double theta){
	       double[] result = new double[2];
		   result[0] = r*Math.cos(theta)/ms(theta);
		   result[1] = r*Math.sin(theta)/ms(theta);
		   return result;
	}

	public static double[] cartesianSquareToPolarDisk(double x, double y){
		   double theta = Math.atan2(y,x);
		   double mst = ms(theta);
		   double r = x * mst / Math.cos(theta);
		   double[] result = {r, theta};
		   return result;
	}

	public static double[] cartesianDiskToCartesianSquare(double x, double y){
		   double r = Math.sqrt(x*x+y*y);

		   double theta = Math.atan2(y,x);
	       return polarDiskToCartesianSquare(r,theta);
	}

	private static double ms(double w){
			return Math.max( Math.abs(Math.sin(w)), Math.abs(Math.cos(w)) );
	}

	public static void main(String[] args){
		   int numAngs = 48;
		   double delTheta = 2*Math.PI/(numAngs-1);
		   double r = 1;
		   for(int i = 0; i < numAngs; ++i){
				   double theta = (double)i*delTheta;
				   int degs = (int)(theta/Math.PI*180d);
				   double[] xy = polarDiskToCartesianSquare(r,theta);
				   System.out.println("theta =\t"+degs+"\tbecomes\t"+xy[0]+",\t"+xy[1]);
			}
	}
}
