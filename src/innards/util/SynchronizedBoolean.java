package innards.util;

/**
 * Thread-synchronized boolean value.
 * For when you want a flag that's shared between multiple threads.
 * 
 * @author mattb
 */
public class SynchronizedBoolean
{
	protected boolean b;
	
	public SynchronizedBoolean(boolean value)
	{
		b = value;		
	}
	
	public synchronized boolean get() { return b; }
	
	public synchronized void set(boolean value) { b = value; }
}