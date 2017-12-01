package innards.math.util;

/**
   A 1-D array of complex numbers. Implemented with shorts for maximum speed when using the Intel FFT library.
*/
public class ComplexArray {

    /**
       Constructs the ComplexArray from an array of shorts, where every two entries represents a complex pair.
    */
    public ComplexArray(short[]rep) { this.rep = rep; }
    
    /**
       The internal representation: an array of shorts, where every two entries represents a complex pair.
    */
    public short[] rep;

    /**
       Returns the real part of the <code>i</code>th complex number.
    */
    public short real(int i) { return rep[i*2]; }

    /**
       Returns the imaginary part of the <code>i</code>th complex number.
    */
    public short imag(int i) { return rep[i*2+1]; }

    /**
       Returns the magnitude of the <code>i</code>th complex number.
    */
    public int mag(int i) { 
        int real = real(i); int imag = imag(i);
        return (int)Math.sqrt(real*real + imag*imag); 
    }

    /**
       Sets the <code>i</code>th complex number to value [<code>re</code>, <code>im</code>].
    */
    public void set(int i, short re, short im) { rep[i*2] = re; rep[i*2+1] = im; }

    /**
       Returns the number of complex numbers in this ComplexArray.
    */
    public int numElements() {
        return rep.length / 2;
    }

    /**
       Copies the stored real values and imaginary values into separate arrays.
    */
    public void toTwoArrays(short[] real, short [] imag) {
        int numElements = numElements();
        for (int i = 0; i < numElements; i++) {
            real[i] = rep[i*2];
            imag[i] = rep[i*2+1];
        }
    }
}

