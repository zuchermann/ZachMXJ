package innards.namespace;

import innards.*;

import java.util.Vector;

/**
    simple class for when you want to gather lots of nodes or things
    into a vector and return it.
    
    example use is
    
    Vector everything = (new GatherAction()
    {
        public void apply(NamedObject node, Vector stack)
        {
            stack.addElement(node);
        }
    }).gather(root);
    
    */

public abstract class GatherAction extends BaseTraversalAction implements Cloneable
{

	Vector v= new Vector();

	abstract public void apply(iNamedObject node, Vector stack);
	protected boolean actionImplementation(iNamedObject node)
	{
		apply(node, v);
		return true;
	}

	public Vector gather(iNamedGroup root)
	{
		applyAction(root);
		return v;
	}
}