package MotifPlayer;

/**
 * Created by yn on 11/18/16.
 */
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;

import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

public class MotifParser {
    public static final int NOTE_ON = 0x90;

    //temporary. Replace with Mason's path planning stuff when possible
    public static ArrayList getDirectControlList(File midiFile){
        String fileName = "files/" + midiFile.getName().substring(0, midiFile.getName().lastIndexOf('_')) + "_script.txt"; //temp

        BufferedReader br = null;
        String strLine = "";
        ArrayList<ArrayList<Integer>> result = new ArrayList();
        try {
            br = new BufferedReader( new FileReader(fileName));
            while( (strLine = br.readLine()) != null){
                String[] strings = strLine.split("\\s+");
                ArrayList<Integer> controlMessage = new ArrayList<Integer>();
                for(String s : strings) controlMessage.add(Integer.valueOf(s));
                result.add(controlMessage);
            }
        } catch (FileNotFoundException e) {
            System.err.println("Unable to find the file: " + fileName);
        } catch (IOException e) {
            System.err.println("Unable to read the file: " + fileName);
        }
        return result;
    }

    public static int nearestLength(double actualLength){
        int guess = 0;
        double diff = Math.abs(actualLength - guess);
        double prevDiff = Math.abs(actualLength - guess);
        int prevGuess = 0;
        while (guess < actualLength) {
            prevDiff = diff;
            prevGuess = guess;
            if (guess == 0) {
                guess = 4;
                diff = Math.abs(actualLength - guess);
            }
            else {
                guess = guess * 2;
                diff = Math.abs(actualLength - guess);
            }
        }
        if(prevDiff < diff){
            return prevGuess;
        }
        else {
            return guess;
        }
    }

    public static void main(String[] args) throws Exception {
        JSONObject outputJSON = new JSONObject();
        JSONArray motifNames = new JSONArray();
        JSONObject motifs = new JSONObject();

        File[] files = new File("files").listFiles();
        for (File file : files) {
            if (file.getName().endsWith(".mid")) {
                String motifName = file.getName();
                motifNames.add(file.getName());
                Sequence sequence = MidiSystem.getSequence(file);
                //ArrayList<ArrayList<Integer>> controlList = getDirectControlList(file);
                double ppq = sequence.getResolution();
                JSONObject motif = new JSONObject();
                for (Track track : sequence.getTracks()) {
                    long finalTick = 0;
                    int currentNote = 0;
                    for (int i = 0; i < track.size(); i++) {
                        MidiEvent event = track.get(i);
                        MidiMessage message = event.getMessage();
                        if (message instanceof ShortMessage) {
                            ShortMessage sm = (ShortMessage) message;
                            if (sm.getCommand() == NOTE_ON && sm.getData2() > 0) {
                                int[] data = {99, 99, 99, 99, sm.getData1(), 9999, 99};//9s are placeHolders
                                long tick = event.getTick();
                                double beatOffsetLocation = tick/ppq;
                                String locationStr = Double.toString(beatOffsetLocation);
                                String[] mySplit = locationStr.split("\\.");
                                String beat = mySplit[0];
                                String offset = "0." + mySplit[1];
                                if(motif.containsKey(beat)) {
                                    JSONObject newMessage = new JSONObject();
                                    newMessage.put("constrolList", data);
                                    newMessage.put("offset", Double.parseDouble(offset));
                                    ((JSONArray) motif.get(beat)).add(newMessage);
                                } else {
                                    JSONObject newMessage = new JSONObject();
                                    newMessage.put("constrolList", data);
                                    newMessage.put("offset", Double.parseDouble(offset));
                                    JSONArray control = new JSONArray();
                                    control.add(newMessage);
                                    motif.put(beat, control);
                                }
                                currentNote++;
                            }
                        }
                        else {
                            finalTick = event.getTick();
                        }
                    }
                    double finalBeat = nearestLength(finalTick/ppq);
                    motif.put("length", finalBeat);
                }
                motifs.put(motifName, motif);
            }
        }
        outputJSON.put("motifs", motifs);
        outputJSON.put("motifNames", motifNames);
        try {
            File outputFile = new File("output.json");
            outputFile.createNewFile();
            FileWriter jsonWriter = new FileWriter(outputFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            jsonWriter.write(gson.toJson(outputJSON));
            jsonWriter.flush();
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
