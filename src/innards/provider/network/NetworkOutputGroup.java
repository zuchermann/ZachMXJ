package innards.provider.network;

import innards.NamedObject;
import innards.math.linalg.Vec;
import innards.util.InnardsDefaults;

import java.net.*;

public class NetworkOutputGroup extends NamedObject
{
	public static String DEFAULT_GROUP= InnardsDefaults.getProperty("NetworkDoubleProvider.group.output", "239.45.89.15");
	public static int DEFAULT_PORT= InnardsDefaults.getIntProperty("NetworkDoubleProvider.port.output", 1230);
	public final static byte TIME_TO_LIVE= 1;

	protected DatagramPacket dp;
	protected InetAddress multicastGroup= null;

	protected MulticastSocket outputSocket= null;
	protected int port= 0;

	protected DatagramPacket stringPacket;

	public NetworkOutputGroup()
	{
		this(DEFAULT_GROUP, DEFAULT_PORT);
	}

	public NetworkOutputGroup(String group, int port)
	{
		super("MulticastTransmitter");

		this.port= port;
		try
		{
			outputSocket= new MulticastSocket(port);
			outputSocket.setSendBufferSize(NetworkGroup.MAX_PACKET_SIZE*5);
		} catch (Exception ex)
		{
			System.out.println("unable to make a multicast socket on port:" + port + " ?\n");
		}

		try
		{
			multicastGroup= InetAddress.getByName(group);
		} catch (Exception ex)
		{
			System.out.println("Unable to resolve address:" + group + " ?\n" + ex);
		}
		dp= new DatagramPacket(new byte[NetworkGroup.MAX_PACKET_SIZE], NetworkGroup.MAX_PACKET_SIZE, multicastGroup, port);
	}

	public void send(String name, float value)
	{
		byte[] buffer= dp.getData();
		encode(buffer, 0, 1);
		encode(buffer, 4, value);
		encodeString(buffer, 8, name);
		try
		{
			int ttl= outputSocket.getTimeToLive();
			outputSocket.setTimeToLive(TIME_TO_LIVE);
			outputSocket.send(dp);
			outputSocket.setTimeToLive(ttl);
		} catch (Exception ex)
		{
			System.out.println("Error sending packet!: " + ex);
		}
	}

	public void send(String name, Vec vec)
	{
		byte[] buffer= dp.getData();
		encode(buffer, 0, vec.dim());
		for (int i= 0; i < vec.dim(); i++)
		{
			encode(buffer, 4 + 4 * i, (float) vec.get(i));
		}
		encodeString(buffer, 4 + 4 * vec.dim(), name);
		try
		{
			int ttl= outputSocket.getTimeToLive();
			outputSocket.setTimeToLive(TIME_TO_LIVE);
			outputSocket.send(dp);
			outputSocket.setTimeToLive(ttl);
		} catch (Exception ex)
		{
			System.out.println("Error sending packet!: " + ex);
		}
	}

	public static int encode(byte[] to, int at, float me)
	{
		return encode(to, at, Float.floatToRawIntBits(me));
	}

	public static int encode(byte[] to, int at, int me)
	{
		to[at]= (byte) (me >> 24);
		to[at + 1]= (byte) ((me >> 16) & 255);
		to[at + 2]= (byte) ((me >> 8) & 255);
		to[at + 3]= (byte) (me & 255);
		return at + 4;
	}

	public static int encodeString(byte[] to, int at, String me)
	{
		to[at++]= (byte) (me.length());
		int conv;
		for (int i= 0; i < me.length(); i++)
		{
			conv= (int) (me.charAt(i));
			to[at++]= (byte) ((conv >> 8) & 255);
			to[at++]= (byte) (conv & 255);
		}
		return at;
	}
}
