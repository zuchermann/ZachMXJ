package innards.namespace.context.key;

import innards.namespace.context.ContextTree;

import java.util.List;

/**
 * @author marc
 */
public class FaKey extends CKey 
{
	final float[] nothing = new float[0];
	
	public FaKey(String s) {
		super(s);
		pushRoot().lookup(this).pop();
	}
	public float[] get() {
		Object o = this.run(nothing);
		if (o == nothing)
			throw new IllegalArgumentException(" ran OKey <" + this
					+ ">, no default, found nothing in context <"
					+ ContextTree.dir() + ">");
		return (float[])o;
	}
	public float[] get(float[] def) {
		if (def == null) def  = nothing;
		
		Object o = this.run(def);
		if (o == failure)
			throw new IllegalArgumentException(" ran OKey <" + this
					+ ">, with default <"+def+">got failure <"
					+ ContextTree.dir() + ">");
		return (float[]) (o==nothing ? null : o);
	}
	
	public FaKey set(float[] f)
	{
		ContextTree.set(this, f);
		return this;
	}
	
	public FaKey rootSet(float[] f)
	{
		pushRoot();
		ContextTree.set(this, f);
		pop();
		return this;
	}
	
	static public float[] toArray(List l)
	{
		float[] r = new float[l.size()];
		for(int i=0;i<l.size();i++)
		{
			r[i] = ((Number)l.get(i)).floatValue();
		}
		return r;
	}
}
