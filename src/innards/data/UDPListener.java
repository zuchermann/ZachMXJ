package innards.data;

import java.net.*;
import innards.*;
import java.util.*;

public class UDPListener extends NamedObject implements Runnable
{

	Vector handlers= new Vector();
	public interface Handler
	{
		public void handle(byte[] contents, int length, InetAddress from);
	}

	int port;
	DatagramSocket socket;
	DatagramPacket packet;
	byte[] buffer;
	int maxPacketLength;

	public UDPListener(int port, int maxPacketLength)
	{
		super("unnamed");
		try
		{
			this.port= port;
			this.buffer= new byte[maxPacketLength];
			this.packet= new DatagramPacket(buffer, maxPacketLength);
			this.socket= new DatagramSocket(port);
			this.maxPacketLength= maxPacketLength;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		(new Thread(this)).start();
	}

	public void run()
	{
		while (true)
		{
			try
			{
				packet.setLength(maxPacketLength);
				socket.receive(packet);
				for (int i= 0; i < handlers.size(); i++)
				{
					((Handler) handlers.get(i)).handle(packet.getData(), packet.getLength(), packet.getAddress());
				}
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
	}

	public void addHandler(Handler handle)
	{
		handlers.add(handle);
	}

	static public class PrintHandler implements Handler
	{
		public void handle(byte[] contents, int length, InetAddress from)
		{
			System.out.println(" from: " + from + " " + from.getHostName());
			System.out.println(" bytes: " + length);
			int rows= length / 30 + 1;
			for (int r= 0; r < rows; r++)
			{
				for (int c= 0; c < 30; c++)
				{
					int i= r * 30 + c;
					if (i < length)
					{
						int z= contents[i];
						if (z < 0)
							z= 256 + z;
						String a= z + "";
						if (z < 10)
							a += " ";
						if (z < 100)
							a += " ";
						System.out.print(a + " ");
					}
				}
				System.out.println();
			}
			System.out.println();
		}
	}
	static public void main(String[] command)
	{
		int port= 1000;
		if (command.length > 0)
		{
			port= Integer.parseInt(command[0]);
		}
		System.out.println(" opening listener on port <" + port + ">");
		UDPListener l= new UDPListener(port, 5000);
		l.addHandler(new PrintHandler());
	}
}