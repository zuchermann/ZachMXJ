package innards.namespace;

import innards.*;

public class FindByKey extends FindByPredicate implements Cloneable 
{   
	protected Key key;
	public FindByKey(Key key)
	{
		this.key = key;
	}    

	public boolean is(iNamedObject no) {
		return key == no.getKey();
	}
}