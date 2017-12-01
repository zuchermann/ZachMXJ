package innards.namespace.rhizome;

import java.util.*;

/**
* "To be rhizomorphous is to produce stems and filaments that seem to be roots, or better yet connect with them by penetrating 
* the trunk, but put them to strange new uses. We're tired of trees. We should stop believing in trees, roots, and radicles. The've 
* made us suffer too much. All of arborescent culture is founded on them, from biology to linguistics. Nothing is beautiful or
* loving or political aside from underground stems and aerial roots, adventitous growths and rhizomes." 

* Deleuze and Guattari "A Thousand Plateaus, capitalism and schizophrenia".

 * 
 * @author marc
 * Created on May 6, 2003
 */
public interface iTopologicalView	
{
	/**
	 * list of all the children of this object
	 * @param ofThisObject
	 * @return
	 */
	public List getChildren(Object ofThisObject);
	
	public Object getChild(Object ofThisObject, int i);
	
	/**
	 * liast of all the parents of this object
	 * @param ofThisObject
	 * @return
	 */
	public List getParents(Object ofThisObject);

	public interface Mutable extends iTopologicalView
	{
		/**
		 * adds a child
		 * @param toThisObject
		 * @param thisIsNowAChild
		 */
		public void addChild(Object toThisObject, Object thisIsNowAChild);

		/**
		 * adds a child at this index
		 * @param toThisObject
		 * @param thisIsNowAChild
		 */
		public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild);

		/**
		 * removes a child
		 * @param toThisObject
		 * @param thisIsNowAChild
		 * @return the removed child if it was actually removed
		 */
		public Object removeChild(Object fromThisObject, Object thisIsNowNotAChild);
		
		public List sortChildren(Object ofThisObject, Comparator c);
	}
		
	// interfaces for installing watches ------------------------------------------------------------------------
	public interface iDispatchesTopolgyChange
	{

		public void addHandler(iHandlesTopologyChange h);
		public void removeHandles(iHandlesTopologyChange h);
	}

	public interface iHandlesTopologyChange
	{
		public void notifyInsertChild(iDispatchesTopolgyChange from, Object to, int atIndex, Object child);
		public void notifyRemoveChild(iDispatchesTopolgyChange from, Object fromC, Object child);
		public void notifyNodeAdded(iDispatchesTopolgyChange from, Object added);
		public void notifyNodeRemoved(iDispatchesTopolgyChange from, Object removed);
	}

}
