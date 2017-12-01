package innards.util;

import innards.provider.*;

import java.lang.reflect.*;
import java.util.*;

/**
 * @author marc
 */
public class ReflectionTools
{

	static HashMap methodsCache = new HashMap();

	static public Method[] getAllMethods(Class of)
	{
		Method[] ret = (Method[]) methodsCache.get(of);
		if (ret == null)
		{
			ArrayList methodsList = new ArrayList();
			_getAllMethods(of, methodsList);
			methodsCache.put(of, ret = (Method[]) methodsList.toArray(new Method[0]));
		}
		return ret;
	}

	static protected void _getAllMethods(Class of, List into)
	{
		if (of == null)
			return;
		Method[] m = of.getDeclaredMethods();
		into.addAll(Arrays.asList(m));
		_getAllMethods(of.getSuperclass(), into);
		Class[] interfaces = of.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
			_getAllMethods(interfaces[i], into);
	}

	static HashMap fieldsCache = new HashMap();

	static public Field[] getAllFields(Class of)
	{
		Field[] ret = (Field[]) fieldsCache.get(of);
		if (ret == null)
		{
			List fieldsList = new ArrayList();
			_getAllFields(of, fieldsList);
			fieldsCache.put(of, ret = (Field[]) fieldsList.toArray(new Field[0]));
		}
		return ret;
	}

	static protected void _getAllFields(Class of, List into)
	{
		if (of == null)
			return;
		Field[] m = of.getDeclaredFields();
		into.addAll(Arrays.asList(m));
		_getAllFields(of.getSuperclass(), into);
		Class[] interfaces = of.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
			_getAllFields(interfaces[i], into);
	}

	static HashMap constructorsCache = new HashMap();

	static public Constructor[] getAllConstructors(Class of)
	{
		Constructor[] ret = (Constructor[]) constructorsCache.get(of);
		if (ret == null)
		{
			List constructorsList = new ArrayList();
			_getAllConstructors(of, constructorsList);
			constructorsCache.put(of, ret = (Constructor[]) constructorsList.toArray(new Constructor[0]));
		}
		return ret;
	}

	static protected void _getAllConstructors(Class of, List into)
	{
		if (of == null)
			return;
		Constructor[] m = of.getDeclaredConstructors();
		into.addAll(Arrays.asList(m));
		_getAllConstructors(of.getSuperclass(), into);
		Class[] interfaces = of.getInterfaces();
		for (int i = 0; i < interfaces.length; i++)
			_getAllConstructors(interfaces[i], into);
	}

	static public Field getField(Class from, String called)
	{
		Field[] ff = getAllFields(from);
		for (int i = 0; i < ff.length; i++)
		{
			if (ff[i].getName().equals(called))
				return ff[i];
		}
		return null;
	}

