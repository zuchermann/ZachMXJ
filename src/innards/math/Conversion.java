package innards.math;

import innards.math.linalg.Vec3;

/**
 * Just wanted a place to dump any conversion constants and never think about them ever again.
 * 
 * @author cchao
 *
 */
public class Conversion {

	public static final double METERS_PER_INCH = 0.0254; 
	public static final double INCHES_PER_METER = 1.0/METERS_PER_INCH;
	public static final double RADIANS_PER_DEGREE = Math.PI/180.0;
	public static final double DEGREES_PER_RADIAN = 180.0/Math.PI;
	
	public static float inchesToMeters(double inches) {
		return (float)(METERS_PER_INCH*inches);
	}
	
	public static Vec3 inchesToMeters(Vec3 inches) {
		return inches.scale((float)METERS_PER_INCH);
	}
	
	public static float metersToInches(double meters) {
		return (float)(INCHES_PER_METER*meters);
	}
	
	public static Vec3 metersToInches(Vec3 meters) {
		return meters.scale((float)INCHES_PER_METER);
	}
	
	public static float cmToInches(double cm) {
		return metersToInches(cm/100.0);
	}
	
	public static float inchesToCm(double inches) {
		return (float)(100.0*inchesToMeters(inches));
	}
	
	public static float degreesToRadians(double degrees) {
		return (float)(RADIANS_PER_DEGREE*degrees);
	}
	
	public static float radiansToDeg(double radians) {
		return (float)(DEGREES_PER_RADIAN*radians);
	}
	
}
