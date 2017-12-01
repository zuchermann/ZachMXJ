package innards.namespace.context;


/**
	these are tools for doing maths  with things - manily floats and things that can provide floats

	*/

public class MathContextTree
	extends ContextTree
{

	public interface Combiner
	{
		public void start();
		public void add(float value);
		public float end();
	}

	static public class Accumulator
		implements MathContextTree.Combiner
	{
		protected float soFar;
		protected boolean nothing = true;
		public void start()
		{
			soFar = 0;
		}
		public void add(float value)
		{
			soFar = value;
		}
		public float end()
		{
			return soFar;
		}
	}

	static public class Average
		extends Accumulator
	{
		int num = 0;
		public void start()
		{
			super.start();
			num = 0;
		}
		public void add(float value)
		{
			soFar += value;
			num ++;
		}
		public float end()
		{
			return soFar / num;
		}
	}

	static public class WeightedAverage
		extends Accumulator
	{
		float exponent = 0.5f;
		float num = 0;
		float weight = 1;

		/**
			1 - all things weight the same
			2 - children are worth twice what their parents are worth
			0.5-children are worth half that
			*/
		public WeightedAverage(float exponent)
		{
			this.exponent = 1/exponent;
		}
		public WeightedAverage()
		{
		}
		public void start()
		{
			super.start();
			num = 0;
			weight = 1;
		}
		public void add(float value)
		{
			soFar += value*weight;
			num += weight;
			weight *= exponent;
		}
		public float end()
		{
			return soFar / num;
		}
	}

	static public float getFloat(Object key, MathContextTree.Combiner comb){return getFloat(key,comb,true);}

	static public float getFloat(Object key, MathContextTree.Combiner comb, boolean allNullIsBad)
	{
		boolean never = true;
		Bobj aat = getAt();
		comb.start();
		while(aat!=null)
		{
			Object o = aat.get(key);
			if (o!=null)
			{
				float f = resolveNumber(o);
				comb.add(f);
				never = false;
			}
			aat = (Bobj)aat.getParent();
		}
		if (allNullIsBad && never)
		{
			System.err.println(" boo.getFloat comb failed in dir:"+ContextTree.pwd());
			System.err.println("   dir is <"+ContextTree.dir());
			throw new NullPointerException(" looking for get <"+key+"> of class <"+key.getClass().getName()+"> found nothing ");
		}
		return comb.end();
	}
}
