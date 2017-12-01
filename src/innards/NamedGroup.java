package innards;

import java.util.*;

/**
 * Base class for creating a <code> NamedObject </code> that has other
 * NamedObjects as children.
 * 
 * @see innards.iNamedGroup
 * @see innards.NamedObject
 * @author synchar
 */
public class NamedGroup extends NamedObject implements iNamedGroup
{
	protected ArrayList _children;
	private List _children_unmodifiable;
	boolean immutable= false; // Can we add/remove children?
	boolean allowDuplicateChildren= false;

	public NamedGroup(String name)
	{
		super(name);
		_children= new ArrayList();
		_children_unmodifiable= Collections.unmodifiableList(_children);
	}

	public NamedGroup(Key key)
	{
		super(key);
		_children= new ArrayList();
		_children_unmodifiable= Collections.unmodifiableList(_children);
	}

	public NamedGroup()
	{
		super("Unnamed NamedGroup");
		_children= new ArrayList();
		_children_unmodifiable= Collections.unmodifiableList(_children);
	}

	public void setImmutable(boolean b)
	{
		immutable= b;
	}

	/**
	 * 
	 * Returns an iterator to an unmodifiable list of children
	*/
	public Iterator getChildrenIterator()
	{
		return _children_unmodifiable.iterator();
	}

	/**
	 * returns an unmodifiable list of children
	 */
	public List getChildrenList()
	{
		return _children_unmodifiable;
	}

	public void removeAllChildren()
	{
		_children.clear();
	}

	/**
	    this is useful for comparing hierarchies and things. Can only be called if 
	    immutable is set to false.
	    */
	public void sortChildrenAlphabetically()
	{
		if (immutable)
			throw new SecurityException("NamedGroup.sortChildrenAlphabetically, I'm immutable!");
		Collections.sort(_children, new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				NamedObject no1= (NamedObject) o1;
				NamedObject no2= (NamedObject) o2;
				return no1.getName().compareTo(no2.getName());
			}
		});
	}

	/** 
	    If the child is not already in my list, add it, and set its parent to me. Can only be called if 
	    immutable is set to false.
	*/
	public void addChild(iNamedObject child)
	{
		if (immutable)
			throw new SecurityException("NamedGroup::Add - Group is immutable!");
		if (!_children.contains(child) || allowDuplicateChildren)
		{
			_children.add(child);
		}
		child.setParent(this);
	}

	/** 
	    Stick the child at the specified index location and move anyone
	    else out of the way.  If the index specified is invalid, that is
	    would produce "holes" in the children, we do not add it and
	    throw the exception. Can only be called if immutable is set to false.
	*/
	public void insertChildAt(iNamedObject child, int index) throws ArrayIndexOutOfBoundsException
	{
		if (immutable)
			throw new SecurityException("NamedGroup::insertChildAt - Group is immutable!");
		if (!_children.contains(child) || allowDuplicateChildren)
			_children.add(index, child);
		child.setParent(this);
	}

	/** 
	    Return the child with child_name or else null
	      
	    in this implementation, this is not done using hashtables
	    if this becomes an issue, then we need to change the rep
	    in this class from Vector, to hashtable. this, of course,
	    should break nothing, be remember the warnings about 
	    changing names after linking objects
	*/
	public iNamedObject getChild(String child_name)
	{
		Iterator it= getChildrenIterator();
		while (it.hasNext())
		{
			NamedObject ng= (NamedObject) it.next();
			if (ng.getName().equals(child_name))
				return ng;
		}
		return null;
	}

	/**
	    Return the child at the index or null
	     
	    (this currently does not call getChild() but goes to the
	    rep directly )
	*/
	public iNamedObject getChild(int index) throws ArrayIndexOutOfBoundsException
	{
		return (NamedObject) _children.get(index);
	}

	public int getIndexOfChild(iNamedObject child) throws NoSuchElementException
	{
		int ret= _children.indexOf(child);
		if (ret == -1)
			throw new NoSuchElementException("Couldn't find child <" + child + "> as a child of <" + this +">");
		return ret;
	}

	public boolean hasChild(iNamedObject child)
	{
		return _children.indexOf(child) != -1;
	}

	public boolean hasChild(String child)
	{
		return getChild(child) != null;
	}

	public iNamedObject removeChild(iNamedObject child_to_remove) throws NoSuchElementException
	{
		if (immutable)
			throw new SecurityException("NamedGroup::removeChild - Group is immutable!");
		boolean b= _children.remove(child_to_remove);
		if (!b)
			throw new NoSuchElementException("Couldn't find child <" + child_to_remove + "> in removeChild() in <" + this +">");

		child_to_remove.removeParent(this);

		return child_to_remove;
	}

	public iNamedObject removeChild(String child_to_remove) throws NoSuchElementException
	{
		if (immutable)
			throw new SecurityException("NamedGroup::removeChild - Group is immutable!");
		iNamedObject ng= getChild(child_to_remove);
		if (ng == null)
			throw new NoSuchElementException("Couldn't find child <" + child_to_remove + "> in removeChild() in <" + this +">");
		removeChild(ng);
		return ng;
	}

	public int getNumChildren()
	{
		return _children.size();
	}

	protected void setAllowDuplicateChildren(boolean allowDuplicateChildren)
	{
		this.allowDuplicateChildren= allowDuplicateChildren;
	}

	public String toDebugString()
	{
		String retVal= new String("Namespace object <" + getName() + "> has children:  \r\n");

		Iterator iter= getChildrenIterator();
		NamedGroup ptr;

		while (iter.hasNext())
		{
			ptr= (NamedGroup) iter.next();

			retVal= retVal + "\t<" + ptr.getName() + ">\r\n";
		}

		return retVal;
	}
	
	public List sortChildren(Comparator c)
	{
		Collections.sort(_children);
		return getChildrenList();
	}

	/** 
	  Some example code.  Yeah!  We love example code.
	*/

	public static void main(String args[])
	{
		NamedGroup root= new NamedGroup("root");
		NamedGroup A= new NamedGroup("A");
		NamedGroup B= new NamedGroup("B");
		NamedGroup C= new NamedGroup("C");
		NamedGroup D= new NamedGroup("D");
		NamedGroup E= new NamedGroup("E");

		root.addChild(A); // A
		System.out.println("A");
		System.out.println(root.toDebugString());

		root.addChild(B); // A, B
		System.out.println("A, B");
		System.out.println(root.toDebugString());

		root.addChild(C); // A, B, C
		System.out.println("A, B, C");
		System.out.println(root.toDebugString());

		root.addChild(D); // A, B, C, D
		System.out.println("A, B, C, D");
		System.out.println(root.toDebugString());

		root.removeChild(C); // A, B, D
		System.out.println("A, B, D");
		System.out.println(root.toDebugString());

		root.insertChildAt(C, 1); // A, C, B, D
		System.out.println("A, C, B, D");
		System.out.println(root.toDebugString());

		root.removeChild(A);
		System.out.println("C, B, D");
		System.out.println(root.toDebugString());

		root.removeChild(D);
		System.out.println("C, B");
		System.out.println(root.toDebugString());

		System.exit(0);
	}
}
