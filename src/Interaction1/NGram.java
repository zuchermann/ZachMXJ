package Interaction1;

import java.lang.reflect.Array;
import java.util.*;
import java.util.List;

import java.util.*;
class MapUtil {
    public static<K> List<K> sortByValue( HashMap<K, Double> map ) {
        List<HashMap.Entry<K, Double>> list = new LinkedList<>(map.entrySet());
        Collections.sort( list, (o1, o2) ->
                (o1.getValue()) > (o2.getValue()) ? 1 : ((o1.getValue() > o2.getValue()) ? -1 : 0));

        List<K> result = new ArrayList<K>();
        for (HashMap.Entry<K, Double> entry : list)
        {
            result.add( entry.getKey() );
        }
        return result;
    }
}

/**
 * Created by yn on 11/20/16.
 */
public class NGram<T> {
    private HashMap<List<T>, Integer> counts; //input, first is always count
    private HashMap<Integer, HashMap<List<T>, HashMap<T,Integer>>> probs;
    private Class<T> theClass;

    public NGram(Class<T> theClass) {
        this.counts = new HashMap<List<T>, Integer>();
        this.probs = new HashMap<Integer, HashMap<List<T>, HashMap<T,Integer>>>();
    }

    private List<List<T>> breakUp(List<T> val) {
        List<List<T>> broken = new ArrayList<List<T>>();
        for(int i = 1; i <= val.size(); i++) {
            List<T> innerList = new ArrayList<T>();
            for(int j = val.size() - i; j < val.size(); j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        return broken;
    }

    private void insertHelp(List<T> val) {
        //insert into counts
        List<T> sublist = val.subList(0, val.size() - 1);
        if (counts.containsKey(sublist)) {
            int current = counts.get(sublist);
            counts.put(sublist, current + 1);
        } else {
            counts.put(sublist, 1);
        }

        //insert into ngram
        HashMap<List<T>, HashMap<T, Integer>> nGram = probs.get(val.size());
        if(nGram == null) {
            probs.put(val.size(), new HashMap<List<T>, HashMap<T, Integer>>());
            nGram = probs.get(val.size());
        }
        HashMap<T, Integer> probabilities = nGram.get(sublist);
        if(probabilities == null) {
            HashMap<T, Integer> newPrediction = new HashMap<T, Integer>();
            newPrediction.put(val.get(val.size()-1), 1);
            nGram.put(sublist, newPrediction);
        } else {
            T predicted = val.get(val.size()-1);
            if (probabilities.containsKey(predicted)){
                probabilities.put(predicted, probabilities.get(predicted) + 1);
            } else {
                probabilities.put(predicted, 1);
            }
        }
    }

    public void insert(List<T> val){
        List<List<T>> brokenUp = breakUp(val);
        //System.out.println("broken = " + brokenUp);
        for(List<T> subVal : brokenUp){
            insertHelp(subVal);
        }
    }

    private List<List<T>> breakBackwards (List<T> val){
        List<List<T>> broken = new ArrayList<List<T>>();
        for(int i = val.size(); i >= 0; i--) {
            List<T> innerList = new ArrayList<T>();
            for(int j = i; j < val.size(); j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        //System.out.println(broken);
        return broken;
    }

    public Integer getNumberOfOccurences(List<T> val) {
        Integer count = counts.get(val);
        if(count == null) {
            count = 0;
        }
        return  count;
    }

    public HashMap<T, Integer> getPredictionCounts(List<T> val) {
        HashMap<T, Integer> result = new HashMap<T, Integer>();
        HashMap<List<T>, HashMap<T, Integer>> nGram =  probs.get(val.size() + 1);
        //System.out.println(nGram);
        if(nGram != null) {
            HashMap<T, Integer> innerMap = nGram.get(val);
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
    public HashMap<T, Double> getProbabilities(List<T> val){
        HashMap<T, Double> result = new HashMap<T, Double>();
        Integer num = getNumberOfOccurences(val);
        HashMap<T, Integer> predictionCounts = getPredictionCounts(val);
        Set<T> keys = predictionCounts.keySet();
        for (T key : keys){
            Double probability =  predictionCounts.get(key) / (double) num;
            result.put(key, probability);
        }
        return result;
    }

    private T getProbabilistic(HashMap<T, Double> val) {
        Random r = new Random();
        double randomValue = r.nextDouble();
        double minDist = 1;
        T prediction = null;
        Set<T> keys = val.keySet();
        for(T key : keys) {
            Double prob = val.get(key);
            Double dist = Math.abs(prob - randomValue);
            if (dist < minDist) {
                minDist = dist;
                prediction = key;
            }
        }
        return prediction;
    }

    public T predict(List<T> val) {
        List<List<T>> broken = new ArrayList<List<T>>();
        for(int i = val.size() - 1; i >= 0; i--) {
            List<T> innerList = new ArrayList<T>();
            for(int j = i; j < val.size(); j++) {
                innerList.add(val.get(j));
            }
            broken.add(innerList);
        }
        //System.out.println(broken);
        //System.out.println("broken = " + broken);
        //System.out.println(val);
        List<HashMap<T, Double>> result = new ArrayList<HashMap<T, Double>>();
        for (List<T> sublist : broken) {
            HashMap<T, Double> probabilityList = getProbabilities(sublist);
            result.add(probabilityList);
            //System.out.println(sublist);
        }
        List<HashMap<T, Double>> rhythmProbs = result;
        for(int i = 0; i < rhythmProbs.size(); i ++) {
            HashMap<T, Double> prob = rhythmProbs.get(i);
            if(prob.size() > 0){
                return getProbabilistic(prob);
            }
        }
        return val.get(val.size() - 1);
    }

    public List<HashMap<T, Double>> getAllProbabilities(List<T> val) {
        List<List<T>> broken = breakBackwards(val);
        //System.out.println("broken = " + broken);
        //System.out.println(val);
        List<HashMap<T, Double>> result = new ArrayList<HashMap<T, Double>>();
        for (List<T> sublist : broken) {
            HashMap<T, Double> probabilityList = getProbabilities(sublist);
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

    private T getMax(HashMap<T, Integer> map){
        int max = -1;
        T result = null;
        for(T key : map.keySet()) {
            if(map.get(key) > max) {
                max = map.get(key);
                result = key;
            }
        }
        return result;
    }

    public T[] getMaxProbOfOrder(int length){
        HashMap<List<T>, HashMap<T,Integer>> ngram = this.probs.get(length);
        int max = -1;
        List<T> maxKey = null;
        T[] result = (T[]) Array.newInstance(theClass, length);
        if(ngram != null) {
            for (List<T> key : ngram.keySet()) {
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

    public List<T> shuffleWeighted(HashMap<T, Double> probs){
        List<T> result = new ArrayList<T>(probs.size());
        double totalWeight = 1.0d;
        while(probs.size() > 0){
            double random = Math.random() * totalWeight;
            for (T key : probs.keySet())
            {
                random -= probs.get(key);
                if (random <= 0.0d)
                {
                    result.add(key);
                    totalWeight -= probs.get(key);
                    probs.remove(key);
                    break;
                }
            }
        }
        return result;
    }

    // Gets all possible prediction then probabilistically shuffles and returns.
    // The item at position 0 is the most probable
    public List<T> getAllShuffled(List<T> val){
        return shuffle(getAllProbabilities(val));
    }

    // Gets all possible prediction then sorts based on probability and returns.
    // The item at position 0 is the most probable
    public List<T> getAllSorted(List<T> val){
        return hashSort(getAllProbabilities(val));
    }

    public List<T> hashSort(List<HashMap<T, Double>> probs){
        List<T> result = new ArrayList<T>();
        for (HashMap<T, Double> prob : probs) {
            List<T> sorted = MapUtil.sortByValue(prob);
            result.addAll(sorted);
        }
        Collections.reverse(result);
        return result;
    }

    public List<T> shuffle(List<HashMap<T, Double>> probs){
        List<T> result = new ArrayList<T>();
        for (HashMap<T, Double> prob : probs){
            List<T> shuffled = shuffleWeighted(prob);
            Collections.reverse(shuffled);
            result.addAll(shuffled);
            //System.out.println(shuffled);
        }
        Collections.reverse(result);
        return result;
    }

    public String toString(){
        return probs.toString();
    }

    public static void main(String[] args){

        //test insert
        NGram myNgram = new NGram<>((new Double(0)).getClass());
        List<Double> newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        //System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram);
        newVal = new ArrayList<Double>();
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        newVal.add(5.2);
        myNgram.insert(newVal);
        //System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram);
        newVal = new ArrayList<Double>();
        newVal.add(3.2);
        newVal.add(4.2);
        newVal.add(5.2);
        newVal.add(6.2);
        myNgram.insert(newVal);
        //System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram);
        newVal = new ArrayList<Double>();
        newVal.add(4.2);
        newVal.add(5.2);
        newVal.add(6.2);
        newVal.add(7.2);
        myNgram.insert(newVal);
        //System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram);
        newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(3.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        //System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram);
        newVal = new ArrayList<Double>();
        newVal.add(1.2);
        newVal.add(2.2);
        newVal.add(4.2);
        newVal.add(4.2);
        myNgram.insert(newVal);
        System.out.println(myNgram);
        System.out.println(myNgram.getAllProbabilities(newVal));
        System.out.println(myNgram.getAllSorted(newVal));
        System.out.println(myNgram.getAllShuffled(newVal));
        System.out.println(myNgram);
        //System.out.println();
        //System.out.println(myNgram);
        //System.out.println(myNgram.probs);
        //System.out.println(myNgram.probs);

        //System.out.println(Arrays.toString(myNgram.getMaxProbOfOrder(4)));
    }

}
