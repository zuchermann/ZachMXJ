package innards.namespace;

import innards.*;

import java.util.*;

/** 
    Abstract base class for the iTraversalAction interface.
    <p>
    @author synchar
*/

public abstract class BaseTraversalAction implements iTraversalAction, Cloneable
{
	private boolean loopProtection= false;
	private HashMap loopProtectionMap;

	/**
	 * Apply the action to this node, then recursively in a
	 *left-to-right depth first fashion on your children.
	 */
	public Object applyAction(iNamedObject root)
	{
		recursionLevel = 0;
		
		if (loopProtection)
		{
			loopProtectionMap= new HashMap(1000);
		}

		internalApplyAction(root);
		actionDone(root);

		//prevents us from keeping a hold of things
		loopProtectionMap= null;

		return null;
	}

	private boolean internalApplyAction(iNamedObject root)
	{
		recursionLevel++;
		boolean retVal= true;

		if (!loopProtection || (loopProtectionMap.get(root) == null))
		{
			if (loopProtection)
			{
				loopProtectionMap.put(root, root);
			}
			pushAction(root);

			//if action returns true for traverseThisNode() children are traversed
			if (traverseThisNode(root))
			{
				List children= getChildren(root);
				if (children != null)
				{
					for (int c= 0; c < children.size(); c++)
					{
						internalApplyAction((iNamedObject) (children.get(c)));
					}
				}
			}

			/*
			    if action returns true for applyThisNode() action is performed on this 
			    node.  Even if it returns false, children are still searched.
			*/
			if (applyActionThisNode(root))
			{
				retVal= actionImplementation(root);
			}

			popAction(root);
			recursionLevel--;
			return retVal;
		} else
		{
			recursionLevel--;
			return true;
		}
	}

	/**
	Returns the children of NamedObject passed in. Overridable by 
	subclasses that might want to filter children (or that might want to
	do exhaustive searches of all fields of a NamedObject)
	*/
	public List getChildren(iNamedObject no)
	{
		if (no instanceof iNamedGroup)
		{
			return ((iNamedGroup) no).getChildrenList();
		}
		return emptyVec;
	}

	private Vector emptyVec= new Vector();

	/** 
	    Do the actual action on 'node'.
	    Your action will override this function.
	    <p>
	    returns 'true' to continue action
	    returns 'false' to terminate action
	*/
	protected abstract boolean actionImplementation(iNamedObject node);

	/**
	    Override this method to test whether or not this node should be traversed        
	    returns 'true' if the node should be traversed, and 'false' otherwise.
	*/
	protected boolean traverseThisNode(iNamedObject node)
	{
		return true;
	}

	/** 
	    Override this method to indicate whether or not to apply the given action
	    to this node.  If call returns 'false' action is not performed, but children
	    are still traversed.  If call returns 'true' everything proceeds normally.
	*/
	protected boolean applyActionThisNode(iNamedObject node)
	{
		return true;
	}

	/**
	    This routine gets called when node is entered.  Override if you're interested
	*/
	protected void pushAction(iNamedObject node)
	{
	}

	/**
	    This routine gets called when node is exited.  Override if you're interested
	*/
	protected void popAction(iNamedObject node)
	{
	}

	/**
	    Called when the action is all done.  Allows any one-time post processing
	*/
	protected void actionDone(iNamedObject node)
	{
	}

	public BaseTraversalAction turnOnLoopProtection()
	{
		loopProtection= true;
		return this;
	}
	
	protected int recursionLevel;
	
	protected int getRecursionLevel()
	{
		return recursionLevel;
	}

	/**
	
	    Some example code.
	    
	    This also demonstrates how to use an anonymous class to write a search action
	    in one line.
	
	
	                        root
	                _________|_________
	                |        |        |
	                A        B        C
	            ____|_____   |     ___|___            
	           |    |     |  |    |       |
	           D    E     F  G    H       I
	        ___|___               |
	       |       |              |
	       J       K              L
	               |
	               M
	               |
	               N                                           
	
	*/

	static public void main(String args[])
	{
		int ii;

		// I DONT want to use an array.  I want control over each letter
		NamedGroup root= new NamedGroup("root");
		NamedGroup A= new NamedGroup("A");
		NamedGroup B= new NamedGroup("B");
		NamedGroup C= new NamedGroup("C");
		NamedGroup D= new NamedGroup("D");
		NamedGroup E= new NamedGroup("E");
		NamedGroup F= new NamedGroup("F");
		NamedGroup G= new NamedGroup("G");
		NamedGroup H= new NamedGroup("H");
		NamedGroup I= new NamedGroup("I");
		NamedGroup J= new NamedGroup("J");
		NamedGroup K= new NamedGroup("K");
		NamedGroup L= new NamedGroup("L");
		NamedGroup M= new NamedGroup("M");
		NamedGroup N= new NamedGroup("N");

		//root.checkValidity();

		root.addChild(A);
		root.addChild(B);
		root.addChild(C);

		A.addChild(D);
		A.addChild(E);
		A.addChild(F);

		B.addChild(G);

		C.addChild(H);
		H.addChild(L);
		C.addChild(I);

		D.addChild(J);
		D.addChild(K);

		H.addChild(L);

		K.addChild(M);
		M.addChild(N);

		// Lookey here.
		// This is kinda tricky
		// We're using an anonymous class to define a doAction() for the abstract
		// class BaseGroupAction
		new BaseTraversalAction()
		{
			protected boolean actionImplementation(iNamedObject node)
			{
				System.out.println(node.getName());
				return true;
			}
		}
		.applyAction(root);

		System.exit(0);
	}
}
