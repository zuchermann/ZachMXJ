package innards.namespace;

import innards.*;

/**
   Interface for describing a recursive action on a NamedGroup
   hierarchy structure.   
*/
public interface iTraversalAction
{
  /**
     Apply the action to this node, then recursively in a
     left-to-right depth first fashion on your children.
  */
  public Object applyAction(iNamedObject root);
}
