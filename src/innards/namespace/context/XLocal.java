package innards.namespace.context;

import java.util.*;


/**
 * @author marc
 * Created on May 6, 2003
 */
public abstract class XLocal
{

	protected Map knownContexts= new WeakHashMap();
	protected List knownContextList= new ArrayList();

	protected Object cacheCurrent= null;
	protected Object cacheCurrentData= null;

	public interface iProvidesContext
	{
		public Object context();
	}

	iProvidesContext context;

	public XLocal(iProvidesContext context)
	{
		this.context= context;
	}

	/**
	 * called to provide the default data for a new context
	 * @return Object
	 */
	abstract public Object newData(Object at, Object oldContext, Object oldData);

	public Object forkData(Object at, Object oldContext, Object oldData)
	{
		return newData(at,oldContext, oldData);
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
		return old == current;
	}

	public Object getData()
	{
		//		here is the action
		Object current= context.context();
//		System.out.println(" context is <"+current+"> current ? <"+current==cacheCurrent+">");
		
		if (current == cacheCurrent)
			return cacheCurrentData;

		// otherwise, is it known?
		Wrapper wrapper= (Wrapper) knownContexts.get(current);
		if (wrapper != null)
			return wrapper.data;

		// otherwise, is it the same as any of the other ones?
		final Wrapper newWrapper= new Wrapper(current);
		for (Iterator iter= knownContextList.iterator(); iter.hasNext();)
		{
			Wrapper wrapped= (Wrapper) iter.next();
			if (wrapped.equals(newWrapper))
			{
				knownContexts.put(current, wrapped);

				cacheCurrentData= wrapped.data;
				cacheCurrent= current;

				return wrapped.data;
			}
		}

		// otherwise, it is genuinely new
		newWrapper.data= newData(current, cacheCurrent, cacheCurrentData);
		knownContexts.put(current, newWrapper);
		knownContextList.add(newWrapper);
		
		cacheCurrentData= newWrapper.data;
		cacheCurrent= current;

		return newWrapper.data;
	}
	
	public void fork(Object[] otherContexts)
	{
		Object currentContext = getContext();
		
		Wrapper[] w = new Wrapper[otherContexts.length];
		
		Object dataHere = getData();
		for(int i=0;i<w.length;i++)
		{
			w[i] = new Wrapper(otherContexts[i]);
			w[i].data = forkData(currentContext, otherContexts[i], dataHere);
			knownContexts.put(otherContexts[i], w[i]);
			knownContextList.add(w[i]);
		}
	}
	
	public Object getContext()
	{
		return context.context();
	}
	
	class Wrapper
	{
		Object to;
		Object data;

		public Wrapper(Object to)
		{
			this.to= to;
		}

		public boolean equals(Object o)
		{
			if (!(o instanceof Wrapper))
				return super.equals(o);
			return sameContext(to, ((Wrapper) o).to);
		}

		public int hashCode()
		{
			return to.hashCode();
		}
	}

}
