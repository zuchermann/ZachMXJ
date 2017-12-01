/*
 * Created on Oct 17, 2003
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
public class ConstantIntProvider implements iIntProvider
{
	
	protected int i;
	
	public ConstantIntProvider(int i)
	{
		super();
		this.i = i;
	}
	
	public int evaluate()
	{
		return i;
	}

}
