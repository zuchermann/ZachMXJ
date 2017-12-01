/*
 * Created on Aug 6, 2003
 */
package innards.namespace.rhizome;

import java.util.List;

import innards.math.BaseMath.MutableFloat;


/**
 * An extension of <code>iTopologicalView</code> which adds support for connection weights,
 * as in, for example, a connectionist network.
 * 
 * @author derek
 */
public interface iWeightedTopologicalView extends iTopologicalView
{
	/**
	 * This method should return a <code>List</code> of all the designated parent's connection weights.
	 */
	public List getConnectionWeights(Object ofThisParent);
	/**
	 * This method should adjust the weight of the connection that exists between the
	 * specified parent and child.
	 */
	public boolean setConnectionWeight(Object parent, Object child, float weight);
	
	/**
	 * This method should return the connection weight that exists between the specified
	 * parent and child.   Should return a null result if the designated parent-child pair cannot be found.
	 */
	public MutableFloat getConnectionWeight(Object parent, Object child);
	
	public interface WeightedMutable extends iWeightedTopologicalView, iTopologicalView.Mutable
	{
		/**
		 * adds a child with the designated connection weight
		 * @param toThisObject
		 * @param thisIsNowAChild
		 */
		 public void addChild(Object toThisObject, Object thisIsNowAChild, float withThisWeight);

	 	/**
		 * adds a child at this index with the designated connection weight
		 * @param toThisObject
		 * @param thisIsNowAChild
		 */
	 	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild, float withThisWeight);
	}

	public interface iDispatchesTopologyChange extends iTopologicalView.iDispatchesTopolgyChange
	{
		public void addHandler(iHandlesTopologyChange h);
		public void removeHandler(iHandlesTopologyChange h);
	}
	
	public interface iHandlesTopologyChange extends iTopologicalView.iHandlesTopologyChange
	{
		public void notifyWeightAdjusted(iDispatchesTopologyChange from, Object to, Object child, float weight);
	}
}
