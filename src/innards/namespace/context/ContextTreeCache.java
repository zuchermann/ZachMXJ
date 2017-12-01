package innards.namespace.context;

import java.util.*;

/**
	for caching where things are, this is a simple implementation

	deleting _contexts_ is no problem for now, because nothing can refer to a non-existant child, the only thing that will have caches that point
 to the thing that you've just deleted are going to be south of that in the heirarchy, but you've just deleted those too.
	if we move to a non-heirarchical Boo then the above statement wont hold.

	deleting _entries_ require a call to invalidateKey, thus, for safety, we should invalidateKey on all of the entries of any deleted context. see ContextTree.delete

 */
public class ContextTreeCache
{

	Map keysToListsOfCaches = new WeakHashMap(); // bobjs that hold requests for such keys, that need to be invalidated when someone overwrites a null key

	// this is all the interface that a BObj needs
	public class Cache
	{
		Map keysToLocations = new WeakHashMap();

		public Cache(){}

		/**
			call this when someone overwrites a null slot
		 */
		public void notifyNullOverwritten(Object key)
		{
			ContextTreeCache.this.invalidateKey(key);
		}

		public void cacheLocation(Object askedFor, ContextTree.Bobj foundHere)
		{
			keysToLocations.put(askedFor, foundHere);
			List l = (List)keysToListsOfCaches.get(askedFor);
			if (l==null) keysToListsOfCaches.put(askedFor, l = new ArrayList());
			l.add(this);
		}

		public ContextTree.Bobj lookup(Object askedFor)
		{
			ContextTree.Bobj c =  (ContextTree.Bobj)keysToLocations.get(askedFor);
			//if (c !=null) System.out.println(" (cached answer)");
			return c;
		}

		protected void invalidate(Object key)
		{
			keysToLocations.remove(key);
		}
	}

	public void invalidateKey(Object key)
	{
		// lookup list
		List list = (List)keysToListsOfCaches.get(key);
		if (list!=null)
		{
			for(int i=0;i<list.size();i++)
			{
				Cache c = (Cache)list.get(i);
				c.invalidate(key);
			}
		}
	}

		
}
