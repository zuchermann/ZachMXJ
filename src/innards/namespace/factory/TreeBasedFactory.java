package innards.namespace.factory;

import innards.*;

/**
 * a factory based on a tree of NamedObjects.<br>
 * you can specify production rules for nodes or their children.<br> 
 * if no rule is specified for a node, it inherits from its parents.
 * the factory searches up the tree until a node is found that either specifies
 * a construction rule for its children or failing that, itself.
 * @author synchar
 */
public class TreeBasedFactory extends Factory
{
	protected Factory productionsForChildren;
	
	public TreeBasedFactory(String name)
	{
		super(name);
		productionsForChildren = new Factory(name+"'s productions for children");
	}
	
	/**
	 * add a production rule for a tree node.  <code>identifier</code> must be
	 * an iNamedObject.  the production rule automatically cascades to the
	 * children of the node, unless a production rule for the children is
	 * explicitly specified using the <code>addProductionForChildren</code>
	 * method.
	 * @see innards.namespace.factory.Factory#addProduction(java.lang.Object, innards.namespace.factory.iProduction)
	 */
	public void addProduction(Object identifier, iProduction prod)
	{
		super.addProduction(((iNamedObject)identifier).getKey(), prod);
	}

	/**
	 * add a production rule for the children of a tree node.
	 * <code>identifier</code> must be an iNamedObject.
	 */
	public void addProductionForChildren(Object identifier, iProduction prod)
	{
		productionsForChildren.addProduction(((iNamedObject)identifier).getKey(), prod);
	}

	/**
	 * performs the production associated with <code>identifier</code>, an
	 * iNamedObject, or inherited by <code>identifier</code> from one of its
	 * ancestors.
	 * @see innards.namespace.factory.Factory#produce(java.lang.Object, java.lang.Object)
	 */
	public Object produce(Object identifier, Object parameters) throws IllegalArgumentException
	{
		iNamedObject start = (iNamedObject)identifier;
		
		iNamedObject node = start;
		while (true)
		{
			if (super.canProduce(node.getKey()))
			{
				return super.produce(node.getKey(), parameters);
			}

			node = node.getParent();
			if (node == null) break;

			if (productionsForChildren.canProduce(node.getKey()))
			{
				return productionsForChildren.produce(node.getKey(), parameters);
			}
		}
		
		return super.produce(start.getKey(), parameters);
	}

	/**
	 * returns true if the factory has an explicit production rule for
	 * <code>identifier</code>, an iNamedObject, or if <code>identifier</code>
	 * has inherited a production rule from one of its ancestors.
	 * @see innards.namespace.factory.Factory#canProduce(java.lang.Object)
	 */
	public boolean canProduce(Object identifier)
	{
		iNamedObject start = (iNamedObject)identifier;
		
		iNamedObject node = start;
		while (true)
		{
			if (super.canProduce(node.getKey()))
			{
				return true;
			}

			node = node.getParent();
			if (node == null) break;

			if (productionsForChildren.canProduce(node.getKey()))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * @see innards.namespace.factory.Factory#setDefaultProduction(innards.namespace.factory.iProduction)
	 */
	public void setDefaultProduction(iProduction prod)
	{
		super.setDefaultProduction(prod);
	}
}
