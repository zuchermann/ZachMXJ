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
        for(int i = 2; i < val.size(); i ++) {
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

    public List<HashMap<Double, Double>> getAllProbabilities(List<Double> val) {
        List<List<Double>> broken = breakBackwards(val);
        List<HashMap<Double, Double>> result = new ArrayList<HashMap<Double, Double>>();
        for (List<Double> sublist : broken) {
            HashMap<Double, Double> probabilityList = getProbabilities(sublist);
            result.add(probabilityList);
            //System.out.println(sublist);
        }
        return result;
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
        newVal.add(2.2);
        newVal.add(2.2);
        newVal.add(2.2);
        newVal.add(2.2);
        myNgram.insert(newVal);
        System.out.println(myNgram.getAllProbabilities(newVal));
        //System.out.println(myNgram.probs);

    }

}
