/*
 * Created on Jan 27, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package innards.provider;

/**
 * @author daphna
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ConstantObjectProvider implements iObjectProvider
{
	protected Object o;
	
	public ConstantObjectProvider(Object o)
	{
		this.o = o;
	}
	public Object evaluate()
	{
		return o;
	}

}
