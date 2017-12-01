package innards.namespace.rhizome.metavariable;

import java.util.*;
import innards.util.*;

/**
 * @author marc
 * Created on May 6, 2003
 */
public class MetaChangable implements iChangable
{

	Map map = new MetaMap();

	public iModCount getModCount(Object withRespectTo)
	{
		iModCount count = (iModCount)map.get(withRespectTo);
		if (count==null)
		{
			map.put(withRespectTo, count = new ModCount());
		}
		if (map.size()==0) throw new IllegalArgumentException();
		
		return count;
	}
	
	public void dirty()
	{
		Iterator i = map.values().iterator();
		while(i.hasNext())
		{
			((ModCount)i.next()).dirty.setValue(true);
		}
	}
	
	
	iChangable.iModCount[] chainCount;
	
	public void chainWith(iChangable.iModCount[] chainCount)
	{
		this.chainCount = chainCount;	
	}
	
	private class ModCount implements iChangable.iModCount
	{
		MetaMutableNumber dirty = new MetaMutableNumber(true);
		Object data = null;
		
		public Object data()
		{
			return (dirty.booleanValue() | checkChain()) ? null : data;
		}
		
		public Object data(iRecompute recomp)
		{
			if (dirty.booleanValue() | checkChain())
			{
				Object r = recomp.recompute();
				clear(r);
				return r;
			}
			else return data;
		}
		public boolean hasChanged()
		{
			return dirty.booleanValue() | checkChain();
		}
		public iModCount clear(Object data)
		{
			this.data = data;
			dirty.setValue(false);
			return this;
		}
		
		public String toString()
		{
			return "child of <"+MetaChangable.this.toString()+">";
		}
	}
	
	protected boolean checkChain()
	{
		if (chainCount == null) return false;
		boolean ret = false;
		for(int i=0;i<chainCount.length;i++)
		{
			if (chainCount[i].hasChanged())
			{
				dirty();
				chainCount[i].clear(null);
				ret = true;
			}
		}
		return ret;
	}
	

}
