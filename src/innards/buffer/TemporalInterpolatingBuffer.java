package innards.buffer;


import java.util.LinkedList;
import java.util.Iterator;
import java.lang.System;
import innards.math.BaseMath;

/**
 * User: jg
 * Date: Jan 16, 2004
 * Time: 6:11:16 PM
 *
 * This class is designed help resample a data stream <br>
 * It is designed to take in data from one source when it is available and <br>
 * produce output interpolated data when it is required. <br> <br>
 *
 * Normal use of this class would be to give it data each update of your main loop, <br>
 * Then ask for the data from some faster updating loop (or, more likely, use the <br>
 * Threading provided to push the data somewhere at a faster speed.<br><br>
 *
 * You must override "interpolate" for your type of data;<br>
 * You must also override "outputData" if you are using the built-in threading to push data<br><br>
 *
 * This class has two modes:<br>
 * in "REAL_TIME_INPUT" mode, the data coming in through addData is assumed to be real-time;<br>
 * that is, if two frames are farther apart than usual, it is assumed to be intentional and treated as<br>
 * such in the interpolation<br>
 * in "VIRTUAL_TIME_INPUT mode, it is assumed that incoming data is "meant" to be evenly spaced,<br>
 * so an attempt will be made to smooth over any irregularities in timing between incoming frames.<br>
 * This problem is pretty ill defined, so the solution implemented here may or may not be helpful for you.
 *
 */
public abstract class TemporalInterpolatingBuffer implements Runnable{

	public static final int REAL_TIME_INPUT = 2;
	public static final int VIRTUAL_TIME_INPUT = 3;

	private static final boolean USE_NANOS = true;
	private static final long VTICKS_PER_SAMPLE = 1000000000; //1000 for millis, 1000000000 for nanos
	protected int mode;
	protected LinkedList dataList = new LinkedList();
	private long bufferLength;
	private long pushSleep;
	private boolean keepPushing;
	private float addInterval;

	protected static class Data{
		public long time;
		public Object data;
		public Data(Object data, long time){
			this.time = time;
			this.data = data;
		}
	}

	protected static class EnclosingSamples{
		public Data lowData, highData, outsideLow, outsideHigh;
		public EnclosingSamples(Data outsideLow, Data lowData, Data highData, Data outsideHigh){
			this.lowData = lowData;
			this.highData = highData;
			this.outsideHigh = outsideHigh;
			this.outsideLow = outsideLow;
		}

		public boolean enclosed(){
			return lowData!=null && highData!=null;
		}

		public boolean enclosedQuad(){
			return lowData!=null && highData!=null && outsideLow!=null && outsideHigh!=null;
		}

		public Data getSingleton(){
			if(lowData!=null)
				return lowData;
			if(highData!=null)
				return highData;
			return null;
		}
	}

	protected long getTime(){
		if(USE_NANOS){
			return System.nanoTime();
		}else{
			return System.currentTimeMillis();
		}
	}

	/**
	 *
	 * @param inputMode choose REAL_TIME_INPUT if input comes in real time, VIRTUAL_TIME_INPUT otherwise.
	 */
	public TemporalInterpolatingBuffer(int inputMode, int pushFPS){
		this.mode = inputMode;
		if(mode == REAL_TIME_INPUT){
			if(USE_NANOS)
				bufferLength = 1000000000*2;
			else
				bufferLength = 1000*2;
		}else if(mode == VIRTUAL_TIME_INPUT){
			bufferLength = (long)VTICKS_PER_SAMPLE*(long)20;
		}
		//startTime = getTime();
		if(pushFPS!=0){
			setOutputFPS(pushFPS);
			keepPushing = true;
			Thread pushThread = new Thread(this);
			pushThread.start();
			pushThread.setPriority(Thread.MAX_PRIORITY);
		}
	}

	public abstract Object interpolate(Object preceding, Object left, Object right, Object subsequent, float alphaTweenLeftAndRight);


