package innards.provider;

import innards.iUpdateable;
import innards.math.linalg.Vec;
import innards.namespace.context.*;

/**
	boo and mathboo provider functiosn.

	I though I wrote this before ?
	*/
public class ContextVectorProvider
	implements iVectorProvider, iUpdateable
{
	Object[] key;
	String cd;
	MathContextTree.Combiner combiner;

	public ContextVectorProvider(Object[] key)
	{
		cd = null;
		this.key = key;
		combiner = null;
		now = new double[key.length];
	}

	public ContextVectorProvider(Object[] key, String cd)
	{
		this.cd = cd;
		this.key = key;
		this.combiner = null;
		now = new double[key.length];
	}

	public ContextVectorProvider(Object[] key, MathContextTree.Combiner comb)
	{
		this.cd = null;
		this.key = key;
		this.combiner = comb;
		now = new double[key.length];
	}

	public ContextVectorProvider(Object[] key, String cd, MathContextTree.Combiner comb)
	{
		this.cd = cd;
		this.key = key;
		this.combiner = comb;
		now = new double[key.length];
	}

	double[] now;

	public void update()
	{
		if (cd!=null)
		{
			ContextTree.begin(cd);
		}

		if (combiner == null)
		{
			for(int i=0;i<now.length;i++)
			{
				now[i] = ContextTree.getFloat(key[i]);
			}
		}
		else
		{
			for(int i=0;i<now.length;i++)
			{
			    now[i] = MathContextTree.getFloat(key[i], combiner);
			}
		}

		if (cd!=null)
		{
			ContextTree.end(cd);
		}
	}

	public void get(Vec v)
	{
		ProviderUpdator.update(this);
		for(int i=0;i<now.length;i++)
		{
			v.set(i, now[i]);
		}
	}

	public Vec construct()
	{
		return new Vec(now.length);
	}

}
