/*
 * Created on Aug 19, 2003
 */
package innards.namespace.rhizome;

import java.util.*;

import innards.*;
import innards.math.BaseMath.MutableFloat;
import innards.namespace.context.ContextTree;

/**
 * An extension of <code>VirtualizedNamedGroup</code> which provides
 * support for <code>iWeightedTopologicalViews</code>.
 * 
 * @author derek
 */
public class WeightedVirtualizedNamedGroup extends VirtualizedNamedGroup
{

	public WeightedVirtualizedNamedGroup(String name)
	{
		super(name);
	}

	public WeightedVirtualizedNamedGroup(Key key)
	{
		super(key);
	}

	public WeightedVirtualizedNamedGroup()
	{
		super();
	}
	
	/**
	 * Return a non-weighted, read-only version of the current view.
	 */
	public iTopologicalView getView()
	{
		return (iTopologicalView) getWeightedView();
	}

	/**
	 * Return a non-weighted, writable version of the current view.
	 */
	public iTopologicalView.Mutable getMutableView()
	{
		return (iTopologicalView.Mutable) getWeightedMutableView();
	}
	
	/**
	 * Return a weighted, read-only version of the current view.
	 * <p>
	 * Subclasses should override this method to implement their own logic for view retrieval.
	 */
	public iWeightedTopologicalView getWeightedView()
	{
		return (iWeightedTopologicalView) ContextTree.get(default_weighted_hierarchical_view, null);
	}
	
	/**
	 * Return a weighted, writable version of the current view.
	 * <p>
	 * Subclasses should override this method to implement their own logic for view retrieval.
	 */
	public iWeightedTopologicalView.WeightedMutable getWeightedMutableView()
	{
		return (iWeightedTopologicalView.WeightedMutable) ContextTree.get(default_weighted_hierarchical_view, null);
	}

	/**
	 * Add a child with the designated connection weight.
	 */
	public void addChild(iNamedObject child, float weight)
	{
		getWeightedMutableView().addChild(this, child, weight);	
	}
	
	/**
	 * Add a child with default connection weight.
	 * @see innards.iNamedGroup#addChild(innards.iNamedObject)
	 */
	public void addChild(iNamedObject child)
	{
		getWeightedMutableView().addChild(this, child);
	}

	/**
	 * Insert a child at the designated index with the designated connection weight.
	 */
	public void insertChildAt(iNamedObject child, int index, float weight)
	{
		getWeightedMutableView().insertChild(this, index, child,  weight);
	}

	/**
	 * Insert a child at the designated index with default connection weight.
	 * @see innards.iNamedGroup#insertChildAt(innards.iNamedObject, int)
	 */
	public void insertChildAt(iNamedObject child, int index) throws ArrayIndexOutOfBoundsException
	{
		getMutableView().insertChild(this, index, child);
	}
	
	/**
	 * Get the connection weight for the child with the given name.  May return null if
	 * the designated child cannot be found.
	 */
	public MutableFloat getWeightForChild(String childName)
	{
		List l = getWeightedView().getChildren(this);
		for (int i = 0; i < l.size(); i++)
		{
			Object o = l.get(i);
			if (o instanceof iNamedObject)
			{
				if (((iNamedObject) o).getName().equals(childName))
				{
					return getWeightedView().getConnectionWeight(this, o);
				}
			}
		}
		return null;	
	}
	
	/**
	 * Get the connection weight for the child with the given index. 
	 */
	public MutableFloat getWeightForChild(int index)
	{
		return (MutableFloat) getWeightedView().getConnectionWeights(this).get(index);
	}

	/**
	 * Remove a child from this parent.
	 * @see innards.iNamedGroup#removeChild(innards.iNamedObject)
	 */
	public iNamedObject removeChild(iNamedObject childToRemove) throws NoSuchElementException
	{
		return (iNamedObject) getWeightedMutableView().removeChild(this, childToRemove);
	}

	/**
	 * Remove a child from this parent.
	 * @see innards.iNamedGroup#removeChild(java.lang.String)
	 */
	public iNamedObject removeChild(String childToRemove) throws NoSuchElementException
	{
		iNamedObject r = getChild(childToRemove);
		return removeChild(r);
	}
}
