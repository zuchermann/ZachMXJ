package innards.namespace.context;

import innards.*;
import innards.iNamedObject;
import innards.namespace.BaseTraversalAction;
import innards.provider.iObjectProvider;


/**

	advanced hierarchical context tree. formerly, Boo2

 */
public class ContextTree extends ContextTreeSpecial
{

	/**
	 * opens a context called 'name'
	 * @param name
	 */
	static public void begin(String name)
	{
		begin(name, null);
	}

	
	/**
		 closes a context. if it wasn't called 'name' throw an IllegalStateException
		 * @param name0
		 */
	static public void end(String name)
	{
		if (contextTreeDebug)
			System.err.println(" CTREE end: " + name);
		if (!getAt().getName().equals(name))
			throw new IllegalStateException(" Boo.end('" + name + "') found but '" + getAt().getName() + "' expected ");
		setAt((Bobj) getAt().getParent());
	}
	/**
	 * retreives an entry from this context, or, if that cannot be found, parent contexts (in incresing order of distance)
	 * @param key
	 * @return Object
	 * @throws NullPointerException - if nothing called this is found
	 */
	static public Object get(Object key)
	{
		Object r = get(key, true);
		if (r instanceof iObjectProvider)
		{
			return ((iObjectProvider)r).evaluate();
		}
		return r;
	}

	/**
	 * like 'get' but with a default should nothing be found anywhere
	 * @param key
	 * @param def - default
	 * @return Object
	 */
	static public Object get(Object key, Object def)
	{
		Object r= get(key, false);
		if (r instanceof iObjectProvider)
		{
			return ((iObjectProvider)r).evaluate();
		}
		
		if (r == null)
			return def;
		return r;
	}

	static public float getFloat(Object key)
	{
		return resolveNumber(get(key));
	}

	/**
	 * like 'get' but with a default should nothing be found anywhere
	 * @param key
	 * @param def - default
	 * @return Object
	 */
	static public float getFloat(Object key, float def)
	{
		Object r= get(key, false);
		if (r == null)
			return def;
		return resolveNumber(r);
	}
	/**
		 * like 'get' but with a default should nothing be found anywhere
		 * @param key
		 * @param def - default
		 * @return Object
		 */

	static public int getInt(Object key, int def)
	{
		Object r= get(key, false);
		if (r == null)
			return def;
		return (int) resolveNumber(r);
	}

	


	/**
	 * searches up the stack for a child context called "context", returns Bobj that you can use to 'set' 
	 * @param key
	 * @return Object
	 * @throws IllegalArgumentException if no child context called "context" could be found
	 */

	static public Bobj parentContext(String context)
	{
		Bobj at= getAt();
		while (at != null)
		{
			if (at.hasChild(context))
				return (Bobj) at.getChild(context);
			at= (Bobj) at.getParent();
		}
		throw new IllegalArgumentException(" propogate: couldn't find context called <" + context + ">");
	}

	/**
	 * sets in parameter in the parent context
	 * @param key
	 * @param value
	 */
	static public void propagate(Object key, float value)
	{
		((Bobj) getAt().getParent()).set(key, value);
	}
	/**
	 * sets in parameter in the parent context
	 * @param key
	 * @param value
	 */
	static public void propagate(Object key, int value)
	{
		((Bobj) getAt().getParent()).set(key, value);
	}
	/**
	 * sets in parameter in the parent context
	 * @param key
	 * @param value
	 */
	static public void propagate(Object key, Object value)
	{
		((Bobj) getAt().getParent()).set(key, value);
	}

	/**
	 * sets in parameter in this context
	 * @param key
	 * @param value
	 */
	static public void set(Object key, float value)
	{
		getAt().set(key, value);
	}
	/**
	 * sets in parameter in this context
	 * @param key
	 * @param value
	 */
	static public void set(Object key, int value)
	{
		getAt().set(key, value);
	}

	

	
	/**
	 * sets in parameter in this context
	 * @param key
	 * @param value
	 */
	static public void set(Object key, Object value)
	{
		getAt().set(key, value);
	}

	/**
	 * sets in subcontext. equivilent to 
	 * begin(subcontext)
	 * set(key,value)
	 * end(subcontext)
	 * @param subcontext
	 * @param key
	 * @param value
	 */
	static public void setIn(String subcontext, Object key, float value)
	{
		begin(subcontext);
		set(key, value);
		end(subcontext);
	}

	/**
	 * sets in subcontext. equivilent to 
	 * begin(subcontext)
	 * set(key,value)
	 * end(subcontext)
	 * @param subcontext
	 * @param key
	 * @param value
	 */
	static public void setIn(String subcontext, Object key, int value)
	{
		begin(subcontext);
		set(key, value);
		end(subcontext);
	}

	/**
	 * sets in subcontext. equivilent to 
	 * begin(subcontext)
	 * set(key,value)
	 * end(subcontext)
	 * @param subcontext
	 * @param key
	 * @param value
	 */
	static public void setIn(String subcontext, Object key, Object value)
	{
		begin(subcontext);
		set(key, value);
		end(subcontext);
	}
	

	/**
	 * static access only
	 */
	protected ContextTree()
	{
	}


	/**
	 * 
	 */
	public static void debugPrintTopology()
	{
		new BaseTraversalAction()
		{
			/*
			 * @see innards.namespace.BaseTraversalAction#actionImplementation(innards.iNamedObject)
			 */
			protected boolean actionImplementation(iNamedObject node)
			{
				System.out.println(spaces(getRecursionLevel())+node.getName());
				return true;
			}
			
			protected String spaces(int i)
			{
				String s = "";
				while(s.length()<i) s += " ";
				return s;
			}
			
		}.applyAction((iNamedGroup)root);
	}

}