	private float lastAlpha;
	protected boolean countQuadErrors = true;
	public synchronized Object getDataAtTime(long time){
		EnclosingSamples p = getEnclosingPair(time);
		if(p.enclosed()){
			long t1 = p.lowData.time;
			long t2 = p.highData.time;
			float pos = (float)(time - t1) / (float)(t2 - t1);
			lastAlpha = pos;
			if(countQuadErrors){
				if(!p.enclosedQuad()){
					rails++;
					//System.out.println("           !!!TEMPORAL INTERPOLATING BUFFER TOO TO EDGE FOR QUAD!!!");
				}
			}
			return interpolate(p.outsideLow!=null?p.outsideLow.data:null, p.lowData.data, p.highData.data, p.outsideHigh!=null?p.outsideHigh.data:null, pos);
		}else{
			rails++;
			//System.out.println("           !!!TEMPORAL INTERPOLATING BUFFER IS RAILED!!!");
			lastAlpha = 0;
			Data d = p.getSingleton();
			if(d!=null){
				return d.data;
			}else{
				return null;
			}
		}
	}

	private int lastLowerIndex;
	protected EnclosingSamples getEnclosingPair(long time){
		Iterator ii = dataList.iterator();
		Data preceding = null;
		Data precedesPreceding = null;
		int i = 0;
		int lli = 0;
		while(ii.hasNext()){
			Data d = (Data)ii.next();
			if(d.time > time){
				lastLowerIndex = lli;
				return new EnclosingSamples(precedesPreceding, preceding, d, ii.hasNext()?(Data)ii.next():null);
			}
			precedesPreceding = preceding;
			preceding = d;
			lli = i;
			i++;
		}
		lastLowerIndex = i;
		return new EnclosingSamples(precedesPreceding, preceding, null, null);
	}

	private long lastAdd = 0;
	private float fpsFilter = 0.99f;
	private long vIndex = 0;
	private float actualDelayFilter = 0.95f;
	//private long startTime = 0;
	public synchronized void addData(Object data){
		long curTime = getTime();
		long timeCutoff = 0;
		Data newData = null;
		//if(data!=null)StaticGraph.recordStatic("ActualPos", (float)((double)ReceiveMotorPackets.cTime(curTime)/1000000.0), ((float[])data)[27]);
		if(mode == REAL_TIME_INPUT){
			newData = new Data(data, curTime);
			timeCutoff = curTime - bufferLength;
			if(lastTimePushed!=0){
				actualDelayMS = (1-actualDelayFilter)*(curTime - lastTimePushed)/(USE_NANOS?1000000:1)+actualDelayFilter*actualDelayMS;
				actualDelayFrames = dataList.size() - 1 - (lastLowerIndex + lastAlpha);
			}
		}else if(mode == VIRTUAL_TIME_INPUT){
			newData = new Data(data, vIndex+=VTICKS_PER_SAMPLE);
			timeCutoff = vIndex - bufferLength;
			if(lastTimePushed != 0){
				actualDelayFrames = (float)((1-actualDelayFilter)*(vIndex - lastTimePushed)/(double)VTICKS_PER_SAMPLE+actualDelayFilter*(actualDelayFrames));
				actualDelayMS = actualDelayFrames * addInterval / (USE_NANOS?1000000:1);

				//System.out.println("ActualDelayMS="+actualDelayMS+" from actualDelayFrames:"+actualDelayFrames+" * addInterval:"+addInterval);
				//actualDelayFrames = actualDelayVTicks/1000f;
			}
		}
		dataList.addLast(newData);
		Iterator ii = dataList.iterator();
		while(ii.hasNext()){
			Data d = (Data)ii.next();
			if(d.time < timeCutoff){
				ii.remove();
			}else{
				break;
			}
		}
		if(lastAdd!=0){
			long lastFrame = curTime - lastAdd;
			//these maxmins are maintained for debugging.
			if(lastFrame > maxAddIntervalGARSD){
				maxAddIntervalGARSD = lastFrame;
			}
			if(lastFrame < minAddIntervalGARSD){
				minAddIntervalGARSD = lastFrame;
			}
			if(addInterval==0){
				//start it off
				addInterval = lastFrame;
			}else{
				addInterval = addInterval*fpsFilter+lastFrame*(1f-fpsFilter);
				if(mode == VIRTUAL_TIME_INPUT && actualPushInterval>BaseMath.epsilon){
					updatePushFilter();
				}
				//StaticGraph.recordStatic("filteredAddInterval", curTime - startTime, addInterval);
			}
			//StaticGraph.recordStatic("addInterval (ms)", curTime - startTime, lastFrame);
			//ReceiveMotorPackets.pulse(curTime);
		}
		lastAdd = curTime;
	}

