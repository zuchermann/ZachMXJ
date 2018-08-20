package Interaction1;

import com.leff.midi.MidiFile;
import com.leff.midi.MidiTrack;
import com.leff.midi.event.MidiEvent;
import com.leff.midi.event.NoteOn;
import com.leff.midi.event.meta.Tempo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by yn on 4/3/17.
 */

public class NGramParser {
    private static File[] files;
    private static RhythmList rhythmList;
    private static final double chordGroupTime = 40;

    public static void parse(String directoryPath, int nGramLength, Simple model, boolean isRhythm) throws IOException {
        files = new File(directoryPath).listFiles();
        System.out.println(directoryPath);
        rhythmList = new RhythmList(1);

        for (File file : files) {
            com.leff.midi.MidiFile midi = new MidiFile(file);
            for (MidiTrack track : midi.getTracks()) {

                Iterator<MidiEvent> it = track.getEvents().iterator();
                com.leff.midi.event.MidiEvent lastEvent = null;
                //System.out.println(midi.getLengthInTicks());

                LinkedList<Double> note = null;
                float noteTime = 0.f;
                double ms_per_tick = ((Tempo.DEFAULT_MPQN/1000) * midi.getResolution());
                while (it.hasNext()) {
                    MidiEvent event = it.next();
                    if (event instanceof Tempo) {
                        Tempo tempoEvent = (Tempo) event;
                        ms_per_tick = ((tempoEvent.getMpqn()/1000.) / (double) midi.getResolution());
                    }
                    if (event instanceof NoteOn) {
                        //System.out.println(((NoteOn) event).getNoteValue());
                        if (lastEvent != null) {
                            float lastEventTime = lastEvent.getTick();
                            float thisEventTime = event.getTick();
                            //System.out.println((ms_per_tick * (thisEventTime - lastEventTime)));
                            if ((ms_per_tick * (thisEventTime - lastEventTime)) <= chordGroupTime) {
                                //chord
                                note.add((double) ((NoteOn) event).getNoteValue());
                            } else {
                                //not chord
                                if (isRhythm) {
                                    rhythmList.inlet(thisEventTime - lastEventTime);
                                    double[] rhythmProp = rhythmList.getRhythmList();
                                    if (rhythmProp[0] > 0) model.insertOnlyDoubleList(rhythmList.getRhythmList());
                                } else {
                                    Collections.sort(note);
                                    Double[] noteList = new Double[note.size()];
                                    noteList = note.toArray(noteList);
                                    model.insertOnlyDoubleList(toPrimitiveArray(noteList));
                                }
                                note = new LinkedList<>();
                                note.add((double) ((NoteOn) event).getNoteValue());
                            }
                        }
                        if (note == null) {
                            //first note
                            note = new LinkedList<>();
                            note.add((double) ((NoteOn) event).getNoteValue());
                        }
                        lastEvent = event;
                    }
                }
                if(!isRhythm && note != null && note.size() > 0){
                    Double[] noteList = new Double[note.size()];
                    noteList = note.toArray(noteList);
                    model.insertOnlyDoubleList(toPrimitiveArray(noteList));
                }
            }
            model.clearValueQueue();
        }
        //System.out.println(model.getNGram());
    }

    private static double[] toPrimitiveArray(Double[] arr){
        double[] result = new double[arr.length];
        for(int i = 0; i < arr.length; i ++){
            result[i] = arr[i];
        }
        return result;
    }
}
