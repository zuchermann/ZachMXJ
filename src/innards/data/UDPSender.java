package innards.data;

import java.net.*;
import innards.*;
import java.util.*;

public class UDPSender extends NamedObject
{
	static public boolean EXIT_ON_EXCEPTION= true;

	int port;
	DatagramSocket socket;
	DatagramPacket packet;
	byte[] buffer;
	InetAddress toa;

	public UDPSender(int port, String to, int maxPacketLength)
	{
		super("unnamed");
		try
		{
			System.out.println(" opening port " + port + " to <" + to + ">");
			this.port= port;
			this.buffer= new byte[maxPacketLength];
			toa= InetAddress.getByName(to);
			this.packet= new DatagramPacket(buffer, maxPacketLength, toa, port);
			//sock = new DatagramSocket(port);        
			//dp = new DatagramPacket(message,message.length,add,port);
			this.socket= new DatagramSocket(port);
			//this.socket.setSendBufferSize(maxPacketLength);               
		} catch (Exception ex)
		{
			System.out.println(" port:" + port + " to:" + to);
			ex.printStackTrace();
			if (EXIT_ON_EXCEPTION)
			{
				System.err.println("}}} exception thrown in udp sender opening port <" + port + "> to <" + to + ">. dead");
				System.exit(0);
			}
		}
	}

	boolean aggressiveSend= false;

	public void setAggressiveSend()
	{
		aggressiveSend= true;
	}

	public void send(byte[] a, int length)
	{
		try
		{
			synchronized (packet)
			{
				//packet.setAddress(toa);
				packet.setLength(length);
				//System.out.println(" sending :"+length);
				System.arraycopy(a, 0, buffer, 0, length);
				if (aggressiveSend)
					socket.setSendBufferSize(length);
				socket.send(packet);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	public void send(byte[] a)
	{
		send(a, a.length);
	}

	public void setOutputBufferSize(int size)
	{
		try
		{
			this.socket.setSendBufferSize(size);
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}

	static public void main(String[] command)
	{
		int port= 1000;
		String s= null;
		if (command.length > 0)
		{
			port= Integer.parseInt(command[0]);
			s= command[1];
		}
		System.out.println(" opening sender on port <" + port + "> to <" + s + ">");
		UDPSender send= new UDPSender(port, s, 50);
		byte[] b= new byte[10];
		try
		{
			int c= 0;
			while (true)
			{
				System.out.println(" sending packet ");
				for (int i= 0; i < b.length; i++)
				{
					b[i]= (byte) ((i + c) % b.length);
				}
				c++;
				send.send(b);
				Thread.sleep(1000);
			}
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}