package Interaction1;


/**
 * Created by yn on 11/20/16.
 */
import Interaction1.NGram;
import com.cycling74.max.*;

import java.util.*;

public class Simple extends MaxObject{
    NGram ngram;
    int ngramSize;
    double lastPrediction;
    Stack<Double> valueQueue;
    Stack<Double> accuracyQueueQueue;
    Stack<Double> accuracyQueue;

    public Simple() {
        this(2);
    }

    public Simple(int ngramSize) {
        this.ngramSize = ngramSize;
        this.ngram = new NGram();
        this.valueQueue = new Stack<Double>();
        this.accuracyQueue = new Stack<Double>();
        this.accuracyQueueQueue = new Stack<Double>();
        this.lastPrediction = 0;

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[] {
                "values to predict",
        });
        setOutletAssist(new String[] { "value with max prob in largest n-gram",
                "probabilistic select value in largest n-gram",
                "change in accuracy"
        });
    }

    private void insert (double val) {
        this.valueQueue.add(val);
        while(valueQueue.size() > ngramSize) {
            valueQueue.remove(0);
        }
        if(valueQueue.size() == ngramSize) {
            ngram.insert(valueQueue);
        }
        //outlet(0, valueQueue.toString());
    }

    private double getProbabilistic(HashMap<Double, Double> val) {
        Random r = new Random();
        double randomValue = r.nextDouble();
        double minDist = 1;
        double prediction = 0;
        Set<Double> keys = val.keySet();
        for(Double key : keys) {
            double prob = val.get(key);
            double dist = Math.abs(prob - randomValue);
            if (dist < minDist) {
                minDist = dist;
                prediction = key;
            }
        }
        return prediction;
    }

    private double getMax(HashMap<Double, Double> val) {
        Set<Double> keys = val.keySet();
        double maxProb = 0;
        double prediction = 0;
        for(Double key : keys) {
            double prob = val.get(key);
            if(prob > maxProb) {
                maxProb = prob;
                prediction = key;
            }
        }
        return prediction;
    }

    private double predict(double val) {
        List<HashMap<Double, Double>> probs = ngram.getAllProbabilities(valueQueue);
        Double probabilistic = null;
        Double max = null;
        for(int i = 0; i < probs.size(); i ++) {
            HashMap<Double, Double> prob = probs.get(i);
            if(prob.size() > 0){
                probabilistic = getProbabilistic(prob);
                max = getMax(prob);
            }
        }
        if(max != null) {
            outlet(0, max);
        } else {
            outlet(0, val);
            max = val;
        }
        if(probabilistic != null){
            outlet(1, probabilistic);
        } else {
            outlet(1, val);
        }
        return max;
    }

    public static double mean(Stack<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }

    private void calculateAccuracy(double val) {
        this.accuracyQueue.add(Math.abs(val - lastPrediction));
        while(accuracyQueue.size() > ngramSize * 2) {
            accuracyQueue.remove(0);
        }
        if(accuracyQueue.size() == ngramSize * 2) {
            double firstHalf = 0;
            double secondHalf = 0;
            for (int i = 0; i < ngramSize; i ++) {
                firstHalf = firstHalf + accuracyQueue.get(i);
                secondHalf = secondHalf + accuracyQueue.get(i + ngramSize);
            }
            double accuracy = (firstHalf/(double) ngramSize) - (secondHalf/(double) ngramSize);
            this.accuracyQueueQueue.add(accuracy);
            while(accuracyQueueQueue.size() > ngramSize) {
                accuracyQueueQueue.remove(0);
            }
            outlet(2, mean(accuracyQueueQueue));
        }
    }


    public void inlet(float val) {
        int intlet_no = getInlet();
        switch (intlet_no) {
            case 0:
                insert(val);
                calculateAccuracy(val);
                lastPrediction = predict(val);
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }

    public void bang() {
        post("reset!");
        this.ngram = new NGram();
        this.valueQueue = new Stack<Double>();
        this.accuracyQueue = new Stack<Double>();
        this.accuracyQueueQueue = new Stack<Double>();
        this.lastPrediction = 0;
    }

    public static void main(String[] args) {
    }
}
