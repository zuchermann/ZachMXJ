package innards.sound.basics;

import innards.debug.ANSIColorUtils;

import java.nio.FloatBuffer;


/**
 * @author marc Created on May 9, 2003
 */
public class BufferedInput { //implements AudioDeviceIOProc {

//	private AudioDevice device;

	protected int internalBufferSize = -1;
	protected int numBuffers;

	public BufferedInput(int numBuffers) {
		this.numBuffers = numBuffers;
	}

//	public static void printDeviceNames(){
//		try{
//			System.out.println("Audio Device List:");
//			AudioDevice[] devices = AudioHardware.getAudioDevices();
//			if(devices!=null){
//				for(int i = 0; i < devices.length; i++){
//					String deviceName = devices[i].getName();
//					System.out.println("   Device["+i+"]=\""+deviceName+"\"");
//				}
//			}else{
//				System.out.println("   Devices list is null!");
//			}
//			if(devices.length == 0){
//				System.out.println("   Devices list has 0 elements");
//			}
//
//		}catch(CAException e){
//			System.out.println("Error printing audio device names:");
//			e.printStackTrace();
//		}
//	}

//	public BufferedInput start(){
//		return this.start(null);
//	}

//	public BufferedInput start(String deviceName) {
//		try {
//			System.out.println(" opening device");
//			if(deviceName == null){
//				device = AudioHardware.getDefaultInputDevice();
//			}else{
//				AudioDevice[] devices = AudioHardware.getAudioDevices();
//				for(int i = 0; i < devices.length; i++){
//					if(devices[i].getName().equals(deviceName)){
//						device = devices[i];
//						break;
//					}
//				}
//				if(device == null){
//					throw new IllegalArgumentException("No Input Device \""+deviceName+"\"");
//				}
//			}
//			System.out.println(" adding ioproc <" + device + ">");
//			device.addIOProc(this);
//			System.out.println(" starting device");
//			device.start(this);
//
//			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//				public void run() {
//					System.out.println(" buffered input shutdown hock");
//					device.removeAllIOProcs();
//					try {
//						device.stop();
//					} catch (CAException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}));
//		} catch (CAException e) {
//			e.printStackTrace();
//		}
//		return this;
//	}

	public int getInternalBufferSize() {
		synchronized (this) {
			while (internalBufferSize == -1) {
				try {
					wait();
				} catch (InterruptedException e) {
					// return Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return internalBufferSize;
	}

	private boolean firstTime = true;
	/*
	 * @see com.apple.audio.hardware.AudioDeviceIOProc#execute(com.apple.audio.hardware.AudioDevice, com.apple.audio.util.AudioTimeStamp, com.apple.audio.util.AudioBufferList, com.apple.audio.util.AudioTimeStamp, com.apple.audio.util.AudioBufferList,
	 * com.apple.audio.util.AudioTimeStamp)
	 */
//	public int execute(AudioDevice dev, AudioTimeStamp time, AudioBufferList in, AudioTimeStamp inAt, AudioBufferList out, AudioTimeStamp outAt) {
//		CAMemoryObject data = in.getBuffer(0).getData();
//		if (firstTime) {
//			buildBufferQueue(data.getSize() / 4);
//			firstTime = false;
//		}
//
//		synchronized (this) {
//			// read from readHead
//			FloatBuffer b = circular[writeHead];
//			int cap = b.capacity();
//			for (int i = 0; i < cap; i++) {
//				b.put(i, data.getFloatAt(i * 4));
//				//if (data.getFloatAt(i*4)!=0) System.out.println(data.getFloatAt(i*4));
//			}
//
//			//System.out.println(" read from <"+readHead+">");
//			writeHead = (writeHead + 1) % circular.length;
//			notifyAll();
//			if (readHead == writeHead)
//				BufferedOutput.out.println(ANSIColorUtils.red(" (( input buffer overrun )) "));
//
//		}
//
//		return 1;
//	}

	public boolean read(FloatBuffer buffer) {
		if (circular == null)
			return false;
		// this will write
		synchronized (this) {
			try {
				while (cannotRead(1)) {
					//System.out.println(" blocking write...");
					wait();
				}
				copyBuffer(circular[readHead], buffer);
				readHead = (readHead + 1) % circular.length;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		//BufferedOutput.out.println(" input <" + readHead + "> <" + writeHead + ">");
		return true;
	}

	protected boolean cannotRead(int n) throws InterruptedException {
		int w = writeHead;
		int r = readHead;
		if (w < r)
			w += circular.length;

		return (w - (r + n)) <= 0;
	}

	public boolean canRead() {
		return canRead(1);
	}
	public boolean canRead(int num) {
		try {
			return !cannotRead(num);
		} catch (InterruptedException e) {
			// return Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	protected void copyBuffer(FloatBuffer from, FloatBuffer to) {
		from.rewind();
		to.put(from);
	}

	FloatBuffer[] circular;
	int readHead = 0;
	int writeHead = 0;

	/**
	 * @param internalSize
	 */
	protected void buildBufferQueue(int internalSize) {
		this.internalBufferSize = internalSize;
		circular = new FloatBuffer[numBuffers];
		for (int i = 0; i < circular.length; i++) {
			circular[i] = FloatBuffer.allocate(internalSize);
		}
		readHead = 0;
		writeHead = circular.length / 2;
	}
}
