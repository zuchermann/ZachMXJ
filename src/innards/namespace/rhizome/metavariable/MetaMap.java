package innards.namespace.rhizome.metavariable;

import innards.Key;
import innards.namespace.context.*;
import innards.namespace.context.ContextTreeInternals.Bobj;

import java.util.*;


/**
 * @author marc
 */
public class MetaMap implements Map
{
	static int contextMapKeyUniq= 0;
	//Key storageKey= new Key((contextMapKeyUniq++) + "_contextMapStorage");

	Object cached_listOfMaps_where= null;
	List _cached_listOfMaps= new ArrayList();
	
	public MetaMap()
	{
		Cull.registerForCull(this);
	}
	
	/**
	 * propogates the contents of this level up to the parent level rarely used it seems
	 */
	public MetaMap retain()
	{
		List l= listOfMaps();
		Map lowest= ((Map) l.get(0));
		Bobj at= (Bobj) ContextTree.where().getParent(0);
		if (at != null)
		{
			Map list= (Map) at.get(this, null);
			if (list == null)
				at.set(this, new HashMap(lowest));
			else
				list.putAll(lowest);
			lowest.clear();
		}
		return this;
	}

	/**
	 * propogates up a single key
	 */
	public MetaMap retain(Object key)
	{
		List l= listOfMaps();
		Map lowest= ((Map) l.get(0));
		Bobj at= (Bobj) ContextTree.where().getParent(0);
		if (at != null)
		{
			Map list= (Map) at.get(this, null);
			if (list == null)
			{
				HashMap hm = new HashMap();
				hm.put(key, this.get(key));
				at.set(this, hm);
			}
			else
				list.put(key, this.get(key));
		}
		return this;
	}

	protected List listOfMaps()
	{
		Bobj at= ContextTree.where();
		if (at == cached_listOfMaps_where)
			return _cached_listOfMaps;
		// need to build lists
		_cached_listOfMaps.clear();
		Map firstList= (Map) at.get(this, null);
		if (firstList == null)
			ContextTree.set(this, firstList= new WeakHashMap());
		_cached_listOfMaps.add(firstList);
		final Bobj fat= at;
		cached_listOfMaps_where= at;

		while (at != null)
		{
			at= (Bobj) at.getParent(0);
			if (at != null)
			{
				Map list= (Map) at.get(this, null);
				if (list != null)
				{
					_cached_listOfMaps.add(list);
				}
			}
		}
		Metas.ensureMetaAdded(this);
		return _cached_listOfMaps;
	}

	/**
		inaccurate -doesn't account for overlap
	 *  * @see java.util.Map#size()
	 */
	public int size()
	{
		List l= listOfMaps();
		int t= 0;
		for (int i= 0; i < l.size(); i++)
			t += ((Map) l.get(i)).size();
		return t;
	}

	/**
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty()
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
			if (!((Map) l.get(i)).isEmpty())
				return false;
		return true;
	}

	/**
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key)
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
			if (((Map) l.get(i)).containsKey(key))
				return true;
		return false;
	}

	/**
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value)
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
			if (((Map) l.get(i)).containsValue(value))
				return true;
		return false;
	}

	/**
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key)
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
			if (((Map) l.get(i)).containsKey(key))
				return ((Map) l.get(i)).get(key);
		return null;
	}

	/**
	 * @see java.util.Map#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value)
	{
		List l= listOfMaps();
		return ((Map) l.get(0)).put(key, value);
	}

	/**
	 * @see java.util.Map#remove(java.lang.Object)
	 */
	public Object remove(Object key)
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
		{
			Object o= ((Map) l.get(i)).remove(key);
			if (o != null)
				return o;
		}
		return null;
	}

	/**
	 * @see java.util.Map#putAll(java.util.Map)
	 */
	public void putAll(Map t)
	{
		List l= listOfMaps();
		((Map) l.get(0)).putAll(t);
	}

	/**
	 * @see java.util.Map#clear()
	 */
	public void clear()
	{
		List l= listOfMaps();
		for (int i= 0; i < l.size(); i++)
			 ((Map) l.get(i)).clear();
	}

	/**
	 * @see java.util.Map#keySet()
	 */
	public Set keySet()
	{
		List l= listOfMaps();
		HashSet set= new HashSet();
		for (int i= 0; i < l.size(); i++)
			set.addAll(((Map) l.get(i)).keySet());
		return set;
	}

	/**
	 * @see java.util.Map#values()
	 */
	public Collection values()
	{
		List l= listOfMaps();
		List n= new ArrayList();
		for (int i= 0; i < l.size(); i++)
			n.addAll(((Map) l.get(i)).values());
		return n;
	}

	/**
	 * @see java.util.Map#entrySet()
	 */
	public Set entrySet()
	{
		List l= listOfMaps();
		HashSet set= new HashSet();
		for (int i= 0; i < l.size(); i++)
			set.addAll(((Map) l.get(i)).entrySet());
		return set;
	}

	public String toString()
	{
		return listOfMaps().toString();
	}
}
