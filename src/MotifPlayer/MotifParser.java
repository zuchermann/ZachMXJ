package MotifPlayer;

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

public class MotifParser {
    public static final int NOTE_ON = 0x90;

    //temporary. Replace with Mason's path planning stuff when possible
    public static ArrayList<ArrayList<Integer>> getDirectControlList(File midiFile){
        String fileName = "files/" + midiFile.getName().substring(0, midiFile.getName().lastIndexOf('_')) + "_script.txt"; //temp

        BufferedReader br = null;
        String strLine = "";
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
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

    public static JSONObject parse(String workingDir) throws InvalidMidiDataException, IOException {
        JSONObject outputJSON = new JSONObject();
        JSONArray motifNames = new JSONArray();
        JSONObject motifs = new JSONObject();

        File[] files = new File(workingDir + "/motif_midi_files").listFiles();
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
                                JSONArray data = new JSONArray();
                                Integer[] newData = { 99, 99, 99, 99, sm.getData1(), 999,99 };//9s are placeHolders
                                data.addAll(Arrays.asList(newData));
                                long tick = event.getTick();
                                double beatOffsetLocation = tick/ppq;
                                String locationStr = Double.toString(beatOffsetLocation);
                                String[] mySplit = locationStr.split("\\.");
                                String beat = mySplit[0];
                                String offset = "0." + mySplit[1];
                                if(motif.containsKey(beat)) {
                                    JSONObject newMessage = new JSONObject();
                                    JSONArray newArray = new JSONArray();
                                    newMessage.put("controlList", data);
                                    newMessage.put("offset", Double.parseDouble(offset));
                                    ((JSONArray) motif.get(beat)).add(newMessage);
                                } else {
                                    JSONObject newMessage = new JSONObject();
                                    newMessage.put("controlList", data);
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
        /*try {
            File outputFile = new File(workingDir + "/motifs.json");
            outputFile.createNewFile();
            FileWriter jsonWriter = new FileWriter(outputFile);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            jsonWriter.write(gson.toJson(outputJSON));
            jsonWriter.flush();
            jsonWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        return outputJSON;
    }
}
