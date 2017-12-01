package innards.data;

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.util.Enumeration;

import innards.*;

/**
 * //basic nio udp receiver. subclass and implement "processBuffer" to do something with the buffers. //
 * <p>
 * //some danger: packets received immediately after creation will show up in first update, // potentially a lot later. //
 * <p>
 * //this class should do threadsafe caching of recieved packets for you. //as many as numBuffers packets can be received between updates without dropping one. //then processBuffer is called on each of the received packets in order during the update. //
 * <p>
 * //processBuffer will be called at most numBuffer times per update.
 * <p>
 * -jg
 */

public class UDPNIOListener implements iUpdateable, Runnable {
	protected iSocketBufferProvider channel;
	protected ByteBuffer[] buffers;
	protected int numBuffers;
	protected boolean keepReceiving = true;
	protected boolean flushData = false;
	protected Object lock = new Object();
	protected InetSocketAddress address;
	protected SocketAddress[] froms;

	protected static class SynchronizedData {
		public int receivingInto;
		public int lastProcessed;
	}

	protected static class PauseSyncMonitor {
		public boolean pause = false;
	}
	
	protected SynchronizedData syncData = new SynchronizedData();
	protected PauseSyncMonitor pauseSyncMonitor = new PauseSyncMonitor();

	/**
	//numBuffers must be 3 or greater.
	*/
	public UDPNIOListener(int port, int maxPacketSize, int numBuffers){
		this.channel = makeChannel(port, false);
		setupBuffersAndGo(maxPacketSize, numBuffers, true, false);
	}

	public UDPNIOListener(int port, int maxPacketSize, int numBuffers, boolean setReuse){
		this.channel = makeChannel(port, setReuse);
		setupBuffersAndGo(maxPacketSize, numBuffers, true, false);
	}
	
	public UDPNIOListener(DatagramChannel channel, int maxPacketSize, int numBuffers){
		this.channel = new DatagramBufferProvider(channel);
		setupBuffersAndGo(maxPacketSize, numBuffers, true, false);
	}

	/**
	 * //useNativeBuffers only if you'll be using this listener for a while. //set flushdata to allow new data to overwrite old; //this improves latency for streams that are so fast we can't read every packet.
	 */

	public UDPNIOListener(int port, int maxPacketSize, int numBuffers, boolean useNativeBuffers, boolean flushData){
		this.channel = makeChannel(port, false);
		setupBuffersAndGo(maxPacketSize, numBuffers, useNativeBuffers, flushData);
	}

	public UDPNIOListener(DatagramChannel channel, int maxPacketSize, int numBuffers, boolean useNativeBuffers, boolean flushData) {
		this.channel = new DatagramBufferProvider(channel);
		setupBuffersAndGo(maxPacketSize, numBuffers, useNativeBuffers, flushData);
	}

	/**
	 *
	 * @param port
	 * @param multicastGroupAddress
	 * @param localAddress can be null to autochoose
	 * @param maxPacketSize
	 * @param numBuffers
	 * @param useNativeBuffers
	 * @param flushData
	 */
	public UDPNIOListener(int port, String multicastGroupAddress, String localAddress, int maxPacketSize, int numBuffers, boolean useNativeBuffers, boolean flushData){
		this.channel = makeMulticastChannel(port, multicastGroupAddress, localAddress);
		setupBuffersAndGo(maxPacketSize, numBuffers, useNativeBuffers, flushData);
	}

	public UDPNIOListener(MulticastSocket socket, int maxPacketSize, int numBuffers, boolean useNativeBuffers, boolean flushData) {
		this.channel = new MulticastBufferProvider(socket);
		setupBuffersAndGo(maxPacketSize, numBuffers, useNativeBuffers, flushData);
	}

	protected static boolean reuseTestPerformed = false;
	protected static boolean reuseTestSucceeded;

	/**
	 * related to fix for nio-nonreuse bug - allows the forcing of nio for reusable ports.
	 * maybe you want to do this if you know you're not running another c5 on the same computer, and want the extra speed of nio.
	 * @param forceNIO
	 */
	public static synchronized void setUseNIORegardlessOfReuseError(boolean forceNIO){
		if(forceNIO){
			reuseTestPerformed = true;
			reuseTestSucceeded = true;
		}else{
			reuseTestPerformed = false;
		}
	}

