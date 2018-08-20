package MotifDetection;

import sun.awt.image.ImageWatched;

import java.lang.reflect.Array;
import java.nio.channels.Pipe;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by yn on 6/19/18.
 *
 * Queue of notes collected. The main job of this class is to keep a bunch of notes stored as note events
 * and convert it into a constant-sampled signal. Note sequence can be sampled at any sample rate.
 *
 * You can set a maximum number of notes to collect in the sequence, which behaves like a fifo queue.
 *
 * When converting the sequence into a signal, you can also set maximum time to look back.
 *
 * This class also contains functionality such as simple self-similarity based motif detection
 */
public class NoteSequence {
    private Queue<Note> noteQueue;
    private int numNotes;

    NoteSequence(int numNotes){
        this.numNotes = numNotes;
        noteQueue = new LinkedList<>();
    }

    public void insertNote(Note note){
        noteQueue.add(note);
        while(noteQueue.size() > numNotes){
            noteQueue.poll();
        }
    }

    public ArrayList<Note> filterNotes(double current_time, double max_time){
        return filterNotes(current_time, max_time, getNoteQueueAsList());
    }

    //filters the noteQueue so that it only contains notes that occurred at or after current_time - max_time
    public ArrayList<Note> filterNotes(double current_time, double max_time, ArrayList<Note> sublist){
        ArrayList<Note> filtered = new ArrayList<>();
        double min_time = current_time - max_time;
        for(Note n : sublist){
            if(n.getTime() >= min_time && n.getTime() < current_time){
                filtered.add(n);
            }
        }
        return filtered;
    }

    private ArrayList<Note> getNoteQueueAsList(){
        ArrayList<Note> theList = new ArrayList<>();
        theList.addAll(noteQueue);
        return theList;
    }

    public ArrayList<Double> generateSignal(double reporting_period, double max_time, double current_time){
        return  generateSignal(reporting_period, max_time, current_time, getNoteQueueAsList());
    }

    //converts a series of discreet note events into a pitch/time signal.
    //
    //argument reporting_period is how often you want to sample the note-
    //event list, and is specified in milliseconds (ei 20. will sample
    //every 20ms or with a sampling frequency of 50Hz).
    //
    //max time is how much time you want to look back. This is also specified
    //in milliseconds. So if you specified max_time to be 5 seconds (5000ms)
    //and a note occured more than 5 seconds ago, this note will not appear
    //in the signal output. -1 sets to no max_time, consider all notes regardless
    //
    //Current time is the current time in milliseconds
    //
    //returns an an array list representing signal version of notes. This
    //may need to be expanded to allow for polyphony; to do this you would
    //probably also need to expand the Note class to account for note-offs.
    public ArrayList<Double> generateSignal(double reporting_period, double max_time, double current_time, ArrayList<Note> sublist){
        ArrayList<Note> filtered_notes = sublist;
        if(max_time != -1.0) {
            filtered_notes = filterNotes(current_time, max_time, sublist);
        }
        ArrayList<Double> noteSignal = new ArrayList<>();
        boolean is_first = true;
        double sample_time = -1.0;

        int numNotes = filtered_notes.size();
        for(int i = 0; i < numNotes; i ++){
            Note n = filtered_notes.get(i);
            double nextEvent = i == numNotes - 1 ? current_time : filtered_notes.get(i + 1).getTime();
            if(is_first){
                is_first = false;
                sample_time =  n.getTime();
            }
            while(sample_time < nextEvent){
                noteSignal.add(n.getPitch());
                sample_time += reporting_period;
            }
        }
        return  noteSignal;
    }

    // returns an ArrayList of the most recent num_notes notes in notes
    public ArrayList<Note> getSubList(ArrayList<Note> notes, int num_notes){
        ArrayList<Note> sublist = new ArrayList<>();
        for(int i = notes.size() - num_notes; i < notes.size(); i++){
            sublist.add(notes.get(i));
        }
        //System.out.println(sublist);
        return sublist;
    }

