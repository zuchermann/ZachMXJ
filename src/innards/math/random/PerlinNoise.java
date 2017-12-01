package innards.math.random;

import innards.debug.Debug;

import java.io.Serializable;
import java.util.Random;

/**
   An implementation of the Perlin Noise function, taken from his code in
   SIGGRAPH 92, Course 23 Procedural Modeling. Perlin noise is a rough
   approximation to "pink" (band-limited) noise, implemented by a pseudorandom
   tricubic spline. 
   <P>
   Perlin's original code available from:
   <br>
   ftp://ftp.cis.ohio-state.edu/pub/siggraph92/siggraph92_C23.shar
   <P>
   We have adapted this code to work for animations by making multiple noise sources as a function of time, rather than space.

      This -- is an Oscar.
      
   @author Chris Kline and Michael Patrick Johnson
*/

public final class PerlinNoise implements Serializable
{

	/*
	  EXCERPTED FROM SIGGRAPH 92, COURSE 23
	  PROCEDURAL MODELING
	
	  Ken Perlin
	  New York University
	
	  3.6 TURBULENCE AND NOISE
	
	  3.6.2 The noise function
	
	  noise3 is a rough approximation to "pink"  (band-limited)
	  noise,  implemented  by  a  pseudorandom tricubic spline.
	  Given a vector in  3-space,  it returns a  value  between
	  -1.0  and 1.0.  There are two principal tricks to make it
	  run fast:
	
	  - Precompute an array of pseudo-random unit  length  gra-
	  dients g[n].
	
	  - Precompute a permutation  array  p[]  of  the  first  n
	  integers.
	
	  Given the above two arrays,  any  integer  lattice  point
	  (i,j,k)  can be quickly mapped to a pseudorandom gradient
	  vector by:
	
	
	  g[ (p[ (p[i] + j) % n ] + k) % n]
	
	
	  By extending the g[] and p[] arrays, so that  g[n+i]=g[i]
	  and  p[n+i]=p[i], the above lookup can be replaced by the
	  (somewhat faster):
	
	
	  g[ p[ p[i] + j ] + k ]
	
	
	  Now for any point in 3-space,  we just  have  to  do  the
	  following two steps:
	
	  (1) Get the  gradient  for  each  of  its  surrounding  8
	  integer lattice points as above.
	
	  (2) Do a tricubic hermite  spline  interpolation,  giving
	  each lattice point the value 0.0.
	
	  The second step above is just an evaluation of  the  her-
	  mite  derivative basis function 3 * t * t - 2 * t * t * t
	  in each by a dot product of the gradient at the lattice.
	
	*/

	public PerlinNoise(int seed)
	{
		this.seed= seed;
	}

	public PerlinNoise()
	{
		Random r= new Random();
		this.seed= (int) (100000 * r.nextDouble());
	}

	private int seed= 0;
	private final int B= 256;
	private int[] p= new int[B + B + 2];
	private float[][] g= new float[B + B + 2][3];
	private boolean initted= false;

	private final void init()
	{
		Random random= new Random();
		int i, j, k;
		float[] v= new float[3];
		float s;

		// Create an array of random gradient vectors uniformly on the unit sphere

		random.setSeed(seed);
		for (i= 0; i < B; i++)
		{
			do
			{ // Choose uniformly in a cube 
				for (j= 0; j < 3; j++)
					v[j]= (float) ((Math.abs(random.nextLong()) % (B + B)) - B) / B;
				s= v[0] * v[0] + v[1] * v[1] + v[2] * v[2];

			}
			while (s > 1.0); // If not in sphere try again 

			s= (float) Math.sqrt(s);

			// Else normalize
			for (j= 0; j < 3; j++)
				g[i][j]= v[j] / s;
		}

		// Create a pseudorandom permutation of [1..B] 

		for (i= 0; i < B; i++)
			p[i]= i;
		for (i= B; i > 0; i -= 2)
		{
			k= p[i];
			j= (int) (Math.abs(random.nextLong()) % B);
			p[i]= p[j];
			p[j]= k;
		}

		// Extend g and p arrays to allow for faster indexing 

		for (i= 0; i < B + 2; i++)
		{
			p[B + i]= p[i];
			for (j= 0; j < 3; j++)
				g[B + i][j]= g[i][j];
		}

	}

	private static final float at(float rx, float ry, float rz, float q[])
	{
		return rx * q[0] + ry * q[1] + rz * q[2];
	}

	private static final float surve(float t)
	{
		return t * t * (3.0f - 2.0f * t);
	}

	private static final float lerp(float t, float a, float b)
	{
		return a + t * (b - a);
	}

	/** Given a vector in 3-space, it returns a sample noise value between -1.0
	    and 1.0. */
	private final float sample(float[] vec)
	{
		if (!initted)
		{
			init();
			initted= true;
		}

		float t= vec[0] + 10000.0f;
		int bx0= ((int) t) & (B - 1);
		int bx1= (bx0 + 1) & (B - 1);
		float rx0= t - (int) t;
		float rx1= rx0 - 1.0f;

		t= vec[1] + 10000.0f;
		int by0= ((int) t) & (B - 1);
		int by1= (by0 + 1) & (B - 1);
		float ry0= t - (int) t;
		float ry1= ry0 - 1.0f;

		t= vec[2] + 10000.0f;
		int bz0= ((int) t) & (B - 1);
		int bz1= (bz0 + 1) & (B - 1);
		float rz0= t - (int) t;
		float rz1= rz0 - 1.0f;

		int i= p[bx0];
		int j= p[bx1];

		int b00= p[i + by0];
		int b10= p[j + by0];
		int b01= p[i + by1];
		int b11= p[j + by1];

		float sx= surve(rx0);
		float sy= surve(ry0);
		float sz= surve(rz0);

		float[] q= g[b00 + bz0];
		float u= at(rx0, ry0, rz0, q);
		q= g[b10 + bz0];
		float v= at(rx1, ry0, rz0, q);
		float a= lerp(sx, u, v);

		q= g[b01 + bz0];
		u= at(rx0, ry1, rz0, q);
		q= g[b11 + bz0];
		v= at(rx1, ry1, rz0, q);
		float b= lerp(sx, u, v);

		// interpolate in y at lo x
		float c= lerp(sy, a, b);

		q= g[b00 + bz1];
		u= at(rx0, ry0, rz1, q);
		q= g[b10 + bz1];
		v= at(rx1, ry0, rz1, q);
		a= lerp(sx, u, v);

		q= g[b01 + bz1];
		u= at(rx0, ry1, rz1, q);
		q= g[b11 + bz1];
		v= at(rx1, ry1, rz1, q);
		b= lerp(sx, u, v);

		// interpolate in y at hi x
		float d= lerp(sy, a, b);

		// interpolate in z
		return 1.5f * lerp(sz, c, d);
	}

	private float[] time= new float[3];
	/** reuturn the ith independent noise function at time t.  only works for i = 0,1,2 now. */
	public float noise(int i, float t)
	{
		Debug.doAssert(i >= 0 && i < 3, "PerlinNoise.noise(int, float): nosie index must be 0,1,2.");
		time[0]= 0.0f;
		time[1]= 0.0f;
		time[2]= 0.0f;
		time[i]= t;
		return sample(time);
	}

}
