package innards;

import java.io.*;
import java.util.*;

/**
    Base object for the system.
    @author synchar
*/
public class NamedObject implements iNamedObject
{
	private String name;
	private Key key= null;
	protected ArrayList<iNamedGroup> parents;
	
	public static final boolean allocationStackTraces = false;
	public StackTraceElement[] stacktrace;

	//protected Bobj creationContext;

	/**
	    NamedObject constructor requires you to pass a name as a parameter.
	*/
	public NamedObject(String name)
	{
		this.name= name.intern();
		if (allocationStackTraces)
		{
			stacktrace = new Exception().getStackTrace();
		}
		
		//creationContext = ContextTreeSpecial.where();
	}

	public NamedObject(Key key)
	{
		//this.name= key.toString().intern();
		this.name= key.toString();
		this.key= key;
		if (allocationStackTraces)
		{
			stacktrace = new Exception().getStackTrace();
		}
		//creationContext = ContextTreeSpecial.where();
	}

	/** Gets the "name" in NamedObject */
	public String getName()
	{
		return name;
	}

	protected void setName(String n)
	{
		name= n.intern();
	}
	
	protected void setKey(Key key)
	{
		this.name = key.toString();
		this.key = key;
	}

	public Key getKey()
	{
		if (key == null)
		{
			key= new Key(getName());
		}
		return key;
	}

	/** Sets the parent.  Set it to null to remove the parent. */
	public void setParent(iNamedGroup parent)
	{
		if (parents == null)
		{
			parents = new ArrayList<iNamedGroup>(1);
		}
		parents.add(parent);
	}
	
	public void removeParent(iNamedGroup parent)
	{
		parents.remove(parent);
	}

	/**
	 * @deprecated for backwards compatability - remember back when NamedObjects had only one
	 * parent? returns the first one in the list, so you better want that one. if you don't
	 * use getParents() or getParent(name).
	 */
	public iNamedGroup getParent()
	{
		return parents == null ? null : (iNamedGroup) parents.get(0);
	}
	
	public List<iNamedGroup> getParents()
	{
		return parents;
	}
	
	public iNamedGroup getParent(String name)
	{
		iNamedGroup parent;
		iNamedGroup ret = null;
		for (Iterator<iNamedGroup> iter = parents.iterator(); iter.hasNext();)
			{
				parent = iter.next();
				if(parent.getName().equals(name))
					{
						ret = parent;
						break;
					}
			}
			
			return ret;
	}
	
	public iNamedGroup getParent(int i)
	{
		return (iNamedGroup) parents.get(i);
	}
	
	private String myToString= null;
	public String toString()
	{
		if (myToString == null)
			myToString= new String(this.getClass().getName() + "<" + getName() + ">:" + this.hashCode());
		return myToString;
	}

	private void writeObject(java.io.ObjectOutputStream stream) throws IOException
	{
		System.out.println("Writing out " + getName()+" of class "+getClass().getName());
		stream.defaultWriteObject();
	}
	
	/**
	 * @see java.lang.Object#finalize()
	 */
	/*protected void finalize() throws Throwable

	{
		super.finalize();
		System.out.println(" Named Object <"+this.getName()+"> GC...");
		for(int i=0;i<stacktrace.length;i++)
		{
			System.out.println("    "+stacktrace[i]);
		}
	}

*/	

}
