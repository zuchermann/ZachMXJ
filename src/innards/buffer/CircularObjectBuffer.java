package innards.buffer;
import java.io.Serializable;
import java.util.LinkedList;

/**    
    warning, this class is delta - time ignorant - that is why it
    does not implement DoubleFilter or DoubleProvider
    @author marc
    */

public class CircularObjectBuffer implements Serializable
{
	Object[] dataArray= new Object[1];
	int length= 1;
	int index= 0;
	int numValid= 0;

	public CircularObjectBuffer(int size)
	{
		dataArray= new Object[size];
		length= size;
	}

	/** Initialize by copying the CircularDoubleBuffer passed in */
	public CircularObjectBuffer(CircularObjectBuffer cob)
	{
		synchronized (cob)
		{
			length= cob.length;
			numValid= cob.numValid;
			index= cob.index;
			dataArray= new Object[length];
			for (int c= 0; c < length; c++)
			{
				dataArray[c]= cob.dataArray[c];
			}
		}
	}

	private Object[] getDataArray()
	{
		return dataArray;
	}
	private int getIndex()
	{
		return index;
	}
	public int getNumValid()
	{
		return numValid;
	}
	public int getLength()
	{
		return numValid;
	}
	private void setIndex(int i)
	{
		index= i;
	}
	private void setNumValid(int i)
	{
		numValid= i;
	}
	public int getSize()
	{
		return length;
	}

	/** Perform a shallow copy of the buffer
	*/
	public synchronized CircularObjectBuffer copy()
	{
		CircularObjectBuffer o= new CircularObjectBuffer(this);

		return o;
	}

	/** Returns the exemplars in oldest-to-newest order; returns null if empty
	*/
	public LinkedList getExemplars()
	{
		Object[] toReturn= getExemplarArray();
		if (toReturn == null)
			return null;
		LinkedList ll= new LinkedList();
		for (int i= 0; i < backSize(); i++)
		{
			ll.add(toReturn[i]);
		}
		return ll;
	}

	public Object[] getExemplarArray()
	{
		if (backSize() == 0)
		{
			return null;
		}
		Object[] toReturn= new Object[backSize()];
		for (int i= 0; i < backSize(); i++)
		{
			toReturn[i]= getOffset(i);
		}
		return toReturn;
	}

	public void setSize(int size)
	{
		Object[] newDataArray= new Object[size];

		int minLength= (length > size) ? size : length;
		for (int i= 0; i < minLength; i++)
		{
			newDataArray[i]= internalGet(i - length + 1);
		}
		dataArray= newDataArray;
		index= minLength - 1;
		length= size;
	}

	private Object internalGet(int offset)
	{
		return dataArray[(index + offset + length) % length];
	}

	private void internalSet(int offset, Object value)
	{
		dataArray[(index + offset + length) % length]= value;
	}

	/**
	    returns the value of what comes off the stack 
	    */
	public Object push(Object newValue)
	{
		numValid++;
		if (numValid > length)
			numValid= length;
		Object ret= null;
		if (numValid == length)
			ret= dataArray[index];
		dataArray[index]= newValue;
		index++;
		index %= length;
		return ret;
	}

	/**
	    e.g. getOffset(0) gets the current value
	    */
	public Object getOffset(int positiveSizeBack)
	{
		return internalGet(-1 - positiveSizeBack);
	}

	/**
	    e.g. getOffset(0) gets the current value
	    */
	public void setOffset(int positiveSizeBack, Object value)
	{
		internalSet(-1 - positiveSizeBack, value);
	}

	/**
	    returns how far back it is wise to look 
	    */
	public int backSize()
	{
		return numValid;
	}

	public String toString()
	{
		String ret= "";
		for (int i= 0; i < dataArray.length; i++)
		{
			ret += dataArray[i] + " ";
		}
		ret += "\n index = " + index + " backSize = " + backSize();
		return ret;
	}

}