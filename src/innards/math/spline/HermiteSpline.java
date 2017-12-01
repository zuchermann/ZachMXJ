package innards.math.spline;

/**
   N-dimensional Kochanek-Bartels spline. An interpolating spline, with specifiable curve
   characteristics at each sample point. Can be reduced to a Catmull-Rom spline.<nl>
   For details, see Eberly, "3D Game Design," sx 7.3.4<P>
   
   Usage:<P>
   
   eval(double u) returns the coordinates of the point at spline parameter u, where
   u is the spline parameter that varies from 0 at the beginning of the spline to 1
   at the end.<P>
   
   The constructor permits specification of the u values of the supplied
   control points (samples), allowing control over the speed of progression along the spline.
   It also allows for specification of the sharpness of the bend at a control point
   through the <b>tension</b> parameter. Zero tension results in a Catmull-Rom spline.
   The <b>continuity</b> parameter, when zero, enforces second-order derivative continuity.
   When not zero, it introduces an angle at that control point, up to 90 degrees on either side of the spline, depending on its value from -1 to 1.
   The <b>bias</b> parameter biases the tangent at its sample to point more towards
   the previous sample, or more towards the next sample, depending on its
   value from -1 to 1.<P>
   
   Company:      Synthetic Characters Group, MIT Media Lab
   @author       mkg
   @version 1.0
*/

import java.util.Arrays;

public class HermiteSpline {

    private boolean isLoop_;

    // Storage is 3.5 * size of sample array
    private double[][] points_;
    private double[][] incomingTangents_;
    private double[][] outgoingTangents_;
    private double[] pointParams_;

    // offset between point array index and tangent array indices;
    private int iTanIndexOffset_;
    private int oTanIndexOffset_;

    /**
      Constructs a Catmull-Rom spline, a.k.a. a Kochanek-Bartels spline with zero tension, first-derivative continuity, and no slope bias.
     */
    public HermiteSpline(double[][] points){
	this(points, null, null, null, null);
    }

	public void diffTest(){}

