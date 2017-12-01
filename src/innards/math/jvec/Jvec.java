package innards.math.jvec;

import innards.math.BaseMath;
import innards.util.ResourceLocator;

import java.lang.reflect.Field;
import java.nio.*;

/**
	files for makeing altivec calls from java
 */
public class Jvec 
{
	// mattb: adding some hacks to guarantee intel source compatibility - this library only works on G4s and G5s.
	// a better long-term solution would be to make a universal vector acceleration library.
	public static final boolean IS_SUPPORTED = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
	
	static {
		if (IS_SUPPORTED)
		{
			System.load(ResourceLocator.getPathForResource("libCharactersSound.jnilib"));
		}
	}
	
	public Jvec()
	{
		if (!IS_SUPPORTED)
		{
			UnsupportedOperationException e = new UnsupportedOperationException("Jvec is not supported on Intel just yet!");
			e.printStackTrace();
			throw e;
		}
	}

	public FloatBuffer newFloatBuffer(int i)
	{
		return newAlignedStorage(i * 4).asFloatBuffer();
	}
	public native ByteBuffer newAlignedStorage(int i);

	public native void freeAlignedStorage(Buffer b);

	// one vector, one scalar -> one vector

	public void vadd(FloatBuffer one, float scalar, FloatBuffer result)
	{
		// coded out underneal
		if (one.capacity()%4!=0)throw new IllegalArgumentException(one.capacity()+"");
		vaddS(one, scalar, result, one.capacity());
	}
	native public void vaddS(FloatBuffer one, float scalar, FloatBuffer result, int i);

	public void vmul(FloatBuffer one, float scalar, FloatBuffer result)
	{
		vmulS(one, scalar, result, one.capacity());
	}
	native public void vmulS(FloatBuffer one, float scalar, FloatBuffer result, int i);

	// two vectors -> one vector ---------------------------------- ---------------------------------- ----------------------------------

	public void vadd(FloatBuffer one, FloatBuffer two, FloatBuffer result)
	{
		vadd(one, two, result, one.capacity());
	}
	native public void vadd(FloatBuffer one, FloatBuffer two, FloatBuffer result, int length);

	public void vmul(FloatBuffer one, FloatBuffer two, FloatBuffer result)
	{
		vmul(one, two, result, one.capacity());
	}
	native public void vmul(FloatBuffer one, FloatBuffer two, FloatBuffer result, int length);


	public void vmulscalaraddvec(FloatBuffer one,float mulOne,FloatBuffer addScaledOne,FloatBuffer result){
		nativeZeQaXpY(one,mulOne,addScaledOne,result,result.capacity());
	}
	//ie native method z eQuals a X plus Y
	native public void nativeZeQaXpY(FloatBuffer x,float alpha,FloatBuffer y, FloatBuffer z, int length);

	public void vsub(FloatBuffer one, FloatBuffer two, FloatBuffer result)
	{
		vsub(one, two, result, one.capacity());
	}
	native public void vsub(FloatBuffer one, FloatBuffer two, FloatBuffer result, int length);

	public void vdiv(FloatBuffer one, FloatBuffer two, FloatBuffer result)
	{
		// coded out underneath
		if (one.capacity()%4!=0)throw new IllegalArgumentException(one.capacity()+"");
		if (two.capacity()%4!=0)throw new IllegalArgumentException(two.capacity()+"");
		vdiv(one, two, result, one.capacity());
	}
	native public void vdiv(FloatBuffer one, FloatBuffer two, FloatBuffer result, int length);

	// three vectors -> one vector

	// no docs for which way around this is....
	public void vam(FloatBuffer one, FloatBuffer two, FloatBuffer three, FloatBuffer out)
	{
		vam(one, two, three, out, out.capacity());
	}
	native public void vam(FloatBuffer one, FloatBuffer two, FloatBuffer three, FloatBuffer out, int i);

	// two vectors -> one scalar
	public float vdot(FloatBuffer one, FloatBuffer two)
	{
		return vdot(one, two, one.capacity());
	}
	native public float vdot(FloatBuffer one, FloatBuffer two, int i);
	
	public void vinterleave(FloatBuffer one, FloatBuffer two, FloatBuffer out)
	{
		vinterleave(one, two, out, one.capacity());
	}
	native public float vinterleave(FloatBuffer o, FloatBuffer t, FloatBuffer oo, int i);
	

