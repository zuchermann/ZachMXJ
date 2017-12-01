package innards.namespace.context;

import java.util.*;

/**
 * 
 * tools for reusing one instance accross multiple contexts. Think of this as a
 * ContextLocal as in ThreadLocal. It needs to be subclassed to be useful.
 * 
 * @author marc
 * 
 */
public abstract class ContextLocal extends XLocal
{

	protected Map knownContexts= new WeakHashMap();
	protected List knownContextList= new ArrayList();

	protected ContextTree.Bobj cacheCurrent= null;
	protected Object cacheCurrentData= null;

	public ContextLocal()
	{
		super(new iProvidesContext()
		{
			public Object context()
			{
				return ContextTree.where();
			}
		});
	}

	/**
	 * returns the data that is 'at' this level of the context
	 * @return Object
	 */
	public Object getData()
	{
		final Object o= super.getData();

		if (!knownContexts.containsKey(context.context()))
		{
			ContextTree.Trap trap;
			((ContextTree.Bobj) context.context()).addTrap(trap = new ContextTree.Trap()
			{
				public void begin()
				{
				}
	
				public boolean end()
				{
					return true;
				}
	
				public void delete()
				{
					deleteData(o);
				}
			});
			knownContexts.put(context.context(), trap);
		}
		return o;
	}

	/**
		 * called to provide the default data for a new context
		 * @return Object
		 */
	public Object newData(Object at, Object oldContext, Object oldData)
	{
		return newData( (ContextTree.Bobj)at, oldContext, oldData);
	}

	/**
	 * is 'old' the same context as 'new'? used to see if we need to create new storage for something
	 * defaults to "=="
	 * @param old
	 * @param current
	 * @return boolean
	 */
	protected boolean sameContext(Object old, Object current)
	{
		return sameContext((ContextTree.Bobj) old, (ContextTree.Bobj) current);
	}

	/**
	 * called when a containing context is deleted
	 * @param data
	 * @return Object
	 */
	abstract public Object deleteData(Object data);

	/**
	 * is 'old' the same context as 'new'? used to see if we need to create new storage for something
	 * defaults to "=="
	 * @param old
	 * @param current
	 * @return boolean
	 */
	protected boolean sameContext(ContextTree.Bobj old, ContextTree.Bobj current)
	{
		return old == current;
	}

	/**
	 * called to provide the default data for a new context
	 * @return Object
	 */
	abstract public Object newData( ContextTree.Bobj at, Object o, Object o2);
	

}
