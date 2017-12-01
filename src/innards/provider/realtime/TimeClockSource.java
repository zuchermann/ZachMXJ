package innards.provider.realtime;



public class TimeClockSource
    implements ClockSource
{

    long base;
    
    public TimeClockSource()
    {
        base = System.currentTimeMillis();
    }

    /** returns time in units
    */
    public double getTime()
    {
        long l = System.currentTimeMillis()-base;
        return l/1000.0;
    }
}