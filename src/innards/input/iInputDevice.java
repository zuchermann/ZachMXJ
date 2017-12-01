package innards.input;

import innards.*;
import innards.data.*;

/**
 * the interface for any device which provides input into the system <br>
 * an input device's name should be unique.
 * 
 * @see innards.input.InputDispatcher
 */
public interface iInputDevice extends iNamedObject
{
    /**
	 * returns the input for the device.  the type of data returned is up to the
	 * implementing device, and may be null if no input has been received.
	 */
	public iDataRecord getInput();
	
	/**
	 * returns the key that this iInputDevice will label its iDataRecord (passed back by getInput) with
	 */
	public Key getDataRecordKey();
}
