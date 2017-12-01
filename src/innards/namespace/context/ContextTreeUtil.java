package innards.namespace.context;

import innards.namespace.context.ContextTreeInternals.Bobj;

/**
 * @author marc
 * Created on May 15, 2003
 */
public class ContextTreeUtil
{

	static public class JoinWith implements ContextTreeInternals.iNewContextDelegate
	{
		ContextTree.Bobj with;
		public JoinWith(ContextTree.Bobj with)
		{
			this.with = with;
		}
		public Bobj createAndAttach(String name, Bobj parent)
		{
			ContextTreeInternals.BaseBobj base= new ContextTreeInternals.BaseBobj(name);
			parent.addChild(base);
			ContextTreeSpecial.addPriorParent(with);
			return base;
		}
	}

}
