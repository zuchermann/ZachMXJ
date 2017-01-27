package Interaction1;

import com.cycling74.max.*;

/**
 * Created by Hanoi on 12/7/16.
 */
public class Doubler extends MaxObject{
    double minVal;
    double MIN = 20;

    public Doubler(){
        this(500);
    }

    public Doubler(double minVal){
        this.minVal = minVal;

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL });
        declareOutlets(new int[]{ DataTypes.ALL });
    }

    public void inlet(float val){
        if(val > MIN) {
            double result = val;
            while (result < minVal) {
                result = result * 2.;
            }
            outlet(0, result);
        }
    }

    public void setLatency(double minVal){
        this.minVal = minVal;
        post("doubler set to " + minVal);
    }
}
