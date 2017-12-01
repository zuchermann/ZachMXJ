package innards.math.random;

/**
	Interface for defining Histograms -- pairs of labels and values.  Very simple.
	"Histograms" are also known as Bar Charts.
*/

public interface Histogram
{

	/**
		Get the name of this histogram.
	*/
	public String getName();

	/** 
		Get the name of element 'index'
	*/
	public String getName(int index);

	/**
		Get the value of element 'index'
	*/
	public float getValue(int index);

	/**
		Returns the number of elements in this histogram
	*/
	public int getNumElements();

	/**
		Get maximum value of this histogram.  Minimum value fixed at zero
	*/
	public float getMaxValue();
}