package innards.math.util;

import innards.util.Conversions;

/**
   Java interface to the Intel FFT libraries. Has specialized accelerated FFT calls for "forward" FFTs of real signals. Consult the Intel documentation for the scaling convention used.
*/
public class Fft {

    /**
       Constructor.
    */
    public Fft(){}

    /**
       Converts the Intel FFT flag <code>order</code> to the number of samples.
    */
    public static int orderToLength(int order) { return 1 << order; }

    /**
       Converts the number of samples to an Intel FFT <code>order</code> flag.
    */
    public static int lengthToOrder(int length) {
        int order = 0;
        while (length > 1) { length = length >> 1; order++; }
        return order;
    }

    /**
       Performs a forward FFT on a real double-precision signal. 
       @param in The signal
       @param order The base-2 logarithm of the number of samples in the signal.
    */
    private static native void nspdRealFftForward(double[]in, int order);

    /** 
	Performs a forward FFT on a real short-int-precision signal.
       @param in The signal
       @param order The base-2 logarithm of the number of samples in the signal.
    */
    private static native void nspwRealFftForward(short[]in, int order);

    /**
       Performs an inverse FFT on a double-precision signal.
       @param in The signal
       @param order The base-2 logarithm of the number of samples in the signal.
    */
    private static native void nspdCcsFftInv(double[]in, int order);

    /** 
	Performs an inverse FFT on a short-int-precision signal.
       @param in The signal
       @param order The base-2 logarithm of the number of samples in the signal.
    */
    private static native void nspwCcsFftInv(short[]in, int order);

    /** 
	Performs an inverse FFT on a complex double-precision signal.
	@param input The signal
    */
    public static double[] nspdCcsFftInv(ComplexDoubleArray input) {
        double[] rep = input.rep;
        double[] toIntel = new double[rep.length];
        System.arraycopy(rep, 0, toIntel, 0, toIntel.length);
        nspdCcsFftInv(toIntel, lengthToOrder(rep.length -2));
        double [] result = new double[rep.length-2];
        System.arraycopy(toIntel, 0, result, 0, rep.length-2);
        return result;
    }

    /**
       Perfoms an inverse FFT on a complex short-int-precision signal.
	@param input The signal
    */
    public static short[] nspwCcsFftInv(ComplexArray input) {
        short[] rep = input.rep;
        short[] toIntel = new short[rep.length];
        System.arraycopy(rep, 0, toIntel, 0, toIntel.length);
        nspwCcsFftInv(toIntel, lengthToOrder(rep.length -2));
        short [] result = new short[rep.length-2];
        System.arraycopy(toIntel, 0, result, 0, rep.length-2);
        return result;
    }

    /**
       Performs a forward FFT on a real signal.
    */
    public static ComplexDoubleArray nspdRealFftForward(double[] realIn) {
        // doesn't mutate the input
        // needs an array a bit longer for the output.
        double[] stuff = new double[realIn.length + 2];
        System.arraycopy(realIn, 0, stuff, 0, realIn.length);
        nspdRealFftForward(stuff, lengthToOrder(realIn.length));
        return new ComplexDoubleArray(stuff);
    }

    /**
       Performs a forward FFT on a real signal.
    */
    public static ComplexArray nspwRealFftForward(short[] realIn) {
        // doesn't mutate the input
        // needs an array a bit longer for the output.
        short[] stuff = new short[realIn.length + 2];
        System.arraycopy(realIn, 0, stuff, 0, realIn.length);
        nspwRealFftForward(stuff, lengthToOrder(realIn.length));
        return new ComplexArray(stuff);
    }


    static {
        System.loadLibrary("splDLL");
    }

    /**
       Applies a band-pass filter to a 1-D short-int-precision signal.
    */
    public static short[] bandPassAll(short[]sound, int windowSize, int sampleRate, int lowFreq, int highFreq) {
        int halfWindowSize = windowSize / 2;
        int quarterWindowSize = windowSize / 4;
        short[] paddedSound = new short[sound.length + windowSize];
        short[]result = new short[sound.length+halfWindowSize];

        int resultPointer = 0;
        int paddedSoundPointer = 0;
        for (int i = 0; i < sound.length; i++) paddedSound[i+halfWindowSize] = sound[i];

        short[] temp = new short[windowSize];

        while (paddedSoundPointer < paddedSound.length - halfWindowSize) {
            System.arraycopy(paddedSound, paddedSoundPointer, temp, 0, windowSize);

            FftData thisData = new FftData(sampleRate, temp);
            thisData.highPass(lowFreq);
            thisData.lowPass(highFreq);
            temp = thisData.fourierToSamples();

            System.arraycopy(temp, quarterWindowSize, result, resultPointer, halfWindowSize);
            resultPointer += halfWindowSize;
            paddedSoundPointer += halfWindowSize;
        }
        short[] toReturn = new short[sound.length];
        System.arraycopy(result, quarterWindowSize, toReturn, 0, sound.length - halfWindowSize);
        return toReturn;
    }



