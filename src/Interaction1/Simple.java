package Interaction1;


/**
 * Created by yn on 11/20/16.
 */
import Interaction1.NGram;
import com.cycling74.max.*;

import java.io.IOException;
import java.util.*;

public class Simple extends MaxObject{
    private NGram<String> ngram;
    private int ngramSize;
    //Double[] lastPrediction;
    private Stack<String> valueQueue;
    //Stack<Integer> accuracyQueueQueue;
    //Stack<Integer> accuracyQueue;
    private float accuracy; /// not related to accuracyQueue - tells how accurate we want our values to be when rounded
    private static Class nGramClass = ("").getClass();

    public Simple() {
        this(2);
    }

    public Simple(int ngramSize) {
        this.ngramSize = ngramSize;
        this.ngram = new NGram<String>(nGramClass);
        this.valueQueue = new Stack<String>();
        //this.accuracyQueue = new Stack<Integer>();
        //this.accuracyQueueQueue = new Stack<Integer>();
        //this.lastPrediction = new Double[] {0.0};
        this.accuracy = 1000;

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});

        setInletAssist(new String[] {
                "values to predict",
        });
        setOutletAssist(new String[] { "value with max prob in largest n-gram",
                "probabilistic select value in largest n-gram",
                "nothing comes out of here"
        });
    }

    public NGram<String> getNGram() {
        return ngram;
    }

    private void insert(String val) {
        this.valueQueue.add(val);
        while(valueQueue.size() > ngramSize) {
            valueQueue.remove(0);
        }
        ngram.insert(valueQueue);
        //outlet(0, valueQueue.toString());
    }

    private String atomString(Atom[] atomArray){
        String result = "";
        for(int i = 0; i < atomArray.length - 1; i++){
            result = result + (Math.round(atomArray[i].toFloat() * this.accuracy) / this.accuracy) + " ";
        }
        result = result + (Math.round(atomArray[atomArray.length - 1].toFloat() * this.accuracy) / this.accuracy);
        return result;
    }

    private String doublesToString(double[] atomArray){
        String result = "";
        if(atomArray.length > 0) {
            for (int i = 0; i < atomArray.length - 1; i++) {
                result = result + (Math.round(atomArray[i] * this.accuracy) / this.accuracy) + " ";
            }
            result = result + (Math.round(atomArray[atomArray.length - 1] * this.accuracy) / this.accuracy);
        }
        return result;
    }

    public void insertList(Atom[] val) {
        insert(atomString(val));
        predict(atomString(val));
        //System.out.println(ngram);
    }

    public void insertOnly(Atom[] val) {
        insert(atomString(val));
    }

    public void postNGram() {
        post(ngram.toString());
    }

    public void predictOnly(Atom[] val) {
        this.valueQueue.add(atomString(val));
        while(valueQueue.size() > ngramSize) {
            valueQueue.remove(0);
        }
        predict(atomString(val));
    }

    public void insertOnlyDoubleList(double[] val) {
        insert(doublesToString(val));
    }

    private String getProbabilistic(HashMap<String, Double> val) {
        Random r = new Random();
        double randomValue = r.nextDouble();
        double minDist = 1;
        String prediction = "";
        Set<String> keys = val.keySet();
        for(String key : keys) {
            double prob = val.get(key);
            double dist = Math.abs(prob - randomValue);
            if (dist < minDist) {
                minDist = dist;
                prediction = key;
            }
        }
        return prediction;
    }

    private String getMax(HashMap<String, Double> val) {
        Set<String> keys = val.keySet();
        double maxProb = 0;
        String prediction = "";
        for(String key : keys) {
            double prob = val.get(key);
            if(prob > maxProb) {
                maxProb = prob;
                prediction = key;
            }
        }
        return prediction;
    }

    private double[] toPrimitiveArray(Double[] val){
        double[] result = new double[val.length];
        for(int i = 0; i < val.length; i++){
            result[i] = val[i];
        }
        return result;
    }

    public void parseMidiRhythm(Atom[] args) throws IOException {
        String path = args[0].getString();
        NGramParser.parse(path, ngramSize, this, true);
    }

    public void parseMidiMelody(Atom[] args) throws IOException {
        String path = args[0].getString();
        NGramParser.parse(path, ngramSize, this, false);
    }

    private String predict(String val) {
        List<HashMap<String, Double>> probs = ngram.getAllProbabilities(valueQueue);
        //System.out.println(probs);
        String probabilistic = null;
        String max = null;
        for(int i = 0; i < probs.size(); i ++) {
            HashMap<String, Double> prob = probs.get(i);
            if(prob.size() > 0){
                probabilistic = getProbabilistic(prob);
                max = getMax(prob);
            }
        }
        if(max != null) {
            outlet(0, Atom.parse(max));
        } else {
            outlet(0, Atom.parse(val));
            max = val;
        }
        if(probabilistic != null){
            outlet(1, Atom.parse(probabilistic));
        } else {
            outlet(1, Atom.parse(val));
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

    /*
    private void calculateAccuracy(double val) {
        //this.accuracyQueue.add(Math.abs(val - lastPrediction));
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
            //this.accuracyQueueQueue.add(accuracy);
            while(accuracyQueueQueue.size() > ngramSize) {
                accuracyQueueQueue.remove(0);
            }
            //outlet(2, mean(accuracyQueueQueue));
        }
    }
    */


    public void inlet(float val) {
        int intlet_no = getInlet();
        switch (intlet_no) {
            case 0:
                String newVal = "" + Math.round(val * this.accuracy) / this.accuracy;
                insert(newVal);
                predict(newVal);
                //calculateAccuracy(val);
                //lastPrediction = predict(newVal);
                break;
        }
    }

    public void clearValueQueue() {
        this.valueQueue = new Stack<String>();
    }

    public void clear(){
        bang();
    }

    public void bang() {
        post("reset!");
        this.ngram = new NGram<String>(nGramClass);
        this.valueQueue = new Stack<String>();
        //this.accuracyQueue = new Stack<Double>();
        //this.accuracyQueueQueue = new Stack<Double>();
        //this.lastPrediction = new Double[] {0.0};
    }

    public static void main(String[] args) {
    }
}
