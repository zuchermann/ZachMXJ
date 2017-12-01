package innards.namespace;

import java.util.Vector;

import innards.*;

public class FindByName extends FindByPredicate implements Cloneable 
{   
	String name;
    public FindByName(String name)
    {
    	this.name = name;
    }    

    public boolean is(iNamedObject no) {
    	return name.equals(no.getName());
    }
    
    public static NamedObject findFirst(String name, iNamedObject root) {
    	FindByName fbn = new FindByName(name);
    	fbn.mode = FindByPredicate.FIND_FIRST;
    	fbn.applyAction(root);
    	if (fbn.ret.size()>0) {
    		return (NamedObject)(fbn.ret.elementAt(0));
    	}
    	else return null;
    }
    
	public static NamedObject findLast(String name, iNamedObject root) {
    	FindByName fbn = new FindByName(name);
    	fbn.mode = FindByPredicate.FIND_LAST;
    	fbn.applyAction(root);
    	if (fbn.ret.size()>0) {
    		return (NamedObject)(fbn.ret.elementAt(0));
    	}
    	else return null;
    }

    public static Vector findAll(String name, iNamedObject root) {
    	FindByName fbn = new FindByName(name);
    	fbn.mode = FindByPredicate.FIND_ALL;
    	fbn.applyAction(root);
		return fbn.ret;
    }
}
