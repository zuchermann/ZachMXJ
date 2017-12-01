package innards.util;

import innards.math.BaseMath;

import java.io.*;
import java.nio.*;
import java.nio.channels.FileChannel;

import innards.math.jvec.Jvec;

/**
	float images, createImae and MemeoryImageSources are not supported
 */
public class FloatBufferImage
{
	static Jvec jv = null;
	static
	{
		if (Jvec.IS_SUPPORTED)
		{
			jv = new Jvec();
		}
	}
	public static boolean readRawAlpha= false;
	public FloatBuffer data;

	public int w, h;

	public FloatBufferImage(int w, int h)
	{
		this.w= w;
		this.h= h;
		if (Jvec.IS_SUPPORTED)
		{
			data= jv.newFloatBuffer(w * h);
			jv.vzero(data);
		}
		else
		{
			data = ByteBuffer.allocateDirect(w * h * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		}
	}
	public FloatBufferImage(FloatBuffer from, int w, int h)
	{
		this.w= w;
		this.h= h;
		data= from;
	}

	public void clear(int to)
	{
		for (int y= 0; y < h; y++)
			for (int x= 0; x < w; x++)
			{
				set(x, y, to);
			}
	}

	public void multiply(float f)
	{
		for (int y= 0; y < h; y++)
			for (int x= 0; x < w; x++)
			{
				float c= get(x, y);
				set(x, y, c * f);
			}
	}

	public void multiply(int x, int y, float f)
	{
		float c= get(x, y);
		set(x, y, c * f);
	}

	public int getWidth()
	{
		return w;
	}
	public int getHeight()
	{
		return h;
	}

	public void set(int x, int y, float z)
	{
		x= (x + w) % w;
		y= (y + h) % h;
		data.put(y * w + x, z);
	}

	public float get(int x, int y)
	{
		x= (x + w) % w;
		y= (y + h) % h;
		return data.get(y * w + x);
	}

	public void readRaw(String filename)
	{
		int x= -1;
		int y= -1;

		try
		{
			FileChannel rwChannel= new RandomAccessFile(new File(filename), "rw").getChannel();
			ByteBuffer wrBuf= rwChannel.map(FileChannel.MapMode.READ_WRITE, 0, (int) data.capacity() * 2);
			ShortBuffer shBuf= wrBuf.asShortBuffer();
			for (int i= 0; i < shBuf.capacity(); i++)
			{
				int z= shBuf.get();
				if (z < 0)
					z= 0xffff + ((int) z);
				//System.out.println(z);				
				data.put(z / (float) (0xffff));
			}
			data.rewind();

		} catch (Exception ex)
		{
			System.out.println(" at :" + x + " " + y);
			ex.printStackTrace();
		}
	}

	public float[] getFloatArray()
	{
		return data.array();
	}
	public void copy(FloatBufferImage from)
	{
		for (int y= 0; y < h; y++)
		{
			for (int x= 0; x < w; x++)
			{
				set(x, y, from.get(x, y));
			}
		}
	}

	public void saveRaw(String filename)
	{
		System.out.println(" saving framebuffer to file <" + filename + ">");
		try
		{
			BufferedOutputStream bos= new BufferedOutputStream(new FileOutputStream(new File(filename)));

			for (int y= 0; y < h; y++)
			{
				for (int x= 0; x < w; x++)
				{
					int d= (int) (data.get(y * w + x) * 0xffff);
					bos.write((d >> 8) & 255);
					bos.write(d & 255);
				}
			}

			bos.close();
		} catch (IOException ex)
		{
			System.out.println(" problem saving FrameBuffer to file <" + filename + ">");
			ex.printStackTrace();
		}
	}

	public void divide(FloatBufferImage buffer)
	{
		if (buffer.w != this.w || buffer.h != this.h)
			throw new IllegalArgumentException(" dimension mismatch <" + buffer.w + " " + this.w + " " + buffer.h + " " + this.h + ">");
		for (int y= 0; y < h; y++)
		{
			for (int x= 0; x < w; x++)
			{
				float z= buffer.get(x, y);
				if (z != 0)
				{
					this.set(x, y, this.get(x, y) / z);
				}
			}
		}
	}

	static ThreadLocal zeroLocal= new ThreadLocal()
	{
		protected Object initialValue()
		{
			return null;
		}
	};
	static ThreadLocal fftInit= new ThreadLocal()
	{
		protected Object initialValue()
		{
			return null;
		}
	};

	public FloatBufferImage[] fourierTransformImage(FloatBufferImage real, FloatBufferImage imag)
	{
		FloatBuffer[] f= fourierTransform(real.data, imag.data);
		return new FloatBufferImage[] { real, imag };
	}

	public FloatBuffer[] fourierTransform(FloatBuffer real, FloatBuffer imag)
	{
		if (!Jvec.IS_SUPPORTED) throw new UnsupportedOperationException("Jvec-based FFT is not supported on Intel just yet.");
		
		if (imag == null)
			imag= jv.newFloatBuffer(data.capacity());
		if (real == null)
			real= jv.newFloatBuffer(data.capacity());

		if (zeroLocal.get() == null)
			zeroLocal.set(jv.newFloatBuffer(data.capacity()));
		if (((FloatBuffer) zeroLocal.get()).capacity() != data.capacity())
			zeroLocal.set(jv.newFloatBuffer(data.capacity()));

		if (fftInit.get() == null)
			fftInit.set(jv.initializeFFT(jv.log2Of((int) Math.sqrt(data.capacity()))));
		if (((Jvec.FFTInit) fftInit.get()).size() != jv.log2Of((int) Math.sqrt(data.capacity())))
			fftInit.set(jv.initializeFFT(jv.log2Of((int) Math.sqrt(data.capacity()))));

		jv.performComplex2DFFTOutOfPlace((Jvec.FFTInit) fftInit.get(), data, ((FloatBuffer) zeroLocal.get()), real, imag);

		return new FloatBuffer[] { real, imag };
	}

	static public FloatBuffer[] fourierTransfrom(FloatBuffer realI, FloatBuffer imagI, FloatBuffer realO, FloatBuffer imagO)
	{
		if (!Jvec.IS_SUPPORTED) throw new UnsupportedOperationException("Jvec-based FFT is not supported on Intel just yet.");

		if (realO == null)
			realO= jv.newFloatBuffer(realI.capacity());
		if (imagO == null)
			imagO= jv.newFloatBuffer(realI.capacity());

		
		jv.performComplex2DFFTOutOfPlace((Jvec.FFTInit) fftInit.get(), realI, imagI, realO, imagO);

		return new FloatBuffer[] { realO, imagO };
	}

	/**
	 * @return
	 */
	public FrameBuffer createFrameBuffer()
	{
		FrameBuffer ret= new FrameBuffer(this.w, this.h);
		for (int y= 0; y < h; y++)
		{
			for (int x= 0; x < w; x++)
			{
				float zz = this.get(x, y);
				int z= FrameBuffer.colour(zz<0 ? 0 : (zz>1 ? 1 : zz));
				ret.set(x, y, z);
			}

		}
		return ret;
	}
}