    /**
       Class for storing the results of a double-precision FFT operation performed by innards.math.util.Fft.
    */
    public static class FftData {

	/**
	   For FFTs of short-int-precision signals. Calculates and stores the FFT of <code>source</code>.
	 */
        public FftData(int sampleRate, short[] source) {
            this(sampleRate, source, Fft.nspdRealFftForward(Conversions.shortToDouble(source)));
        }

	/**
	   For FFTs of short-int-precision signals.
	   @param sampleRate The sampling rate of the signal.
	   @param source The signal.
	   @param fourier The FFT of <code>source</code>
	*/
        public FftData(int sampleRate, short [] source, ComplexDoubleArray fourier) {
            this.sampleRate = sampleRate;
            this.source = Conversions.shortToDouble(source);
            this.fourier = fourier;
            numElements = fourier.numElements();
//            System.out.println("numElements " + numElements);
//            System.out.println("1 " + indexToHertz(1) + " 2 " + indexToHertz(2));
            sqrtNumElements = Math.sqrt((double)numElements);
        }

	/**
	   For FFTs of double-precision signals. Calculates and stores the FFT of <code>source</code>.
	*/
        public FftData(int sampleRate, double[] source) {
            this(sampleRate, source, Fft.nspdRealFftForward(source));
        }

	/**
	   For FFTs of double-precision signals.
	   @param sampleRate The sampling rate of the signal.
	   @param source The signal.
	   @param fourier The FFT of <code>source</code>
	*/
        public FftData(int sampleRate, double [] source, ComplexDoubleArray fourier) {
            this.sampleRate = sampleRate;
            this.source = source;
            this.fourier = fourier;
            numElements = fourier.numElements();
//            System.out.println("numElements " + numElements);
//            System.out.println("1 " + indexToHertz(1) + " 2 " + indexToHertz(2));
            sqrtNumElements = Math.sqrt((double)numElements);
        }





        private double energy = -1;
        private int numElements;
        private int sampleRate;
        private double sqrtNumElements;

	/**
	   Internal representation of the source signal.
	*/        
	public double[] source;


	/**
	   Internal representation of the FFT of <code>source</code>.
	*/
        public ComplexDoubleArray fourier;

	/**
	   Converts a frequency to an array index for a FFT data array.
	*/
        public int hertzToIndex (int freq) {
            return (int) numElements * freq / sampleRate; //before 2*numElements
        }

	/**
	   Converts an FFT data array index to its corresponding frequency.
	*/
        public double indexToHertz (int index) {
            return  (double) index * sampleRate / (2*numElements); //changed back to /2*numElements
        }

	/**
	   Calculates the energy of a single frequency, specified by fourier signal array index.
	   @see #hertzToIndex(int)
	*/
        public double getEnergy(int i) {
            return (double)fourier.mag(i) / sqrtNumElements;
        }

	/**
	   Calculates the average energy of the stored signal.
	*/
        public double avgEnergy() {
            if (energy == -1) { energy = avgEnergyInRange(0, numElements); }
            return energy;
        }

	/**
	   Calculates the average energy of a range of frequencies.
	 */
        public double avgEnergyInRange(int startFreq, int endFreq) {
            int startElement = hertzToIndex(startFreq);
            int endElement = hertzToIndex(endFreq);
            int totalEnergy = 0;
            for (int i = startElement; i < endElement; i++) {
                totalEnergy += fourier.mag(i);
            }
            double result = (double)totalEnergy / (double)(endElement - startElement);
            return (double)result / (double)sqrtNumElements;

        }

