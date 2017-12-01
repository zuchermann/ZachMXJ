package innards.namespace.rhizome;

import innards.NamedObject;

import java.util.*;

/**
 * where convience methods are stored
 * 
 * @author marc
 * Created on May 24, 2003
 */
public abstract class AbstractTopologicalView extends NamedObject implements iTopologicalView.Mutable
{

	/**
	 * @param name
	 */
	public AbstractTopologicalView(String name)
	{
		super(name);
	}

	/**
	 * makes all the elements 'children' children of all the elements of 'parents'
	 */
	public void allAreChildren(Collection parents, Collection children)
	{
		for (Iterator i= children.iterator(); i.hasNext();)
		{
			Object c= (Object) i.next();
			for (Iterator j= parents.iterator(); j.hasNext();)
			{
				Object p= (Object) j.next();
				this.addChild(p, c);
			}
		}
	}

	/**
	 * makes none of the elements of 'children' children of the elements of 'parents'
	 */
	public void noneAreChildren(Collection parents, Collection children)
	{
		for (Iterator i= children.iterator(); i.hasNext();)
		{
			Object c= (Object) i.next();
			for (Iterator j= parents.iterator(); j.hasNext();)
			{
				Object p= (Object) j.next();
				this.removeChild(p, c);
			}
		}
	}

	/**
	 * returns closure of all the the children+ of  this node
	 */
	public Set childrenClosure(Object node)
	{
		Set s= new HashSet();
		return childrenClosure(node, s);
	}

	/**
	 * returns closure of all the the children+ of  this node, adding things to set 'into', ignoring things that are already inside 'into'
	 */
	public Set childrenClosure(Object node, Set into)
	{
		if (!into.contains(into))
		{
			into.add(node);
			childrenClosureOfAll(this.getChildren(node), into);
		}
		return into;
	}

	/**
	 * the childrenClosure of all the nodes inside this Collection
	 */
	public Set childrenClosureOfAll(Collection node)
	{
		Set s= new HashSet();
		return childrenClosureOfAll(node, s);
	}

	/**
	 * the childrenClosure of all the nodes inside this Collection, placing things into set 's', ignoring things already inside 's'
	 */
	public Set childrenClosureOfAll(Collection node, Set s)
	{
		for (Iterator i= node.iterator(); i.hasNext();)
		{
			Object element= (Object) i.next();
			s= childrenClosure(element, s);
		}
		return s;
	}

	/**
	 * returns closure of all the the parent+ of  this node
	 */
	public Set parentClosure(Object node)
	{
		Set s= new HashSet();
		return parentClosure(node, s);
	}

	/**
	 * returns closure of all the the parent+ of  this node, placing things into 'into' and ignoring thigns already there
	 */
	public Set parentClosure(Object node, Set into)
	{
		if (!into.contains(into))
		{
			into.add(node);
			parentClosureOfAll(this.getParents(node), into);
		}
		return into;
	}

	/**
	 * returns closure of all the the parent+ of  the nodes inside this collection,
	 */
	public Set parentClosureOfAll(Collection node)
	{
		Set s= new HashSet();
		return parentClosureOfAll(node, s);
	}

	/**
	 * returns closure of all the the parent+ of  the nodes inside this collection, placing things into 's' and ignoring thigns already there
	 */
	public Set parentClosureOfAll(Collection node, Set s)
	{
		for (Iterator i= node.iterator(); i.hasNext();)
		{
			Object element= (Object) i.next();
			s= childrenClosure(element, s);
		}
		return s;
	}

	/**
	 * returns closure of all the the parent+ of  this node
	 */
	public Set biClosure(Object node)
	{
		Set s= new HashSet();
		return biClosure(node, s);
	}

	/**
	 * returns closure of all the the parent+ and children+ of  this node, placing things into 'into' and ignoring thigns already there
	 */
	public Set biClosure(Object node, Set into)
	{
		if (!into.contains(into))
		{
			into.add(node);
			biClosureOfAll(this.getParents(node), into);
			biClosureOfAll(this.getChildren(node), into);
		}
		return into;
	}

	/**
	 * returns closure of all the the parent+ and children+  of  the nodes inside this collection,
	 */
	public Set biClosureOfAll(Collection node)
	{
		Set s= new HashSet();
		return biClosureOfAll(node, s);
	}

	/**
	 * returns closure of all the the parent+ and children+  of  the nodes inside this collection, placing things into 's' and ignoring thigns already there
	 */
	public Set biClosureOfAll(Collection node, Set s)
	{
		for (Iterator i= node.iterator(); i.hasNext();)
		{
			Object element= (Object) i.next();
			s= childrenClosure(element, s);
		}
		return s;
	}

	/**
	 * all the direct parents of collection 'in'
	 */
	public Set getParentsOfAll(Collection in)
	{
		Set s= new HashSet();
		for (Iterator i= in.iterator(); i.hasNext();)
		{
			Object element= (Object) i.next();
			s.addAll(getParents(element));
		}
		return s;
	}

	/**
	 * all the direct children of collection 'in'
	 */
	public Set getChildrenOfAll(Collection in)
	{
		Set s= new HashSet();
		for (Iterator i= in.iterator(); i.hasNext();)
		{
			Object element= (Object) i.next();
			s.addAll(getChildren(element));
		}
		return s;
	}
	
	/**
	 * finds the ultimate parents of everything inside the collection
	 */
	public Set getRootParentsOfAll(Collection in)
	{
		Set ret = new HashSet();
		Set working = new HashSet();
		working.addAll(in);
		Set working2 = new HashSet();
		Set seen =new HashSet();
		while(working.size()>0)
		{
			for (Iterator i= working.iterator(); i.hasNext();)
			{
				Object element= (Object) i.next();
				List l = getParents(element);
				if ((l.size()==0) || (seen.contains(element)))
				{
					i.remove();
					ret.add(element);
				}
				else
				{
					working2.addAll(l);
					seen.add(element);
				}
			}
			working = working2;
			working2 = new HashSet();
		}
		return ret;
	}

}
