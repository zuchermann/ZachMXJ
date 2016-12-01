package Interaction1;

import com.cycling74.max.*;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by yn on 12/1/16.
 */
public class NoveltyStack extends MaxObject{

    private int listLength;
    private double[] outList;

    public NoveltyStack() {
        this(7);
    }

    public NoveltyStack(int listLength) {
        this.listLength = listLength;
        this.outList = new double[listLength];
    }

    private static int getRandomInt(int max){
        Random rand = new Random();
        int n = rand.nextInt(50);
        return n;
    }

    private void shift(double newVal) {
        for(int i = 1; i < outList.length; i ++) {
            outList[i - 1] = outList[i];
        }
        outList[outList.length - 1] = newVal;
    }

    public void inlet(float value) {
        if(Arrays.asList(outList).contains(value)){
            shift(value);
        }
        int randIndex = getRandomInt(outList.length);
        outlet(0, outList[randIndex]);
    }

    public void bang() {
        post("reset!");
        this.outList = new double[listLength];
    }
}
