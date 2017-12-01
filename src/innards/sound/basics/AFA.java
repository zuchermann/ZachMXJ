package innards.sound.basics;

import java.nio.FloatBuffer;

import innards.math.jvec.Jvec;


/**
was:	16-bye Aligned Float Array
now: a wrapper around a floatBuffer
	*/
public class AFA 
{

	public FloatBuffer storage;
	public int offset;
	public int length;
	
	public AFA(int size)
	{
		storage = (new Jvec()).newFloatBuffer(size);
		length = size;
	}
	
	public void updateOffset()
	{
	}
	
	static public void copy(FloatBuffer from, FloatBuffer to)
	{
		to.rewind();
		from.rewind();
		to.put(from);
	}
	static public void copy(FloatBuffer from, int fstart, FloatBuffer to, int tstart, int num)
	{
		for(int i=0;i<num;i++) to.put(tstart+i, from.get(fstart+i));
	}
	
	/**@deprecated*/
	public void verify()
	{

	}
}