    /**
      Constructor.
      @param points Sample points. The first array index specifies which point; the second specifies which dimension.
      @param pointParams The spline parameter at each sample. Must be monotonically increasing from 0 to 1. If null, equal spacings will be used.
      @param tensions The tension values for each point. If null, zero will be used for all samples.
      @param continuities The continuity values for each point. If null, zero will be used for all samples, enforcing 2nd order continuity.
      @param biases The bias values for each point. If null, zero will be used for all samples.
     */
    public HermiteSpline(double[][] points, double[] pointParams, double[] tensions, double[] continuities, double[] biases) {

	/* ------ Start fascistic input check ----- */

	// fill in defaults for any nulls, check legality of non-null args

	// PointParams:

	// don't check null
	if( pointParams != null ){
	    // check length
	    if( pointParams.length != points.length ){
		throw new IllegalArgumentException( "pointParams.length ("+pointParams.length+") doesn't match points.length ("+points.length+")");
	    }
	    // make sure that pointParams are monotonically increasing from 0 to 1
	    else {

		if( pointParams[0] != 0 ){
		    throw new IllegalArgumentException( "pointParams must start at 0" );
		}
		for( int i = 1; i < pointParams.length; ++i){
		    if( pointParams[i-1] >= pointParams[i] ){
			throw new IllegalArgumentException( "pointParams must be monotonically increasing.\t (pointParams["+(i-1)+"] = "+pointParams[i-1]+", pointParams["+i+"] = "+pointParams[i]+")");
		    }

		    if( pointParams[i] > 1 ){
			throw new IllegalArgumentException( "pointParams can't be greater than 1" );
		    }
		}
	    }
	}

	// Tensions:
	// check null
	if( tensions == null ){
	    tensions = new double[points.length];
	}
	// check length
	else if( tensions.length != points.length ){
	    throw new IllegalArgumentException( "tensions.length ("+tensions.length+") doesn't match points.length ("+points.length+")");
	}
	// check range
	else {
	    for( int i = 0; i < points.length; ++i ){
		if( tensions[i] < -1 || tensions[i] > 1 ){
		    throw new IllegalArgumentException( "tensions["+i+"] must be between -1 and 1. ("+tensions[i]+")");
		}
	    }
	}

	// Continuities:
	// check null
	if( continuities == null ){
	    continuities = new double[points.length];
	}
	// check length
	else if( continuities.length != points.length ){
	    throw new IllegalArgumentException( "continuities.length ("+continuities.length+") doesn't match points.length ("+points.length+")");
	}
	// check range
	for( int i = 0; i < points.length; ++i ){
	    if( continuities[i] < -1 || continuities[i] > 1 ){
		throw new IllegalArgumentException( "continuities["+i+"] must be between -1 and 1. ("+continuities[i]+")");
	    }
	}

	// Biases:
	// check null
	if( biases == null ){
	    biases = new double[points.length];
	}
	// check length
	else if( biases.length != points.length ){
	    throw new IllegalArgumentException( "biases.length ("+biases.length+") doesn't match points.length ("+points.length+")");
	}
	// check range
	for( int i = 0; i < points.length; ++i ){
	    if( biases[i] < -1 || biases[i] > 1 ){
		throw new IllegalArgumentException( "biases["+i+"] must be between -1 and 1. ("+biases[i]+")");
	    }
	}



	/* ----- End fascistic input check ----- */



	points_ = points;
	pointParams_ = pointParams;
	incomingTangents_ = new double[numPoints()-1][numDimensions()];
	outgoingTangents_ = new double[numPoints()-1][numDimensions()];

	int numPoints = numPoints();
	int numDims = numDimensions();

	// make outgoing tangents

	// It's correct to assume that the first and last tangents are scaled by 1
	outgoingTangents_[0] = getDiff( points[1], points[0] );

	// Non-endpoint outgoing tangents
	for(int i = 1; i < numPoints-1; ++i){

	    double[] outTan1 = getDiff(points[i+1], points_[i]);
	    double c1 = (1-tensions[i]) * (1-continuities[i]) * (1-biases[i]) / 2;
	    scaleArr(outTan1 , c1);
	    //System.out.println("OutTan c1 "+i+" = \t"+c1);

	    double[] outTan2 = getDiff(points_[i], points[i-1]);
	    double c2 = (1-tensions[i]) * (1+continuities[i]) * (1+biases[i]) / 2;
	    scaleArr(outTan2, c2);
	    //System.out.println("OutTan c2 "+i+" = \t"+c2);

	    outgoingTangents_[i] = getSum(outTan1, outTan2);

	    if( pointParams_ != null ){
		double delT = pointParams[i+1] - pointParams[i];
		double delTprev = pointParams[i] - pointParams[i-1];
		double tSkew = 2*delT/(delTprev+delT);
		scaleArr(outgoingTangents_[i], tSkew);
	    }
	}

	// make incoming tangents

	// First/last incoming tangents. Note that the indexes of incomingTangents_ are shifted by -1
	// from the indexes of the points they're associated with.
	//incomingTangents_[0] = getDiff(points[1], points[0]);
	incomingTangents_[numPoints-2] = getDiff(points_[numPoints-1], points_[numPoints-2]);

	// Non-endpoint incoming tangents
	for(int i = 1; i < numPoints-1; ++i){
	    double[] inTan1 = getDiff(points[i+1], points_[i]);
	    double c1 = (1-tensions[i]) * (1+continuities[i]) * (1-biases[i]) / 2;
	    scaleArr(inTan1, c1);

	    double[] inTan2 = getDiff(points[i], points[i-1]);
	    double c2 = (1-tensions[i]) * (1-continuities[i]) * (1+biases[i]) / 2;
	    scaleArr(inTan2, c2);

	    incomingTangents_[i-1] = getSum(inTan1, inTan2);

	    if( pointParams_ != null ){
		double delT = pointParams[i+1] - pointParams[i];
		double delTprev = pointParams[i] - pointParams[i-1];
		double tSkew = 2*delTprev/(delTprev+delT);
		scaleArr(incomingTangents_[i-1], tSkew);
	    }
	}

    }

