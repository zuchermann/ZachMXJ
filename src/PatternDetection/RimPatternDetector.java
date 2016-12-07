package PatternDetection;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;

/**
 * Created by Hanoi on 12/3/16.
 */
public class RimPatternDetector extends MaxObject {

    private double[] rhythmVector;
    private double[][] rhythmPatterns;
    private double bar_time;
    private double ms_p_quant;
    private double threshold;
    private int quantization_step;
    private int next_note_slot;
    private String dir;

    //threshold = 1.5 by default

    public RimPatternDetector() throws InvalidMidiDataException, IOException {
        this(16, 1.5);
        //quantization = 16 by default (1/16 notes)
        //threshold = 15 by default for drum session detector
    }

    public RimPatternDetector(int quantization_step) throws InvalidMidiDataException, IOException {
        this(quantization_step, 1.5);
        //threshold = 1.5 by default for drum session detector
    }

    public RimPatternDetector(int quantization_step, double threshold) throws InvalidMidiDataException, IOException {
        this.dir = this.getCodeSourcePath();
        int index = dir.lastIndexOf('/');
        dir = dir.substring(0,index);
        //post(dir + "/rim_midi_files");
        this.rhythmVector = new double[quantization_step];
        this.rhythmPatterns = RimPatternParser.parse(quantization_step, dir); // Returns a double[][] pattern_matrix
        this.quantization_step = quantization_step;
        this.next_note_slot = -1;
        this.threshold = threshold;

        resetRhythmVector();


        createInfoOutlet(false);

        declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL });

        setInletAssist(new String[] {
                "set ms per beat - from top left of patch",
                "new measure - cpuclock",
                "rim hits - cpuclock",
                "make decison - bang",
        });
        setOutletAssist(new String[] {
                "detected pattern outlet",
        });

    }

    public void inlet(float val) {

        //inlet 0: number curent beat
        //inlet 1: number or symbolcurrent motif
        //inlet 2: number current tempo
        //inlet 3: 0 or 1 play/don't play

        int intlet_no = getInlet();
        switch(intlet_no) {
        case 0:
            setMsPerBeatQuant(val);
            break;
        case 1:
            newBarTime(val);
            break;
        case 2:
            newEventTime(val);
            break;
        default:
            post("INLET NOT SUPPORTED");
    }
}

    public void bang(){
        //inlet 0: number curent beat
        //inlet 1: number or symbolcurrent motif
        //inlet 2: number current tempo
        //inlet 3: 0 or 1 play/don't play
        int intlet_no = getInlet();
        switch(intlet_no) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                makeDecision();
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }

    private void newBarTime(double bar_time){
        //post("new bar");
        resetRhythmVector();
        if(next_note_slot != -1) {
            rhythmVector[0] = 1;
            next_note_slot = -1;
        }
        this.bar_time = bar_time; // in ms
    }

    private void newEventTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_note_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmVector[(int) (val)] = 1.0;
            }
        }
    }

    private void makeDecision(){
        int index;
        double min_dist;
        double[] distances;
        //printVector();

        distances = new double[rhythmPatterns.length];

        distances = calc_dists(rhythmVector, rhythmPatterns);
        min_dist = getMinValue(distances);
        //System.out.println("min dist: " + min_dist);

        if (min_dist < threshold){ //threshold = 1.5 by default
            index = findSmallestIndex(distances);
            outlet(0, index);
            //System.out.println("index of min val: " + index);
            //System.out.println("Pattern " + index + " detected");
            //System.out.println();
        }
        else {
            //System.out.println("No known pattern detected");
            outlet(0, -1);
        }
    }

    private static double[] calc_dists(double[] vector, double[][] patterns){
        double[] dists;
        dists = new double[patterns.length];

        for (int i = 0; i < patterns.length; i++){
            dists[i] = calc_euc_dist(vector, patterns[i]);
        }
        //print method
        for (int i = 0; i < dists.length; i++){
            //System.out.println("dist " + i + " " + dists[i]);
        }
        return dists;
    }

    private static double calc_euc_dist(double[] a, double[] b) {
        double diff_square_sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            diff_square_sum += (a[i] - b[i]) * (a[i] - b[i]);
        }
        return Math.sqrt(diff_square_sum);
    }

    private void setMsPerBeatQuant(double ms_p_beat){
        this.ms_p_quant = (ms_p_beat * 4.0) / (double)(this.quantization_step);
        //System.out.println("quantization_step: " + this.quantization_step);
        //System.out.println("ms_p_quant: " + ms_p_quant);
    }

    private void printVector() {
        //System.out.println("vector: ");
        for(int i = 0; i < this.rhythmVector.length; i++) {
            System.out.print(""+ this.rhythmVector[i] + " ");
        }
        System.out.println();
    }

    private void printRhythmPatterns() {
        //System.out.println("Rhythmpatterns: ");
        for(int i = 0; i < this.rhythmPatterns.length; i++) {
            for(int j = 0; j < this.rhythmPatterns[i].length; j++){
                System.out.print(""+ this.rhythmPatterns[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private void resetRhythmVector() {
        for(int i = 0; i < this.rhythmVector.length; i++){
            this.rhythmVector[i] = 0.0;
        }
    }

    private static int findSmallestIndex (double[] array){ //start method

        int index = 0;
        double min = array[index];
        for (int i=1; i<array.length; i++){

            if (array[i] < min ){
                min = array[i];
                index = i;
            }
        }
        return index;
    }

    private static double getMinValue(double[] inputArray) {
        double minValue = inputArray[0];
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] < minValue) {
                minValue = inputArray[i];
            }
        }
        return minValue;
    }

    /*
    public static void main (String[] args) throws InvalidMidiDataException, IOException{

        RimPatternDetector myDetector = new RimPatternDetector(16);
        myDetector.setMsPerBeatQuant(428.571429);
        myDetector.printRhythmPatterns();

        myDetector.newBarTime(512676.248876);
        myDetector.newEventTime(512676.248876 + 0.8 * 428.571429);
        myDetector.newEventTime(512676.248876 + 1.2 * 428.571429);
        myDetector.newEventTime(512676.248876 + 2.9 * 428.571429);
        myDetector.newEventTime(512676.248876 + 3.99 * 428.571429);
        myDetector.printVector();
        myDetector.makeDecision();
        System.out.println();
        //System.out.println("next_note_slot: " + myDetector.next_note_slot);
        myDetector.newBarTime(512676.248876 + 4.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 5.0 * 428.571429);
        //myDetector.newEventTime(512676.248876 + 6.9 * 428.571429);
        myDetector.printVector();
        myDetector.makeDecision();
        System.out.println();
        myDetector.newBarTime(512676.248876 + 8.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 8.05 * 428.571429);
        myDetector.newEventTime(512676.248876 + 9.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 10.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 11.0 * 428.571429);
        //myDetector.newEventTime(512676.248876 + 6.9 * 428.571429);
        myDetector.printVector();
        myDetector.makeDecision();
        myDetector.newBarTime(512676.248876 + 12.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 12.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 12.5 * 428.571429);
        myDetector.newEventTime(512676.248876 + 13.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 13.5 * 428.571429);
        myDetector.newEventTime(512676.248876 + 14.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 14.5 * 428.571429);
        myDetector.newEventTime(512676.248876 + 15.0 * 428.571429);
        myDetector.newEventTime(512676.248876 + 15.6 * 428.571429);
        //myDetector.newEventTime(512676.248876 + 6.9 * 428.571429);
        myDetector.printVector();
        myDetector.makeDecision();
    }
    */
}
