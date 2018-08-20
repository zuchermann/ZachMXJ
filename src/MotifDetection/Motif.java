package MotifDetection;

import com.cycling74.max.Atom;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by yn on 6/20/18.
 *
 * Representation of detected motifs.
 *
 * Main job of motif is to store the notes of a possibility as well as store likelihood information about motif
 *
 * Also handles conversion of motif to Atom array for Max communication.
 */
public class Motif {
    private ArrayList<Note> notes;
    private double errorMeasure;

    Motif(ArrayList<Note> notes, double errorMeasure){
        this.notes = notes;
        this.errorMeasure = errorMeasure;
    }

    private LinkedList<Double> getAllPitches(){
        LinkedList<Double> allPitches = new LinkedList<>();
        for (Note n : notes){
            allPitches.add(n.getPitch());
        }
        return allPitches;
    }

    public int getNoteEventCount(){
        return notes.size();
    }

    //returns number of unique notes in the note sequence
    public int getUniqueCount(){
        LinkedList<Double> uniqueNotes = new LinkedList<Double>();
        LinkedList<Double> allPitches = getAllPitches();
        for (Double d : allPitches){
            if (!uniqueNotes.contains(d)){
                uniqueNotes.add(d);
            }
        }
        return uniqueNotes.size();
    }

    //returns list of pitches as a max-compatible atom array
    public Atom[] getPitchesAtom(){
        Atom[] result = new Atom[notes.size() + 1];
        result[0] = Atom.newAtom("motif_pitches");
        for(int i = 0; i < notes.size(); i++){
            Note n = notes.get(i);
            result[i + 1] = Atom.newAtom(n.getPitch());
        }
        return result;
    }

    //pass in now because time between now and the last note represents the time to repeat the motif
    //or the time before the first note should be played
    public Atom[] getRhythmsAtom(double now){
        Atom[] result = new Atom[(notes.size() + 1)];
        double prevTime = notes.get(0).getTime();
        result[0] = Atom.newAtom("motif_rhythms");
        for(int i = 0; i < notes.size(); i++){
            Note n = notes.get(i);
            result[i + 1] = Atom.newAtom(n.getTime() - prevTime);
            prevTime = n.getTime();
        }
        result[1] = Atom.newAtom(now - prevTime);
        return result;
    }

    public double getErrorMeasure(){
        return errorMeasure;
    }

    public String toString(){
        return "[" + Integer.toString(notes.size()) + " note motif with error: " + Double.toString(errorMeasure)+"]";
    }
}
