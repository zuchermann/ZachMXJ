package innards.util;

import java.io.Serializable;
import java.util.*;

/**
 * @author marc
 * Created on May 6, 2003
 */
public class SimpleChangable implements iChangable, Serializable
{

	Map map = new HashMap();

	public iModCount getModCount(Object withRespectTo)
	{
//		System.out.println(" allocated mod count");
//		new Exception().printStackTrace();
		iModCount count = (iModCount)map.get(withRespectTo);
		if (count==null)
		{
			map.put(withRespectTo, count = new ModCount());
		}
		if (map.size()==0) throw new IllegalArgumentException();
		
		return count;
	}
	
	int count = 0;
	
	public void dirty()
	{
//		System.out.println(" size is <"+map.size()+">");
//		if (map.size()==0) throw new IllegalArgumentException();
		count++;
		
		/*Iterator i = map.values().iterator();
		while(i.hasNext())
		{
			((ModCount)i.next()).dirty = true;
		}*/
	}
	
	
	iChangable.iModCount[] chainCount;
	
	public void chainWith(iChangable.iModCount[] chainCount)
	{
		this.chainCount = chainCount;	
	}
	
	private class ModCount implements iChangable.iModCount, Serializable
	{
		transient int dirty = -1;
		transient Object data = null;
		
		public Object data()
		{
			return ((dirty!=count) | checkChain()) ? null : data;
		}
		
		public Object data(iRecompute recomp)
		{
			if ((dirty!=count) | checkChain())
			{
				Object r = recomp.recompute();
				clear(r);
				return r;
			}
			else return data;
		}
		public boolean hasChanged()
		{
			return (dirty!=count) | checkChain();
		}
		public iModCount clear(Object data)
		{
			this.data = data;
			dirty = count;
			return this;
		}
		
		public String toString()
		{
			return "child of <"+SimpleChangable.this.toString()+">";
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
