package innards.namespace.factory;

import innards.*;

import java.lang.reflect.*;
import java.util.HashMap;

/**
    the simplest production rule that you can think of. Takes a (fully qualified) class that is
    going to be constructed 
    <p>
    see comment in Factory
    @see innards.namespace.factory.Factory
    @author marc, naimad
    */
public class SimpleFactoryProduction extends NamedObject implements iProduction
{
	private Class createThis;
	private Constructor useThisConstructor = null;
	private boolean allowDifferentClasses;

	/**
	    @param createThisClass the (fully qualified, using $ notation for inner classes) classname of the
	        class that is going to do the creation 
	    @param parameterClass the (fully qualified, etc..) classname of the class that you expect the parameter
	        to be
	    @exception IllegalArgumentException if this couldn't find the class coresponding to 'createThisClass' <i>or</i>
	        we couldn't find a one parameter constructor in that class that takes a parameter of class 'parameterClass'
	        as its parameter 
	            
	    */
	public SimpleFactoryProduction(String createThisClass, String parameterClass) throws IllegalArgumentException
	{
		super("<" + createThisClass + "(" + parameterClass + ")");
		try
		{
			this.createThis = Class.forName(createThisClass);
		} catch (ClassNotFoundException ex)
		{
			throw new IllegalArgumentException(" couldn't find class <" + createThisClass + "> in SimpleFactoryProducer ");
		}
		useThisConstructor = findConstructor(parameterClass);
		allowDifferentClasses = false;
		checkConstructor();
	}
	/**
	    this is like SimpleFactoryProduction(String createThisClass, String parameterClass), but will try to find
	    the correct constuctor when produce is called. This lets you have the same class with different
	    constructors for different classes of parameters.
	        
	    @param createThisClass the (fully qualified, using $ notation for inner classes) classname of the
	        class that is going to do the creation 
	    @exception IllegalArgumentException if this couldn't find the class coresponding to 'createThisClass'     
	
	    
	*/
	public SimpleFactoryProduction(String createThisClass)
	{
		super("<" + createThisClass + "(...)");
		try
		{
			this.createThis = Class.forName(createThisClass);
		} catch (ClassNotFoundException ex)
		{
			throw new IllegalArgumentException(" couldn't find class <" + createThisClass + "> in SimpleFactoryProducer ");
		}
		useThisConstructor = null;
		allowDifferentClasses = true;
	}

	/**
	    this is like SimpleFactoryProduction(String createThisClass, String parameterClass), but will try to find
	    the correct constuctor when produce is called. This lets you have the same class with different
	    constructors for different classes of parameters. it is exactly like SimpleFactoryProduction(String createThisClass)
	    but taking a class rather than a string
	        
	    @param createThisClass the (fully qualified, using $ notation for inner classes) classname of the
	        class that is going to do the creation 
	*/

	public SimpleFactoryProduction(Class createThis)
	{
		super("<" + createThis.getName() + "(...)");
		this.createThis = createThis;
		useThisConstructor = null;
		allowDifferentClasses = true;
	}

	/**
	    do the production, and return it, or throw an IllegalArgumentException
	        
	    @param parameters is the object whose class we look for, if we aren't doing the constructor
	        resolution at production-time then if (! the constructor's parameter .isAssignableFrom(parameters.getClass()))
	        then we'll throw an IllegalArgumentException. If we are doing constructor resolution then we'll try to find
	        one where that condition holds, and if we fail, we'll throw an exception all the same
	        <p>
	        somebody ought to check the order that constructors are returned. if it is consistant than you can do
	        <pre>
	        public class myclass
	        {
	            public myclass(Object o){...}
	            public myclass(Float f){...}                    
	        }
	        </pre>
	        and expect the Float version to be called if you get a 'Float f', otherwise you might get Object being
	        called if it is returned in Class.GetConstructors first.
	        <p>
	        (incidently
	        <pre>
	        public class muclass
	        {
	            public myclass(String o){...}
	            public myclass(Float f){...}                                        
	        }
	        </pre>
	        will always work)
	    */
	public Object produce(Object parameters) throws IllegalArgumentException
	{
		if (allowDifferentClasses)
		{
			useThisConstructor = findConstructor(parameters.getClass().getName());
		}
		checkConstructor();
		if (!allowDifferentClasses)
		{
			if (!(useThisConstructor.getParameterTypes())[0].isAssignableFrom(parameters.getClass()))
			{
				throw new IllegalArgumentException(
					" tried to create a <"
						+ createThis.getName()
						+ ">) "
						+ "by passing in a parameter of class <"
						+ parameters.getClass().getName()
						+ ">, this SimpleFactoryProduction was expecting a <"
						+ (useThisConstructor.getParameterTypes())[0].getName()
						+ ">");
			}
		}

		Object[] args = { parameters };
		Object instance = null;
		try
		{
			instance = useThisConstructor.newInstance(args);
		} catch (InstantiationException ex)
		{
			throw new IllegalArgumentException(" problem newInstance'ing constructor in SimpleFactoryProducer(" + parameters + ") embedded exception is <" + ex + ">");
		} catch (InvocationTargetException ex)
		{
			ex.getTargetException().printStackTrace();
			throw new IllegalArgumentException(
				" problem newInstance'ing constructor in SimpleFactoryProducer(" + parameters + ") embedded exception is <" + ex + "> target <" + ex.getTargetException() + ">");
		} catch (IllegalAccessException ex)
		{
			throw new IllegalArgumentException(" problem newInstance'ing constructor in SimpleFactoryProducer(" + parameters + ") embedded exception is <" + ex + ">");
		}

		return instance;
	}

	protected HashMap constructorCache = null;
	
	Constructor findConstructor(String paramClassname)
	{
		// try to find a one parameter constructor of Class 'createThis' that
		// can handle paramExample
		//report("trying to find constructor for <"+paramClassname+">");
		if (constructorCache == null) constructorCache = new HashMap();
		
		if (constructorCache.containsKey(paramClassname))
		{
			return (Constructor)constructorCache.get(paramClassname);
		}

		Constructor[] con = createThis.getConstructors();
		Class paramClass = null;
		try
		{
			paramClass = Class.forName(paramClassname);
		} catch (ClassNotFoundException ex)
		{
		}
		if (paramClass == null)
			return null;

		for (int i = 0; i < con.length; i++)
		{
			Class[] params = con[i].getParameterTypes();
			if (params.length == 1)
			{
				if (params[0].isAssignableFrom(paramClass))
				{
					constructorCache.put(paramClassname, con[i]);
					return con[i];
				}
			}
		}
		return null;
	}

	void checkConstructor() throws IllegalArgumentException
	{
		if (useThisConstructor == null)
			throw new IllegalArgumentException(
				" couldn't find a one parameter constructor in class "
					+ createThis.getName()
					+ " perhaps there is one, but it doesn't take the correct parameters "
					+ " therfore, we can't use SimpleFactoryProducer.");
	}
}
