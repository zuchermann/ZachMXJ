package innards.namespace.rhizome;

import innards.namespace.BaseTraversalAction;
import innards.namespace.rhizome.iTopologicalView.iDispatchesTopolgyChange;
import innards.util.*;

import java.util.*;

/**
 * 
 * for, say, a multi-resolution graph structure
 * <p>
 * extends BasicHierarchicalView for the child parent relationships, adds (has-a) another BasicHierarchicalView for the up(parent) / down (children) relationships.
 * <p> 
 * (tries to) maintain a list of the 'layer' that nodes are in., currently this doesn't work, becuase global consistancy cannot be  assured..
 * One might be able to get what you want by getUpDownHeirarchicalView().listOfRoots()
 * @author marc
 * Created on May 24, 2003
 */
public class MultiLayerTopologicalView extends BasicTopologicalView
{

	BasicTopologicalView upDown;

	public MultiLayerTopologicalView(BasicTopologicalView upDown)
	{
		this.upDown= upDown;
		((iTopologicalView.iDispatchesTopolgyChange) upDown).addHandler(new LayerHandler());
		changeable.chainWith(new iModCount[]{upDown.changeable.getModCount(new Object())});
		upDown.changeable.getModCount(changeable.getModCount(new Object()));
	}

	public MultiLayerTopologicalView()
	{
		this(new BasicTopologicalView());
	}

	public BasicTopologicalView getLevelView()
	{
		return upDown;
	}

	/**
	 * take everything that this node is connected to at this level and make sure that the 'up' of this
	 * node is connected to (parent of) all the 'up's of all those nodes
	 * <p>
	 * equivlent to a allAreChildren(nodeAtLevelOne.getParents(), upwardsFromChildrenOf(nodeAtLevelOne))
	 * 
	 */
	public void propogateTopologyUpwards(Object nodeAtLevelOne)
	{
		List up= upDown.getParents(nodeAtLevelOne);
		Set connectTo= upwardsFromChildrenOf(nodeAtLevelOne);
		allAreChildren(up, connectTo);
	}

	/**
	 * returns the set of all nodes  that are 'up' from direct children of this node
	 * @param node
	 * @return
	 */
	public Set upwardsFromChildrenOf(Object node)
	{
		return upDown.getParentsOfAll(getChildren(node));
	}

	/**
		 * returns the set of all nodes  that are 'up' from direct parents of this node
		 * @param node
		 * @return
		 */
	public Set upwardsFromParentsOf(Object node)
	{
		return upDown.getParentsOfAll(getParents(node));
	}

	/**
	 * returns the set of all nodes  that are 'up' from children of this node
	 * @param node
	 * @return
	 */
	public Set downwardsFromParentsOf(Object node)
	{
		return upDown.getChildrenOfAll(getParents(node));
	}

	/**
	 * retuns an iterator that can be used to iterate over all the layers, use getLayer(iterator.next()) to get at the layer sets
	 * 
	 * the O(...) of this algorithm is left as an exercise to the reader...
	 * @return
	 */
	public Iterator orderedLayerIterator()
	{
		List sortedList= new ArrayList();
		sortedList.addAll(((Map) layerSetsCached.data(new iChangable.iRecompute()
		{
			public Object recompute()
			{
				List roots= upDown.listOfRoots();
				// starting with the roots of the up down hierarchy we need to obtain a globally consistant view of the hierarchical levels
				nodesToLayers= new HashMap();
				layersToNodes= new HashMap();

				// this is propogating downward, which is sufficient if all roots are at the same (top) level  and propogating back on level which should be
				// complete for any hierarchy.

				Integer i0= new Integer(0);
				Set parentSet= new HashSet();
				parentSet.addAll(roots);
				while (parentSet.size() != 0)
				{
					Set childrenClosure= biClosureOfAll(getLevelView().getChildrenOfAll(parentSet));
					Set parentClosure= biClosureOfAll(getLevelView().getParentsOfAll(childrenClosure), parentSet);
					declareLayerOfAll(parentClosure, i0);

					parentSet= childrenClosure;
					i0= new Integer(i0.intValue() - 1);
				}

				return layersToNodes;
			}
		})).keySet());
		Collections.sort(sortedList);
		return sortedList.iterator();
	}

