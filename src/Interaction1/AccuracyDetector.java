package Interaction1;

import com.cycling74.max.*;

import java.util.*;

/**
 * Created by yn on 12/7/16.
 */
public class AccuracyDetector extends MaxObject{
    private static final int RECORD_LENGTH = 4;
    private static final double OVERHANG = 50.;
    private static final double ON_BEAT_THRESH = 0.08;
    private static final double PROPORTION_ON_THRESH = 0.6;
    private static final double QUANTIZATION_BIAS = 1.0;
    private static final double[] QUANTIZATION_LIST      = {0.0, 0.25, 1.0/3.0, 0.5, 2.0/3.0, 0.75};
    private static final double[] QUANTIZATION_WEIGHTS   = {1.0, 0.5,  0.25,    1.0, 0.25,    0.5 };
    

    private ArrayList<Double> eventQueue;
    private double msPerBeat;
    private double lastMeasure;



    public AccuracyDetector() {
        this.eventQueue = new ArrayList<Double>();
        this.msPerBeat = 428.571442;
        this.lastMeasure = -1;

        createInfoOutlet(false);
        declareInlets(new int[]{ DataTypes.ALL });
        declareOutlets(new int[]{ DataTypes.ALL });
    }

    public void setMsPerBeat(double msPerBeat){
        this.msPerBeat = msPerBeat;
    }

    public void insert(double newVal) {
        //post("insert! " + newVal);
        eventQueue.add(newVal);
        if(eventQueue.size() > RECORD_LENGTH * 16) {
            eventQueue.remove(0);
        }
    }

    private ArrayList<Double> trim(double time) {
        ArrayList<Double> result = new ArrayList<Double>();
        //post("between " + lastMeasure + " and " + time);
        //post("event queue: " + eventQueue.toString());
        for (double event : eventQueue) {
            if (((event <= time) && (event >= lastMeasure)) || Math.abs(event - time) < OVERHANG || Math.abs(lastMeasure - event) < OVERHANG) {
                result.add(((event - lastMeasure) / msPerBeat));
            }
        }
        return result;
    }

    private boolean contains(double val, double[] list){
        for(int i = 0; i < list.length; i ++){
            if (val == list[i]){
                return true;
            }
        }
        return false;
    }

    private double smallest(Collection<Double> list){
        double smallest = -1;
        for(Double d : list){
            if(smallest == -1 || d < smallest){
                smallest = d;
            }
        }
        return smallest;
    }

    private double calcScore(double event, ArrayList<Double> seen){
        double[] seenArray = toArray(seen);
        double result = 0.0;
        HashMap<Double, Double> guesses = new HashMap<Double, Double>();
        for(int i = 0; i < QUANTIZATION_LIST.length; i++){
            double offset = event % 1.0;
            //post("offset " + offset);
            double quant = QUANTIZATION_LIST[i];
            double diff = Math.abs(offset - quant);
            double weighted = diff / Math.pow(QUANTIZATION_WEIGHTS[i], QUANTIZATION_BIAS);
            if(weighted < ON_BEAT_THRESH){
                double place = ((event - offset) + quant);
                if(place < RECORD_LENGTH) {
                    if (!contains(place, seenArray)) {
                        guesses.put(weighted, place);
                    }
                }
            }
        }
        double smallest = smallest(guesses.keySet());
        if(smallest != -1) {
            seen.add(guesses.get(smallest));
            result = 1.0;
        }
        return result;
    }

    public double[] toArray(ArrayList<Double> list){
        double[] result = new double[list.size()];
        for(int i = 0; i < list.size(); i++){
            result[i] = list.get(i);
        }
        return result;
    }

    public double[] calculateAccuracy(double time){
        double[] result = null;
        if(lastMeasure > 0.0){
            double on_count = 0.0;
            double total_count = 0.0;
            ArrayList<Double> trimmed = trim(time);
            //post("trimmed" + trimmed);
            ArrayList<Double> seen = new ArrayList<Double>();
            for(double event : trimmed){
                on_count = on_count + calcScore(event, seen);
                //post("seen " + seen.toString());
                total_count = total_count + 1.0;
            }
            if(total_count >= RECORD_LENGTH){
                if(on_count/total_count >= PROPORTION_ON_THRESH){
                    //post("prop on: " + on_count/total_count);
                    result = toArray(seen);
                }
            }
        }
        lastMeasure = time;
        return result;
    }

    public void beat(double time, double beatCounter){
        if((beatCounter - 1) % 4 == 0){
            //post("new measure!");
            double[] trimmed = calculateAccuracy(time - msPerBeat);
            if(trimmed != null){
                Arrays.sort(trimmed);
                outlet(0, Atom.newAtom(trimmed));
            }
        }
    }

    public void bang(){
        this.eventQueue = new ArrayList<Double>();
        this.msPerBeat = 428.571442;
        this.lastMeasure = -1;
    }
}