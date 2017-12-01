package innards.provider;

import java.io.Serializable;

import innards.math.linalg.Vec3;

/**
 * Created on Sep 16, 2003
 * @author daphna
 */
public class ConstantVec3Provider implements iVec3Provider, Serializable
{
	/**
	 * 
	 */
	protected Vec3 val;
	
	public ConstantVec3Provider(Vec3 val)
	{
		super();
		this.val = val;
	}

	public Vec3 evaluate()
	{
		return val;
	}
}
