package PatternDetection;
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hanoi on 12/5/16.
 */
public class DrumPatternDetector extends MaxObject {
    private double[][] rhythmMatrix;
    private double[][] rhythmPatterns;
    private double bar_time;
    private double ms_p_quant;
    private double threshold;
    private int quantization_step;
    private int next_kick_slot;
    private int next_snare_slot;
    private int next_rim_slot;
    private int next_hh_slot;
    private int next_tom_slot;
    private int next_ride_slot;
    private int num_drum_components = 6;
    private ArrayList<String> fileNames = new ArrayList<String>();
    private String dir;

    public DrumPatternDetector() throws InvalidMidiDataException, IOException {
        this(32, 2.0);
        //threshold = 2.0 by default for drum session detector
    }

    public DrumPatternDetector(int quantization_step) throws InvalidMidiDataException, IOException {
        this(quantization_step, 2.0);
        //threshold = 2.0 by default for drum session detector
    }

    public DrumPatternDetector(int quantization_step, double threshold) throws InvalidMidiDataException, IOException {

        this.dir = this.getCodeSourcePath();
        int index = dir.lastIndexOf('/');
        dir = dir.substring(0,index);
        //post(dir + "/rim_midi_files");

        // Making arraylist for file names
        //File[] files = new File("src/PatternDetection/drum_midi_pattern").listFiles();
        File[] files = new File(dir + "/drum_midi_pattern").listFiles();
        //System.out.println("There are " + files.length + " midi pattern files");
        post("There are " + files.length + " midi pattern files");
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".mid")) {
                fileNames.add(file.getName());
            }
        }

        this.rhythmMatrix = new double[num_drum_components][quantization_step];
        resetRhythmMatrix();
        this.rhythmPatterns = DrumSessionCentroids.parse(quantization_step, dir); // Returns a double[][] pattern_matrix
        this.quantization_step = quantization_step;
        this.next_kick_slot = -1;
        this.next_snare_slot = -1;
        this.next_rim_slot = -1;
        this.next_hh_slot = -1;
        this.next_tom_slot = -1;
        this.next_ride_slot = -1;
        this.threshold = threshold;

        // Declerations for Max Object
        createInfoOutlet(false);
        declareInlets(new int[]{ DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL, DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL });

        setInletAssist(new String[] {
                "set ms per beat - from top left of patch",
                "set ms of new measure ",
                "new kick hit - cpuclock",
                "new snare hit - cpuclock",
                "new rim hit - cpuclock",
                "new hihat hit - cpuclock",
                "new tom hit - cpuclock",
                "new ride hit - cpuclock",
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
                newKickTime(val);
                break;
            case 3:
                newSnareTime(val);
                break;
            case 4:
                newRimTime(val);
                break;
            case 5:
                newHHTime(val);
                break;
            case 6:
                newTomTime(val);
                break;
            case 7:
                newRideTime(val);
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }

    public void inlet(int counter) {
        if (counter % 4 == 0) {
            post(Integer.toString(counter));
            makeDecision();
        }
    }

    public void bang() {
        //inlet 0: number curent beat
        //inlet 1: number or symbolcurrent motif
        //inlet 2: number current tempo
        //inlet 3: 0 or 1 play/don't play
        int intlet_no = getInlet();
        switch (intlet_no) {
            case 7:
                makeDecision();
                break;
            default:
                post("INLET NOT SUPPORTED");
        }
    }

        private void newBarTime(double bar_time){ // This function checks to see if there was a note in the previous bar that should be snapped to the current bar
        //post("new bar");
        resetRhythmMatrix();
        if(next_kick_slot != -1) {
            rhythmMatrix[0][0] = 1.0;
            next_kick_slot = -1;
        }
        if(next_snare_slot != -1) {
            rhythmMatrix[1][0] = 1;
            next_snare_slot = -1;
        }
        if(next_rim_slot != -1) {
            rhythmMatrix[2][0] = 1.0;
            next_rim_slot = -1;
        }
        if(next_hh_slot != -1) {
            rhythmMatrix[3][0] = 1.0;
            next_hh_slot = -1;
        }
        if(next_tom_slot != -1) {
            rhythmMatrix[4][0] = 1.0;
            next_tom_slot = -1;
        }
        if(next_ride_slot != -1) {
            rhythmMatrix[5][0] = 1.0;
            next_hh_slot = -1;
        }
        this.bar_time = bar_time; // in ms
        System.out.println("bar_time: " + bar_time);
    }

    //There are 6 functions called newKickTime, newSnareTime etc, which correspond separate inlets that take in the cpuclock time associated with a hit
    private void newKickTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_kick_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[0][(int) (val)] = 1.0;
            }
        }
    }

    private void newSnareTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_snare_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[1][(int) (val)] = 1.0;
            }
        }
    }

    private void newRimTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_rim_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[2][(int) (val)] = 1.0;
            }
        }
    }

    private void newHHTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_hh_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[3][(int) (val)] = 1.0;
            }
        }
    }

    private void newTomTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_tom_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[4][(int) (val)] = 1.0;
            }
        }
    }

    private void newRideTime(double event_time){
        //post("new event");
        double val;
        val = event_time % bar_time; // modulo time
        //System.out.println("val: " + val);
        val = val / ms_p_quant; // find the number of times the quant_step divides into the observed event time
        //System.out.println("val: " + val);
        val = java.lang.Math.round(val); // round the number to the nearest quantization step in the array
        //System.out.println("val: " + val);
        if (val == quantization_step) { // if the value is 16, this needs to be "snapped" to the beginning of the next bar
            next_ride_slot = 1;
        }
        else {
            if (!(val > quantization_step)) {
                this.rhythmMatrix[5][(int) (val)] = 1.0;
            }
        }
    }

    private void makeDecision(){
        int index;
        double min_dist;
        double[] distances = new double[rhythmPatterns.length];
        double[] rhythmVector = flatten_matrix(rhythmMatrix);

        distances = calc_dists(rhythmVector, rhythmPatterns);
        min_dist = getMinValue(distances);
        //System.out.println("min dist: " + min_dist);

        if (min_dist < threshold){ //threshold = 2.0 by default for drum session detector
            index = findSmallestIndex(distances);
            outlet(0, fileNames.get(index));
            System.out.println("index of min val: " + index);
            System.out.println("Pattern detected: " + fileNames.get(index));
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

    private void resetRhythmMatrix() {
        for(int i = 0; i < this.rhythmMatrix.length; i++){
            for(int j = 0; j < this.rhythmMatrix[0].length; j++) {
                this.rhythmMatrix[i][j] = 0.0;
            }
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

    private static void printVector(double[] vector) {
        //System.out.println("vector: ");
        for(int i = 0; i < vector.length; i++) {
            System.out.print(""+ vector[i] + " ");
        }
        System.out.println();
    }

    private void printRhythmPatterns() {
        //System.out.println("Rhythmpatterns: ");
        printFormattedMatrix(this.rhythmPatterns);
    }

    private void printRhythmMatrix() {
        //System.out.println("RhythmMatrix (KICK on top, RIDE on the bottom: ");
        for(int i = 0; i < this.rhythmMatrix.length; i++) {
            for(int j = 0; j < this.rhythmMatrix[i].length; j++){
                System.out.print(""+ this.rhythmMatrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    private static double[] flatten_matrix(double[][] matrix) {
        double[] flat_vector = new double[(matrix.length * matrix[0].length)];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                flat_vector[j + (i * matrix[0].length)] = matrix[i][j];
            }
        }
        return flat_vector;
    }

    private static void printFormattedMatrix(double[][] arr) {
        System.out.println("Formatted Matrix: ");
        for (int row = 0; row < arr.length; row++) {
            for (int col = 0; col < arr[0].length; col++) {
                System.out.printf("%9.4f", arr[row][col]);
            }
            System.out.printf("\n");
        }
    }

    /*
    public static void main (String[] args) throws InvalidMidiDataException, IOException{

        DrumPatternDetector myDetector = new DrumPatternDetector(16, 10.0);
        myDetector.setMsPerBeatQuant(428.571429);
        myDetector.printRhythmPatterns();
        myDetector.printRhythmMatrix();

        myDetector.newBarTime(512676.248876);

        myDetector.newKickTime(512676.248876);
        myDetector.newKickTime(512676.248876 + 2.5 * 428.571429);
        myDetector.newKickTime(512676.248876 + 3.5 * 428.571429);
        myDetector.newKickTime(512676.248876 + 3.99 * 428.571429);

        myDetector.newSnareTime(512676.248876 + 2 * 428.571429);
        myDetector.newSnareTime(512676.248876 + 3 * 428.571429);
        myDetector.newSnareTime(512676.248876 + 3.99 * 428.571429);

        myDetector.newHHTime(512676.248876);
        myDetector.newHHTime(512676.248876 + 0.5 * 428.571429);
        myDetector.newHHTime(512676.248876 + 1 * 428.571429);
        myDetector.newHHTime(512676.248876 + 1.5 * 428.571429);
        myDetector.newHHTime(512676.248876 + 2 * 428.571429);
        myDetector.newHHTime(512676.248876 + 2.5 * 428.571429);
        myDetector.newHHTime(512676.248876 + 3 * 428.571429);
        myDetector.newHHTime(512676.248876 + 3.5 * 428.571429);
        myDetector.newHHTime(512676.248876 + 3.99 * 428.571429);

        myDetector.newRimTime(512676.248876 + 1 * 428.571429);
        myDetector.newRimTime(512676.248876 + 2 * 428.571429);
        myDetector.newRimTime(512676.248876 + 3 * 428.571429);
        myDetector.newRimTime(512676.248876 + 3.99 * 428.571429);

        myDetector.printRhythmMatrix();
        myDetector.makeDecision();
        System.out.println();

        myDetector.newBarTime(512676.248876 + 4 * 428.571429);
        myDetector.printRhythmMatrix();
        System.out.println();

        myDetector.newBarTime(512676.248876 + 8 * 428.571429);
        myDetector.printRhythmMatrix();
        System.out.println();

    }
    */

}
