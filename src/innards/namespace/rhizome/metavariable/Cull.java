/* Created on Jul 30, 2003 */
package innards.namespace.rhizome.metavariable;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author marc
 * created on Jul 30, 2003
 */
public class Cull {

	public interface iLocatable
	{
		public float getLocation();
	}

	static WeakHashMap lists= new WeakHashMap();
	static WeakHashMap maps= new WeakHashMap();
	static WeakHashMap refs= new WeakHashMap();

	static public void registerForCull(List list) {
		lists.put(list, null);
	}

	static public void registerForCull(Map map) {
		maps.put(map, null);
	}

	static public void registerForCull(MetaRef ref) {
		refs.put(ref, null);
	}

	static public void cullNow(float timeShed) {

		//System.gc();
		System.out.println(" culling <"+lists.size()+"> <"+maps.size()+"> <"+refs.size()+">");
		
		// lists;

		Iterator i= lists.keySet().iterator();
		while (i.hasNext()) {
			List l= (List) (i.next());
			cullList(l, timeShed);
		}

		// maps;
		i= maps.keySet().iterator();
		while (i.hasNext()) {
			Map l= (Map) (i.next());
			cullMap(l, timeShed);
		}
		//		refs;
		
		i= refs.keySet().iterator();
		while (i.hasNext()) {
			MetaRef l= (MetaRef) (i.next());
			cullRef(l, timeShed);
		}

	}

	static protected void cullList(List l, float timeShed) {
		List remove= new LinkedList();
		int size= l.size();
		for (int n= 0; n < size; n++) {
			Object o= l.get(n);
			if (o instanceof iLocatable) {
				if (((iLocatable) o).getLocation() < timeShed)
					remove.add(o);
			}
		}
		for (int n= 0; n < remove.size(); n++) {
			Object o= remove.get(n);
			System.out.println(" culled list entry<" + o + "> from list <" + l.getClass() + "> <" + l.hashCode() + ">");
			l.remove(o);
		}
	}
	
	static protected void cullRef(MetaRef r, float timeShed)
	{
		Object o = r.get();
		if (o instanceof iLocatable)
		{
			if (((iLocatable) o).getLocation() < timeShed)
				r.set(null);
		}
		if (o instanceof List)
		{
			cullList((List)o, timeShed);
		}
		if (o instanceof Map)
		{
			cullMap((Map)o, timeShed);
		}
	}

	static protected void cullMap(Map l, float timeShed) {
		List remove= new LinkedList();
		Iterator i= l.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry entry= (Entry) i.next();
			Object key= entry.getKey();
			Object value= entry.getKey();
			if (key instanceof iLocatable) {
				if (((iLocatable) key).getLocation() < timeShed)
					remove.add(key);
			} else if (value instanceof iLocatable) {
				if (((iLocatable) key).getLocation() < timeShed)
					remove.add(key);
			}

			if (value instanceof List) {
				cullList((List) value, timeShed);
			}
		}
		for (int n= 0; n < remove.size(); n++) {
			Object o= remove.get(n);
			Object removed= l.remove(o);
			System.out.println(
				" culled map entry <" + o + "> <" + removed + "> <" + l.getClass() + "> <" + l.hashCode() + ">");
		}
	}

}