	public void convertARGBToFloat(ByteBuffer inBuffer,FloatBuffer outBuffer){
		nativeConvertARGBToFloat(inBuffer,outBuffer,outBuffer.capacity());
	}
	native public void nativeConvertARGBToFloat(ByteBuffer byteBuffer,FloatBuffer floatBuffer, int length);

	public int 	findMinAbs(FloatBuffer buffer){
		return nativeFindMinAbs(buffer,buffer.capacity());
	}
	native public int nativeFindMinAbs(FloatBuffer fBuffer, int length);
	
	public int findMaxMag(FloatBuffer buffer){
		return nativeFindMaxMag(buffer,buffer.capacity());
	}
	native public int nativeFindMaxMag(FloatBuffer fButter, int length);
		
	
	native public void vForwardWaveletTransform(IntBuffer rgb8in, LongBuffer rgb16Out, LongBuffer temp, int width, int height);
	native public void vInverseWaveletTransform(LongBuffer rgb16in, IntBuffer rgb8Out, LongBuffer temp, int width, int height);
	
	
	native public void v5x5Convolve(FloatBuffer image, int width, int height, FloatBuffer kernel, FloatBuffer output);

	// dsp code --------------------------------------------------------------------------------------------------------------------------
	public FFTInit initializeFFT(int log2Size)
	{
		System.out.println("abot");
		FFTInit i = new FFTInit(nativeInitializeFFT(log2Size), log2Size);
		System.out.println("abot");
		return i;
	}
	native public int nativeInitializeFFT(int l);

	public class FFTInit
	{
		protected int ref;
		protected FloatBuffer tempI;
		protected FloatBuffer tempO;
		protected FloatBuffer temp3;
		protected int log2Size;
		protected int size;
		FFTInit(int i, int log2Size)
		{
			ref= i;
			size= 1 << log2Size;
			int len= 4 * (size);
			tempI= newFloatBuffer(len);
			tempO= newFloatBuffer(len);
			temp3= newFloatBuffer(len);
			this.log2Size= log2Size;
		}

		// returns index that you'll want to look at
		// actually, the docs are not clear enough to know what is going on. use performComplexFFT
		public int readRealTransform(int element)
		{
			if (element == 0)
				return 0;
			if (element == size - 1)
				return 1;
			return element + 1;
		}

		/**
		 * @return
		 */
		public int size()
		{
			return log2Size;
		}

	}

	// you'll need to use FFTInit.readRealTransform to get at the results of this thing, because the results are packed in a particular way
	// actually, the docs are not clear enough to know what is going on. use performComplexFFT
	public void performRealFFT(FFTInit init, FloatBuffer realInplace, FloatBuffer imagOut)
	{
		performRealFFT(init.ref, realInplace, imagOut, init.tempI, init.tempO, init.log2Size);
	}

	native public void performRealFFT(int i, FloatBuffer inR, FloatBuffer outI, FloatBuffer temp1, FloatBuffer temp2, int l2);

	// this doesn't seem to scale things at all (despite docs)
	// so...

	//j.vmul(f1, 1/(float)Math.sqrt(len), f1);
	//j.vmul(f2, 1/(float)Math.sqrt(len), f2);
	// might be what you are looking for if you want a round trip to not scale by len
	public void performComplexFFT(FFTInit init, FloatBuffer realInplace, FloatBuffer imagInplace, boolean forward)
	{
		performComplexFFT(init.ref, realInplace, imagInplace, init.tempI, init.tempO, init.log2Size, forward ? 1 : 0);
	}

	native public void performComplexFFT(int i, FloatBuffer inR, FloatBuffer outI, FloatBuffer temp1, FloatBuffer temp2, int l2, int com);

	// this doesn't seem to scale things at all (despite docs)
	// so...

	//j.vmul(f1, 1/(float)Math.sqrt(len), f1);
	//j.vmul(f2, 1/(float)Math.sqrt(len), f2);
	// might be what you are looking for if you want a round trip to not scale by len
	public void performComplexFFTOutOfPlace(
		FFTInit init,
		FloatBuffer realIn,
		FloatBuffer imagIn,
		FloatBuffer realOut,
		FloatBuffer imagOut,
		boolean forward)
	{
		performComplexFFTOutOfPlace(init.ref, realIn, imagIn, realOut, imagOut, init.tempI, init.tempO, init.log2Size, forward ? 1 : 0);
	}

