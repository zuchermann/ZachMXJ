package innards.util;

import innards.NamedObject;
/**
A class that can be used to prevent something from dithering.  It allows you to set an upper and lower bound,
and will set itself to be true and return true if it's already true and above the lower bound, 
or if it's false and above the upper bound.
Otherwise, it sets itself to be false and returns false.

If you make it inverted, it will return true when it falls below the lower bound, and stay true until it 
goes above the upper bound.

@author badger, dlyons
*/

public class AntiDither extends NamedObject
{
    public static final boolean NORMAL = false;
    public static final boolean INVERTED = true;
    public float lowerBound;
    public float upperBound;
    public boolean onOrOff;
    public boolean inverted = false;
    public AntiDither(String name, float lower, float upper, boolean initOnOrOff)
    {
        super(name);
        this.lowerBound = lower;
        this.upperBound = upper;
        onOrOff = initOnOrOff;
    }
    public AntiDither(String name, float lower, float upper, boolean initOnOrOff, boolean inverted)
    {
        this(name, lower, upper, initOnOrOff);
        this.inverted = inverted;
    }
    public boolean isOn(float value)
    {
        if (!inverted)
        {
            if (onOrOff && value >lowerBound) onOrOff = true;
            else if (value > upperBound) onOrOff = true;
            else onOrOff = false;
            //System.out.println("AntiDither: "+this.getName() +" returning "+onOrOff+" lowerBound = "+lowerBound+" upperBound = "+upperBound+" value = "+value);
            return onOrOff;
        }
        else
        {
            if (onOrOff && value <upperBound) onOrOff = true;
            else if (value < lowerBound) onOrOff = true;
            else onOrOff = false;
            //System.out.println("InvertedAntiDither: "+this.getName() +" returning "+onOrOff+" lowerBound = "+lowerBound+" upperBound = "+upperBound+" value = "+value);
            return onOrOff;
        }
    }
    
    public void setBounds(float l, float u)
    {
        //System.out.println(this.getName()+" setting bounds to be "+ l+" and "+u);
        lowerBound = l;
        upperBound = u;
    }
    
    public void resetState(boolean newState) {
        onOrOff = newState;
    }
    
    public static void main(String[] args)
    {
        //AntiDither unDitheredSin = new AntiDither("sinDitherer", 0.5, 0.7, true);
        AntiDither unDitheredSin = new AntiDither("sinDitherer", 0.5f, 0.7f, true, AntiDither.INVERTED);
        float number = 0;
        while (true)
        {
            float mySin = (float)Math.sin(number);
            System.out.println("Number = "+mySin+" and therefore it's "+ unDitheredSin.isOn(mySin));
            number += 0.05;
        }
   }
}