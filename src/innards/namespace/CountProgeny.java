package innards.namespace;

import innards.*;

/**
 * 	total number of things beath this in and including this in a hierarchy
 * @author marc
 *
 */
public class CountProgeny extends BaseTraversalAction
{
	int count;

	public Object applyAction(iNamedObject root)
	{
		count= 0;
		super.applyAction(root);
		return new Integer(count);
	}

	public boolean actionImplementation(iNamedObject no)
	{
		count++;
		return true;
	}

	public static int apply(iNamedGroup root)
	{
		CountProgeny prog= new CountProgeny();
		return ((Integer) (prog.applyAction(root))).intValue();
	}

}