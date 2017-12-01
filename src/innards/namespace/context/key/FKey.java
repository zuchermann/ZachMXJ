package innards.namespace.context.key;

import innards.namespace.context.ContextTree;
import innards.provider.iFloatProvider;

/**
 * @author marc
 */
public class FKey extends CKey implements iFloatProvider
{
	public FKey(String s)
	{
		super(s);
		pushRoot().lookup(this).pop();
	}

	public float evaluate()
	{
		return asFloat();
	}

	public FKey defaults(final float f)
	{
		executionStack.add(new Defaults(new fu()
		{
			public float ret()
			{
				return f;
			}
		}));
		return this;
	}
	public FKey defaults(final iFloatProvider f)
	{
		executionStack.add(new Defaults(new fu()
		{
			public float ret()
			{
				return f.evaluate();
			}
		}));
		return this;
	}
	public FKey defaults(final Number f)
	{
		executionStack.add(new Defaults(new fu()
		{
			public float ret()
			{
				return f.floatValue();
			}
		}));
		return this;
	}

	public FKey set(float f)
	{
		ContextTree.set(this, f);
		return this;
	}
	
	public FKey rootSet(float f)
	{
		pushRoot();
		ContextTree.set(this, f);
		pop();
		return this;
	}
}
