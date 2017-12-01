package innards.namespace.rhizome.metavariable;

import innards.Key;
import innards.namespace.context.ContextTree;

/**
 * I wish I had generics
 * @author marc
 */
public class MetaRef
{
	Object defaultValue;
	static protected int uniq = 0;
	
	public MetaRef(Object o)
	{
		this.defaultValue = o;
		Cull.registerForCull(this);
	}
	
	
	public Object get()
	{
		return ContextTree.get(this, defaultValue);
	}
	
	public Object set(Object o)
	{
		Metas.ensureMetaAdded(this);
		Object old = get();
		ContextTree.set(this, o);
		return old;
	}
	
}