	public synchronized void reset(){
		dataList.clear();
		lastTimePushed = 0;
		lastAdd = 0;
		vIndex = 0;
	}

	//below here is stuff for threaded pushing.

	/**
	 * This gets called by a seperate thread at user selected fps to output interpolated data.<br>
	 * Override it to do something.
	 * @param data the interpolated data to output
	 */
	public boolean outputData(Object data){
		//do nothing; override this to push.
		return true;
	}


	public void stopPushingThread(){
		keepPushing = false;
	}

	public void setOutputFPS(int pushFPS){
		this.pushSleep = (long)((1f/pushFPS)*1000);
	}

	public void setDelay(int delayMS){
		if(USE_NANOS){
			this.delay = delayMS*1000000;
		}else{
			this.delay = delayMS;
		}
	}

	public float getOutputDesiredFPS(){
		return 1f/pushSleep * 1000;
	}

	public float getOutputRealFPS(){
		if(actualPushInterval == 0)
			return 0;
		else
			return 1f/actualPushInterval * (USE_NANOS?1000000000:1000);
	}

	public int getPreLead(){
		return (int)(newSmoothenerOffset / VTICKS_PER_SAMPLE); //ok, this will technically lead a little, but seriously
	}

	public void setPreLead(int leadSamples){
		//blah.println("Setting PRELEAD to "+leadVTicks);
		this.newSmoothenerOffset = leadSamples * VTICKS_PER_SAMPLE;
	}

	private float actualDelayFrames;
	private float actualDelayMS;
	public float getActualDelayMS(){
		return actualDelayMS;
	}

	public int getDesiredDelayMS(){
		return (int)(delay / (USE_NANOS?1000000:1));
	}

	public float getActualDelayFrames(){
		return actualDelayFrames;
	}


	private float minOutputVTickSpeedGARSD = Float.MAX_VALUE;
	private float maxOutputVTickSpeedGARSD = -Float.MAX_VALUE;
	private float minAddIntervalGARSD = Float.MAX_VALUE;
	private float maxAddIntervalGARSD = -Float.MAX_VALUE;

	/**
	 * useful for debugging, to see how much the time is fluctuating.  1.0 would be ideal.
	 * not particularly threadsafe, but fine if this information is just for visualizing.
	 * @return
	 */
	public float getAndResetSpeedDelta(){
		if(mode == REAL_TIME_INPUT){
			//high delta's here is only bad if your input is supposed to be regualar, which it may not for this mode.
			float ret = maxAddIntervalGARSD / minAddIntervalGARSD;
			minAddIntervalGARSD = Float.MAX_VALUE;
			maxAddIntervalGARSD = -Float.MAX_VALUE;
			return ret;
		}else{
			//high delta's here is bad.  probably means the delay is too low.
			float ret = maxOutputVTickSpeedGARSD / minOutputVTickSpeedGARSD;
			minOutputVTickSpeedGARSD = Float.MAX_VALUE;
			maxOutputVTickSpeedGARSD = -Float.MAX_VALUE;
			return ret;
		}
	}

	int rails;
	public int getRails(){
		return rails;
	}



	/**
	 * This method relates only to the VIRTUAL_TIME_INPUT mode of this buffer.<br>
	 * Calculates the appropriate push filter constant to acheive a delay of desiredDelay vTicks
	 * if the behavior update rate is graphicalFPS and the motor update rate is motorFPS.
	 * <br><br>
	 * The constant returned is approximate; it assumes that the progress during one frame is
	 * significantly less than the delay amount.  This should be fine unless you try delay
	 * amounts less than a few thousand vTicks.
	 * <br>
	 * @param graphicalFPS the behavior update rate (rate this buffer gets data)
	 * @param motorFPS the output rate of this buffer
	 * @param desiredDelayVTicks the desired delay in vTicks, ie (frames_to_lag * 1000)
	 * @return the filter constant to acheive this delay
	 */
	/*

	protected float calculatePushFilter(float graphicalFPS, float motorFPS, long desiredDelayVTicks){
		float oneMinusF = (1000*graphicalFPS)/(motorFPS*desiredDelayVTicks);
		float f = 1 - oneMinusF;
		return f;
	}
	*/

