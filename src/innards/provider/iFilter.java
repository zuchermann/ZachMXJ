package innards.provider;


/**
	a filternetwork wraps up a bunch of iDoubleProviders
	*/

public interface iFilter
{
	public float filter(float value);
}
