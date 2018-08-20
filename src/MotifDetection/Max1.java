package MotifDetection;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import java.util.ArrayList;

/**
 * Created by yn on 6/20/18.
 *
 * Interface  motif detection with Max/MSP
 */
public class Max1 extends MaxObject{
    //REPORTING_PERIOD is the time between samplings of the note sequence. This is used when converting
    //the series of discreet notes into a continuous signal in order to compute self-similarity.
    //Default represents a sampling pperiod of 100ms.
    private static final double REPORTING_PERIOD = 100;

    //MAX_TIME is the maximum time into the past that notes will be considered for motif detection.
    //Default od 60000ms represents 1min.
    private static final double MAX_TIME = 60000;

    private double detectionThreshold; //number below which a line will be considered detected
    private int minimumPitchCount; //number of unique pitches needed to consider a motif
    private int minimumNoteEvents; //number of notes needed (not unique, repeats count)
    private NoteSequence noteSequence; // where incoming notes are stored

    //historyLength number of notes to keep saved in the noteSequence
    Max1(double detectionThreshold, int minimumPitchCount, int minimumNoteEvents, int historyLength){
        this.detectionThreshold = detectionThreshold;
        this.minimumPitchCount = minimumPitchCount;
        this.minimumNoteEvents = minimumNoteEvents;
        this.noteSequence = new NoteSequence(historyLength);
        declareInlets(new int[]{ DataTypes.ALL});
        declareOutlets(new int[]{ DataTypes.ALL, DataTypes.ALL });
        setInletAssist(new String[] {"float: input pitches"});
        setOutletAssist(new String[] {"list: notes of motif in midi", "list: inter-onset intervals of motif in ms"});
    }

    public void inlet(float pitch){
        double now = System.currentTimeMillis();
        Note newNote = new Note((double) pitch, now);
        noteSequence.insertNote(newNote);
        ArrayList<Motif> motifs =  noteSequence.scoreQueueError(REPORTING_PERIOD, MAX_TIME, now);
        Motif final_motif = null; //store motif, only output the longest motif
        for(Motif m : motifs){
            if(m.getUniqueCount() >= minimumPitchCount){
                if(m.getNoteEventCount() >= minimumNoteEvents){
                    if(m.getErrorMeasure() <= detectionThreshold){
                        final_motif = m;
                    }
                }
            }
        }
        if(final_motif != null){
            outlet(1, final_motif.getRhythmsAtom(now));
            outlet(0, final_motif.getPitchesAtom());
        }
    }

}
