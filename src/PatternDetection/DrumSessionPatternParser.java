package PatternDetection;

/**
 * Created by Hanoi on 12/5/16.
 */

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.sound.midi.*;

public class DrumSessionPatternParser {
    public static final int NOTE_ON = 0x90;
    private static final int num_drum_components = 6;

    public static double[][] parse(int quantization_step) throws InvalidMidiDataException, IOException {

        //File[] files = new File(workingDir + "/motif_midi_files").listFiles();
        File[] files = new File("src/PatternDetection/drum_midi_pattern").listFiles();
        //File[] files = new File("src/PatternDetection/rim_midi_patterns").listFiles();

        double[][] centroids;
        centroids = new double[files.length][quantization_step * num_drum_components]; // [Rows] = number of patterns in directory, [Columns] = quantization_step * number of drum components e.g 16 x 6

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

        for (File file : files) {
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
                double num_bars = (int) java.lang.Math.round(raw_num_bars);
                System.out.println("num_bars " + num_bars);
                double overflow = num_bars * quantization_step;
                System.out.println("overflow " + overflow);

                double[] pattern_vector;
                pattern_vector = new double[(int) (num_drum_components * quantization_step * num_bars)];
                System.out.println("vector length: " + pattern_vector.length);

                for (Track track : sequence.getTracks()) {
                    for (int i = 0; i < track.size(); i++) {
                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();
                        //System.out.println(message);
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            //System.out.println(sm);
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 0) { // first byte is what kind of message it is, getData2, gets the second byte.
                                System.out.println("------ { new note } ------");
                                int note;
                                note = sm.getData1(); // Extract the event midi value
                                System.out.println("Note: " + note);
                                String type = drum_type.get(note);
                                System.out.println("Type: " + type);
                                double tick = event.getTick(); // Extract the event tick
                                System.out.println("Tick: " + tick);
                                tick = tick / ticks_per_quant; // Find how many times the current quantization_step fits in the event tick, then round to closest quantization step value
                                System.out.println("tick / ticks_per_quant: " + tick);
                                int quant_tick = (int) java.lang.Math.round(tick);
                                System.out.println("quant_tick: " + quant_tick);
                                int offset_2 = quant_tick / quantization_step;
                                System.out.println("offset_2: " + offset_2);
                                int shifted_quant_tick = quant_tick + (quantization_step * drum_map.get(note) +                                                                                                 (offset_2 * num_drum_components * quantization_step)); //Since we have made a 6 x 16 bar into a long 96 element vector, we simply add an offset corresponding to the drum value
                                System.out.println("shifted_quant_tick: " + shifted_quant_tick);

                                if (quant_tick == overflow) {// this is to catch the note at the very very end that is quantized to the next bar after num of bars. Simply ignore it
                                    //System.out.println("Deferred to next bar");
                                } else {
                                    //System.out.println("Quantized Tick: " + quant_tick);
                                    pattern_vector[shifted_quant_tick] = 1.0;
                                }
                            }
                        }

                    }
                }
                printVector(pattern_vector);
                printVectorFormatted(pattern_vector,quantization_step,1);
            }
            file_counter++;
        }

        return centroids;
    }

    private static void printVector(double[] vector) {
        System.out.println("Vector: ");
        for (int i = 0; i < vector.length; i++) {
            System.out.print("" + vector[i] + " ");
        }
        System.out.println();
    }

    private static void printVectorFormatted(double[] vector, int quantization_step,int bar_no) {
        System.out.println("Formatted Vector: ");
        for (int i = 0; i < num_drum_components; i++) {
            for (int j = 0; j < quantization_step; j++) {
                System.out.print("" + vector[j + (i * quantization_step)] + " ");

            }
            System.out.println();
        }

        System.out.println();
    }

    private static void printMatrix(double[][] matrix) {
        System.out.println("Matrix: ");
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print("" + matrix[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static void main (String[] args) throws InvalidMidiDataException, IOException{
        DrumSessionPatternParser.parse(16);
    }
}
