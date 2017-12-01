package innards.input.device;

import innards.Key;
import innards.data.*;
import innards.debug.Debug;
import innards.input.*;

import java.util.*;

/**
 * An <code>InputDevice</code> which wraps an arbitrary number
 * of member <code>iInputDevices</code>.
 * <p>
 * Data for all of the member devices is returned bundled into a single
 * 'super' <code>iDataRecord</code>.  The data for a particular device is
 * indexed in the super data record by the device's key.
 * 
 * @see InputDevice
 * @see iDataRecord
 * 
 * @author synchar
 */
public class MultiInputDevice extends InputDevice
{	
	protected ArrayList inputDevices = new ArrayList();
	
	public MultiInputDevice(String name, Key key)
	{
		super(name, key);
	}
	
	
	/**
	 * Add a member <code>iInputDevice</code> to the
	 * <code>MultiInputDevice</code>.
	 */
	public void add(iInputDevice device)
	{
		inputDevices.add(device);
	}
	
	/**
	 * Remove a member <code>iInputDevice</code> from the
	 * <code>MultiInputDevice</code>.
	 * @return boolean True if the device was succesfully removed, false if it
	 * was not found.
	 */
	public boolean remove(iInputDevice device)
	{
		return inputDevices.remove(device);
	}

	/**
	 * Gets the latest input from each member device, then bundles all of the
	 * results into a single output <code>iDataRecord</code>.
	 * @see innards.input.InputDevice#getInput()
	 * @return innards.iDataRecord Data for each member device is indexed by
	 * that device's key.
	 */
	protected WritableDataRecord internalGetInput(WritableDataRecord wdr)
	{
		HashMap dataFromDevices = new HashMap();
		for (Iterator iter = inputDevices.iterator(); iter.hasNext();)
		{
			// wdr hashmaps expect Keys vs Objects not Strings vs Object - no? marc. changed from getName(), to getKey()
			iInputDevice device = (iInputDevice)iter.next();
			Key key = device.getDataRecordKey();
			iDataRecord record = device.getInput();
			Debug.doAssert(key==record.getKey(), "key declared: " + key.toString() + " is not key used: " + record.getKey());
			
			dataFromDevices.put(key , record);
		}
		if (!dataFromDevices.isEmpty())
		{
			wdr.setMap(dataFromDevices);
			return wdr;
		}
		else
		{
			return null;
		}
	}
}
