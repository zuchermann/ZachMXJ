package innards.namespace.context.key;

import innards.namespace.context.ContextTree;

import java.nio.ByteBuffer;

/**
 * @author marc
 * Created on Nov 29, 2003
 */
public class BKey extends CKey {

	public BKey(String s) {
		super(s);
		pushRoot().lookup(this).pop();
	}
	
	public ByteBuffer evaluate(ByteBuffer def)
	{
		Object o = (Object ) run(failure);
		if (o == failure) return def;
		return (ByteBuffer) o;
	}
	
	public BKey rootSet(ByteBuffer f)
	{
		pushRoot();
		ContextTree.set(this, f);
		pop();
		return this;
	}

	/**@param lookupTable*/
	public void set(ByteBuffer f) 
	{
		ContextTree.set(this, f);
	}
}
