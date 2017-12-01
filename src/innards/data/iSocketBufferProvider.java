package innards.data;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * this is a superinterface for providing uniform, NIO-style access to sockets such as
 * UDP unicast and multicast sockets - once there is NIO support for UDP multicast,
 * this will most likely be no longer necessary
 * 
 * @author mattb
 */
public interface iSocketBufferProvider
{
	/**
	 * receives a datagram, coping it into the destination buffer starting at its
	 * current position.  if the buffer does not have enough space available to hold the
	 * datagram, the remainder of the datagram is silently discarded.
	 * 
	 * @param destinationBuffer the buffer into which the datagram is to be transferred
	 * @return the datagram's source address, or null if the channel is in non-blocking mode
	 * and no datagram was immediately available
	 */
	public SocketAddress receiveBuffer(ByteBuffer destinationBuffer) throws IOException;
	
	/**
	 * @return the underlying datagram socket
	 */
	public DatagramSocket getSocket();
}