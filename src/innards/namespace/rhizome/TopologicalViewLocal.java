package innards.namespace.rhizome;

import innards.namespace.context.XLocal;

/**
 * @author marc
 * Created on May 6, 2003
 */
public abstract class TopologicalViewLocal extends XLocal
{

	public TopologicalViewLocal(iProvidesContext view)
	{
		super(view);
	}
	
	public TopologicalViewLocal(final VirtualizedNamedGroup namedGroup)
	{
		this(new iProvidesContext()
		{
			public Object context()
			{
				return namedGroup.getView();
			}
		});
	}
	

	/**
	 * called to provide the default data for a new context
	 * @return Object
	 */
	abstract public Object newData(Object at, Object oldContext, Object oldData);


	/**
	 * is 'old' the same context as 'new'? used to see if we need to create new storage for something
	 * defaults to "=="
	 * @param old
	 * @param current
	 * @return boolean
	 */
	protected boolean sameContext(Object old, Object current)
	{
		return old == current;
	}
}
