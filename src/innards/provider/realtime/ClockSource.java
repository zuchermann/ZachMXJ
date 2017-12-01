package innards.provider.realtime;



public interface ClockSource
{

    /** returns time in units
    */
    public double getTime();
}