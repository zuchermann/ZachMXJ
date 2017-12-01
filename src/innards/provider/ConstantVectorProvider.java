package innards.provider;
import innards.math.linalg.*;

/**    
    @author marc
    */

public class ConstantVectorProvider implements iVectorProvider
{
	protected Vec me;
	public ConstantVectorProvider(Vec m)
	{
		me= new Vec(m);
	}
	public ConstantVectorProvider(Vec3 m)
	{
		me= new Vec(3);
		m.toVec(me);
	}
	public void get(Vec inplace)
	{
		inplace.setValue(me);
	}
	public Vec construct()
	{
		return new Vec(me.dim());
	}
}