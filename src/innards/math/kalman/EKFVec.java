package innards.math.kalman;

import innards.math.linalg.Vec;

/**
    This class represents the minimum functionality of a state, control or observation vector.
    It is expected that the user will add other set and get functions to make this easier to use.

    @author AYB 03/30/00
*/

public class EKFVec
{
    private Vec state = null;
    
    /**
       initializes an EKFVec of specified dimension.
       @param size Number of dimensions
    */
    public EKFVec(int size)
    {
        state = new Vec(size); 
        state.scale(0);
    }
    
    /** 
	Key EKFVec function; returns the state as a Vec.
    */
    public Vec getVec() {return state;}
    
    /**
       Mimics Vec.get()
       @see innards.math.linalg.Vec#get(int)
    */
    public double get(int n) {return state.get(n);}

    /**
       Mimics Vec.set()
       @see innards.math.linalg.Vec#set(int, double)
    */
    public void set(int n, double val) {state.set(n, val);}

    /**
       Mimics Vec.size()
       @see innards.math.linalg.Vec#size()
    */
    public int size() {return state.size();}
}