	/**
	 * runs the default, no-arg constrctor of this plane. Returns only null (and a .printStackTrace()) on failure
	 */
	public static Object instantiate(Class clazz)
	{
		try
		{
			Constructor c = clazz.getConstructor(new Class[] {
			});
			Object i = c.newInstance(new Object[] {
			});
			return i;
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	public static Object instantiate(Class clazz, Object[] parameters)
	{
		try
		{
			Constructor[] c = clazz.getConstructors();
			for (int m = 0; m < c.length; m++)
			{
				Class[] t = c[m].getParameterTypes();
				if (t.length == parameters.length)
				{
					boolean good = true;
					for (int l = 0; l < t.length; l++)
					{
						if (!((parameters[l] == null) || t[l].isAssignableFrom(parameters[l].getClass()) || samePrimative(t[l], parameters[l].getClass())))
						{
							good = false;
							break;
						}
					}
					if (good)
					{
						return c[m].newInstance(parameters);
					}
				}
			}
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param classes
	 * @param method
	 * @return
	 */
	public static Method findMethodWithParameters(Class[] classes, Method[] method)
	{
		for (int i = 0; i < method.length; i++)
		{
			Class[] c = method[i].getParameterTypes();
			if (c.length == classes.length)
			{
				boolean cool = true;
				for (int m = 0; m < c.length; m++)
				{
					if (!classes[m].isAssignableFrom(c[m]))
					{
						cool = false;
						break;
					}
				}
				if (cool)
				{
					method[i].setAccessible(true);
					return method[i];
				}
			}
		}
		return null;
	}

	public static boolean samePrimative(Class c1, Class c2)
	{
		if ((c1 == Integer.TYPE) && (c2 == Integer.class))
			return true;
		if ((c1 == Float.TYPE) && (c2 == Float.class))
			return true;
		if ((c1 == Double.TYPE) && (c2 == Double.class))
			return true;
		if ((c1 == Boolean.TYPE) && (c2 == Boolean.class))
			return true;
		if ((c1 == Short.TYPE) && (c2 == Short.class))
			return true;
		if ((c1 == Character.TYPE) && (c2 == Character.class))
			return true;
		if ((c1 == Byte.TYPE) && (c2 == Byte.class))
			return true;
		return false;
	}

	public static Object illegalGetObject(Object client, String string)
	{
		Field[] f = getAllFields(client.getClass());
		for (int i = 0; i < f.length; i++)
		{
			if (f[i].getName().equals(string))
			{
				f[i].setAccessible(true);
				try
				{
					return f[i].get(client);
				} catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public static void illegalSetObject(Object o, String string, Object to)
	{
		Field[] f = getAllFields(o.getClass());
		for (int i = 0; i < f.length; i++)
		{
			if (f[i].getName().equals(string))
			{
				f[i].setAccessible(true);
				try
				{
					f[i].set(o, to);
				} catch (IllegalArgumentException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**  @param called
	/**  @return */
	public static Method findFirstMethodCalled(Class from, String called)
	{
		Method[] methods = getAllMethods(from);
		for (int i = 0; i < methods.length; i++)
		{
			if (methods[i].getName().equals(called)) return methods[i];
		}
		return null;
	}

	/**
	 * get the method for this class or its superclass that is public and takes 1 param of type paramType.
	 * @param c the class with the method or a subclass of that class
	 * @param name the name of the method
	 * @param paramType the parameter the method takes.
	 * @return the method, or null if not found.
	 * @see innards.util.ReflectionTools#getPublicMethodForNameAndParams(java.lang.Class, java.lang.String, java.lang.Class[]) see getPublicMethodForNameAndParams for functions with non 1 params number
	 */
	public static Method getPublicMethodForNameAndParam(Class c, String name, Class paramType)
	{
		while (c != null)
		{
			Method[] m= c.getMethods();
			for (int i= 0; i < m.length; i++)
			{
				if (m[i].getName().equals(name))
				{
					//check if it accepts param.
					Class[] params= m[i].getParameterTypes();
					if (params.length == 1 && params[0].isAssignableFrom(paramType))
					{
						m[i].setAccessible(true);
						return m[i];
					}
				}
			}
			c= c.getSuperclass();
		}
		return null;
	}

	/**
	 * get the method for this class (or its superclass) that is public and takes params paramTypes.
	 * @param c the class with the method, or a subclass of that class.
	 * @param name the name of the method.
	 * @param paramTypes the parameter types: null or empty for void params functions.
	 * @return the method, or null if not found.
	 */
	public static Method getPublicMethodForNameAndParams(Class c, String name, Class[] paramTypes)
	{
		int numParams = 0;
		if(paramTypes!=null){
			numParams = paramTypes.length;
		}
		while (c != null)
		{
			Method[] m= c.getMethods();
			for (int i= 0; i < m.length; i++)
			{
				if (m[i].getName().equals(name))
				{
					//check if it accepts param.
					Class[] params= m[i].getParameterTypes();
					if (params.length == numParams)
					{
						boolean ok = true;
						for(int k = 0; k < numParams; k++){
							if(!params[k].isAssignableFrom(paramTypes[k])){
								ok = false;
								break;
							}
						}
						if(ok){
							m[i].setAccessible(true);
							return m[i];
						}
					}
				}
			}
			c= c.getSuperclass();
		}
		return null;
	}

	/**
	 * for encapsulating a float providing function as an iFloatProvider using reflection.
	 * @param o the object with the method
	 * @param methodName the name of the method
	 * @return an iFloatProvider that returns the float from the method, or null if not possible
	 */
	public static iFloatProvider getFloatProvider(final Object o, String methodName){
		final Method m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
		if(m!=null){
			return new iFloatProvider(){
				Object[] args = new Object[0];
				public float evaluate(){
					try{
						return ((Float)m.invoke(o, args)).floatValue();
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
					return 0;
				}
			};
		}
		return null;
	}

	/**
	 * for encapsulating a boolean providing function as an iBooleanProvider using reflection.
	 * @param o the object with the method
	 * @param methodName the name of the method
	 * @return an iBooleanProvider that returns the boolean from the method, or null if not possible
	 */
	public static iBooleanProvider getBooleanProvider(final Object o, String methodName){
		final Method m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
		if(m!=null){
			return new iBooleanProvider(){
				Object[] args = new Object[0];
				public boolean provideBoolean(){
					try{
						return ((Boolean)m.invoke(o, args)).booleanValue();
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
					return false;
				}
			};
		}
		return null;
	}

	/**
	 * for encapsulating a method as a float acceptor using reflection.
	 * @param o the object with the method
	 * @param methodName the method name
	 * @param allowVoidArgs true allows silent success even if method takes no float. (like maybe you don't care because you want it called anyway)
	 * @return an iFloatAcceptor that may or may not (see allowVoidArgs) pass your float through, or null if none found.
	 */
	public static iFloatAcceptor getFloatAcceptor(final Object o, String methodName, boolean allowVoidArgs){
		boolean sf = true;
		Method m = getPublicMethodForNameAndParam(o.getClass(), methodName, float.class);
		if(m == null && allowVoidArgs){
			m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
			sf = false;
		}
		final boolean sendFloat = sf;
		final Method method = m;
		if(method!=null){
			return new iFloatAcceptor(){
				Object[] args = new Object[sendFloat?1:0];
				public void set(float f){
					try{
						if(sendFloat){
							args[0] = new Float(f);
						}
						method.invoke(o, args);
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}

	/**
	 * for encapsulating a method as an iObjectAcceptor using reflection.
	 * @param o the object with the method
	 * @param methodName the method name
	 * @param argumentType the type of object the class should accept.  may be Object.class for most permissive.
	 * @param allowVoidArgs true allows silent success even if method takes no arg. (like maybe you don't care because you want it called anyway)
	 * @return an iObjectAcceptor that may or may not (see allowVoidArgs) pass your objects through, or null if none found.
	 */
	public static iObjectAcceptor getObjectAcceptor(final Object o, String methodName, Class argumentType, boolean allowVoidArgs){
		boolean so = true;
		Method m = null;
		if(argumentType!=null){
			m = getPublicMethodForNameAndParam(o.getClass(), methodName, argumentType);
		}
		if(m == null && allowVoidArgs){
			m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
			so = false;
		}
		final boolean sendObject = so;
		final Method method = m;
		if(method!=null){
			return new iObjectAcceptor(){
				Object[] args = new Object[sendObject?1:0];
				public void set(Object arg){
					try{
						if(sendObject){
							args[0] = arg;
						}
						method.invoke(o, args);
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}


	/**
	 * for encapsulating a method as a boolean acceptor.
	 * @param o the object with the method
	 * @param methodName the method name
	 * @param allowVoidArgs true allows silent success even if method takes no boolean. (like maybe you don't care because you want it called anyway)
	 * @return an iBooleanAcceptor that may or may not (see allowVoidArgs) pass your boolean through, or null if none found.
	 */
	public static iBooleanAcceptor getBooleanAcceptor(final Object o, String methodName, boolean allowVoidArgs){
		boolean sb = true;
		Method m = getPublicMethodForNameAndParam(o.getClass(), methodName, boolean.class);
		if(m == null && allowVoidArgs){
			m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
			sb = false;
		}
		final boolean sendBoolean = sb;
		final Method method = m;
		if(method!=null){
			return new iBooleanAcceptor(){
				Object[] args = new Object[sendBoolean?1:0];
				public void set(boolean b){
					try{
						if(sendBoolean){
							args[0] = new Boolean(b);
						}
						method.invoke(o, args);
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}

	/**
	 * for encapsulating an int, float, boolean, void, or String accepting method as a String acceptor.
	 * @param o the object with the method
	 * @param methodName the method name
	 * @return an iStringAcceptor that may or may not pass your String through, or null if none found.
	 */
	public static iStringAcceptor getStringConverter(final Object o, String methodName){
		Class[] someClasses = new Class[]{String.class,  int.class,  float.class, boolean.class};
		Class[] someObjectClasses = new Class[]{String.class,  Integer.class,  Float.class, Boolean.class};
		for(int i = 0; i < someClasses.length; i++){
			final Method method = getPublicMethodForNameAndParam(o.getClass(), methodName, someClasses[i]);
			if(method!=null){
				try{
					final Constructor constructor = someObjectClasses[i].getConstructor(new Class[]{String.class});
					return new iStringAcceptor(){
						Object[] args = new Object[1];
						Object[] consArgs = new Object[1];
						public void acceptString(String s){
							try{
								consArgs[0] = s;
 								args[0] = constructor.newInstance(consArgs);
								method.invoke(o, args);
							}catch(Exception ex){
								ex.printStackTrace();
							}
						}
					};
				}catch(NoSuchMethodException e){
					e.printStackTrace();
				}
			}
		}
		final Method method = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
		if(method!=null){
			return new iStringAcceptor(){
				Object[] args = new Object[0];
				public void acceptString(String s){
					try{
						method.invoke(o, args);
					}catch(IllegalAccessException e){
						e.printStackTrace();
					}catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}

	/**
	 * for encapsulating a method as a String acceptor.
	 * @param o the object with the method
	 * @param methodName the method name
	 * @param allowVoidArgs true allows silent success even if method takes no String. (like maybe you don't care because you want it called anyway)
	 * @return an iStringAcceptor that may or may not (see allowVoidArgs) pass your String through, or null if none found.
	 */
	public static iStringAcceptor getStringAcceptor(final Object o, String methodName, boolean allowVoidArgs){
		boolean ss = true;
		Method m = getPublicMethodForNameAndParam(o.getClass(), methodName, String.class);
		if(m == null && allowVoidArgs){
			m = getPublicMethodForNameAndParams(o.getClass(), methodName, null);
			ss = false;
		}
		final boolean sendString = ss;
		final Method method = m;
		if(method!=null){
			return new iStringAcceptor(){
				Object[] args = new Object[sendString?1:0];
				public void acceptString(String s){
					try{
						if(sendString){
							args[0] = s;
						}
						method.invoke(o, args);
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}


	/**
	 * Useful for encapsulating a method that you want to call.<br>
	 * Calls getRunnableForMethodSignature with args to allow finding any function with methodName, regardless of args.
	 * @param o the object to find methodName in
	 * @param methodName the name of the method.
	 * @return a runnable that calls your method on run, or null if none could be found.
	 * @see innards.util.ReflectionTools#getRunnableForMethodSignature getRunnableForMethodSignature
	 */
	public static Runnable getRunnableForMethodNamed(Object o, String methodName){
		return getRunnableForMethodSignature(o, methodName, null, null);
	}

	/**
	 * Useful for encapsulating a method that you want to call.<br>
	 * Calls getRunnableForMethodSignature with args to allow finding only void args functions with methodName.
	 * @param o the object to find methodName in
	 * @param methodName the name of the method.
	 * @return a runnable that calls your method on run, or null if none could be found.
	 * @see innards.util.ReflectionTools#getRunnableForMethodSignature getRunnableForMethodSignature
	 */
	public static Runnable getRunnableForVoidMethodNamed(Object o, String methodName){
		return getRunnableForMethodSignature(o, methodName, new Class[]{}, new Object[]{});
	}

	/**
	 * Useful for encapsulating a method that you want to call.
	 * @param o the object to call the method on
	 * @param methodName the name of the method
	 * @param argClasses the classes the method takes.  (null if you don't care and just want to find by name)
	 * @param args the arguments you want passed to the method (null if you don't care; a valid set of null args will be passed in)
	 * @return a runnable that executes your method on "run", or null if none found.
	 */
	public static Runnable getRunnableForMethodSignature(final Object o, String methodName, Class[] argClasses, Object[] args){
		Method m;
		if(argClasses != null){
			m = getPublicMethodForNameAndParams(o.getClass(), methodName, argClasses);
		}else{
			m = findFirstMethodCalled(o.getClass(), methodName);
			args = new Object[m.getParameterTypes().length];
		}
		final Object[] theArgs = args;
		final Method method = m;
		if(m!=null){
			return new Runnable(){
				public void run(){
					try{
						method.invoke(o, theArgs);
					} catch(IllegalAccessException e){
						e.printStackTrace();
					} catch(InvocationTargetException e){
						e.printStackTrace();
					}
				}
			};
		}
		return null;
	}
}