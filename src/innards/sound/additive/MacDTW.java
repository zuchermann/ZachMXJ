package innards.sound.additive;

import innards.iLaunchable;
import innards.math.jvec.Jvec;



public class MacDTW implements iLaunchable
{
	static
	{
		new Jvec();
	}

	// this is the basic dtw call
	// inputVector1 and inputVector2 are two dimensional arrays flattened to one-dimensional arrays with the 
	// ordering something like (x0,y0,z0,x1,y1,z1 ... xN,yN,zN) for something like Vec3 say
	// in this case, dimension would be three
	public float dtw(float[] inputVector1, float[] inputVector2, int dimension,short[] outputPath1, short[] outputPath2)
	{
		/*
		String in1 = new String();
		
		for (int i = 0; i < inputVector1.length; i++)
		{	
			in1 += "<" + inputVector1[i] + ">";
		}
		
		String in2 = new String();
		for (int i = 0; i < inputVector2.length; i++)
		{	
			in2 += "<" + inputVector2[i] + ">";
		}
		System.out.println("macDTW given inputVector 1 <" + in1 + "> \n and inputVector 2 <" + in2 + ">");
		*/
		float r = nativeDTW(inputVector1, inputVector1.length/dimension, inputVector2, inputVector2.length/dimension, dimension, outputPath1, outputPath2);
		return r;
	}
	
	native public float nativeDTW(float[] inputVector1, int len1, float[] inputVector2, int len2, int dimension, short[] outputPath1, short[] outputPath2);

	/**
	 *
	 */

	public void launch() {
		// test this thing
		
		MacDTW dtw = new MacDTW();
		
		float[] d1 = {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f};
		float[] d2 = {1.0f,2.0f,3.0f,4.0f, 4.0f,4.0f, 4.0f, 5.0f,6.0f};
				
		short[] out1 = new short[d1.length];
		short[] out2 = new short[d2.length];
		
		System.out.println(" distance is <"+dtw.dtw(d1, d2, 1, out1, out2)+">   "+toString(out1)+" "+toString(out2));
	}
	
	static public String toString(short[] a)
	{
		String r = "[";
		for(int i=0;i<a.length;i++) r+=a[i]+" ";
		r+="]";
		return r;
	}

}
