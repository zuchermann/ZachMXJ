package innards.math.util;

/**
   A 1-D array of double-precision complex numbers.
*/    
public class ComplexDoubleArray {

    /**
       Constructs a ComplexDoubleArray from an array of doubles, where every two values represents a complex pair.
    */
    public ComplexDoubleArray(double[]rep) { this.rep = rep; }

    /**
        The internal representation: an array of doubles, where every two entries represents a complex pair.
    */
    public double[] rep;

    /**
       Returns the real part of the <code>i</code>th complex number.
    */
    public double real(int i) { return rep[i*2]; }

    /**
       Returns the imaginary part of the <code>i</code>th complex number.
    */
    public double imag(int i) { return rep[i*2+1]; }

    /**
       Returns the magnitude of the <code>i</code>th complex number.
    */
    public double mag(int i) { 
        double real = real(i); double imag = imag(i);
        return Math.sqrt(real*real + imag*imag); 
    }

    
    /**
       Sets the <code>i</code>th complex number to value [<code>re</code>, <code>im</code>].
    */
    public void set(int i, double re, double im) { rep[i*2] = re; rep[i*2+1] = im; }

    /**
       Returns the number of complex numbers in this ComplexArray.
    */
    public int numElements() {
        return rep.length / 2;
    }

    /**
       Copies the stored real values and imaginary values into separate arrays.
    */
    public void toTwoArrays(double[] real, double [] imag) {
        int numElements = numElements();
        for (int i = 0; i < numElements; i++) {
            real[i] = rep[i*2];
            imag[i] = rep[i*2+1];
        }
    }
}

