package innards.namespace;

import innards.*;

import java.util.*;

/**
 * for 'uniquely' having a reference to an object in a hierarchy based on names and child numbers. this is useful for presistance and visualizeation
 * @author marc
 * <I>Created on Feb 28, 2003</I>
 */
public class NamedGroupPathName
{
	protected List path;

	/**
	 * goes all the way up the parent chain from object to root, saving as it goes
	 * @param blendedObject
	 * @param root
	 */
	public NamedGroupPathName(List path)
	{
		this.path= path;
	}

	public NamedGroupPathName()
	{
		path= new ArrayList();
	}

	public iNamedObject find(iNamedGroup root, boolean weak)
	{
		iNamedGroup at= root;
		for (int i= 0; i < path.size(); i++)
		{
			iNamedObject o= findChild(at, ((Element) path.get(i)).name, ((Element) path.get(i)).childNumber, weak);
			if (o == null)
				return null;
			if (o instanceof iNamedGroup)
				at= (iNamedGroup) o;
			else if (i != path.size() - 1)
			{
				return null;
			}
			else
				return o;
		}
		return at;
	}

	protected iNamedObject findChild(iNamedGroup from, String name, int number, boolean weak)
	{
		// first of all, how many children are called 'name'
		int numChildren= 0;
		List l= from.getChildrenList();
		iNamedObject last= null;
		float  diff = Float.POSITIVE_INFINITY;
		for (int i= 0; i < l.size(); i++)
		{
			if (((iNamedObject) l.get(i)).getName().equals(name))
			{
				numChildren++;
				if (Math.abs(i-number)<diff)
				{
					last= ((iNamedObject) l.get(i));
					diff = Math.abs(i-number);
				} 
			}
		}
		if (numChildren==1)
		{
			// easy, we've found it - this is the best that we can do, even if the number is wrong
			return last;
		}
		else if (numChildren==0)
		{
			// easy, we haven't found it - we can do no better than this (unless we want to guess the number)
			if (weak)
			{
				if (from.getNumChildren()>number) return from.getChild(number);
			}
			return null;
		}
		else
		{
			return last;
		}
	}

	static public NamedGroupPathName fromNamedObject(iNamedObject object, iNamedGroup root)
	{
		List l= new ArrayList();
		iNamedObject at= object;
		while (at != root)
		{
			iNamedGroup parent= at.getParent();
			if (parent==null) System.out.println(" looking for parent that was <"+root+"> couldn't get past <"+at+"> children of root are <"+root.getChildrenList()+"> at <"+at.hashCode()+">");
			int num= parent.getChildrenList().indexOf(at);
			Element element= new Element(at.getName(), num);
			l.add(0, element);
			at= parent;
		}
		return new NamedGroupPathName(l);
	}

	/**
	 * something:num/somethingElse:num
	 * @param path
	 * @return NamedGroupPathName
	 */
	static public NamedGroupPathName fromExternalizedString(String path)
	{
		StringTokenizer tokenizer= new StringTokenizer(path, "/:");
		List l= new ArrayList();
		while (tokenizer.hasMoreTokens())
		{
			String name= tokenizer.nextToken();
			int num= Integer.parseInt(tokenizer.nextToken());
			l.add(new Element(name, num));
		}
		return new NamedGroupPathName(l);
	}

	public String toExternalizedString()
	{
		String s= new String();
		for (Iterator i= path.iterator(); i.hasNext();)
		{
			Element element= (Element) i.next();
			s += element.name + ":" + element.childNumber + "/";
		}
		return s;
	}

	static public class Element
	{
		String name;
		int childNumber;
		public Element(String name, int childNumber)
		{
			this.name= name;
			this.childNumber= childNumber;
		}
	}

}
