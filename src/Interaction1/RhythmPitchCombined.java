package Interaction1;

import com.cycling74.max.*;

import java.util.*;

/**
 * Used for influx. Assumes we know milliseconds per beat and have access to a global counter incremented
 * at a steady tempo. We also assume we are in 4/4 time
 *
 * Created by yn on 12/3/16.
 */
public class RhythmPitchCombined extends MaxObject{
    private NGram rhythmNGram;
    private NGram pitchNGram;

    private int sameCount;
    private double lastPitch;

    double lastPrediction;
    private Stack<Double> rhythmValueQueue;
    private Stack<Double> pitchValueQueue;
    private Stack<Double> rhythmAccuracyQueue;
    private Stack<Double> pitchAccuracyQueue;

    private static final int RHYTHM_NGRAM_SIZE = 8;
    private static final int PITCH_NGRAM_SIZE = 16;
    private static final double[] RHYTHM_LIST = {0.25, 0.5, 2.0/3.0, 1, 1.5, 2, 3, 4};


    public RhythmPitchCombined() {
        this.sameCount = 0;
        this.rhythmNGram = new NGram();
        this.pitchNGram = new NGram();
        this.rhythmValueQueue = new Stack<Double>();
        this.pitchValueQueue = new Stack<Double>();

        this.rhythmAccuracyQueue = new Stack<Double>();
        this.pitchAccuracyQueue = new Stack<Double>();
        this.lastPrediction = 0;
        this.lastPitch = -1;

        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.LIST,});
        declareOutlets(new int[]{ DataTypes.LIST, DataTypes.FLOAT, DataTypes.FLOAT });

        setInletAssist(new String[] {
                "list (time (from timer), pitch, globalCounter, milliseconds per beat) "
        });
        setOutletAssist(new String[] { "Output probable next note (note, delay)",
                "rhythm accuracy measure (standard deviation of last" + RHYTHM_NGRAM_SIZE + "predictions from all observation)",
                "pitch accuracy measure (standard deviation of last" + PITCH_NGRAM_SIZE + "predictions from all observation)"
        });
    }

    private void insert (double[] val) {
        //**RHYTHM
        this.rhythmValueQueue.add(val[0]);
        while(rhythmValueQueue.size() > RHYTHM_NGRAM_SIZE) {
            rhythmValueQueue.remove(0);
        }
        if(rhythmValueQueue.size() == RHYTHM_NGRAM_SIZE) {
            rhythmNGram.insert(rhythmValueQueue);
        }

        //**PITCH
        //only input one repeat pitch, after that ignore same pitches
        if(lastPitch == val[1]) {
            sameCount++;
        } else {
            sameCount = 0;
        }
        lastPitch = val[1];
        if(sameCount < 2) {
            this.pitchValueQueue.add(val[1]);
            while (pitchValueQueue.size() > PITCH_NGRAM_SIZE) {
                pitchValueQueue.remove(0);
            }
            if (pitchValueQueue.size() == PITCH_NGRAM_SIZE) {
                pitchNGram.insert(pitchValueQueue);
            }
        }
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

    private double[] predict(double[] val) {
        //**RHYTHM
        List<HashMap<Double, Double>> rhythmProbs = rhythmNGram.getAllProbabilities(rhythmValueQueue);
        Double rhythmPrediction = val[0];
        for(int i = 0; i < rhythmProbs.size(); i ++) {
            HashMap<Double, Double> prob = rhythmProbs.get(i);
            if(prob.size() > 0){
                rhythmPrediction = getProbabilistic(prob);
            }
        }

        //**PITCH
        List<HashMap<Double, Double>> pitchProbs = pitchNGram.getAllProbabilities(pitchValueQueue);
        Double pitchPrediction = val[1];
        for(int i = 0; i < pitchProbs.size(); i ++) {
            HashMap<Double, Double> prob = pitchProbs.get(i);
            if(prob.size() > 0){
                pitchPrediction = getProbabilistic(prob);
            }
        }
        double[] result = {rhythmPrediction, pitchPrediction};
        return result;
    }

    public static double mean(Stack<Double> m) {
        double sum = 0;
        for (int i = 0; i < m.size(); i++) {
            sum += m.get(i);
        }
        return sum / m.size();
    }

    private void calculateAccuracy(double val) {
        this.pitchAccuracyQueue.add(Math.abs(val - lastPrediction));
        while(pitchAccuracyQueue.size() > PITCH_NGRAM_SIZE) {
            pitchAccuracyQueue.remove(0);
        }
        if(pitchAccuracyQueue.size() == PITCH_NGRAM_SIZE) {
            //
            post("RhythmPitchCombined - calculateAccuracy IMPLEMENT ME!");
        }
    }

    private double convertRhythm(double rhythmTimeVal, double msPerBeat) {
        double actualRhythmValue = rhythmTimeVal/msPerBeat;
        double guess = -1;

        if(actualRhythmValue <= 4.0) {
            for (int i = 0; i < RHYTHM_LIST.length; i++) {
                double newGuess = Math.abs(RHYTHM_LIST[i] - actualRhythmValue);
                double bestGuess = Math.abs(guess - actualRhythmValue);
                if ((newGuess < bestGuess) || (guess == -1)) {
                    guess = RHYTHM_LIST[i];
                }
            }
        }

        return guess;
    }

    public void list(Atom[] args) {
        double rhythmTimeVal = args[0].getFloat();
        double PitchVal = args[1].getFloat();
        int globalCounter = args[2].getInt();
        double msPerBeat = args[3].getFloat();

        //convert millisecond times to closest rhythmic value
        double rhythmBeatVal  = convertRhythm(rhythmTimeVal, msPerBeat);
        post("rhythmVal " + rhythmBeatVal);

        double delay = 0;
        double pitch = PitchVal;

        double[] rhythmPitch = {rhythmBeatVal, PitchVal};
        double[] prediction = predict(rhythmPitch);

        if(rhythmBeatVal < 0) {
            //too long of a pause
            pitch = prediction[1];
        } else {
            //appropriately long pause
            insert(rhythmPitch);
            delay = prediction[0] * msPerBeat;
            pitch = prediction[1];
        }

        double[] output = {pitch, delay};
        outlet(0, Atom.newAtom(output));
    }

    public void bang() {
        post("reset!");

        this.sameCount = 0;
        this.rhythmNGram = new NGram();
        this.pitchNGram = new NGram();
        this.rhythmValueQueue = new Stack<Double>();
        this.pitchValueQueue = new Stack<Double>();

        this.rhythmAccuracyQueue = new Stack<Double>();
        this.pitchAccuracyQueue = new Stack<Double>();
        this.lastPrediction = 0;
        this.lastPitch = -1;
    }
}
