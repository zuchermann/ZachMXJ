package innards.util;

import java.io.Serializable;

/**
	general utility classes exploiting template classes
	
	actually, the templateness was removed for eclipse.
 */

public class Generics
{
	// maths, vec(n,2,3), numbers, matrices, quaternions, 2d angles and coordinate systems can all, in Object way, do these things
	static public interface Blendable/*/*<A>*/
	{
		public void add(Blendable/*<A>*/ one, Blendable/*<A>*/ two, Blendable/*<A>*/ result);
		public void blend(Blendable/*<A>*/ from, Blendable/*<A>*/ two, double alpha, Blendable/*<A>*/ result);
		public void scalarMultiply(Blendable/*<A>*/ from, double by, Blendable/*<A>*/ result);
	}

	// other
	static public interface iObjectProvider/*<A>*/
	{
		public Object get();
	}
	
	static public interface iInplaceProvider/*<A>*/
	{
		public void get(Object a);
	}

	static public interface iFactory/*<A,B>*/
	{
		public Object produce(Object b);
	}

	static public interface Metric/*<A>*/
	{
		public float distance(Object edge);
	}

	/** for when you want to key off of two things in Object HashMap, or Object Set
	*/
	static public class Pair implements Serializable/*<A,B>*/
	{
		public Object left;
		public Object right;

		public Pair(Object a, Object b)
		{
			left = a;
			right = b;
		}
		
		public boolean equals(Object o)
		{
			if (!(o instanceof Pair)) return false;
			Pair p = (Pair)o;
			if  ( (((left==null) && (p.left==null)) || (left.equals(p.left)))
				&& (((right==null) && (p.right==null)) || (right.equals(p.right)))) return true;
			return false;
		}

		public int hashCode()
		{
			int l = (left==null ? 0 : left.hashCode());
			int r = (right==null ? 0 : right.hashCode());
			return l+r;
		}
		
		public String toString()
		{
			return "left: <" + left + "> right: <" + right + " >";
		}
	}

	static public class Triplet/*<A,B,C>*/
	{
		public Object one;
		public Object  two;
		public Object three;
		
		public Triplet(Object a, Object b, Object c)
		{
			one = a;
			two = b;
			three = c;
		}

		public boolean equals(Object o)
		{
			if (!(o instanceof Pair)) return false;
			Triplet p = (Triplet)o;
			if  ( (((one==null) && (p.one==null)) || (one.equals(p.one)))
				&& (((two==null) && (p.two==null)) || (two.equals(p.two)))
				&& (((three==null) && (p.three==null)) || (three.equals(p.three)))
				) return true;
			return false;
		}

		public int hashCode()
		{
			int l = (one==null ? 0 : one.hashCode());
			int r = (two==null ? 0 : two.hashCode());
			int m = (three==null ? 0 : three.hashCode());
			return l+r+m;
		}
		
	}
}
