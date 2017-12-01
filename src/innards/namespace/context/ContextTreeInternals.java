package innards.namespace.context;

import innards.*;
import innards.namespace.BaseTraversalAction;
import innards.provider.*;
import innards.provider.iFloatProvider;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author marc Created on May 12, 2003
 */
public class ContextTreeInternals
{

	static public class BaseBobj extends NamedGroup implements Bobj
	{
		ContextTreeCache.Cache cache;

		HashMap childmap = new HashMap();

		public Map map = new WeakHashMap();

		public List parentList = new ArrayList(1);

		public WeakHashMap trapChain = null;
		public HashMap watches = null;

		public BaseBobj()
		{
			cache = ((ContextTreeCache) booCache.get()).new Cache();
		}

		public BaseBobj(String name)
		{
			super(name);
			cache = ((ContextTreeCache) booCache.get()).new Cache();
		}

		public void addChild(Bobj o)
		{
			super.addChild(o);
			childmap.put(o.getName(), o);
		}

		public void addTrap(Trap trap)
		{
			if (trapChain == null)
				trapChain = new WeakHashMap();
			trapChain.put(trap, null);
		}

		public void addWatchPoint(Object key, WatchPoint point)
		{
			if (watches == null)
			{
				watches = new HashMap();
			}
			if (!watches.containsKey(key))
			{
				watches.put(key, new ArrayList());
			}
			List watchPointList = (List)watches.get(key);
			watchPointList.add(point);
		}

		public void removeTrap(Trap t)
		{
			if (trapChain != null)
			{
				trapChain.remove(t);
			}
			if (trapChain != null)
				if (trapChain.size() == 0)
					trapChain = null;
		}

		public void removeWatchPoint(Object key, WatchPoint point)
		{
			if (watches != null)
			{
				if (watches.containsKey(key))
				{
					List watchPointList = (List)watches.get(key);
					watchPointList.remove(point);
					
					if (watchPointList.size() == 0)
					{
						watches.remove(key);
					}
					if (watches.size() == 0)
					{
						watches = null;
					}
				}
			}
		}

		public void begin()
		{
			if (trapChain != null)
			{
				if (trapChain.size() > 0)
				{
					Iterator i = trapChain.keySet().iterator();
					while (i.hasNext())
					{
						((Trap) i.next()).begin();
					}
				}
			}
		}
		public void delete()
		{
			if (trapChain != null)
			{
				Iterator i = trapChain.keySet().iterator();
				while (i.hasNext())
				{
					((Trap) i.next()).delete();
				}
			}
			Iterator i = map.entrySet().iterator();
			while (i.hasNext())
			{
				Map.Entry e = (Entry) i.next();
				((ContextTreeCache) booCache.get()).invalidateKey(e.getKey());
			}
		}

		protected void doWatch(Object key)
		{
			List watchPointList = (List)watches.get(key);
			if (watchPointList != null)
			{
				for (int i=0; i<watchPointList.size(); i++)
				{
					WatchPoint point = (WatchPoint)watchPointList.get(i);
					//System.out.println(" watch point for <"+key+"> of class <"+key.getClass().getName()+"> is <"+point+">");
					point.get(key, map.get(key));
				}
			}
			
			List nullWatchPointList = (List)watches.get(null);
			if (nullWatchPointList != null)
			{
				for (int i=0; i<nullWatchPointList.size(); i++)
				{
					WatchPoint nullPoint = (WatchPoint)nullWatchPointList.get(i);
					nullPoint.get(key, map.get(key));
				}
			}
		}