    //return a list of motifs, containing their respective half-way self-similarity error metrics.
    public ArrayList<Motif> scoreQueueError(double reporting_period, double max_time, double current_time){
        ArrayList<Note> filtered_notes = filterNotes(current_time, max_time);
        ArrayList<Motif> result = new ArrayList<>();
        for(int i = 1; i <= filtered_notes.size(); i++){
            ArrayList<Note> sublist = getSubList(filtered_notes, i);
            ArrayList<Double> noteSignal = generateSignal(reporting_period, -1.0, current_time, sublist);
            //System.out.println(noteSignal);
            if((noteSignal.size() % 2) != 0){
                double last_val = noteSignal.get(noteSignal.size() - 1);
                noteSignal.add(last_val);
            }
            int split_size = noteSignal.size() / 2;
            ArrayList<Double> signal_a = new ArrayList<>();
            ArrayList<Double> signal_b = new ArrayList<>();
            for(int j = 0; j < noteSignal.size(); j++){
                if(j < split_size){
                    signal_a.add(noteSignal.get(j));
                } else {
                    signal_b.add(noteSignal.get(j));
                }
            }
            if(split_size != 0) {
                double errorScore = getAverageError(signal_a, signal_b);
                Motif this_motif = new Motif(sublist, errorScore);
                result.add(this_motif);
            }
        }
        return result;
    }

    //takes int two arraylists representing signals. Returns the pairwise average error between two signals.
    //Both input lists must be the same length, otherwise will throw an error.
    //Neither list can have size of zero, or error will be thrown
    public static double getAverageError(ArrayList<Double> signal_a, ArrayList<Double> signal_b){
        if(signal_a.size() == 0 || signal_b.size() == 0){
            throw new AssertionError("getAverageError cannot be called on lists with size of zero");
        }
        else if(signal_a.size() != signal_b.size()){
            throw new AssertionError("getAverageError expects ArrayLists of equal size, got sizes of "
                    + Integer.toString(signal_a.size()) + " and "
                    + Integer.toString(signal_b.size()));
        } else {
            int len = signal_a.size();
            double error_sum = 0.0;
            for(int i = 0; i < len; i++){
                error_sum += Math.abs(signal_a.get(i) - signal_b.get(i));
                //System.out.println(""+signal_a.get(i) +" "+signal_b.get(i));
            }
            return (error_sum/(double)len);
        }
    }

    public String toString(){
        String str = "";
        for(Note n : noteQueue) {
            str = str + Double.toString(n.getPitch()) + " ";
        }
        return str;
    }

    //main method for simple testing only
    public static void main(String[] args){
        double now = System.currentTimeMillis();
        Note note1 = new Note(60, now + 0);
        Note note2 = new Note(61, now + 1000);
        Note note3 = new Note(62, now + 3000);
        Note note4 = new Note(63, now + 4000);
        Note note5 = new Note(61, now + 5000);
        Note note6 = new Note(62, now + 7000);
        Note note7 = new Note(63, now + 8000);
        Note note8 = new Note(60, now + 9000);
        NoteSequence newSequence = new NoteSequence(10);
        newSequence.insertNote(note1);
        System.out.println(newSequence);
        newSequence.insertNote(note2);
        System.out.println(newSequence);
        newSequence.insertNote(note3);
        System.out.println(newSequence);
        newSequence.insertNote(note4);
        System.out.println(newSequence);
        newSequence.insertNote(note5);
        System.out.println(newSequence);
        newSequence.insertNote(note6);
        System.out.println(newSequence);
        newSequence.insertNote(note7);
        System.out.println(newSequence);
        newSequence.insertNote(note8);
        System.out.println(newSequence);

        System.out.println(newSequence.scoreQueueError(100, 30000, now + 9000));


        System.out.println(newSequence.generateSignal(100, 2000, now+9000));
    }
}
