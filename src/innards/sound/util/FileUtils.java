package innards.sound.util;

import innards.math.BaseMath;
import innards.provider.filter.ButterworthBandpass;

import java.io.*;
import java.io.File;
import java.nio.*;
import java.nio.FloatBuffer;

import javax.sound.sampled.*;
import javax.sound.sampled.spi.AudioFileWriter;


/**
	just checking to see if I can load and save wav files, otherwise it's going to be a long night
 *  * @author marc
 * Created on Feb 25, 2003
 */
public class FileUtils {


	/*  original
	static public FloatBuffer fileToFloatBuffer(String filename) {
		System.out.println(" loading soundfile <" + filename + ">");
		try {
			AudioInputStream a= AudioSystem.getAudioInputStream(new File(filename));
			float[] array= new float[10000];
			int at= 0;
			byte[] b2= new byte[2];
			while (a.available() != 0) {
				if (at >= array.length) {
					float[] array2= new float[1 + array.length * 2];
					System.arraycopy(array, 0, array2, 0, array.length);
					array= array2;
					System.out.println(" array is <"+array.length+">");
				}
				int r= 0;
				while (r < 2)
					r += a.read(b2, r, 2 - r);

				array[at]= toFloat(b2);
				//System.out.println(" read <"+array[at]+"> <"+at+"> <"+b2[0]+"> <"+b2[1]+">");
				//if ((at>500) && (at<550)) System.out.println("--" + BaseMath.toDP(array[at],5));
				at++;
			}
			float[] array2= new float[at];
			System.arraycopy(array, 0, array2, 0, array2.length);
			return FloatBuffer.wrap(array2);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	*/


