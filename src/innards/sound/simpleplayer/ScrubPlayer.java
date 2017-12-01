package innards.sound.simpleplayer;



import java.nio.FloatBuffer;

import innards.sound.util.FileUtils;

/**
 * Simple sound player, a bit more controllable than NSSound.
 * You can start it at any point play for a certain duration, and stop it whenever.
 * It can also tell you current sample index or ms.
 * 
 * User: jg
 * Date: Aug 25, 2004
 * Time: 2:44:49 PM
 */
public class ScrubPlayer { //implements AudioDeviceIOProc{
	float[] audioData;

	//CAMemoryObject needs the data split into tasty bitesize morsels.
	float[][] morselsOfFloats;

	public ScrubPlayer(String fileName){
//		try{
//			device = getAudioDevice();
//			AudioStreamDescription format = new AudioStreamDescription(true);
//			device.getOutputProperty(0, AHConstants.kAudioDevicePropertyStreamFormat, format);
//			System.out.println("Gonna load audio file:"+fileName);
//			audioData = FileUtils.fileToFloatArray(fileName);
//			if(FileUtils.fileIsMono(fileName)){
//				System.out.println("Converting Mono to Stereo");
//				float[] f2 = new float[audioData.length * 2];
//				for(int i = 0; i < f2.length; i++){
//					f2[i] = audioData[i/2];
//				}
//				audioData = f2;
//			}
//			int numMorsels = audioData.length/500;
//			if(audioData.length % 500 != 0){
//				numMorsels++;
//			}
//			morselsOfFloats = new float[numMorsels][];
//			int sourceIndex = 0;
//			int morselsNum = 0;
//			while(sourceIndex < audioData.length){
//				int toCopy = 500;
//				if(audioData.length - sourceIndex < 500){
//					toCopy = audioData.length - sourceIndex;
//				}
//				morselsOfFloats[morselsNum] = new float[toCopy];
//				System.arraycopy(audioData, sourceIndex, morselsOfFloats[morselsNum], 0, toCopy);
//				sourceIndex += toCopy;
//				morselsNum++;
//			}
//		} catch(CAException e){
//			e.printStackTrace();
//		}

	}

	int currentIndex;
	int endIndex;
	long pipeDelay;
	long diffTime;
	long lastTime;
	/**
	 *	called by coreaudio!!
	 */
//	public int execute(AudioDevice dev, AudioTimeStamp time, AudioBufferList in, AudioTimeStamp inAt, AudioBufferList out, AudioTimeStamp outAt){
//		long timeStampWritten = outAt.getHostTime();
//		long currentTime = time.getHostTime();
//		pipeDelay = timeStampWritten - currentTime;
//		diffTime = currentTime - lastTime;
//		lastTime = currentTime;
//		if(currentIndex < endIndex){
//			CAMemoryObject data = out.getBuffer(0).getData();
//			int capacity = data.getSize() / 4;
//			int memIndex = 0;
//			while(capacity>0 && currentIndex < endIndex){
//				int morselNum = currentIndex / 500;
//				int morselOffset = currentIndex % 500;
//				int leftInMorsel = morselsOfFloats[morselNum].length - morselOffset;
//				int toCopy = capacity<leftInMorsel?capacity:leftInMorsel;
//				//System.out.println("Copying from morsel:"+morselNum+" at offset:"+morselOffset+" length:"+toCopy);
//				data.copyFromArray(memIndex, morselsOfFloats[morselNum], morselOffset, toCopy);
//				currentIndex+=toCopy;
//				capacity -= toCopy;
//				memIndex += (toCopy*4);
//			}
//		}
//		//maybe should copy 0's in for the rest?  no for now.
//
//		return 1;
//	}

//	protected AudioDevice getAudioDevice() throws CAException{
//		//String contains = (InnardsDefaults.getProperty("ScrubPlayer.contains", null));
//		//if (contains== null) return AudioHardware.getDefaultOutputDevice();
//		return AudioHardware.getDefaultOutputDevice();
//		//AudioDevice[] l= AudioHardware.getAudioDevices();
//		//for(int i=0;i<l.length;i++)
//		//{
//		//	System.out.println(" BufferedOutput, comparing <"+l[i].getName()+"> with <"+contains+">");
//		//	if (l[i].getName().indexOf(contains)!=-1) return l[i];
//		//}
//		//System.out.println(" no match");
//		//return null;
//
//	}


	public int getCurrentSample(){
		return currentIndex;
	}

	public int getCurrentMS(){
		return (currentIndex / 2) * 1000 / 44100;
	}

	// added by Zoz so that external classes (e.g. visualizers) don't have to read the sound file twice
	public float[] getAudioData()
	{
		return audioData;
	}

	public void playFromTime(int ms){
		//assume 44.1khz.
		int samplesOffset = ms * 44100 / 1000;
		int forStereo = samplesOffset * 2;
		currentIndex = forStereo;
		endIndex = audioData.length;
		System.out.println("Pipe Delay1:"+pipeDelay+" diffTime:"+diffTime);
	}

	public void playFromTimeWithDuration(int startMS, int durationMS){
		int samplesOffset = startMS * 44100 / 1000;
		int forStereo = samplesOffset * 2;
		currentIndex = forStereo;
		int endSamplesOffset = (startMS+durationMS) * 44100 / 1000;
		int endForStereo = endSamplesOffset * 2;
		endIndex = endForStereo;
		System.out.println("Pipe Delay2:"+pipeDelay+" diffTime:"+diffTime);
	}

	public void stop(){
		currentIndex = endIndex = audioData.length;
	}

	public boolean isPlaying(){
		return currentIndex < endIndex;
	}

	protected boolean enabled;
//	AudioDevice device;
//
//	protected ADevicePropertyListener overloadListener = new ADevicePropertyListener(){
//		public int execute(AudioDevice audioDevice, int channel, boolean isInput, int inPropertyID){
//			System.out.println("Scrub Player overloaded!!");
//			return 1;
//		}
//	};
//	public void enable(boolean enable){
//		if(enable && !enabled){
//			try{
//				System.out.println(" opening device");
//
//				// add the IOProc so we can supply data to the device
//				System.out.println(" adding ioproc to <"+device+">");
//				device.addIOProc(this);
//				System.out.println(" starting device");
//				device.start(this);
//
//				Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
//				{
//					public void run()
//					{
//						System.out.println(" buffered output shutdown hock");
//						device.removeAllIOProcs();
//						device.removeAllListeners();
//						try
//						{
//							device.stop();
//						} catch (CAException e)
//						{
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//					}
//				}));
//				device.addOutputPropertyListener(0, AHConstants.kAudioDeviceProcessorOverload, overloadListener);
//			} catch (CAException e)
//			{
//				e.printStackTrace();
//			}
//			enabled = true;
//		}else if(!enable && enabled){
//			try{
//				device.removeIOProc(this);
//				device.stop();
//				device.removeOutputPropertyListener(0, AHConstants.kAudioDeviceProcessorOverload, overloadListener);
//			} catch(CAException e){
//				e.printStackTrace();
//			}
//			enabled = false;
//		}
//	}
}
