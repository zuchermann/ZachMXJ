package innards.namespace.factory;

import innards.*;

/**
   a wrapper for another prodution rule that will only create one thing
   once, and for all subsequent times, return the same thing.
   <p>
   usefull, for example, if you only want there to be one camera in the
   world
  */
  
public class UniqueFactoryProduction extends NamedObject implements iProduction
{
    private Object _made = null;
    iProduction wrapped;

    public UniqueFactoryProduction(iProduction wrap)
    {
        super("unique <"+wrap.toString()+">");
        wrapped = wrap;
    }
    public void reset(){_made = null;}
        
    public Object produce(Object params)
    {
        if (_made == null)
            return _made = wrapped.produce(params);
        else
            return _made;
    }
}
 