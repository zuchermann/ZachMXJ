package innards.provider;

import innards.*;
import innards.namespace.context.*;


/**
	boo and mathboo provider functiosn.

	*/
public class ContextProvider
	extends NamedObject
	implements iFloatProvider, iUpdateable

{
	Object key;
	String cd;
	MathContextTree.Combiner combiner;

	public ContextProvider(Object key)
	{
		super("BooProvider:"+key);
		cd = null;
		this.key = key;
		combiner = null;
	}

	public ContextProvider(Object key, String cd)
	{
		super("BooProvider:"+key);
		this.cd = cd;
		this.key = key;
		this.combiner = null;
	}

	public ContextProvider(Object key, MathContextTree.Combiner comb)
	{
		super("BooProvider:"+key);
		this.cd = null;
		this.key = key;
		this.combiner = comb;
	}

	public ContextProvider(Object key, String cd, MathContextTree.Combiner comb)
	{
		super("BooProvider:"+key);
		this.cd = cd;
		this.key = key;
		this.combiner = comb;
	}

	boolean hasDef = false;
	float def = 0;
	
	public ContextProvider setDefault(float def)
	{
		hasDef = true;
		this.def = def;
		return this;
	}

	protected float now = 0;

	public void update()
	{
		if (cd!=null)
		{
			ContextTree.begin(cd);
		}

		if (combiner == null)
		{
			if (hasDef)
				now = ContextTree.getFloat(key, def);
			else
				now = ContextTree.getFloat(key);
		}
		else
		{
			now = MathContextTree.getFloat(key, combiner);
		}

		if (cd!=null)
		{
			ContextTree.end(cd);
		}
	}

	public float evaluate()
	{
		ProviderUpdator.update(this);
		return now;		
	}

}
