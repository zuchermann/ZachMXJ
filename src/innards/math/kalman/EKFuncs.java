package innards.math.kalman;			

import innards.math.linalg.*;

/** 
    This interface prototypes the functions necessary for the Extended Kalman Filter
    class. It contains declarations for each of the possibly varying matrices, as well
    as accessors to check whether they in fact vary in this instance. Also, the f and h
    functions (see Welch and Bishop, An Introduction to the Kalman Filter) can be stored
    in predict and correct, with isPredictLinear and isCorrectLinear used to state whether
    these function are used.  The constant matrices should be stored here as well. Note
    that for sparse constant matrices, there may be some speed up achieved by using correct and
    predict instead.  
    
    @author AYB 03/30/00.

*/

public interface EKFuncs
{
    public boolean isAconst();
    public boolean isBconst();
    public boolean isHconst();
    public boolean isQconst();
    public boolean isRconst();
    
    public void getA(Vec x,Vec u,Matrix out);
    public void getB(Vec x,Vec u,Matrix out);
    public void getH(Vec x,Matrix out);
    public void getQ(Vec x,Vec u,Matrix out);
    public void getR(Vec x,Matrix out);
    
    public int getStateSize();
    public int getObservationSize();
    public int getControlSize();
    
    public boolean isPredictLinear();
    public boolean isCorrectLinear();
    
    // These functions do not have to allow in-place operation.
    public void predict(Vec x,Vec u,Vec out);
    public void correct(Vec x,Vec out);
}