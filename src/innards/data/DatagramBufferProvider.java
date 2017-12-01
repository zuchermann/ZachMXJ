package innards.data;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author mattb
 */
public class DatagramBufferProvider implements iSocketBufferProvider
{
	protected DatagramChannel theDatagramChannel;
	
	public DatagramBufferProvider(DatagramChannel datagramChannel)
	{
		this.theDatagramChannel = datagramChannel;
	}

	public SocketAddress receiveBuffer(ByteBuffer destinationBuffer) throws IOException
	{
		return theDatagramChannel.receive(destinationBuffer);
	}

	public DatagramSocket getSocket()
	{
		return theDatagramChannel.socket();
	}
}
