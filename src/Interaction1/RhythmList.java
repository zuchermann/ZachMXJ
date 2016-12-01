package Interaction1;

import com.cycling74.max.*;

/**
 * Created by yn on 12/1/16.
 */
public class RhythmList extends MaxObject{
    int listLength;
    int div;
    double[] propList;
    double[] rhythmList;
    static final double BIG_BOY = 9999;
    double prev;

    public  RhythmList() {
        this(4);
    }

    public RhythmList(int listLength) {
        this.listLength = listLength;
        this.div = 4;
        this.propList = new double[this.listLength * this.listLength];
        this.rhythmList = new double[this.listLength];
        this.prev = BIG_BOY;

        for(int i = 0; i < listLength; i++){
            rhythmList[i] = 0.0;
        }

        for(int i = 0; i < div; i++) {
            for(int j = 0; j < div; j++){
                propList[(i*div) + j] = ((double) (i+1)/ (double) (j+1));
            }
        }

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL });
        declareOutlets(new int[]{ DataTypes.ALL});

        setInletAssist(new String[] {
                "float input from timer",
        });
        setOutletAssist(new String[] { "output rhythm list"});
    }

    private void shift(double newVal) {
        for(int i = 1; i < rhythmList.length; i ++) {
            rhythmList[i - 1] = rhythmList[i];
        }
        rhythmList[rhythmList.length - 1] = newVal;
    }

    public void inlet(float value){

        double closestMatch = BIG_BOY;
        double matchDiff = BIG_BOY;
        if(prev != BIG_BOY){
            double prop = value / prev;
            for(int i = 0; i < propList.length; i++){
                double testProp = propList[i];
                double newDiff = Math.abs(prop - testProp);
                if(newDiff < matchDiff){
                    matchDiff = newDiff;
                    closestMatch = testProp;
                }
            }
            if(closestMatch != BIG_BOY) {
                shift(closestMatch);
                outlet(0, rhythmList);
            }
        }
        prev = value;
    }

    public void bang() {
        post("reset!");
        this.propList = new double[this.listLength * this.listLength];
        this.rhythmList = new double[this.listLength];
        this.prev = BIG_BOY;

        for(int i = 0; i < listLength; i++){
            rhythmList[i] = 0.0;
        }

        for(int i = 0; i < div; i++) {
            for(int j = 0; j < div; j++){
                propList[(i*div) + j] = ((double) (i+1)/ (double) (j+1));
            }
        }
    }

}
