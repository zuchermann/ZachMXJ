package innards.namespace.rhizome.metavariable;

import java.util.*;
import java.util.Map;

import innards.namespace.rhizome.BasicTopologicalView;

/**
 * @author marc
 */
public class MetaTopologicalView extends BasicTopologicalView
{
	/**
	 * @see innards.namespace.rhizome.BasicTopologicalView#createMap()
	 */
	protected Map createMap()
	{
		return new MetaMap();
	}

	protected List newList(int size)
	{
		return new MetaList();
	}

	public String toString()
	{
		return "children<"+this.children+">\nparents<"+this.parents+">";
	}
	
	public Map getParentMap()
	{
		return parents;
	}

	public Map getChildrenMap()
	{
		return children;
	}
}
