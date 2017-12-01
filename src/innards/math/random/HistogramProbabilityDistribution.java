package innards.math.random;

import innards.NamedObject;
import innards.math.BaseMath;
import innards.util.iChangePollable;

/**
    Edited by Rob to have it deal with negative numbers as follows:
    - add the minvalue to each of the bins before selection so that all values are non-negative
    
    */

public class HistogramProbabilityDistribution extends NamedObject implements Histogram, iChangePollable
{
	// rep
	private float[] data;
	private float[] cumData;
	private int size;

	public HistogramProbabilityDistribution copy()
	{
		HistogramProbabilityDistribution hpd= new HistogramProbabilityDistribution(size);
		for (int i= 0; i < size; i++)
		{
			hpd.data[i]= data[i];
			hpd.cumData[i]= cumData[i];
		}
		return hpd;
	}

	// @@@benresfixme -- oh gosh, this should certainly be better.
	public boolean getChanged()
	{
		return true;
	}

	public void setChanged(boolean val)
	{
	}
	public HistogramProbabilityDistribution(int size)
	{
		super("hpd");
		setSize(size);
	}

	public HistogramProbabilityDistribution(int size, String name)
	{
		super(name);
		setSize(size);
	}

	public HistogramProbabilityDistribution(String name, float[] data)
	{
		super(name);
		size= data.length;
		this.data= data;
		cumData= new float[size];
		for (int i= 0; i < size; i++)
			cumData[i]= 0;
	}

	public void setSize(int size)
	{
		data= new float[size];
		cumData= new float[size];
		this.size= size;
		for (int i= 0; i < size; i++)
			data[i]= 0;
		for (int i= 0; i < size; i++)
			cumData[i]= 0;
	}

	public int getSize()
	{
		return size;
	}

	public void setProbability(int index, float at)
	{
		data[index]= at;
	}
	public float getProbability(int index)
	{
		return data[index];
	}

	public int select()
	{
		float[] oldData= new float[data.length];
		float minValue= Float.MAX_VALUE;

		for (int i= 0; i < size; i++)
		{
			oldData[i]= data[i];
			if (data[i] < 0)
				data[i]= 0;
		}

		cumData[0]= data[0];
		for (int i= 1; i < size; i++)
		{
			cumData[i]= (cumData[i - 1] + data[i]);
		}
		float n= cumData[size - 1];

		if (n < BaseMath.epsilon)
			return (int) (Math.random() * size);

		float sample= n * (float) Math.random();

		// undo any change
		for (int i= 0; i < size; i++)
		{
			data[i]= oldData[i];
		}

		for (int i= 0; i < size; i++)
		{
			if (cumData[i] > sample)
				return i;
		}
		return size - 1;
	}

	public String toString()
	{
		String ret= "";
		for (int i= 0; i < size; i++)
		{
			ret += data[i] + " ";
		}
		return ret;
	}

	public int getNumElements()
	{
		return data.length;
	}

	public String getName(int index)
	{
		return new String("" + index);
	}

	public float getValue(int index)
	{
		return data[index];
	}

	public float[] getValues()
	{
		return data;
	}

	public float getMaxValue()
	{
		int ii;
		float max= 0;

		for (ii= 0; ii < data.length; ii++)
		{
			if (data[ii] > max)
			{
				max= data[ii];
			}
		}
		return max;
	}
}
