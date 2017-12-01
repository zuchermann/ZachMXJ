package innards.namespace.context.key;
import innards.namespace.context.ContextTree;
import innards.provider.iFloatProvider;
/**
 * @author marc
 */
public class OKey extends CKey {

	final Object nothing = new Object();
	
	public OKey(String s) {
		super(s);
		pushRoot().lookup(this).pop();
	}
	public Object get() {
		Object o = this.run(nothing);
		if (o == nothing)
			throw new IllegalArgumentException(" ran OKey <" + this
					+ ">, no default, found nothing in context <"
					+ ContextTree.dir() + ">");
		return o;
	}
	public Object get(Object def) {
		if (def == null) def  = nothing;
		
		Object o = this.run(def);
		if (o == failure)
			throw new IllegalArgumentException(" ran OKey <" + this
					+ ">, with default <"+def+">got failure <"
					+ ContextTree.dir() + ">");
		return o==nothing ? null : o;
	}
	
	public OKey set(Object f)
	{
		ContextTree.set(this, f);
		return this;
	}
	
	public OKey rootSet(Object f)
	{
		pushRoot();
		ContextTree.set(this, f);
		pop();
		return this;
	}
}
