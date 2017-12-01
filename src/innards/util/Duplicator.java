package innards.util;

import innards.namespace.rhizome.metavariable.MetaMutableNumber;

import java.lang.reflect.*;
import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * utility for helping implement copy() methods for classes, see test.innards.util.T_Duplicator for example use
 * @author marc
 */
public class Duplicator
{
	Object thisRoot;
	Object[] constructionParameters;

	public Duplicator(Object thisRoot, Object[] constructionParameters)
	{
		this.thisRoot= thisRoot;
		this.constructionParameters= constructionParameters;
		resolveFields();
	}

	public Duplicator subclass(Object thisNow, Object[] constructionParameters)
	{
		this.thisRoot= thisNow;
		this.constructionParameters= constructionParameters;
		resolveFields();
		return this;
	}
	static public WrappedField field(String name)
	{
		WrappedField w= new WrappedField();
		w.name= name;
		return w;
	}

	public Object duplicate(Object thisNow)
	{
		Class classToDuplicate= thisNow.getClass();
		Constructor[] constructors= classToDuplicate.getConstructors();
		Constructor found= null;
		for (int i= 0; i < constructors.length; i++)
		{
			Class[] params= constructors[i].getParameterTypes();
			if (params.length == constructionParameters.length)
			{
				boolean passed= true;
				for (int m= 0; m < params.length; m++)
				{
					if (!(constructionParameters == null
						|| (params[m].isAssignableFrom(constructionParameters[m].getClass()) || samePrimative(params[m], constructionParameters[m].getClass()))
						|| sameWrappedField(params[m], constructionParameters[m])))
					{
						passed= false;
						break;
					}
				}
				if (passed)
				{
					found= constructors[i];
					break;
				}
			}
		}
		if (found != null)
		{
			try
			{
				return found.newInstance(unwrapParameters(thisNow, constructionParameters));
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
		}
		throw new IllegalArgumentException(
			" duplicate failed, looking for <"
				+ Arrays.asList(constructionParameters)
				+ "> out of <"
				+ Arrays.asList(constructors)
				+ "> \n you need a call to someting like duplicator = duplicator.subclass(this, new Object[]{construction, parameters, here}); in your subclass of <"
				+ thisRoot.getClass()
				+ ">");
	}

	protected boolean sameWrappedField(Class c1, Object wr)
	{
		if (!(wr instanceof WrappedField))
			return false;
		return c1.isAssignableFrom(((WrappedField) wr).field.getType()) || samePrimative(c1, ((WrappedField) wr).field.getType());
	}

	static public class WrappedField
	{
		public Field field;
		public String name;
	}

	protected void resolveFields()
	{
		for (int i= 0; i < constructionParameters.length; i++)
		{
			if (constructionParameters[i] instanceof WrappedField)
			{
				try
				{
					((WrappedField) constructionParameters[i]).field=ReflectionTools.getField(thisRoot.getClass(), ((WrappedField)constructionParameters[i]).name);
				} catch (SecurityException e)
				{
					throw new IllegalArgumentException(
						" couldn't find field <" + (((WrappedField) constructionParameters[i]).name) + "> in <" + thisRoot + "> <" + thisRoot.getClass() + ">");
				}
			}
		}
	}

	protected Object[] unwrapParameters(Object This, Object[] list) throws IllegalArgumentException, IllegalAccessException
	{
		Object[] r= new Object[list.length];
		for (int i= 0; i < r.length; i++)
		{
			if (list[i] instanceof WrappedField)
			{
				((WrappedField) list[i]).field.setAccessible(true);
				r[i]= ((WrappedField) list[i]).field.get(This);
							}
			else
			{
				r[i] = list[i];
			}
		}
		return r;
	}

	protected boolean samePrimative(Class c1, Class c2)
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

}