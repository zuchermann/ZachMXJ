package innards.namespace.rhizome;

import innards.util.*;

import java.io.Serializable;
import java.util.*;

/**
 * a simple (mutable)hierarchicalView.
 * 
 * @author marc Created on May 6, 2003
 */
public class BasicTopologicalView extends AbstractTopologicalView implements iTopologicalView.Mutable, iChangable, iTopologicalView.iDispatchesTopolgyChange {

	// convience functions here ------------------------------------------------------------------------

	/**
	 * @param name
	 */
	public BasicTopologicalView(String name) {
		super(name);
	}

	public BasicTopologicalView() {
		super("unnamed BasicTopologicalView");
	}
	/**
	 * surpisingly expensive, but completely cached
	 */
	public List listOfRoots() {
		return (List) cachedListOfRoots.data(new iChangable.iRecompute() {
			public Object recompute() {
				// scan for things in children with no things in parents
				List l = new ArrayList();
				for (Iterator iter = children.entrySet().iterator(); iter.hasNext();) {
					Map.Entry element = (Map.Entry) iter.next();
					Object key = element.getKey();
					if (!parents.containsKey(key))
						l.add(key);
					else if (((List) parents.get(key)).size() == 0)
						l.add(key);
				}
				return l;
			}
		});
	}

	public Set everything() {
		return (Set) cachedSetOfEverything.data(new iChangable.iRecompute() {
			public Object recompute() {
				// scan for things in children with no things in parents
				Set l = new HashSet();
				l.addAll(children.keySet());
				l.addAll(parents.keySet());

				System.out.println(" view has <" + children.keySet() + "> <" + parents.keySet() + ">");
				return l;
			}
		});
	}

	//	handlers
	List handlers = new ArrayList();

	public void addHandler(iHandlesTopologyChange h) {
		handlers.add(h);
	}
	public void removeHandles(iHandlesTopologyChange h) {
		handlers.remove(h);
	}
	/*
	 * @see marc.rhizome.iChangable#getModCount(java.lang.Object)
	 */
	public iModCount getModCount(Object withRespectTo) {
		return changeable.getModCount(withRespectTo);
	}

	// implementation of iMutableHierarchicalView below
	protected SimpleChangable changeable = new SimpleChangable();
	iChangable.iModCount cachedListOfRoots = changeable.getModCount(new Serializable() {
	});
	iChangable.iModCount cachedSetOfEverything = changeable.getModCount(new Serializable() {
	});

	/**
	 * parents (Object) vs children (List(Object));
	 */
	protected Map children = createMap();
	/**
	 * children (Object) vs parents (List(Object));
	 */
	protected Map parents = createMap();

	protected Map createMap() {
		return new LinkedHashMap();
	}

	/**
	 * @see marc.rhizome.iMutableHierarchicalView#addChild(java.lang.Object, java.lang.Object)
	 */
	public void addChild(Object toThisObject, Object thisIsNowAChild) {
		changeable.dirty();
		List childList;

		if ((numChildren(thisIsNowAChild) == 0) && (numParents(thisIsNowAChild) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeAdded(this, thisIsNowAChild);
		if ((numParents(toThisObject) == 0) && (numParents(toThisObject) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeAdded(this, toThisObject);

		(childList = safeGetChildren(toThisObject)).add(thisIsNowAChild);
		safeGetParents(thisIsNowAChild).add(toThisObject);

		for (int i = 0; i < handlers.size(); i++)
			 ((iHandlesTopologyChange) handlers.get(i)).notifyInsertChild(this, toThisObject, childList.size(), thisIsNowAChild);
	}

	/**
	 * @see marc.rhizome.iMutableHierarchicalView#insertChild(java.lang.Object, int, java.lang.Object)
	 */
	public void insertChild(Object toThisObject, int atIndex, Object thisIsNowAChild) {
		changeable.dirty();

		if ((numChildren(thisIsNowAChild) == 0) && (numParents(thisIsNowAChild) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeAdded(this, thisIsNowAChild);
		if ((numParents(toThisObject) == 0) && (numParents(toThisObject) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeAdded(this, toThisObject);

		safeGetChildren(toThisObject).add(atIndex, thisIsNowAChild);
		safeGetParents(thisIsNowAChild).add(toThisObject);
		for (int i = 0; i < handlers.size(); i++)
			 ((iHandlesTopologyChange) handlers.get(i)).notifyInsertChild(this, toThisObject, atIndex, thisIsNowAChild);
	}

	/*
	 * @see marc.rhizome.iMutableHierarchicalView#removeChild(java.lang.Object, java.lang.Object)
	 */
	public Object removeChild(Object fromThisObject, Object thisIsNowNotAChild) {
		changeable.dirty();

		Object o = safeGetParents(thisIsNowNotAChild).remove(safeGetChildren(fromThisObject).remove(thisIsNowNotAChild) ? fromThisObject : null) ? thisIsNowNotAChild : null;
		//		if (o!=null) for(int i=0;i<removalHandles.size();i++) ((iHandlesRemoval)removalHandles.get(i)).removeChild(fromThisObject, thisIsNowNotAChild);
		for (int i = 0; i < handlers.size(); i++)
			 ((iHandlesTopologyChange) handlers.get(i)).notifyRemoveChild(this, fromThisObject, thisIsNowNotAChild);

		if ((numParents(fromThisObject) == 0) && (numParents(fromThisObject) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeRemoved(this, fromThisObject);
		if ((numParents(thisIsNowNotAChild) == 0) && (numParents(thisIsNowNotAChild) == 0))
			for (int i = 0; i < handlers.size(); i++)
				 ((iHandlesTopologyChange) handlers.get(i)).notifyNodeRemoved(this, thisIsNowNotAChild);

		return o;
	}

	/*
	 * @see marc.rhizome.iHierarchicalView#getChildren(java.lang.Object)
	 */
	public List getChildren(Object ofThisObject) {
		return safeGetChildren(ofThisObject);
	}

	public Object getChild(Object ofThisObject, int i) {
		return safeGetChildren(ofThisObject).get(i);
	}

	/*
	 * @see marc.rhizome.iHierarchicalView#getParents(java.lang.Object)
	 */
	public List getParents(Object ofThisObject) {
		return safeGetParents(ofThisObject);
	}

	/**
	 * @param toThisObject
	 */
	protected List safeGetChildren(Object toThisObject) {
		List l = (List) children.get(toThisObject);
		if (l == null)
			children.put(toThisObject, l = newList(1));
		return l;
	}

	protected List safeGetParents(Object toThisObject) {
		List l = (List) parents.get(toThisObject);
		if (l == null)
			parents.put(toThisObject, l = newList(1));
		return l;
	}

	protected int numChildren(Object ofThisObject) {
		List l = (List) children.get(ofThisObject);
		return l == null ? 0 : l.size();
	}
	protected int numParents(Object ofThisObject) {
		List l = (List) parents.get(ofThisObject);
		return l == null ? 0 : l.size();
	}

	protected List newList(int size) {
		return new ArrayList(size);
	}

	public List sortChildren(Object ofThisObject, Comparator c) {
		List itsChildren = safeGetChildren(ofThisObject);
		Collections.sort(itsChildren, c);
		children.put(ofThisObject, itsChildren);
		return itsChildren;
	}

}
