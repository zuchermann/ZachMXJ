package innards.namespace.factory;

import java.util.*;
import innards.*;

/**
    a factory is something that lets you make calls like:
    <p>
    Food todaysFruit = (Food)foodFactory.produce("fruit",new MyFoodParameters("banana"));
    <p>
    (nomenclature: foodFactory.produce(Object identifier, Object parameters))
    <p>
    a factory needs 'production rules' i.e. mappings of the string "fruit" to something that
    knows how to return food.
    <p>
    three examples of why this is a good thing:
    <p>
    one of the ideas is that if I come along with an all-new-and-improved fruit class then I can plug
    it into the factory and everybody is none-the-wiser.
    <p>
    another one of the ideas is that you can ask a different factory for a "fruit" and get a
    fruit that is appropriate for that factory (i.e. you can ask a different GraphicsRenderer for a
    "transform" and get a iTransform that is appropriate for use in that GraphicsRenderer)
    <p>
    another idea is that if I want all those "funkyInputDevices" to turn into buttons, then I can do it
    with one call, in one place, and that is that. (note this functionality requires an additional convention
    which hasn't been decided yet. but, it will be)
    <p>
    this Factory class implements most of the meat for creating things like this.  Simply stated, Factory is
    a hashtable of identifier objects ("fruit") vs Vectors of iProduction production rule objects.  When you call produce,
    the Vector corresponding to the identifier Object is traversed backwards until an Object is successfully produced using
    the specified paramters Object.  if no Object is successfully produced, the factory attempts to use
    the default production rule, which can be specified using the setDefaultProduction method.
    <p>
    The class SimpleFactoryProduction is a useful production rule that lets you supply
    a (fully qualified)class that has a constructor that takes the parameters. If you let it (see
    associated docs) it will try to find the constructor that takes a type that is the parameters.
    This lets you have different constructors for different classes of parameter. For example,
    <pre>
    public class Banana
    {
        public Banana(BananaParameters params)
        {
            ....
        }
        public Banana(String name)
        {
            ....
        }
    }            
    </pre>
    <code>Food todaysFruit = (Food)foodFactory.produce("fruit",new BananaParameters("curvy"));</code>
    <p>
    would call the  <code>Banana(BananaParameters params)</code> constructor
    <p>
    whereas :
    <p>
    <code>Food todaysFruit = (Food)FoodFactory.produce("fruit","my banana");</code>
    <p>
    would call the  <code>Banana(String name) </code>constructor
    <p>
        ---
    <p>
    I've been thinking. perhaps this should extend Hashtable.
    <p>
    Thatway, it would be a collection, and people could .registerCollection(
    on it. That would be <i>the way</i> to expose the factory mechanism
    to other people.
    <p>
    We need an iProduction that you can install that contains
    an Overwrite table, to handle cases where we want to say:
    <p>
    'when you create an object of name (i.e. parameter) "marc" make it
    a DebugFunkyMarcObject (instead of a BoringDefaultMarcObject). Otherwise
    , if it isn't called "marc" just do what you normally do.'
    <p>
    this will a) make an awful lot of our 'delegate tendancies' go away and
    b) make some of the reasons not to encapsulate and reuse evaporate.
    <p>
    I'd still like to see a convention for specifying the 'creation context'
    that was more profound than 'name' to make this a little more useful. I'm
    working on it - this is probably a python thing
    <p>
    
    @see innards.namespace.factory.iProduction
    @see innards.namespace.factory.SimpleFactoryProduction
    @author marc, naimad
*/

public class Factory extends NamedObject
{
	Hashtable theFactory = new Hashtable();

	iProduction defaultProduction = null;

	public Factory(String name)
	{
		super(name);
		theFactory = new Hashtable();
	}

	/**
	    adds producer against a identifier
	    
	    @param identifier would be "fruit" in the example given at the start
	    @param prod would be an iProduction that you want to delegate the task of generating fruit for, often a SimpleFactoryProduction
	    */
	public void addProduction(Object identifier, iProduction prod)
	{
		Vector v = (Vector) (theFactory.get(identifier));
		if (v == null)
		{
			v = new Vector();
			theFactory.put(identifier, v);
		}
		v.addElement(prod);
	}

	public void setDefaultProduction(iProduction prod)
	{
		defaultProduction = prod;
	}

	/** 
	    does the production or throws IllegalArgumentException if you made a mistake
	    <p>
	    this performes an action on a bit of the factory graph, finding the deepest producer willing
	    to produce something. this lets you perform complex subverting things with factories
	    <p>
	    @param identifier some magic string for the thing that you want to create 
	    @param parameters parameters for the creation of it
	    @return the thing produced, might be null - ought to check it 
	    @exception if it couldn't produce it for a 'bad reason' (rather than null, which is for a non-'exceptional' reason)
	    
	    */
	public Object produce(Object identifier, Object parameters) throws IllegalArgumentException
	{
		Vector productions = (Vector) (theFactory.get(identifier));
		if (productions == null)
		{
			if (defaultProduction == null)
				throw new IllegalArgumentException(" couldn't find a Factory Production for identifier <" + identifier + ">");
			else
			{
				return defaultProduction.produce(parameters);
			}
		}

		Object o = null;
		boolean successful = false;
		iProduction ptemp;
		for (int c = productions.size() - 1; c >= 0 && !successful; c--)
		{
			ptemp = (iProduction) (productions.elementAt(c));
			try
			{
				o = ptemp.produce(parameters);
				if (o != null)
					successful = true;
			} catch (UtilityFactoryProductionException ex)
			{
			}
		}
		if (!successful)
		{
			if (defaultProduction == null)
				throw new IllegalArgumentException("Invalid arguments for production " + identifier);
			else
				return defaultProduction.produce(parameters);
		} else
			return o;
	}

	/** Ask if you're able to produce this thing. */
	public boolean canProduce(Object identifier)
	{
		Vector productions = (Vector) (theFactory.get(identifier));
		return (productions != null);
	}

	/** this is a safe, runtime exception that is ignored by the creation
	    search action - this is useful when you want convience methods
	    to punt creation automatically see Builder.needed(...)
	    */
	static public class UtilityFactoryProductionException extends RuntimeException
	{
		public UtilityFactoryProductionException()
		{
		}
	}
}