package innards.sound.au;

import innards.math.BaseMath;
import innards.util.ResourceLocator;

import java.nio.FloatBuffer;


/**
 * 
 * biting the 
 * @author marc
 * Created on May 23, 2003
 */
public class AUV2Wrapper
{

	static final int default_ramp_sample_time= 512;

	static {
		ResourceLocator.loadLibrary("libauv2wrapper.jnilib");
	}

	// 64-bit ready
	protected long nativePeer;

	public AUV2Wrapper(String type, String subType, String manu)
	{
		this(fourCC(type), fourCC(subType), fourCC(manu));
	}

	public AUV2Wrapper(int type, int subType, int manu)
	{
		nativePeer= nativeInit(type, subType, manu);
		if (nativePeer == 0)
			throw new IllegalArgumentException(" couldn't init class for <" + type + "> <" + subType + "> <" + manu + ">");
	}

	public AUV2Wrapper makeMono()
	{
		if (nativeMakeMono(nativePeer) == 0)
			throw new IllegalArgumentException(" couldn't make mono");
		return this;
	}

	public AUV2Wrapper makeStereo()
	{
		if (nativeMakeStereo(nativePeer) == 0)
			throw new IllegalArgumentException(" couldn't make stereo");
		return this;
	}

	boolean uiIsOpen= false;
	public boolean openUI()
	{
		return uiIsOpen= nativeOpenUI(nativePeer);
	}

	public void closeUI()
	{
		if (uiIsOpen)
		{
			nativeCloseUI(nativePeer);
			uiIsOpen= false;
		}
	}

	public void renderSliceMono(FloatBuffer monoIn, FloatBuffer out)
	{
		sourceBuffer= monoIn;
		outputBuffer= out;
		sourceBufferRight= monoIn;

		nativeRender(nativePeer);

		sampleTime += out.capacity();
//		hostTime += HostTime.convertNanosToHostTime((long) (0.5f * out.capacity() * 1000000000L / 44100.0));
	}

	public void renderSliceStereo(FloatBuffer left, FloatBuffer right, FloatBuffer oleft, FloatBuffer oright)
	{
		sourceBuffer= left;
		sourceBufferRight= right;
		outputBuffer= oleft;
		outputBufferRight= oright;

		nativeRenderStereo(nativePeer);

		sampleTime += oleft.capacity();
//		hostTime += HostTime.convertNanosToHostTime((long) (0.5f * oleft.capacity() * 1000000000L / 44100.0));
	}

	public Parameter guessParameter(String name, int scope)
	{
		long parameterBlock= nativeGuessParameter(nativePeer, name, scope);
		if (parameterBlock == 0)
			throw new IllegalArgumentException(" couln't init class for <" + name + "> <" + scope + ">");
		return new Parameter(parameterBlock, scope);
	}

	public void openMidi(String destName)
	{
		nativeOpenMidi(nativePeer, destName);
	}

	public class Parameter
	{
		long block;
		int scope;
		public Parameter(long block, int scope)
		{
			this.block= block;
			this.scope= scope;
		}

		public void set(float value)
		{
			nativeSetParameter(block, value, scope, default_ramp_sample_time);
		}
	}
	protected FloatBuffer sourceBuffer;
	protected FloatBuffer outputBuffer;
	protected FloatBuffer sourceBufferRight;
	protected FloatBuffer outputBufferRight;
	protected double sampleTime;
	protected double hostTime;
	protected FloatBuffer obtainInputBuffer()
	{
		return sourceBuffer;
	}
	protected FloatBuffer obtainInputBufferRight()
	{
		return sourceBufferRight;
	}

	protected double obtainSampleTime()
	{
		return sampleTime;
	}

	protected double obtainHostTime()
	{
		return hostTime;
	}

	protected FloatBuffer obtainOutputBuffer()
	{
		return outputBuffer;
	}
	protected FloatBuffer obtainOutputBufferRight()
	{
		return outputBufferRight;
	}

	static public int fourCC(String s)
	{
		assert s.length() == 4 : "length should be 4, is <" + s.length() + ">";
		int i=
			(BaseMath.intify((byte) s.charAt(0)) << 24)
				| (BaseMath.intify((byte) s.charAt(1)) << 16)
				| (BaseMath.intify((byte) s.charAt(2)) << 8)
				| (BaseMath.intify((byte) s.charAt(3)));
		return i;
	}
	
	public void dlsSetBank(String filename)
	{
		nativeLoadBank(nativePeer, filename);
	}

	native protected long nativeInit(int type, int subType, int manu);
	native protected long nativeRender(long peer);
	native protected long nativeRenderStereo(long peer);
	native protected void nativeSetParameter(long block, float value, int scope, int ahead);
	native protected long nativeGuessParameter(long peer, String name, int scope);
	native protected long nativeMakeMono(long peer);
	native protected long nativeMakeStereo(long peer);
	native protected boolean nativeOpenUI(long peer);
	native protected boolean nativeCloseUI(long peer);
	native protected void nativeOpenMidi(long peer, String name);
	native protected void nativeLoadBank(long peer, String filen);
}
