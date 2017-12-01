package innards.util;

public class Conversions
{
    public static float[] doubleToFloat(double[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (float)in[i];
        }
        return out;
    }
    public static float[] intToFloat(int[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (float)in[i];
        }
        return out;
    }
    public static float[] shortToFloat(short[] in) {
        float[] out = new float[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (float)in[i];
        }
        return out;
    }
    public static short[] floatToShort(float[] in) {
        short[] out = new short[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (short)in[i];
        }
        return out;
    }
    public static double[] shortToDouble(short[] in) {
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (double)in[i];
        }
        return out;
    }
    public static short[] doubleToShort(double[] in) {
        short[] out = new short[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (short)in[i];
        }
        return out;
    }
    public static double[] floatToDouble(float[] in) {
        double[] out = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            out[i] = (double)in[i];
        }
        return out;
    }

    public static byte[] floatToByte(float[]in) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength * 2;

        byte[] out = new byte[outLength];

        for (int i = 0; i < inLength; i++) {
            float inDouble = in[i];
            int inInt = (int)inDouble;
            out[i*2 + 1] = (byte)(inInt & 255);
            out[i*2]  = (byte)((inInt >> 8) & 255);

        }
        return out;
    }


// BYTE TO DOUBLE

    public static double[] byteBigEndianToDouble(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        double[] out = new double[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i]; inByte2 = in[i+1];
            out[i/2] = (double)( inByte2 + (((int)inByte1 << 8))) ;
        }

        return out;

    }
    public static double[] byteLittleEndianToDouble(byte[]in, int amountToConvert) {
        return byteToDouble(in, amountToConvert);
    }
    public static double[] byteToDouble(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        double[] out = new double[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i+1]; inByte2 = in[i];
            out[i/2] = (double)( inByte2 + (((int)inByte1 << 8))) ;
        }

        return out;

    }


    public static double[] byteToDouble(byte[]in) {
        return byteToDouble(in, in.length);
    }

    public static byte[] doubleToByte(double[]in) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength * 2;

        byte[] out = new byte[outLength];

        for (int i = 0; i < inLength; i++) {
            double inDouble = in[i];
            out[i*2 + 1] = (byte)((int)inDouble & 255);

            out[i*2]  = (byte)(((int)inDouble >> 8) & 255);
        }
        return out;
    }


// BYTE TO INT

    public static int[] byteBigEndianToInt(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        int[] out = new int[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i]; inByte2 = in[i+1];
            out[i/2] = ( inByte2 + (((int)inByte1 << 8))) ;
        }

        return out;

    }
    public static int[] byteLittleEndianToInt(byte[]in, int amountToConvert) {
        return byteToInt(in, amountToConvert);
    }
    public static int[] byteToInt(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        int[] out = new int[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i+1]; inByte2 = in[i];
            out[i/2] = ( inByte2 + (((int)inByte1 << 8))) ;
        }

        return out;

    }


    public static int[] byteToInt(byte[]in) {
        return byteToInt(in, in.length);
    }

    public static byte[] intToByte(int[]in) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength * 2;

        byte[] out = new byte[outLength];

        int inInt;
        for (int i = 0; i < inLength; i++) {
            inInt = in[i];
            out[i*2 + 1] = (byte)((int)inInt & 255);

            out[i*2]  = (byte)(((int)inInt >> 8) & 255);
        }
        return out;
    }


///////////////////////
// BYTE TO SHORT

    public static short[] byteBigEndianToShort(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        short[] out = new short[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i]; inByte2 = in[i+1];
            out[i/2] = (short)((int) innards.math.BaseMath.intify(inByte2) + ((int)innards.math.BaseMath.intify(inByte1) << 8) );
//            out[i/2] = (short)((short) inByte2 + (short)((short)inByte1 << (short)8) );
        }

        return out;

    }
    public static short[] byteLittleEndianToShort(byte[]in, int amountToConvert) {
        return byteToShort(in, amountToConvert);
    }
    public static short[] byteToShort(byte[]in, int amountToConvert) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength / 2;

        short[] out = new short[outLength];

        // endiannessness
        byte inByte1, inByte2;
        for (int i = 0; i < amountToConvert; i+=2) {
            inByte1 = in[i+1]; inByte2 = in[i];
            out[i/2] = (short)((int) innards.math.BaseMath.intify(inByte2) + ((int)innards.math.BaseMath.intify(inByte1) << 8) );
            //out[i/2] = (short)((int) (inByte2) + ((int)(inByte1) << 8) );
        }

        return out;

    }


    public static short[] byteToShort(byte[]in) {
        return byteToShort(in, in.length);
    }

    public static byte[] shortToByte(short[]in) {
        if (in == null) return null;
        int inLength = in.length;
        int outLength = inLength * 2;

        byte[] out = new byte[outLength];

        short inShort;
        for (int i = 0; i < inLength; i++) {
            inShort = in[i];
            out[i*2 + 1] = (byte)((int)inShort & 255);

            out[i*2]  = (byte)(((int)inShort >> 8) & 255);
        }
        return out;
    }

///////////////////////
}