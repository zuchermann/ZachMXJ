package innards.namespace.factory;
import innards.*;
import innards.debug.*;

import java.io.*;

public abstract class BaseFactoryProduction 
    extends NamedObject
    implements iProduction
{
    public BaseFactoryProduction(){super("unname");}
    public BaseFactoryProduction(String nam){super(nam);}
    abstract public Object produce(Object parameters) throws IllegalArgumentException;
    public String description;
    protected void setDescription(String s){description = s;};
    public String toLongString(){return description;}
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException
    {
        out.defaultWriteObject();
        
        Debug.doReport("factory"," SERIAL: writing producer ");
        Debug.doReport("factory", " SERIAL:     "+description);
        
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException
    {
        try{
            in.defaultReadObject();
            Debug.doReport("factory", " SERIAL: reading producer ");
            Debug.doReport("factory"," SERIAL:     "+description);
        }
        catch(ClassNotFoundException ex)
        {
            Debug.doAssert(false," exception in readObject <"+ex+">");
        }
               
    }
}