	private int updatesOfPushFilter;
	private float pushFilterFilter = 0.95f;
	protected void updatePushFilter(){
		if(updatesOfPushFilter++%4 == 0){
			smoothenerOffset = newSmoothenerOffset;
			//do it now and then.
			float addFPS = 1f/(addInterval) * (USE_NANOS?1000000000:1000);

			//convert desired ms delay into vticks, there are addFPS * 1000 vticks/sec //s
			//and delayInVTicks = vTicksPerSecnond * (delay/1000) //t
			//so we get delayInVTicks = addFPS * 1000 * delay / 1000 = addFPS * delay.  weird.
			//float delayInVTicks = (addFPS * (delay+smoothenerOffsetMultiplier*delay));
			float delayInVTicks = (addFPS * delay)*(VTICKS_PER_SAMPLE/(USE_NANOS?1000000000:1000))+smoothenerOffset;
			float newFilter = delayInVTicks/(delayInVTicks+VTICKS_PER_SAMPLE);
			//float newFilter = calculatePushFilter(addFPS, 1f/actualPushInterval * 1000, delayInVTicks);
			if(newFilter >= 1){
				System.out.println("outputTimeFilter wanted to be > 1, no way.");
				newFilter = 0.999f;
			}
			if(newFilter <= 0){
				System.out.println("outputTimeFilter wanted to be < 0, no way.");
				newFilter = 0.001f;
			}

			outputTimeFilter = newFilter * (1 - pushFilterFilter) + pushFilterFilter * outputTimeFilter;
			//StaticGraph.recordStatic("TemporalFilter*100", System.currentTimeMillis()-startTime, outputTimeFilter*100);
		}
	}


	/**
	 * This should return 1 when we're at the desired delay, <1 if actual is less, and >1 if actual is more.
	 * but it should do it to make the output smooth!!
	 * @param actualDelay
	 * @param desiredDelay
	 * @return
	 */
	/*
	protected float calcSpeedMod(float actualDelay, long desiredDelay){
		//return 1;
		//(.01x)^3+1, where x = actualDelay-desiredDelay
		float sx = (actualDelay - desiredDelay)*0.005f;
		float fx = sx*sx*sx+1;

		blah.println("returning "+fx+" for actual:"+actualDelay+" desired:"+desiredDelay+" delta:"+(actualDelay-desiredDelay));
		return fx;
		//float min = 0.3f;
		//float v = (actualDelay+min)/(float)(desiredDelay+min);
		//return v;
	}
	*/
	/*
	static PrintStream blah;
	static{
		try{
			blah = new PrintStream(new FileOutputStream("/dev/ttyp2"),true);
		} catch(FileNotFoundException e){
			e.printStackTrace();
			System.exit(1);
		}
	}
	*/

	protected long filterIt(long timeToPush, long lastTimePushed, float fractionOfGTick){
		//make it from 0!
		//float addFPS = 1f/(addInterval) * 1000;
		//timeToPush+=smoothenerOffsetMultiplier*delay*addFPS;
		timeToPush+=smoothenerOffset;
		long delta = timeToPush-lastTimePushed;

		double alpha = Math.pow(outputTimeFilter, fractionOfGTick);

		long filteredDelta = delta - (long)(delta*alpha);

		//blah.println("converted "+outputTimeFilter+" to "+alpha+" gFrac:"+fractionOfGTick+"\n got:"+(filteredDelta+lastTimePushed)+" between "+lastTimePushed+" and "+(timeToPush-smoothenerOffset));
		return filteredDelta+lastTimePushed;
	}