		protected void doWatch(Object key, Object value)
		{
			List watchPointList = (List)watches.get(key);
			if (watchPointList != null)
			{
				for (int i=0; i<watchPointList.size(); i++)
				{
					WatchPoint point = (WatchPoint)watchPointList.get(i);
					point.set(key, map.get(key), value);
				}
			}
			
			List nullWatchPointList = (List)watches.get(null);
			if (nullWatchPointList != null)
			{
				for (int i=0; i<nullWatchPointList.size(); i++)
				{
					WatchPoint nullPoint = (WatchPoint)nullWatchPointList.get(i);
					nullPoint.set(key, map.get(key), value);
				}
			}
		}
		public void end()
		{
			if (trapChain != null)
			{
				if (trapChain.size() > 0)
				{
					Iterator i = trapChain.keySet().iterator();
					while (i.hasNext())
					{
						if (!((Trap) i.next()).end())
							i.remove();
					}
				}
			}
		}
		public Object get(Object key)
		{
			//if (key == iPosegraph_keys.Read.MOTOR_DESIRED)
			//{
				//System.out.println(" get motor desired, watches are <" + watches + ">");
			//}
			if (watches != null)
				doWatch(key);
			return map.get(key);
		}

		public Object get(Object key, Object def)
		{
			//if (key == iPosegraph_keys.Read.MOTOR_DESIRED)
			//{
			//	System.out.println(" get motor desired, watches are <" + watches + ">");
			//}
			if (watches != null)
				doWatch(key);
			Object r = this.get(key);
			if (r == null)
				return def;
			return r;
		}
		public ContextTreeCache.Cache getCache()
		{
			return cache;
		}

		public iNamedObject getChild(String name)
		{
			return (iNamedObject) childmap.get(name);
		}
		public float getFloat(Object key)
		{
			return ((Number) get(key)).floatValue();
		}

		public float getFloat(Object key, float def)
		{
			//if (key == iPosegraph_keys.Read.MOTOR_DESIRED)
			//{
			//	System.out.println(" get motor desired, watches are <" + watches + ">");
			//}
			if (watches != null)
				doWatch(key);
			Object r = this.get(key);
			if (r == null)
				return def;
			return ((Number) r).floatValue();
		}
		public float getInt(Object key)
		{
			return ((Number) get(key)).intValue();
		}

		public Map getMap()
		{
			return map;
		}

		public int getNumParents()
		{
			return parentList.size();
		}

		public iNamedGroup getParent(int i)
		{
			if (i >= parentList.size())
				return null;
			return (Bobj) parentList.get(i);
		}
		public void remove(Object key)
		{
			map.remove(key);
		}

		public iNamedObject removeChild(iNamedObject o)
		{
			iNamedObject ret = super.removeChild(o);
			childmap.remove(o.getName());
			if (cache != null)
				cache.invalidate(o.getName());
			return ret;
		}
		public void removeParent(iNamedGroup parent)
		{
			super.removeParent(parent);
			parentList.remove(parent);
		}

		public void set(Object key, float value)
		{
			Float f = new Float(value);
			if (watches != null)
				doWatch(key, f);
			Object ret = map.put(key, f);
			if ((ret == null))
				cache.notifyNullOverwritten(key);
		}
		public void set(Object key, int value)
		{
			Float f = new Float(value);
			if (watches != null)
				doWatch(key, f);
			Object ret = map.put(key, f);
			if ((ret == null))
				cache.notifyNullOverwritten(key);
		}

		public void set(Object key, Object value)
		{
			if (watches != null)
				doWatch(key, value);
			Object ret = map.put(key, value);
			if ((ret == null) && (value != null))
				cache.notifyNullOverwritten(key);
		}

		public void setParent(iNamedGroup parent)
		{
			/*if (parent == null)
				parentList.remove(parentList.size() - 1);

			//			System.out.println("
			// setParent called ... ");
			if (super.getParent() == null)
				super.setParent(parent);
			if (parent != null)
				parentList.add(parent);
			//			System.out.println(" node
			// <"+this+"> now has
			// <"+parentList.size()+">
			// parents ");*/
		}

		public String toString()
		{
			return this.getName() + "/";
		}
	}

	public interface Bobj extends iNamedGroup
	{

		public void addChild(Bobj o);
		public void addTrap(Trap trap);
		public void removeTrap(Trap trap);

		public void addWatchPoint(Object key, WatchPoint point);
		public void removeWatchPoint(Object key, WatchPoint point);

