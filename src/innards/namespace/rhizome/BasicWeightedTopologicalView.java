/*
 * Created on Aug 5, 2003
 */
package innards.namespace.rhizome;

import java.util.*;

import innards.math.BaseMath.MutableFloat;

/**
 * A <code>BasicTopologicalView</code> in which parents maintain a connection weight for each of
 * their children.
 * <p>
 * Note that if children are added without a specified weight, then their weight value takes on a 
 * default value; the 'default default' is 1.0. 
 * <p>
 * Useful for assembling <code>VirtualizedNamedGroups</code> into something resembling a connectionist
 * network.
 * 
 * @author derek
 */
public class BasicWeightedTopologicalView extends BasicTopologicalView implements iWeightedTopologicalView.WeightedMutable
{
	/**
	 * Representation for storing weight information.  The HashMap contains
	 * parent <code>Objects</code> mapped to <code>List(MutableFloats)</code>
	 * corresponding to the weights of the children.
	 * <p>
	 * Note that the contents of this HashMap must be changed in synchrony with
	 * the contents of the <code>children</code> HashMap defined in the superclass.
	 */
	private Map weights = createMap();
	
	/**
	 * Children added without a specified weight are given this weight value.
	 */
	private float DEFAULT_WEIGHT = 1.0f;
	
	public void setDefaultWeight(float f)
	{
		DEFAULT_WEIGHT = f;
	}
	
	public float getDefaultWeight()
	{
		return DEFAULT_WEIGHT;
	}
	
	/**
	 * Get a <code>List</code> of all the connection weights for the given parent.
	 */
	public List getConnectionWeights(Object forThisObject)
	{
		return safeGetWeights(forThisObject);
	}
	
	/**
	 * Use this method to adjust the weight of a parent-child connection which has
	 * already been created.
	 * <p>
	 * Returns true if the specified parent-child pair was found and updated, and false otherwise.
	 */
	public boolean setConnectionWeight(Object parent, Object child, float weight)
	{
		int childIndex = safeGetChildren(parent).indexOf(child);
		if (childIndex != -1)
		{
			((MutableFloat) safeGetWeights(parent).get(childIndex)).d = weight;
			return true;
		}
		
		else return false;
	}
	
	/**
	 * Determine the weighting that exists between a given parent-child pair.  Tosses an assert
	 * if the parent-child pairing cannot be found.
	 */
	public MutableFloat getConnectionWeight(Object parent, Object child)
	{
		int childIndex = safeGetChildren(parent).indexOf(child);
		if (childIndex != -1)
		{
			return new MutableFloat(((MutableFloat) safeGetWeights(parent).get(childIndex)).d);
		}
		
		else return null;
	}
	
	public void addChild(Object toThisObject, Object thisIsNowAChild, float withThisWeight)
	{
		super.addChild(toThisObject, thisIsNowAChild);
		safeGetWeights(toThisObject).add(new MutableFloat(withThisWeight));
	}
	
	public void addChild(Object toThisObject, Object thisIsNowAChild)
	{
		super.addChild(toThisObject, thisIsNowAChild);
		safeGetWeights(toThisObject).add(new MutableFloat(DEFAULT_WEIGHT));
	}

	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild, float withThisWeight)
	{
		super.insertChild(toThisObject, atIndex, thisIsNowAChild);
		safeGetWeights(toThisObject).add(atIndex, new MutableFloat(withThisWeight));
	}	
	
	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild)
	{
		super.insertChild(toThisObject, atIndex, thisIsNowAChild);
		safeGetWeights(toThisObject).add(atIndex, new MutableFloat(DEFAULT_WEIGHT));
	}

	public Object removeChild(Object fromThisObject, Object thisIsNowNotAChild)
	{
		int childIndex = safeGetChildren(fromThisObject).indexOf(thisIsNowNotAChild);
		Object o = super.removeChild(fromThisObject, thisIsNowNotAChild);
		
		if (childIndex != -1)
		{
			safeGetWeights(fromThisObject).remove(childIndex);
		}
		
		return o;
	}
		
	protected List safeGetWeights(Object parentObject)
	{
		List l = (List) weights.get(parentObject);
		if (l == null)
		{
			weights.put(parentObject, l = newList(1));
		}
		return l;
	}
}
