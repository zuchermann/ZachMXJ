package MotifGen;

import com.leff.midi.*;
import Interaction1.*;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOff;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;
import com.leff.midi.event.meta.TimeSignature;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by yn on 1/23/17.
 */
public class NGramGen {

    public static final int MAX_LEN = 3600;
    public static final int OUTPUT_NUM = 1000;
    private static Class doubleClass = (new Double(0)).getClass();

    public static void main(String[] args) throws IOException {
        NGram pitchNGram = new NGram<Double>(doubleClass);
        NGram rhythmNGram = new NGram<Double>(doubleClass);

        HashMap<Integer, Double> startCounts = new HashMap<>();
        HashMap<Double, Double> startRhythmCounts = new HashMap<>();
        double noteCount = 0.0;

        File[] files = new File("src/MotifGen/motif_midi_files").listFiles();
        for (File file : files) {
            com.leff.midi.MidiFile midi = new MidiFile(file);
            MidiTrack track = midi.getTracks().get(0);

            Iterator<MidiEvent> it = track.getEvents().iterator();
            double lastEvent = -1;
            double lastLastEvent = -1;
            double lastNoteValue = -1;
            //System.out.println(midi.getLengthInTicks());

            while(it.hasNext())
            {
                MidiEvent event = it.next();

                if(event instanceof NoteOn)
                {
                    if(lastEvent != -1 && lastLastEvent != -1){
                        double duration1 = lastEvent - lastLastEvent;
                        double duration2 = event.getTick() - lastEvent;
                        LinkedList<Double> rhythm = new LinkedList<>();
                        rhythm.add(0, duration1);
                        rhythm.add(1, duration2);
                        rhythmNGram.insert(rhythm);
                        double note1 = lastNoteValue;
                        double note2 = ((NoteOn) event).getNoteValue();
                        LinkedList<Double> melody = new LinkedList<>();
                        melody.add(0, note1);
                        melody.add(1, note2);
                        pitchNGram.insert(melody);
                    } else if (lastEvent == -1 && lastLastEvent == -1) {
                        int startNote = ((NoteOn) event).getNoteValue();
                        if(startCounts.containsKey(startNote)) {
                            startCounts.replace(startNote, startCounts.get(startNote) + 1.0);
                        } else {
                            startCounts.put(startNote, 1.0);
                        }
                        noteCount += 1.0;
                    } else {
                        double startRhythm = ((NoteOn) event).getTick() - lastEvent;
                        if(startRhythmCounts.containsKey(startRhythm)) {
                            startRhythmCounts.replace(startRhythm, startRhythmCounts.get(startRhythm) + 1.0);
                        } else {
                            startRhythmCounts.put(startRhythm, 1.0);
                        }
                        noteCount += 1.0;
                    }
                    lastNoteValue = ((NoteOn) event).getNoteValue();
                    lastLastEvent = lastEvent;
                    lastEvent = (double) event.getTick();
                }
            }
        }

        for(Double key : startRhythmCounts.keySet()){
            startRhythmCounts.replace(key, startRhythmCounts.get(key)/noteCount);
        }

        for(Integer key : startCounts.keySet()){
            startCounts.replace(key, startCounts.get(key)/noteCount);
        }

        for (int i = 0; i < OUTPUT_NUM; i ++) {

            //int currentTick = 0;
            Random r = new Random();
            double randomValue = r.nextDouble();
            //System.out.println(randomValue);
            double min = 999.0;
            double lastRhythm = -1;
            for (Double key : startRhythmCounts.keySet()) {
                if (min == 999.0 || Math.abs(startRhythmCounts.get(key) - randomValue) < min) {
                    min = Math.abs(startRhythmCounts.get(key) - randomValue);
                    lastRhythm = key;
                }
            }
            randomValue = r.nextDouble();
            //System.out.println(randomValue);
            min = 999.0;
            double lastNote = -1;
            for (Integer key : startCounts.keySet()) {
                if (min == 999.0 || Math.abs(startCounts.get(key) - randomValue) < min) {
                    min = Math.abs(startCounts.get(key) - randomValue);
                    lastNote = key;
                }
            }

            MidiTrack tempoTrack = new MidiTrack();
            MidiTrack noteTrack = new MidiTrack();

            TimeSignature ts = new TimeSignature();
            ts.setTimeSignature(4, 4, TimeSignature.DEFAULT_METER, TimeSignature.DEFAULT_DIVISION);

            Tempo tempo = new Tempo();
            tempo.setBpm(140);

            tempoTrack.insertEvent(ts);
            tempoTrack.insertEvent(tempo);

            int channel = 0;
            int pitch = (int) lastNote;
            int velocity = 127;
            long tick = 0;
            long duration = (long) lastRhythm;

            //System.out.println(lastNote);
            noteTrack.insertNote(channel, pitch, velocity, tick, duration);
            tick += duration;

            while (tick < MAX_LEN) {
                ArrayList<Double> lastPitchList = new ArrayList<>();
                lastPitchList.add(lastNote);
                double nextNote = (Double) pitchNGram.predict(lastPitchList);
                ArrayList<Double> lastRhythmList = new ArrayList<>();
                lastRhythmList.add(lastRhythm);
                double nextRhythm = (Double) rhythmNGram.predict(lastRhythmList);

                channel = 0;
                pitch = (int) nextNote;
                velocity = 127;
                duration = (long) nextRhythm;
                //System.out.println(pitch);

                noteTrack.insertNote(channel, pitch, velocity, tick, duration);
                tick += duration;
                //System.out.println(tick);

                lastNote = nextNote;
                lastRhythm = nextRhythm;
            }

            ArrayList<MidiTrack> tracks = new ArrayList<MidiTrack>();
            tracks.add(tempoTrack);
            tracks.add(noteTrack);

            //System.out.println(rhythmNGram);

            MidiFile midi = new MidiFile(MidiFile.DEFAULT_RESOLUTION, tracks);

            File output = new File("src/MotifGen/output/out" + Integer.toString(i) + ".mid");
            midi.writeToFile(output);

            //System.out.println("path " + output.getAbsolutePath());
        }
    }
}
