package innards.input;

import innards.Key;
import innards.data.iDataRecord;
import innards.debug.Debug;
import innards.iUpdateable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 * A singleton object which provides a centralized clearinghouse for data from
 * <code>iInputDevices</code>.
 * <p>
 * Each <code>InputDevice</code> should be registered with the
 * <code>InputDispatcher</code>.  <code>iDataRecordHandlers</code> can then
 * register themselves to receive input from particular devices, which are
 * referenced by name.
 * 
 * @see innards.input.InputDevice
 * @see innards.input.iDataRecordHandler
 * @author synchar
 */
public class InputDispatcher implements iUpdateable
{
	protected static InputDispatcher theDispatcher = new InputDispatcher();
	
	/**
	 * Return the <code>InputDispatcher</code> object. Remember! -- there is
	 * only ever one <code>InputDispatcher</code>.
	 */
	public static InputDispatcher getInputDispatcher()
	{
		return theDispatcher;
	}
	
	protected HashMap<String, iInputDevice> inputDevices = new HashMap<String, iInputDevice>();
	protected HashMap<String, ArrayList> handlerChains = new HashMap<String, ArrayList>();
	protected HashMap<String, ArrayList> cascadeFlagChains = new HashMap<String, ArrayList>();
	
	protected InputDispatcher()
	{
	}
	
	/**
	 * Register an <code>InputDevice</code> with the
	 * <code>InputDispatcher</code>.
	 */
	public void addInputDevice(iInputDevice device)
	{
		inputDevices.put(device.getName(), device);
		handlerChains.put(device.getName(), new ArrayList());
		cascadeFlagChains.put(device.getName(), new ArrayList());
	}
	
	public iInputDevice getInputDevice(String name) {
		return inputDevices.get(name);
	}
	
	/**
	 * Deregister an <code>InputDevice</code> with the
	 * <code>InputDispatcher</code>.  Throws an assertion if the argument device
	 * was not previously registered.
	 */
	public void removeInputDevice(iInputDevice device)
	{
		Debug.doAssert(inputDevices.containsKey(device.getName()), "input device <" + device.getName() + "> not found.");
		inputDevices.remove(device.getName());
	}

	/**
	 * Query to see if device exists - useful if you want the data if available, but don't want
	 * to create an error if its not.
	 */
	public boolean hasInputDevice(String deviceName){
		return inputDevices.containsKey(deviceName);
	}

	/**
	 * Register a new <code>iDataRecordHandler</code> to receive input from a
	 * particular <code>InputDevice</code>.  Using this version of the method
	 * results in default cascading behavior (can cascade) for the registered
	 * handler.
	 * @param handler 
	 * @param inputDeviceName The name of the <code>InputDevice</code> which
	 * the argument handler wants to receive data from.
	 */
	public void addDataRecordHandler(iDataRecordHandler handler, String inputDeviceName)
	{
		addDataRecordHandler(handler, inputDeviceName, true);
	}

	/**
	 * Register a new <code>iDataRecordHandler</code> to receive input from a
	 * particular <code>InputDevice</code>.
	 * @param handler 
	 * @param inputDeviceName The name of the <code>InputDevice</code> which
	 * the argument handler wants to receive data from.
	 * @param canCascadeDataRecords If true, the handler will always pass
	 * <code>iDataRecords</code> which it receives on to later handlers in the
	 * chain.  If false, the <code>iDataRecords</code> are passed on only if the
	 * handler fails to handle them.
	 */
	public void addDataRecordHandler(iDataRecordHandler handler, String inputDeviceName, boolean canCascadeDataRecords)
	{
		Debug.doAssert(inputDevices.containsKey(inputDeviceName), "input device <" + inputDeviceName + "> not found.");
		((ArrayList)handlerChains.get(inputDeviceName)).add(handler);
		((ArrayList)cascadeFlagChains.get(inputDeviceName)).add(new Boolean(canCascadeDataRecords));
	}

    /**
     * Remove a <code>iDataRecordHandler</code> from the chain of handlers to receive
     * input from a device
     *
     * @param handler
     * @param inputDeviceName
     */
    public void removeDataRecordHandler(iDataRecordHandler handler, String inputDeviceName)
    {
        Debug.doAssert(inputDevices.containsKey(inputDeviceName), "input device <" + inputDeviceName + "> not found.");

        int index = ((ArrayList)handlerChains.get(inputDeviceName)).indexOf(handler);
        Debug.doAssert(index != -1, "Handler <"+ handler + "> does not handle input device <" + inputDeviceName + ">");

        ((ArrayList)handlerChains.get(inputDeviceName)).remove(handler);
        ((ArrayList)cascadeFlagChains.get(inputDeviceName)).remove(index);

    }
	/**
	 * Polls each <code>InputDevice</code> for its latest input, then passes
	 * the input to all of the <code>iDataHandlers</code> which are registered
	 * to receive data from that device.
	 * @see innards.iUpdateable#update()
	 */
	public void update()
	{
		for (Iterator<iInputDevice> devices = inputDevices.values().iterator(); devices.hasNext();)
		{
			iInputDevice device = (iInputDevice)devices.next();
			iDataRecord dr = device.getInput();
			Key key = device.getDataRecordKey();
			
			ArrayList dataRecordHandlers = handlerChains.get(device.getName());
			ArrayList cascadeFlags = cascadeFlagChains.get(device.getName());
			
			if (dr != null)
			{
				Debug.doAssert(key==dr.getKey(), "key declared is not key used <"+key+"> <"+dr.getKey()+">");
				
				int i = 0;
				boolean canCascade = true;
				boolean handledData;
				while (canCascade && (i < dataRecordHandlers.size()))
				{
					handledData = ((iDataRecordHandler)dataRecordHandlers.get(i)).handleDataRecord(dr);
					canCascade = ((!handledData) || ((Boolean)cascadeFlags.get(i)).booleanValue());
					i++;
				}
			}			
		}
	}
}
