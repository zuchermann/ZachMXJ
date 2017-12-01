/*
 * Created on Oct 10, 2003
 */
package innards.provider.network;

import innards.math.linalg.Vec;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashMap;

/**
 * @author marc
 */
public class NIONetworkGroup implements Runnable {

	private DatagramChannel channel;
	private DatagramSocket socket;
	private InetSocketAddress address;
	private ByteBuffer buffer;

	public NIONetworkGroup(int port) {
		try {
			channel = DatagramChannel.open();
			socket = channel.socket();
			address = new InetSocketAddress(port);
			channel.configureBlocking(true);
			channel.socket().bind(address);
			buffer = ByteBuffer.allocateDirect(NIONetworkOutputGroup.max_packet_size);
			socket.setReceiveBufferSize(NIONetworkOutputGroup.max_packet_size);
			(new Thread(this)).start();
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	HashMap dispatchTable = new HashMap();
	AnyHandler defaultHandler = null;

	public interface AnyHandler {
	}

	public interface Handler extends AnyHandler {
		public void handle(String name, float f);
	}

	public interface VecHandler extends AnyHandler {
		public void handle(String name, Vec v);
	}
	public Handler register(Handler h, String name) {
		Handler ha = (Handler) dispatchTable.put(name, h);
		return ha;
	}
	public VecHandler register(VecHandler h, String name) {
		VecHandler ha = (VecHandler) dispatchTable.put(name, h);
		return ha;
	}

	public AnyHandler registerDefaultHandler(AnyHandler h) {
		AnyHandler old = defaultHandler;
		defaultHandler = h;
		return old;
	}

	public void run() {
		DatagramSocket sock = channel.socket();
		while (!sock.isClosed()) {
			buffer.clear();
			try {
				channel.receive(buffer);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			decode(buffer);
		}
	}

	protected void decode(ByteBuffer b) {
		b.rewind();
		int dim = decodeInt(b);
		if (dim == 1) {
			float value = decodeFloat(buffer);
			String name = decodeString(buffer);
			set(name, value);
		} else {
			Vec v = new Vec(dim);
			for (int i = 0; i < dim; i++) {
				float val = decodeFloat(buffer);
				v.set(i, val);
			}
			String name = decodeString(buffer);
			System.out.println(" net double provider got vec <" + v + ">");
			set(name, v);
		}
	}

	public void set(String name, float value) {
		// find it
		Object o = dispatchTable.get(name);
		if (o == null)
			o = defaultHandler;
		if (o instanceof Handler) {
			Handler handler = (Handler) o;
			handler.handle(name, value);
		} else if (o instanceof VecHandler) {
			VecHandler handler = (VecHandler) o;
			Vec v = new Vec(1);
			v.set(0, value);
			handler.handle(name, v);
		} else {
			System.out.println("networkdoubleprovider: warning: couldn't find handler called <" + name + ">");
		}
	}

	public void set(String name, Vec value) {
		// find it
		Object o = dispatchTable.get(name);
		if (o == null)
			o = defaultHandler;
		if (o instanceof Handler) {
			Handler handler = (Handler) o;
			handler.handle(name, (float) value.get(0));
		} else if (o instanceof VecHandler) {
			VecHandler handler = (VecHandler) o;
			handler.handle(name, value);
		} else {
			System.out.println("networkdoubleprovider: warning: couldn't find handler called <" + name + ">");
		}
	}

	public static float decodeFloat(ByteBuffer from) {
		return Float.intBitsToFloat(decodeInt(from));
	}

	public static int decodeInt(ByteBuffer from) {
		int i = (from.get() << 24) & (255 << 24);
		i += (from.get() << 16) & (255 << 16);
		i += (from.get() << 8) & (255 << 8);
		i += (from.get()) & 255;
		return i;
	}

	public static String decodeString(ByteBuffer from) {
		char[] result;
		int string_length = from.get();
		result = new char[string_length];

		for (int i = 0; i < result.length; i++) {
			result[i] = (char) ((from.get()<< 8) & (255 << 8));
			result[i] += (from.get()) & 255;
		}

		return new String(result);
	}
}
