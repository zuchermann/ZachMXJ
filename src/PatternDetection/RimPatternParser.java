package PatternDetection;

/**
 * Created by Hanoi on 12/4/16.
 */

/**
 * Created by yn on 11/18/16.
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

import javax.sound.midi.*;


public class RimPatternParser {
    public static final int NOTE_ON = 0x90;

    public static double[][] parse(int quantization_step) throws InvalidMidiDataException, IOException {


        //File[] files = new File(workingDir + "/motif_midi_files").listFiles();
        File[] files = new File("src/PatternDetection/rim_midi_patterns").listFiles();

        double[][] pattern_matrix;
        pattern_matrix = new double[files.length][quantization_step];

        //printMatrix(pattern_matrix);

        int file_counter = 0;

        for (File file : files) {
            if (file.getName().endsWith(".mid")) {
                //System.out.println("---------------------------");
                //System.out.println("file_counter: " + file_counter);
                String rim_patt_name = file.getName();
                //System.out.println("Pattern Name: " + rim_patt_name);
                Sequence sequence = MidiSystem.getSequence(file);
                double ticks_per_beat = sequence.getResolution();
                //System.out.println("ticks_per_beat: " + ticks_per_beat);
                double ticks_per_quant = ticks_per_beat * 4.0 / (double) quantization_step; // There are 4 beats in a bar, then divide by quantization_step
                //System.out.println("ticks_per_quant: " + ticks_per_quant);

                for (Track track : sequence.getTracks()) {
                    long finalTick = 0;
                    int currentNote = 0;
                    for (int i = 0; i < track.size(); i++) {
                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();
                        //System.out.println(message);
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            //System.out.println(sm);
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 0) { // first byte is what kind of message it is, getData2, gets the second byte.
                                int note;
                                note = sm.getData1(); // Extract the event midi value
                                //System.out.println("Note: " + note);
                                double tick = event.getTick(); // Extract the event ick
                                //System.out.println("Tick: " + tick);
                                tick = tick / ticks_per_quant; // Find how many times the current quantization_step fits in the event tick, then round to closest quantization step value
                                //System.out.println("tick / ticks_per_quant: " + tick);
                                int quant_tick = (int) java.lang.Math.round(tick);
                                if (quant_tick == quantization_step) {
                                    //System.out.println("Deferred to next bar");
                                } else {
                                    //System.out.println("Quantized Tick: " + quant_tick);
                                    pattern_matrix[file_counter][quant_tick] = 1.0;
                                }
                            }
                        }
                    }
                }
            }
            file_counter++;
        }

        //printMatrix(pattern_matrix); // At the end of method, print the final matrix

        return pattern_matrix;
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
}