	/**
	 * Hack to determine if computer fails to re-use nio adresses.
	 *   On some computers, setReuseAdress doesn't do anything to sockets obtained from "datagramChannel.socket()".
	 *   We wish to detect this condition so we can substitute old style DatagramSockets
	 *
	 * @return false if computer fails to properly use the setReuseAddress functionality with nio calls.
	 */
	protected synchronized boolean computerSupportsChannelReuse(){
		if(!reuseTestPerformed){
			try {
				System.out.println("Testing setAddressReuse with nio - hack to revert to classic networking if we need setAddressReuse on effected computers");
				DatagramChannel dc1 = DatagramChannel.open();
				DatagramChannel dc2 = DatagramChannel.open();
				DatagramSocket ds1 = dc1.socket();
				DatagramSocket ds2 = dc2.socket();
				ds1.setReuseAddress(true);
				ds2.setReuseAddress(true);
				ds1.bind(null);
				ds2.bind(ds1.getLocalSocketAddress());
				//if we're still here we succeeded.
				System.out.println("Computer Passed nio setAddressReuse test.");
				reuseTestSucceeded = true;
				ds1.close();
				ds2.close();
			} catch (Exception ex) {
				System.out.println("Computer Failed nio setAddressReuse test:"+ex);
				reuseTestSucceeded = false;
			}
			reuseTestPerformed = true;
		}
		return reuseTestSucceeded;
	}

	protected iSocketBufferProvider makeChannel(int port, boolean setReuse){
		DatagramChannel channel = null;
		DatagramSocket socket = null;

		if(!setReuse || computerSupportsChannelReuse()){
			//TODO fix this hack when the java setReuseAddress bug is fixed.
			//this block is the right way to do it, get rid of above if and just have this block.
			//
			// what's up? there is a bug with non-reusable addresses on some computers
			// only if the socket comes from channel.socket()
			//thus we provide this switch to allow for reverting to old, non nio sockets
			//for those computers.
			try {
				channel = DatagramChannel.open();
			} catch (Exception ex) {
			System.out.println("Error creating channel for UDPNIOListener");
				ex.printStackTrace();
			}
			try {
				socket = channel.socket();
				//socket.setReuseAddress(true); //this should probably be an option.  oh it is now.
			} catch (Exception ex) {
				System.out.println("error getting socket for channel.");
				ex.printStackTrace();
			}
		}else{
			System.out.println("Making a classic style socket on port "+port+" since computer failed setReuseAddress test on nio sockets");
			try{
				socket = new DatagramSocket(null);
			} catch(SocketException e){
				System.out.println("Error making classic style socket");
				e.printStackTrace();
			}
		}
		if(setReuse){
			try{
				socket.setReuseAddress(true);
			}catch(Exception ex){
				System.out.println("error setting propery \"reuseaddress\" for socket.");
				ex.printStackTrace();
			}
		}
		try{
			address = new InetSocketAddress(port);
		} catch (Exception ex) {
			System.out.println("Error creating socketAddress for port:" + port);
			ex.printStackTrace();
		}
		try {
			if(channel!=null){
				channel.configureBlocking(true);
			}
		} catch (Exception ex) {
			System.out.println("error setting channel to blocking mode.");
			ex.printStackTrace();
		}
		try {
			socket.bind(address);
			//channel.connect(address);
		} catch (Exception ex) {
			System.out.println("unable to connect channel in UDPNIOListener to address:" + address);
			if(!setReuse){
				System.out.println("setReuse arg not set to true - consider using setReuse on both sockets that need to share a port");
			}else{
				System.out.println("You may have disabled compatibility checking for nio/setReuse using key IRCP.useNioEvenIfReuseFails (this process or others on this computer), which can lead to this bind error");
			}
			ex.printStackTrace();
		}

		try {
			System.out.println("Socket receive buffer size:" + socket.getReceiveBufferSize());
		} catch (Exception ex) {
			System.out.println("error trying to print out the receiveBufferSize of the socket.");
			ex.printStackTrace();
		}
		if(channel!=null){
			return new DatagramBufferProvider(channel);
		}else{
			return new ClassicDatagramBufferProvider(socket);
		}
	}