	native public void performComplexFFTOutOfPlace(
		int i,
		FloatBuffer inR,
		FloatBuffer inI,
		FloatBuffer outR,
		FloatBuffer outI,
		FloatBuffer temp1,
		FloatBuffer temp2,
		int l2,
		int com);

	public void complexToMagnitudePhase(FloatBuffer realIn, FloatBuffer imagIn, FloatBuffer magOut, FloatBuffer phaseOut)
	{
		assert(
			(realIn.capacity() == imagIn.capacity())
				&& (imagIn.capacity() == magOut.capacity())
				&& (magOut.capacity() == phaseOut.capacity())) : "dimension mismatch";
		performComplexToMP(realIn, imagIn, magOut, phaseOut, phaseOut.capacity());
	}

	public native void performComplexToMP(FloatBuffer r, FloatBuffer i, FloatBuffer m, FloatBuffer p, int ll);
	
	public void complexToMagnitude(FloatBuffer realIn, FloatBuffer imagIn, FloatBuffer magOut)
	{
		assert(
			(realIn.capacity() == imagIn.capacity())
				&& (imagIn.capacity() == magOut.capacity())) : "dimension mismatch";
		performComplexToM(realIn, imagIn, magOut, magOut.capacity());
	}

	public native void performComplexToM(FloatBuffer r, FloatBuffer i, FloatBuffer m, int ll);

	public void performReal2DFFTOutOfPlace(FFTInit i, FloatBuffer in, FloatBuffer zero, FloatBuffer outR, FloatBuffer outI)
	{
		int l2 = (int)(Math.log(Math.sqrt(in.capacity()))/Math.log(2));
		int w = (int)Math.sqrt(in.capacity());
		performReal2DFFTOutOfPlace(i.ref, in, zero, outR, outI, w, l2,l2);
	}
	
	public native void performReal2DFFTOutOfPlace(int ref, FloatBuffer in, FloatBuffer zero, FloatBuffer outR, FloatBuffer outI, int w, int c, int c2);

	public void performComplex2DFFTOutOfPlace(FFTInit i, FloatBuffer in, FloatBuffer inI, FloatBuffer outR, FloatBuffer outI)
	{
		int l2 = (int)(Math.log(Math.sqrt(in.capacity()))/Math.log(2));
		int w = (int)Math.sqrt(in.capacity());
		performComplex2DFFTOutOfPlace(i.ref, in, inI, outR, outI, w, l2,l2);
	}
	public native void performComplex2DFFTOutOfPlace(int ref, FloatBuffer in, FloatBuffer zero, FloatBuffer outR, FloatBuffer outI, int w, int c, int c2);


	/**
	 * currently only featureDimension = 1 is supported
	 * @param features1In
	 * @param weights1In
	 * @param features2In
	 * @param weights2in
	 * @param featureDimension
	 * @param weightsIn
	 * @return
	 */
	public float performEMD(FloatBuffer features1In, FloatBuffer weights1In, FloatBuffer features2In, FloatBuffer weights2in, int featureDimension)
	{
		return performEMD(
			features1In,
			features1In.capacity(),
			weights1In,
			weights1In.capacity(),
			features2In,
			features2In.capacity(),
			weights2in,
			weights2in.capacity(),
			featureDimension);
	}

	native private float performEMD(
		FloatBuffer features1In,
		int i,
		FloatBuffer weights1In,
		int j,
		FloatBuffer features2In,
		int k,
		FloatBuffer weights2in,
		int l,
		int featureDimension);
	/**
		vectorized random number generator 
	
		TODO, bah, needs all kind of long routines, lets just put it native side
	
	float ran0(long *seed)
	{
		long k;
	
		k= (*seed)/127773;
		*seed=16807*( (*seed)-k*12773)-2836*k;
		if ((*seed)<0) *seed += 2147483647;
		return (*seed)/2147483647.0;
	}
	*/

	// if seed is null, returns a seed array, use this in subsequent calls, but don't interpret it 
	public LongBuffer vran0(LongBuffer seed, FloatBuffer buffer)
	{
		if ((seed!=null) && (seed.capacity() != buffer.capacity()))
		{
			System.out.println(" freeing buffer");
			freeAlignedStorage(seed);
			seed= null;
		}
		if (seed == null)
		{
			seed= this.newAlignedStorage(buffer.capacity() * 8).asLongBuffer();
			for (int i= 0; i < seed.capacity(); i++)
			{
				seed.put((long) (Math.random() * Long.MAX_VALUE));
			}
		}
		vrnd0(seed, buffer, buffer.capacity());
		return seed;
	}

