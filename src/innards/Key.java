package innards;

import java.io.*;
import java.io.Serializable;
import java.lang.ref.*;
import java.util.*;

/**
 * A class that is primarly meant to be used to create unique pointers which
 * have a string label.
 * <p>
 * Keys are used extensively in this system when interacting with <code>
 * contextTrees </code>,
 * such as <code> WorkingMemory </code>. This use is documented in <code> iKeyInterfaces </code>.
 * <p>
 * if assertions are enabled at runtime (java -ea:innards.Key) this class will
 * generate stack traces for allocations of keys. (this will only be true on
 * 1.4) for information on how to set this see:
 * http://java.sun.com/j2se/1.4/docs/guide/lang/assert.html
 * 
 * @see innards.iKeyInterfaces
 * @author synchar
 */
public class Key implements Serializable
{
	static public Map<String, Reference<Key>> internedKeys = new WeakHashMap<String, Reference<Key>>();

	protected String rep;

	protected Key()
	{
	}

	public Key(String s)
	{
		this.rep = s.intern();

		// proof of concept - zero cost (at non-debug
		// time) allocation stack trace
		//assert generateAllocationStackTrace() :
		// "generate allocation stack trace (should
		// never fail)";

		//assert(!internedKeys.containsKey(rep)):
		// "can't have two keys with the same
		// string...";

		if (!internedKeys.containsKey(rep))
			internedKeys.put(rep, new WeakReference<Key>(this));
	}

	public String toString()
	{
		return rep;
	}

	/**
	 * for persistance
	 * --------------------------------------------------------------------------------------------------------
	 */

	/**
	 * throws IllegalArgumentException if the key cannot be found
	 * in the map. This call is (and should only be) used for
	 * persisting objects that mention keys
	 */
	static public Key internKey(Key k)
	{
		Reference<Key> r = internedKeys.get(k.rep);
		if (r == null)
			throw new IllegalArgumentException(" couldn't find already interned key called <" + k.rep + ">");
		Key found = (Key) (r.get());
		if (found == null)
			throw new IllegalArgumentException(" couldn't find already interned key called <" + k.rep + ">");
		return found;
	}

	/**
	 * proof of concept - zero cost (at non-debug time) allocation
	 * stack trace ----------------------------------
	 */

	transient protected StackTraceElement[] allocationStackTrace = null;
	protected boolean generateAllocationStackTrace()
	{
		allocationStackTrace = new Exception().getStackTrace();
		return true;
	}

	/**
	 * I will return null if asserts are not enabled for innards.*
	 * 
	 * @return StackTraceElement[]
	 */

	public StackTraceElement[] getAllocationStackTrace()
	{
		return allocationStackTrace;
	}

	public StackTraceElement whereAllocated()
	{
		if (allocationStackTrace == null)
			return null;
		return allocationStackTrace[allocationStackTrace.length - 2];
	}

	/**
	 * for the deserialization of keys from, if you really care
	 * about what this is doing, you'll have to read the Java
	 * Object Serialization Specification.
	 */
	Object readResolve() throws ObjectStreamException
	{
		Reference<Key> o = internedKeys.get(this.rep);
		return o == null ? this : o.get();
	}

	public Object skaReadResolve()
	{
		try
		{
			return readResolve();
		}
		catch (ObjectStreamException e)
		{
			// TODO Auto-generated catch
			// block
			e.printStackTrace();
		}
		return this;
	}

	// use for debugging only
	static public Key stringToKey(String s)
	{
		Reference<Key> ref = internedKeys.get(s);
		if (ref != null) {
			return ref.get();
		}
		
		// second option
		try
		{
			Key k = Key.internKey(new Key(s));
			return k;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}