	/**
	 * @param port
	 * @param multicastGroupAddress
	 * @param localAddress the local address to bind to, null to auto-choose.
	 * @return
	 */
	protected iSocketBufferProvider makeMulticastChannel(int port, String multicastGroupAddress, String localAddress)
	{
		MulticastSocket socket = null;
		try
		{
			if(localAddress == null){
				localAddress = null;
				Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
				while(interfaces.hasMoreElements()){
					NetworkInterface ni = (NetworkInterface)interfaces.nextElement();
					Enumeration interfaceAddresses = ni.getInetAddresses();
					if (interfaceAddresses.hasMoreElements())
					{
						localAddress = ((InetAddress)interfaceAddresses.nextElement()).getHostName();
						System.out.println("Multicast Channel going to bind to address "+localAddress+" on interface "+ni.getDisplayName());
						break;
					}else{
						System.out.println("Interface "+ni.getDisplayName()+" has no addresses??  will try more if we have them");
					}
				}
				if(localAddress == null){
					System.out.println("Machine has no network interfaces with addresses.  Where is loopback??");
				}
			}
			socket = new MulticastSocket(port);
			socket.setInterface(InetAddress.getByName(localAddress));
			socket.setReceiveBufferSize(230000);
		} 
		catch (Exception ex)
		{
			System.out.println("error creating multicast socket on port: " + port);
			ex.printStackTrace();
		}
		
		try
		{
			System.out.println("Multicast socket receive buffer size:" + socket.getReceiveBufferSize());
		}
		catch (Exception ex)
		{
			System.out.println("error trying to print out the receiveBufferSize of the multicast socket.");
			ex.printStackTrace();
		}
		
		try
		{
			InetAddress groupAddress = InetAddress.getByName(multicastGroupAddress);
			socket.joinGroup(groupAddress);
		}
		catch (Exception ex)
		{
			System.out.println("error joining multicast group: " + multicastGroupAddress);
			ex.printStackTrace();
		}
		
		return new MulticastBufferProvider(socket);
	}

	protected void setupBuffersAndGo(int maxPacketSize, int numBuffers, boolean useNativeBuffers, boolean flushData) {
		this.flushData = flushData;
		this.numBuffers = numBuffers;

		if (numBuffers <= 3) {
			throw new IllegalArgumentException("numBuffers must be 3 or greater.  got " + numBuffers);
		}
		buffers = new ByteBuffer[numBuffers];
		froms = new SocketAddress[numBuffers];

		for (int i = 0; i < numBuffers; i++) {
			if (useNativeBuffers) {
				buffers[i] = ByteBuffer.allocateDirect(maxPacketSize);
			} else {
				buffers[i] = ByteBuffer.allocate(maxPacketSize);
			}
		}
		syncData.receivingInto = 0;
		syncData.lastProcessed = numBuffers - 1; //last one "before" 0, using mod arithmatic.

		 (new Thread(this)).start();
	}

	public void setLittleEndian(){
		for(int i = 0; i < buffers.length; i++){
			buffers[i].order(ByteOrder.LITTLE_ENDIAN);
		}
	}