	// note, long's are not accelerated it appears, this code, despite the name, isn't accelerated.

	native public void  vrnd0(LongBuffer seed, FloatBuffer buffer, int length);
	
	// altivec implementation of the mersenne twister algorithm. 5 times faster than carefully optimized scalar
	// rnd0 it seems.... (and some 60 times faster than calling Math.random() a lot)
	// seed is ignored
	native public void vrnd0v(IntBuffer seed, FloatBuffer output, int length);

	// like vrnd0v2 but insteady of outputing r1,r2,r3,r4 .... it does r1,r2,r1,r2,r3,r4,r3,r4 ....
	
	native public void vrnd0v2(Object object, FloatBuffer aux6, int i);

	static public String toString(FloatBuffer f)
	{
		String s= "";
		for (int i= 0; i < f.capacity(); i++)
		{
			s += f.get(i) + "\n";
		}
		return s;
	}

	public String toString(FloatBuffer[] f)
	{
		String s= "";
		for (int i= 0; i < f[0].capacity(); i++)
		{
			for (int m= 0; m < f.length; m++)
			{
				s += BaseMath.toDP(f[m].get(i), 4) + " ";
			}
			s += "\n";
		}
		return s;
	}

	static public void main(String[] s)
	{
		Jvec j= new Jvec();

		int l2= 8;
		int len= 1 << l2;

		FloatBuffer f1= j.newFloatBuffer(len);
		FloatBuffer f2= j.newFloatBuffer(len);
		for (int i= 0; i < f1.capacity(); i++)
		{
			f1.put(i, 1000 * (float) Math.cos(2 * Math.PI * 3 * i / (float) f1.capacity()));
			f2.put(i, 0);
		}

		Jvec.FFTInit in= j.initializeFFT(l2);

		for (int i= 0; i < 10; i++)
		{
			System.out.println((i * len) + " forward.... ");
			j.performComplexFFT(in, f1, f2, true);

			j.vmul(f1, 1 / (float) len, f1);
			j.vmul(f2, 1 / (float) len, f2);
			//System.out.println(j.toString(f1));

			System.out.println(" and back again.... ");
			j.performComplexFFT(in, f1, f2, false);
		}
		System.out.println(j.toString(f1));

	}

	static public void main0(String[] s)
	{
		Jvec t= new Jvec();

		FloatBuffer f1= t.newFloatBuffer(1000000);
		FloatBuffer f2= t.newFloatBuffer(1000000);
		for (int i= 0; i < f1.capacity(); i++)
		{
			f1.put(i, i + 1);
			f2.put(i, 1 / (float) (i + 1));
		}
		FloatBuffer f3= t.newFloatBuffer(1000000);
		//		System.out.println( " adding ... ");
		//		t.vadd(f1,f2,f3);
		//		System.out.println( " ... adding");

		System.out.println(" dotting .... ");
		System.out.println(t.vdot(f1, f2));
		System.out.println(" dotting .... ");

		//		System.out.println(t.toString(f3));
	}

	static public String printAddress(Object i)
	{
		try
		{
			Field f= Buffer.class.getDeclaredField("address");
			f.setAccessible(true);
			long address= f.getLong(i);
			System.out.println(" address is <" + Long.toHexString(address) + ">");
			return address + "";
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * could be faster
	 * @param temp1
	 */
	public void vzero(FloatBuffer temp1)
	{
		vzero(temp1, temp1.capacity());
	}
	native void vzero(FloatBuffer t1, int l);
	
	public int log2Of(int s)
	{
		return (int)Math.floor(Math.log(s)/Math.log(2));
	}
	
	/**
	 * temp12= temp2*f+temp1;
	 */
	public void vmadd(FloatBuffer temp2, float f, FloatBuffer temp1, FloatBuffer temp12)
	{
		vmadd(temp2, f, temp1, temp12, temp2.capacity());
	}
	native public int vmadd(FloatBuffer temp2, float f, FloatBuffer temp1, FloatBuffer temp12, int length);
}
