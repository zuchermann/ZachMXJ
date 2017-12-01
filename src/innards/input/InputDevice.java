package innards.input;

import innards.*;
import innards.data.*;

/**
 * the interface for any device which provides input into the system <br>
 * an input device's name should be unique.
 * 
 * @see innards.input.InputDispatcher
 */
public abstract class InputDevice extends NamedObject implements iInputDevice
{
	/**
	 * All <code>iDataRecords</code> produced by this
	 * <code>InputDevice</code> will be given this key so that they can be
	 * identified downstream.
	 */
	protected Key key;
	
	public InputDevice(String name, Key key)
	{
		super(name);
		this.key = key;
	}
	/**
	 * returns the input for the device.  the type of data returned is up to the
	 * implementing device, and may be null if no input has been received.
	 * <p>
	 * Note that this method is final, in order to enforce the fact that all
	 * returned <code>iDataRecords</code> should have identical keys.
	 * Subclassers should implement internalGetInput to do their thing.
	 */
	public final iDataRecord getInput()
	{
		WritableDataRecord wdr = new WritableDataRecord(key);
		wdr = internalGetInput(wdr);
		if (wdr!=null) return wdr.getReadOnlyView();
		return null;
	}
	
	/**
	 * final to enforce the use of the correct key
	 */
	public final Key getDataRecordKey()
	{
		return key;
	}
	
	/**
	 * Subclassers should implement this in order to return their desired data.
	 * <p>
	 * NOTE!  YOU MUST RETURN THE ARGUMENT <code>WritableaRecord</code> (or
	 * null if you have nothing to add to it). It has been properly keyed/named, so you just need to stick your
	 * data in it.
	 * @param wdr
	 * @return iDataRecord
	 */
	protected abstract WritableDataRecord internalGetInput(WritableDataRecord wdr); 	
}
