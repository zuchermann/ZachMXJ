package innards.namespace;

/** 

    Finds things and then remembers how to find them again.
    
    'a drop in replacement' for FindByName.findFirst that does hashmaps (again).
    
    except that you can use an instance (rather than statics); 

*/

import innards.*;
import java.util.*;
import java.lang.ref.*;

public class FindByNameCached extends FindByName implements Cloneable
{
	WeakHashMap map;
	iNamedObject root;

	public FindByNameCached(iNamedObject root)
	{
		super("unnamed");
		map = new WeakHashMap();
		this.root = root;
	}

	public boolean is(NamedObject no)
	{
		//System.out.println(" comparing <"+no.getName()+"> with <"+name+">");
		return name.equals(no.getName());
	}

	public NamedObject findFirstCached(String name)
	{
		NamedObject ref = (NamedObject) map.get(name);
		if (ref == null)
		{
			//System.out.println(" nothing in the cache for <"+name+">");
			ref = findFirstImp(name);
			//System.out.println(" got <"+ref+">");
			map.put(name, ref);
		} else
		{
			//System.out.println(" returning cached entry for <"+name+"> is <"+ref+">");
		}
		return ref;
	}

	protected NamedObject findFirstImp(String name)
	{
		this.mode = FindByPredicate.FIND_FIRST;
		this.name = name;
		this.ret.clear();
		this.applyAction(root);
		if (this.ret.size() > 0)
		{
			return (NamedObject) (this.ret.elementAt(0));
		} else
			return null;
	}

	static WeakHashMap cacheMap = new WeakHashMap();

	static public NamedObject findFirst(String name, NamedObject root)
	{
		// look up the findbynamechaced
		FindByNameCached c = (FindByNameCached) cacheMap.get(root);
		if (c == null)
		{
			c = new FindByNameCached(root);
			cacheMap.put(root, c);
		}

		return c.findFirstCached(name);
	}
}