	/**
	 * takes an opaque object that the LayerIterator() retuns and gives you the corresponding set
	 */
	public Set toLayer(Object layerID)
	{
		return (Set) layersToNodes.get(layerID);
	}

	/**
	 * which layer is 'of' on? returns defaultLayer if we do not know
	 * @param of
	 * @return
	 */
	public int getLayerOf(Object of)
	{
		Integer i= (Integer) nodesToLayers.get(of);
		if (i == null)
			return defaultLayer.intValue();
		return i.intValue();
	}

	// insides -----------
	HashMap nodesToLayers= new HashMap();
	HashMap layersToNodes= new HashMap();
	Integer defaultLayer= new Integer(0);
	Integer defaultNextLayer= new Integer(1);

	SimpleChangable layerSetsChangeable= new SimpleChangable();
	iChangable.iModCount layerSetsCached= layerSetsChangeable.getModCount(new Object());

	public void addChild(Object toThisObject, Object thisIsNowAChild)
	{
		super.addChild(toThisObject, thisIsNowAChild);
		makeLayerRelationship(toThisObject, thisIsNowAChild, 0);
	}

	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild)
	{
		super.insertChild(toThisObject, atIndex, thisIsNowAChild);
		makeLayerRelationship(toThisObject, thisIsNowAChild, 0);
	}

	public void makeLayerRelationship(Object o1, Object o2, int delta)
	{
		Integer i1= (Integer) nodesToLayers.get(o1);
		Integer i2= (Integer) nodesToLayers.get(o2);
		if (i1 == null)
		{
			if (i2 == null)
			{
				declareLayer(o1, defaultLayer);
				declareLayer(o2, new Integer(defaultLayer.intValue() + delta));
			} else
			{
				declareLayer(o1, new Integer(i2.intValue() - delta));
			}
		} else
		{
			if (i2 == null)
			{
				declareLayer(o2, new Integer(i1.intValue() + delta));
			} else
			{
				// here we might have a contradiction - child wins
				// but we cannot assure a globally solution
				if (declareLayer(o1, new Integer(i1.intValue() - delta)))
					layerSetsChangeable.dirty();
			}
		}
	}

	public boolean declareLayer(Object o, Integer i)
	{
		Integer old= (Integer) nodesToLayers.get(o);
		if (old != i)
		{
			if (old != null)
			{
				Set s= (Set) layersToNodes.get(nodesToLayers.remove(o));
				s.remove(o);
			}
			Set s= (Set) layersToNodes.get(i);
			if (s == null)
				layersToNodes.put(i, s= new HashSet());
			s.add(o);
			nodesToLayers.put(o, i);
			return true;
		}
		return false;
	}

	public boolean declareLayerOfAll(Collection o, Integer i)
	{
		boolean b= false;
		for (Iterator iter= o.iterator(); iter.hasNext();)
		{
			b |= declareLayer(iter.next(), i);
		}
		return b;
	}

	class LayerHandler implements iTopologicalView.iHandlesTopologyChange
	{
		/*
		 * @see innards.namespace.rhizome.iHierarchicalView.iHandlesTopologyChange#notifyInsertChild(innards.namespace.rhizome.iHierarchicalView.iDispatchesTopolgyChange, java.lang.Object, int, java.lang.Object)
		 */
		public void notifyInsertChild(iDispatchesTopolgyChange from, Object to, int atIndex, Object child)
		{
			makeLayerRelationship(to, child, -1);
		}
		public void notifyNodeAdded(iDispatchesTopolgyChange from, Object added)
		{
		}
		public void notifyNodeRemoved(iDispatchesTopolgyChange from, Object removed)
		{
		}
		public void notifyRemoveChild(iDispatchesTopolgyChange from, Object fromC, Object child)
		{
		}
	}

}
