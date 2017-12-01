package innards.sound.au;

import java.nio.FloatBuffer;


/**
 * wraps an deprecated V1 AudioUnit, as good as it gets right now from java.
 *  * @author marc
 * Created on May 18, 2003
 */
public class AUV1Wrapper
{
//	AUComponent component;
//	AudioUnit unit;


	/**
	 * note, in the broken world of apple's v1 audio unit, manu refers to the subType and subType to the type of the AU
	 * so:
	 * new AUV1Wrapper(kAudioUnitSubType_Effect, kAudioUnitID_LowPassFilter)
	 * @param subType
	 * @param manu
	 */
	public AUV1Wrapper(int subType, int manu) throws Exception
	{
//		ComponentDescription cd= new ComponentDescription();
//		cd.setSubType(subType);
//		cd.setManufacturer(manu);
//		component= AUComponent.findAU(cd);
//
//		unit= component.open();
//		unit.initialize();
//		
//		unit.setInputCallback(new RenderCallback(), 0);
		
	}

	// just in case Apple fixes this
	public AUV1Wrapper(int type, int subType, int manu) throws Exception
	{
//		ComponentDescription cd= new ComponentDescription();
//		cd.setType(type);
//		cd.setSubType(subType);
//		cd.setManufacturer(manu);
//		component= AUComponent.findAU(cd);
//
//		unit= component.open();
//		unit.initialize();
//		unit.setInputCallback(new RenderCallback(), 0);		
	}


//	AudioTimeStamp inputTimeStamp= new AudioTimeStamp();
//	AudioBuffer buffer;
	FloatBuffer sourceBuffer;

	/**
	 */
	public void renderSliceMono(FloatBuffer monoIn, FloatBuffer out) throws Exception
	{
//		sourceBuffer= monoIn;
//		buffer= new AudioBuffer(1, monoIn.capacity() * 4);
//		unit.renderSlice(0, inputTimeStamp, 0, buffer);
//		for (int i= 0; i < out.capacity(); i++)
//		{
//			out.put(i, buffer.getData().getFloatAt(i * 4));
//		}
//		inputTimeStamp.setSampleTime(inputTimeStamp.getSampleTime() + out.capacity());
//		inputTimeStamp.setHostTime(inputTimeStamp.getHostTime() + HostTime.convertNanosToHostTime((long) (out.capacity() * 1000000000L / 44100.0)));
	}

	public void renderSliceStereo(FloatBuffer monoIn, FloatBuffer out) throws Exception
	{
//		sourceBuffer= monoIn;
//		buffer= new AudioBuffer(2, monoIn.capacity() * 4);
//		unit.renderSlice(0, inputTimeStamp, 0, buffer);
//		for (int i= 0; i < out.capacity(); i++)
//		{
//			out.put(i, buffer.getData().getFloatAt(i * 4));
//		}
//		inputTimeStamp.setSampleTime(inputTimeStamp.getSampleTime() + out.capacity());
//		inputTimeStamp.setHostTime(inputTimeStamp.getHostTime() + HostTime.convertNanosToHostTime((long) (0.5f*out.capacity() * 1000000000L / 44100.0)));
	}
	
	// parameter lookup
	
	public Parameter guessParameter(String name, int scope) throws Exception
	{
//		int[] p = unit.getParameterList(scope);
//		for(int i=0;i<p.length;i++)
//		{
//			AUParameterInfo info = unit.getParameterInfo(scope, p[i]);
//			String iname= info.getName();
//			System.out.println(" name is <"+iname+"> <"+info+">");
//			if (iname.indexOf(name)!=-1)
//			{
//				return new Parameter(info, p[i], scope);
//			}
//		}
		return null;
	}

	public class Parameter
	{
//		AUParameterInfo info;
//		int i;
//		int scope;
//		
//		public Parameter(AUParameterInfo info, int i, int scope)
//		{
//			this.info = info;
//			this.i = i;
//			this.scope = scope;
//		}
//		
//		public void set(float value) throws CAException
//		{
//			unit.setParameter(i, scope, 0, value, 0);
//		}
	}
	

//	public class RenderCallback implements AURenderCallback
//	{
//		public int execute(AudioUnit arg0, int arg1, AudioTimeStamp arg2, int arg3, AudioBuffer buffer)
//		{
//			for (int i= 0; i < sourceBuffer.capacity(); i++)
//			{
//				buffer.getData().setFloatAt(i * 4, sourceBuffer.get(i));
//			}
//			return 0;
//		}
//	}
}
