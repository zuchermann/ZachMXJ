package innards.namespace.factory;

import java.io.*;


public class NullFactoryProduction  extends BaseFactoryProduction
    implements Serializable, iProduction
{
    public NullFactoryProduction(){}
    public NullFactoryProduction(String description){this.setDescription(description);}
    
    public Object produce(Object o2){return null;}
    
    public String toLongString()
    {
        return description;
    }    
}