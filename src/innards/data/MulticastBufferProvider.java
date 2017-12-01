package innards.data;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author mattb
 */
public class MulticastBufferProvider implements iSocketBufferProvider
{
	protected MulticastSocket multicastSocket;
	protected DatagramPacket packet;
	
	public MulticastBufferProvider(MulticastSocket multicastSocket)
	{
		this.multicastSocket = multicastSocket;
		this.packet = new DatagramPacket(new byte[]{0}, 1);
	}

	public SocketAddress receiveBuffer(ByteBuffer destinationBuffer) throws IOException
	{
		packet.setData(destinationBuffer.array());
		multicastSocket.receive(packet);
		destinationBuffer.position(packet.getLength());
		return packet.getSocketAddress();
	}

	public DatagramSocket getSocket()
	{
		return multicastSocket;
	}
}
