package innards.namespace.rhizome.metavariable;

import innards.Key;
import innards.namespace.context.*;
import innards.namespace.context.ContextTree;

/**
 * TODO: change to lazy Key / ContextTree access. only needed on the first get after the first set that the context has changed in 
 * @author marc
 */
public class MetaMutableNumber extends Number
{
	static protected int uniq= 0;

	float defaultValue;
	
	ContextTreeInternals.Bobj lazyWhere = null;
	boolean isLazy = true;
	float lazyValue;
	
	public MetaMutableNumber(float value)
	{
		this.defaultValue= value;
		lazyWhere = ContextTree.where();
		lazyValue = defaultValue;
	}

	public MetaMutableNumber(boolean value)
	{
		this.defaultValue= value ? 1 : 0;
		lazyWhere = ContextTree.where();
		lazyValue = defaultValue;
	}

	public int intValue()
	{
		return (int) value();
	}

	public long longValue()
	{
		return (long) value();
	}

	public float floatValue()
	{
		return (float) value();
	}

	public double doubleValue()
	{
		return (double) value();
	}
	public boolean booleanValue()
	{
		return value() != 0;
	}

	public float setValue(float v)
	{
		if (isLazy)
		{
			if (lazyWhere == ContextTree.where())
			{
				float old = lazyValue;
				lazyValue = v;
				return old;
			}
			else
			{
				lazyWhere.set(this, lazyValue);
				isLazy=false;
				return setValue(v);
			}
		}
		Metas.ensureMetaAdded(this);
		float old= value();
		ContextTree.set(this, v);
		return old;
	}

	public boolean setValue(boolean  v)
	{
		if (isLazy)
		{
			if (lazyWhere == ContextTree.where())
			{
				float  old = lazyValue;
				lazyValue = v ? 1 : 0;
				return old==1;
			}
			else
			{
				lazyWhere.set(this, lazyValue);
				isLazy=false;
				return setValue(v);
			}
		}
		Metas.ensureMetaAdded(this);
		boolean  old= booleanValue();
		ContextTree.set(this, v ? 1 : 0);
		return old;
	}

	protected float value()
	{
		if (isLazy) return lazyValue;
		return ContextTree.getFloat(this, defaultValue);
	}
	
	public String toString()
	{
		return "a:"+value();
	}
}
