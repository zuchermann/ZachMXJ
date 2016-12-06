package PatternDetection;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Hanoi on 12/5/16.
 */
public class DrumSessionCentroids {
    public static final int NOTE_ON = 0x90;
    private static final int num_drum_components = 6;
    private static double NaN;

    public static double[][] parse(int quantization_step) throws InvalidMidiDataException, IOException {

        //File[] files = new File(workingDir + "/motif_midi_files").listFiles();
        File[] files = new File("src/PatternDetection/drum_midi_pattern").listFiles();
        //File[] files = new File("src/PatternDetection/rim_midi_patterns").listFiles();

        double[][] centroids;
        ArrayList<double[]> centroidList = new ArrayList<double[]>();
        //centroids = new double[files.length][quantization_step * num_drum_components]; // [Rows] = number of patterns in directory, [Columns] = quantization_step * number of drum components e.g 16 x 6

        //printMatrix(pattern_matrix);

        HashMap<Integer, Integer> drum_map = new HashMap<Integer, Integer>();
        drum_map.put(36, 0); // KICK NOTE = 36
        drum_map.put(38, 1); // SNARE NOTE = 38
        drum_map.put(40, 2); // RIM NOTE = 40
        drum_map.put(49, 3); // HH NOTE = 49
        drum_map.put(45, 4); // TOM NOTE = 45
        drum_map.put(46, 5); // RIDE NOTE = 46

        HashMap<Integer, String> drum_type = new HashMap<Integer, String>();
        drum_type.put(36, "KICK"); // KICK NOTE = 36
        drum_type.put(38, "SNARE"); // SNARE NOTE = 38
        drum_type.put(40, "RIM"); // RIM NOTE = 40
        drum_type.put(49, "HIHAT"); // HH NOTE = 49
        drum_type.put(45, "TOM"); // TOM NOTE = 45
        drum_type.put(46, "RIDE"); // RIDE NOTE = 46


        int file_counter = 0;
        double num_bars = 0;

        for (File file : files) {
            double[][] counter_matrix = new double[num_drum_components][quantization_step];


            if (file.getName().endsWith(".mid")) {
                System.out.println("+++++++++++++++++++++++++++++++++++++++++++");
                System.out.println("file_counter: " + file_counter);
                String drum_patt_name = file.getName();
                System.out.println("Pattern Name: " + drum_patt_name);
                Sequence sequence = MidiSystem.getSequence(file);
                double ticks_per_beat = sequence.getResolution();
                System.out.println("ticks_per_beat: " + ticks_per_beat);
                double ticks_per_quant = ticks_per_beat * 4.0 / (double) quantization_step; // There are 4 beats in a bar, then divide by quantization_step
                System.out.println("ticks_per_quant: " + ticks_per_quant);
                double midi_file_length = (sequence.getTickLength());
                System.out.println("midi file length (ticks): " + midi_file_length);
                double raw_num_bars = midi_file_length / (ticks_per_beat * 4.0);
                System.out.println("raw_num_bars " + raw_num_bars);
                num_bars = java.lang.Math.round(raw_num_bars);
                System.out.println("num_bars " + num_bars);
                //double overflow = num_bars * quantization_step;
                //System.out.println("overflow " + overflow);

                for (Track track : sequence.getTracks()) {
                    for (int i = 0; i < track.size(); i++) {
                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();
                        //System.out.println(message);
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            //System.out.println(sm);
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 0) { // first byte is what kind of message it is, getData2, gets the second byte.
                                //System.out.println("------ { new note } ------");
                                int note;
                                note = sm.getData1(); // Extract the event midi value
                                //System.out.println("Note: " + note);
                                String type = drum_type.get(note);
                                //System.out.println("Type: " + type);
                                double tick = event.getTick(); // Extract the event tick
                                //System.out.println("Tick: " + tick);
                                tick = tick / ticks_per_quant; // Find how many times the current quantization_step fits in the event tick, then round to closest quantization step value
                                //System.out.println("tick / ticks_per_quant: " + tick);
                                int quant_tick = (int) java.lang.Math.round(tick);
                                //System.out.println("quant_tick: " + quant_tick);
                                int mod_quant_tick = quant_tick % quantization_step;
                                //System.out.println("mod_quant_tick: " + mod_quant_tick);
                                counter_matrix[drum_map.get(note)][mod_quant_tick]++;
                            }
                        }
                    }
                }
                printFormattedMatrix(counter_matrix);
                System.out.println("num_bars: " + num_bars);
                double[][] centroid = calc_centroid(counter_matrix, num_bars);
                printFormattedMatrix(centroid);
                //System.out.println("flat centroid \n" + Arrays.toString(flat_centroid));
                double[] centroid_vector = flatten_matrix(centroid);
                printVector(centroid_vector);
                centroidList.add(centroid_vector);
                //centroids[file_counter] = centroid_vector;

                file_counter++;

            }
        }

        centroids = centroidList.toArray(new double[centroidList.size()][]);
        System.out.println("centroid rows = " + centroids.length);
        System.out.println("centroid columns = " + centroids[0].length);
        printFormattedMatrix(centroids);
        return centroids;
    }

    private static double[][] calc_centroid(double[][] array, double num_bars) {
        double[][] new_array = new double[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[0].length; j++) {
                new_array[i][j] = array[i][j] / num_bars;
            }
        }
        return new_array;
    }

    private static double[][] normalize(double[][] array) {
        double[][] new_array = new double[array.length][array[0].length];
        for (int i = 0; i < array.length; i++) {
            double row_sum = sumOfRow(array[i]);
            System.out.println("row sum: " + row_sum);
            for (int j = 0; j < array[0].length; j++) {
                new_array[i][j] = array[i][j] / row_sum;
            }
        }
        return new_array;
    }

    private static double getMaxValue(double[] inputArray) {
        double maxValue = inputArray[0];
        for (int i = 1; i < inputArray.length; i++) {
            if (inputArray[i] > maxValue) {
                maxValue = inputArray[i];
            }
        }
        return maxValue;
    }

    private static double sumOfRow(double[] row) {
        double sum = 0;
        for (double i : row) {
            sum += i;
        }
        return sum;
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


    private static void printVector(double[] vector) {
        System.out.println("Vector: ");
        for (int i = 0; i < vector.length; i++) {
            System.out.print("" + vector[i] + " ");
        }
        System.out.println();
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


    private static void printMatrix(double[][] matrix) {
        System.out.println("Matrix: ");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print("" + matrix[i][j] + "     ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main(String[] args) throws InvalidMidiDataException, IOException {
        DrumSessionCentroids.parse(16);
    }
}