    /**
       Returns the point on the spline corresponding to spline parameter <code>u</code>.
       @param u The spline parameter (between 0 and 1 inclusive).
       @return The vector value of the spline at <code>u</code>, returned as an array.
     */
    public double[] eval(double u){
	if( u < 0 || u > 1 ){
	    throw new IllegalArgumentException("u wasn't between 0 and 1. ("+ u +")");
	}

	// prevTindex = the index of the control point before or at t
	int prevUindex;
	// intervalT = the t, normalized between the previous sample's t and the upcoming sample's t
	double intervalU;

	if( pointParams_ != null ){
	    prevUindex = prevUindex(u);

	    if( prevUindex == (points_.length-1) ){
		double[] lastPoint = new double[numDimensions()];
		System.arraycopy(points_[numPoints()-1], 0, lastPoint, 0, numDimensions());
		return lastPoint;
	    }

	    int nextUindex = prevUindex+1;
	    double prevU = pointParams_[prevUindex];
	    double nextU = pointParams_[nextUindex];
	    intervalU = (u-prevU)/(nextU-prevU);
	}
	else {
	    double uScaledMax = (double)(numPoints() - 1);
	    double uScaled = u*uScaledMax;
	    double uScaledPrev = Math.floor(uScaled);
	    double uScaledNext = uScaledPrev+1;

	    prevUindex = (int)uScaledPrev;

	    // if ti == last index, return last point
	    if( prevUindex == (points_.length-1) ){
		double[] result = new double[numDimensions()];
		System.arraycopy(points_[numPoints()-1], 0, result, 0, numDimensions());
		return result;
	    }

	    intervalU = (uScaled-uScaledPrev) / (uScaledNext-uScaledPrev);
	}
	//   System.out.println("t:\t"+t+"\tprevUindex:\t"+prevUindex+"\tprevU:\t"+prevU+"\tnextU:\t"+nextU+"\tintervalT:\t"+intervalT);

	double u2 = Math.pow(intervalU, 2);
	double u3 = Math.pow(intervalU, 3);

	double h0 = 2*u3 - 3*u2 + 1;
	double h1 = -2*u3 + 3*u2;
	double h2 = u3 - 2*u2 + intervalU;
	double h3 = u3 - u2;

	double[] term0 = getProd(points_[prevUindex], h0);
	double[] term1 = getProd(points_[prevUindex+1], h1);
	double[] term2 = getProd(getOutTan(prevUindex), h2);
	double[] term3 = getProd(getInTan(prevUindex+1), h3);

	addArr(term0, term1);
	addArr(term0, term2);
	addArr(term0, term3);
	return term0;
    }

    /**
       Evaluates points on the spline at equal spline parameter intervals.
       @param numPoints The number of points to be evaluated.
       @return An array of spline points. Each point is itself a one-dimensional array.
    */
    public double[][] evalMultiple(int numPoints){
	double[] uArr = new double[numPoints];
	double delU = 1.0/(numPoints-1);

	for(int i = 0; i < numPoints-1; ++i){
	    uArr[i] = i*delU;
	}
	uArr[numPoints-1] = 1;
	return evalMultiple(uArr);
    }

    /**
       Evaluates points on the spline at the specified spline parameter values.
       @param spParams An array of spline parameters.
       @return An array of spline points corresponding to the parameters in <code>spParams</code>.
     */
    public double[][] evalMultiple(double[] spParams){
	double[][] result = new double[spParams.length][];
	for(int i = 0; i < spParams.length; ++i){
	    //System.out.print("i:\t"+i);
	    result[i] = eval(spParams[i]);
	}
	return result;
    }

    /**
       Returns the number of dimensions of this spline.
     */
    public int numDimensions(){
	return points_[0].length;
    }

    /**
     * Returns the number of sample (control) points.
     */
    public int numPoints(){
	return points_.length;
    }


    /* ----- private ----- */

    private double[] getOutTan( int i ){
	return outgoingTangents_[i];
    }

    private double[] getInTan( int i ){
	return incomingTangents_[i-1];
    }

    private int prevUindex( double u ){
	int searchI = Arrays.binarySearch(pointParams_, u);
	if( searchI >= 0 ){
	    return searchI;
	}

	return -(searchI+2);
    }

    private double[] getProd(double[] arr, double scale){
	double[] result = new double[arr.length];
	System.arraycopy(arr, 0, result, 0, arr.length);
	scaleArr(result, scale);
	return result;
    }

    private void scaleArr(double[] arr, double scale){
	for(int i = 0; i < arr.length; ++i){
	    arr[i] *= scale;
	}
    }

    private double[] getSum( double[] arr1, double[] arr2 ){
	double[] result = new double[arr1.length];
	System.arraycopy(arr1, 0, result, 0, arr1.length);
	addArr(result, arr2);
	return result;
    }

    private void addArr( double[] arr1, double[] arr2 ){
	for(int i = 0; i < arr1.length; ++i){
	    arr1[i] += arr2[i];
	}
    }

    private double[] getDiff( double[] arr1, double[] arr2 ){
	double[] result = new double[arr1.length];
	System.arraycopy(arr1, 0, result, 0, arr1.length);
	subArr(result, arr2);
	return result;
    }

    private void subArr( double[] arr1, double[] arr2 ){
	for(int i = 0; i < arr1.length; ++i){
	    arr1[i] -= arr2[i];
	}
    }
}
