package innards.namespace.factory;

import innards.*;

/**
 * wraps a production with a static construction parameter.<br>
 * when <code>produce</code> gets called, calls <code>produce</code> on the
 * wrapped production, passing the static parameter specified at construction
 * time and ignoring the parameter passed in at produce time.<br>
 * useful when the parameters are known at construct time and it's easier not to
 * track them down later.
 */
public class StaticParamFactoryProduction extends NamedObject implements iProduction
{
	protected iProduction wrapped;
	protected Object param;
	
	public StaticParamFactoryProduction(iProduction wrap, Object param)
	{
		super("production <"+wrap.toString()+"> with static parameter <"+param.toString()+">");
		this.wrapped = wrap;
		this.param = param;
	}

	/**
	 * @see innards.namespace.factory.iProduction#produce(java.lang.Object)
	 */
	public Object produce(Object ignore)
	{
		return wrapped.produce(this.param);
	}
}
