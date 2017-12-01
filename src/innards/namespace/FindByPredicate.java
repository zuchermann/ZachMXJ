package innards.namespace;

import innards.*;

import java.util.Vector;

public abstract class FindByPredicate extends BaseTraversalAction implements Cloneable 
{    
    public static final int FIND_ALL = 0;
    public static final int FIND_FIRST = 1;
    public static final int FIND_LAST = 2;
        
    protected int mode = FIND_FIRST;
    protected boolean terminate = false;
    protected Vector ret;
    
    public FindByPredicate()
    {
        ret = new Vector();
    }

    public Object applyAction(iNamedObject root) {
		terminate = false;
		super.applyAction(root);
		return ret;
    }
    
    public Vector findAll(NamedObject root) {
    	mode = FIND_ALL;
    	return (Vector)applyAction(root);
    }
    
    public Object findFirst(NamedObject root) {
    	mode = FIND_FIRST;
    	Vector v = (Vector) applyAction(root);
    	if (v.size()==0) return null; else return v.get(0);
    }
    
    public Object findLast(NamedObject root) {
    	mode = FIND_LAST;
    	Vector v = (Vector) applyAction(root);
    	if (v.size()==0) return null; else return v.get(0);
    }
    
    static FindByPredicate by(final iPredicate pred)
    {
    	return new FindByPredicate()
    	{
    		public boolean is(iNamedObject no)
    		{
    			return pred.is(no);
    		}
    	};
    }

    public abstract boolean is(iNamedObject no);

    /** 
        Do the actual action on 'node' with context dependant 'traversal_state'
        Your action will override this function.
        <p>
        returns 'true' to continue action
        returns 'false' to terminate action
    */
    protected boolean actionImplementation(iNamedObject node)
    {
        if (terminate) return false;
        if (is(node))
        {
            if (mode == FIND_LAST) 
            {
                if (ret.size()==0) ret.addElement(node); else ret.setElementAt(node,0);
            }
            else
            if (mode == FIND_FIRST)
            {
                if (!terminate) ret.addElement(node);
                terminate = true;
            }
            else // mode == FIND_ALL
            {
                ret.addElement(node);
            }
        }
        return !terminate;
    }
    
    /**
        Override this method to test whether or not this node should be traversed        
        returns 'true' if the node should be traversed, and 'false' otherwise.
    */        
    protected boolean traverseThisNode(iNamedObject node) {
        return !terminate && super.traverseThisNode(node);
    }          
    
}