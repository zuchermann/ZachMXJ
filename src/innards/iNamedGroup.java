package innards;

import java.util.*;

/** 
 * An interface for creating an <code> iNamedObject </code> that has other
 * NamedObjects as children.
 * 
 * @see innards.iNamedObject
 * @see innards.NamedObject
 * @see innards.NamedGroup
 * @author synchar
 */
public interface iNamedGroup extends iNamedObject
{
	/**
	    Return an iterator with all of my children in the proper order.
	*/
	public Iterator<iNamedGroup> getChildrenIterator();

	public List<iNamedGroup> getChildrenList();

	/** 
	    If the child is not already in my list, add it, and set its parent to me.
	*/
	public void addChild(iNamedObject child);

	public void insertChildAt(iNamedObject child, int index) throws ArrayIndexOutOfBoundsException;
	/** 
	    Return the child with child_name or else null
	      
	    in this implementation, this is not done using hashtables
	    if this becomes an issue, then we need to change the rep
	    in this class from Vector, to hashtable. this, of course,
	    should break nothing, be remember the warnings about 
	    changing names after linking objects
	*/
	public iNamedObject getChild(String child_name);
	/**
	    Return the child at the index or null
	*/
	public iNamedObject getChild(int index) throws ArrayIndexOutOfBoundsException;

	public boolean hasChild(iNamedObject child);

	public boolean hasChild(String child);

	public iNamedObject removeChild(iNamedObject child_to_remove) throws NoSuchElementException;

	public iNamedObject removeChild(String child_to_remove) throws NoSuchElementException;

	public int getNumChildren();
	
	public List<iNamedGroup> getParents();
	
	//public List sortChildren(Comparator c);
}
