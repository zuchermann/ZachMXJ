package innards.math.linalg;

/**
   An interface for classes that store a coordinate system transformation 
   (translation + rotation) with respect to a base coordinate system.<P>
   
   CoordinateFrames can be concatenated to produce hierarchical
   frames.
 
   @author Michael Patrick Johnson <aries@media.mit.edu> 

*/

public interface CoordinateFrame
{
	/** 
	Sets this coordinate system to align with the base frame. 
	*/
	public void setIdentity();

	/** 
	Returns the orientation of this frame with respect to it's base frame. 
	(safe to pass in null q);
	*/
	public Quaternion getOrientation(Quaternion q);

	/** 
	Returns the origin of this frame with respect to its base frame. 
	(safe to pass in null origin);
	*/
	public Vec3 getOrigin(Vec3 origin);

	/** 
	Sets the orientation of this frame with respect to its base frame. 
	*/
	public void setOrientation(Quaternion q);

	/** 
	Sets the orientation of this frame with respect to its base frame. 
	*/
	public void setOrigin(Vec3 origin);

	/**
	   Sets the rotation and orientation of this frame with respect to its base frame.
	 */
	public void setValue(Quaternion q, Vec3 origin);

	/**
	   Copies the transformation of another CoordinateFrame.
	*/
	public void setValue(CoordinateFrame frame);

	/** 
	<code>this = frame * this</code>
	*/
	public void concatLeft(CoordinateFrame frame);

	/** 
	<code>this = this * frame</code>
	*/
	public void concatRight(CoordinateFrame frame);

	/** 
		<code>out = frame * this</code>
	 */
	public void multLeft(CoordinateFrame frame, CoordinateFrame out);

	/** 
	<code>out = this * frame</code>
	*/
	public void multRight(CoordinateFrame frame, CoordinateFrame out);

	/** 
	Inverts <code>this</code>.
	*/
	public void invert();

	/** 
	Writes the inverse of this coordinate frame into the argument.
	*/
	public void getInverse(CoordinateFrame frame);

	/** 
	<code>p = this * p</code>
	@param p the point to transform. 
	*/
	public void transformPoint(Vec3 p);

	/** 
	<code> trans_p = this * p</code>
	@param p the point to transform.
	@param trans_p the transformed point.
	*/
	public void transformPoint(Vec3 p, Vec3 trans_p);

	/** 
		Transforms a directional (free) vector through this frame. Similar to
		<code>transformPoint(Vec)</code>, except that translation is ignored. 
		@param d the vector to transform.
	 */
	public void transformDirection(Vec3 d);

	/** 
	Transforms a directional (free) vector through this frame. 
	Similar to <code>transformPoint(Vec, Vec)</code>, except that 
	translation is ignored.
	@param d direction in previous frame to xform.
	@param trans_p the transformed direction stored here.
	*/
	public void transformDirection(Vec3 d, Vec3 trans_d);

	/**
	   Returns a string representation.
	*/
	public String toString();
}