        /**
	   Highpass-filters the stored signal.
	*/
        public void highPass(int cutOff) {
            int indexToStopAt = hertzToIndex(cutOff);
            for (int i = 0; i < indexToStopAt; i++) {
                fourier.set(i, (short)0, (short)0);
            }
        }

	/**
	   Lowpass-filters the stored signal.
	*/
        public void lowPass(int cutOff) {
            int indexToStopAt = hertzToIndex(cutOff);
            for (int i = fourier.numElements()-1; i > indexToStopAt; i--) {
                fourier.set(i, (short)0, (short)0);
            }
        }

	/**
	   Inverse-transforms the stored FFT back into samples.
	*/
        public short[] fourierToSamples() {
            source = nspdCcsFftInv(fourier);
            return Conversions.doubleToShort(source);
        }

	/**
	   Returns the stored signal in short-int-precision.
	*/
        public short[] getSource() {
            return Conversions.doubleToShort(source);
        }

	/**
	   Returns the stored signal.
	*/
        public double[] getDoubleSource() {
            return source;
        }

    }


    //// END FFTDATA


    // THE FOLLOWING IS GOOD FOR DISPLAYS BUT NOT FOR ANALYSIS

    /**
       Class for storing the results of a short-int-precision FFT operation performed by innards.math.util.Fft.
    */
    public static class ShortFftData {

	/**
	   Constructor. Calculates and stores the FFT of <code>source</code>.
	*/
        public ShortFftData(int sampleRate, short[] source) {
            this(sampleRate, source, Fft.nspwRealFftForward(source));
        }

	/**
	   Constructor.
	*/
        public ShortFftData(int sampleRate, short [] source, ComplexArray fourier) {
            this.sampleRate = sampleRate;
            this.source = source;
            this.fourier = fourier;
            numElements = fourier.numElements();
//            System.out.println("numElements " + numElements);
//            System.out.println("1 " + indexToHertz(1) + " 2 " + indexToHertz(2));
            sqrtNumElements = Math.sqrt((double)numElements);
        }
        private double energy = -1;
        private int numElements;
        private int sampleRate;
        private double sqrtNumElements;

	/**
	   Internal representation of the source signal.
	*/
        public short[] source;

	/**
	   Internal representation of the FFT of <code>source</code>.
	*/
        public ComplexArray fourier;

	/**
	   Converts a frequency to an array index for a FFT data array.
	*/
        public int hertzToIndex (int freq) {
            return (int) 2* numElements * freq / sampleRate;
        }

	/**
	   Converts an FFT data array index to its corresponding frequency.
	*/
        public double indexToHertz (int index) {
            return  (double) index * sampleRate / (2*numElements);
        }

	/**
	   Calculates the energy of a single frequency, specified by fourier signal array index.
	   @see #hertzToIndex(int)
	*/
        public double getEnergy(int i) {
            return (double)fourier.mag(i) / sqrtNumElements;
        }

	/**
	   Calculates the average energy of the stored signal.
	*/
        public double avgEnergy() {
            if (energy == -1) { energy = avgEnergyInRange(0, numElements); }
            return energy;
        }

	/**
	   Calculates the average energy of a range of frequencies.
	 */
        public double avgEnergyInRange(int startFreq, int endFreq) {
            int startElement = hertzToIndex(startFreq);
            int endElement = hertzToIndex(endFreq);
            int totalEnergy = 0;
            for (int i = startElement; i < endElement; i++) {
                totalEnergy += fourier.mag(i);
            }
            double result = (double)totalEnergy / (double)(endElement - startElement);
            return (double)result / (double)sqrtNumElements;

        }

        /**
	   Highpass-filters the stored signal.
	*/
        public void highPass(int cutOff) {
            int indexToStopAt = hertzToIndex(cutOff);
            for (int i = 0; i < indexToStopAt; i++) {
                fourier.set(i, (short)0, (short)0);
            }
        }

	/**
	   Lowpass-filters the stored signal.
	*/
        public void lowPass(int cutOff) {
            int indexToStopAt = hertzToIndex(cutOff);
            for (int i = fourier.numElements()-1; i > indexToStopAt; i--) {
                fourier.set(i, (short)0, (short)0);
            }
        }

	/**
	   Inverse-transforms the stored FFT back into samples.
	*/
        public short[] fourierToSamples() {
            source = nspwCcsFftInv(fourier);
            return source;
        }
    }


        // END SHORTFFTDATA
}

