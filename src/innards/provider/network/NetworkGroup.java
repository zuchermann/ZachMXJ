package innards.provider.network;
/*
//import innards.namespace.BaseTraversalAction;
import innards.NamedGroup;
*/
import innards.NamedObject;
import innards.math.linalg.Vec;

import java.net.*;
import java.util.HashMap;

/**
 *  takes multicast networked ip messages for double providers and dispatches
 *  them. based on work by jesse
 *
 *@author     marc
 *@created    September 23, 2001
 */

public class NetworkGroup extends NamedObject implements Runnable
{
	
	public static boolean debugArrival = false;
	
	public static int MAX_PACKET_SIZE= 200;
	public static String DEFAULT_GROUP= NetworkOutputGroup.DEFAULT_GROUP;
	public static int DEFAULT_PORT= NetworkOutputGroup.DEFAULT_PORT;
	public final static int THREAD_SLEEP_TIME= 0;

	protected MulticastSocket inputSocket= null;
	protected DatagramPacket dp;

	HashMap dispatchTable= new HashMap();
	AnyHandler defaultHandler= null;

	public interface AnyHandler
	{
	}

	public interface Handler extends AnyHandler
	{
		public void handle(String name, float f);
	}

	public interface VecHandler extends AnyHandler
	{
		public void handle(String name, Vec v);
	}

	public NetworkGroup()
	{
		this(DEFAULT_GROUP, DEFAULT_PORT);
	}

	public NetworkGroup(String group, int port)
	{
		super("Multicast Receiver");
		try
		{
			inputSocket= new MulticastSocket(port);
			inputSocket.setReceiveBufferSize(MAX_PACKET_SIZE*5);
			System.out.println(" system buffer size is <" + inputSocket.getReceiveBufferSize() + ">");
		}
		catch (Exception ex)
		{
			System.out.println("could not make a socket on port:" + port + " ?\n" + ex);
		}
		InetAddress multicastGroup= null;
		try
		{
			multicastGroup= InetAddress.getByName(group);
		}
		catch (Exception ex)
		{
			System.out.println("could not resolve address:" + group + " ?\n" + ex);
		}
		try
		{
			inputSocket.joinGroup(multicastGroup);
		}
		catch (Exception ex)
		{
			System.out.println("could not join group:" + group + " ?\n" + ex);
		}

		byte[] buffer= new byte[MAX_PACKET_SIZE];
		dp= new DatagramPacket(buffer, buffer.length);

		(new Thread(this)).start();
	}

	public Handler register(Handler h, String name)
	{
		Handler ha= (Handler) dispatchTable.put(name, h);
		return ha;
	}
	public VecHandler register(VecHandler h, String name)
	{
		VecHandler ha= (VecHandler) dispatchTable.put(name, h);
		return ha;
	}

	public AnyHandler registerDefaultHandler(AnyHandler h)
	{
		AnyHandler old= defaultHandler;
		defaultHandler= h;
		return old;
	}

	public void run()
	{
		try
		{
			byte[] buffer= dp.getData();
			while (true)
			{
				//gotta reset the length, because length of a datagram is both length of most recent
				//packet received, and the cut off for recieving new data- so it will just keep shrinking
				//on its own.\/
				dp.setLength(MAX_PACKET_SIZE);
				try
				{
					inputSocket.receive(dp);
				}
				catch (Exception ex)
				{
					System.out.println("failed while trying to receive packet!" + ex);
				}
			
				int dim= decodeInt(buffer, 0);
				if (dim == 1)
				{
					// get the flow
					float value= decode(buffer, 4);
					// get the name
					String name= decodeString(buffer, 8);
					if (debugArrival)System.out.println(" net <"+name+"> := <"+value+">");
					set(name, value);
				}
				else
				{
					
					Vec v= new Vec(dim);
					for (int i= 0; i < dim; i++)
					{
						float val= decode(buffer, 4 + 4 * i);
						v.set(i, val);
					}
					String name= decodeString(buffer, 4 + 4 * dim);
					if (debugArrival)System.out.println(" net <"+name+"> := <"+v+">");
					set(name, v);
				}
				try
				{
					Thread.sleep(THREAD_SLEEP_TIME);
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
		catch (RuntimeException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void set(String name, float value)
	{
		// find it
		Object o= dispatchTable.get(name);
		if (o == null)
			o= defaultHandler;
		if (o instanceof Handler)
		{
			Handler handler= (Handler) o;
			handler.handle(name, value);
		}
		else if (o instanceof VecHandler)
		{
			VecHandler handler= (VecHandler) o;
			Vec v= new Vec(1);
			v.set(0, value);
			handler.handle(name, v);
		}
		else
		{
			//System.out.println("networkdoubleprovider: warning: couldn't find handler called <" + name + ">");
		}
	}

	public void set(String name, Vec value)
	{
		// find it
		Object o= dispatchTable.get(name);
		if (o == null)
			o= defaultHandler;
		if (o instanceof Handler)
		{
			Handler handler= (Handler) o;
			handler.handle(name, (float) value.get(0));
		}
		else if (o instanceof VecHandler)
		{
			VecHandler handler= (VecHandler) o;
			handler.handle(name, value);
		}
		else
		{
			//System.out.println("networkdoubleprovider: warning: couldn't find handler called <" + name + ">");
		}
	}

	public static float decode(byte[] from, int at)
	{
		return Float.intBitsToFloat(decodeInt(from, at));
	}

	public static int decodeInt(byte[] from, int at)
	{
		int i= (from[at] << 24) & (255 << 24);
		i += (from[at + 1] << 16) & (255 << 16);
		i += (from[at + 2] << 8) & (255 << 8);
		i += (from[at + 3]) & 255;
		return i;
	}

	public static String decodeString(byte[] from, int at)
	{
		char[] result;
		int string_length= from[at++];
		result= new char[string_length];

		for (int i= 0; i < result.length; i++)
		{
			result[i]= (char) ((from[at++] << 8) & (255 << 8));
			result[i] += (from[at++]) & 255;
		}

		return new String(result);
	}
}