		public void begin();
		public void delete();
		public void end();

		public Object get(Object key);

		public Object get(Object key, Object def);
		public ContextTreeCache.Cache getCache();

		public iNamedObject getChild(String name);
		public float getFloat(Object key);

		public float getFloat(Object key, float def);
		public float getInt(Object key);

		public Map getMap();
		public int getNumParents();

		public iNamedGroup getParent(int i);
		public void remove(Object key);
		public void set(Object key, float value);
		public void set(Object key, int value);
		public void set(Object key, Object value);

	}

	public interface iNewContextDelegate
	{
		/**
		 * needs to 'create' (or alias or something)
		 * and parent.addChild(...) or something and
		 * return the child
		 * 
		 * @param name
		 * @param parent
		 * @return
		 */
		public Bobj createAndAttach(String name, Bobj parent);
	}

	/**
	 * interface for a context level trap - these get called when a
	 * context is entered, exited or deleted
	 */
	static public interface Trap
	{
		/**
		 * called on entry to this level
		 */
		public void begin();
		/**
		 * called on delete of this level
		 */
		public void delete();
		/**
		 * called on exit from this level return true
		 * if you don't want to be removed (i.e. if you
		 * want to be called again)
		 */
		public boolean end();
	}

	/**
	 * interface for a context entry watch point - useful for
	 * debugging.
	 */
	static public interface WatchPoint
	{
		/**
		 * called when an entry is retrieved
		 * 
		 * @param key
		 * @param now
		 */
		public void get(Object key, Object now);
		/**
		 * called when an entry is set
		 * 
		 * @param key
		 * @param old
		 * @param newObject
		 */
		public void set(Object key, Object oldObject, Object newObject);
	}
	static {
		System.out.println(" boo2 loaded, classLoader is <" + ContextTree.class.getClassLoader() + ">");
	}

	protected static ThreadLocal at = new ThreadLocal()
	{
		protected Object initialValue()
		{
			return root;
		}
	};

	protected static ThreadLocal booCache = new ThreadLocal()
	{
		protected Object initialValue()
		{
			return new ContextTreeCache();
		}
	};

	public static Bobj root = new BaseBobj("root");

	protected static boolean contextTreeDebug = false;

	// public for contextTreeDebugging
	public static Bobj _cached_get_located_at = null;

	protected static Object _get(Bobj start, Object key)
	{
		_cached_get_located_at = null;
		Object o = start.get(key);
		if (o != null)
		{
			_cached_get_located_at = start;
			return o;
		}
		int p = start.getNumParents();
		if (p == 1)
			return _get((Bobj) start.getParent(), key);
		if (p == 0)
			return null;

		for (int i = 0; i < start.getNumParents(); i++)
		{
			o = _get((Bobj) start.getParent(i), key);
			if (o != null)
			{
				return o;
			}
		}
		return null;
	}

	public static Object get(Object key, boolean nullIsBad)
	{
		//System.out.println(" getting <"+key+">
		// <"+key.getClass().getName()+">");
		// search for it here, and all parents until we
		// get there

		Bobj aat = getAt();
		Bobj start = aat;
		Bobj guess = start.getCache().lookup(key);
		if (guess != null)
		{
			Object ret = guess.get(key);
			if (ret != null)
				return ret;
		}
		else
		{
			// recur on this
			Object o = _get(aat, key);
			if (o != null)
			{
				if (_cached_get_located_at != aat)
					start.getCache().cacheLocation(key, _cached_get_located_at);
				return o;
			}
		}
		if (nullIsBad)
		{
			System.err.println(" get of " + key + " failed in dir:" + ContextTree.pwd());
			System.err.println("   dir is <" + ContextTree.dir());
			throw new NullPointerException(" looking for get <" + key + "> of class <" + key.getClass().getName() + "> found nothing ");
		}
		return null;
	}

	protected static Bobj getAt()
	{
		return (Bobj) at.get();
	}

