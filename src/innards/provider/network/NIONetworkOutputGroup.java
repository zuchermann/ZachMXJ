/*
 * Created on Oct 10, 2003
 */
package innards.provider.network;

import innards.NamedObject;
import innards.math.linalg.Vec;
import innards.util.*;

import java.io.IOException;
import java.lang.reflect.*;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * @author marc
 */
public class NIONetworkOutputGroup extends NamedObject {
	public static String DEFAULT_GROUP = InnardsDefaults.getProperty("NetworkDoubleProvider.group.output", "239.45.89.15");
	public static int DEFAULT_PORT = InnardsDefaults.getIntProperty("NetworkDoubleProvider.port.output", 1230);
	public final static byte TIME_TO_LIVE = 1;

	public static int max_packet_size = 100;

	ByteBuffer outputBuffer;

	protected int port = 0;
	private InetSocketAddress ip;
	private DatagramChannel channel;
	private DatagramSocket socket;

	public NIONetworkOutputGroup() {
		this(DEFAULT_GROUP, DEFAULT_PORT);
	}

	public NIONetworkOutputGroup(String dest, int port) {
		super("MulticastTransmitter");

		this.port = port;

		outputBuffer = ByteBuffer.allocateDirect(max_packet_size);

		ip = new InetSocketAddress(dest, port);

		try {
			channel = DatagramChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}
		socket = channel.socket();
		try {
			socket.setBroadcast(true);
			socket.bind(null);
			socket.setSendBufferSize(NIONetworkOutputGroup.max_packet_size);
		} catch (SocketException e1) {
			e1.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}
	}

	public void send(String name, float value) {
		outputBuffer.rewind();
		outputBuffer.limit(outputBuffer.capacity());
		encode(outputBuffer, 1);
		encode(outputBuffer, value);
		encodeString(outputBuffer, name);
		outputBuffer.limit(outputBuffer.position());
		outputBuffer.rewind();
		try {
			channel.send(outputBuffer, ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void send(String name, Vec vec) {
		outputBuffer.rewind();
		outputBuffer.limit(outputBuffer.capacity());

		encode(outputBuffer, vec.dim());
		for (int i = 0; i < vec.dim(); i++) {
			encode(outputBuffer, (float) vec.get(i));
		}
		encodeString(outputBuffer, name);
		outputBuffer.limit(outputBuffer.position());
		outputBuffer.rewind();
		try {
			channel.send(outputBuffer, ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void encode(ByteBuffer buffer, float me) {
		encode(buffer,  Float.floatToRawIntBits(me));
	}

	public static void  encode(ByteBuffer to, int me) {
		to.put((byte) (me >> 24));
		to.put((byte) ((me >> 16) & 255));
		to.put((byte) ((me >> 8) & 255));
		to.put((byte) (me & 255));
	}

	public static void  encodeString(ByteBuffer to,  String me) {
		to.put((byte) (me.length()));
		int conv;
		for (int i = 0; i < me.length(); i++) {
			conv = (int) (me.charAt(i));
			to.put((byte) ((conv >> 8) & 255));
			to.put((byte) (conv & 255));
		}	}
}
