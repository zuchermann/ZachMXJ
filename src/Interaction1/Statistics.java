package Interaction1;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

/**
 * Stolen from
 * http://stackoverflow.com/questions/7988486/how-do-you-calculate-the-variance-median-and-standard-deviation-in-c-or-java
 */
public class Statistics {
    Stack<double[]> data;
    int size;

    public Statistics(Stack<double[]> data) {
        this.data = data;
        size = data.size();
    }

    double[] getMean() {
        double[] result = new double[2];
        for(int i = 0; i < result.length; i++) {
            double sum = 0.0;
            for (double a : data.get(i))
                sum += a;
            result[i] = sum / (double) size;
        }
        return result;
    }

    double[] getVariance() {
        double[] result = new double[2];
        for(int i = 0; i < result.length; i++) {
            double mean = getMean()[i];
            double temp = 0;
            for (double a : data.get(i))
                temp += (a - mean) * (a - mean);
            result[i] = temp / size;
        }
        return result;
    }

    double[] getStdDev() {
        double[] result = new double[2];
        for(int i = 0; i < result.length; i++) {
            result[i] = Math.sqrt(getVariance()[i]);
        }
        return result;
    }

    public double[] median() {
        double[] result = new double[2];
        for(int i = 0; i < result.length; i++) {
            Arrays.sort(data.get(i));

            if (data.get(i).length % 2 == 0) {
                result[i] = (data.get(i)[(data.get(i).length / 2) - 1] + data.get(i)[data.get(i).length / 2]) / 2.0;
            }
            result[i] = data.get(i)[data.get(i).length / 2];
        }
        return result;
    }
}
