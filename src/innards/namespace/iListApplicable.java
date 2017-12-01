package innards.namespace;

import java.util.*;

/**
	believe it or not, but this is useful for a particular style of inner class heavy programming
	*/
public abstract class iListApplicable 
{

	protected Object result = null;

	// subclass me, return null if you'd like
	protected Object perform(Object o){return null;}

	public List applyToList(List l)
	{
		ArrayList retRet = null;
		int m = l.size();
		for(int i=0;i<m;i++)
		{
			Object ret = perform(l.get(i));
			if (ret!=null)
			{
				if (retRet == null) retRet = new ArrayList();
				retRet.add(ret);
			}
		}
		return retRet;
	}
	
	public iListApplicable apply(List l)
	{
		applyToList(l);
		return this;
	}
	
	public Object getResult(){return result;}
}
