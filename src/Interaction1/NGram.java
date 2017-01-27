package Interaction1;

import java.util.*;
import java.util.List;

/**
 * Created by yn on 11/20/16.
 */
public class NGram {
    HashMap<List<Double>, Integer> counts; //input, first is always count
    HashMap<Integer, HashMap<List<Double>, HashMap<Double,Integer>>> probs;

    public NGram() {
        this.counts = new HashMap<List<Double>, Integer>();
        this.probs = new HashMap<Integer, HashMap<List<Double>, HashMap<Double,Integer>>>();
    }

    private List<List<Double>> breakUp(List<Double> val) {
        List<List<Double>> broken = new ArrayList<List<Double>>();
        for(int i = 2; i <= val.size(); i ++) {
            List<Double> innerList = new ArrayList<Double>();
            for(int j = 0; j < i; j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        return broken;
    }

    private void insertHelp(List<Double> val) {
        //insert into counts
        List<Double> sublist = val.subList(0, val.size() - 1);
        if (counts.containsKey(sublist)) {
            int current = counts.get(sublist);
            counts.put(sublist, current + 1);
        } else {
            counts.put(sublist, 1);
        }

        //insert into ngram
        HashMap<List<Double>, HashMap<Double, Integer>> nGram = probs.get(val.size());
        if(nGram == null) {
            probs.put(val.size(), new HashMap<List<Double>, HashMap<Double, Integer>>());
            nGram = probs.get(val.size());
        }
        HashMap<Double, Integer> probabilities = nGram.get(sublist);
        if(probabilities == null) {
            HashMap<Double, Integer> newPrediction = new HashMap<Double, Integer>();
            newPrediction.put(val.get(val.size()-1), 1);
            nGram.put(sublist, newPrediction);
        } else {
            Double predicted = val.get(val.size()-1);
            if (probabilities.containsKey(predicted)){
                probabilities.put(predicted, probabilities.get(predicted) + 1);
            } else {
                probabilities.put(predicted, 1);
            }
        }
    }

    public void insert(List<Double> val){
        List<List<Double>> brokenUp = breakUp(val);
        //System.out.println("broken = " + brokenUp);
        for(List<Double> subVal : brokenUp){
            insertHelp(subVal);
        }
    }

    private List<List<Double>> breakBackwards (List<Double> val){
        List<List<Double>> broken = new ArrayList<List<Double>>();
        for(int i = val.size() - 1; i > 0; i--) {
            List<Double> innerList = new ArrayList<Double>();
            for(int j = i; j < val.size(); j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        //System.out.println(broken);
        return broken;
    }

    public Integer getNumberOfOccurences(List<Double> val) {
        Integer count = counts.get(val);
        if(count == null) {
            count = 0;
        }
        return  count;
    }

    public HashMap<Double, Integer> getPredictionCounts(List<Double> val) {
        HashMap<Double, Integer> result = new HashMap<Double, Integer>();
        HashMap<List<Double>, HashMap<Double, Integer>> nGram =  probs.get(val.size() + 1);
        //System.out.println(nGram);
        if(nGram != null) {
            HashMap<Double, Integer> innerMap = nGram.get(val);
            //System.out.println(innerMap);
            //System.out.println(val);
            //System.out.println(nGram);
            if(innerMap != null) {
                result = innerMap;
            }
        }
        return result;
    }

    //predicted value and its probability 0-1
    public HashMap<Double, Double> getProbabilities(List<Double> val){
        HashMap<Double, Double> result = new HashMap<Double, Double>();
        Integer num = getNumberOfOccurences(val);
        HashMap<Double, Integer> predictionCounts = getPredictionCounts(val);
        Set<Double> keys = predictionCounts.keySet();
        for (Double key : keys){
            Double probability =  predictionCounts.get(key) / (double) num;
            result.put(key, probability);
        }
        return result;
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

    public double predict(List<Double> val) {
        List<List<Double>> broken = new ArrayList<List<Double>>();
        for(int i = val.size() - 1; i >= 0; i--) {
            List<Double> innerList = new ArrayList<Double>();
            for(int j = i; j < val.size(); j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        //System.out.println(broken);
        //System.out.println("broken = " + broken);
        //System.out.println(val);
        List<HashMap<Double, Double>> result = new ArrayList<HashMap<Double, Double>>();
        for (List<Double> sublist : broken) {
            HashMap<Double, Double> probabilityList = getProbabilities(sublist);
            result.add(probabilityList);
            //System.out.println(sublist);
        }
        List<HashMap<Double, Double>> rhythmProbs = result;
        for(int i = 0; i < rhythmProbs.size(); i ++) {
            HashMap<Double, Double> prob = rhythmProbs.get(i);
            if(prob.size() > 0){
                return getProbabilistic(prob);
            }
        }
        return val.get(val.size() - 1);
    }

    public List<HashMap<Double, Double>> getAllProbabilities(List<Double> val) {
        List<List<Double>> broken = breakBackwards(val);
        //System.out.println("broken = " + broken);
        //System.out.println(val);
        List<HashMap<Double, Double>> result = new ArrayList<HashMap<Double, Double>>();
        for (List<Double> sublist : broken) {
            HashMap<Double, Double> probabilityList = getProbabilities(sublist);
            result.add(probabilityList);
            //System.out.println(sublist);
        }
        return result;
    }

    private static int getRandomInt(int min, int max){
        Random rand = new Random();
        int n = rand.nextInt(max - min);
        return n + min;
    }

    private double getMax(HashMap<Double, Integer> map){
        int max = -1;
        double result = -1;
        for(Double key : map.keySet()) {
            if(map.get(key) > max) {
                max = map.get(key);
                result = key;
            }
        }
        return result;
    }

    public double[] getMaxProbOfOrder(int length){
        HashMap<List<Double>, HashMap<Double,Integer>> ngram = this.probs.get(length);
        int max = -1;
        List<Double> maxKey = null;
        double[] result = new double[length];
        if(ngram != null) {
            for (List<Double> key : ngram.keySet()) {
                if (counts.get(key) > max) {
                    max = counts.get(key);
                    maxKey = key;
                }
            }
        }
        if(maxKey != null) {
            for (int i = 0; i < length; i++) {
                if (i == length - 1) {
                    //System.out.println(ngram.get(maxKey));
                    result[i] = getMax(ngram.get(maxKey));
                } else {
                    result[i] = maxKey.get(i);
                }
            }
        } else {
            result = null;
        }
        return result;
    }

    public String toString(){
        return probs.toString();
    }

    public static void main(String[] args){

        //test insert
        NGram myNgram = new NGram();
        List<Double> newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        newVal = new ArrayList<Double>();
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        newVal.add(5.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        newVal = new ArrayList<Double>();
        newVal.add(3.2);
        newVal.add(4.2);
        newVal.add(5.2);
        newVal.add(6.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        newVal = new ArrayList<Double>();
        newVal.add(4.2);
        newVal.add(5.2);
        newVal.add(6.2);
        newVal.add(7.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(4.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        //System.out.println(myNgram.probs);
        //System.out.println(myNgram.probs);

        System.out.println(Arrays.toString(myNgram.getMaxProbOfOrder(4)));
    }

}
