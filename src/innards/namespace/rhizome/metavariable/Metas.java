package innards.namespace.rhizome.metavariable;

import innards.Key;
import innards.namespace.context.ContextTree;

import java.lang.ref.WeakReference;
import java.util.*;

/**
 * conventions for Meta (dynamic) variables comming soon 'retain' on all of things, 
 * TODO: need to cull this thing
 * @author marc
 */
public class Metas
{
	protected static Key metaDirKey = new Key("metaDirKey");
	/**
	 * makes sure that this meta has been added to a local director of meta variables are this level
	 */
	static public void ensureMetaAdded(Object o)
	{
		// commented out because currently it is one great big leak.
		return ;
		/*
		ContextTree.begin("_meta");
		// sigh, pass by function...
		List dir = (List)ContextTree.get(metaDirKey, null);
		if (dir == null) 
		{
			dir = new ArrayList(1); 
			ContextTree.set(metaDirKey, dir);
		}
		dir.add(new WeakReference(o));
		ContextTree.end("_meta");
		*/
	}
}
