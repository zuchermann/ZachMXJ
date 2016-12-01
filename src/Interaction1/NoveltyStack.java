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

    private static int getRandomInt(int min, int max){
        Random rand = new Random();
        int n = rand.nextInt(max - min);
        return n + min;
    }

    private void shift(double newVal) {
        for(int i = 1; i < outList.length; i ++) {
            outList[i - 1] = outList[i];
        }
        outList[outList.length - 1] = newVal;
    }

    private boolean contains(double[] list, double val) {
        for(int i = 0; i < list.length; i ++){
            if (list[i] == val) return true;
        }
        return false;
    }

    public void inlet(float value) {
        if(!contains(outList, ((double) value))){
            shift(value);
        }
        int randIndex = getRandomInt(0, outList.length);
        outlet(0, outList[randIndex]);
        outlet(1, outList);
    }

    public void bang() {
        post("reset!");
        this.outList = new double[listLength];
    }
}
