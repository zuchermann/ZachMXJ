package innards.namespace.rhizome;

import innards.iNamedGroup;

import java.util.*;
import java.util.List;

/**
 * an iHeirarchicalView that looks onto a iNamedGroup, and deals purely in terms of NamedGroups
 * 
 * @author marc
 * Created on May 25, 2003
 */
public class NamedGroupTopologicalView implements iTopologicalView
{
	/*
	 * @see innards.namespace.rhizome.iHierarchicalView#getChildren(java.lang.Object)
	 */
	public List getChildren(Object ofThisObject)
	{
		assert ofThisObject instanceof iNamedGroup : "type error <"+ofThisObject.getClass()+">";
		return ((iNamedGroup)ofThisObject).getChildrenList();
	}
	/*
	 * @see innards.namespace.rhizome.iHierarchicalView#getParents(java.lang.Object)
	 */
	public List getParents(Object ofThisObject)
	{
		assert ofThisObject instanceof iNamedGroup : "type error <"+ofThisObject.getClass()+">";
		Object g = ((iNamedGroup)ofThisObject).getParent();
		if (g == null) return Collections.EMPTY_LIST;
		return Collections.singletonList(g);
	}
	/**@see innards.namespace.rhizome.iTopologicalView#getChild(java.lang.Object, int)*/
	public Object getChild(Object ofThisObject, int i)
	{
		return getChildren(ofThisObject).get(i);
	}
}
