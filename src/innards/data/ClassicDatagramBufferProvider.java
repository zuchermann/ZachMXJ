package innards.data;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @author jg
 *
 * this is a wrapper around DatagramSockets to make them usuable in all the  code
 * that uses iSocketBufferProviders.  A little messy since its meant for nio, thus meant for bytebuffers.
 * necessary to use non-nio sometimes because of a bug on some computers with non-reusable ports for nio DatagramChannels.
 */
public class ClassicDatagramBufferProvider implements iSocketBufferProvider
{
	protected DatagramSocket theDatagramSocket;
	protected DatagramPacket packet;

	public ClassicDatagramBufferProvider(DatagramSocket datagramSocket)
	{
		this.theDatagramSocket = datagramSocket;
		this.packet = new DatagramPacket(new byte[1], 1);
	}

	//doesn't respect position.  should it?
	public SocketAddress receiveBuffer(ByteBuffer destinationBuffer) throws IOException
	{
		if(destinationBuffer.hasArray()){
			//this part is untested, haven't seen a destinationBuffer with backing array yet.
			packet.setData(destinationBuffer.array(), destinationBuffer.arrayOffset(), destinationBuffer.capacity()+destinationBuffer.arrayOffset());
			packet.setLength(destinationBuffer.capacity());
			theDatagramSocket.receive(packet);
			destinationBuffer.limit(packet.getLength());
		}else{
			if(destinationBuffer.capacity() > packet.getData().length){
				packet.setData(new byte[destinationBuffer.capacity()]);
			}
			packet.setLength(destinationBuffer.capacity());
			theDatagramSocket.receive(packet);
			destinationBuffer.put(packet.getData(), 0, packet.getLength());
			destinationBuffer.limit(packet.getLength());
		}
		return packet.getSocketAddress();
	}

	public DatagramSocket getSocket()
	{
		return theDatagramSocket;
	}
}