	static public float[] fileToFloatArray(String filename) {
		System.out.println(" loading soundfile <" + filename + ">");
		try {
			AudioInputStream a= AudioSystem.getAudioInputStream(new File(filename));
			AudioFormat format = a.getFormat();
			System.out.println("FMT:Channels:"+format.getChannels());
			System.out.println("FMT:Encoding:"+format.getEncoding());
			System.out.println("FMT:Frame Rate:"+format.getFrameRate());
			System.out.println("FMT:Frame Size:"+format.getFrameSize());
			System.out.println("FMT:Sample Rate:"+format.getSampleRate());
			System.out.println("FMT:Sample Size(bits):"+format.getSampleSizeInBits());
			System.out.println("FMT:Big Endian:"+format.isBigEndian());
			System.out.println("AIS:numFrames:"+a.getFrameLength());

			int frameSize = format.getFrameSize();	// number of bytes in frame
			int sampleSize = format.getSampleSizeInBits()/8;	// number of bytes per sample
			boolean bigEndian = format.isBigEndian();
			float[] array= new float[(int)(a.getFrameLength()*frameSize/sampleSize)];	// one float per sample
			int at= 0;
			byte[] b2= new byte[frameSize];	// equivalent storage to one frame
			float max=0, min =0;
			while (a.available() != 0) {
				int r= 0;
				while (r < frameSize){
					int re = a.read(b2, r, frameSize - r);	// read as much as possible up to one frame
					if (re<=0) break;
					r+=re;
				}

				if (r != frameSize) break;

				int i = 0;
				while(i < frameSize){
					array[at]= toSignedFloat(b2, i, sampleSize, bigEndian);
					if(array[at]>max)max=array[at];
					if(array[at]<min)min=array[at];
					at++;
					i+=sampleSize;
				}
			}
			if (at != array.length)
			{
				System.out.println("Weird - size of sound buffer array (" +at +") does not match predicted (" +array.length +")");
				float[] array2= new float[at];
				System.arraycopy(array, 0, array2, 0, array2.length);
				return array2;
			}
			else
			{
				return array;
			}
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	static public FloatBuffer fileToFloatBuffer(String filename)
	{
		float[] array = fileToFloatArray(filename);

		if (array != null)
		{
			return FloatBuffer.wrap(array);
		}
		else
		{
			return null;
		}
	}

	static public void floatBufferToFile(String filename, final FloatBuffer f) {
		floatBufferToFile(filename, f, false);
	}

	static public void normalize(FloatBuffer f) {
		float max= 1e-10f;
		for (int i= 0; i < f.capacity(); i++)
			max= Math.abs(f.get(i)) > max ? Math.abs(f.get(i)) : max;
		for (int i= 0; i < f.capacity(); i++)
			f.put(i, 0.95f * f.get(i) / max);
	}

	static public void floatBufferToFile(String filename, final FloatBuffer f, boolean normalize) {
		if (normalize) {
			normalize(f);
		}
		try {
			AudioSystem.write(new AudioInputStream(new InputStream() {
				int m= 0;
				public int available() throws IOException {
					return m < f.capacity() ? f.capacity() - m : 0;
				}

				boolean lo= true;
				public int read() throws IOException {
					float ff= f.get(m);
					if (lo) {
						int i= ((byte) (((int) (ff * Short.MAX_VALUE)) & 255));
						//System.out.print(i);				
						lo= false;
						return BaseMath.intify((byte) i);
					} else {
						int i= ((byte) (((int) (ff * Short.MAX_VALUE) >> 8) & 255));
						//System.out.println(" "+i);				
						lo= true;
						m++;
						return BaseMath.intify((byte) i);
					}
				}
			}, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 1, 2, 44100, false), f.capacity()), AudioFileFormat.Type.WAVE, new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	static public void floatBufferToStereoFile(String filename, final FloatBuffer f, boolean normalize) {
		if (normalize) {
			normalize(f);
		}
		try {
			f.rewind();
			AudioSystem.write(new AudioInputStream(new InputStream() {
				int m= 0;
				public int available() throws IOException {
					return m < f.capacity() ? f.capacity() - m : 0;
				}

				boolean lo= true;
				public int read() throws IOException {
					float ff= f.get(m);
					if (lo) {
						int i= ((byte) (((int) (ff * Short.MAX_VALUE)) & 255));
						//System.out.print(i);				
						lo= false;
						return BaseMath.intify((byte) i);
					} else {
						int i= ((byte) (((int) (ff * Short.MAX_VALUE) >> 8) & 255));
						//System.out.println(" "+i);				
						lo= true;
						m++;
						return BaseMath.intify((byte) i); 
					}
				}
			}, new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 44100, 16, 2, 4, 44100, false), f.capacity()/2), AudioFileFormat.Type.WAVE, new File(filename));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static public float toFloat(byte[] b) {
		int t= (((int) b[1]) << 8) | ((BaseMath.intify(b[0])));
		float f= t / (float) Short.MAX_VALUE;
		//System.out.println(b[0]+" "+b[1]+" = "+f);
		return f;
	}
	static public float toSignedFloat(byte[] b, int index, int length, boolean bigEndian) {
		float f;
		if(length == 1){
			//8 bit types are unsigned.
			int t = BaseMath.intify(b[index]);
			//not sure if this is right. - 0 is center?
			f = (t - (0xFF / 2)) / (float)(0xFF / 2);
		}else if(length ==2){
			// 16 bit types are signed!
			int t;
			if(bigEndian){
				t = BaseMath.intify(b[index]) << 8 | BaseMath.intify(b[index+1]);
			}else{
				t = BaseMath.intify(b[index+1]) << 8 | BaseMath.intify(b[index]);
			}
			short s = (short)t;
			f = s / 32768f;//(float)Short.MAX_VALUE;
		}else if(length ==3){
			// 24 bit types are signed!
			int t;
			if(bigEndian){
				t = BaseMath.intify(b[index]) << 16 | BaseMath.intify(b[index+1]) << 8 | BaseMath.intify(b[index+2]);
			}else{
				t = BaseMath.intify(b[index+2]) << 16 | BaseMath.intify(b[index+1]) << 8 | BaseMath.intify(b[index]);
			}
			//woah, gotta make a signed 24 bit type.
			t = t << 8;
			t = t / 256;
			f = t / (float)(0xFFFFFF / 2);
		}else if(length ==4){
			// 32 bit types are signed!
			int t;
			if(bigEndian){
				t = BaseMath.intify(b[index]) << 24 | BaseMath.intify(b[index+1]) << 16 | BaseMath.intify(b[index+2]) << 8 | BaseMath.intify(b[index+3]);
			}else{
				t = BaseMath.intify(b[index+3]) << 24 | BaseMath.intify(b[index+2]) << 16 | BaseMath.intify(b[index+1]) << 8 | BaseMath.intify(b[index]);
			}
			f = t / (float)(Integer.MAX_VALUE);
		}else{
			throw new IllegalArgumentException("Unsupported length:"+length);
		}
		return f;
	}

	static public FloatBuffer sinBuffer() {
		return sinBuffer(80240, 200);
	}

	static public FloatBuffer sinBuffer(int length, int cycles) {
		float[] f= new float[length];
		for (int i= 0; i < f.length; i++) {
			f[i]= (float) (Math.sin(Math.PI * 2 * i / (float) f.length * cycles)) * 0.5f;
		}
		return FloatBuffer.wrap(f);
	}

	public static void main(String[] args) {
		// make a FloatBuffer containing raw data
		//floatBuferToFile(args[1], sinBuffer());
		//System.out.println();
		FloatBuffer f= fileToFloatBuffer(args[0]);
		//		for(int i=0;i<f.capacity();i++) System.out.println(f.get());

		// filter buffer
		ButterworthBandpass filter= new ButterworthBandpass(400, 44100, 0.1f);
		for (int i= 0; i < f.capacity(); i++)
			f.put(i, (float) filter.filter(f.get(i)));
		floatBufferToFile(args[1], f);
	}

	/**
	 * @param resultSound
	 * @param buffer
	 * @return
	 */
	public static FloatBuffer addBuffer(FloatBuffer resultSound, FloatBuffer buffer, float mul) {
		if (resultSound == null)
			resultSound= FloatBuffer.allocate(buffer.capacity());
		for (int i= 0; i < buffer.capacity(); i++) {
			resultSound.put(i, resultSound.get(i) + buffer.get(i) * mul);
		}
		return resultSound;
	}

	/**
	 * @param filename
	 * @param startSample
	 * @param endSample
	 */
	public static FloatBuffer fileToFloatBuffer(String filename, int startSample, int endSample) {
		try {
			AudioInputStream a= AudioSystem.getAudioInputStream(new File(filename));
			float[] array= new float[endSample - startSample];
			int at= 0;
			byte[] b2= new byte[2];

			int toSkip= startSample * 2;
			while (toSkip > 0) {
				toSkip -= a.skip(toSkip);
				//				System.out.println("toSkip<"+toSkip+">");
			}

			/*						for (int i= 0; i < startSample; i++)
									{
										a.read(b2);
									}
			*/
			while (a.available() != 0) {
				if (at >= array.length) {
					float[] array2= new float[1 + array.length * 2];
					System.arraycopy(array, 0, array2, 0, array.length);
					array= array2;
				}
				int r= 0;
				while (r < 2)
					r += a.read(b2, r, 2 - r);

				array[at]= toFloat(b2);
				//if ((at>500) && (at<550)) System.out.println("--" + BaseMath.toDP(array[at],5));
				at++;
				if (at > (endSample - startSample))
					break;
			}
			float[] array2= new float[at];
			System.arraycopy(array, 0, array2, 0, array2.length);
			return FloatBuffer.wrap(array2);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static int numSamples(String filename) {
		try {
			AudioInputStream a= AudioSystem.getAudioInputStream(new File(filename));
			return a.available() / 2;
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public interface Saver {
		public void add(float f);
		public void add(FloatBuffer f);
		public void finished();
	}

	static public Saver saveFromFile(final String filename, final boolean normalize) {
		return saveFromFile(filename, normalize, false);
	}
	static public Saver saveFromFile(final String filename, final boolean normalize, final boolean isStereo) {
		return new Saver() {
			float[] backing= new float[2];
			int at= 0;
			public void add(float f) {
				backing[at]= f;
				at++;
				if (at >= backing.length) {
					float[] n= new float[(int) (backing.length * 1.5)];
					System.arraycopy(backing, 0, n, 0, backing.length);
					backing= n;
				}
			}
			public void add(FloatBuffer f) {
				for (int i= 0; i < f.capacity(); i++) {
					add(f.get(i));
				}
			}
			public void finished() {
				float[] fa= new float[at];
				System.arraycopy(backing, 0, fa, 0, at - 1);
				FloatBuffer f= FloatBuffer.wrap(fa);

				System.out.println(" wrapping <" + backing.length + "> <" + at + "> <" + f.capacity() + ">");
				if (isStereo) {
					floatBufferToStereoFile(filename, f, normalize);
				} else {
					floatBufferToFile(filename, f, normalize);

				}
			}
		};
	}

	/**
	 * @param filename
	 * @return
	 */
	public static boolean fileIsMono(String filename) {
		boolean r= false;
		try {
			AudioInputStream a= AudioSystem.getAudioInputStream(new File(filename));
			r= a.getFormat().getChannels() == 1;
			a.close();
		} catch (UnsupportedAudioFileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

}