	private long delay = 200*(USE_NANOS?1000000:1);
	private long lastPushUpdate = 0;
	// private float outputTimeFilter = 0.985f;
	private float outputTimeFilter = 0.95f;
	private float actualPushInterval = 0;
	private long lastTimePushed = 0;
	private float outputFPSFilter = 0.99f;
	private long smoothenerOffset = 10*VTICKS_PER_SAMPLE;
	private long newSmoothenerOffset = smoothenerOffset;
	//private long smoothenerOffsetMultiplier = 3;
	private long lastTimeToPushUnfiltered = Long.MIN_VALUE;
	public void run() {
		while(keepPushing){
			Object curData = null;
			long timeToPush = 0;
			long curTime = 0;
			synchronized(this){
				curTime = getTime();
				if(mode == REAL_TIME_INPUT){
					//leave curTime alone
					timeToPush = curTime - delay;
				}else if(mode == VIRTUAL_TIME_INPUT){
					if(addInterval==0){ //we don't have any data yet.  might need to wait even more than this.
						timeToPush = vIndex; //start us off at now.
					}else{
						//change it to virtual time coords.
						long timeSinceLastAdd = curTime - lastAdd;
						long addIntervalOffset = (long)((VTICKS_PER_SAMPLE * timeSinceLastAdd) / addInterval);
						timeToPush = addIntervalOffset + vIndex;// this is now
						if(timeToPush < lastTimeToPushUnfiltered + 0*actualPushInterval*0.5f){//dont backtrack.  this is 0.5 bit kinda random bullshit, to help smooth
							timeToPush = lastTimeToPushUnfiltered + 0*(long)(actualPushInterval*0.5f);
						}
						lastTimeToPushUnfiltered = timeToPush;
						long tSinceLastPush = curTime - lastPushUpdate;
						if(lastTimePushed!=0 && addInterval!=0){
							//long timeToAdvance = (long)(1000f * millisSinceLastPush/addInterval); //(distance * thisTime/totalTime)
							//float speedMod = calcSpeedMod(((timeToPush-(lastTimePushed+timeToAdvance))*addInterval/1000f), delay);
							//timeToPush = (long)(lastTimePushed + speedMod*timeToAdvance);
							//timeToPush = (long)(outputTimeFilter * lastTimePushed + (1-outputTimeFilter)*timeToPush);
							//float ttp = timeToPush;
							timeToPush = filterIt(timeToPush, lastTimePushed, (float)tSinceLastPush/addInterval);
							//if(timeToPush < lastTimePushed){
								//blah.println("Filtered "+ttp+" to "+timeToPush+" even with last="+lastTimePushed+" and Millis:"+millisSinceLastPush+" and addI"+addInterval);
								//System.exit(1);
							//}
						}
					}
				}
				curData = getDataAtTime(timeToPush);
			}

			if(lastPushUpdate!=0){
				long thisInterval = curTime - lastPushUpdate;
				long bufferInterval = timeToPush - lastTimePushed;
				float timePerTime = (float)bufferInterval/(float)thisInterval;
				///these max/mins are maintained for debugging.
				if(timePerTime > maxOutputVTickSpeedGARSD){
					maxOutputVTickSpeedGARSD = timePerTime;
				}
				if(timePerTime < minOutputVTickSpeedGARSD){
					minOutputVTickSpeedGARSD = timePerTime;
				}
				//StaticGraph.recordStatic("renderInterval (ms)", curTime - startTime, thisInterval);
				//StaticGraph.recordStatic("renderSpeed (VTicks/ms)", curTime - startTime, timePerTime);
				if(actualPushInterval == 0){
					//start it off without filtering.
					actualPushInterval = thisInterval;
				}else{
					actualPushInterval = outputFPSFilter*actualPushInterval + (1-outputFPSFilter)*(float)thisInterval;
				}
				//StaticGraph.recordStatic("pushIntervalFiltered", curTime - startTime, actualPushInterval);
			}else{

			}
			//if(curData!=null){
			//	ReceiveMotorPackets.graphIt(((float[])curData)[27]);
			//}

			boolean didOutput = outputData(curData);
			if(didOutput){
				lastTimePushed = timeToPush;
				lastPushUpdate = curTime;
			}
			//System.out.println("sleeping for "+pushSleep+" ms");
			try{Thread.currentThread().sleep(pushSleep);}catch(Exception ex){ex.printStackTrace();}
		}
	}

}
