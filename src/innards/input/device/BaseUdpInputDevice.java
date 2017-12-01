

package innards.input.device;

import innards.Key;
import innards.data.*;
import innards.input.InputDevice;

import java.net.InetAddress;

/**
	bridges the world of udp and InputDispatcher

	subclass for functionality
	
	@author marc
*/

public abstract class BaseUdpInputDevice extends InputDevice implements UDPListener.Handler
{

	UDPListener listener;

	protected iDataRecord latestDataRecord= null;
	iDataRecord lastSentDataRecord= null;
	protected Object latestIntermediate;

	public BaseUdpInputDevice(String sName, Key key, int portNumber, int maxPacketLength)
	{
		super(sName, key);

		listener= new UDPListener(portNumber, maxPacketLength);
		init();
	}

	protected WritableDataRecord internalGetInput(WritableDataRecord wdr)
	{
		synchronized (this)
		{
			if (latestIntermediate != null) // fixme  should be lastSentDataRecord ? 
			{
				wdr = populateDataRecord(latestIntermediate, wdr);
				latestIntermediate= null;
				return wdr;
			}
			else
			{
				return null;
			}
		}
	}

	/* Start the Input Device up*/
	private void init()
	{
		System.out.println(" BaseUdpInputDevice <" + this.getClass().getName() + ":" + this.getName() + "> starting up ");
		listener.addHandler(this);
	}
	
	public void handle(byte[] contents, int length, InetAddress from)
	{
		synchronized (this)
		{
			Object dr= this.createIntermediateObject(latestIntermediate, contents, length, from);
			latestIntermediate = dr;
		}
	}


	// subclass interface ----------------------------------

	/**
	 * creates an 'intermediate object' based on this byte[] (the byte[] from the udp packet).
	 * later (when the main thread asks for getInput()) subclasses will have an opportunity to populate  a WriteableDataRecord, perhaps with
	 * something based on this object.
	 * <p>
	 * the 'currentIntermediate' is also passed in, giving you the opportunity to use a list or something, to handle the moments when multiple inputs
	 * are recieved between calls to getInput(...). This currentIntermediate is  cleared before getInput  returns.
	 * if you return null, you will never get the change to populateDataRecord(...)
	 * <b>called from within network server thread!</b>
	 */
	abstract protected Object createIntermediateObject(Object currentIntermediate, byte[] data, int length, InetAddress from);

	/**
	 * given an intermediate object (created through createIntermediateObject), populate a datarecord.
	 * return null to abort the creation of the  data record. otherwise, return the WriteableDataRecord, populated, that is passed in.
	 * called from within world thread
	 */
	abstract protected WritableDataRecord populateDataRecord(Object intermediate, WritableDataRecord wdrt);
}