	/** package scope - returns children dir of us */
	protected static Iterator getDirIterator()
	{
		return getAt().getChildrenIterator();
	}

	/** package scope - returns entrySet().iterator() */
	protected static Iterator getValueIterator()
	{
		return getAt().getMap().entrySet().iterator();
	}

	protected static Object getWithDirectory(String a)
	{
		Object restore = null;
		if (a.indexOf("/") != -1)
		{
			restore = ContextTree.where();
			int i = a.lastIndexOf("/");
			String to = a.substring(0, i);
			a = a.substring(i + 1, a.length());
			ContextTree.cd(to);
		}
		Object ret = ContextTree.get(a, false);
		//		        System.out.println(" getting :<"+a+"> ->
		// <"+ret+">");

		if (restore != null)
			ContextTree.go((Bobj) restore);
		return ret;
	}

	public static void go(Object o)
	{
		setAt((Bobj) o);
	}

	/**
	 * todo, should be a merge as well
	 */
	public static void load(String pathname)
	{
		try
		{

			ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(pathname))));
			Object o = ois.readObject();
			Bobj b = (Bobj) o;

			root = b;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	/**
	 * handles numbers and iFloatProviders mag of Vec3 or Vec2 or
	 * Vec
	 */
	protected static float resolveNumber(Object o)
	{
		if (o instanceof iObjectProvider) return resolveNumber(((iObjectProvider)o).evaluate());
		if (o instanceof Number)
			return ((Number) o).floatValue();
		if (o instanceof iFloatProvider)
			return (float) ((iFloatProvider) o).evaluate();
		if (o instanceof innards.math.linalg.Vec)
			return (float) ((innards.math.linalg.Vec) o).mag();
		if (o instanceof innards.math.linalg.Vec3)
			return (float) ((innards.math.linalg.Vec3) o).mag();
		if (o instanceof innards.math.linalg.Vec2)
			return (float) ((innards.math.linalg.Vec2) o).mag();
		throw new ClassCastException(" expected something that I could turn into a number, got " + o.getClass().getName() + " instead ");
	}

	public static void save(String pathname)
	{
		try
		{

			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(pathname))));
			oos.writeObject(root);
			oos.close();
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	protected static void setAt(Bobj to)
	{
		getAt().end();
		at.set(to);
		to.begin();
	}

	/**
	 * adds a trap to this context
	 * 
	 * @param trap
	 */
	public static void trap(Trap trap)
	{
		getAt().addTrap(trap);
	}

	// -----------------------------------------------------------

	/**
	 * advanced use only - thisContext will be searched before the
	 * current natural parent
	 * 
	 * @param thisContext
	 */
	static public void addPriorParent(Bobj thisContext)
	{
		thisContext.addChild(getAt());
	}
	/**
	 * advanced use only - creates a 'joinPoint' in the tree. If
	 * you do not understand the contents of joinPoint(...) you
	 * probably don't want to make this call
	 * 
	 * @param name
	 * @param alias
	 */
	static public void alias(String name, Bobj alias)
	{
		Bobj b = (Bobj) getAt().getChild(name);
		if (b == null)
		{
			b = joinPoint(getAt(), alias);
			getAt().addChild(b);
		}
		setAt(b);
	}

	/**
	 * delegate is called if the context doesn't exist
	 * 
	 * @param name
	 * @param delegate
	 */
	static public void begin(String name, iNewContextDelegate delegate)
	{
		//		   if (contextTreeDebug)
		//	System.err.println(" CTREE begin: " + name+"
		// "+getAt());
		Bobj b = (Bobj) getAt().getChild(name);
		if (b == null)
		{
			if (delegate == null)
			{
				b = new BaseBobj(name);

				getAt().addChild(b);
			}
			else
			{
				b = delegate.createAndAttach(name, getAt());
			}
		}
		setAt(b);

	}

	/**
	 * you can do something like
	 * :/hello/something/../something_else/boo/:/hello/somehting_more
	 * <p>
	 * ':' represents the root context, '..' is the parent
	 * directory '/' is the directory separator
	 */
	static public void cd(String s)
	{
		StringTokenizer tok = new StringTokenizer(s, "/");
		while (tok.hasMoreTokens())
		{
			String to = (String) tok.nextToken();
			if (to.equals(":"))
				setAt(root);
			else if (to.equals(".."))
				setAt((Bobj) getAt().getParent());
			else
				begin(to, null);
		}
	}

	/**
	 * like 'cd(String s)' but with no string tokenizer overhead
	 */

	static public void cd(String[] s)
	{
		for (int i = 0; i < s.length; i++)
		{
			if (s[i].equals(":"))
				setAt(root);
			else if (s[i].equals(".."))
				setAt((Bobj) getAt().getParent());
			else
				begin(s[i], null);
		}
	}
	/**
	 * see comment in ContextTreeCache about deleting things
	 * 
	 * @see ContextTreeCache
	 */
	static public void delete(Object name)
	{
		Bobj o = getAt();
		if (name instanceof String)
			if (o.hasChild((String) name))
			{
				Bobj parent = (Bobj) o.removeChild((String) name);
				new BaseTraversalAction()
				{
					protected boolean actionImplementation(iNamedObject node)
					{
						((Bobj) node).delete();
						return true;
					}
				}
				.applyAction(parent);
			}
		o.remove(name);
		((ContextTreeCache) booCache.get()).invalidateKey(name);
	}

	/**
	 * returns a 'directory' listing
	 * 
	 * @return String
	 */
	static public String dir()
	{
		try
		{
			String ret = " directory <" + getAt().getName() + ">\n";
			Iterator i = getAt().getChildrenIterator();
			while (i.hasNext())
			{
				Bobj b = (Bobj) i.next();
				ret += "  (dir) " + b.getName() + " \n";
				//System.out.println("
				// ret = " +
				// ret);

			}
			// and values

			i = getAt().getMap().entrySet().iterator();
			while (i.hasNext())
			{
				Map.Entry e = (Map.Entry) i.next();
				ret += " " + e.getKey() + "   <-   " + e.getValue() + "\n";
			}

			return ret;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * end with no name is not good
	 * 
	 * @deprecated
	 */
	static public void end()
	{
		if (contextTreeDebug)
			System.err.println(" CTREE end");
		setAt((Bobj) getAt().getParent());
		if (at == null)
			throw new IllegalStateException(" end with no begin");
	}

	/**
	 * sets the position in the ContextTree
	 * 
	 * warning: Traps are not called as one might expect during
	 * entry and exit using where() and go() calls. these calls are
	 * for efficiency only and their use is discouraged
	 * 
	 * @return Bobj
	 */
	static public ContextTreeInternals.Bobj go(Bobj o)
	{
		ContextTreeInternals.Bobj r = getAt();
		setAt((Bobj) o);
		return r;
	}

	/**
	 * 
	 * returns a Bobj, that will always have the same contents as
	 * 'delegateTo', but will have a different parent (see alias)
	 *  
	 */
	static protected Bobj joinPoint(final Bobj parent, final Bobj delegateTo)
	{
		return (Bobj) Proxy.newProxyInstance(ContextTree.class.getClassLoader(), new Class[] { Bobj.class }, new InvocationHandler()
		{
			public Object invoke(Object proxy, Method method, Object[] args)
			{
				try
				{
					// delegate
					// it
					// to
					// delegateTo,
					// unless
					// it
					// is
					// a
					// call
					// to
					// getParent
					if (!method.getName().equals("getParent"))
						return method.invoke(delegateTo, args);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
				return parent;
			}
		});
	}

	/**
	 * @return String - the current 'working directory'
	 */
	static public String pwd()
	{
		String r = "";
		Bobj aat = getAt();
		while (aat != null)
		{
			r = aat.getName() + "/" + r;
			aat = (Bobj) aat.getParent();
		}
		return r;
	}
	static public Bobj where()
	{
		return getAt();
	}
}
