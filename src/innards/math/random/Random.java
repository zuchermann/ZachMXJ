package innards.math.random;

/** Numerical Recipies' Uniform Generator #2 ("recommended" -- good
  balance of speed and accuracy).

  Implemented because the random number generator in the N32 JVM seems
  to be broken. When it is fixed, toss this and replace with
  java.util.Random.

  Note that this is not a public class since this is just a temporary hack. */

class Random
{
	/** All the #defines */
	long IM1= 2147483563;
	long IM2= 2147483399;
	double AM= 1.0 / (double) IM1;
	long IMM1= IM1 - 1;
	long IA1= 40014;
	long IA2= 40692;
	long IQ1= 53668;
	long IQ2= 52774;
	long IR1= 12211;
	long IR2= 3791;
	int NTAB= 32;
	long NDIV= 1 + IMM1 / NTAB;
	double EPS= 1.2e-7;
	double RNMX= 1.0 - EPS;

	/** Internal state */
	boolean isReset;
	long seed;
	long idum, idum2;
	long iy;
	long[] iv;

	// It seems that with too large a seed, this random number generator fails.
	// Clamp the seed to be within a reasonable value.
	public Random()
	{
		isReset= true;
		seed= System.currentTimeMillis() % IA1;
		//    seed = System.currentTimeMillis();
		//    System.out.println("Seed was " + seed);
		iv= new long[NTAB];
	}

	public void setSeed(long newSeed)
	{
		seed= newSeed % IA1;
		//    seed = newSeed;
		//    System.out.println("New seed was " + seed);
		isReset= true;
	}

	public double nextDouble()
	{
		if (isReset)
		{
			idum= (seed < 0) ? seed : -seed;
			idum2= 123456789;
			iy= 0;
			isReset= false;
		}

		int j;
		long k;
		double temp;

		if (idum <= 0)
		{
			if (-idum < 1)
				idum= 1;
			else
				idum= -idum;
			idum2= idum;
			for (j= NTAB + 7; j >= 0; j--)
			{
				k= idum / IQ1;
				idum= (IA1 * (idum - k * IQ1) - IR1 * k);
				if (idum < 0)
				{
					idum += IM1;
				}
				if (j < NTAB)
					iv[j]= idum;
			}
			iy= iv[0];
		}
		k= idum / IQ1;
		//    System.out.println("1: idum was " + idum);
		idum= IA1 * (idum - k * IQ1) - k * IR1;
		if (idum < 0)
			idum += IM1;
		//    System.out.println("2: idum was " + idum);
		k= idum2 / IQ2;
		//    System.out.println("1: idum2 was " + idum2);
		idum2= IA2 * (idum2 - k * IQ2) - k * IR2;
		if (idum2 < 0)
			idum2 += IM2;
		//    System.out.println("2: idum2 was " + idum2);
		//    System.out.println("iy was " + iy);
		//    System.out.println("NDIV was " + NDIV);
		j= (int) (iy / NDIV);
		//    System.out.println("j was " + j);
		iy= iv[j] - idum2;
		iv[j]= idum;
		if (iy < 1)
			iy += IMM1;
		if ((temp= AM * iy) > RNMX)
			//      {
			//	System.out.println("innards.math.random.Random.nextDouble() returning "
			//			   + RNMX);
			return RNMX;
		//      }
		else
			//      {
			//	System.out.println("innards.math.random.Random.nextDouble() returning "
			//			   + temp);
			return temp;
		//      }
	}

	public static void main(String[] argv)
	{
		Random rand= new Random();
		rand.setSeed(8887);
		for (int i= 0; i < 10; i++)
			System.out.println("Random number: " + rand.nextDouble());
	}
}
