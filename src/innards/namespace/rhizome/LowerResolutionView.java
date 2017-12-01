package innards.namespace.rhizome;

import innards.util.*;
import innards.util.iChangable.iModCount;

import java.util.*;


/**
 * uses a (and wraps a) MultiLayerHierarchicalView to provide a reduced resolution view of a hierarchy.
 * <p>
 * at any time one can call getWorkginSet to get all the nodes in this view.
 * <p> 
 * TODO: dispatchers connected to this view only get topological change notifications between elements in the working set.
 */
public class LowerResolutionView extends AbstractTopologicalView
{
	MultiLayerTopologicalView container;
	Set workingSet= new HashSet();

	HashMap getChildren_cache= new HashMap();
	HashMap getParents_cache= new HashMap();
	SimpleChangable changeable= new SimpleChangable();

	public LowerResolutionView(MultiLayerTopologicalView container)
	{
		super("unnamed");
		this.container= container;
		changeable.chainWith(new iModCount[]{container.getModCount(new Object())});
	}

	public MultiLayerTopologicalView getView()
	{
		return container;
	}

	public Set getWorkingSet()
	{
		return workingSet;
	}
	// forms a higher level node, with the right initial connectivity
	public void mergeNodes(Object newNode, List ofNodes)
	{
		container.getLevelView().allAreChildren(Collections.singleton(newNode), ofNodes);

		workingSet.removeAll(ofNodes);
		workingSet.add(newNode);
	}

	public List unmergeNodes(Object groupingNode)
	{
		List children= container.getLevelView().getChildren(groupingNode);
		ArrayList cachedChildren = new ArrayList(children);
		
		container.getLevelView().noneAreChildren(Collections.singleton(groupingNode), cachedChildren);
		workingSet.remove(groupingNode);
		workingSet.addAll(cachedChildren);
		return children;
	}

	/*
	 * TODO - MUST BE CACHED FOR SPEED. OUCH.
	 * 
	 * @see innards.namespace.rhizome.iHierarchicalView#getChildren(java.lang.Object)
	 */
	public List getChildren(Object ofThisObject)
	{
		iChangable.iModCount m= (iModCount) getChildren_cache.get(ofThisObject);
		if (m == null)
			getChildren_cache.put(ofThisObject, m= changeable.getModCount(new Object()));
		if (!m.hasChanged())
			return (List) m.data();

		List l= container.getLevelView().getChildren(ofThisObject);
		if (l.size() == 0)
			l.add(ofThisObject);
		Set c= container.getChildrenOfAll(l);
		// resolve all of these to be top level nodes
		Set s= container.getLevelView().getRootParentsOfAll(c);
		s.removeAll(Collections.singleton(ofThisObject));
		List finalAnswer= new ArrayList(s);
		m.clear(finalAnswer);
		return finalAnswer;
	}
	/*
	 * TODO - MUST BE CACHED FOR SPEED. OUCH.
	 * @see innards.namespace.rhizome.iHierarchicalView#getParents(java.lang.Object)
	 */
	public List getParents(Object ofThisObject)
	{

		iChangable.iModCount m= (iModCount) getParents_cache.get(ofThisObject);
		if (m == null)
			getParents_cache.put(ofThisObject, m= changeable.getModCount(new Object()));
		if (!m.hasChanged())
			return (List) m.data();

		List l= container.getLevelView().getChildren(ofThisObject);
		if (l.size() == 0)
			l.add(ofThisObject);
		Set c= container.getParentsOfAll(l);
		// resolve all of these to be top level nodes
		Set s= container.getLevelView().getRootParentsOfAll(l);
		s.removeAll(Collections.singleton(ofThisObject));
		List finalAnswer= new ArrayList(s);
		m.clear(finalAnswer);
		return finalAnswer;
	}
	/*
	 * @see innards.namespace.rhizome.iHierarchicalView.Mutable#addChild(java.lang.Object, java.lang.Object)
	 */
	public void addChild(Object toThisObject, Object thisIsNowAChild)
	{
		// if either the parent or the child here have no parent in an upper view then they are part of the working set
		if (container.getLevelView().getParents(toThisObject).size() == 0)
			workingSet.add(toThisObject);
		if (container.getLevelView().getParents(thisIsNowAChild).size() == 0)
			workingSet.add(thisIsNowAChild);

		container.addChild(toThisObject, thisIsNowAChild);
		// if called on lower layer childen, ensures the consisancy of upper parent connections
		container.allAreChildren(new ArrayList(container.getLevelView().getParents(toThisObject)), new ArrayList(container.getLevelView().getParents(thisIsNowAChild)));
	}
	/*
	 * @see innards.namespace.rhizome.iHierarchicalView.Mutable#insertChild(java.lang.Object, int, java.lang.Object)
	 */
	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild)
	{
		container.insertChild(toThisObject, atIndex, thisIsNowAChild);
		// if called on lower layer childen, ensures the consisancy of upper parent connections
		container.allAreChildren(new ArrayList(container.getLevelView().getParents(toThisObject)), new ArrayList(container.getLevelView().getParents(thisIsNowAChild)));
	}
	/*
	 * @see innards.namespace.rhizome.iHierarchicalView.Mutable#removeChild(java.lang.Object, java.lang.Object)
	 */
	public Object removeChild(Object fromThisObject, Object thisIsNowNotAChild)
	{
		container.noneAreChildren(new ArrayList(container.getLevelView().getParents(fromThisObject)), new ArrayList(container.getLevelView().getParents(thisIsNowNotAChild)));
		return thisIsNowNotAChild;
	}

	/**@see innards.namespace.rhizome.iTopologicalView.Mutable#sortChildren(java.lang.Object, java.util.Comparator)*/
	public List sortChildren(Object ofThisObject, Comparator c)
	{
		throw new IllegalArgumentException(" not implemented");
	}

	/**@see innards.namespace.rhizome.iTopologicalView#getChild(java.lang.Object, int)*/
	public Object getChild(Object ofThisObject, int i)
	{
		return null;
	}

}