	public void run() {
		DatagramSocket sock = channel.getSocket();
		//if (sock instanceof MulticastSocket)//maybe we should just do this always. -jg.
		//{
			Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
		//}
		while (!sock.isClosed()) {
			while (pauseSyncMonitor.pause) {
				synchronized (pauseSyncMonitor) {
					if (pauseSyncMonitor.pause) {
						try {
							pauseSyncMonitor.wait();
						} catch (Exception ex) {
							System.out.println("wait interrupted, huh?");
							ex.printStackTrace();
						}
					}
				}
			}
			try {
				buffers[syncData.receivingInto].clear();
				froms[syncData.receivingInto] = channel.receiveBuffer(buffers[syncData.receivingInto]);
				if(!useThreadsafeUpdate){
					buffers[syncData.receivingInto].flip();
					buffercurrentlybeingprocessedbysubclass = syncData.receivingInto;
					processBuffer(buffers[syncData.receivingInto]);
					buffercurrentlybeingprocessedbysubclass = -1;
					buffers[syncData.receivingInto].clear();
					continue;
				}
			} catch (java.nio.channels.AsynchronousCloseException asex) {
				System.out.println("UDPNIOListener socket threw an 'AsynchronousCloseException' exception; probably someone closed it.");
				//sockEX.printStackTrace();
			} catch (Exception ex) {
				System.out.println("error receiving packet in UDPNIOListener receive thread");
				ex.printStackTrace();
				continue;
			}
			while (syncData.receivingInto == syncData.lastProcessed) {
				synchronized (syncData) {
					if (syncData.receivingInto == syncData.lastProcessed) {
						try {
							syncData.wait();
						} catch (Exception ex) {
							System.out.println("wait interrupted, huh?");
							ex.printStackTrace();
						}
					}
				}
			}
			synchronized (syncData) {
				if (!flushData || (syncData.receivingInto + 1) % numBuffers != syncData.lastProcessed) {
					//only do this if we're not flushing data, so its ok to stall,
					//OR if we are flushing data and doing this won't make us stall.
					syncData.receivingInto = (syncData.receivingInto + 1) % numBuffers;
				}
			}
		}
		System.out.println("UDPNIOListener receive thread exited.");
	}

	private boolean useThreadsafeUpdate = true;
	//this is not thoroughly tested.
	public void setUseThreadsafeUpdate(boolean safe){
		this.useThreadsafeUpdate = safe;
	}
	/**
	 * //probably don't override this, it handles the buffer. check out "processBuffer".
	 */
	protected int buffercurrentlybeingprocessedbysubclass = -1;
	public void update() {
		if(!useThreadsafeUpdate)return;
		int ri = syncData.receivingInto;

		//I could see an argument for updating lastProcessed and calling notify for each
		//iteration here to get though a huge backlog of packets in the native buffer
		//of the socket... but i dunno, this method could execute for an arbitrary length of time
		//in that case, if the packets were coming fast enough...
		for (int i = (syncData.lastProcessed + 1) % numBuffers; i != ri; i = (i + 1) % numBuffers) {
			buffers[i].flip();
			try {
				buffercurrentlybeingprocessedbysubclass = i;
				processBuffer(buffers[i]);
				buffercurrentlybeingprocessedbysubclass = -1;
			}catch (Exception ex) {
				System.out.println("Exception processing buffer, skipping.");
				ex.printStackTrace();
			}
			buffers[i].clear();
		}
		synchronized (syncData) {
			syncData.lastProcessed = (ri + numBuffers - 1) % numBuffers;
			syncData.notify();
		}
	}

	public SocketAddress whereDidThisBufferComeFrom() {
		if(buffercurrentlybeingprocessedbysubclass == -1){
			System.out.println("error, you cannot call \"whereDidThisBufferComeFrom\" unless you are calling it from PacketHandler.handle(..)");
		}		
		return froms[buffercurrentlybeingprocessedbysubclass];
	}

	/**
	 * //override this method to actually do something.
	 * <p>// bb is only valid for the duration of this method; it is cleared immediately afterwards
	 * <p>
	 * //this method is called from within the main update loop, so go nuts; no need to worry about //missing packets by doing something slow here, or thread safety with main update loop objects.
	 */
	protected void processBuffer(ByteBuffer bb) {
	}

	/**
	 * //stops the receive thread(even if its blocked in "receive") //by calling socket.close().
	 */
	public void stopReceiveThread() {
		channel.getSocket().close();
	}
	
	public void pauseReceiveThread() {
		pauseSyncMonitor.pause = true;
	}
	
	public void unpauseReceiveThread() {
		synchronized (pauseSyncMonitor) {
			pauseSyncMonitor.pause = false;
			pauseSyncMonitor.notify();
		}
	}
}
