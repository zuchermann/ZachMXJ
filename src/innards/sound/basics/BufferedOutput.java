package innards.sound.basics;

import innards.debug.ANSIColorUtils;
import innards.util.InnardsDefaults;

import java.io.*;
import java.nio.FloatBuffer;


/**
 * @author marc
 * Created on May 9, 2003
 */
public class BufferedOutput { //implements AudioDeviceIOProc, iClockSource, iBufferedOutput
	static public PrintStream out= System.out;
	static {
		try
		{
			out= new PrintStream(new FileOutputStream(InnardsDefaults.getProperty("BufferedOutput.out")));
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	FloatBuffer[] circular;

//	private AudioDevice device;

	private boolean firstTime= true;
	double firstTimeStampAt= 0;

	protected int internalBufferSize= -1;
	protected int numBuffers;
	int readHead= 0;

	long samplesWritten;

	boolean started= true;
	double timeStampWritten= -1;
	int writeHead= 0;

	TimedTaskQueue taskQueue;

	boolean underrun= false;

	public BufferedOutput(int numBuffers)
	{
		this.numBuffers= numBuffers;
		// internal and external times are the same, and are in the 'getSampleNumberAtAccess()' units
		this.taskQueue= new TimedTaskQueue(512);
	}

	/**
	 * @param i
	 */
	protected void buildBufferQueue(int internalSize)
	{
		this.internalBufferSize= internalSize;
		circular= new FloatBuffer[numBuffers];
		for (int i= 0; i < circular.length; i++)
		{
			circular[i]= FloatBuffer.allocate(internalSize);
		}
		readHead= 0;
		writeHead= circular.length / 2;
	}

	protected boolean cannotWrite(int n) throws InterruptedException
	{
		int r= readHead;
		int w= writeHead;
		if (r < w)
			r += circular.length;

		return (r - (w + n)) <= 0;
	}

	protected void copyBuffer(FloatBuffer from, FloatBuffer to)
	{
		from.rewind();
		to.rewind();
		to.put(from);
	}

	long timeIn;
	long timeOut;

	/*
	 * @see com.apple.audio.hardware.AudioDeviceIOProc#execute(com.apple.audio.hardware.AudioDevice, com.apple.audio.util.AudioTimeStamp, com.apple.audio.util.AudioBufferList, com.apple.audio.util.AudioTimeStamp, com.apple.audio.util.AudioBufferList, com.apple.audio.util.AudioTimeStamp)
	*/
//	public int execute(AudioDevice dev, AudioTimeStamp time, AudioBufferList in, AudioTimeStamp inAt, AudioBufferList out, AudioTimeStamp outAt)
//	{
//		CAMemoryObject data= out.getBuffer(0).getData();
//		if (firstTime)
//		{
//			buildBufferQueue(data.getSize() / 4);
//			firstTime= false;
//		}
//
//		synchronized (this)
//		{
//			// read from readHead
//			FloatBuffer b= circular[readHead];
//			//System.out.println(" read from <"+readHead+">");
//			int cap= b.capacity();
//			//for (int i= 0; i < cap; i++)
//			//	data.setFloatAt(i * 4, b.get(i));
//
//			data.copyFromArray(0, b.array(), 0, cap);
//			//update number of samples written to device;
//
//			samplesWritten += b.capacity();
//			if (timeStampWritten == -1)
//				firstTimeStampAt= timeStampWritten;
//			timeStampWritten= outAt.getSampleTime();
//
//			readHead= (readHead + 1) % circular.length;
//			notifyAll();
//			if (readHead == writeHead)
//				underrun= true;
//		}
//		return 1;
//	}

	synchronized public int getInternalBufferSize()
	{
//		if (!started)
//			start();
		try
		{
			while (internalBufferSize == -1)
				wait(2000);
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return internalBufferSize;
	}

	/*
	 * @see marc.sound.basics.iClockSource#getSampleNumberAtAccess()
	 */
	public long getSampleNumberAtAccess()
	{
		// samplesWritten up to now at readHead
		// samplesWritten was at timeStampWritten
		synchronized (this)
		{
			int w= writeHead;
			int r= readHead;
			if (w < r)
				w += circular.length;
			int d= (w - r) * internalBufferSize;
			return (long) (timeStampWritten - firstTimeStampAt + d);
		}
	}

	public long getSampleNumberAtNow()
	{
		return (long) (timeStampWritten - firstTimeStampAt);
	}
//
//	public iBufferedOutput start(){
//		return start(null);
//	}

//	synchronized public iBufferedOutput start(String name)
//	{
//		try
//		{
//			System.out.println(" opening device");
//			// lets see if we can compile and play a sine wave, all from java
//			device= getAudioDevice(name);
//			// add the IOProc so we can supply data to the device
//			System.out.println(" adding ioproc to <"+device+">");
//			device.addIOProc(this);
//			System.out.println(" starting device");
//			device.start(this);
//			
//			
//			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
//			{
//				public void run()
//				{
//					System.out.println(" buffered output shutdown hock");
//					device.removeAllIOProcs();
//					try
//					{
//						device.stop();
//					} catch (CAException e)
//					{
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}));
//			
//		} catch (CAException e)
//		{
//			e.printStackTrace();
//		}
//		started= true;
//		return this;
//	}
//	
//	protected AudioDevice getAudioDevice(String name) throws CAException
//	{
//		String contains;
//		if(name == null){
//			contains = (InnardsDefaults.getProperty("BufferedOutput.contains", null));
//		}else{
//			contains = name;
//		}
//		if (contains== null) return AudioHardware.getDefaultOutputDevice();
//		AudioDevice[] l= AudioHardware.getAudioDevices();
//		for(int i=0;i<l.length;i++)
//		{
//			System.out.println(" BufferedOutput, comparing <"+l[i].getName()+"> with <"+contains+">");
//			if (l[i].getName().indexOf(contains)!=-1) return l[i];
//		}
//		System.out.println(" no match");
//		return null;
//	}
	

	public boolean write(FloatBuffer buffer)
	{
		if (underrun)
		{
			out.println(ANSIColorUtils.red(" (( output underrun )) "));
			underrun= false;
		}
		//System.out.println(" samplesWritten <"+samplesWritten+"> <"+(timeStampWritten-firstTimeStampAt)+"> <"+(samplesWritten/(timeStampWritten-firstTimeStampAt))+">");
		//		System.out.println("getSampleNumberAtAccess <"+getSampleNumberAtAccess()+">");

		if (circular == null)
			return false;
		// this will write 
		synchronized (this)
		{
			try
			{
				boolean blocking= false;
				while (cannotWrite(1))
				{
					blocking= true;
					wait();
				}
				//				if (!blocking) System.out.println(ANSIColorUtils.blue(" non - blocking write..."));

				copyBuffer(buffer, circular[writeHead]);
				//System.out.println(" write to <"+writeHead+">");
				writeHead= (writeHead + 1) % circular.length;
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		//out.println("output <"+writeHead+"> <"+readHead+"> <" + (samplesWritten - getSampleNumberAtNow() * 2) + "> <" + (timeOut - timeIn) + ">");

		//System.out.println(" legroom <"+(writeHead-readHead+circular.length)%circular.length+">");

		taskQueue.update(this.getSampleNumberAtAccess());

		return true;
	}

	public TimedTaskQueue getTaskQueue()
	{
		return taskQueue;
	}

}
